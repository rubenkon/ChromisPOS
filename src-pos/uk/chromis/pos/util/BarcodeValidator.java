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
package uk.chromis.pos.util;

import java.util.Random;

/**
 *
 * @author John
 */
public class BarcodeValidator {

    public BarcodeValidator() {

    }

    public static String CreateRandomBarcode() {
 
        int min = 99900000;
        int max = 99999999;

        Random r = new Random();
        int randomnumber = r.nextInt(max - min + 1) + min;

        String code = String.valueOf(randomnumber);
        
        // Create a checkdigit
        // this will be a running total
        int sum = 0;

        // loop through digits from right to left
        for (int i = 0; i < code.length(); i++) {

            // set ch to "current" character to be processed
            char ch = code.charAt(code.length() - i - 1);

            // our "digit" is calculated using ASCII value - 48
            int digit = ch - 48;

            // weight will be the current digit's contribution to
            // the running total
            int weight;
            if (i % 2 == 0) {

                // for alternating digits starting with the rightmost, we
                // use our formula this is the same as multiplying x 2 and
                // adding digits together for values 0 to 9. Using the
                // following formula allows us to gracefully calculate a
                // weight for non-numeric "digits" as well (from their
                // ASCII value - 48).
                weight = (2 * digit) - (digit / 5) * 9;

            } else {

                // even-positioned digits just contribute their ascii
                // value minus 48
                weight = digit;

            }

            // keep a running total of weights
            sum += weight;

        }
        // avoid sum less than 10 (if characters below "0" allowed,
        // this could happen)
        sum = Math.abs(sum) + 10;
        // check digit is amount needed to reach next number
        // divisible by ten
        int Checkdigit = (10 - (sum % 10)) % 10;

        code = code + Checkdigit;
        return code;
    }
    
    public static String BarcodeValidate(String barcode) {

        if (barcode.matches("[0-9]+")) {

            int bSize = barcode.length();
            int odd;
            int even;
            int checkDigit;
            int checkDigit2;

            switch (bSize) {
                case 7:
                    odd = (Character.getNumericValue(barcode.charAt(0))
                            + Character.getNumericValue(barcode.charAt(2))
                            + Character.getNumericValue(barcode.charAt(4))) * 3;

                    even = Character.getNumericValue(barcode.charAt(1))
                            + Character.getNumericValue(barcode.charAt(3))
                            + Character.getNumericValue(barcode.charAt(5));

                    checkDigit = (10 - ((odd + even) % 10)) % 10;

                    if (checkDigit == Character.getNumericValue(barcode.charAt(6))) {
                        return "UPC-E";
                    } else {
                        return "null";
                    }
                case 8:
                    odd = (Character.getNumericValue(barcode.charAt(0))
                            + Character.getNumericValue(barcode.charAt(2))
                            + Character.getNumericValue(barcode.charAt(4))
                            + Character.getNumericValue(barcode.charAt(6))) * 3;

                    even = Character.getNumericValue(barcode.charAt(1))
                            + Character.getNumericValue(barcode.charAt(3))
                            + Character.getNumericValue(barcode.charAt(5));

                    checkDigit = (10 - ((odd + even) % 10)) % 10;

                    if (checkDigit == Character.getNumericValue(barcode.charAt(7))) {
                        return "EAN-8";
                    } else {
                        return "null";
                    }

                case 12:
                    odd = (Character.getNumericValue(barcode.charAt(0))
                            + Character.getNumericValue(barcode.charAt(2))
                            + Character.getNumericValue(barcode.charAt(4))
                            + Character.getNumericValue(barcode.charAt(6))
                            + Character.getNumericValue(barcode.charAt(8))
                            + Character.getNumericValue(barcode.charAt(10))) * 3;

                    even = Character.getNumericValue(barcode.charAt(1))
                            + Character.getNumericValue(barcode.charAt(3))
                            + Character.getNumericValue(barcode.charAt(5))
                            + Character.getNumericValue(barcode.charAt(7))
                            + Character.getNumericValue(barcode.charAt(9));

                    checkDigit = (10 - ((odd + even) % 10))% 10;

                    if (checkDigit == Character.getNumericValue(barcode.charAt(11))) {
                        return "UPC-A";
                    } else {
                        return "null";
                    }
                case 13:
                    odd = (Character.getNumericValue(barcode.charAt(0))
                            + Character.getNumericValue(barcode.charAt(2))
                            + Character.getNumericValue(barcode.charAt(4))
                            + Character.getNumericValue(barcode.charAt(6))
                            + Character.getNumericValue(barcode.charAt(8))
                            + Character.getNumericValue(barcode.charAt(10))) * 3;

                    even = Character.getNumericValue(barcode.charAt(1))
                            + Character.getNumericValue(barcode.charAt(3))
                            + Character.getNumericValue(barcode.charAt(5))
                            + Character.getNumericValue(barcode.charAt(7))
                            + Character.getNumericValue(barcode.charAt(9))
                            + Character.getNumericValue(barcode.charAt(11));

                    checkDigit = (10 - ((odd + even) % 10)) % 10;
                    checkDigit2 = (10 - (((odd / 3) + (even * 3)) % 10))% 10;    
                    
                    if (checkDigit == Character.getNumericValue(barcode.charAt(12))) {
                        return "EAN-13";
                    } else if (checkDigit2 == Character.getNumericValue(barcode.charAt(12))) {
                        return "EAN-13";
                    } else {
                        return "null";
                    }
                case 14:
                    odd = (Character.getNumericValue(barcode.charAt(0))
                            + Character.getNumericValue(barcode.charAt(2))
                            + Character.getNumericValue(barcode.charAt(4))
                            + Character.getNumericValue(barcode.charAt(6))
                            + Character.getNumericValue(barcode.charAt(8))
                            + Character.getNumericValue(barcode.charAt(10))
                            + Character.getNumericValue(barcode.charAt(12))) * 3;

                    even = Character.getNumericValue(barcode.charAt(1))
                            + Character.getNumericValue(barcode.charAt(3))
                            + Character.getNumericValue(barcode.charAt(5))
                            + Character.getNumericValue(barcode.charAt(7))
                            + Character.getNumericValue(barcode.charAt(9))
                            + Character.getNumericValue(barcode.charAt(11));

                    checkDigit = (10 - ((odd + even) % 10)) % 10;

                    if (checkDigit == Character.getNumericValue(barcode.charAt(13))) {
                        return "GTIN";
                    } else {
                        return "null";
                    }
                default:
                    return "CODE128";
            }
        } else {
            return "CODE128";
        }
    }
}
