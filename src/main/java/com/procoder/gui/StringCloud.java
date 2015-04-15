package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Created by reneb_000 on 10-4-2015.
 */
@SuppressWarnings("restriction")
public class StringCloud extends Cloud {

    public StringCloud(String msg) {
        super(true);
        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setStyle(messageStyle);
        vbox.getChildren().add(msgLabel);
    }

    public StringCloud(String msg, String user){
        super(false);
        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setStyle(messageStyle);
        Label userLabel = new Label(user);
        userLabel.setPadding(new Insets(0, 0, 0, 10));
        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;");
        userLabel.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().addAll(userLabel, msgLabel);
    }
}
