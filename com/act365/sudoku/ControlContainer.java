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
import java.awt.datatransfer.* ;
import java.awt.event.* ;
import java.text.DecimalFormat ;

/**
 * A ControlContainer instance contains the various buttons and text
 * fields that sit underneath the Su Doku grid and control
 * its operation. 
 */

public class ControlContainer extends com.act365.awt.Container 
                              implements ActionListener ,
                                         ItemListener ,
                                         ClipboardOwner {
    //
    
    final static int defaultStrategy = Strategy.LEAST_CANDIDATES_HYBRID_II ,
                     defaultMinFilledCellsValue = 32 ;
     
    //
    
	int boxesAcross ,
		boxesDown ,
		minFilledCellsValue ;
    
    GridContainer grid ;
        
	TextField across ,
			  down ,
			  minFilledCells ;
              
	Button solve ,
		   unsolve ,
		   reset ,
           hint ,
		   resize ,
		   evaluate ,
		   compose ,
           interrupt ,
           copy ,
           paste ,
           shuffle ;

    Label solns ,
          complexity ,
          time ,
          cell ,
          value ;
          
    Choice strategy ;
    
    /**
     * Creates a new ControlContainer to control the given
     * GridContainer.
     */
    
    public ControlContainer( GridContainer grid ){
        
        // Set state variables.
        this.grid = grid ;
        boxesAcross = grid.getBoxesAcross();
        boxesDown = grid.getBoxesDown();
        minFilledCellsValue = defaultMinFilledCellsValue ;
            
        // Add the Solve controls.
        solve = new Button("Solve");
        solve.addActionListener( this );
        unsolve = new Button("Unsolve");
        unsolve.addActionListener( this );
        reset = new Button("Reset");
        reset.addActionListener( this );
    
        // Add the Copy/Paste controls.
        copy = new Button("Copy");
        copy.addActionListener( this );
        paste = new Button("Paste");
        paste.addActionListener( this );
        shuffle = new Button("Shuffle");
        shuffle.addActionListener( this );
        
        // Add the Hint controls.
        hint = new Button("Hint");
        hint.addActionListener( this );
        cell = new Label();
        value = new Label();
        
        // Add the Strategy controls.
        strategy = new Choice();
        int i = 0 ;
        while( i < Strategy.strategyNames.length ){
            strategy.add( Strategy.strategyNames[i] ); 
            ++ i ;   
        }
        strategy.select( defaultStrategy );
        grid.setStrategy( defaultStrategy );
        strategy.addItemListener( this );
        time = new Label("0.0s");
            
        // Add the Resize controls.
        resize = new Button("Resize");
        resize.addActionListener( this );
        across = new TextField(1);
        down = new TextField(1);
        
        // Add the Evaluate controls
        evaluate = new Button("Evaluate");
        evaluate.addActionListener( this );
        solns = new Label();
        complexity = new Label();
            
        // Add the Compose controls
        compose = new Button("Compose");
        compose.addActionListener( this );
        minFilledCells = new TextField( 2 );
        interrupt = new Button("Break");
        interrupt.addActionListener( this );
                
        // Lay out the components.
		addComponent( solve , 0 , 0 , 3 , 1 , 1 , 0 );
		addComponent( unsolve , 4 , 0 , 3 , 1 , 1 , 0 );
		addComponent( reset , 8 , 0 , 3 , 1 , 1 , 0 );

        addComponent( copy , 0 , 1 , 3 , 1 , 1 , 0 );
        addComponent( paste , 4 , 1 , 3 , 1 , 1 , 0 );
        addComponent( shuffle , 8 , 1 , 3 , 1 , 1 , 0 );
        
        addComponent( hint , 0 , 2 , 3 , 1 , 1 , 0 );
        addComponent( new Label("Cell") , 4 , 2 , 2 , 1 , 1 , 0 );
        addComponent( cell , 6 , 2 , 1 , 1 , 1 , 0 );
        addComponent( new Label("Value") , 8 , 2 , 2 , 1 , 1 , 0 );
        addComponent( value , 10 , 2 , 1 , 1 , 1 , 0 );
        
        addComponent( new Label("Strategy") , 0 , 3 , 2 , 1 , 1 , 0 );
        addComponent( strategy , 4 , 3 , 3 , 1 , 1 , 0 );
        addComponent( new Label("Time") , 8 , 3 , 2 , 1 , 1 , 0 );
        addComponent( time , 10 , 3 , 1 , 1 , 1 , 0 );
        
		addComponent( resize , 0 , 4 , 3 , 1 , 1 , 0 );
		addComponent( new Label("Across") , 4 , 4 , 2 , 1 , 1 , 0 );
		addComponent( across , 6 , 4 , 1 , 1 , 1 , 0 );
		addComponent( new Label("Down") , 8 , 4 , 2 , 1 , 1 , 0 );
		addComponent( down , 10 , 4 , 1 , 1 , 1 , 0 );	

		addComponent( evaluate , 0 , 5 , 3 , 1 , 1 , 0 );
		addComponent( new Label("Solutions") , 4 , 5 , 2 , 1 , 1 , 0 );
		addComponent( solns , 6 , 5 , 1 , 1 , 1 , 0 );
		addComponent( new Label("Complexity") , 8 , 5 , 2 , 1 , 1 , 0 );
		addComponent( complexity , 10 , 5 , 1 , 1 , 1 , 0 );
		
        addComponent( compose , 0 , 6 , 3 , 1 , 1 , 0 );
        addComponent( new Label("Filled Cells") , 4 , 6 , 2 , 1 , 1 , 0 );
        addComponent( minFilledCells , 6 , 6 , 1 , 1 , 1 , 0 );
        addComponent( interrupt , 8 , 6 , 3 , 1 , 1 , 0 );
          
		write();	
    }    

    /**
     * The ControlContainer looks best at 300x300.
     */
	public Dimension getBestSize() {
		return new Dimension( 300 , 300 );
	}

	/**
	 * Reacts to button presses. 
	 */
    
	public void actionPerformed( ActionEvent evt ){
        
		if( evt.getSource() == solve ){
			grid.solve();
            time.setText( new DecimalFormat("#0.000").format( grid.getSolveTime() )+ "s");
		} else if( evt.getSource() == unsolve ) {
			grid.unsolve();
		} else if( evt.getSource() == reset ){
			grid.reset();
            time.setText("0.0s");
        } else if( evt.getSource() == copy ) {
            getToolkit().getSystemClipboard().setContents( new StringSelection( grid.toString() ) , this );
        } else if( evt.getSource() == paste ){
            Transferable transferable = getToolkit().getSystemClipboard().getContents( this );
            if( transferable != null ){
                try {
                    grid.paste( (String) transferable.getTransferData( DataFlavor.stringFlavor ) );
                    boxesAcross = grid.getBoxesAcross();
                    boxesDown = grid.getBoxesDown();
                    write();
                } catch ( Exception e ) { 
                    grid.reset();
                }
            }
        } else if( evt.getSource() == shuffle ){
            grid.shuffle();
		} else if( evt.getSource() == resize ){
			read();
			grid.setBoxes( boxesAcross , boxesDown );
			write();
		} else if( evt.getSource() == evaluate ){
			switch( grid.evaluate() ){
				case 0 :
				    solns.setText("None");
				    complexity.setText("");
				    break;
				case 1 :
				    solns.setText("Unique");
				    complexity.setText( Integer.toString( grid.getComplexity() ) );
				    break;
				case 2 :
				    solns.setText("Multiple");
				    complexity.setText("");
				    break;
				default:
				    solns.setText("Error");
				    complexity.setText("");
				    break;	    
			}
		} else if( evt.getSource() == compose ) {
			read();
			grid.startComposer( minFilledCellsValue );
			write();
        } else if( evt.getSource() == interrupt ) {
            grid.stopComposer();
		} else if( evt.getSource() == hint ){
            read();
            if( grid.hint() ){
                cell.setText( "(" + grid.getHintX() + "," + grid.getHintY() + ")" );
                value.setText( Integer.toString( grid.getHintValue() ) );
            }
		}
	}
	
    /**
     * Reacts to changes to the strategy list.
     */

    public void itemStateChanged( ItemEvent evt ){
        if( evt.getSource() == strategy ){
            grid.setStrategy( strategy.getSelectedIndex() );
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
		try {
			minFilledCellsValue = Integer.parseInt( minFilledCells.getText() );
		} catch ( NumberFormatException e ) {        	
		}		
	}
	
	/**
	 * Writes state variables to the GUI.
	 */
	
	void write(){
		across.setText( Integer.toString( boxesAcross ) );
		down.setText( Integer.toString( boxesDown ) );
		minFilledCells.setText( Integer.toString( minFilledCellsValue ));
	}
    
    /**
     * Nothing happens if ownership of the clipboard contents is lost.
     */
    
    public void lostOwnership( Clipboard clipboard , Transferable transferable ){
    }
}
