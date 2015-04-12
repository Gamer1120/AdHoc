package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
@SuppressWarnings("restriction")
public class Cloud extends BorderPane {

    private double widht = 300;
    private double height = 100;
    protected double maxWidth = 300;
    private Label message;
    private Label userLabel;
    private ImageView imageView;


    protected VBox vbox;
    protected String messageStyle = "-fx-font-size:18px;";


    // true voor bollonnetje voor rechts, false voor ballontje voor links
    public Cloud(boolean right){
        vbox = new VBox();
        Polygon p;
        if(right){
            p = getLeftTriangle();
            this.setLeft(p);
            BorderPane.setAlignment(p, Pos.CENTER_RIGHT);
            BorderPane.setAlignment(vbox, Pos.CENTER_LEFT);
            vbox.setStyle("-fx-background-color: #93F58E; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
            //vbox.setStyle("-fx-background-color: #93F58E;");
        } else {
            p = getRightTriangle();
            this.setRight(p);
            BorderPane.setAlignment(p, Pos.CENTER_LEFT);
            BorderPane.setAlignment(vbox, Pos.CENTER_RIGHT);
            vbox.setStyle("-fx-background-color: #E8E8E8; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
        }
        vbox.paddingProperty().setValue(new Insets(0,0,0,10));

        //vbox.setAlignment(Pos.CENTER_LEFT);
        this.setMinHeight(25);
        this.setMinWidth(maxWidth);
        this.setCenter(vbox);

    }

    private Polygon getLeftTriangle() {
        Polygon triangle = new Polygon();
        Double[] cords = new Double[] { 0.0, 12.5, 15.0, 5.0, 15.0, 20.0 };
        triangle.getPoints().addAll(cords);
        Color c = Color.web("#93F58E");
        triangle.setFill(c);
        //triangle.setStyle("-fx-stroke:#93F58E;");
        return triangle;
    }

    private Polygon getRightTriangle() {
        Polygon triangle = new Polygon();
        Double[] cords = new Double[] { 15.0, 12.5, 0.0, 5.0, 0.0, 20.0 };
        triangle.getPoints().addAll(cords);
        Color c = Color.web("#E8E8E8");
        triangle.setFill(c);
        return triangle;
    }

}
