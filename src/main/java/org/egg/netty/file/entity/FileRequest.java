package org.egg.netty.file.entity;

import java.io.Serializable;

public class FileRequest implements Serializable {
    private String fileName;

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
