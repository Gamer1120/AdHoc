package com.procoder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI extends JFrame {
    private static final long serialVersionUID = 6016763503029813463L;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private GUI self;
    private JTextField commandField;
    private JButton sentCommandButton;
    private String name = "you";
    private boolean clicked = false;
    private JTextField ipField;
    private Application applicationLayer;


    /**
     * Creates a new ClientGUI.
     */

    public GUI(){


        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);


        scrollPane = new JScrollPane(textArea);


        ipField = new JTextField(15);
        commandField = new JTextField(30);
        sentCommandButton = new JButton("Send");

        commandPanel.add(ipField);
        commandPanel.add(commandField);
        commandPanel.add(sentCommandButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(commandPanel, BorderLayout.PAGE_END);


        setVisible(true);
        Dimension d = new Dimension(900, 800);
        setSize(d);
        self = this;

        commandField.setFocusable(true);
        commandField.requestFocus();
		commandField.addKeyListener(new KeyAdapter() {
            @Override
			public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    //System.out.println("Pressed");
                    sendMsg();
                }
            }
        });

		
		/*Als er op de sent knop gedrukt wordt, dan wordt de thread die op de input wacht
		 * genotifyt
		 */
        sentCommandButton.addMouseListener(new MouseAdapter(){
            @Override
			public void mouseClicked(MouseEvent e){
                synchronized(self){
                    sendMsg();
                }
            }
        });

        applicationLayer = new FlagApplicationLayer(this);
    }



    /**
     * Adds a message to the textArea of this ClientGUI.
     *
     * @param msg is the message to be added.
     */

    public void sendString(String user, String msg){
    	System.out.println("[UI] [RCD]: " + msg);
        System.out.println();
        textArea.append(user+": "+msg + "\n");
    }



    /**
     * Checks whether a string is a valid IPv4 address. All credit to user
     * prmatta on stackoverflow.com for this method.
     *
     * @param ip
     *            is the String to be checked.
     * @return true if the given String represents a valid IPv4 address or false
     *         if it doesn't.
     */
    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private void sendMsg(){
        String des = ipField.getText();
        if(validIP(des)||des.isEmpty()) {

            String command = commandField.getText();
            System.out.println("[UI] [SND]: " + command);
            sendString(name, command);
            commandField.setText("");
            try {
				applicationLayer.send(InetAddress.getByName(des), command);
			} catch (UnknownHostException e) {
				// TODO Betere error-handling
				e.printStackTrace();
			}

        }else{
            System.err.println("INVALID IP");
        }
    }





    public static void main(String[] args){
        new GUI();
    }




}
