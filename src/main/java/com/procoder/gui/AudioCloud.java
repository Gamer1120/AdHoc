package com.procoder.gui;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * Created by reneb_000 on 11-4-2015.
 */
public class AudioCloud extends Cloud {

    public AudioCloud(File music){
        super(true);
        Media media = new Media(music.toURI().toString());
        MediaPlayer mediaplayer = new MediaPlayer(media);
        mediaplayer.play();
        ImageView musicIcon = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("music.png")));

        vbox.getChildren().add(musicIcon);
    }
}
