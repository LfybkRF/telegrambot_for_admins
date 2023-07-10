package ru.flykby.entities;

public class DataChannel {
    private String name;
    private String namechannel;

    public DataChannel(String name, String namechannel) {
        this.name = name;
        this.namechannel = namechannel;
    }

    public String getName() {
        return name;
    }

    public String getNamechannel() {
        return namechannel;
    }

    @Override
    public String toString() {
        return this.name + " : " + this.namechannel;
    }
}
