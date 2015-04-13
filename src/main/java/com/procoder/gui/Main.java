package com.procoder.gui;


import com.procoder.AdhocApplication;
import com.procoder.LongApplicationLayer;
import com.procoder.util.NetworkUtils;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by reneb_000 on 7-4-2015.
 */
@SuppressWarnings("restriction")
public class Main extends Application implements
        EventHandler<javafx.event.ActionEvent>, Observer, AdhocGUI {

    public final static HashSet<String> images = new HashSet<String>(Arrays.asList("png","bmp","jpeg","jpg"));
    public final static HashSet<String> audios = new HashSet<String>(Arrays.asList("mp3"));


    private static final boolean DEBUG = false;

    private VBox side;
    private BorderPane mainPane;
    private BorderPane center;
    private HBox commandPanel; // onder textfield voor invullen voor tekst en
                               // knop om te verzenden
    private ScrollPane scrollPane;
    private HashMap<IdLabel, ChatPane> chatMap;
    private Set<InetAddress> knownAdresses = new HashSet<InetAddress>();

    private Button sendButton;
    private Button optionButton;
    private Button smileyButton;
    private TextField text;
    private Insets padding;
    private IdLabel selected;
    private IdLabel allChat;

    private AdhocApplication applicationLayer;

    private PopOver popover;
    private PopOver smileyOver;
    private PopoverMenu popoverMenu;
    private String ownIp;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("AWESOME ADHOC");
        mainPane = new BorderPane();
        chatMap = new HashMap<IdLabel, ChatPane>();
        padding = new Insets(10);
        selected = null;
        setOwnIp();
        setupCenter();
        setupSideBar();
        Scene mainScene = new Scene(mainPane, 1200, 900);
        mainScene.getStylesheets().add(this.getClass().getClassLoader().getResource("myStyle.css").toURI().toString());

        //addLabel("192.168.2.2");

        ChatPane h = (ChatPane) scrollPane.getContent();
        // h.add(new Cloud("test", false), true);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.setScene(mainScene);
        primaryStage.show();
        //processString("Ikke", "", "Dit is een test");
        //processString("Jije","", "Dit is er ook nog een");
        //processString("192.168.2.2",ownIp, "TEST");

        if (!DEBUG) {
            applicationLayer = new LongApplicationLayer(this);
            applicationLayer.getKnownHostList().addObserver(this);
        }
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Setup
    private void setOwnIp() {
        ownIp = NetworkUtils.getLocalHost().getHostAddress();
    }

    private void setupCenter() {
        center = new BorderPane();

        scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(Double.MAX_VALUE);
        scrollPane.setFitToWidth(true);

        //scrollPane.setStyle("-fx-fit-to-height: false; -fx-fit-to-width: true;-fx-background-size:contain;");
        scrollPane.setId("scroll");
        setBgScrollpane();
        //scrollPane.setPrefViewportHeight(500);

        scrollPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.SPACE) {
                    scrollPane.setVvalue(scrollPane.getVmax());
                }
            }
        });

        commandPanel = new HBox();
        // ipField = new TextField("TODO");
        text = new TextField();
        text.setPrefWidth(500);
        text.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    sendString(text.getText());
                    toBottomScroll();
                }
            }
        });
        popoverMenu = new PopoverMenu(this);
        popover = new PopOver(popoverMenu);
        // popover.setHideOnEscape(true);
        popover.arrowLocationProperty().setValue(
                PopOver.ArrowLocation.BOTTOM_CENTER);

        text.requestFocus();
        sendButton = new Button("Send");

        ImageView v = new ImageView();
        Image i = new Image(this.getClass().getClassLoader()
                .getResourceAsStream("gear.png"));
        v.setImage(i);
        ImageView smv = new ImageView();
        Image smi = new Image(this.getClass().getClassLoader()
                .getResourceAsStream("smiley.png"));
        smv.setImage(smi);

        optionButton = new Button("", v);
        smileyButton = new Button("",smv);
        smileyButton.setOnAction(this);
        optionButton.setOnAction(this);
        sendButton.setOnAction(this);

        smileyOver = new PopOver(new SmileyPanel(this));
        smileyOver.setDetachable(false);
        smileyOver.arrowLocationProperty().setValue(
                PopOver.ArrowLocation.BOTTOM_CENTER);

        commandPanel.getChildren().addAll(text, sendButton, optionButton, smileyButton);
        center.setBottom(commandPanel);
        center.setCenter(scrollPane);
        mainPane.setCenter(center);
    }

    private void setBgScrollpane() {
        File file = new File("background.png");
        scrollPane.setStyle("-fx-background-image:url(" + file.toURI() + ");");
    }

    private void setupSideBar() {
        side = new VBox();
        side.setPrefSize(300, 900);
        side.setStyle("-fx-background-color: #FFFFFF;");
        side.getChildren().add(new ChatLabel());
        addAllChat();
        mainPane.setLeft(side);

    }

    public void addAllChat() {
        allChat = new IdLabel("AllChat");
        allChat.setSelected(true);
        selected = allChat;
        side.getChildren().add(allChat);
        ChatPane chatPane = new ChatPane(allChat);

        chatMap.put(allChat, chatPane);
        allChat.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                idLabelClick(event);
            }
        });
        scrollPane.setContent(chatPane);
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Proces
    @Override
    public void processString(String source, String destination, String msg) {
        ChatPane h = getReceiveChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new StringCloud(msg, source), true);
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

    @Override
    public void processFile(String source, String destination, File file) {
        ChatPane h = getReceiveChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new FileCloud(file, source), true);
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

    @Override
    public void processImage(String source, String destination, Image img) {
        ChatPane h = getReceiveChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new ImageCloud(img, source), true);
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

    public void processAudio(String source, String destination, File sound){
        ChatPane h = getReceiveChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new AudioCloud(sound, source), true);
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

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Send
    public void sendString(String msg) {
        // drawPane.getChildren().add(new Label(msg));
        if (!msg.isEmpty()) {
            ChatPane h = (ChatPane) scrollPane.getContent();
            if (h != null) {
                Cloud newCloud = new StringCloud(msg);
                h.add(newCloud, false);
                text.setText("");
                if (!DEBUG) {
                    applicationLayer.sendString(selected.getInetAdress(), msg);
                }
            }
        }

        scrollPane.setVvalue(scrollPane.getVmax());
    }

    public void sendImage(File img) {
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new ImageCloud(new Image(img.toURI().toString())),
                                    false);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();

        if (!DEBUG) {
            applicationLayer.sendImage(selected.getInetAdress(), img);
        }
    }

    public void sendFile(File file){
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new FileCloud(file), false);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();

        if (!DEBUG) {
            applicationLayer.sendFile(selected.getInetAdress(), file);
        }
    }

    public void sendAudio(File file){
        ChatPane h = (ChatPane) scrollPane.getContent();
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new AudioCloud(file), false);
                        }
                    }
                });
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
        toBottomScroll();

        if (!DEBUG) {
            applicationLayer.sendAudio(selected.getInetAdress(), file);
        }
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Events
    @Override
    public void handle(javafx.event.ActionEvent event) {
        if (event.getSource().equals(sendButton)) {
            sendString(text.getText());
        } else if (event.getSource().equals(optionButton)) {
            if (popover.isShowing()) {
                popover.hide();
            } else {
                popover.show(optionButton);
            }
        }else if(event.getSource().equals(smileyButton)){
            if(smileyOver.isShowing()){
                smileyOver.hide();
            }else{
                smileyOver.show(smileyButton);
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Set<InetAddress> newAdress = new HashSet<>((Set<InetAddress>) arg);
        Set<InetAddress> copie = new HashSet<InetAddress>();
        copie.addAll(knownAdresses);

        copie.removeAll(newAdress);
        for (InetAddress a : copie) {
            updateStatus(a, false);
        }

        for (InetAddress a : knownAdresses) {
            if (newAdress.contains(a)) {
                updateStatus(a, true);
            }
        }

        newAdress.removeAll(knownAdresses);
        for (InetAddress a : newAdress) {
            Platform.runLater(() -> addLabel(a.getHostAddress()));

            knownAdresses.add(a);
        }

    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Getters
    public PopOver getPopover() {
        return popover;
    }

    private IdLabel getIdLabel(InetAddress a) {
        for (IdLabel i : chatMap.keySet()) {
            if (i.getAdress().equals(a.getHostAddress())) {
                return i;
            }
        }
        return null;
    }

    public HashMap<IdLabel, ChatPane> getChatMap(){
        return chatMap;
    }

    public IdLabel getSelected(){
        return selected;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    private ChatPane getReceiveChatPane(String source, String destination) {
        String comparer = ownIp.equals(destination) ? source : destination;
        for(IdLabel i: chatMap.keySet()){
            if (i.getAdress().equals(comparer)) {
                return chatMap.get(i);
            }
        }
        return chatMap.get(allChat);

    }

    public void addSmiley(String s){
        text.setText(text.getText() + s);
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Overige shit
    public void addLabel(String name) {
        IdLabel newLabel = new IdLabel(name);
        side.getChildren().add(newLabel);
        ChatPane chatPane = new ChatPane(newLabel);
        chatMap.put(newLabel, chatPane);
        newLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                idLabelClick(event);
            }
        });
    }

    private void idLabelClick(MouseEvent event) {
        if (selected != null) {
            selected.setSelected(false);
        }
        selected = (IdLabel) event.getSource();
        scrollPane.setContent(chatMap.get(selected));
        selected.setSelected(true);
    }

    public void updateStatus(InetAddress address, boolean active) {
        Platform.runLater(() -> {
            if (active) {
                setActive(address);
            } else {
                setInactive(address);
            }
        });

    }

    public void setInactive(InetAddress inactive) {
        IdLabel l = getIdLabel(inactive);
        if (l != null) {
            l.setActive(false);
        }
    }

    public void setActive(InetAddress active) {
        IdLabel l = getIdLabel(active);
        if (l != null) {
            l.setActive(true);
        }
        // getIdLabel(active).setActive(true);
    }

    private void toBottomScroll() {
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> scrollPane.setVvalue(scrollPane
                        .getVmax()));
                return null;
            }
        });
        t.setDaemon(true);
        t.start();
    }




}
