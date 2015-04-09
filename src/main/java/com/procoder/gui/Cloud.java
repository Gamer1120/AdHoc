package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Created by reneb_000 on 8-4-2015.
 */
public class Cloud extends BorderPane {

    private double widht = 300;
    private double height = 100;
    private double maxWidth = 300;

    //true voor bollonnetje voor rechts, false voor ballontje voor links
    public Cloud(String msg, boolean right){
        Label message = new Label(msg);
        message.setPrefWidth(maxWidth);
        message.setMinHeight(25);
        message.setPadding(new Insets(5, 10, 5, 10));
        message.setWrapText(true);

        Polygon p;
        if(right){
            message.setStyle("-fx-background-color: #93F58E; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");
            //box.getChildren().addAll(getLeftTriangle(), message);
            p = getLeftTriangle();
            this.setLeft(p);
            BorderPane.setAlignment(p, Pos.CENTER_RIGHT);
            BorderPane.setAlignment(message, Pos.CENTER_LEFT);
        }else{
            message.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");
            //box.getChildren().addAll(message, getRightTriangle());
            p = getRightTriangle();
            this.setRight(p);
            BorderPane.setAlignment(p, Pos.CENTER_LEFT);
            BorderPane.setAlignment(message, Pos.CENTER_RIGHT);
        }

        this.setCenter(message);
    }

    public Cloud(String msg, String user){
        VBox vbox = new VBox();

        Label message = new Label(msg);
        message.setPrefWidth(maxWidth);
        message.setMinHeight(25);
        message.setPadding(new Insets(-10, 10, 5, 10));
        message.setWrapText(true);
        //message.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");
        //box.getChildren().addAll(message, getRightTriangle());
        Label userLabel = new Label(user);
        userLabel.setMinHeight(25);
        userLabel.setPadding(new Insets(0, 10, 0, 10));
        userLabel.setWrapText(true);
        //userLabel.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");

        //this.setTop(userLabel);
        vbox.getChildren().addAll(userLabel, message);
        message.setStyle("-fx-font-size:18px;");
        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;-fx-font-fill:#FFFFFF;"); //TODO change color
        vbox.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
        Polygon p = getRightTriangle();
        this.setRight(p);
        BorderPane.setAlignment(p, Pos.CENTER_LEFT);
        BorderPane.setAlignment(message, Pos.CENTER_RIGHT);
        this.setCenter(vbox);
        //this.setCenter(message);
    }

    public Cloud(Image img, String user) {
        VBox vbox = new VBox();

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(maxWidth);
        imageView.minHeight(25);
        //message.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");
        //box.getChildren().addAll(message, getRightTriangle());
        Label userLabel = new Label(user);
        userLabel.setMinHeight(25);
        userLabel.setPadding(new Insets(0, 10, 0, 10));
        userLabel.setWrapText(true);
        //userLabel.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");

        //this.setTop(userLabel);
        vbox.getChildren().addAll(userLabel, imageView);

        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;-fx-font-fill:#FFFFFF;"); //TODO change color
        vbox.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
        Polygon p = getRightTriangle();
        this.setRight(p);
        BorderPane.setAlignment(p, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(imageView, Pos.CENTER_RIGHT);
        this.setCenter(vbox);
        //this.setCenter(message);
    }

    public Cloud(Image img) {
        VBox vbox = new VBox();

        ImageView imageView = new ImageView(img);
        System.out.println("Width "+img.getWidth());
        if(img.getWidth()>maxWidth) {
            double scale = img.getWidth()/maxWidth;
            imageView.setFitWidth(maxWidth);
            imageView.setFitHeight(img.getHeight()/scale);

        }
        imageView.minHeight(25);
        //message.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");
        //box.getChildren().addAll(message, getRightTriangle());

        //userLabel.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:18px;");

        //this.setTop(userLabel);
        vbox.getChildren().addAll(imageView);
        vbox.setStyle("-fx-background-color: #93F58E; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
        Polygon p = getLeftTriangle();
        this.setLeft(p);
        BorderPane.setAlignment(p, Pos.CENTER_LEFT);
        BorderPane.setAlignment(imageView, Pos.CENTER_RIGHT);
        this.setCenter(vbox);
        //this.setCenter(message);
    }



    private Polygon getLeftTriangle(){
        Polygon triangle = new Polygon();
        Double[] cords = new Double[]{0.0,12.5,15.0,5.0,15.0,20.0};
        triangle.getPoints().addAll(cords);
        Color c = Color.web("#93F58E");
        triangle.setFill(c);
        return triangle;
    }

    private Polygon getRightTriangle(){
        Polygon triangle = new Polygon();
        Double[] cords = new Double[]{15.0,12.5,0.0,5.0,0.0,20.0};
        triangle.getPoints().addAll(cords);
        Color c = Color.web("#E8E8E8");
        triangle.setFill(c);
        return triangle;
    }


    public Node getEmptyCloud(){
        Label newLabel = new Label();
        newLabel.setPrefSize(this.getPrefWidth(), this.getPrefHeight());
        return newLabel;
    }

}
