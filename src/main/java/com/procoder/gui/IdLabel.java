package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javax.swing.border.Border;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class IdLabel extends BorderPane {

    private double widht = 300;
    private double height = 100;
    private ImageView statusView;
    private Color c; //#F1F1F1
    private String adress;

    public IdLabel(String name){

        adress = name;

        this.setPrefSize(widht, height);
        this.setStyle("-fx-background-color: #FFFFFF;-fx-border-width:2px,0px,2px,0px;-fx-border-color:#F1F1F1");

        Image image;
        ImageView img;
        if(name.equals("AllChat")) {
            image = new Image(this.getClass().getClassLoader().getResourceAsStream("group.png"));
            img = new ImageView();

        }else{
            image = new Image(this.getClass().getClassLoader().getResourceAsStream("alone.png"));
            img = new ImageView();
        }

        img.setImage(image);
        Label label = new Label(name);
        label.setStyle("-fx-font-size:25px;-fx-font-weight:bold");
        label.setMinWidth(100);
        statusView = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("greenBall.png")));

        BorderPane.setAlignment(img, Pos.CENTER_LEFT);
        this.setLeft(img);
        BorderPane.setAlignment(label, Pos.CENTER_LEFT);
        this.setCenter(label);
        BorderPane.setAlignment(statusView, Pos.CENTER);
        this.setRight(statusView);

        this.setMargin(img, new Insets(10));
        this.setMargin(statusView, new Insets(10));


        //this.getChildren().addAll(img, label, statusView);
        //this.setAlignment(Pos.CENTER);
        //this.setSpacing(20);

    }

    public void setSelected(boolean selected){
        if(selected){
            this.setStyle("-fx-background-color: #3998d6;");
        }else{
            this.setStyle("-fx-background-color: #FFFFFF;-fx-border-width:2px,0px,2px,0px;-fx-border-color:#F1F1F1");
        }
    }

    public void setActive(boolean active){
        if(active){
            statusView.setImage(new Image(this.getClass().getClassLoader().getResourceAsStream("greenBall.png")));
        }else{
            statusView.setImage(new Image(this.getClass().getClassLoader().getResourceAsStream("redBall.png")));
        }
    }

    public String getAdress(){
        return adress;
    }

}
