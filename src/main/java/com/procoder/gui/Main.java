package com.procoder.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class Main extends Application implements EventHandler<javafx.event.ActionEvent> {

    private VBox side;
    private BorderPane mainPane;
    private BorderPane center;
    private HBox commandPanel; //onder textfield voor invullen voor tekst en knop om te verzenden
    //private TextArea textArea;
    private ScrollPane scrollPane;
    private VBox drawPane;


    private Button sendButton;
    private TextField ipField;
    private TextField text;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("AWESOME ADHOC com.procoder.GUI");
        mainPane = new BorderPane();

        setupSideBar();

        setupCenter();


        Scene mainScene = new Scene(mainPane, 1200, 900);


        //mainScene.getStylesheets().add("Css.css");


        primaryStage.setScene(mainScene);
        primaryStage.show();

    }

    private void setupCenter() {
        center = new BorderPane();
        //textArea = new TextArea();
        //textArea.setEditable(false);
        //center.setCenter(textArea);
        drawPane = new VBox();
        scrollPane = new ScrollPane(drawPane);
        scrollPane.setPrefSize(900, Double.MAX_VALUE);
        center.setCenter(scrollPane);

        commandPanel = new HBox();
        ipField = new TextField("TODO");
        text = new TextField();
        sendButton = new Button("Send");

        sendButton.setOnAction(this);

        commandPanel.getChildren().addAll(ipField, text, sendButton);
        center.setBottom(commandPanel);
        mainPane.setCenter(center);
    }

    private void setupSideBar() {
        side = new VBox();
        side.setPrefSize(300, 900);
        side.setStyle("-fx-background-color: #FFFFFF;");
        side.getChildren().add(new ChatLabel());
        addLabel("192.168.2.2");
        mainPane.setLeft(side);


    }


    public void addLabel(String name){
        side.getChildren().add(new IdLabel(name));
    }

    public void addMsg(String msg){
        drawPane.getChildren().add(new Label(msg));
    }


    @Override
    public void handle(javafx.event.ActionEvent event) {
        if(event.getSource().equals(sendButton)){
            addMsg(text.getText());
        }
    }
}
