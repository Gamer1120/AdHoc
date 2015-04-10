package com.procoder.gui;

import java.io.File;

import javafx.scene.image.Image;

@SuppressWarnings("restriction")
public interface AdhocGUI {

    //public void addMsg(String msg);

    public void processString(String source, String destination, String msg);

    public void processFile(String source, String destination, File file);

    public void processImage(String source, String destination, Image img);

}
