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

import java.awt.* ;
import java.awt.event.* ;

/**
 * A ControlContainer instance contains the various buttons and text
 * fields that sit underneath the Su Doku grid and control
 * its operation. 
 */

public class ControlContainer extends com.act365.awt.Container 
                              implements ActionListener {

	int boxesAcross ,
		boxesDown ;
    
    GridContainer grid ;
        
	TextField across ,
			  down ;
              
	Button solve ,
		   unsolve ,
		   reset ,
		   resize ;

    /**
     * Creates a new ControlContainer to control the given
     * GridContainer.
     */
    
    public ControlContainer( GridContainer grid ){
        
        // Set state variables.
        this.grid = grid ;
        boxesAcross = grid.getBoxesAcross();
        boxesDown = grid.getBoxesDown();
        
        // Add the Solve controls.
        solve = new Button("Solve");
        solve.addActionListener( this );
        unsolve = new Button("Unsolve");
        unsolve.addActionListener( this );
        reset = new Button("Reset");
        reset.addActionListener( this );
        
        // Add the Resize controls.
        resize = new Button("Resize");
        resize.addActionListener( this );
        across = new TextField(1);
        down = new TextField(1);
        
        // Lay the components out.
		addComponent( solve , 0 , 0 , 3 , 1 , 1 , 0 );
		addComponent( unsolve , 4 , 0 , 3 , 1 , 1 , 0 );
		addComponent( reset , 8 , 0 , 3 , 1 , 1 , 0 );
		addComponent( resize , 0 , 1 , 3 , 1 , 1 , 0 );
		addComponent( new Label("Across") , 4 , 1 , 2 , 1 , 1 , 0 );
		addComponent( across , 6 , 1 , 1 , 1 , 1 , 0 );
		addComponent( new Label("Down") , 8 , 1 , 2 , 1 , 1 , 0 );
		addComponent( down , 10 , 1 , 1 , 1 , 1 , 0 );	
		
		write();	
    }    

    /**
     * The ControlContainer looks best at 300x100.
     */
	public Dimension getBestSize() {
		return new Dimension( 300 , 100 );
	}

	/**
	 * Reacts to button presses. 
	 */
    
	public void actionPerformed( ActionEvent evt ){
        
		if( evt.getSource() == solve ){
			grid.solve();
		} else if( evt.getSource() == unsolve ) {
			grid.unsolve();
		} else if( evt.getSource() == reset ){
			grid.reset();
		} else if( evt.getSource() == resize ){
			read();
			grid.setBoxes( boxesAcross , boxesDown );
			write();
		}
	}
	
	/**
	 * Reads data from the GUI to the state variables.
	 */
	
	void read(){
		try {
			boxesAcross = Integer.parseInt( across.getText() );
		} catch ( NumberFormatException e ) {        	
		}

		try {
			boxesDown = Integer.parseInt( down.getText() );
		} catch ( NumberFormatException e ) {        	
		}		
	}
	
	/**
	 * Writes state variables to the GUI.
	 */
	
	void write(){
		across.setText( Integer.toString( boxesAcross ) );
		down.setText( Integer.toString( boxesDown ) );		
	}
}
