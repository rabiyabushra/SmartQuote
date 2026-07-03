// StorageService.java
package com.smartquote.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

/**
 * File storage abstraction. Currently backed by local disk (no cloud
 * provider chosen yet in the project docs — see class-level note). Every
 * other service calls only uploadFile()/deleteFile(), so swapping the
 * backend later is contained entirely to this file.
 */
@Service
public class StorageService {

    @Value("${storage.local.base-dir:./storage}")
    private String baseDir;

    @Value("${storage.base-url:http://localhost:8080/files}")
    private String baseUrl;

    /** Saves the file and returns a URL the frontend can use to download/preview it. */
    public String uploadFile(byte[] content, String fileName, String contentType) {
        try {
            Path dir = Paths.get(baseDir);
            Files.createDirectories(dir);

            String safeName = sanitize(fileName);
            String storedName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8) + "-" + safeName;

            Path target = dir.resolve(storedName);
            Files.write(target, content);

            return baseUrl + "/" + storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file '" + fileName + "': " + e.getMessage(), e);
        }
    }

    /** Deletes a file previously returned by uploadFile(), given its stored URL. Safe no-op if already gone. */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return;
        }
        String storedName = fileUrl.substring(baseUrl.length() + 1);
        try {
            Files.deleteIfExists(Paths.get(baseDir).resolve(storedName));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file '" + fileUrl + "': " + e.getMessage(), e);
        }
    }

    /** Strips path separators and unsafe characters so a crafted filename can't escape baseDir. */
    private String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) return "file";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}