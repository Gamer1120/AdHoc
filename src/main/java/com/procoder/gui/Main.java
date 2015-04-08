package com.procoder.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;


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
    //private VBox drawPane;
    private HashMap<IdLabel, ChatPane> chatMap;


    private Button sendButton;
    //private TextField ipField;
    private TextField text;
    private Insets padding;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("AWESOME ADHOC com.procoder.GUI");
        mainPane = new BorderPane();
        chatMap = new HashMap<IdLabel, ChatPane>();
        padding = new Insets(10);

        setupCenter();
        setupSideBar();

        Scene mainScene = new Scene(mainPane, 900, 900);

        //mainScene.getStylesheets().add("Css.css");
        addLabel("192.168.2.2");

        ChatPane h = (ChatPane)scrollPane.getContent();
        h.add(new Cloud("test", false), true);
        primaryStage.setScene(mainScene);
        primaryStage.show();

    }

    private void setupCenter() {
        center = new BorderPane();

        //drawPane = new VBox();
        scrollPane = new ScrollPane();
        //scrollPane.setPrefSize(550, Double.MAX_VALUE);
        scrollPane.setPrefHeight(Double.MAX_VALUE);
        scrollPane.setFitToWidth(true);
        //scrollPane.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        center.setCenter(scrollPane);


        commandPanel = new HBox();
        //ipField = new TextField("TODO");
        text = new TextField();
        text.setPrefWidth(500);
        text.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode()== KeyCode.ENTER){
                    addMsg(text.getText());
                }
            }
        });

        text.requestFocus();
        sendButton = new Button("Send");

        sendButton.setOnAction(this);

        commandPanel.getChildren().addAll(text, sendButton);
        center.setBottom(commandPanel);
        mainPane.setCenter(center);
    }

    private void setupSideBar() {
        side = new VBox();
        side.setPrefSize(300, 900);
        side.setStyle("-fx-background-color: #FFFFFF;");
        side.getChildren().add(new ChatLabel());
        //addLabel("AllChat");
        addAllChat();
        mainPane.setLeft(side);
        //scrollPane.setContent(chatMap.get());


    }

    public void addAllChat(){
        IdLabel newLabel = new IdLabel("AllChat");
        side.getChildren().add(newLabel);
        //VBox newPane = new VBox();
        ChatPane chatPane = new ChatPane();

        chatMap.put(newLabel,chatPane);
        newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scrollPane.setContent(chatMap.get(event.getSource()));
            }
        });
        scrollPane.setContent(chatPane);
    }
    public void addLabel(String name){
        IdLabel newLabel = new IdLabel(name);
        side.getChildren().add(newLabel);
        //VBox newPane = new VBox();
        ChatPane chatPane = new ChatPane();
        chatMap.put(newLabel,chatPane);
        newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scrollPane.setContent(chatMap.get(event.getSource()));
            }
        });
    }

    public void addMsg(String msg){
        //drawPane.getChildren().add(new Label(msg));
        if(!msg.isEmpty()) {
            ChatPane h = (ChatPane) scrollPane.getContent();
            if (h != null) {
                //h.getChildren().add(new Label(msg));
                //Cloud newCloud = new Cloud(msg);
                //h.setAlignment(newCloud, Pos.CENTER_LEFT);
                //h.getChildren().add(new Cloud(msg));
                Cloud newCloud = new Cloud(msg, true);
                h.add(newCloud, false);
                scrollPane.setVvalue(scrollPane.getHmax());
            }
        }
    }


    @Override
    public void handle(javafx.event.ActionEvent event) {
        if(event.getSource().equals(sendButton)){
            addMsg(text.getText());
        }
    }
}
