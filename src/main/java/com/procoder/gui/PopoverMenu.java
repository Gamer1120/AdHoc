package com.procoder.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * Created by reneb_000 on 9-4-2015.
 */

public class PopoverMenu extends VBox implements EventHandler<ActionEvent> {

    private Button uploadButton;
    private Button backgroundButton;
    private Main main;

    private double minWidth = 120;

    public PopoverMenu(Main main){
        //this.getChildren().add(new Label("TEST"));
        this.main = main;
        uploadButton = new Button("Upload");
        backgroundButton = new Button("Background");
        uploadButton.setFocusTraversable(false);
        backgroundButton.setFocusTraversable(false);
        uploadButton.setMinWidth(minWidth);
        backgroundButton.setMinWidth(minWidth);


        uploadButton.setId("buttons");
        backgroundButton.setId("buttons");
        this.getChildren().addAll(uploadButton, backgroundButton);



        uploadButton.setOnAction(this);
        backgroundButton.setOnAction(this);

    }

    public Button getUploadButton() {
        return uploadButton;
    }

    public Button getBackgroundButton(){
        return uploadButton;
    }

    @Override
    public void handle(ActionEvent event) {
        if(event.getSource().equals(uploadButton)){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(new Stage());
            //Image image = new Image(file.toURI().toString());
            if(file!=null) {
                String[] s = file.getName().split("\\.");
                String extension = s[s.length-1];
                if(Main.images.contains(extension)){
                    main.sendImage(file);
                }else if(Main.audios.contains(extension)){
                    main.sendAudio(file);
                }else {
                    main.sendFile(file);
                }
            }
            main.getPopover().hide();

        }else if(event.getSource().equals(backgroundButton)){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open background file");
            File file = fileChooser.showOpenDialog(new Stage());
            if(file!=null) {
                //System.out.println(file.toURI());
                main.getScrollPane().setStyle("-fx-background-image:url(" + file.toURI() + ");");

                byte dataToWrite[] = null;
                try {
                    dataToWrite = Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream out;
                try {
                    out = new FileOutputStream("background.png");
                    out.write(dataToWrite);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
