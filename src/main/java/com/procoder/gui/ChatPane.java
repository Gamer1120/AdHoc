package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * Created by reneb_000 on 8-4-2015.
 */
@SuppressWarnings("restriction")
public class ChatPane extends GridPane {

    private int counter;
    private Insets margin = new Insets(10);
    private IdLabel idLabel;

    public ChatPane(IdLabel idLabel) {
        this.idLabel = idLabel;
        counter = 0;
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();

        column1.setPercentWidth(50);
        column2.setPercentWidth(50);

        this.getColumnConstraints().addAll(column1, column2);

        this.setStyle("-fx-control-inner-background:transparent;-fx-background-color:transparent;");

    }

    // true for right false for left
    public void add(Cloud cloud, boolean right) {
        if (!right) {
            this.add(cloud, 0, counter);
        } else {
            this.add(cloud, 1, counter);
            idLabel.setTextBall();
        }

        counter++;
        GridPane.setMargin(cloud, margin);

    }

}
