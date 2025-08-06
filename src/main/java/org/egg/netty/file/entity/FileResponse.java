package org.egg.netty.file.entity;

import java.io.Serializable;

public class FileResponse implements Serializable {
    private boolean exist;
    private String fileName;
    private long fileSize;
    private String error;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public FileResponse(boolean exist, String error) {
        this.exist = exist;
        this.error = error;
    }

    public FileResponse(boolean exist, String fileName, long fileSize) {
        this.exist = exist;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
