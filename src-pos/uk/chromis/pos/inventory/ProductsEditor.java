//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) (c) 2015-2016
//    http://www.chromis.co.uk
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.
//
package uk.chromis.pos.inventory;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import uk.chromis.basic.BasicException;
import uk.chromis.data.gui.ComboBoxValModel;
import uk.chromis.data.gui.ListValModel;
import uk.chromis.data.gui.MessageInf;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.data.user.DirtyManager;
import uk.chromis.data.user.EditorRecord;
import uk.chromis.format.Formats;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.DataLogicSales;
import uk.chromis.pos.forms.DataLogicSystem;
import uk.chromis.pos.sales.TaxesLogic;
import uk.chromis.pos.util.BarcodeValidator;
import uk.chromis.pos.ticket.ProductInfoExt;
import uk.chromis.pos.util.AutoCompleteComboBox;

/**
 *
 * @author adrianromero
 */
public final class ProductsEditor extends JPanel implements EditorRecord {

    private SentenceList m_sentbarcodes;
    private ListValModel m_BarcodesModel;
    private final SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    private final SentenceList m_sentpromotion;
    private ComboBoxValModel m_PromotionModel;
    private final SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;
    private final SentenceList attsent;
    private ComboBoxValModel attmodel;
    private final SentenceList taxsent;
    private TaxesLogic taxeslogic;
    private final ComboBoxValModel m_CodetypeModel;
    private final SentenceList packproductsent;
    private ComboBoxValModel packproductmodel;
    private Object m_id;
    private Object pricesell;
    private boolean priceselllock = false;
    private boolean reportlock = false;
    private BarcodeValidator validate;
    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;
    private Boolean displayEdited = false;
    private String originalDisplay;
    private Properties m_PropertyOptions;
    private WebScrapeBookers webscraper = null;
    
    /**
     * Creates new form JEditProduct
     *
     * @param dlSales
     * @param dirty
     */
    public ProductsEditor(DataLogicSales dlSales, DataLogicSystem dlSystem, DirtyManager dirty) {
        initComponents();

        m_dlSales = dlSales;
        m_dlSystem = dlSystem;
        
        // Taxes sentence
        taxsent = dlSales.getTaxList();

        // Categories model
        m_sentcat = dlSales.getCategoriesList();

        m_CategoryModel = new ComboBoxValModel();

        // Barcodes model
        m_BarcodesModel = new ListValModel();

        // Promotions model
        m_sentpromotion = dlSales.getPromotionsList();
        m_PromotionModel = new ComboBoxValModel();

        // Taxes model
        taxcatsent = dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel();

        // Attributes model
        attsent = dlSales.getAttributeSetList();
        attmodel = new ComboBoxValModel();

        m_CodetypeModel = new ComboBoxValModel();
        m_CodetypeModel.add(null);
        m_CodetypeModel.add(CodeType.EAN13);
        m_CodetypeModel.add(CodeType.CODE128);
        m_jCodetype.setModel(m_CodetypeModel);
        m_jCodetype.setVisible(false);

        // Pack Product model
        packproductsent = dlSales.getPackProductList();
        packproductmodel = new ComboBoxValModel();

        m_PropertyOptions = new Properties();
        jPropertyValueText.setVisible( false );
        jPropertyValueCombo.setVisible( false );
        
        try {
            m_PropertyOptions.loadFromXML( new ByteArrayInputStream(m_dlSystem.getResourceAsXML("Product.Properties").getBytes(StandardCharsets.UTF_8)));
        } catch (IOException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
            m_PropertyOptions.put( "Product.Properties", "");
            jPropertyValueText.setVisible( true );
            jPropertyValueText.setText( "Resource load failed");
        }
        for (Map.Entry<Object, Object> e : m_PropertyOptions.entrySet()) {
          jComboProperties.addItem( (String) e.getKey() );
        }
        jComboProperties.setSelectedIndex(-1);
        jComboProperties.addActionListener( new PropertyActionListener() );
        m_jRef.getDocument().addDocumentListener(dirty);
        m_jCode.getDocument().addDocumentListener(dirty);
        m_jName.getDocument().addDocumentListener(dirty);
        m_jComment.addActionListener(dirty);
        m_jScale.addActionListener(dirty);
        m_jCategory.addActionListener(dirty);
        jComboBoxPromotion.addActionListener(dirty);
        m_jTax.addActionListener(dirty);
        m_jAtt.addActionListener(dirty);
        m_jPriceBuy.getDocument().addDocumentListener(dirty);
        m_jPriceSell.getDocument().addDocumentListener(dirty);
        m_jImage.addPropertyChangeListener("image", dirty);
        m_jstockcost.getDocument().addDocumentListener(dirty);
        m_jstockvolume.getDocument().addDocumentListener(dirty);
        m_jInCatalog.addActionListener(dirty);
        m_jRetired.addActionListener(dirty);
        m_jCatalogOrder.getDocument().addDocumentListener(dirty);
        txtProperties.getDocument().addDocumentListener(dirty);
        m_jKitchen.addActionListener(dirty);
        m_jService.addActionListener(dirty);
        m_jVprice.addActionListener(dirty);
        m_jVerpatrib.addActionListener(dirty);
        m_jTextTip.getDocument().addDocumentListener(dirty);
        m_jDisplay.getDocument().addDocumentListener(dirty);
        m_jStockUnits.getDocument().putProperty(dlSales, 24);

        m_jIsPack.addActionListener(dirty);
        m_jPackQuantity.getDocument().addDocumentListener(dirty);
        m_jPackProduct.addActionListener(dirty);
        m_jCheckWarrantyReceipt.addActionListener(dirty);
        m_jAlias.getDocument().addDocumentListener(dirty);
        m_jAlwaysAvailable.addActionListener(dirty);
        m_jDiscounted.addActionListener(dirty);
        m_jManageStock.addActionListener(dirty);
        jOtherBarcode.addActionListener(dirty);
        jOtherPackType.addActionListener(dirty);
        jOtherQuantity.addActionListener(dirty);

        m_jPriceBuy.getDocument().addDocumentListener(new PriceBuyManager());
        m_jPriceSell.getDocument().addDocumentListener(new PriceSellManager());
        m_jPriceSellTax.getDocument().addDocumentListener(new PriceTaxManager());
        m_jTax.addActionListener(new PriceTaxManager());
        m_jmargin.getDocument().addDocumentListener(new MarginManager());
        
        // Barcode scanners add CR to the end of the string - we need to ignore
        // that to prevent default button being activated
        jOtherBarcode.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Do nothing
            }});
        m_jCode.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Do nothing
            }});
            
        writeValueEOF();
    }

    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        originalDisplay = "";

        // Load the taxes logic
        taxeslogic = new TaxesLogic(taxsent.list());

        m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
        m_jCategory.setModel(m_CategoryModel);

        m_PromotionModel = new ComboBoxValModel(m_sentpromotion.list());
        jComboBoxPromotion.setModel(m_PromotionModel);

        m_BarcodesModel = new ListValModel();
        jListBarcodes.setModel(m_BarcodesModel);

        taxcatmodel = new ComboBoxValModel(taxcatsent.list());
        m_jTax.setModel(taxcatmodel);

        attmodel = new ComboBoxValModel(attsent.list());
        attmodel.add(0, null);
        m_jAtt.setModel(attmodel);

        packproductmodel = new ComboBoxValModel(packproductsent.list());
        m_jPackProduct.setModel(packproductmodel);

        AutoCompleteComboBox.enable(m_jPackProduct);
    }
    
    public void addBarcode( String barcode ) {
        if( barcode == null || barcode.isEmpty() ) {
            return;
        }
        jTabbedPane1.setSelectedIndex(1);      
        jOtherBarcode.setText(barcode);
        jOtherQuantity.requestFocus();
    }

// Set the product to be edited.  
    private void setProductInfo( ProductInfoExt info ) {
        
        if( info == null ) return;
        
        try {
            activate();
        
            m_jRef.setText( info.getReference());
            m_jCode.setText( info.getCode());
            m_jName.setText( info.getName() );
            m_jComment.setSelected( info.isCom() );
            m_jScale.setSelected( info.isScale() );
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(info.getPriceBuy()));
            m_CategoryModel.setSelectedKey(info.getCategoryID());
            jComboBoxPromotion.setEnabled(true);

            String promID =  info.getPromotionID();
            if ( promID != null && !promID.isEmpty() ) {
                jCheckBoxPromotion.setSelected(true);
            } else {
                jCheckBoxPromotion.setSelected(false);
            }
            m_PromotionModel.setSelectedKey( promID );

            if( info.getTaxCategoryID() == null ) {
                // Try to figure out tax category from the rate
                for( int n = 0; n < taxcatmodel.getSize(); ++n ) {
                    Double r = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getElementAt(n));
                    if( r.compareTo(info.getTaxRate()) == 0) {
                        taxcatmodel.setSelectedItem( taxcatmodel.getElementAt(n));
                    }
                }
            } else {
                taxcatmodel.setSelectedKey( info.getTaxCategoryID());
            }
            attmodel.setSelectedKey( info.getAttributeSetID());
            m_jImage.setImage( info.getImage());
            m_jstockcost.setText(Formats.CURRENCY.formatValue(info.getStockCost()));
            m_jstockvolume.setText(Formats.DOUBLE.formatValue(info.getStockVolume()));
            m_jInCatalog.setSelected( info.getInCatalog());
            m_jRetired.setSelected( info.getRetired());
            m_jCatalogOrder.setText(Formats.INT.formatValue(info.getCatOrder()));
            m_jKitchen.setSelected( info.isKitchen());
            m_jService.setSelected( info.isService());
            m_jDisplay.setText( info.getDisplay());
            setButtonHTML();
            m_jVprice.setSelected( info.isVprice());
            m_jVerpatrib.setSelected( info.isVerpatrib());
            m_jTextTip.setText( info.getTextTip());
            m_jCheckWarrantyReceipt.setSelected( info.getWarranty());
            m_jStockUnits.setText(Formats.DOUBLE.formatValue(info.getStockUnits()));
            m_jAlias.setText( info.getAlias());
            m_jAlwaysAvailable.setSelected( info.getAlwaysAvailable());
            m_jDiscounted.setSelected( info.getCanDiscount());
            m_jManageStock.setSelected( info.getManageStock() );
            m_jIsPack.setSelected( info.getIsPack());
            m_jPackQuantity.setText(Formats.DOUBLE.formatValue(info.getPackQuantity()));
            packproductmodel.setSelectedKey( info.getPromotionID());

            String displayname = "<html>" + m_jName.getText();
            originalDisplay = m_jDisplay.getText();
            displayEdited = displayname.compareToIgnoreCase(originalDisplay) != 0;

            txtProperties.setText( info.getPropertiesXml() );

            if( m_jName.getText().isEmpty() ) {
                m_jTitle.setText(AppLocal.getIntString("label.recordnew"));
            } else {
                m_jTitle.setText( m_jName.getText() );
            }    
            setPriceSell(info.getPriceSell());
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();

            updateBarcodeList();
                    
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Set the product to be edited.  
    private ProductInfoExt getProductInfo( ) {
        
        ProductInfoExt info = new ProductInfoExt();
        info.setReference( m_jRef.getText() );
        info.setCode(m_jCode.getText() );
        info.setName(m_jName.getText() );
        info.setCom(m_jComment.isSelected());
        info.setScale(m_jScale.isSelected());
        info.setPriceBuy( readCurrency(m_jPriceBuy.getText()));
        info.setPriceSell( readCurrency(m_jPriceSell.getText()));
        
        info.setCategoryID((String) m_CategoryModel.getSelectedKey());
        info.setPromotionID((String) m_PromotionModel.getSelectedKey());
        info.setTaxCategoryID((String) taxcatmodel.getSelectedKey());
        info.setAttributeSetID((String) attmodel.getSelectedKey());
        info.setTaxRate(taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem()));
        info.setImage( m_jImage.getImage());

        info.setStockCost( readCurrency( m_jstockcost.getText()));
        info.setStockVolume( readCurrency( m_jstockvolume.getText()));

        info.setInCatalog( m_jInCatalog.isSelected());
        info.setRetired( m_jRetired.isSelected());

        String val = m_jCatalogOrder.getText();
        if( !val.isEmpty() )
            info.setCatOrder( Double.parseDouble( val ) );

        info.setKitchen( m_jKitchen.isSelected());
        info.setService( m_jService.isSelected());
        
        info.setReference( m_jRef.getText() );

        info.setVprice( m_jVprice.isSelected());
        info.setVerpatrib( m_jVerpatrib.isSelected());
        
        info.setTextTip( m_jTextTip.getText() );
        info.setWarranty( m_jCheckWarrantyReceipt.isSelected());

        val = m_jStockUnits.getText();
        if( !val.isEmpty() )
            info.setStockUnits( Double.parseDouble( val ) );

        info.setAlias( m_jAlias.getText() );
        info.setAlwaysAvailable( m_jAlwaysAvailable.isSelected());
        info.setCanDiscount( m_jDiscounted.isSelected());
        info.setManageStock( m_jManageStock.isSelected());
        info.setIsPack( m_jIsPack.isSelected());
        
        val = m_jPackQuantity.getText();
        if( !val.isEmpty() )
            info.setPackQuantity( Double.parseDouble( val ) );
        
        info.setPackProduct((String) packproductmodel.getSelectedKey());

        info.setDisplay( m_jDisplay.getText() );

        Properties props = new Properties();
        try {                                                   
            String xml = txtProperties.getText();
            if( !xml.isEmpty() ) {
                props.loadFromXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            }
            info.setProperties( props );
        } catch (IOException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return info;
    }

    // Set the product to be edited.  
    public void setProduct( String productID, String barcode ) {
        try {
            writeValueInsert();

            if( productID != null ) {
                m_id = productID;
                setProductInfo( m_dlSales.getProductInfo( productID ) );
            } else {
                if( barcode != null ) {
                    m_jRef.setText( barcode );
                    m_jCode.setText(barcode);
                }
            }            
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
     
        jTabbedPane1.setSelectedIndex(0);
        m_jName.setRequestFocusEnabled(true);
    }
    
    public String getID() {
        return (String) m_id;
    }
    
    // Save the currently edited product.
    public void saveProduct( ) {
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {

        reportlock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordeof"));
        m_id = null;
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jName.setText(null);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_CategoryModel.setSelectedKey(null);
        m_PromotionModel.setSelectedKey(null);
        jCheckBoxPromotion.setSelected(false);
        taxcatmodel.setSelectedKey(null);
        attmodel.setSelectedKey(null);
        m_jPriceBuy.setText(null);
        setPriceSell(null);
        m_jImage.setImage(null);
        m_jstockcost.setText(null);
        m_jstockvolume.setText(null);
        m_jInCatalog.setSelected(false);
        m_jRetired.setSelected(false);
        m_jCatalogOrder.setText(null);
        txtProperties.setText(null);
        m_jKitchen.setSelected(false);
        m_jService.setSelected(false);
        m_jDisplay.setText(null);
        m_jVprice.setSelected(false);
        m_jVerpatrib.setSelected(false);
        m_jTextTip.setText(null);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jStockUnits.setVisible(false);
        m_jAlias.setText(null);
        m_jAlwaysAvailable.setSelected(false);
        m_jDiscounted.setSelected(false);
        m_jManageStock.setSelected( false );

        reportlock = false;

        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jName.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jCategory.setEnabled(false);
        jComboBoxPromotion.setEnabled(false);
        jCheckBoxPromotion.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jInCatalog.setEnabled(false);
        m_jRetired.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        txtProperties.setEnabled(false);
        jComboProperties.setEnabled(false);
        jPropertyValueText.setEnabled(false);
        jPropertyValueCombo.setEnabled(false);
        jPropertyAddButton.setEnabled(false);
        
        m_jKitchen.setEnabled(false);
        m_jService.setEnabled(false);
        m_jDisplay.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jTextTip.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jStockUnits.setVisible(false);
        m_jAlias.setEnabled(false);
        m_jAlwaysAvailable.setEnabled(false);
        m_jIsPack.setEnabled(false);
        m_jPackQuantity.setEnabled(false);
        packproductmodel.setSelectedKey(null);
        m_jPackProduct.setEnabled(false);
        jLabelPackQuantity.setEnabled(false);
        jLabelPackProduct.setEnabled(false);

        m_jDiscounted.setEnabled(false);
        m_jManageStock.setEnabled( false );

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        reportlock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordnew"));
        m_id = UUID.randomUUID().toString();
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jName.setText(null);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_CategoryModel.setSelectedKey(null);
        m_PromotionModel.setSelectedKey(null);
        jCheckBoxPromotion.setSelected(false);
        taxcatmodel.setSelectedKey(null);
        attmodel.setSelectedKey(null);
        m_jPriceBuy.setText(null);
        setPriceSell(null);
        m_jImage.setImage(null);
        m_jstockcost.setText("0.00");
        m_jstockvolume.setText("0.00");
        m_jInCatalog.setSelected(true);
        m_jRetired.setSelected(false);
        m_jCatalogOrder.setText(null);
        txtProperties.setText(null);
        m_jKitchen.setSelected(false);
        m_jService.setSelected(false);
        m_jDisplay.setText(null);
        m_jVprice.setSelected(false);
        m_jVerpatrib.setSelected(false);
        m_jTextTip.setText(null);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jStockUnits.setVisible(false);
        m_jAlias.setText(null);
        m_jAlwaysAvailable.setSelected(false);
        m_jDiscounted.setSelected(true);
        m_jManageStock.setSelected( true );

        reportlock = false;

        // Los habilitados
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jName.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jCategory.setEnabled(true);
        jCheckBoxPromotion.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jImage.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jInCatalog.setEnabled(true);
        m_jRetired.setEnabled(true);
        m_jCatalogOrder.setEnabled(false);
        txtProperties.setEnabled(true);
        jComboProperties.setEnabled(true);
        jPropertyValueText.setEnabled(true);
        jPropertyValueCombo.setEnabled(true);
        jPropertyAddButton.setEnabled(true);        
        m_jKitchen.setEnabled(true);
        m_jService.setEnabled(true);
        m_jDisplay.setEnabled(true);
        m_jVprice.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jTextTip.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jStockUnits.setVisible(false);
        m_jAlias.setEnabled(true);
        m_jAlwaysAvailable.setEnabled(true);
        m_jIsPack.setEnabled(true);
        m_jPackQuantity.setEnabled(false);
        m_jPackProduct.setEnabled(false);
        jLabelPackQuantity.setEnabled(false);
        jLabelPackProduct.setEnabled(false);

        m_jDiscounted.setEnabled(true);
        m_jManageStock.setEnabled( true );

        m_jIsPack.setSelected(false);
        m_jPackQuantity.setText(null);
        packproductmodel.setSelectedKey(null);

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();

    }

    private void extractValues(Object[] myprod) {
        m_jTitle.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_NAME]));
        m_id = myprod[DataLogicSales.INDEX_ID];
        m_jRef.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_REFERENCE]));
        m_jCode.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_CODE]));
        m_jName.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_NAME]));
        m_jComment.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISCOM]));
        m_jScale.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISSCALE]));
        m_jPriceBuy.setText(Formats.CURRENCY.formatValue(myprod[DataLogicSales.INDEX_PRICEBUY]));
        setPriceSell(myprod[DataLogicSales.INDEX_PRICESELL]);
        m_CategoryModel.setSelectedKey(myprod[DataLogicSales.INDEX_CATEGORY]);

        Object prom = myprod[DataLogicSales.INDEX_PROMOTIONID];
        if (prom == null) {
            jComboBoxPromotion.setEnabled(false);
            jCheckBoxPromotion.setSelected(false);
        } else {
            jComboBoxPromotion.setEnabled(true);
            jCheckBoxPromotion.setSelected(true);
        }
        m_PromotionModel.setSelectedKey(prom);

        taxcatmodel.setSelectedKey(myprod[DataLogicSales.INDEX_TAXCAT]);
        attmodel.setSelectedKey(myprod[DataLogicSales.INDEX_ATTRIBUTESET_ID]);
        m_jImage.setImage((BufferedImage) myprod[DataLogicSales.INDEX_IMAGE]);
        m_jstockcost.setText(Formats.CURRENCY.formatValue(myprod[DataLogicSales.INDEX_STOCKCOST]));
        m_jstockvolume.setText(Formats.DOUBLE.formatValue(myprod[DataLogicSales.INDEX_STOCKVOLUME]));
        m_jInCatalog.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISCATALOG]));
        m_jRetired.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISRETIRED]));
        m_jCatalogOrder.setText(Formats.INT.formatValue(myprod[DataLogicSales.INDEX_CATORDER]));
        txtProperties.setText(Formats.BYTEA.formatValue(myprod[DataLogicSales.INDEX_ATTRIBUTES]));
        m_jKitchen.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISKITCHEN]));
        m_jService.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISSERVICE]));
        m_jDisplay.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_DISPLAY]));
        m_jVprice.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISVPRICE]));
        m_jVerpatrib.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISVERPATRIB]));
        m_jTextTip.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_TEXTTIP]));
        m_jCheckWarrantyReceipt.setSelected(((Boolean) myprod[DataLogicSales.INDEX_WARRANTY]));
        m_jStockUnits.setText(Formats.DOUBLE.formatValue(myprod[DataLogicSales.INDEX_STOCKUNITS]));
        m_jAlias.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_ALIAS]));
        m_jAlwaysAvailable.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ALWAYSAVAILABLE]));
        m_jDiscounted.setSelected(((Boolean) myprod[DataLogicSales.INDEX_CANDISCOUNT]));
        m_jIsPack.setSelected(((Boolean) myprod[DataLogicSales.INDEX_ISPACK]));
        m_jPackQuantity.setText(Formats.DOUBLE.formatValue(myprod[DataLogicSales.INDEX_PACKQUANTITY]));
        packproductmodel.setSelectedKey(myprod[DataLogicSales.INDEX_PACKPRODUCT]);
        m_jManageStock.setSelected( ((Boolean) myprod[DataLogicSales.INDEX_MANAGESTOCK]) );

        String displayname = "<html>" + m_jName.getText();
        originalDisplay = m_jDisplay.getText();
        displayEdited = displayname.compareToIgnoreCase(originalDisplay) != 0;

    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        reportlock = true;
        Object[] myprod = (Object[]) value;
        extractValues(myprod);
        m_jTitle.setText(Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_REFERENCE]) + " - "
                + Formats.STRING.formatValue(myprod[DataLogicSales.INDEX_NAME]) + " "
                + AppLocal.getIntString("label.recorddeleted"));
        txtProperties.setCaretPosition(0);

        reportlock = false;

        // Los habilitados
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jName.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jCategory.setEnabled(false);
        jComboBoxPromotion.setEnabled(false);
        jCheckBoxPromotion.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jInCatalog.setEnabled(false);
        m_jRetired.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        txtProperties.setEnabled(false);
        jComboProperties.setEnabled(false);
        jPropertyValueText.setEnabled(false);
        jPropertyValueCombo.setEnabled(false);
        jPropertyAddButton.setEnabled(false);
        m_jKitchen.setEnabled(false);
        m_jService.setEnabled(true);
        m_jDisplay.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jTextTip.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jStockUnits.setVisible(false);
        m_jAlias.setEnabled(false);
        m_jAlwaysAvailable.setEnabled(false);
        m_jDiscounted.setEnabled(false);
        m_jIsPack.setEnabled(true);
        m_jManageStock.setEnabled( false );
        
        m_jPackQuantity.setEnabled(m_jIsPack.isSelected());
        m_jPackProduct.setEnabled(m_jIsPack.isSelected());
        jLabelPackQuantity.setEnabled(m_jIsPack.isSelected());
        jLabelPackProduct.setEnabled(m_jIsPack.isSelected());

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        reportlock = true;
        Object[] myprod = (Object[]) value;
        extractValues(myprod);

        txtProperties.setCaretPosition(0);
        reportlock = false;

        // Los habilitados
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jName.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jCategory.setEnabled(true);
        jCheckBoxPromotion.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jImage.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jInCatalog.setEnabled(true);
        m_jRetired.setEnabled(true);
        m_jCatalogOrder.setEnabled(m_jInCatalog.isSelected());
        txtProperties.setEnabled(true);
        jComboProperties.setEnabled(true);
        jPropertyValueText.setEnabled(true);
        jPropertyValueCombo.setEnabled(true);
        jPropertyAddButton.setEnabled(true);
        m_jKitchen.setEnabled(true);
        m_jService.setEnabled(true);
        m_jDisplay.setEnabled(true);
        setButtonHTML();
        m_jVprice.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jTextTip.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jStockUnits.setVisible(false);
        m_jAlias.setEnabled(true);
        m_jAlwaysAvailable.setEnabled(true);

        m_jDiscounted.setEnabled(true);
        m_jManageStock.setEnabled( true );

        m_jPackQuantity.setEnabled(m_jIsPack.isSelected());
        m_jPackProduct.setEnabled(m_jIsPack.isSelected());
        jLabelPackQuantity.setEnabled(m_jIsPack.isSelected());
        jLabelPackProduct.setEnabled(m_jIsPack.isSelected());

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();
    }

    /**
     *
     * @return myprod
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] myprod = new Object[DataLogicSales.FIELD_COUNT];
        myprod[DataLogicSales.INDEX_ID] = m_id;
        myprod[DataLogicSales.INDEX_REFERENCE] = m_jRef.getText();

        String code = m_jCode.getText();

        myprod[DataLogicSales.INDEX_CODE] = code;
        myprod[DataLogicSales.INDEX_CODETYPE] = BarcodeValidator.BarcodeValidate(code);

        myprod[DataLogicSales.INDEX_NAME] = m_jName.getText();
        myprod[DataLogicSales.INDEX_ISCOM] = m_jComment.isSelected();
        myprod[DataLogicSales.INDEX_ISSCALE] = m_jScale.isSelected();
        myprod[DataLogicSales.INDEX_PRICEBUY] = Formats.CURRENCY.parseValue(m_jPriceBuy.getText());
        myprod[DataLogicSales.INDEX_PRICESELL] = pricesell;
        myprod[DataLogicSales.INDEX_CATEGORY] = m_CategoryModel.getSelectedKey();
        myprod[DataLogicSales.INDEX_PROMOTIONID] = m_PromotionModel.getSelectedKey();
        myprod[DataLogicSales.INDEX_TAXCAT] = taxcatmodel.getSelectedKey();
        myprod[DataLogicSales.INDEX_ATTRIBUTESET_ID] = attmodel.getSelectedKey();
        myprod[DataLogicSales.INDEX_IMAGE] = m_jImage.getImage();
        myprod[DataLogicSales.INDEX_STOCKCOST] = Formats.CURRENCY.parseValue(m_jstockcost.getText());
        myprod[DataLogicSales.INDEX_STOCKVOLUME] = Formats.DOUBLE.parseValue(m_jstockvolume.getText());
        myprod[DataLogicSales.INDEX_ISCATALOG] = m_jInCatalog.isSelected();
        myprod[DataLogicSales.INDEX_ISRETIRED] = m_jRetired.isSelected();
        myprod[DataLogicSales.INDEX_CATORDER] = Formats.INT.parseValue(m_jCatalogOrder.getText());
        myprod[DataLogicSales.INDEX_ATTRIBUTES] = Formats.BYTEA.parseValue(txtProperties.getText());
        myprod[DataLogicSales.INDEX_ISKITCHEN] = m_jKitchen.isSelected();
        myprod[DataLogicSales.INDEX_ISSERVICE] = m_jService.isSelected();
        myprod[DataLogicSales.INDEX_DISPLAY] = m_jDisplay.getText();
        myprod[DataLogicSales.INDEX_ISVPRICE] = m_jVprice.isSelected();
        myprod[DataLogicSales.INDEX_ISVERPATRIB] = m_jVerpatrib.isSelected();
        myprod[DataLogicSales.INDEX_TEXTTIP] = m_jTextTip.getText();
        myprod[DataLogicSales.INDEX_WARRANTY] = m_jCheckWarrantyReceipt.isSelected();
        myprod[DataLogicSales.INDEX_STOCKUNITS] = Formats.DOUBLE.parseValue(m_jStockUnits.getText());
        myprod[DataLogicSales.INDEX_ALIAS] = m_jAlias.getText();
        myprod[DataLogicSales.INDEX_ALWAYSAVAILABLE] = m_jAlwaysAvailable.isSelected();
        myprod[DataLogicSales.INDEX_DISCOUNTED] = "no";
        myprod[DataLogicSales.INDEX_CANDISCOUNT] = m_jDiscounted.isSelected();
        myprod[DataLogicSales.INDEX_ISPACK] = m_jIsPack.isSelected();
        myprod[DataLogicSales.INDEX_PACKQUANTITY] = Formats.DOUBLE.parseValue(m_jPackQuantity.getText());
        myprod[DataLogicSales.INDEX_PACKPRODUCT] = packproductmodel.getSelectedKey();
        myprod[DataLogicSales.INDEX_MANAGESTOCK] = m_jManageStock.isSelected();

        return myprod;

    }

    /**
     *
     * @return this
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * Aug 2014 - temporary only! ADD Product now requires a CurrentStock entry
     * record This is experimental whilst developing connex to external hosted
     * DB as need to get online product from its DB. So for now just consume a
     * new DB session. Expensive... (I know!)
     */
    private void setCurrentStock() {

        // connect to the database
        String url = AppLocal.getIntString("db.URL");
        String user = AppLocal.getIntString("db.user");
        String password = AppLocal.getIntString("db.password");

        {
            try {
                // create our java jdbc statement
                try (Connection conn = DriverManager.getConnection(url, "user", "password")) {
                    // create our java jdbc statement
                    Statement statement = conn.createStatement();
                    statement.executeUpdate("INSERT INTO STOCKCURRENT " + "VALUES (1001, 'Simpson', 'Mr.', 'Springfield', 2001)");
                }
            } catch (SQLException e) {
                System.err.println("Got an exception! ");
                System.err.println(e.getMessage());
            }
        }
    }

    private void setCode() {

        Long lDateTime = new Date().getTime(); // USED FOR RANDOM CODE DETAILS

        if (!reportlock) {
            reportlock = true;

            if (m_jRef == null) {
                //m_jCode.setText("0123456789012");
                m_jCode.setText(Long.toString(lDateTime));
            } else if (m_jCode.getText() == null || "".equals(m_jCode.getText())) {
                m_jCode.setText(m_jRef.getText());
            }
            reportlock = false;
        }
    }

    private void setDisplay() {

        if( !displayEdited ) {
            String str = (m_jName.getText());
            int length = str.length();

            if (!reportlock) {
                reportlock = true;

                if (length == 0) {
                    m_jDisplay.setText(null);
                } else if( originalDisplay.contentEquals( m_jDisplay.getText() ) ) {
                    m_jDisplay.setText("<html>" + m_jName.getText());
                    originalDisplay = m_jDisplay.getText();
                }
                reportlock = false;
            }
        }
    }

    private void setButtonHTML() {

        String str = (m_jDisplay.getText());
        int length = str.length();

        if (!reportlock) {
            reportlock = true;

            if (length == 0) {
                jButtonHTML.setText("Click Me");
            } else {
                jButtonHTML.setText(m_jDisplay.getText());
            }
            reportlock = false;
        }
    }

    private void setTextHTML() {
// TODO - expand m_jDisplay HTML functionality        
    }

    private void calculateMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = (Double) pricesell;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jmargin.setText(null);
            } else {
                m_jmargin.setText(Formats.PERCENT.formatValue(dPriceSell / dPriceBuy - 1.0));
            }
            m_jmargin.setForeground(Color.red);            
            
            reportlock = false;
        }
    }

    private void calculatePriceSellTax() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSell = (Double) pricesell;

            if (dPriceSell == null) {
                m_jPriceSellTax.setText(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                m_jPriceSellTax.setText(Formats.CURRENCY.formatValue(dPriceSell * (1.0 + dTaxRate)));
            }
            m_jPriceSellTax.setForeground(Color.red);            
            reportlock = false;
        }
    }

    private void calculateGP() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = (Double) pricesell;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jGrossProfit.setText(null);
            } else {
                m_jGrossProfit.setText(Formats.PERCENT.formatValue((dPriceSell - dPriceBuy) / dPriceSell));
            }
            m_jGrossProfit.setForeground(Color.red);

            reportlock = false;
        }
    }

    private void calculatePriceSellfromMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dMargin = readPercent(m_jmargin.getText());

            if (dMargin == null || dPriceBuy == null) {
                setPriceSell(null);
            } else {
                setPriceSell(dPriceBuy * (1.0 + dMargin));
            }
            m_jPriceSell.setForeground(Color.red);

            reportlock = false;
        }

    }

    private void calculatePriceSellfromPST() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSellTax = readCurrency(m_jPriceSellTax.getText());

            if (dPriceSellTax == null) {
                setPriceSell(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                setPriceSell(dPriceSellTax / (1.0 + dTaxRate));
            }
            m_jPriceSell.setForeground(Color.red);

            reportlock = false;
        }
    }

    private void setPriceSell(Object value) {

        if (!priceselllock) {
            priceselllock = true;
            pricesell = value;
            m_jPriceSell.setText(Formats.CURRENCY.formatValue(pricesell));
            calculatePriceSellTax();
            priceselllock = false;
            m_jPriceSell.setForeground(Color.black);
        }
    }

    private class PropertyActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String sel = (String) jComboProperties.getSelectedItem();
            String val = m_PropertyOptions.getProperty( sel, "" );
            
            String type = val;
            int nComma = val.indexOf( ',' );
            if( nComma >0 ) {
                type = val.substring( 0, nComma ).trim();
                val = val.substring( nComma + 1 ).trim();
            }
            
            switch( type ) {
                case "boolean" :
                    jPropertyValueText.setVisible( false );
                    jPropertyValueCombo.removeAllItems();
                    jPropertyValueCombo.addItem(AppLocal.getIntString("Button.No"));
                    jPropertyValueCombo.addItem(AppLocal.getIntString("Button.Yes"));
                    jPropertyValueCombo.setVisible( true );
                     break;
                case "number" :
                    jPropertyValueCombo.setVisible( false );
                    jPropertyValueText.setVisible( true );
                    jPropertyValueText.setText( "" );
                    break;
                case "option" :
                    jPropertyValueCombo.removeAllItems();
                    nComma = val.indexOf( ',' );
                    while( nComma > 0 ) {
                        jPropertyValueCombo.addItem( val.substring( 0, nComma ) );
                        val = val.substring( nComma + 1 ).trim();
                        nComma = val.indexOf( ',' );
                    }
                    jPropertyValueCombo.addItem(val);
                    jPropertyValueCombo.setVisible( true );
                    jPropertyValueText.setVisible( false );
                    break;
                case "text" :
                    jPropertyValueCombo.setVisible( false );
                    jPropertyValueText.setVisible( true );
                    jPropertyValueText.setText( "" );
                    break;
                default:
                    Logger.getLogger(ProductsEditor.class.getName()).log(Level.WARNING, "Unknown property type (" + type + ") in Product.Properties" );
                    break;
            }           
        }

    }
    
    private class PriceManager implements DocumentListener, ActionListener {

        public void reCalculate() {};
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            reCalculate();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            reCalculate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            reCalculate();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reCalculate();
        }
    }
    
    private class PriceSellManager extends PriceManager {

        @Override
        public void reCalculate() {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            m_jPriceSell.setForeground(Color.black);
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }
    }

    private class PriceBuyManager extends PriceManager {
        
        @Override
        public void reCalculate() {
            calculateMargin();
            calculateGP();
        }
    }

    private class PriceTaxManager extends PriceManager {

        @Override
        public void reCalculate() {
            m_jPriceSellTax.setForeground(Color.black);
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGP();
        }
    }

    private class MarginManager extends PriceManager  {

        @Override
        public void reCalculate() {
            m_jmargin.setForeground(Color.black);
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGP();
        }
    }

    private static Double readCurrency(String sValue) {
        try {
            return (Double) Formats.CURRENCY.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    private static Double readPercent(String sValue) {
        try {
            return (Double) Formats.PERCENT.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel24 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        m_jTitle = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jRef = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jCategory = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        m_jAtt = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        m_jTax = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        m_jPriceSellTax = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jPriceSell = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        m_jmargin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jPriceBuy = new javax.swing.JTextField();
        m_jTextTip = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        m_jGrossProfit = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        m_jAlias = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        m_jVerpatrib = new eu.hansolo.custom.SteelCheckBox();
        m_jCheckWarrantyReceipt = new eu.hansolo.custom.SteelCheckBox();
        jLabel36 = new javax.swing.JLabel();
        jComboBoxPromotion = new javax.swing.JComboBox();
        jCheckBoxPromotion = new eu.hansolo.custom.SteelCheckBox();
        jPanelCodes = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        m_jCode = new javax.swing.JTextField();
        m_jCodetype = new javax.swing.JComboBox();
        jButton2 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListBarcodes = new javax.swing.JList<>();
        jPanel9 = new javax.swing.JPanel();
        jButtonAddCode = new javax.swing.JButton();
        jButtonDeleteCode = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jOtherBarcode = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jOtherQuantity = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jOtherPackType = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        m_jstockcost = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        m_jstockvolume = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        m_jCatalogOrder = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        m_jStockUnits = new javax.swing.JTextField();
        m_jPackQuantity = new javax.swing.JTextField();
        m_jPackProduct = new javax.swing.JComboBox();
        jLabelPackQuantity = new javax.swing.JLabel();
        jLabelPackProduct = new javax.swing.JLabel();
        m_jInCatalog = new eu.hansolo.custom.SteelCheckBox();
        m_jKitchen = new eu.hansolo.custom.SteelCheckBox();
        m_jIsPack = new eu.hansolo.custom.SteelCheckBox();
        m_jAlwaysAvailable = new eu.hansolo.custom.SteelCheckBox();
        m_jScale = new eu.hansolo.custom.SteelCheckBox();
        m_jDiscounted = new eu.hansolo.custom.SteelCheckBox();
        m_jVprice = new eu.hansolo.custom.SteelCheckBox();
        m_jService = new eu.hansolo.custom.SteelCheckBox();
        m_jComment = new eu.hansolo.custom.SteelCheckBox();
        m_jManageStock = new eu.hansolo.custom.SteelCheckBox();
        m_jRetired = new eu.hansolo.custom.SteelCheckBox();
        m_jImage = new uk.chromis.data.gui.JImageEditor();
        jPanel4 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        m_jDisplay = new javax.swing.JTextPane();
        jButtonHTML = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtProperties = new javax.swing.JTextArea();
        jPanelProperties = new javax.swing.JPanel();
        jComboProperties = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jPropertyValueCombo = new javax.swing.JComboBox<>();
        jPropertyValueText = new javax.swing.JTextField();
        jPropertyAddButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jBtnSupplierWeb = new javax.swing.JButton();

        jLabel24.setText("jLabel24");

        jLabel27.setText("jLabel27");

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        m_jTitle.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_jTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        add(m_jTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 30));

        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jPanel1.setLayout(null);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.prodref")); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(10, 10, 65, 25);

        m_jRef.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jRef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jRefFocusLost(evt);
            }
        });
        jPanel1.add(m_jRef);
        m_jRef.setBounds(130, 10, 80, 25);

        jLabel34.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel34.setText(AppLocal.getIntString("Label.Alias")); // NOI18N
        jPanel1.add(jLabel34);
        jLabel34.setBounds(10, 70, 100, 25);

        m_jName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jNameFocusLost(evt);
            }
        });
        jPanel1.add(m_jName);
        m_jName.setBounds(130, 40, 270, 25);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.prodcategory")); // NOI18N
        jPanel1.add(jLabel5);
        jLabel5.setBounds(10, 100, 110, 25);

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(m_jCategory);
        m_jCategory.setBounds(130, 100, 270, 25);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel13.setText(AppLocal.getIntString("label.attributes")); // NOI18N
        jPanel1.add(jLabel13);
        jLabel13.setBounds(10, 130, 110, 25);

        m_jAtt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(m_jAtt);
        m_jAtt.setBounds(130, 130, 170, 25);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxcategory")); // NOI18N
        jPanel1.add(jLabel7);
        jLabel7.setBounds(10, 160, 110, 25);

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(m_jTax);
        m_jTax.setBounds(130, 160, 170, 25);

        jLabel16.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel16.setText(AppLocal.getIntString("label.prodpriceselltax")); // NOI18N
        jPanel1.add(jLabel16);
        jLabel16.setBounds(10, 190, 90, 25);

        m_jPriceSellTax.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPriceSellTax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel1.add(m_jPriceSellTax);
        m_jPriceSellTax.setBounds(130, 190, 80, 25);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(AppLocal.getIntString("label.prodpricesell")); // NOI18N
        jPanel1.add(jLabel4);
        jLabel4.setBounds(210, 190, 100, 25);

        m_jPriceSell.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPriceSell.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel1.add(m_jPriceSell);
        m_jPriceSell.setBounds(310, 190, 70, 25);

        jLabel19.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel19.setText(bundle.getString("label.margin")); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(48, 15));
        jPanel1.add(jLabel19);
        jLabel19.setBounds(390, 190, 70, 25);

        m_jmargin.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jmargin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jmargin.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        m_jmargin.setEnabled(false);
        jPanel1.add(m_jmargin);
        m_jmargin.setBounds(470, 190, 70, 25);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.prodpricebuy")); // NOI18N
        jPanel1.add(jLabel3);
        jLabel3.setBounds(10, 220, 80, 25);

        m_jPriceBuy.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPriceBuy.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel1.add(m_jPriceBuy);
        m_jPriceBuy.setBounds(130, 220, 80, 25);

        m_jTextTip.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel1.add(m_jTextTip);
        m_jTextTip.setBounds(130, 290, 220, 25);

        jLabel21.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel21.setText(bundle.getString("label.texttip")); // NOI18N
        jPanel1.add(jLabel21);
        jLabel21.setBounds(10, 290, 100, 25);

        m_jGrossProfit.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jGrossProfit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jGrossProfit.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        m_jGrossProfit.setEnabled(false);
        jPanel1.add(m_jGrossProfit);
        m_jGrossProfit.setBounds(470, 220, 70, 25);

        jLabel22.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText(bundle.getString("label.grossprofit")); // NOI18N
        jPanel1.add(jLabel22);
        jLabel22.setBounds(370, 220, 90, 20);

        m_jAlias.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel1.add(m_jAlias);
        m_jAlias.setBounds(130, 70, 170, 25);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.prodname")); // NOI18N
        jPanel1.add(jLabel2);
        jLabel2.setBounds(10, 40, 100, 25);

        m_jVerpatrib.setText(bundle.getString("label.mandatory")); // NOI18N
        jPanel1.add(m_jVerpatrib);
        m_jVerpatrib.setBounds(320, 130, 180, 30);

        m_jCheckWarrantyReceipt.setText(bundle.getString("label.productreceipt")); // NOI18N
        jPanel1.add(m_jCheckWarrantyReceipt);
        m_jCheckWarrantyReceipt.setBounds(130, 320, 260, 30);

        jLabel36.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel36.setText("Promotion");
        jPanel1.add(jLabel36);
        jLabel36.setBounds(10, 250, 90, 30);

        jComboBoxPromotion.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(jComboBoxPromotion);
        jComboBoxPromotion.setBounds(160, 250, 380, 30);

        jCheckBoxPromotion.setText(" ");
        jCheckBoxPromotion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPromotionActionPerformed(evt);
            }
        });
        jPanel1.add(jCheckBoxPromotion);
        jCheckBoxPromotion.setBounds(130, 250, 30, 30);

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodgeneral"), jPanel1); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.prodbarcode")); // NOI18N

        m_jCode.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_jCodetype.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/encrypted.png"))); // NOI18N
        jButton2.setToolTipText(bundle.getString("tiptext.createkey")); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(64, 32));
        jButton2.setMinimumSize(new java.awt.Dimension(64, 32));
        jButton2.setPreferredSize(new java.awt.Dimension(64, 32));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel8.setName(bundle.getString("label.prodotherbarcode")); // NOI18N

        jLabel8.setText(bundle.getString("label.prodotherbarcode")); // NOI18N

        jScrollPane3.setViewportView(jListBarcodes);

        jButtonAddCode.setText(bundle.getString("Button.Add")); // NOI18N
        jButtonAddCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddCodeActionPerformed(evt);
            }
        });

        jButtonDeleteCode.setText(bundle.getString("Button.Delete")); // NOI18N
        jButtonDeleteCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteCodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jButtonDeleteCode)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jButtonAddCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jButtonAddCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteCode)
                .addGap(0, 48, Short.MAX_VALUE))
        );

        jLabel11.setText(bundle.getString("label.barcode")); // NOI18N

        jLabel12.setText(bundle.getString("label.quantity")); // NOI18N

        jOtherQuantity.setToolTipText("");

        jLabel14.setText(bundle.getString("label.packtype")); // NOI18N

        jOtherPackType.setText("Case");
        jOtherPackType.setToolTipText("");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel14))
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jOtherQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jOtherPackType, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(24, Short.MAX_VALUE))
                    .addComponent(jOtherBarcode)))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jOtherPackType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jOtherQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOtherBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelCodesLayout = new javax.swing.GroupLayout(jPanelCodes);
        jPanelCodes.setLayout(jPanelCodesLayout);
        jPanelCodesLayout.setHorizontalGroup(
            jPanelCodesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCodesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCodesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelCodesLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelCodesLayout.setVerticalGroup(
            jPanelCodesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCodesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCodesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("label.barcodes"), jPanelCodes); // NOI18N

        jPanel2.setLayout(null);

        jLabel9.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.prodstockcost")); // NOI18N
        jPanel2.add(jLabel9);
        jLabel9.setBounds(250, 30, 120, 25);

        m_jstockcost.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jstockcost.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockcost.setText("0.00");
        jPanel2.add(m_jstockcost);
        m_jstockcost.setBounds(370, 30, 80, 25);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel10.setText(AppLocal.getIntString("label.prodstockvol")); // NOI18N
        jPanel2.add(jLabel10);
        jLabel10.setBounds(250, 70, 120, 25);

        m_jstockvolume.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jstockvolume.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockvolume.setText("0.00");
        jPanel2.add(m_jstockvolume);
        m_jstockvolume.setBounds(370, 70, 80, 25);

        jLabel18.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel18.setText(AppLocal.getIntString("label.prodorder")); // NOI18N
        jLabel18.setToolTipText("");
        jPanel2.add(jLabel18);
        jLabel18.setBounds(250, 110, 120, 25);

        m_jCatalogOrder.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jCatalogOrder.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel2.add(m_jCatalogOrder);
        m_jCatalogOrder.setBounds(370, 110, 80, 25);

        jLabel23.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText(bundle.getString("label.prodminmax")); // NOI18N
        jLabel23.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel2.add(jLabel23);
        jLabel23.setBounds(250, 150, 270, 60);

        m_jStockUnits.setEditable(false);
        m_jStockUnits.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jStockUnits.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockUnits.setText("0");
        m_jStockUnits.setBorder(null);
        jPanel2.add(m_jStockUnits);
        m_jStockUnits.setBounds(370, 210, 80, 25);

        m_jPackQuantity.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jPackQuantity.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPackQuantity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jPackQuantityFocusLost(evt);
            }
        });
        jPanel2.add(m_jPackQuantity);
        m_jPackQuantity.setBounds(350, 240, 80, 25);

        m_jPackProduct.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel2.add(m_jPackProduct);
        m_jPackProduct.setBounds(350, 270, 220, 25);

        jLabelPackQuantity.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelPackQuantity.setText("Pack Quantity");
        jPanel2.add(jLabelPackQuantity);
        jLabelPackQuantity.setBounds(260, 240, 90, 20);

        jLabelPackProduct.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelPackProduct.setText("of Product");
        jPanel2.add(jLabelPackProduct);
        jLabelPackProduct.setBounds(260, 260, 80, 30);

        m_jInCatalog.setSelected(true);
        m_jInCatalog.setText(bundle.getString("label.prodincatalog")); // NOI18N
        m_jInCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jInCatalogActionPerformed(evt);
            }
        });
        jPanel2.add(m_jInCatalog);
        m_jInCatalog.setBounds(20, 50, 200, 30);

        m_jKitchen.setText("Print to Remote Printer");
        jPanel2.add(m_jKitchen);
        m_jKitchen.setBounds(20, 170, 210, 30);

        m_jIsPack.setText("Multi Pack");
        m_jIsPack.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jIsPack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jIsPackActionPerformed(evt);
            }
        });
        jPanel2.add(m_jIsPack);
        m_jIsPack.setBounds(250, 210, 110, 30);

        m_jAlwaysAvailable.setText(bundle.getString("Label.AlwaysAvailable")); // NOI18N
        m_jAlwaysAvailable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jAlwaysAvailableActionPerformed(evt);
            }
        });
        jPanel2.add(m_jAlwaysAvailable);
        m_jAlwaysAvailable.setBounds(20, 230, 210, 30);

        m_jScale.setText(bundle.getString("label.prodscale")); // NOI18N
        jPanel2.add(m_jScale);
        m_jScale.setBounds(20, 140, 200, 30);

        m_jDiscounted.setText(bundle.getString("label.discounted")); // NOI18N
        jPanel2.add(m_jDiscounted);
        m_jDiscounted.setBounds(20, 260, 200, 30);

        m_jVprice.setText(bundle.getString("label.variableprice")); // NOI18N
        jPanel2.add(m_jVprice);
        m_jVprice.setBounds(20, 200, 200, 30);

        m_jService.setText("Service Item");
        jPanel2.add(m_jService);
        m_jService.setBounds(20, 80, 210, 30);

        m_jComment.setText(bundle.getString("label.prodaux")); // NOI18N
        jPanel2.add(m_jComment);
        m_jComment.setBounds(20, 110, 200, 30);

        m_jManageStock.setText(bundle.getString("label.managestock")); // NOI18N
        jPanel2.add(m_jManageStock);
        m_jManageStock.setBounds(20, 290, 200, 30);

        m_jRetired.setText(bundle.getString("label.retired")); // NOI18N
        m_jRetired.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jRetiredActionPerformed(evt);
            }
        });
        jPanel2.add(m_jRetired);
        m_jRetired.setBounds(20, 20, 200, 30);

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodstock"), jPanel2); // NOI18N
        jTabbedPane1.addTab("Image", m_jImage);

        jPanel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel4.setLayout(null);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel28.setText(bundle.getString("label.prodbuttonhtml")); // NOI18N
        jPanel4.add(jLabel28);
        jLabel28.setBounds(10, 10, 270, 20);

        m_jDisplay.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jScrollPane2.setViewportView(m_jDisplay);

        jPanel4.add(jScrollPane2);
        jScrollPane2.setBounds(10, 40, 480, 40);

        jButtonHTML.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jButtonHTML.setText(bundle.getString("button.htmltest")); // NOI18N
        jButtonHTML.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonHTML.setMaximumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setMinimumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setPreferredSize(new java.awt.Dimension(96, 72));
        jButtonHTML.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonHTMLMouseClicked(evt);
            }
        });
        jPanel4.add(jButtonHTML);
        jButtonHTML.setBounds(205, 90, 110, 70);

        jLabel17.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText(bundle.getString("label.producthtmlguide")); // NOI18N
        jLabel17.setToolTipText("");
        jLabel17.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel4.add(jLabel17);
        jLabel17.setBounds(10, 200, 330, 100);
        jPanel4.add(jSeparator1);
        jSeparator1.setBounds(150, 300, 0, 2);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel32.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText(bundle.getString("label.fontexample")); // NOI18N
        jLabel32.setToolTipText(bundle.getString("tooltip.fontexample")); // NOI18N

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText(bundle.getString("label.fontcolour")); // NOI18N
        jLabel25.setToolTipText(bundle.getString("tooltip.fontcolour")); // NOI18N
        jLabel25.setPreferredSize(new java.awt.Dimension(160, 30));

        jLabel29.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel29.setText(bundle.getString("label.fontsizelarge")); // NOI18N
        jLabel29.setToolTipText(bundle.getString("tooltip.fontsizelarge")); // NOI18N
        jLabel29.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel29.setPreferredSize(new java.awt.Dimension(160, 30));

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText(bundle.getString("label.fontsize")); // NOI18N
        jLabel26.setToolTipText(bundle.getString("tooltip.fontsizesmall")); // NOI18N
        jLabel26.setPreferredSize(new java.awt.Dimension(160, 30));

        jLabel31.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel31.setText(bundle.getString("label.fontitalic")); // NOI18N
        jLabel31.setToolTipText(bundle.getString("tooltip.fontitalic")); // NOI18N
        jLabel31.setPreferredSize(new java.awt.Dimension(160, 30));

        jLabel30.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel30.setText(bundle.getString("label.fontweight")); // NOI18N
        jLabel30.setToolTipText(bundle.getString("tooltip.fontbold")); // NOI18N
        jLabel30.setPreferredSize(new java.awt.Dimension(160, 30));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.add(jPanel5);
        jPanel5.setBounds(360, 110, 180, 220);

        jTabbedPane1.addTab("Button", jPanel4);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        txtProperties.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(txtProperties);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(null);

        jPanel7.add(jPropertyValueCombo);
        jPropertyValueCombo.setBounds(0, 0, 250, 24);

        jPropertyValueText.setText("jTextField1");
        jPanel7.add(jPropertyValueText);
        jPropertyValueText.setBounds(8, 0, 230, 20);

        jPropertyAddButton.setText(bundle.getString("Button.Add")); // NOI18N
        jPropertyAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPropertyAddButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelPropertiesLayout = new javax.swing.GroupLayout(jPanelProperties);
        jPanelProperties.setLayout(jPanelPropertiesLayout);
        jPanelPropertiesLayout.setHorizontalGroup(
            jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboProperties, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPropertyAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelPropertiesLayout.setVerticalGroup(
            jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPropertyAddButton)
                    .addGroup(jPanelPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jComboProperties)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.add(jPanelProperties, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab(AppLocal.getIntString("label.properties"), jPanel3); // NOI18N

        jBtnSupplierWeb.setText(bundle.getString("Button.Browser")); // NOI18N
        jBtnSupplierWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSupplierWebActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBtnSupplierWeb)
                .addContainerGap(520, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBtnSupplierWeb)
                .addContainerGap(357, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("label.supplierlink"), jPanel6); // NOI18N

        add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 630, 420));
    }// </editor-fold>//GEN-END:initComponents

    private void m_jRefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jRefFocusLost
        setCode();
    }//GEN-LAST:event_m_jRefFocusLost

    private void m_jNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jNameFocusLost
        setDisplay();
    }//GEN-LAST:event_m_jNameFocusLost

    private void jButtonHTMLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonHTMLMouseClicked
        setButtonHTML();
    }//GEN-LAST:event_jButtonHTMLMouseClicked


    private void m_jAlwaysAvailableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jAlwaysAvailableActionPerformed
        if (m_jAlwaysAvailable.isSelected()) {
            m_jInCatalog.setSelected(false);
        }
    }//GEN-LAST:event_m_jAlwaysAvailableActionPerformed

    private void m_jInCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jInCatalogActionPerformed
        if (m_jInCatalog.isSelected()) {
            m_jCatalogOrder.setEnabled(true);
        } else {
            m_jCatalogOrder.setEnabled(false);
            m_jCatalogOrder.setText(null);
        }

        if (m_jInCatalog.isSelected()) {
            m_jAlwaysAvailable.setSelected(false);
        }
    }//GEN-LAST:event_m_jInCatalogActionPerformed

    private void m_jIsPackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jIsPackActionPerformed
        m_jPackQuantity.setEnabled(m_jIsPack.isSelected());
        m_jPackProduct.setEnabled(m_jIsPack.isSelected());
        jLabelPackQuantity.setEnabled(m_jIsPack.isSelected());
        jLabelPackProduct.setEnabled(m_jIsPack.isSelected());
    }//GEN-LAST:event_m_jIsPackActionPerformed

    private void jCheckBoxPromotionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxPromotionActionPerformed
        if (jCheckBoxPromotion.isSelected()) {
            jComboBoxPromotion.setEnabled(true);
        } else {
            jComboBoxPromotion.setEnabled(false);
            jComboBoxPromotion.getModel().setSelectedItem(null);
        }
    }//GEN-LAST:event_jCheckBoxPromotionActionPerformed

    private void m_jPackQuantityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jPackQuantityFocusLost
       Object selectItem =  m_jPackProduct.getSelectedItem();
       Object selectIndex = m_jPackProduct.getSelectedItem();
        try {
            packproductmodel = new ComboBoxValModel(packproductsent.list());
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }       
        m_jPackProduct.setModel(packproductmodel);
        if (selectItem != null){
            m_jPackProduct.setSelectedItem(selectItem);
            m_jPackProduct.setSelectedItem(selectIndex);            
        }
    }//GEN-LAST:event_m_jPackQuantityFocusLost

    private void m_jRetiredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jRetiredActionPerformed
        if (m_jRetired.isSelected()) {
            Object[] options = {AppLocal.getIntString("Button.Yes"),
                AppLocal.getIntString("Button.No") };
            
            if (JOptionPane.showOptionDialog(this,
                AppLocal.getIntString("message.retiringproduct") ,
                AppLocal.getIntString("Menu.Products"),
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, null,
                options, options[1]) == 0) {
                   
                m_jInCatalog.setSelected(false);
                m_jAlwaysAvailable.setSelected(false);
            } else {
                m_jRetired.setSelected( false );
            }
        }
    }//GEN-LAST:event_m_jRetiredActionPerformed

    private void jPropertyAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPropertyAddButtonActionPerformed
        Properties props = new Properties();

        try {
            if( !txtProperties.getText().isEmpty() ) {
                props.loadFromXML(new ByteArrayInputStream(txtProperties.getText().getBytes(StandardCharsets.UTF_8)));
            }
        } catch (IOException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String sel = (String) jComboProperties.getSelectedItem();
        String type = m_PropertyOptions.getProperty( sel, "" );
            
        int nComma = type.indexOf( ',' );
        if( nComma >0 ) {
            type = type.substring( 0, nComma ).trim();
        }
            
        switch( type ) {
            case "boolean" :
                String sYes = (String) jPropertyValueCombo.getSelectedItem();
                sYes = sYes.compareTo( "Yes" ) == 0 ? "1" : "0";
                props.put( sel, sYes );
                break;
            case "number" :
                Double dValue;
                try {
                    dValue = (Double) Formats.DOUBLE.parseValue(jPropertyValueText.getText());
                } catch (BasicException ex) {
                    dValue = 0.0;
                }
                props.put( sel, dValue.toString() );
                break;
            case "option" :
                props.put( sel, (String) jPropertyValueCombo.getSelectedItem());
                break;
            case "text" :
                props.put( sel, (String) jPropertyValueText.getText());
                break;
            default:
                break;
        }           
        
        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            props.storeToXML(o, AppLocal.APP_NAME, "UTF-8");
            txtProperties.setText(o.toString());
        } catch (IOException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jPropertyAddButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String code = "";
        String name = null;
        do {
            code = BarcodeValidator.CreateRandomBarcode();
            try {
                name = m_dlSales.getProductNameByCode(code);
            } catch (BasicException ex) {
            }
        } while( ! StringUtils.isBlank(name) );
            
        m_jCode.setText( code );        
    }//GEN-LAST:event_jButton2ActionPerformed

    public void scrapeSupplierWeb() {                                                
        if( webscraper == null ) {
            webscraper = new WebScrapeBookers();
            webscraper.StartScraper( webscraper.getSearchUrl(m_jCode.getText()),
                    new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if( e.getActionCommand().matches("PageLoadComplete") ) {
                        webscraper.checkEnableOK();
                    } else if( e.getActionCommand().matches("OK") ) {
                        // User has pressed snapshot button
                        ProductInfoExt info = webscraper.decodeCurrentPage( getProductInfo() );
                        if( info == null ) {
                            // Cancelled - leave the scraper in view
                            return;
                        }
                        setProductInfo( info );

                        webscraper.saveState();
                        webscraper.setVisible( false ); 
                    } else if( e.getActionCommand().matches("Cancel") ) {
                        webscraper.saveState();
                        webscraper.setVisible( false ); 
                    }
                }
            });   
        } else {
            webscraper.findCode( m_jCode.getText() );
        }
        webscraper.setVisible( true );
    }                                               

    private void jBtnSupplierWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSupplierWebActionPerformed
        scrapeSupplierWeb();
    }//GEN-LAST:event_jBtnSupplierWebActionPerformed

    private void updateBarcodeList() {
        String product =  getID();
        if( product != null && !product.isEmpty() ) {
            m_sentbarcodes = m_dlSales.getBarcodesList( product );
            try {
                m_BarcodesModel = new ListValModel( m_sentbarcodes.list() );
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(ex);
                msg.show(this);                   
                m_BarcodesModel = new ListValModel();
            }
        } else {
            m_BarcodesModel = new ListValModel();
        }

        jListBarcodes.setModel( m_BarcodesModel );
        
        jOtherBarcode.setText("");
        jOtherQuantity.setText("");
    }
    
    private void jButtonAddCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddCodeActionPerformed
        
        if( jOtherBarcode.getText().isEmpty() )
            return;
        
        Double dQty = Double.parseDouble( jOtherQuantity.getText() );
        BarcodeInfo info = new BarcodeInfo(  jOtherBarcode.getText(), getID(), jOtherPackType.getText(), dQty   );
        try {
            m_dlSales.addProductCode( info );
            updateBarcodeList();
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(ex);
            msg.show(this);                   
        }
    }//GEN-LAST:event_jButtonAddCodeActionPerformed

    private void jButtonDeleteCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteCodeActionPerformed
        int idx = jListBarcodes.getSelectedIndex();
        BarcodeInfo info = (BarcodeInfo) m_BarcodesModel.getElementAt(idx);
        if( info != null ){
            try {
                m_dlSales.removeProductCode( info.getCode() );
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(ex);
                msg.show(this);                   
            }
            updateBarcodeList();
        }
    }//GEN-LAST:event_jButtonDeleteCodeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnSupplierWeb;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonAddCode;
    private javax.swing.JButton jButtonDeleteCode;
    private javax.swing.JButton jButtonHTML;
    private eu.hansolo.custom.SteelCheckBox jCheckBoxPromotion;
    private javax.swing.JComboBox jComboBoxPromotion;
    private javax.swing.JComboBox<String> jComboProperties;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelPackProduct;
    private javax.swing.JLabel jLabelPackQuantity;
    private javax.swing.JList<String> jListBarcodes;
    private javax.swing.JTextField jOtherBarcode;
    private javax.swing.JTextField jOtherPackType;
    private javax.swing.JTextField jOtherQuantity;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCodes;
    private javax.swing.JPanel jPanelProperties;
    private javax.swing.JButton jPropertyAddButton;
    private javax.swing.JComboBox<String> jPropertyValueCombo;
    private javax.swing.JTextField jPropertyValueText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField m_jAlias;
    private eu.hansolo.custom.SteelCheckBox m_jAlwaysAvailable;
    private javax.swing.JComboBox m_jAtt;
    private javax.swing.JTextField m_jCatalogOrder;
    private javax.swing.JComboBox m_jCategory;
    private eu.hansolo.custom.SteelCheckBox m_jCheckWarrantyReceipt;
    private javax.swing.JTextField m_jCode;
    private javax.swing.JComboBox m_jCodetype;
    private eu.hansolo.custom.SteelCheckBox m_jComment;
    private eu.hansolo.custom.SteelCheckBox m_jDiscounted;
    private javax.swing.JTextPane m_jDisplay;
    private javax.swing.JTextField m_jGrossProfit;
    private uk.chromis.data.gui.JImageEditor m_jImage;
    private eu.hansolo.custom.SteelCheckBox m_jInCatalog;
    private eu.hansolo.custom.SteelCheckBox m_jIsPack;
    private eu.hansolo.custom.SteelCheckBox m_jKitchen;
    private eu.hansolo.custom.SteelCheckBox m_jManageStock;
    private javax.swing.JTextField m_jName;
    private javax.swing.JComboBox m_jPackProduct;
    private javax.swing.JTextField m_jPackQuantity;
    private javax.swing.JTextField m_jPriceBuy;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JTextField m_jPriceSellTax;
    private javax.swing.JTextField m_jRef;
    private eu.hansolo.custom.SteelCheckBox m_jRetired;
    private eu.hansolo.custom.SteelCheckBox m_jScale;
    private eu.hansolo.custom.SteelCheckBox m_jService;
    private javax.swing.JTextField m_jStockUnits;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JTextField m_jTextTip;
    private javax.swing.JLabel m_jTitle;
    private eu.hansolo.custom.SteelCheckBox m_jVerpatrib;
    private eu.hansolo.custom.SteelCheckBox m_jVprice;
    private javax.swing.JTextField m_jmargin;
    private javax.swing.JTextField m_jstockcost;
    private javax.swing.JTextField m_jstockvolume;
    private javax.swing.JTextArea txtProperties;
    // End of variables declaration//GEN-END:variables

}
