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

import com.act365.awt.Frame;

/**
 * The SuDoku app displays a Su Doku solver in a new window.
 */

public class SuDoku extends Frame {

    SuDoku( int boxesAcross , int boxesDown ){
        super("Su Doku Solver");        
        GridContainer grid = new GridContainer( new Grid( boxesAcross , boxesDown ) );
        ControlContainer control = new ControlContainer( grid ); 
        SuDokuContainer suDoku = new SuDokuContainer( grid , control );
        add( suDoku );
        setSize( suDoku.getBestSize() );
    }
 
    /**
     * Starts a new app with a Su Doku grid of the given size.
     * <code>java com.act365.sudoku.SuDoku [-a boxesAcross] [-d boxesDown]</code>
     * <br><code>boxesAcross</code> is the number of boxes to appear across one row of the Su Doku grid - the default is 3.
     * <br><code>boxesDown</code> is the number of boxes to appear down one column of the Su Doku grid - the default is 3.
     */
    
	public static void main(String[] args) {
		
		int boxesAcross = 3 ,
		    boxesDown = 3 ;
		
		int i = 0 ;
		
		while( i < args.length ){
			if( args[i].equals("-a") ){
				++ i ;
				if( i < args.length ){
					try {
						boxesAcross = Integer.parseInt( args[i] );
					} catch ( NumberFormatException e ) {
						System.err.println("boxesAcross should be an integer");
					}
				} else {
					System.err.println("-a requires an argument");
				}
			} else if( args[i].equals("-d") ){
				++ i ;
				if( i < args.length ){
					try {
						boxesDown = Integer.parseInt( args[i] );
					} catch ( NumberFormatException e ) {
						System.err.println("boxesDown should be an integer");
					}
				} else {
					System.err.println("-d requires an argument");
				}
			} else {
				System.err.println("Usage: SuDoku [-a boxesAcross] [-d boxesDown]");
			}
			++ i ;
		}
		
        new SuDoku( boxesAcross , boxesDown ).show();
	}
}
