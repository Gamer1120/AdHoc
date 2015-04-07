package gui;



import javax.swing.*;
import java.awt.*;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class MainFrame extends JFrame {

    public MainFrame(){
        this.setLayout(new BorderLayout());
        this.setSize(800, 600);
        this.setVisible(true);

        //IdPanel test = new IdPanel(0,0,"Test");


        this.add(new ChatPanel(), BorderLayout.CENTER);


    }



    public static void main(String[] args){
        new MainFrame();
    }

}
