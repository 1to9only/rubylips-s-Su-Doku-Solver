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
import java.awt.event.*;

/**
 * The GridContainer class displays a Su Doku grid.
 */

public class GridContainer extends com.act365.awt.Container 
                           implements ActionListener {

    TextField[][] textFields ;
    
    Button solve ,
           unsolve ,
           reset ;

    Grid grid ;
    
    /**
     * Creates a new GridContainer instance. 
     */
    
    public GridContainer() {
    
        int r , c ;
        
        // Set up the underlying grid.
        grid = new Grid();
        
        // Set up the grid of text fields.       
        textFields = new TextField[9][9];
        
        r = 0 ;
        while( r < 11 ){
            c = 0 ;
            while( c < 11 ){
                if( r % 4 < 3 && c % 4 < 3 ){
                    textFields[r/4*3+r%4][c/4*3+c%4] = new TextField(1);
                    addComponent( textFields[r/4*3+r%4][c/4*3+c%4] , c , r , 1 , 1 , 1 , 1 );
                } else {
                    addComponent( new Label() , c , r , 1 , 1 , 1 , 1 );
                }
                ++ c ;
            }
            ++ r ;
        }
        
        // Add the buttons.
        solve = new Button("Solve");
        solve.addActionListener( this );
        addComponent( solve , 0 , 11 , 3 , 1 , 1 , 0 );
        unsolve = new Button("Unsolve");
        unsolve.addActionListener( this );
        addComponent( unsolve , 4 , 11 , 3 , 1 , 1 , 0 );
        reset = new Button("Reset");
        reset.addActionListener( this );
        addComponent( reset , 8 , 11 , 3 , 1 , 1 , 0 );
    }
    
    /**
     * A GridContainer should be displayed at 500x400. 
     */
    
    public Dimension getBestSize() {
        return new Dimension( 500 , 400 );   
    }
    
    /**
     * Reacts to button presses. 
     */
    
    public void actionPerformed( ActionEvent evt ){
        
        if( evt.getSource() == solve ){
            read();
            if( grid.solve() ){
                paint();
            }
        } else if( evt.getSource() == unsolve ) {
            grid.unsolve();
            paint();
        } else if( evt.getSource() == reset ){
            grid.reset();
            paint();
        }
    }

    /**
     * Fills the text fields with values from the grid.
     */
    
    void paint(){        
        
        int r , c ;
        
        c = 0 ;
        while( c < 9 ){
            r = 0 ;
            while( r < 9 ){ 
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
        while( c < 9 ){
            r = 0 ;
            while( r < 9 ){       
                try {
                    grid.data[r][c] = Integer.parseInt( textFields[r][c].getText() );
                } catch ( NumberFormatException e ) {
                    grid.data[r][c] = 0 ;   
                }
                if( grid.data[r][c] < 0 || grid.data[r][c] > 9 ){
                    grid.data[r][c] = 0 ;   
                }
                ++ r ;
            }
            ++ c ;
        }
    }
}
