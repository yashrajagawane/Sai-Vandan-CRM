package com.saivandan.crm.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;

@Service
public class LocalFileStorageService implements FileStorageService {
  private final Path root;
  public LocalFileStorageService(@Value("${app.storage.root:storage}") String root){this.root=Paths.get(root).toAbsolutePath().normalize();}
  @Override public String store(String key, InputStream content) throws IOException {String safe=key.replace('\\','/').replaceAll("\\.\\.","");Path target=root.resolve(safe).normalize();if(!target.startsWith(root))throw new IOException("Invalid storage key");Files.createDirectories(target.getParent());Files.copy(content,target,StandardCopyOption.REPLACE_EXISTING);return safe;}
  @Override public InputStream open(String key) throws IOException {String safe=key.replace('\\','/').replaceAll("\\.\\.","");Path target=root.resolve(safe).normalize();if(!target.startsWith(root)||!Files.exists(target))throw new FileNotFoundException("Stored file not found");return Files.newInputStream(target);}
}
