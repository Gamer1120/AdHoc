package gui;

import javafx.scene.control.Label;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class IdLabel extends Label {

    private double widht = 300;
    private double height = 100;

    public IdLabel(String name){
        super(name);

        this.setPrefSize(widht, height);
        this.setStyle("-fx-background-color: #3AD6FA;");

    }


}
