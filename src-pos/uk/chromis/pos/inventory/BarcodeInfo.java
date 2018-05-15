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
import uk.chromis.data.loader.SerializerRead;

/**
 *
 * @author John
 */
public class BarcodeInfo implements IKeyed {
    
    private static final long serialVersionUID = 9032683595267L;
    private String m_Code;
    private String m_Product;
    private String m_PackType;
    private Double m_Quantity;

    /** Creates a new instance of PromotionInfo
     * @param Code
     * @param Product
     * @param PackType
     * @param Quantity */
    public BarcodeInfo( String Code, String Product, String PackType, Double Quantity )
    {
        m_Code = Code;
        m_Product = Product;
        m_PackType = PackType;
        m_Quantity = Quantity;
    }
    
    @Override
    public Object getKey() {
        return getCode();
    }
    
    /**
     *
     * @return
     */
    public String getCode() {
        return m_Code;
    }

    /**
     *
     * @param Code
     */
    public void setCode(String Code) {
        m_Code = Code;
    }
    
    /**
     *
     * @return
     */
    public String getProduct() {
        return m_Product;
    }

    /**
     *
     * @param Product
     */
    public void setProduct( String Product ) {
        m_Product = Product;
    }
    
    /**
     *
     * @return
     */
    public String getPackType() {
        return m_PackType;
    }
    
    /**
     *
     * @param PackType
     */
    public void setPackType( String PackType ) {
        m_PackType = PackType;
    }
    
    /**
     *
     * @return  */
    public Double getQuantity() {
        return m_Quantity;
    }  
    
    /**
     *
     * @param Quantity
     */
    public void setQuantity(Double Quantity) {
        m_Quantity = Quantity;
    }  

    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {

                BarcodeInfo info = new BarcodeInfo(
                    dr.getString(1),
                    dr.getString(2),
                    dr.getString(3),
                    dr.getDouble(4)
                    );
                return info;
            }
        };
    }

    
    @Override
    public String toString(){
        return m_Code;
    }    

}
