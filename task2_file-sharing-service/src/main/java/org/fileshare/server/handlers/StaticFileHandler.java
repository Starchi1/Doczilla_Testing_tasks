package org.fileshare.server.handlers;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        InputStream resourceStream = getResourceAsStream("public" + path);

        if (resourceStream == null) {
            sendResponse(exchange, 404, "Файл не найден");
            return;
        }

        byte[] fileData = resourceStream.readAllBytes();
        resourceStream.close();

        String mimeType = getMimeType(path);
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, fileData.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileData);
        }
    }

    private InputStream getResourceAsStream(String resourcePath) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            File file = new File(resourcePath);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
        }

        return stream;
    }

    private String getMimeType(String filename) {
        if (filename.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (filename.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (filename.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (filename.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (filename.endsWith(".png")) {
            return "image/png";
        }
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (filename.endsWith(".gif")) {
            return "image/gif";
        }
        if (filename.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "text/plain; charset=utf-8";
    }

    private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}