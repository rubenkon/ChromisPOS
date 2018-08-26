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
package uk.chromis.pos.ticket;

import uk.chromis.basic.BasicException;
import uk.chromis.data.user.EditorCreator;
import uk.chromis.editor.JEditorKeys;
import uk.chromis.pos.forms.DataLogicSales;

/**
 *
 *
 */
public class ProductFilterSearch extends javax.swing.JPanel implements EditorCreator {

    /**
     * Creates new form ProductFilterSales
     *
     * @param dlSales
     * @param jKeys
     */
    public ProductFilterSearch(DataLogicSales dlSales ) {
            
        initComponents();
        
        jIncludeRetired.setSelected(false);
    }

    /**
     *
     */
    public void activate() {

        m_jtxtName.setText("");

    }

    /**
     *
     * @return @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {

        Object[] afilter = new Object[5];

        // Product Name/ref or barcode
        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[0] = null;
            afilter[1] = null;
            afilter[2] = null;
            afilter[3] = null;
            afilter[4] = null;
        } else {
            afilter[0] = jIncludeRetired.isSelected();
            afilter[1] = "%" + m_jtxtName.getText() + "%";
            afilter[2] = m_jtxtName.getText();
            afilter[3] = m_jtxtName.getText();
            afilter[4] = m_jtxtName.getText();
        }
        
        return afilter;
    }

    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jIncludeRetired = new javax.swing.JCheckBox();
        m_jtxtName = new javax.swing.JTextField();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setPreferredSize(new java.awt.Dimension(370, 50));
        setLayout(null);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jIncludeRetired.setText(bundle.getString("label.retired")); // NOI18N
        add(jIncludeRetired);
        jIncludeRetired.setBounds(350, 10, 120, 23);

        m_jtxtName.setText("jTextField1");
        add(m_jtxtName);
        m_jtxtName.setBounds(19, 10, 310, 20);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jIncludeRetired;
    private javax.swing.JTextField m_jtxtName;
    // End of variables declaration//GEN-END:variables

}