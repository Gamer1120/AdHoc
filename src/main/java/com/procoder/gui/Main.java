package com.procoder.gui;


import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.controlsfx.control.PopOver;

import com.procoder.AdhocApplication;
import com.procoder.LongApplicationLayer;



/**
 * Created by reneb_000 on 7-4-2015.
 */
@SuppressWarnings("restriction")
public class Main extends Application implements EventHandler<javafx.event.ActionEvent>, Observer {

    private static final boolean DEBUG = false;

    private VBox side;
    private BorderPane mainPane;
    private BorderPane center;
    private HBox commandPanel; //onder textfield voor invullen voor tekst en knop om te verzenden
    private ScrollPane scrollPane;
    private HashMap<IdLabel, ChatPane> chatMap;
    private Set<InetAddress> knownAdresses = new HashSet<InetAddress>();

    private Button sendButton;
    private Button optionButton;
    private TextField text;
    private Insets padding;
    private IdLabel selected;
 
    private InetAddress sender;
    private AdhocApplication applicationLayer;

    private PopOver popover;
    private PopoverMenu popoverMenu;

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
        //addLabel("192.168.2.2");

        ChatPane h = (ChatPane)scrollPane.getContent();
        //h.add(new Cloud("test", false), true);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


        primaryStage.setScene(mainScene);
        primaryStage.show();
        //sendString("Ikke", "Dit is een test");
        //sendString("Jije", "Dit is er ook nog een");

        if(!DEBUG) {
            sender = InetAddress.getLocalHost();
            applicationLayer = new LongApplicationLayer(this);
            applicationLayer.getKnownHostList().addObserver(this);
        }
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
                    toBottomScroll();
                }
            }
        });
        popoverMenu = new PopoverMenu(this);
        popover = new PopOver(popoverMenu);
        //popover.setHideOnEscape(true);
        popover.arrowLocationProperty().setValue(PopOver.ArrowLocation.BOTTOM_CENTER);



        text.requestFocus();
        sendButton = new Button("Send");

        ImageView v = new ImageView();
        Image i = new Image(this.getClass().getClassLoader().getResourceAsStream("gear.png"));
        v.setImage(i);
        optionButton = new Button("", v);
        optionButton.setOnAction(this);
        sendButton.setOnAction(this);

        commandPanel.getChildren().addAll(text, sendButton, optionButton);
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
                if(!DEBUG) {
                    applicationLayer.send(sender, msg);
                }
            }
        }

        scrollPane.setVvalue(scrollPane.getVmax());
    }

    public void processString(String user, String msg){
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task(){
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        if(h!=null){
                            h.add(new Cloud(msg, user), true);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();
    }

    public void processFile(String user, File file){
        //TODO
    }

    public void processImage(String user, Image img){
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task(){
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        if(h!=null){
                            h.add(new Cloud(img, user), true);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();
    }

    private void sendImage(File img) {
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task(){
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        if(h!=null){
                            h.add(new Cloud(new Image(img.toURI().toString())), false);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();

        if(!DEBUG){
            applicationLayer.send(selected.getInetAdress(), img);
        }
    }

    private void toBottomScroll(){
        Thread t = new Thread(new Task(){
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() ->scrollPane.setVvalue(scrollPane.getVmax()));
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void handle(javafx.event.ActionEvent event) {
        if(event.getSource().equals(sendButton)){
            addMsg(text.getText());
        }else if(event.getSource().equals(optionButton)){
            //TODO
            if(popover.isShowing()){
                popover.hide();
            }else{
                popover.show(optionButton);
            }

        }else if(event.getSource().equals(popoverMenu.getUploadButton())){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(new Stage());
            //Image image = new Image(file.toURI().toString());
            if(file!=null) {
                sendImage(file);
            }
            popover.hide();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Set<InetAddress> newAdress = new HashSet<>((Set<InetAddress>)arg);
        Set<InetAddress> copie = new HashSet<InetAddress>();
        copie.addAll(knownAdresses);

        copie.removeAll(newAdress);
        for(InetAddress a:copie){
            updateStatus(a, false);
        }

        for(InetAddress a:knownAdresses){
            if(newAdress.contains(a)){
                updateStatus(a, true);
            }
        }

        newAdress.removeAll(knownAdresses);
        for(InetAddress a:newAdress){
            Platform.runLater(() -> addLabel(a.toString()));

            knownAdresses.add(a);
        }

    }

    public void updateStatus(InetAddress address, boolean active) {
        Platform.runLater(() -> {
            if(active) {
                setActive(address);
            } else {
                setInactive(address);
            }
        });

    }


    public void setInactive(InetAddress inactive) {
        IdLabel l = getIdLabel(inactive);
        if(l!=null) {
            getIdLabel(inactive).setActive(false);
        }
    }


    private IdLabel getIdLabel(InetAddress a){
        for(IdLabel i:chatMap.keySet()){
            if(i.getAdress().equals(a.getHostName())){
                return i;
            }
        }
        return null;
    }

    public void setActive(InetAddress active) {
        IdLabel l = getIdLabel(active);
        if(l!=null) {
            getIdLabel(active).setActive(true);
        }
        //getIdLabel(active).setActive(true);
    }

    public static void main(String[] args){
        launch();
    }
}
