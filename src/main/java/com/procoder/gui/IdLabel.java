package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by reneb_000 on 7-4-2015.
 */
@SuppressWarnings("restriction")
public class IdLabel extends BorderPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdLabel.class);

    private static final Image GREEN_BALL = new Image(IdLabel.class.getClassLoader().getResourceAsStream("greenBall.png"));
    private static final Image RED_BALL = new Image(IdLabel.class.getClassLoader().getResourceAsStream("redBall.png"));
    private static final Background GREEN_BALL_BACKGROUND = new Background(new BackgroundImage(GREEN_BALL, null, null, null, null));
    private static final Background RED_BALL_BACKGROUND = new Background(new BackgroundImage(RED_BALL, null, null, null, null));


    private double widht = 300;
    private double height = 100;
    private Label statusView;
    private Color c; // #F1F1F1
    private String adress;
    private int counter;

    private String broadcast = "228.0.0.0";
    private boolean selected;
    private boolean active = true;

    public IdLabel(String name) {
        adress = name;
        counter = 0;
        selected = false;
        this.setPrefSize(widht, height);
        this.setStyle("-fx-background-color: #FFFFFF;-fx-border-width:2px,0px,2px,0px;-fx-border-color:#F1F1F1");

        Image image;
        ImageView img;
        if (name.equals("AllChat")) {
            image = new Image(this.getClass().getClassLoader().getResourceAsStream("group.png"));
            img = new ImageView();
        } else {
            image = new Image(this.getClass().getClassLoader().getResourceAsStream("alone.png"));
            img = new ImageView();
        }

        img.setImage(image);
        Label label = new Label(name);
        label.setStyle("-fx-font-size:25px;-fx-font-weight:bold");
        label.setMinWidth(100);
        //statusView = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("greenBall.png")));
        statusView = new Label("");
        statusView.setPrefSize(25, 25);
        statusView.setBackground(GREEN_BALL_BACKGROUND);
        statusView.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(img, Pos.CENTER_LEFT);
        this.setLeft(img);
        BorderPane.setAlignment(label, Pos.CENTER_LEFT);
        this.setCenter(label);
        BorderPane.setAlignment(statusView, Pos.CENTER);
        this.setRight(statusView);

        BorderPane.setMargin(img, new Insets(10));
        BorderPane.setMargin(statusView, new Insets(10));


    }

    public void setSelected(boolean selected) {
        if (selected) {
            this.setStyle("-fx-background-color: #3998d6;");
            resetTextBall();
            this.selected = true;
        } else {
            this.setStyle("-fx-background-color: #FFFFFF;-fx-border-width:2px,0px,2px,0px;-fx-border-color:#F1F1F1");
            this.selected = false;
        }
    }

    public void setActive(boolean active) {
        if (active) {
            statusView.setBackground(GREEN_BALL_BACKGROUND);
        } else {
            statusView.setBackground(RED_BALL_BACKGROUND);
        }
        this.active = active;
    }

    public boolean getActive(){
        return active;
    }

    public void setTextBall(){
        if(!selected) {
            counter++;
            if(counter>=99) {
                statusView.setText("99");
            }else{
                statusView.setText("" + (counter));
            }
        }
    }

    public void resetTextBall(){
        counter=0;
        statusView.setText("");
    }

    public String getAdress() {
        return adress;
    }

    public InetAddress getInetAdress() {
        if (adress.equals("AllChat")) {
            try {
                return InetAddress.getByName(broadcast);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return InetAddress.getByName(adress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
