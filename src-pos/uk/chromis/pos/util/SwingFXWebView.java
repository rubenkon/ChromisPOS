/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.util;

import com.sun.javafx.application.PlatformImpl;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jfree.chart.util.TextUtils;
  
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
    CookieManager cookieManager;
    private String  url;
    private ActionListener mActionListener;
    private String strCookies;
    
    public SwingFXWebView( String starturl, String cookies, ActionListener actionListener ){  
        url = starturl;
        mActionListener = actionListener;
        strCookies = cookies;
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
     

    public void setCookies( String cookies ) {
        String[] aCookies = cookies.split(";");
    
        for (String s: aCookies) {
            int pos1 = s.indexOf('@');
            int pos2 = s.indexOf(':');
            if( pos1 > 0 && pos2 > 0 ) {
                String name = s.substring(0,pos1);
                String domain = s.substring(pos1+1,pos2);
                String value = s.substring(pos2+1);

                HttpCookie cook = new HttpCookie( name, value );
                cookieManager.getCookieStore().add( URI.create(domain), cook);
            }
        }
        strCookies = cookies;
    }
    
    public String getCookies() {

        final List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();

        if (cookies != null && cookies.size() > 0) {
            strCookies = "";
            for( int n = 0; n < cookies.size(); ++n ) {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                if( n > 0 ) strCookies = strCookies + ";";
                strCookies = strCookies + cookies.get(n).getName()
                        + "@" + cookies.get(n).getDomain()
                        + ":" + cookies.get(n).getValue();
            }
        }
        return strCookies;
    }
    
    public void setUrl( String newUrl ) {
        url = newUrl;
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                   webEngine.load(url);
            }
        });
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

                cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(cookieManager);
                
                if( strCookies != null ) {
                    setCookies( strCookies );
                }
                
                // Set up the embedded browser:
                browser = new WebView();
                
                webEngine = browser.getEngine();
                URI uri = URI.create(url);
                webEngine.load( url );
                
                ObservableList<Node> children = root.getChildren();
                children.add(browser);                     
                 
                jfxPanel.setScene(scene);  
            }  
        });  
    }
}    
