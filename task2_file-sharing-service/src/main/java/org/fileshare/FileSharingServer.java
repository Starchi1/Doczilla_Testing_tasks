package org.fileshare;


import org.fileshare.server.handlers.*;
import org.fileshare.service.FileMetadataService;
import org.fileshare.service.FileStorageService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileSharingServer {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        try {
            FileMetadataService metadataService = new FileMetadataService();
            FileStorageService storageService = new FileStorageService();

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    metadataService.cleanupExpiredFiles();
                    System.out.println("Проверка устаревших файлов выполнена");
                } catch (Exception e) {
                    System.err.println("Ошибка при очистке устаревших файлов: " + e.getMessage());
                }
            }, 1, 60, TimeUnit.MINUTES);

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/", new StaticFileHandler());
            server.createContext("/upload", new UploadHandler(metadataService, storageService));
            server.createContext("/files/", new FileDownloadHandler(metadataService, storageService));

            server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы сервера...");
                server.stop(0);
                scheduler.shutdown();
                metadataService.saveMetadata();
                System.out.println("Метаданные сохранены");
            }));

            server.start();
            System.out.println("Файлообменник запущен на порту " + PORT);
            System.out.println("Доступен по адресу: http://localhost:" + PORT);
            System.out.println("Файлы хранятся в папке: uploads/");
            System.out.println("Метаданные хранятся в: metadata.json");

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}