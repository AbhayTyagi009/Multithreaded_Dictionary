package ClientMulti;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    JFrame frame;
    JLabel search, mean;
    JTextField inputArea, meanArea;
    JButton searchButton, addButton, delButton;
    JTextArea jOut;
    String HOSTNAME;
    int PORT_NUMBER;
    BufferedReader b, br;
    BufferedWriter bw;
    int FLAG;
    
    Client() {
        frame = new JFrame("Dictionary Client");
        search = new JLabel("Enter word here");
        inputArea = new JTextField("");
        mean = new JLabel("Enter meaning here");
        meanArea = new JTextField("");
        searchButton = new JButton("Search");
        addButton = new JButton("Add");
        delButton = new JButton("Delete");
        jOut = new JTextArea();
        frame.setSize(380, 400);
        int x = 20;
        int y = 40;
        search.setBounds(x, y-30, 320, 20);
        inputArea.setBounds(x, y, 320, 20);
        mean.setBounds(x, y+30, 320, 20);
        meanArea.setBounds(x, y+60, 320, 20);
        searchButton.setBounds(x, y+90, 100, 20);
        addButton.setBounds(x+110, y+90, 100, 20);
        delButton.setBounds(x+220, y+90, 100, 20);
        jOut.setBounds(x, y+120, 320, 250);
        frame.add(search);
        frame.add(inputArea);
        frame.add(mean);
        frame.add(meanArea);
        frame.add(searchButton);
        frame.add(addButton);
        frame.add(delButton);
        frame.add(jOut);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setVisible(true);
        jOut.setLineWrap(true);
        jOut.setWrapStyleWord(true);
    }
    
    public void sendToServer(int op, String word, String meaning)throws IOException {
        switch (op) {
            case 1:
                bw.write("1::::"+word);
                break;
            case 2:
                bw.write("2::::"+word+"::::"+meaning);
                break;
            case 3:
                bw.write("3::::"+word);
                break;
            default:
                throw new IOException("Error in sending data!");
        }
        bw.newLine();
        bw.flush();
    }
    
    public void recieveFromServer() throws IOException {
        if (br.readLine() != null) {
        try {
            String reply;
            String rep = "";
            while((reply = br.readLine()) != null) {
                rep += reply;
            }
            jOut.setText(rep);
        }
        catch (IOException e) {
            System.out.println("Did an oopsie! : "+e);
        }}
    }
    
    public static void main(String[] args)throws Exception {
        Client c = new Client();
        
        if (args.length == 2) {
            try {
                if (args[0]!=null)
                    c.PORT_NUMBER = Integer.parseInt(args[0]);
                else
                    c.PORT_NUMBER = 1234;

                if (args[1] != null)
                    c.HOSTNAME = args[1];
                else
                    c.HOSTNAME = "localhost";
            }
            catch(NumberFormatException e) {
                c.HOSTNAME = "localhost";
                c.PORT_NUMBER = 1234;
            }
        }
        else {
            c.HOSTNAME = "localhost";
            c.PORT_NUMBER = 1234;
        }
        
        while(true){
            
            try {
                Socket sk = new Socket(c.HOSTNAME, c.PORT_NUMBER);
                c.br = new BufferedReader(new InputStreamReader(sk.getInputStream()));
                c.bw = new BufferedWriter(new OutputStreamWriter(sk.getOutputStream()));
                
                c.searchButton.addActionListener((ActionEvent e) -> {
                    String s = c.inputArea.getText();
                    if(!s.equals("")) {
                        try {
                            c.sendToServer(1, s, null);
                        } catch (IOException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(c.frame, "Enter a word!");
                    }
                });
                
                c.addButton.addActionListener((ActionEvent e) -> {
                    String s = c.inputArea.getText();
                    String m = c.meanArea.getText();
                    if(!s.equals("") && !m.equals("")) {
                        try {
                            c.sendToServer(2, s, m);
                        } catch (IOException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(c.frame, "Enter a word and meaning!");
                    }
                });
                
                c.delButton.addActionListener((ActionEvent e) -> {
                    String s = c.inputArea.getText();
                    if(!s.equals("")) {
                        try {
                            c.sendToServer(3, s, null);
                        } catch (IOException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(c.frame, "Enter a word!");
                    }
                });
                c.recieveFromServer();
                sk.close();
            }
            catch (IOException e) {
                System.out.println("Something is wrong! :"+e);
            }
            
            c.bw.close();
        }
    }
}