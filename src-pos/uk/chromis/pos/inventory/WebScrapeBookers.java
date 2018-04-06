/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.inventory;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import uk.chromis.pos.util.SwingFXWebView;

/**
 *
 * @author john
 */
public class WebScrapeBookers extends JFrame {
    
    private String url;
    private SwingFXWebView WebView = null;
    
    public WebScrapeBookers() {
    }
    
    public void StartScraper( String starturl, ActionListener actionListener ) {
        url = starturl;
        
        // Run this later:
        SwingUtilities.invokeLater(new Runnable() {  
            @Override
            public void run() {  
                
                WebView = new SwingFXWebView( url, actionListener );  
                 
                getContentPane().add( WebView );  
                 
                setMinimumSize(new Dimension(1024, 768));  
                setVisible(true);  
            }  
        });
    }
    
    public void findCode( String code ) {
        if( WebView != null ){
            String url = getStartUrl( code );
            WebView.setUrl( url );
        }    
    }
    
    public String getStartUrl( String code ) {
        return "https://www.booker.co.uk/catalog/products.aspx?categoryName=Default%20Catalog&keywords=" + code;
    }
    
}
