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

package uk.chromis.pos.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import uk.chromis.basic.BasicException;
import uk.chromis.data.user.ListProvider;
import uk.chromis.data.user.ListProviderCreator;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.DataLogicSales;
import uk.chromis.pos.ticket.ProductFilterSales;
import uk.chromis.pos.ticket.ProductFilterSearch;
import uk.chromis.pos.ticket.ProductInfoExt;
import uk.chromis.pos.ticket.ProductRenderer;

/**
 *
 * @author adrianromero
 */
public class JProductFinder extends javax.swing.JDialog {

    private ProductInfoExt m_ReturnProduct;
    private ListProvider lpr;
    
    public final static int PRODUCT_ALL = 0;
    public final static int PRODUCT_NORMAL = 1;
    public final static int PRODUCT_AUXILIAR = 2;
    public final static int PRODUCT_RECIPE = 3;
    public final static int PRODUCT_SIMPLE = 4;
    
    private int m_productsType = PRODUCT_ALL;
    private DataLogicSales m_dlSales;
    private Component m_FilterComponent;
    
    /** Creates new form JProductFinder */
    private JProductFinder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JProductFinder */
    private JProductFinder(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }    
    
    private static int PRODUCT_FINDER_LIMIT = 1000;
    
    private ProductInfoExt init(DataLogicSales dlSales, int productsType ) {
        m_productsType = productsType;
        m_dlSales = dlSales;
        
        initComponents();
        
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        jListProducts.setCellRenderer(new ProductRenderer());

        switch (m_productsType) {
            case PRODUCT_NORMAL:
            {
                ProductFilterSales jproductfilter = new ProductFilterSales(m_dlSales, m_jKeys );
                m_jProductSelect.add(jproductfilter, BorderLayout.CENTER);
                lpr = new ListProviderCreator(dlSales.getProductListNormal( PRODUCT_FINDER_LIMIT ), jproductfilter);
                jproductfilter.activate();
                m_FilterComponent = jproductfilter;
                jButtonMore.setEnabled(false);
            }  break;
            case PRODUCT_SIMPLE:
            {
                ProductFilterSearch jproductfilter = new ProductFilterSearch(m_dlSales, m_jKeys );
                m_jProductSelect.add(jproductfilter, BorderLayout.CENTER);
                lpr = new ListProviderCreator(dlSales.getProductListSearch( PRODUCT_FINDER_LIMIT ), jproductfilter);
                jproductfilter.activate();
                m_FilterComponent = jproductfilter;
                jproductfilter.setVisible(true);
                jButtonMore.setEnabled(true);
            }  break;
            case PRODUCT_AUXILIAR:               
            {
                ProductFilterSales jproductfilter = new ProductFilterSales(m_dlSales, m_jKeys );
                m_jProductSelect.add(jproductfilter, BorderLayout.CENTER);
                lpr = new ListProviderCreator(dlSales.getProductListAuxiliar(PRODUCT_FINDER_LIMIT), jproductfilter);
                jproductfilter.activate();
                m_FilterComponent = jproductfilter;
                jButtonMore.setEnabled(false);
            }   break;
            default: // PRODUCT_ALL
            {
                ProductFilterSales jproductfilter = new ProductFilterSales(m_dlSales, m_jKeys );
                m_jProductSelect.add(jproductfilter, BorderLayout.CENTER);
                lpr = new ListProviderCreator(dlSales.getProductList(PRODUCT_FINDER_LIMIT), jproductfilter);
                jproductfilter.activate();
                m_FilterComponent = jproductfilter;
                jButtonMore.setEnabled(false);
            }   break;                
        }
       
        getRootPane().setDefaultButton(jButtonExecute);   
   
        m_ReturnProduct = null;
        
        setVisible(true);
        
        return m_ReturnProduct;
    }
    
    
    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    /**
     *
     * @param parent
     * @param dlSales
     * @return
     */
    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales) {
        return showMessage(parent, dlSales, PRODUCT_SIMPLE);
    }

    /**
     *
     * @param parent
     * @param dlSales
     * @param productsType
     * @return
     */
    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales, int productsType) {

        Window window = getWindow(parent);

        JProductFinder myMsg;
        if (window instanceof Frame) {
            myMsg = new JProductFinder((Frame) window, true);
        } else {
            myMsg = new JProductFinder((Dialog) window, true);
        }
        return myMsg.init(dlSales, productsType);
    }
    
    private static class MyListData extends javax.swing.AbstractListModel {
        
        private final java.util.List m_data;
        
        public MyListData(java.util.List data) {
            m_data = data;
        }
        
        @Override
        public Object getElementAt(int index) {
            return m_data.get(index);
        }
        
        @Override
        public int getSize() {
            return m_data.size();
        } 
    } 
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new uk.chromis.editor.JEditorKeys();
        jPanel2 = new javax.swing.JPanel();
        m_jProductSelect = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButtonExecute = new javax.swing.JButton();
        jButtonMore = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListProducts = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jcmdOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("form.productslist")); // NOI18N
        setMinimumSize(new java.awt.Dimension(670, 361));
        setPreferredSize(new java.awt.Dimension(670, 361));

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(m_jKeys, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel4, java.awt.BorderLayout.LINE_END);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jProductSelect.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButtonExecute.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jButtonExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/ok.png"))); // NOI18N
        jButtonExecute.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jButtonExecute.setToolTipText(bundle.getString("tiptext.executefilter")); // NOI18N
        jButtonExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteActionPerformed(evt);
            }
        });
        jPanel3.add(jButtonExecute, new org.netbeans.lib.awtextra.AbsoluteConstraints(145, 5, -1, -1));

        jButtonMore.setText("...");
        jButtonMore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoreActionPerformed(evt);
            }
        });
        jPanel3.add(jButtonMore, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 0, -1, -1));

        m_jProductSelect.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel2.add(m_jProductSelect, java.awt.BorderLayout.NORTH);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListProducts.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jListProducts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListProducts.setPreferredSize(new java.awt.Dimension(400, 300));
        jListProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListProductsMouseClicked(evt);
            }
        });
        jListProducts.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListProductsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListProducts);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("Button.Cancel")); // NOI18N
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.setMaximumSize(new java.awt.Dimension(103, 44));
        jcmdCancel.setMinimumSize(new java.awt.Dimension(103, 44));
        jcmdCancel.setPreferredSize(new java.awt.Dimension(103, 44));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jPanel6.setMinimumSize(new java.awt.Dimension(250, 56));
        jPanel6.setPreferredSize(new java.awt.Dimension(250, 56));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("Button.OK")); // NOI18N
        jcmdOK.setEnabled(false);
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.setMaximumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setMinimumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setPreferredSize(new java.awt.Dimension(103, 44));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel6.add(jcmdOK);

        jPanel1.add(jPanel6);

        jPanel2.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(665, 565));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jListProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProductsMouseClicked

        if (evt.getClickCount() == 2) {
            m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
            dispose();
        }
        
    }//GEN-LAST:event_jListProductsMouseClicked

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        
        m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
        dispose();
        
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        
        dispose();
        
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jListProductsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProductsValueChanged

        jcmdOK.setEnabled(jListProducts.getSelectedValue() != null);
        
    }//GEN-LAST:event_jListProductsValueChanged

    private void jButtonExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteActionPerformed

        try {
            jListProducts.setModel(new MyListData(lpr.loadData()));
            if (jListProducts.getModel().getSize() > 0) {
                jListProducts.setSelectedIndex(0);
            }
        } catch (BasicException ex) {
            Logger.getLogger(JProductFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jButtonExecuteActionPerformed

    private void jButtonMoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoreActionPerformed
        ProductFilterSales jproductfilter = new ProductFilterSales(m_dlSales, m_jKeys );
        m_jProductSelect.remove(m_FilterComponent);
        m_jProductSelect.add(jproductfilter, BorderLayout.CENTER);
        lpr = new ListProviderCreator(m_dlSales.getProductListNormal( PRODUCT_FINDER_LIMIT ), jproductfilter);
        jproductfilter.activate();
        m_FilterComponent = jproductfilter;
        jButtonMore.setEnabled(false);
    }//GEN-LAST:event_jButtonMoreActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExecute;
    private javax.swing.JButton jButtonMore;
    private javax.swing.JList jListProducts;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private uk.chromis.editor.JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jProductSelect;
    // End of variables declaration//GEN-END:variables
    
}
