package com.procoder.gui;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Created by reneb_000 on 9-4-2015.
 */
@SuppressWarnings("restriction")
public class PopoverMenu extends VBox {

    private Button uploadButton;
    private Button smileyButton;

    private double minWidth = 100;
    public PopoverMenu(Main main){
        //this.getChildren().add(new Label("TEST"));
        uploadButton = new Button("Upload");
        smileyButton = new Button("Smiley");
        uploadButton.setFocusTraversable(false);
        smileyButton.setFocusTraversable(false);
        uploadButton.setMinWidth(minWidth);
        smileyButton.setMinWidth(minWidth);

        this.getChildren().addAll(uploadButton, smileyButton);


        uploadButton.setOnAction(main);
    }

    public Button getUploadButton(){
        return uploadButton;
    }
}
