//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) 2015 
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

package uk.chromis.pos.inventory;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import uk.chromis.basic.BasicException;
import uk.chromis.data.gui.MessageInf;
import uk.chromis.data.loader.LocalRes;
import uk.chromis.data.user.DirtyManager;
import uk.chromis.data.user.SaveProvider;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.DataLogicSales;
import uk.chromis.pos.forms.DataLogicSystem;

/**
 *
 * @author adrianromero
 */
public class JDlgEditProduct extends javax.swing.JDialog {
    
    private ProductsEditor producteditor;
    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;
    private DirtyManager m_dirty;
    private SaveProvider m_SaveProvider;
    private CompletionCallback m_CallBacks;
    static private int STATE_INSERT = 0;
    static private int STATE_UPDATE = 1;
    
    private int state = STATE_INSERT;
    
    interface CompletionCallback 
    {
        void notifyCompletionOk( String reference );
        void notifyCompletionCancel();
    }
 
    /** Creates new form JDlgUploadProducts */
    public JDlgEditProduct(JFrame parent, boolean modal) {
        super(parent, modal);
        m_CallBacks = null;
    }

    public void setCallbacks( CompletionCallback callBacks ) {
        m_CallBacks = callBacks;
    }
    
    public void init( DataLogicSales dlSales, DataLogicSystem dlSystem, DirtyManager dirty ) {
        m_dlSales = dlSales;
        m_dlSystem = dlSystem;
        m_dirty = dirty;
        initComponents();

        m_SaveProvider = new SaveProvider(
            m_dlSales.getProductCatUpdate(),
            m_dlSales.getProductCatInsert(),
            m_dlSales.getProductCatDelete());
        
        getRootPane().setDefaultButton(jcmdOK);   
   
        producteditor = new ProductsEditor(m_dlSales, m_dlSystem, m_dirty);       
        
        try {
            producteditor.activate();
            
        } catch (BasicException ex) {
            Logger.getLogger(JDlgEditProduct.class.getName()).log(Level.SEVERE, null, ex);
        }
        jPanelEditor.add( producteditor );
        
    }

    public void setProduct( String productID, String barcode ) {
        if( productID == null ) {
            state = STATE_INSERT;
        } else {
            state = STATE_UPDATE;                
        }

        producteditor.setProduct( productID, barcode );
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jcmdOK = new javax.swing.JButton();
        jcmdCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanelEditor = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("caption.producteditor")); // NOI18N
        setResizable(false);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("Button.OK")); // NOI18N
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdOK);

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("Button.Cancel")); // NOI18N
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdCancel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel1.setLayout(null);
        jPanel1.add(jPanelEditor);
        jPanelEditor.setBounds(0, 0, 620, 480);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(634, 566));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        if( m_CallBacks != null ) {
            m_CallBacks.notifyCompletionCancel();
        }
        dispose();
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
       boolean bOK = false;
        String reference = null;
        
        if( producteditor != null ) {
            try {
                Object values = producteditor.createValue();
                if( state == STATE_INSERT ) {
                    if (m_SaveProvider.insertData(values) > 0) {
                        bOK = true;
                    } else {
                        throw new BasicException(LocalRes.getIntString("exception.noupdate"));
                    }
                } else {
                    if (m_SaveProvider.updateData(values) > 0 ) {
                        bOK = true;
                    } else {
                        throw new BasicException(LocalRes.getIntString("exception.noupdate"));
                    }
                }
                
                Object [] aValues = (Object [] ) values;
                reference = (String) aValues[DataLogicSales.INDEX_REFERENCE];
                
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosave"), ex);
                msg.show(this);
                return;
            }
        }
        
        if( m_CallBacks != null ) {
            if( bOK && producteditor != null ) {
                m_CallBacks.notifyCompletionOk( reference );
            } else {
                m_CallBacks.notifyCompletionCancel();                
            }
        }
        dispose();

    }//GEN-LAST:event_jcmdOKActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelEditor;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    // End of variables declaration//GEN-END:variables
    
}
