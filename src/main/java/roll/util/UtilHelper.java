/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.util;

import roll.parser.hoa.Valuation;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilHelper {
    
    public static int ONE = 1;
    public static int ZERO = 0;
    
    public static boolean isEven(int num) {
        return (num & UtilHelper.ONE) == 0;
    }
    
    public static boolean isOdd(int num) {
        return (num & UtilHelper.ONE) != 0;
    }
    
    public static int getNumBits(int num) {
    	return Integer.SIZE - Integer.numberOfLeadingZeros(num - 1);
    }

	public static Valuation translateInteger(int value, int numBits) {
		int bit = 1;
        Valuation result = new Valuation(numBits);
        for (int index = 0; index < numBits; index++) {
            if ((bit & value) == 0) {
                result.clear(index);
            }else {
            	result.set(index);
            }
            bit <<= 1;
        }
        return result;
	}

}
