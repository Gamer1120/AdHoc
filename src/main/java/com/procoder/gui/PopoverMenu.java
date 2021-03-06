package com.procoder.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by reneb_000 on 9-4-2015.
 */

@SuppressWarnings("restriction")
public class PopoverMenu extends VBox implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopoverMenu.class);
    private final AtomicBoolean capturing = new AtomicBoolean();
    private Button uploadButton;
    private Button backgroundButton;
    private Button drawButton;
    private Button selfieButton;
    private Main main;
    private double minWidth = 120;

    public PopoverMenu(Main main){
        capturing.set(false);
        //this.getChildren().add(new Label("TEST"));
        this.main = main;
        uploadButton = new Button("Upload");
        backgroundButton = new Button("Background");
        drawButton = new Button("Draw");
        selfieButton = new Button("Selfie");
        uploadButton.setFocusTraversable(false);
        backgroundButton.setFocusTraversable(false);
        drawButton.setFocusTraversable(false);
        selfieButton.setFocusTraversable(false);
        uploadButton.setMinWidth(minWidth);
        backgroundButton.setMinWidth(minWidth);
        drawButton.setMinWidth(minWidth);
        selfieButton.setMinWidth(minWidth);

        uploadButton.setId("buttons");
        backgroundButton.setId("buttons");
        drawButton.setId("buttons");
        selfieButton.setId("buttons");
        this.getChildren().addAll(uploadButton, backgroundButton, drawButton, selfieButton);



        uploadButton.setOnAction(this);
        backgroundButton.setOnAction(this);
        drawButton.setOnAction(this);
        selfieButton.setOnAction(this);

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
                String filename = file.getName();
                String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
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
        else if(event.getSource().equals(drawButton)){
            new DrawPanel(main);
        }
        else if(event.getSource().equals(selfieButton)){
            main.getPopover().hide();
                if (!capturing.get()) {
                    capturing.set(true);
                    new Thread(() -> {
                        Webcam webcam = Webcam.getDefault();
                        webcam.setViewSize(WebcamResolution.VGA.getSize());
                        try {
                            webcam.open();
                            File capture = new File("webcam.jpg");
                            ImageIO.write(webcam.getImage(), "JPG", capture);
                            main.sendImage(capture);
                        } catch (WebcamException e) {
                            LOGGER.trace("Webcam capture has failed", e);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Webcam error");
                            alert.setHeaderText("Selfie error occurred");
                            alert.setContentText(e.getMessage());
                            alert.show();
                        } catch (IOException e) {
                            LOGGER.trace("Other IO Exception", e);
                        }
                        webcam.close();
                        capturing.set(false);
                    }).start();
                }

        }

    }
}
