package ru.flykby.entities;



public class DataUser {
    private Long id;
    private String channelId;
    private Integer date;

    public DataUser(Long id, String channelId) {
        this.id = id;
        this.channelId = channelId;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getDate() {
        return date;
    }

    public String getChannelId() {
        return channelId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.id + " : " + this.channelId + " -> " + this.date;
    }
}
