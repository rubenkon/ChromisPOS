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

package uk.chromis.pos.inventory;

import uk.chromis.basic.BasicException;
import uk.chromis.data.loader.DataRead;
import uk.chromis.data.loader.IKeyed;
import uk.chromis.data.loader.SerializableRead;

/**
 *
 * @author adrianromero
 * Created on February 13, 2007, 10:13 AM
 *
 */
public class ProductHistoryInfo implements SerializableRead, IKeyed {
    
    private static final long serialVersionUID = 9032687595230L;
    private String m_sID;
    private String m_sDate;
    private int m_reason;
    private double m_units;
    
    /** Creates a new instance of LocationInfo */
    public ProductHistoryInfo() {
        m_sID = null;
        m_sDate = null;
        m_reason = 0;
        m_units = 0;
    }
    
    /**
     *
     * @return
     */
    public Object getKey() {
        return m_sID;
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    public void readValues(DataRead dr) throws BasicException {
        m_sID = dr.getString(1);
        m_sDate = dr.getString(2);
        m_reason = dr.getInt(3);
        m_units = dr.getDouble(4);
    }

    /**
     *
     * @param sID
     */
    public void setID(String sID) {
        m_sID = sID;
    }
    
    /**
     *
     * @return
     */
    public String getID() {
        return m_sID;
    }

    /**
     *
     * @return
     */
    public String getDate() {
        return m_sDate;
    }
    
    /**
     *
     * @return
     */
    public int getReason() {
        return m_reason;
    }
    
    /**
     *
     * @return
     */
    public double getUnits() {
        return m_units;
    }
    
    public String toString(){
        
        String sReason = "Unknown";
        if( m_reason == (Integer) (MovementReason.IN_PURCHASE.getKey()) )
            sReason = (String) (MovementReason.IN_PURCHASE.toString());
        if( m_reason == (Integer) (MovementReason.OUT_SALE.getKey()) )
            sReason = (String) (MovementReason.OUT_SALE.toString());
        if( m_reason == (Integer) (MovementReason.IN_STOCKCHANGE.getKey()) )
            sReason = (String) (MovementReason.IN_STOCKCHANGE.toString());
        if( m_reason == (Integer) (MovementReason.OUT_STOCKCHANGE.getKey()) )
            sReason = (String) (MovementReason.OUT_STOCKCHANGE.toString());
        if( m_reason == (Integer) (MovementReason.OUT_BREAK.getKey()) )
            sReason = (String) (MovementReason.OUT_BREAK.toString());
        if( m_reason == (Integer) (MovementReason.IN_REFUND.getKey()) )
            sReason = (String) (MovementReason.IN_REFUND.toString());
        if( m_reason == (Integer) (MovementReason.OUT_REFUND.getKey()) )
            sReason = (String) (MovementReason.OUT_REFUND.toString());
        if( m_reason == (Integer) (MovementReason.IN_MOVEMENT.getKey()) )
            sReason = (String) (MovementReason.IN_MOVEMENT.toString());
        if( m_reason == (Integer) (MovementReason.OUT_MOVEMENT.getKey()) )
            sReason = (String) (MovementReason.OUT_MOVEMENT.toString());
        if( m_reason == (Integer) (MovementReason.IN_OPEN_PACK.getKey()) )
            sReason = (String) (MovementReason.IN_OPEN_PACK.toString());
        if( m_reason == (Integer) (MovementReason.OUT_OPEN_PACK.getKey()) )
            sReason = (String) (MovementReason.OUT_OPEN_PACK.toString());
        
        return ( m_sDate.toString() + " " + m_units + " " + sReason  );                
    }    
}
