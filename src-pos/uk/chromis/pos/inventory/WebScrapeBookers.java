/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.inventory;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.util.SwingFXWebView;

/**
 *
 * @author john
 */
public class WebScrapeBookers extends JFrame {
    
    private SwingFXWebView WebView = null;
    String cookieFile;
    
    public WebScrapeBookers() {
    }
    
    public void StartScraper( String startUrl, ActionListener actionListener ) {

        cookieFile = System.getProperty("user.home") + '/' + AppLocal.APP_ID + ".cookies";
        
        // Run this later:
        SwingUtilities.invokeLater(new Runnable() {  
            @Override
            public void run() {  
                
                WebView = new SwingFXWebView( startUrl, cookieFile, actionListener );  
                 
                getContentPane().add( WebView );  
                 
                setMinimumSize(new Dimension(1024, 768));  
                pack();
                setVisible(true);  
            }  
        });
    }

    public void saveState() {
        if( WebView != null ) {
            WebView.saveCookies( cookieFile );
        }
    }
    

    public String getSearchUrl( String code ) {
        return "https://www.booker.co.uk/catalog/products.aspx?categoryName=Default%20Catalog&keywords=" + code;
    }    
        
    public void findCode( String code ) {
        if( WebView != null ){
            WebView.setUrl( getSearchUrl(code) );
        }    
    }
}
