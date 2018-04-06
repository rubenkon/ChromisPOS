/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.inventory;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import uk.chromis.pos.util.SwingFXWebView;

/**
 *
 * @author john
 */
public class WebScrapeBookers extends JPanel {
    
    private String url;
    private JFrame frame;
    
    public WebScrapeBookers() {
        
        url = getStartUrl();
        
        // Run this later:
        SwingUtilities.invokeLater(new Runnable() {  
            @Override
            public void run() {  
                frame = new JFrame();  
                 
                frame.getContentPane().add(new SwingFXWebView( url ));  
                 
                frame.setMinimumSize(new Dimension(640, 480));  
                frame.setVisible(true);  
            }  
        });
    }
    
    public String getStartUrl() {
        return "http://www.booker.co.uk";
    }
    
}
