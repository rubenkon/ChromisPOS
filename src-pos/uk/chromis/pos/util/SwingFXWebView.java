/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.chromis.pos.util;

import com.sun.javafx.application.PlatformImpl;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
  
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
    private URI     startUri;
    private ActionListener mActionListener;
    private String cookieFile;
    CookieManager myCookieManager;
    
    public SwingFXWebView( String startUrl, String cookies, ActionListener actionListener ){  
        try {
            startUri = new URI(startUrl);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SwingFXWebView.class.getName()).log(Level.SEVERE, null, ex);
        }
        mActionListener = actionListener;
        cookieFile = cookies;
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

    private String CookieToString( HttpCookie cookie ) {
        String strCookie = "";
        String SEPARATOR = "#";

        String name = cookie.getName();
        String value = "*";
        if (cookie.getValue() != null && !cookie.getValue().contentEquals(""))
                value = cookie.getValue();

        String domain = "*";
        if (cookie.getDomain() != null)
                domain = cookie.getDomain();
        String path = "*";
        if (cookie.getPath() != null)
                path = cookie.getPath();
        int version = cookie.getVersion();
        String ver = String.valueOf(version);
        String expired = "*";
        if (cookie.getMaxAge() != 0 ) 
            expired = Long.toString(cookie.getMaxAge());

        strCookie = name + SEPARATOR
                + value + SEPARATOR
                + domain + SEPARATOR
                + path + SEPARATOR
                + ver + SEPARATOR
                + expired + SEPARATOR;
        
        return strCookie;
    }
    
    private HttpCookie CookieFromString( String strCookie ) {
        HttpCookie cookie = null;

        int i = 0;
        String name = null;
        String value = null;
        String domain = null;
        String path = null;
        String expired = null;
        String version = null;

        StringTokenizer tokens = new StringTokenizer( strCookie, "#");
        i++;
        while (tokens.hasMoreTokens()) {
            switch (i = tokens.countTokens()) {
            case 6:
                    name = tokens.nextToken();

                    break;
            case 5:
                    value = tokens.nextToken();
                    break;
            case 4:
                    domain = tokens.nextToken();
                    break;
            case 3:
                    path = tokens.nextToken();
                    break;
            case 2:
                    version = tokens.nextToken();
                    break;
            case 1:
                    expired = tokens.nextToken();
                    break;

            }
        }
        
        if( name != null && value != null ) {
            cookie = new HttpCookie( name, value );
            if (value.contentEquals("*"))
                    cookie.setValue(null);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setVersion(Integer.valueOf(version));
            cookie.setMaxAge( Long.parseLong(expired) );
        }
        
        return cookie;
    }
    
    public void loadCookies( String cookiefile ) {
        CookieManager manager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        Path p = Paths.get(cookiefile);

        try {
          for (String line : Files.readAllLines(p) ) {
            String[] values = line.split("\\|");
            URI uri = new URI(values[0]);

            String[] actualValues = values[1].split(":");

            if (actualValues.length < 2)
              continue;

            for (String header : actualValues[1].split("~")) {
                HttpCookie cookie = CookieFromString(header);
                if( cookie != null ) {
                  manager.getCookieStore().add(uri, cookie );
                }
            }
          }
        } catch (IOException | URISyntaxException e) {
        }

        CookieHandler.setDefault(manager);    
    }
    
    public void saveCookies( String cookiefile ) {
        
        CookieStore store = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        try {
            Path p = Paths.get(cookiefile);
            Files.write( p, ("").getBytes(), StandardOpenOption.CREATE);

            for (URI uri : store.getURIs()) {
                Map<String, List<String>> map = CookieHandler.getDefault().get(uri, new HashMap<>());

            Files.write( p, (uri + "|Cookie:").getBytes(), StandardOpenOption.APPEND);

            for (HttpCookie cookie : store.get(uri)) {
              if (cookie.hasExpired())
                continue;

              Files.write( p, (CookieToString(cookie) + "~").getBytes(), StandardOpenOption.APPEND);
            }

            Files.write( p, "\n".getBytes(), StandardOpenOption.APPEND);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    public void setUrl( String newUrl ) {
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                   webEngine.load(newUrl);
            }
        });
    }

    // Called when page has finished loading
    public void onLoadComplete() {
        
        Document doc = webEngine.getDocument();
       
        String html = (String) webEngine.executeScript("document.documentElement.outerHTML");

        int lprice = html.indexOf("<span>RRP:</span>")+18;
        String sprice = html.substring( lprice, lprice + 10 );
/*
        try {
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

             transformer.transform(new DOMSource(doc),
                     new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
        } catch (Exception ex) {
             ex.printStackTrace();
        }       
*/
        if( html != null ) {
            mActionListener.actionPerformed( new ActionEvent( browser, ACTION_PERFORMED, html ) );
        }
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

                // Set the cookies into the default store
                // before cteating and using our own store.
                // This avoids issues with the java.net and sun versions
                // of the CookieManager. Our store will inherit these cookies
                if( cookieFile != null ) {
                   loadCookies( cookieFile );
                }

                // Set up the embedded browser:
                browser = new WebView();
                webEngine = browser.getEngine();
  
                webEngine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(
                      ObservableValue<? extends Worker.State> observable,
                      Worker.State oldValue, Worker.State newValue ) {

                      if( newValue != Worker.State.SUCCEEDED ) {
                          return;
                      }
                      onLoadComplete();
                    }
                  } );

                webEngine.load( startUri.toString() );
                
                ObservableList<Node> children = root.getChildren();
                children.add(browser);                     
                 
                jfxPanel.setScene(scene);  
            }  
        });  
    }
}    
