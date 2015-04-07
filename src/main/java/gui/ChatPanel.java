package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class ChatPanel extends JPanel{


    private int widht = 300;
    private int height = 200;
    private Color backgroundC = new Color(200,200,200);
    private Font font = new Font("Verdana", Font.BOLD, 12);

    public ChatPanel(){
        this.setSize(widht, height);
        this.setBackground(backgroundC);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JTextField Chats = new JTextField("Chats");
        Chats.setFont(font);
        this.add(Chats);
        this.add(new IdPanel(0,200,"AllChat"));
        this.add(new IdPanel(0,300,"192.168.2.1"));
    }



}
