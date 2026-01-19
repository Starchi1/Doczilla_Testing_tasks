package org.fileshare.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.fileshare.model.FileInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileMetadataService {
    private static final String METADATA_FILE = "metadata.json";
    private static final long EXPIRY_DAYS = 30;
    private static final long EXPIRY_MILLIS = EXPIRY_DAYS * 24 * 60 * 60 * 1000L;

    private final Map<String, FileInfo> fileRegistry = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileMetadataService() {
        loadMetadata();
    }

    public void saveMetadata() {
        try {
            String json = gson.toJson(fileRegistry);
            Files.writeString(Paths.get(METADATA_FILE), json);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения метаданных: " + e.getMessage());
        }
    }

    private void loadMetadata() {
        Path path = Paths.get(METADATA_FILE);
        if (!Files.exists(path)) {
            return;
        }

        try {
            String content = Files.readString(path);
            Type type = new TypeToken<Map<String, FileInfo>>(){}.getType();
            Map<String, FileInfo> loaded = gson.fromJson(content, type);
            if (loaded != null) {
                fileRegistry.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки метаданных: " + e.getMessage());
            // Создаем резервную копию поврежденного файла
            try {
                Files.move(path, Paths.get(METADATA_FILE + ".backup_" + System.currentTimeMillis()));
            } catch (IOException ex) {
                // Игнорируем ошибку резервного копирования
            }
        }
    }

    public void cleanupExpiredFiles() {
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, FileInfo> entry : fileRegistry.entrySet()) {
            if (now - entry.getValue().getLastAccessTime() > EXPIRY_MILLIS) {
                toRemove.add(entry.getKey());
            }
        }

        for (String id : toRemove) {
            fileRegistry.remove(id);
            System.out.println("Удалён устаревший файл из реестра: " + id);
        }

        if (!toRemove.isEmpty()) {
            saveMetadata();
        }
    }

    public void registerFile(FileInfo fileInfo) {
        fileRegistry.put(fileInfo.getId(), fileInfo);
        saveMetadata();
    }

    public FileInfo getFileInfo(String id) {
        return fileRegistry.get(id);
    }

    public FileInfo findFileByOriginalName(String originalName) {
        for (FileInfo info : fileRegistry.values()) {
            if (info.getOriginalName().equals(originalName)) {
                return info;
            }
        }
        return null;
    }

    public void updateFileAccess(String id) {
        FileInfo info = fileRegistry.get(id);
        if (info != null) {
            info.setLastAccessTime(System.currentTimeMillis());
            info.incrementDownloadCount();
            saveMetadata();
        }
    }

    public void deleteFileInfo(String id) {
        fileRegistry.remove(id);
        saveMetadata();
    }

    public List<FileInfo> getAllFiles() {
        return new ArrayList<>(fileRegistry.values());
    }
}