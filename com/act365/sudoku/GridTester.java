/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com November 2004
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
 * The GridTester class tests the Su Doku solver with some graded examples
 * takes from The Times newspaper.
 * 
 * @see <a href="http://www.sudoku.com">www.sudoku.com</a>
 * @see <a href="http://www.timesonline.co.uk">The Times online</a>
 */

public class GridTester {

    /**
     * Prints a (possibly incomplete) grid to the screen. The solver will
     * attempt so complete the grid where possible.
     * 
     * @param testGrid - the (possibly incomplete) grid to be printed.
     */
    
    static void print( Grid testGrid ){
        
        int i , j ;
        
        if( testGrid.solve() ){
            i = 0 ;
            while( i < 9 ){
                j = 0 ;
                while( j < 9 ){
                    System.out.print( testGrid.data[i][j] + " " );
                    ++ j ;
                }
                System.out.println();
                ++ i ;
            }
        } else {
            System.out.println("No solution found");
        }        
    }
    
    /**
     * Solves standard Su Doku problems taken from The Times.
     */
    
	public static void main(String[] args) {
        
        Grid testGrid = new Grid();
        
        System.out.println("EASY");
        
        testGrid.data = new int[][]{ { 0 , 2 , 0 , 8 , 1 , 0 , 7 , 4 , 0 } ,
                                     { 7 , 0 , 0 , 0 , 0 , 3 , 1 , 0 , 0 } ,
                                     { 0 , 9 , 0 , 0 , 0 , 2 , 8 , 0 , 5 } ,
                                     { 0 , 0 , 9 , 0 , 4 , 0 , 0 , 8 , 7 } ,
                                     { 4 , 0 , 0 , 2 , 0 , 8 , 0 , 0 , 3 } ,
                                     { 1 , 6 , 0 , 0 , 3 , 0 , 2 , 0 , 0 } ,
                                     { 3 , 0 , 2 , 7 , 0 , 0 , 0 , 6 , 0 } ,
                                     { 0 , 0 , 5 , 6 , 0 , 0 , 0 , 0 , 8 } ,
                                     { 0 , 7 , 6 , 0 , 5 , 1 , 0 , 9 , 0 } };


        print( testGrid );
        
        System.out.println("VERY HARD");
        
        testGrid.data = new int[][]{ { 0 , 4 , 3 , 0 , 8 , 0 , 2 , 5 , 0 } ,
                                     { 6 , 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } ,
                                     { 0 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 4 } ,
                                     { 9 , 0 , 0 , 0 , 0 , 4 , 0 , 7 , 0 } ,
                                     { 0 , 0 , 0 , 6 , 0 , 8 , 0 , 0 , 0 } ,
                                     { 0 , 1 , 0 , 2 , 0 , 0 , 0 , 0 , 3 } ,
                                     { 8 , 2 , 0 , 5 , 0 , 0 , 0 , 0 , 0 } ,
                                     { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 , 5 } ,
                                     { 0 , 3 , 4 , 0 , 9 , 0 , 7 , 1 , 0  } };

        print( testGrid );
	}
}
