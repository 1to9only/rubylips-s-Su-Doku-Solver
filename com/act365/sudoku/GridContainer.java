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

import java.awt.*;

/**
 * The GridContainer class displays a Su Doku grid.
 */

public class GridContainer extends com.act365.awt.Container {

    Grid grid ;
    
    TextField[][] textFields ;
    
    /**
     * Creates a new GridContainer instance. 
     */
    
    public GridContainer( Grid grid ) {
        this.grid = grid ;
        layoutComponents();    
    }
    
    /**
     * Lays out the grid of text fields.
     */
    
    void layoutComponents(){
		
		textFields = new TextField[grid.cellsInRow][grid.cellsInRow];

		int r , c ;
        
		r = 0 ;
		while( r < grid.cellsInRow + grid.boxesDown - 1 ){
			c = 0 ;
			while( c < grid.cellsInRow + grid.boxesAcross - 1 ){
				if( r % (grid.boxesAcross+1) < grid.boxesAcross && c % (grid.boxesDown+1) < grid.boxesDown ){
					textFields[r/(grid.boxesAcross+1)*grid.boxesAcross+r%(grid.boxesAcross+1)][c/(grid.boxesDown+1)*grid.boxesDown+c%(grid.boxesDown+1)] = new TextField(1);
					addComponent( textFields[r/(grid.boxesAcross+1)*grid.boxesAcross+r%(grid.boxesAcross+1)][c/(grid.boxesDown+1)*grid.boxesDown+c%(grid.boxesDown+1)] , c , r , 1 , 1 , 1 , 1 );
				} else {
					addComponent( new Label() , c , r , 1 , 1 , 1 , 1 );
				}
				++ c ;
			}
			++ r ;
		}
    }
    
    /**
     * A GridContainer should display each square as 40x40 pixels.
     */
    
    public Dimension getBestSize() {
        return new Dimension( 40 *( grid.cellsInRow + grid.boxesDown - 1 ) , 
                              40 *( grid.cellsInRow + grid.boxesAcross - 1 ) );   
    }

    /**
     * Solves the grid.
     */
    
    public void solve(){  
    	read();
    	grid.solve();
    	write();  
    }
    
    /**
     * Unsolves the grid (i.e. reverts its state to that prior to the 
     * most recent solve.
     */
    
    public void unsolve(){
    	grid.unsolve();
    	write();
    }
    
    /**
     * Resets the grid.
     */
    
    public void reset(){
    	grid.reset();
    	write();
    }
    
    /**
     * Resizes the grid.
     * @param boxesAcross - number of boxes across one row of the Su Doku grid
     * @param boxesDown - number of boxes down one column of the Su Doku grid
     */
    
    public void setBoxes( int boxesAcross ,
                          int boxesDown ){
        grid.resize( boxesAcross , boxesDown );
        removeAll();
        layoutComponents();
        validate();
    }
    
    /** 
     * Returns number of boxes across one row of the Su Doku grid.
     */
    
    public int getBoxesAcross(){
    	return grid.boxesAcross ;
    }
    
    /**
     * Returns number of boxes down one column of the Su Doku grid.
     */
    
    public int getBoxesDown(){
    	return grid.boxesDown ;
    }
    
    /**
     * Fills the text fields with values from the grid.
     */
    
    void write(){        
        
        int r , c ;
        
        c = 0 ;
        while( c < grid.cellsInRow ){
            r = 0 ;
            while( r < grid.cellsInRow ){ 
                if( grid.data[r][c] > 0 ){
                    textFields[r][c].setText( Integer.toString( grid.data[r][c] ) );   
                } else {
                    textFields[r][c].setText("");   
                }
                ++ r ;
            }
            ++ c ;
        }
    }
    
    /**
     * Reads the values in the text fields and populates the underlying grid.
     */
    
    void read(){
        
        int r , c ;
        
        c = 0 ;
        while( c < grid.cellsInRow ){
            r = 0 ;
            while( r < grid.cellsInRow ){       
                try {
                    grid.data[r][c] = Integer.parseInt( textFields[r][c].getText() );
                } catch ( NumberFormatException e ) {
                    grid.data[r][c] = 0 ;   
                }
                if( grid.data[r][c] < 0 || grid.data[r][c] > grid.cellsInRow ){
                    grid.data[r][c] = 0 ;   
                }
                ++ r ;
            }
            ++ c ;
        }
    }
}
