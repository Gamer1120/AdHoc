package com.procoder.gui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by reneb_000 on 13-4-2015.
 */
public class SmileyPanel extends GridPane {

    List<String> smiley = new ArrayList<String>();

    public SmileyPanel(Main main){
        //this.setStyle("-fx-font-family: OpenSansEmoji; -fx-font-size: 16;");
        //new Label(new String(new byte[]{(byte)0xD8,(byte)0x3D,(byte)0xDE,(byte)0x05}, "UTF-16")));

        smiley.add(getS(0xD8, 0x3D,0xDE,0x04));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x0A));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x03));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x09));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x0D));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x18));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x1A));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x33));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x0C));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x01));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x1C));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x1D));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x12));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x0F));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x13));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x14));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x1E));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x16));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x25));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x30));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x28));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x23));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x22));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x2D));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x02));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x32));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x31));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x20));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x21));
        smiley.add(getS(0xD8, 0x3D,0xDE,0x2A));
        smiley.add(getS(0xD8, 0x3D, 0xDE, 0x37));
        smiley.add(getS(0xD8, 0x3D,0xDC,0x7F));
        smiley.add(getS(0x00, 0x00,0x27,0x64));
        smiley.add(getS(0xD8, 0x3D,0xDC,0xA8));
        smiley.add(getS(0xD8, 0x3D,0xDC,0xA6));
        smiley.add(getS(0xD8, 0x3D,0xDC,0xA9));
        int counterx = 0;
        int countery = 0;


        for(int i=0;i<smiley.size();i++){
            Label newLabel = new Label(smiley.get(i));
            newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    main.addSmiley(newLabel.getText());
                }
            });
            this.add(newLabel,counterx, countery);
            counterx++;
            if(counterx>5){
                counterx=0;
                countery++;
            }
        }

        //this.add(new Label(getS(0xD8, 0x3D,0xDE,0x1D)),0,0);
        this.setStyle("-fx-font-family: OpenSansEmoji; -fx-font-size: 32;");

    }

    private String getS(int een, int twee, int drie, int vier){
        try {
            return new String(new byte[]{(byte)een, (byte)twee, (byte)drie, (byte)vier}, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
