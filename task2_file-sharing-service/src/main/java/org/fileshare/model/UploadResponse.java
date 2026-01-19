package org.fileshare.model;

public class UploadResponse {
    private String url;
    private String id;
    private String originalName;
    private long expiresAt;
    private long size;
    private int downloadCount;

    public UploadResponse(String url, String id, String originalName,
                          long expiresAt, long size, int downloadCount) {
        this.url = url;
        this.id = id;
        this.originalName = originalName;
        this.expiresAt = expiresAt;
        this.size = size;
        this.downloadCount = downloadCount;
    }

    public String getUrl() { return url; }
    public String getId() { return id; }
    public String getOriginalName() { return originalName; }
    public long getExpiresAt() { return expiresAt; }
    public long getSize() { return size; }
    public int getDownloadCount() { return downloadCount; }
}