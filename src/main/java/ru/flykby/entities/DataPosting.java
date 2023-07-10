package ru.flykby.entities;

public class DataPosting {
    private int id;
    private String channel;
    private String photoId;
    private String message;
    private String isPhoto;
    
    
    public DataPosting(String channel, String photoId, String message, String isPhoto) {
        this.channel = channel;
        this.photoId = photoId;
        this.message = message;
        this.isPhoto = isPhoto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
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
        return id + " " + channel + " " + photoId + " " + message + " " + isPhoto;
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