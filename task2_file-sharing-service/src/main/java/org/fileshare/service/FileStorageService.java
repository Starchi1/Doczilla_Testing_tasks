package org.fileshare.service;


import java.io.*;
import java.nio.file.*;
import java.util.UUID;

public class FileStorageService {
    private static final String UPLOAD_DIR = "uploads";
    private final Path uploadPath;

    public FileStorageService() throws IOException {
        this.uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
    }

    public String saveFile(byte[] fileData, String originalFilename) throws IOException {
        String id = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        String savedFilename = id + extension;

        Path filePath = uploadPath.resolve(savedFilename);
        Files.write(filePath, fileData);

        return id;
    }

    public byte[] readFile(String filename) throws IOException {
        Path filePath = uploadPath.resolve(filename);
        return Files.readAllBytes(filePath);
    }

    public boolean deleteFile(String filename) {
        try {
            return Files.deleteIfExists(uploadPath.resolve(filename));
        } catch (IOException e) {
            System.err.println("Ошибка удаления файла: " + e.getMessage());
            return false;
        }
    }

    public long getFileSize(String filename) throws IOException {
        Path filePath = uploadPath.resolve(filename);
        return Files.size(filePath);
    }

    public boolean fileExists(String filename) {
        return Files.exists(uploadPath.resolve(filename));
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }

    public String generateFilename(String id, String originalName) {
        String extension = getFileExtension(originalName);
        return id + extension;
    }
}