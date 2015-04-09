package com.procoder.gui;

import java.io.File;

import javafx.scene.image.Image;

@SuppressWarnings("restriction")
public interface AdhocGUI {

    public void addMsg(String msg);

    public void processString(String user, String msg);

    public void processFile(String user, File file);

    public void processImage(String user, Image img);

}
