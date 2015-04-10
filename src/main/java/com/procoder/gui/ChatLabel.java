package com.procoder.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Created by reneb_000 on 7-4-2015.
 */
@SuppressWarnings("restriction")
public class ChatLabel extends Label {

    private double widht = 300;
    private double height = 100;

    public ChatLabel() {
        super("Chats");
        this.setStyle("-fx-font: 100px Tahoma; -fx-background-color: #FDFDFD;");
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(widht, height);

    }

}
