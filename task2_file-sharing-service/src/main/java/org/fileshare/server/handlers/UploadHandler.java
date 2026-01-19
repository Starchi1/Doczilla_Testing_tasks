package org.fileshare.server.handlers;


import com.google.gson.Gson;
import org.fileshare.model.FileInfo;
import org.fileshare.model.UploadResponse;
import org.fileshare.service.FileMetadataService;
import org.fileshare.service.FileStorageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UploadHandler implements HttpHandler {
    private static final Gson gson = new Gson();
    private static final long EXPIRY_DAYS = 30;
    private static final long EXPIRY_MILLIS = EXPIRY_DAYS * 24 * 60 * 60 * 1000L;

    private final FileMetadataService metadataService;
    private final FileStorageService storageService;

    public UploadHandler(FileMetadataService metadataService, FileStorageService storageService) {
        this.metadataService = metadataService;
        this.storageService = storageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Метод не поддерживается");
            return;
        }

        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                sendResponse(exchange, 400, "Некорректный Content-Type");
                return;
            }

            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            MultipartData multipart = parseMultipart(requestBody, contentType);

            if (multipart.fileName == null || multipart.fileData == null) {
                sendResponse(exchange, 400, "Файл не найден в запросе");
                return;
            }

            FileInfo existingFile = metadataService.findFileByOriginalName(multipart.fileName);

            if (existingFile != null) {
                existingFile.setLastAccessTime(System.currentTimeMillis());
                metadataService.saveMetadata();

                String url = "http://" + exchange.getLocalAddress().getHostString() +
                        ":" + exchange.getLocalAddress().getPort() +
                        "/files/" + existingFile.getFilename();

                UploadResponse response = new UploadResponse(
                        url,
                        existingFile.getId(),
                        existingFile.getOriginalName(),
                        existingFile.getLastAccessTime() + EXPIRY_MILLIS,
                        existingFile.getSize(),
                        existingFile.getDownloadCount()
                );

                sendJsonResponse(exchange, 200, gson.toJson(response));
                return;
            }

            String id = storageService.saveFile(multipart.fileData, multipart.fileName);
            String filename = storageService.generateFilename(id, multipart.fileName);

            long now = System.currentTimeMillis();
            long fileSize = multipart.fileData.length;

            FileInfo fileInfo = new FileInfo(id, filename, multipart.fileName, now, fileSize);
            metadataService.registerFile(fileInfo);

            String url = "http://" + exchange.getLocalAddress().getHostString() +
                    ":" + exchange.getLocalAddress().getPort() +
                    "/files/" + filename;

            UploadResponse response = new UploadResponse(
                    url,
                    id,
                    multipart.fileName,
                    now + EXPIRY_MILLIS,
                    fileSize,
                    0
            );

            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private MultipartData parseMultipart(byte[] body, String contentType) {
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return new MultipartData();
        }

        String bodyStr = new String(body, StandardCharsets.UTF_8);
        String[] parts = bodyStr.split("--" + boundary);

        for (String part : parts) {
            if (part.contains("Content-Disposition: form-data;") && part.contains("filename=")) {
                MultipartData data = new MultipartData();

                // Извлекаем имя файла
                int nameStart = part.indexOf("filename=\"") + 10;
                int nameEnd = part.indexOf("\"", nameStart);
                if (nameStart >= 10 && nameEnd > nameStart) {
                    data.fileName = part.substring(nameStart, nameEnd);
                }

                // Извлекаем данные файла
                int dataStart = part.indexOf("\r\n\r\n") + 4;
                int dataEnd = part.lastIndexOf("\r\n");
                if (dataStart >= 4 && dataEnd > dataStart) {
                    data.fileData = Arrays.copyOfRange(body,
                            bodyStr.substring(0, dataStart).getBytes(StandardCharsets.UTF_8).length,
                            bodyStr.substring(0, dataEnd).getBytes(StandardCharsets.UTF_8).length
                    );
                }

                return data;
            }
        }

        return new MultipartData();
    }

    private String extractBoundary(String contentType) {
        if (contentType.startsWith("multipart/form-data; boundary=")) {
            return contentType.substring("multipart/form-data; boundary=".length());
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private static class MultipartData {
        String fileName;
        byte[] fileData;
    }
}