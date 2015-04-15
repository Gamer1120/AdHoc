package com.procoder.gui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by reneb_000 on 10-4-2015.
 */
@SuppressWarnings("restriction")
public class FileCloud extends Cloud {

    File file;
    HBox hBox;

    public FileCloud(File file) {
        super(true);
        this.file = file;
        hBox = new HBox();
        ImageView imgv = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("fileImg.png")));

        Label label = new Label(file.getName());
        label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        label.setMaxWidth(200);
        hBox.getChildren().addAll(imgv, label);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 0, 10, 0));
        addButtons();
        vbox.getChildren().add(hBox);
    }

    public FileCloud(File file, String user){
        super(false);
        this.file = file;
        hBox = new HBox();

        ImageView imgv = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("fileImg.png")));
        Label label = new Label(file.getName());
        label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        label.setMaxWidth(200);
        hBox.getChildren().addAll(imgv, label);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 0, 10, 0));

        Label userLabel = new Label(user);
        userLabel.setPadding(new Insets(0, 0, 0, 10));
        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;");
        userLabel.setAlignment(Pos.CENTER_LEFT);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        addButtons();
        vbox.getChildren().addAll(userLabel, hBox);
    }

    private void addButtons(){
        Button safe = new Button("Save");
        Button open = new Button("Open");
        //safe.setMinWidth(100);
        //open.setMinWidth(100);

        safe.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent arg0) {
                DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle("Open Directory");
                //System.out.println(dc.showDialog(new Stage()));

                File directory = new File(dc.showDialog(new Stage()).getPath()+File.separator+file.getName());

                System.out.println(directory.toPath());
                if(directory!=null) {

                    //System.out.println(file.renameTo(new File(directory.toURI().toString() + file.getName())));
                    try {

                        Files.copy(file.toPath(), directory.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println(file.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }
        });

        open.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent arg0) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        hBox.getChildren().addAll(open, safe);

    }
}
