/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.awt.Dimension;
import javax.swing.*;

/**
 *
 * @author Kristian Nielsen
 */
public class ServerWindow {
    
    JFrame mf;
    JPanel mp;
    
    public static void main(String[] args) {
        new ServerWindow().setup();
    }
    
    public void setup(){
        mf = new JFrame();
        mf.setSize(new Dimension(300, 600));
        mf.setVisible(true);
    }
    
}
