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

import java.text.DateFormat ;
import java.util.Date ;
import java.util.StringTokenizer ;

/**
 * SuDokuUtils contains utility functions that are called from several other classes.
 */

public class SuDokuUtils {

    /**
     * Controls whether integers greater than or equal to 10 are displayed 
     * as numbers or letters.
     */
    
    public final static int NUMERIC      = 0 ,
                            ALPHANUMERIC = 1 ;

    public final static String[] labels = { "Numeric from 1", "Alphanumeric from 0" };
    
    /**
     * The default display format for values greater than or equal to 10.
     */
    
    public static int defaultFormat = NUMERIC ;

    // Copy types
    
    public final static int PLAIN_TEXT          = 0 ,
                            LIBRARY_BOOK        = 1 ,
                            CELL_STATE          = 2 ,
                            NUMBER_STATE        = 3 ,
                            NEIGHBOUR_STATE     = 4 ,
                            LINEAR_SYSTEM_STATE = 5 ;
    
    public final static String[] copyTypes = { "Plain Text Puzzle", 
                                               "Library Book Puzzle" , 
                                               "Cell State" ,
                                               "Number State" ,
                                               "Neighbour State" ,
                                               "Linear System State" };
    
    public static int defaultCopyType = PLAIN_TEXT ;
    
    public final static String[] featuredGrades = {"Ungraded"};
                                    
    /**
     * Creates a string representation of a two-dimensional integer array.
     * @param maxDatum maximum permitted value in the data array
     * @param format format for numbers greater than or equal to 10
     */

    public static String toString( byte[][] data , int boxesAcross , int maxDatum , int format ){
        
        final int cellsInRow = data.length ,
                  boxesDown = data.length / boxesAcross ;
                  
        StringBuffer sb = new StringBuffer();
        
        int i , j , k , v ;
        int number = maxDatum , fieldWidth = 1 , numberWidth , boxWidth ;
        if( format == NUMERIC ){
            while( ( number /= 10 ) >= 1 ){
                ++ fieldWidth ;
            }
        }
        boxWidth = cellsInRow / boxesAcross *( fieldWidth + 1 ) + 2 ;
        i = 0 ;
        while( i < cellsInRow ){
            if( i > 0 && i % boxesAcross == 0 ){
                k = 0 ;
                while( k < ( fieldWidth + 1 )* cellsInRow + ( boxesAcross - 1 )* 2 ){
                    if( k % boxWidth == boxWidth - 1 ){
                        sb.append('+');
                    } else {
                        sb.append('-');
                    }
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
                    if( format == NUMERIC ){
                        while( ( number /= 10 ) >= 1 ){
                            ++ numberWidth ;
                        }
                    }
                    while( k < 1 + fieldWidth - numberWidth ){
                        sb.append(' ');
                        ++ k ;
                    }
                    switch( format ){
                        case NUMERIC:
                            sb.append( data[i][j] );
                            break;
                        case ALPHANUMERIC:
                            if( data[i][j] > 10 ){
                                sb.append( (char)( 'A' + data[i][j] - 11 ) );
                            } else {
                                sb.append( data[i][j] - 1 );
                            }
                            break;
                    }
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
     * Creates a string representation of a two-dimensional integer array.
     * @param maxDatum maximum permitted value in the data array
     */

    public static String toString( byte[][] data , int boxesAcross , int maxDatum ){
        return toString( data , boxesAcross , maxDatum , defaultFormat );
    }    

    /**
     * Creates a string representation of a two-dimensional integer array
     * with a maximum value equal to the grid size.
     */

    public static String toString( byte[][] data , int boxesAcross ){
        return toString( data , boxesAcross , data.length , defaultFormat );
    }
    
    /**
     * Creates a string representation of a two-dimensional string array.
     * @param maxLength length of the longest string in the array
     */

    public static String toString( String[][] data , int boxesAcross , int[] maxLength ){
        
        final int cellsInRow = data.length ,
                  boxesDown = data.length / boxesAcross ;
                  
        StringBuffer sb = new StringBuffer();
        
        int i , j , k , v , length ;
        i = 0 ;
        while( i < cellsInRow ){
            if( i > 0 && i % boxesAcross == 0 ){
                j = 0 ;
                while( j < cellsInRow ){
                    k = 0 ;
                    while( k < maxLength[j] + 2 ){
                        sb.append('-');
                        ++ k ;
                    }
                    if( ++ j < cellsInRow && j % boxesDown == 0 ){
                        sb.append("-+");
                    }
                }
                sb.append(" \n");
            }
            j = 0 ;
            while( j < cellsInRow ){
                k = 0 ;
                if( ( length = data[i][j].length() ) > 0 ){
                    while( k < 2 + maxLength[j] - length ){
                        sb.append(' ');
                        ++ k ;
                    }
                    sb.append( data[i][j] );
                } else {
                    sb.append("  ");
                    while( k < maxLength[j] ){
                        sb.append('.');
                        ++ k ;
                    }
                }
                if( ++ j < cellsInRow && j % boxesDown == 0 ){
                    sb.append(" |");
                }
            }
            sb.append(" \n");
            ++ i ;
        }
        
        return sb.toString();
    }
        
    /**
     * Populates a data array according to a string in the given format.
     */

    public static void populate( byte[][] data , String s , int format ){        
        StringTokenizer st = new StringTokenizer( s , " \t\n\r*|¦-+");
        String token ;
        int i , j ;
        char c ;
        i = 0 ;
        while( i < data.length ){
            j = 0 ;
            while( j < data[0].length ){
                token = st.nextToken();
                data[i][j++] = parse( token );
            }
            ++ i ;
        }
    }

    /**
     * Populates a data array according to a string in the default format.
     */

    public static void populate( byte[][] data , String s ){
        populate( data , s , defaultFormat );
    }        
    
    /**
     * Converts a string representation of a cell in the given format 
     * into a data value.
     */
    
    public static byte parse( String s , int format ){
        byte datum = 0 ;
        char c ;
        switch( format ){
        case NUMERIC:
            try {
                datum = Byte.parseByte( s );
            } catch( NumberFormatException e ) {
            }
            break;
        case ALPHANUMERIC:  
            if( s.length() == 1 ){
                c = s.charAt( 0 );
                if( c >= 'A' && c <= 'Z' ){
                    datum = (byte)( c - 'A' + 11 );
                    break ;
                } else if( c >= 'a' && c <= 'z' ){
                    datum = (byte)( c - 'a' + 11 );
                    break ;
                }
            }
            try {
                datum = (byte)( 1 + Byte.parseByte( s ) );
            } catch( NumberFormatException e ) {
            }
            break;
        }
        return datum ;
    }
    
    /**
     * Converts a string representation of a cell in the default format 
     * into a data value.
     */
    
    public static byte parse( String s ){
        return parse( s , defaultFormat );
    }
    
    /**
     * Writes out a single item of data in the specified format.
     */

    public static String toString( int datum , int format ){
        if( datum > 0 ){
            switch( format ){
            case NUMERIC:
                return Integer.toString( datum );
            case ALPHANUMERIC:
                if( datum > 10 ){
                    return Character.toString( (char)( 'A' + datum - 11 ) );
                } else {
                    return Integer.toString( datum - 1 );                        
                }
            }
        }
        return new String();
    }
    
    /**
     * Writes out a single item of data in the default format.
     */

    public static String toString( int datum ){
        return toString( datum , defaultFormat );
    }
    
    /**
     * Writes out the XML header for Pappocom library books.
     */

    public static String libraryBookHeader( String className ,
                                            int cellsInRow ,
                                            int boxesAcross ,
                                            String[] featuredGrades ){
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        sb.append("<sudoku-book>\n");
        sb.append("<note>Generated by ");
        sb.append( className );
        sb.append(" on ");
        sb.append( DateFormat.getDateTimeInstance().format( new Date() ) );
        sb.append(".</note>\n");
        sb.append("<user>0</user>\n");
        sb.append("<last>000000000000</last>\n");
        sb.append("<checked>000000000000</checked>\n");
        sb.append("<xtra>0</xtra>\n");            
        sb.append("<puzzle-type>");
        sb.append( cellsInRow == 9 && boxesAcross == 3 ? '0' : '1' );
        sb.append("</puzzle-type>\n");
        sb.append("<cells-in-row>");
        sb.append( cellsInRow );
        sb.append("</cells-in-row>\n");
        sb.append("<boxes-across>");
        sb.append( boxesAcross );
        sb.append("</boxes-across>\n");
        sb.append("<boxes-down>");
        sb.append( cellsInRow / boxesAcross );
        sb.append("</boxes-down>\n");
            
        int i = 0 ;
        while( i < featuredGrades.length ){
            sb.append("<featuredGrade>");
            sb.append( featuredGrades[i] );
            sb.append("</featuredGrade>\n");
            ++ i ;
        }
        
        return sb.toString();
    }
    
    /**
     * Writes out the XML header for Pappocom library books.
     */

    public static String libraryBookFooter() {
        return "</sudoku-book>\n" ;
    }
}
