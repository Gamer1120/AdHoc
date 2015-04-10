package com.procoder.gui;


import com.procoder.AdhocApplication;
import com.procoder.LongApplicationLayer;
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
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by reneb_000 on 7-4-2015.
 */
@SuppressWarnings("restriction")
public class Main extends Application implements
        EventHandler<javafx.event.ActionEvent>, Observer, AdhocGUI {

    private static final boolean DEBUG = true;

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
    private TextField text;
    private Insets padding;
    private IdLabel selected;
    private IdLabel allChat;

    private InetAddress sender;
    private AdhocApplication applicationLayer;

    private PopOver popover;
    private PopoverMenu popoverMenu;
    private String ownIp;

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

        Scene mainScene = new Scene(mainPane, 1000, 900);
        mainScene.getStylesheets().add(this.getClass().getClassLoader().getResource("myStyle.css").toURI().toString());

        addLabel("192.168.2.2");

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
        processString("Ikke", "", "Dit is een test");
        processString("Jije","", "Dit is er ook nog een");
        processString("192.168.2.2",ownIp, "TEST");

        if (!DEBUG) {
            sender = InetAddress.getLocalHost();
            applicationLayer = new LongApplicationLayer(this);
            applicationLayer.getKnownHostList().addObserver(this);
        }
    }
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Setup
    private void setOwnIp() {
        try {
            ownIp = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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
        optionButton = new Button("", v);
        optionButton.setOnAction(this);
        sendButton.setOnAction(this);

        commandPanel.getChildren().addAll(text, sendButton, optionButton);
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
        ChatPane h = getChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new Cloud(msg, source), true);
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
        // TODO
    }
    @Override
    public void processImage(String source, String destination, Image img) {
        ChatPane h = getChatPane(source, destination);
        Thread t = new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.add(new Cloud(img, source), true);
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
    public void processAudio(String user, File sound){
        //TODO
    }



    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Send
    public void sendString(String msg) {
        // drawPane.getChildren().add(new Label(msg));
        if (!msg.isEmpty()) {
            ChatPane h = (ChatPane) scrollPane.getContent();
            if (h != null) {
                Cloud newCloud = new Cloud(msg, true);
                h.add(newCloud, false);
                text.setText("");
                if (!DEBUG) {
                    applicationLayer.sendString(sender, msg);
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
                            h.add(new Cloud(new Image(img.toURI().toString())),
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
        //TODO
    }
    public void sendAudio(File file){
        //todo
    }




    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //Events
    @Override
    public void handle(javafx.event.ActionEvent event) {
        if (event.getSource().equals(sendButton)) {
            sendString(text.getText());
        } else if (event.getSource().equals(optionButton)) {
            // TODO
            if (popover.isShowing()) {
                popover.hide();
            } else {
                popover.show(optionButton);
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
            Platform.runLater(() -> addLabel(a.toString()));

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
            if (i.getAdress().equals(a.getHostName())) {
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
    private ChatPane getChatPane(String source, String destination) {
        for(IdLabel i: chatMap.keySet()){
            if(i.getAdress().equals(source)){
                return chatMap.get(i);
            }
        }
        return chatMap.get(allChat);

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
            getIdLabel(inactive).setActive(false);
        }
    }

    public void setActive(InetAddress active) {
        IdLabel l = getIdLabel(active);
        if (l != null) {
            getIdLabel(active).setActive(true);
        }
        // getIdLabel(active).setActive(true);
    }

    public static void main(String[] args) {
        launch();
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
