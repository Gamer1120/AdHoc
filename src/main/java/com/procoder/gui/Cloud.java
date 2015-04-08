package com.procoder.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Created by reneb_000 on 8-4-2015.
 */
public class Cloud extends Pane {

    private double widht = 300;
    private double height = 100;
    private double maxWidth = 200;


    //true voor bollonnetje voor rechts, false voor ballontje voor links
    public Cloud(String msg, boolean right){
        Label message = new Label(msg);
        //message.setMinWidth(maxWidth);
        //message.setMaxWidth(maxWidth+100);
        message.setPrefWidth(maxWidth);
        message.setMinHeight(25);
        //message.setStyle("-fx-font: 100;");
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);



        //box.getChildren().add(message);



        if(right){
            message.setStyle("-fx-background-color: #93F58E; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:13px;");
            box.getChildren().addAll(getLeftTriangle(), message);
        }else{
            message.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;-fx-font-size:13px;");
            box.getChildren().addAll(message, getRightTriangle());
        }


        this.getChildren().add(box);
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
        newLabel.setPrefSize(this.getWidth(), this.getHeight());
        return newLabel;
    }

}
