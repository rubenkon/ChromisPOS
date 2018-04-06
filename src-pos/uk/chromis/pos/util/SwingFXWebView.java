/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.util;

import com.sun.javafx.application.PlatformImpl;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JPanel;
  
/** 
 * SwingFXWebView 
 */  
public class SwingFXWebView extends JPanel {  
     
    private Stage stage;  
    private WebView browser;  
    private JFXPanel jfxPanel;  
    private JButton okButton;  
    private JButton cancelButton;  
    private WebEngine webEngine;  
    private String  url;
    private ActionListener mActionListener;
    
    public SwingFXWebView( String starturl, ActionListener actionListener ){  
        url = starturl;
        mActionListener = actionListener;
        initComponents();          
    }  
  
    private void initComponents(){  
         
        jfxPanel = new JFXPanel();  
        createScene();  
         
        setLayout(new BorderLayout());  
        add(jfxPanel, BorderLayout.CENTER);  
         
        okButton = new JButton();  
        okButton.addActionListener(mActionListener);  
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages");
        okButton.setText( bundle.getString("Button.OK"));         
        jPanel.add(okButton, BorderLayout.EAST );  

        cancelButton = new JButton();  
        cancelButton.addActionListener(mActionListener);  
        cancelButton.setText( bundle.getString("Button.Cancel"));         
        jPanel.add(cancelButton, BorderLayout.WEST );  
 
        add( jPanel, BorderLayout.SOUTH);  
    }     
     
    public void setUrl( String url ) {
        webEngine.load(url);
    }
    
    /** 
     * createScene 
     * 
     * Note: Key is that Scene needs to be created and run on "FX user thread" 
     *       NOT on the AWT-EventQueue Thread 
     * 
     */  
    private void createScene() {  
        PlatformImpl.startup(new Runnable() {  
            @Override
            public void run() {  
                 
                stage = new Stage();  
                 
                stage.setTitle("ChromisPOS WebView");  
                stage.setResizable(true);  
   
                StackPane root = new StackPane();  
                Scene scene = new Scene(root,80,20);  
                stage.setScene(scene);  
                 
                // Set up the embedded browser:
                browser = new WebView();
                webEngine = browser.getEngine();
                webEngine.load( url );
                
                ObservableList<Node> children = root.getChildren();
                children.add(browser);                     
                 
                jfxPanel.setScene(scene);  
            }  
        });  
    }
}    
