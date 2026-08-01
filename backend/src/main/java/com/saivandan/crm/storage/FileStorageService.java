package com.saivandan.crm.storage;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {
  String store(String key, InputStream content) throws IOException;
  InputStream open(String key) throws IOException;
}
