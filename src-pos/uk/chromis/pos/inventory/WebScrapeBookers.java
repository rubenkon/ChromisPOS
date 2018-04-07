/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.inventory;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import uk.chromis.pos.forms.AppConfig;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.util.SwingFXWebView;

/**
 *
 * @author john
 */
public class WebScrapeBookers extends JFrame {
    
    private String url;
    private SwingFXWebView WebView = null;
    private String cookies;
    File configFile;
    
    public WebScrapeBookers() {
    }
    
    public void StartScraper( String starturl, ActionListener actionListener ) {
        url = starturl;

        configFile = new File( System.getProperty("user.home"),
                    AppLocal.APP_ID + ".WebScrapeBookers" );
        
        loadState();
        
        // Run this later:
        SwingUtilities.invokeLater(new Runnable() {  
            @Override
            public void run() {  
                
                WebView = new SwingFXWebView( url, cookies, actionListener );  
                 
                getContentPane().add( WebView );  
                 
                setMinimumSize(new Dimension(1024, 768));  
                setVisible(true);  
            }  
        });
    }

    public void loadState() {
        
        try {
            cookies = new Scanner( configFile ).useDelimiter("\\Z").next();
        } catch (FileNotFoundException ex) {
        }  
    }
    
    public void saveState() {
        if( WebView != null ) {
            cookies = WebView.getCookies();
            
            OutputStream out = null;
            try {
                out = new FileOutputStream(configFile);
                out.write( cookies.getBytes() );
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(WebScrapeBookers.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WebScrapeBookers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    public void findCode( String code ) {
        if( WebView != null ){
            String url = "https://www.booker.co.uk/catalog/products.aspx?categoryName=Default%20Catalog&keywords=" + code;
            WebView.setUrl( url );
        }    
    }
    
    public String getStartUrl( ) {
        return "https://www.booker.co.uk/";
    }
    
}
