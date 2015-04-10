package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by reneb_000 on 10-4-2015.
 */
public class FileCloud extends Cloud {

    public FileCloud(File file) {
        super(true);
        HBox hBox = new HBox();
        ImageView imgv = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("fileImg.png")));

        Label label = new Label(file.getName());
        label.setMinWidth(200);
        hBox.getChildren().addAll(imgv, label);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10,0,10,0));
        vbox.getChildren().add(hBox);
    }

    public FileCloud(File file, String user){
        super(false);
        HBox hBox = new HBox();

        ImageView imgv = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("fileImg.png")));
        Label label = new Label(file.getName());
        label.setMinWidth(200);
        hBox.getChildren().addAll(imgv, label);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10, 0, 10, 0));

        Label userLabel = new Label(user);
        userLabel.setPadding(new Insets(0, 0, 0, 10));
        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;");
        userLabel.setAlignment(Pos.CENTER_LEFT);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(userLabel, hBox);
    }
}
