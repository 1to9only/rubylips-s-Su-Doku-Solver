/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com March 2005
 * 
 * Web site: http://act365.com/sudoku
 * E-mail: developers@act365.com
 * 
 * The Su Doku Solver solves Su Doku problems - see http://www.sudoku.com.
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.act365.sudoku;

/**
 * SuDokuUtils contains utility functions that are called from several other classes.
 */

public class SuDokuUtils {

    /**
     * Creates a string representation of a two-dimensional integer array.
     * @param maxDatum maximum permitted value in the data array
     */

    public static String toString( int[][] data , int boxesAcross , int maxDatum ){
        
        final int cellsInRow = data.length ,
                  boxesDown = data.length / boxesAcross ;
                  
        StringBuffer sb = new StringBuffer();
        
        int i , j , k , v ;
        int number = maxDatum , fieldWidth = 1 , numberWidth ;
        while( ( number /= 10 ) >= 1 ){
            ++ fieldWidth ;
        }
        i = 0 ;
        while( i < cellsInRow ){
            if( i > 0 && i % boxesAcross == 0 ){
                k = 0 ;
                while( k < ( fieldWidth + 1 )* cellsInRow + ( boxesAcross - 1 )* 2 ){
                    sb.append('-');
                    ++ k ;
                }
                sb.append(" \n");
            }
            j = 0 ;
            while( j < cellsInRow ){
                if( j > 0 && j % boxesDown == 0 ){
                    sb.append(" |");
                }
                k = 0 ;
                if( data[i][j] > 0 ){
                    numberWidth = 1 ;
                    number = data[i][j];
                    while( ( number /= 10 ) >= 1 ){
                        ++ numberWidth ;
                    }
                    while( k < 1 + fieldWidth - numberWidth ){
                        sb.append(' ');
                        ++ k ;
                    }
                    sb.append( data[i][j] );
                } else {
                    sb.append(' ');
                    while( k < fieldWidth ){
                        sb.append('.');
                        ++ k ;
                    }
                }
                ++ j ;
            }
            sb.append(" \n");
            ++ i ;
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a string representation of a two-dimensional integer array
     * with a maximum value equal to the grid size.
     */

    public static String toString( int[][] data , int boxesAcross ){
        return toString( data , boxesAcross , data.length );
    }
}
