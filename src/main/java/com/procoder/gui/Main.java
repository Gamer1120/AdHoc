package com.procoder.gui;

import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.net.InetAddress;
import java.util.*;



/**
 * Created by reneb_000 on 7-4-2015.
 */
public class Main extends Application implements EventHandler<javafx.event.ActionEvent>, Observer {

    private VBox side;
    private BorderPane mainPane;
    private BorderPane center;
    private HBox commandPanel; //onder textfield voor invullen voor tekst en knop om te verzenden
    private ScrollPane scrollPane;
    private HashMap<IdLabel, ChatPane> chatMap;
    private Set<InetAddress> knownAdresses = new HashSet<InetAddress>();

    private Button sendButton;
    private TextField text;
    private Insets padding;
    private IdLabel selected;



    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("AWESOME ADHOC");
        mainPane = new BorderPane();
        chatMap = new HashMap<IdLabel, ChatPane>();
        padding = new Insets(10);
        selected = null;
        setupCenter();
        setupSideBar();

        Scene mainScene = new Scene(mainPane, 1000, 900);

        //mainScene.getStylesheets().add("Css.css");
        addLabel("192.168.2.2");

        ChatPane h = (ChatPane)scrollPane.getContent();
        //h.add(new Cloud("test", false), true);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
            }
        });


        primaryStage.setScene(mainScene);
        primaryStage.show();
        sendString("Ikke", "Dit is een test");
        sendString("Jije", "Dit is er ook nog een");

    }

    private void setupCenter() {
        center = new BorderPane();

        scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(Double.MAX_VALUE);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-fit-to-height: false; -fx-fit-to-width: true;");
        //scrollPane.setPrefViewportHeight(500);
        scrollPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode()==KeyCode.SPACE){
                    scrollPane.setVvalue(scrollPane.getVmax());
                    System.out.println("SCROLLED");
                }
            }
        });

        commandPanel = new HBox();
        //ipField = new TextField("TODO");
        text = new TextField();
        text.setPrefWidth(500);
        text.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    addMsg(text.getText());
                    Thread t = new Thread(new Task(){
                        @Override
                        protected Object call() throws Exception {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Platform.runLater(() ->scrollPane.setVvalue(scrollPane.getVmax()));
                            //System.out.println("WAITED");
                            return null;
                        }
                    });
                    t.setDaemon(true);
                    t.start();


                }
            }
        });

        text.requestFocus();
        sendButton = new Button("Send");

        sendButton.setOnAction(this);

        commandPanel.getChildren().addAll(text, sendButton);
        center.setBottom(commandPanel);
        center.setCenter(scrollPane);
        mainPane.setCenter(center);
    }

    private void setupSideBar() {
        side = new VBox();
        side.setPrefSize(300, 900);
        side.setStyle("-fx-background-color: #FFFFFF;");
        side.getChildren().add(new ChatLabel());
        addAllChat();
        mainPane.setLeft(side);


    }

    public void addAllChat(){
        IdLabel newLabel = new IdLabel("AllChat");
        newLabel.setSelected(true);
        selected = newLabel;
        side.getChildren().add(newLabel);
        ChatPane chatPane = new ChatPane();

        chatMap.put(newLabel, chatPane);
        newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                idLabelClick(event);
            }
        });
        scrollPane.setContent(chatPane);
    }
    public void addLabel(String name){
        IdLabel newLabel = new IdLabel(name);
        side.getChildren().add(newLabel);
        ChatPane chatPane = new ChatPane();
        chatMap.put(newLabel, chatPane);
        newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                idLabelClick(event);
            }
        });
    }

    private void idLabelClick(MouseEvent event){
        if (selected != null) {
            selected.setSelected(false);
        }
        selected = (IdLabel) event.getSource();
        scrollPane.setContent(chatMap.get(selected));
        selected.setSelected(true);
    }

    public void addMsg(String msg){
        //drawPane.getChildren().add(new Label(msg));
        if(!msg.isEmpty()) {
            ChatPane h = (ChatPane) scrollPane.getContent();
            if (h != null) {
                Cloud newCloud = new Cloud(msg, true);
                h.add(newCloud, false);
                text.setText("");
                //scrollPane.setVvalue(1.0);
                //System.out.println(scrollPane.getViewportBounds() + " Heigt of content " + h.getHeight());


                //System.out.println(scrollPane.getHeight());
            }
        }

        scrollPane.setVvalue(scrollPane.getVmax());
    }

    public void sendString(String user, String msg){
        ChatPane h = (ChatPane) scrollPane.getContent();
        if(h!=null){
            h.add(new Cloud(msg, user),true);
            scrollPane.setVvalue(scrollPane.getVmax());
        }
    }

    @Override
    public void handle(javafx.event.ActionEvent event) {
        if(event.getSource().equals(sendButton)){
            addMsg(text.getText());
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Set<InetAddress> newAdres = (Set<InetAddress>) arg;
        Set<InetAddress> copie = new HashSet<InetAddress>();
        copie.addAll(knownAdresses);

        copie.removeAll(newAdres);
        for(InetAddress a:copie){
            setInactive(a);
        }

        for(InetAddress a:knownAdresses){
            if(newAdres.contains(a)){
                setActive(a);
            }
        }

        newAdres.removeAll(knownAdresses);
        for(InetAddress a:newAdres){
            addLabel(a.toString());
            knownAdresses.add(a);
        }

    }

    public void setInactive(InetAddress inactive) {
        getIdLabel(inactive).setActive(false);
    }


    private IdLabel getIdLabel(InetAddress a){
        for(IdLabel i:chatMap.keySet()){
            if(i.getAdress().equals(a.toString())){
                return i;
            }
        }
        return null;
    }

    public void setActive(InetAddress active) {
        getIdLabel(active).setActive(true);
    }

    public static void main(String[] args){
        launch();
    }
}
