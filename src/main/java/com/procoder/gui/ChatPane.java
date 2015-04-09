package com.procoder.gui;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Created by reneb_000 on 8-4-2015.
 */
public class ChatPane extends GridPane {


    private int counter;
    private Insets margin = new Insets(10);

    public ChatPane(){

        counter = 0;
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();

        column1.setPercentWidth(50);
        column2.setPercentWidth(50);

        this.getColumnConstraints().addAll(column1, column2);
        //this.setMar(new Insets(10));
    }


    //true for right false for left
    public void add(Cloud cloud, boolean right){
        if(!right){
            this.add(cloud, 0, counter);
        }else{
            this.add(cloud, 1, counter);
        }
        counter++;
        this.setMargin(cloud, margin);

    }


}
