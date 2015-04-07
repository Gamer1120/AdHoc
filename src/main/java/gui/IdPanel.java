package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class IdPanel extends JPanel {


    private int widht = 300;
    private int height = 100;
    private String userName;
    private Color backgroundC = new Color(191,191,191);

    public IdPanel(int x, int y, String userName){
        this.setSize(widht, height);
        this.setBackground(backgroundC);
        this.setLayout(new GridLayout(1,1));
        this.setVisible(true);
        this.userName = userName;



        JTextField userNameField = new JTextField(userName);
        this.add(userNameField);
    }

    public String getUserName(){
        return userName;
    }
}
