package com.saivandan.crm.inventory;

import com.saivandan.crm.security.AuditService;
import com.saivandan.crm.security.CurrentUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
  private final JdbcTemplate jdbc; private final AuditService audit;
  public InventoryController(JdbcTemplate jdbc, AuditService audit){this.jdbc=jdbc;this.audit=audit;}

  @GetMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
  public List<Map<String,Object>> inventory(@RequestParam(required=false) UUID projectId, @RequestParam(required=false) String status, @RequestParam(required=false) String wing, @RequestParam(required=false) String configuration){
    releaseExpired();
    StringBuilder sql=new StringBuilder("select u.id,u.project_id as projectId,p.code as projectCode,p.name as projectName,u.wing,u.floor,u.unit_number as unitNumber,u.configuration,u.carpet_area as carpetArea,u.built_up_area as builtUpArea,u.base_price as basePrice,u.facing,u.parking,u.amenities,u.status,u.reserved_until as reservedUntil from units u join projects p on p.id=u.project_id where u.deleted_at is null");
    List<Object> args=new ArrayList<>(); if(projectId!=null){sql.append(" and u.project_id=?");args.add(projectId);} if(status!=null&&!status.isBlank()){sql.append(" and u.status=?");args.add(status.toUpperCase());} if(wing!=null&&!wing.isBlank()){sql.append(" and u.wing=?");args.add(wing);} if(configuration!=null&&!configuration.isBlank()){sql.append(" and u.configuration=?");args.add(configuration);} sql.append(" order by p.code,u.wing,cast(u.floor as int),u.unit_number");
    return jdbc.queryForList(sql.toString(),args.toArray());
  }

  @GetMapping("/projects") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
  public List<Map<String,Object>> projects(){return jdbc.queryForList("select id,code,name,city,address,status from projects order by name");}

  @PostMapping("/projects") @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> createProject(@Valid @RequestBody ProjectRequest req,@AuthenticationPrincipal CurrentUser current){
    UUID id=UUID.randomUUID(); jdbc.update("insert into projects(id,code,name,city,address,status) values (?,?,?,?,?,?)",id,req.code(),req.name(),req.city(),req.address(),req.status()==null?"ACTIVE":req.status()); audit.record(current.user().getId(),"PROJECT",id,"CREATE",null,req.name(),null); return project(id);
  }

  @PutMapping("/projects/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> updateProject(@PathVariable UUID id,@Valid @RequestBody ProjectRequest req,@AuthenticationPrincipal CurrentUser current){
    Map<String,Object> before=project(id); int changed=jdbc.update("update projects set code=?,name=?,city=?,address=?,status=?,updated_at=current_timestamp where id=?",req.code(),req.name(),req.city(),req.address(),req.status()==null?"ACTIVE":req.status().toUpperCase(),id); if(changed==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found."); audit.record(current.user().getId(),"PROJECT",id,"UPDATE",before.toString(),req.name(),null); return project(id);
  }

  @GetMapping("/projects/{id}/structure") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
  public Map<String,Object> structure(@PathVariable UUID id){
    project(id);
    return Map.of("project",project(id),"wings",jdbc.queryForList("select w.id,w.code,w.name,count(f.id) as floors from project_wings w left join project_floors f on f.wing_id=w.id where w.project_id=? group by w.id,w.code,w.name order by w.code",id));
  }

  @PostMapping("/projects/{projectId}/wings") @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> createWing(@PathVariable UUID projectId,@Valid @RequestBody WingRequest req,@AuthenticationPrincipal CurrentUser current){
    project(projectId); UUID id=UUID.randomUUID(); try {jdbc.update("insert into project_wings(id,project_id,code,name) values (?,?,?,?)",id,projectId,req.code(),req.name());} catch(Exception ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Wing code already exists in this project.");} audit.record(current.user().getId(),"PROJECT_WING",id,"CREATE",null,req.code(),null); return jdbc.queryForMap("select id,code,name from project_wings where id=?",id);
  }

  @PostMapping("/wings/{wingId}/floors") @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> createFloor(@PathVariable UUID wingId,@Valid @RequestBody FloorRequest req,@AuthenticationPrincipal CurrentUser current){
    UUID id=UUID.randomUUID(); try {jdbc.update("insert into project_floors(id,wing_id,floor_number,label) values (?,?,?,?)",id,wingId,req.floorNumber(),req.label());} catch(Exception ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Floor already exists in this wing.");} audit.record(current.user().getId(),"PROJECT_FLOOR",id,"CREATE",null,String.valueOf(req.floorNumber()),null); return jdbc.queryForMap("select id,wing_id as wingId,floor_number as floorNumber,label from project_floors where id=?",id);
  }

  @PostMapping("/units") @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> createUnit(@Valid @RequestBody UnitRequest req,@AuthenticationPrincipal CurrentUser current){
    UUID id=UUID.randomUUID(); try {jdbc.update("insert into units(id,project_id,wing,floor,unit_number,configuration,carpet_area,built_up_area,base_price,facing,parking,amenities,status) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",id,req.projectId(),req.wing(),req.floor(),req.unitNumber(),req.configuration(),req.carpetArea(),req.builtUpArea(),req.basePrice(),req.facing(),req.parking(),req.amenities(),req.status()==null?"AVAILABLE":req.status().toUpperCase());} catch(Exception ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Unit number already exists in this project.");}
    jdbc.update("insert into unit_price_history(unit_id,new_price,reason,changed_by) values (?,?,?,?)",id,req.basePrice(),"Initial price",current.user().getId()); jdbc.update("insert into unit_status_history(unit_id,new_status,reason,changed_by) values (?,?,?,?)",id,req.status()==null?"AVAILABLE":req.status().toUpperCase(),"Initial availability",current.user().getId()); audit.record(current.user().getId(),"UNIT",id,"CREATE",null,req.unitNumber(),null); return unit(id);
  }

  @PutMapping("/units/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Transactional
  public Map<String,Object> updateUnit(@PathVariable UUID id,@Valid @RequestBody UnitRequest req,@AuthenticationPrincipal CurrentUser current){
    Map<String,Object> before=unit(id); String oldStatus=String.valueOf(value(before,"status")); BigDecimal oldPrice=decimal(value(before,"baseprice","basePrice"));
    String nextStatus=req.status()==null?oldStatus:req.status().toUpperCase(); int changed=jdbc.update("update units set wing=?,floor=?,unit_number=?,configuration=?,carpet_area=?,built_up_area=?,base_price=?,facing=?,parking=?,amenities=?,status=?,reserved_until=?,updated_at=current_timestamp where id=? and deleted_at is null",req.wing(),req.floor(),req.unitNumber(),req.configuration(),req.carpetArea(),req.builtUpArea(),req.basePrice(),req.facing(),req.parking(),req.amenities(),nextStatus,req.reservedUntil(),id); if(changed==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unit not found.");
    if(oldPrice==null||req.basePrice().compareTo(oldPrice)!=0)jdbc.update("insert into unit_price_history(unit_id,old_price,new_price,reason,changed_by) values (?,?,?,?,?)",id,oldPrice,req.basePrice(),"Price update",current.user().getId()); if(!oldStatus.equals(nextStatus))jdbc.update("insert into unit_status_history(unit_id,old_status,new_status,reason,changed_by) values (?,?,?,?,?)",id,oldStatus,nextStatus,"Availability update",current.user().getId()); audit.record(current.user().getId(),"UNIT",id,"UPDATE",before.toString(),req.unitNumber(),null); return unit(id);
  }

  @PutMapping("/units/{id}/price") @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Transactional
  public Map<String,Object> updatePrice(@PathVariable UUID id,@Valid @RequestBody PriceRequest req,@AuthenticationPrincipal CurrentUser current){
    Map<String,Object> before=unit(id); BigDecimal oldPrice=decimal(value(before,"baseprice","basePrice")); if(oldPrice!=null&&oldPrice.compareTo(req.price())==0) return before; int changed=jdbc.update("update units set base_price=?,updated_at=current_timestamp where id=? and deleted_at is null",req.price(),id); if(changed==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unit not found."); jdbc.update("insert into unit_price_history(unit_id,old_price,new_price,reason,changed_by) values (?,?,?,?,?)",id,oldPrice,req.price(),req.reason(),current.user().getId()); audit.record(current.user().getId(),"UNIT",id,"PRICE_UPDATE",before.toString(),req.price().toPlainString(),null); return unit(id);
  }

  @PostMapping("/units/{id}/reserve") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER')")
  @Transactional
  public Map<String,Object> reserve(@PathVariable UUID id,@RequestBody ReservationRequest req,@AuthenticationPrincipal CurrentUser current){
    releaseExpired(); int hours=Math.max(1,Math.min(req.hours()==null?48:req.hours(),168)); Instant expiry=Instant.now().plusSeconds(hours*3600L);
    int changed=jdbc.update("update units set status='RESERVED',reserved_until=? where id=? and deleted_at is null and (status='AVAILABLE' or (status='RESERVED' and reserved_until<current_timestamp))",expiry,id); if(changed==0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Unit is not available for reservation.");
    jdbc.update("update unit_reservations set status='EXPIRED' where unit_id=? and status='ACTIVE'",id); UUID rid=UUID.randomUUID(); jdbc.update("insert into unit_reservations(id,unit_id,reserved_by,expires_at) values (?,?,?,?)",rid,id,current.user().getId(),expiry); jdbc.update("insert into unit_status_history(unit_id,old_status,new_status,reason,changed_by) values (?,?,?,?,?)",id,"AVAILABLE","RESERVED","Reserved for sales workflow",current.user().getId()); audit.record(current.user().getId(),"UNIT",id,"RESERVE",null,"expires="+expiry,null); return unit(id);
  }

  @PostMapping("/units/{id}/release") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER')")
  @Transactional
  public Map<String,Object> release(@PathVariable UUID id,@AuthenticationPrincipal CurrentUser current){Map<String,Object> before=unit(id); int changed=jdbc.update("update units set status='AVAILABLE',reserved_until=null where id=? and status='RESERVED'",id); if(changed==0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Unit is not currently reserved."); jdbc.update("update unit_reservations set status='RELEASED' where unit_id=? and status='ACTIVE'",id); jdbc.update("insert into unit_status_history(unit_id,old_status,new_status,reason,changed_by) values (?,?,?,?,?)",id,"RESERVED","AVAILABLE","Reservation released",current.user().getId()); audit.record(current.user().getId(),"UNIT",id,"RELEASE",before.toString(),"AVAILABLE",null); return unit(id);}

  @GetMapping("/units/{id}/history") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
  public Map<String,Object> history(@PathVariable UUID id){return Map.of("priceHistory",jdbc.queryForList("select old_price as oldPrice,new_price as newPrice,reason,changed_at as changedAt from unit_price_history where unit_id=? order by changed_at desc",id),"statusHistory",jdbc.queryForList("select old_status as oldStatus,new_status as newStatus,reason,changed_at as changedAt from unit_status_history where unit_id=? order by changed_at desc",id));}

  private void releaseExpired(){jdbc.update("update units set status='AVAILABLE',reserved_until=null where status='RESERVED' and reserved_until<current_timestamp"); jdbc.update("update unit_reservations set status='EXPIRED' where status='ACTIVE' and expires_at<current_timestamp");}
  private Map<String,Object> project(UUID id){return jdbc.queryForMap("select id,code,name,city,address,status from projects where id=?",id);}
  private Map<String,Object> unit(UUID id){return jdbc.queryForMap("select u.id,u.project_id as projectId,p.code as projectCode,p.name as projectName,u.wing,u.floor,u.unit_number as unitNumber,u.configuration,u.carpet_area as carpetArea,u.built_up_area as builtUpArea,u.base_price as basePrice,u.facing,u.parking,u.amenities,u.status,u.reserved_until as reservedUntil from units u join projects p on p.id=u.project_id where u.id=?",id);}
  private Object value(Map<String,Object> row,String... keys){for(String key:keys){if(row.containsKey(key))return row.get(key); for(String actual:row.keySet())if(actual.equalsIgnoreCase(key))return row.get(actual);}return null;}
  private BigDecimal decimal(Object value){return value==null?null:value instanceof BigDecimal b?b:new BigDecimal(value.toString());}
  public record ProjectRequest(@NotBlank String code,@NotBlank String name,String city,String address,String status){}
  public record WingRequest(@NotBlank String code,@NotBlank String name){}
  public record FloorRequest(@NotNull Integer floorNumber,String label){}
  public record UnitRequest(@NotNull UUID projectId,@NotBlank String wing,@NotBlank String floor,@NotBlank String unitNumber,@NotBlank String configuration,@NotNull BigDecimal carpetArea,@NotNull BigDecimal builtUpArea,@NotNull BigDecimal basePrice,String facing,String parking,String amenities,String status,Instant reservedUntil){}
  public record PriceRequest(@NotNull BigDecimal price,@NotBlank String reason){}
  public record ReservationRequest(Integer hours){}
}
