package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by reneb_000 on 10-4-2015.
 */
public class ImageCloud extends Cloud {


    public ImageCloud(Image img) {
        super(true);
        ImageView imageView = new ImageView(img);
        if (img.getWidth() > maxWidth - 20) {
            double scale = img.getWidth() / (maxWidth - 20);
            imageView.setFitWidth(maxWidth - 20);
            imageView.setFitHeight(img.getHeight() / scale);
        }
        imageView.minHeight(25);
        vbox.getChildren().add(imageView);
    }

    public ImageCloud(Image img, String user){
        super(false);

        ImageView imageView = new ImageView(img);
        if (img.getWidth() > maxWidth - 20) {
            double scale = img.getWidth() / (maxWidth - 20);
            imageView.setFitWidth(maxWidth - 20);
            imageView.setFitHeight(img.getHeight() / scale);
        }
        imageView.minHeight(25);

        Label userLabel = new Label(user);
        userLabel.setPadding(new Insets(0, 0, 0, 10));
        userLabel.setStyle("-fx-font-size:12px;-fx-font-style:italic;");
        userLabel.setAlignment(Pos.CENTER_LEFT);
        vbox.getChildren().addAll(userLabel, imageView);
    }
}
