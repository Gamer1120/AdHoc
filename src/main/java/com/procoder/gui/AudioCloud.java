package com.procoder.gui;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by reneb_000 on 11-4-2015.
 */
public class AudioCloud extends Cloud {

    private boolean play = false;
    private Slider slider;
    private Label timeLabel;
    private Media media;
    private MediaPlayer mediaplayer;
    private Duration duration;
    private DecimalFormat df;
    private Button playButton;


    public AudioCloud(File music){
        super(true);
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);

        media = new Media(music.toURI().toString());
        mediaplayer = new MediaPlayer(media);
        //mediaplayer.play();
        ImageView musicIcon = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("music.png")));
        playButton = new Button();
        playButton.setMinHeight(25);
        playButton.setMinWidth(25);
        String url = this.getClass().getClassLoader().getResource("play.png").toString();
        playButton.setStyle("-fx-background-image:url(" + url + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setPlayButton();
            }
        });

        Button stop = new Button();
        String stopurl = this.getClass().getClassLoader().getResource("stop.png").toString();
        stop.setStyle("-fx-background-image:url(" + stopurl + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");
        stop.setMinSize(25, 25);
        stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mediaplayer.stop();
                play = false;
                String url = this.getClass().getClassLoader().getResource("play.png").toString();
                playButton.setStyle("-fx-background-image:url(" + url + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");
            }
        });
        Button volume = new Button();
        volume.setMinSize(25, 25);
        String volumeurl = this.getClass().getClassLoader().getResource("volume.png").toString();
        volume.setStyle("-fx-background-image:url(" + volumeurl + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");

        mediaplayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                duration = media.getDuration();
                timeLabel.setText(formatTime(mediaplayer.getCurrentTime(),duration));
            }
        });

        df = new DecimalFormat("0.00");
        slider = new Slider();
        slider.setMinWidth(100);


        timeLabel = new Label();

        //timeLabel = new Label(formatTime(mediaplayer.getCurrentTime(),duration));
        timeLabel.setMinWidth(50);




        slider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (slider.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    if (duration != null) {
                        mediaplayer.seek(duration.multiply(slider.getValue() / 100.0));
                    }
                    updateValues();
                }
            }
        });


        mediaplayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                updateValues();
            }
        });


        hbox.getChildren().addAll(musicIcon, playButton, volume, slider,timeLabel, stop);

        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(hbox);
    }


    protected void updateValues() {
        if (slider != null && duration != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mediaplayer.getCurrentTime();
                    timeLabel.setText(formatTime(currentTime, duration));
                    slider.setDisable(duration.isUnknown());
                    if (!slider.isDisabled() && duration.greaterThan(Duration.ZERO) && !slider.isValueChanging()) {
                        slider.setValue(currentTime.divide(duration.toMillis()).toMillis() * 100.0);
                    }
                    if(currentTime.greaterThanOrEqualTo(duration)){
                        slider.setValue(0);
                        mediaplayer.stop();
                        currentTime = mediaplayer.getCurrentTime();
                        timeLabel.setText(formatTime(currentTime, duration));
                        setPlayButton();
                    }
                    /*
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mp.getVolume() * 100));
                    }*/
                }
            });
        }
    }

    private String formatTime(Duration currentTime, Duration duration){
        if(duration!=null) {
            return df.format(currentTime.toMinutes()) + "/" + df.format(duration.toMinutes());
        }else{
            return "";
        }
    }

    private void setPlayButton(){
        if(play){
            play = false;
            mediaplayer.pause();
            String url = this.getClass().getClassLoader().getResource("play.png").toString();
            playButton.setStyle("-fx-background-image:url(" + url + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");
        } else {
            play = true;
            mediaplayer.play();
            String url = this.getClass().getClassLoader().getResource("pause.png").toString();
            playButton.setStyle("-fx-background-image:url(" + url + ");-fx-background-color:transparent;-fx-background-repeat:no-repeat");
        }
    }
}
