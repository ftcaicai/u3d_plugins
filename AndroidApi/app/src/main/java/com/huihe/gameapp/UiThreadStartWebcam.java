package com.huihe.gameapp;

public abstract class UiThreadStartWebcam implements Runnable {
    public String type;

    public String filepath;

    public String filename;

    public UiThreadStartWebcam(String type, String filepath, String filename) {
        this.type = type;
        this.filepath = filepath;
        this.filename = filename;
    }
}
