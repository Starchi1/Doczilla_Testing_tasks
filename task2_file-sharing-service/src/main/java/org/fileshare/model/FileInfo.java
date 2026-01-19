package org.fileshare.model;


public class FileInfo {
    private String id;
    private String filename;
    private String originalName;
    private long uploadTime;
    private long lastAccessTime;
    private int downloadCount;
    private long size;

    public FileInfo() {}

    public FileInfo(String id, String filename, String originalName, long uploadTime, long size) {
        this.id = id;
        this.filename = filename;
        this.originalName = originalName;
        this.uploadTime = uploadTime;
        this.lastAccessTime = uploadTime;
        this.downloadCount = 0;
        this.size = size;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public long getUploadTime() { return uploadTime; }
    public void setUploadTime(long uploadTime) { this.uploadTime = uploadTime; }

    public long getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }
}