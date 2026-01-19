package org.fileshare.server.handlers;


import org.fileshare.service.FileMetadataService;
import org.fileshare.service.FileStorageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileDownloadHandler implements HttpHandler {
    private final FileMetadataService metadataService;
    private final FileStorageService storageService;

    public FileDownloadHandler(FileMetadataService metadataService, FileStorageService storageService) {
        this.metadataService = metadataService;
        this.storageService = storageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (!path.startsWith("/files/")) {
            sendResponse(exchange, 400, "Некорректный путь");
            return;
        }

        String filename = path.substring("/files/".length());
        if (filename.isEmpty()) {
            sendResponse(exchange, 400, "Не указано имя файла");
            return;
        }

        try {
            // Извлекаем ID из имени файла (удаляем расширение)
            String id = filename;
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex != -1) {
                id = filename.substring(0, dotIndex);
            }

            // Проверяем метаданные
            var fileInfo = metadataService.getFileInfo(id);
            if (fileInfo == null) {
                sendResponse(exchange, 404, "Файл не найден");
                return;
            }

            // Проверяем существование файла на диске
            if (!storageService.fileExists(filename)) {
                metadataService.deleteFileInfo(id);
                sendResponse(exchange, 404, "Файл отсутствует на диске");
                return;
            }

            byte[] fileData = storageService.readFile(filename);

            metadataService.updateFileAccess(id);

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Disposition",
                    "attachment; filename=\"" + fileInfo.getOriginalName() + "\"");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(fileData.length));

            exchange.sendResponseHeaders(200, fileData.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileData);
            }

        } catch (IOException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Ошибка чтения файла");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}