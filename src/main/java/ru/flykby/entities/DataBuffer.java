package ru.flykby.entities;

public class DataBuffer {
    private String photoId;
    private String message;
    private String isPhoto;
    
    
    public DataBuffer(String photoId, String message, String isPhoto) {
        this.photoId = photoId;
        this.message = message;
        this.isPhoto = isPhoto;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIsPhoto() {
        return isPhoto;
    }

    @Override
    public String toString() {
        return photoId + " " + message + " " + isPhoto;
    }

    @Override
    public boolean equals(Object dataBuffer) {
        return this.hashCode() == dataBuffer.hashCode();
    }

    @Override
    public int hashCode() {
        return photoId.hashCode();
    }

}
