/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.inventory;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import uk.chromis.data.gui.JImageEditor;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.ticket.ProductInfoExt;
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
    
    private String ExtractString( String html, String preamble, String postamble  ) {
        String extract = "";
        
        int nStart = html.indexOf(preamble);
        if( nStart > 0 ) {
            nStart += preamble.length();
            if( postamble.isEmpty() ) {
                extract = html.substring(nStart);
            } else {
                int nEnd = html.indexOf(postamble, nStart );
                if( nEnd > 0 ) {
                    extract = html.substring( nStart, nEnd );
                }
            }
        }
        
        return extract;
    }
    
    public ProductInfoExt decodeCurrentPage( ProductInfoExt infoOld  ) {
        ProductInfoExt infoNew = new ProductInfoExt( infoOld );
        String productInfo = WebView.getPageSource();
        if( !productInfo.isEmpty() ) {
            String value;
            Double taxRate = 0.0;
            Double packSize = 1.0;
            Properties props = infoNew.getProperties();

            value = ExtractString( productInfo, "<h3>", "<span ");
            if( !value.isEmpty() ) {
                infoNew.setName( value );
                infoNew.setDisplay( "<html>" + value );
            }
            
            value = ExtractString( productInfo, "Case of<br>", "</p>");
            if( !value.isEmpty() ) {
                packSize = Double.parseDouble(value);
                infoNew.setPackQuantity( packSize );
            }

            value = ExtractString( productInfo, "li>Code: <span>", "</span>");
            if( !value.isEmpty() ) {
                infoNew.setReference( value );
            }
            
            value = ExtractString( productInfo, "<li id=\"BPIH_liVAT\">VAT: <span>", "%</span>");
            if( !value.isEmpty() ) {
                taxRate = Double.parseDouble(value);
                infoNew.setTaxRate( taxRate/100.0 );
                infoNew.setTaxCategoryID(null);
            }

            value = ExtractString( productInfo, "<li id=\"BPIH_liWSP\">WSP: <span>£", "</span>");
            if( !value.isEmpty() ) {
                infoNew.setPriceBuy( Double.parseDouble(value) / packSize );
            }
            
            value = ExtractString( productInfo, "<li id=\"BPIH_liRRP\">RRP: <span>£", "</span>");
            if( !value.isEmpty() ) {
                infoNew.setPriceSell( Double.parseDouble(value) / (1+(taxRate/100.0) ) );
            }
            value = ExtractString( productInfo, "cies-flashimage\"><img style=\"width:200;\" src=\"", "\" alt=\"" );
            if( !value.isEmpty() ) {
                JImageEditor image = new JImageEditor();
                image.LoadFromUrl( "https://www.booker.co.uk/" + value );
                infoNew.setImage( image.getImage() );
            }
            
            value = ExtractString( productInfo, "<li id=\"BPIH_liUnitInfo\">Alcohol Units: <span>", "</span>" );
            if( !value.isEmpty() ) {
                if( Double.parseDouble(value) > 0.0 ) {
                    props.setProperty( "Age_Min", "18.0" );
                    infoNew.setCanDiscount(false);
                }
            }
                    
            props.setProperty( "Supplier", "Bookers" );
            infoNew.setProperties( props );
        }
       
       return( infoNew );
        
    }

    public void checkEnableOK() {
        boolean bEnable = false;
        
        String html = WebView.getPageSource();
        if( !html.isEmpty() ) {
            String str = ExtractString( html, "<h3>", "<span ");
            if( !str.isEmpty() ) {
                bEnable = true;
            }
        }
        WebView.enableOK(bEnable);
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
