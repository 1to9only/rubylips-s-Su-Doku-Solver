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
import java.util.StringTokenizer ;

/**
 * A ControlContainer instance contains the various buttons and text
 * fields that sit underneath the Su Doku grid and control
 * its operation. 
 */

public class ControlContainer extends com.act365.awt.Container 
                              implements ActionListener ,
                                         ItemListener ,
                                         MouseListener ,
                                         ClipboardOwner {

    final static int defaultMinFilledCellsValue = 32 ;
     
	int boxesAcross ,
		boxesDown ,
		minFilledCellsValue ;
    
    boolean isApplet ;
    
    GridContainer grid ;
        
	TextField across ,
			  down ,
			  minFilledCells ;

    TextArea reasoningArea ;
                  
	Button solve ,
		   unsolve ,
		   reset ,
		   resize ,
		   evaluate ,
		   compose ,
           interrupt ,
           copy ,
           paste ,
           shuffle ;

    Label solns ,
          complexity ,
          singleSectorCandidatesEliminations ,
          disjointSubsetsEliminations ,
          xWingsEliminations ,
          swordfishEliminations ,
          nishioEliminations ,
          nGuesses ;
          
    Checkbox singleSectorCandidates ,
             disjointSubsets ,
             xWings ,
             swordfish ,
             nishio ,
             guess ;
             
    SuDokuClipboard clipboard ;
    
    /**
     * Creates a new ControlContainer to control the given
     * GridContainer.
     */
    
    public ControlContainer( GridContainer grid , boolean isApplet ){
        
        // Set state variables.
        this.grid = grid ;
        if( ( this.isApplet = isApplet ) ){
            clipboard = new SuDokuClipboard();    
        }
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
        
        // Add the Evaluate controls
        evaluate = new Button("Evaluate");
        evaluate.addActionListener( this );
        solns = new Label();
        complexity = new Label();
            
        // Add the Resize controls.
        resize = new Button("Resize");
        resize.addActionListener( this );
        across = new TextField(1);
        down = new TextField(1);
        
        // Add the Compose controls
        compose = new Button("Compose");
        compose.addActionListener( this );
        minFilledCells = new TextField( 2 );
        interrupt = new Button("Break");
        interrupt.addActionListener( this );
    
        // Add the Strategy controls.
        singleSectorCandidates = new Checkbox("Single Sector Candidates" , grid.getStrategy().useSingleSectorCandidates );
        singleSectorCandidates.addItemListener( this );
        singleSectorCandidatesEliminations = new Label("0");
        disjointSubsets = new Checkbox("Disjoint Subsets" , grid.getStrategy().useDisjointSubsets );
        disjointSubsets.addItemListener( this );
        disjointSubsetsEliminations = new Label("0");
        xWings = new Checkbox("X-Wings" , grid.getStrategy().useXWings );
        xWings.addItemListener( this );
        xWingsEliminations = new Label("0");
        swordfish = new Checkbox("Swordfish" , grid.getStrategy().useSwordfish );
        swordfish.addItemListener( this );
        swordfishEliminations = new Label("0");
        nishio = new Checkbox("Nishio" , grid.getStrategy().useNishio );
        nishio.addItemListener( this );
        nishioEliminations = new Label("0");
        guess = new Checkbox("Guess" , grid.getStrategy().useGuesses );
        guess.addItemListener( this );
        nGuesses = new Label("0");
        
        // Add the Reasoning area
        reasoningArea = new TextArea();
        reasoningArea.setEditable( false );
        reasoningArea.addMouseListener( this );
                    
        // Lay out the components.
		addComponent( solve , 0 , 0 , 3 , 1 , 1 , 0 );
		addComponent( unsolve , 4 , 0 , 3 , 1 , 1 , 0 );
		addComponent( reset , 8 , 0 , 3 , 1 , 1 , 0 );

        addComponent( evaluate , 0 , 1 , 3 , 1 , 1 , 0 );
        addComponent( new Label("Solutions") , 4 , 1 , 2 , 1 , 1 , 0 );
        addComponent( solns , 6 , 1 , 1 , 1 , 1 , 0 );
        addComponent( new Label("Complexity") , 8 , 1 , 2 , 1 , 1 , 0 );
        addComponent( complexity , 10 , 1 , 1 , 1 , 1 , 0 );
        
        addComponent( copy , 0 , 2 , 3 , 1 , 1 , 0 );
        addComponent( paste , 4 , 2 , 3 , 1 , 1 , 0 );
        addComponent( shuffle , 8 , 2 , 3 , 1 , 1 , 0 );
        
		addComponent( resize , 0 , 3 , 3 , 1 , 1 , 0 );
		addComponent( new Label("Across") , 4 , 3 , 2 , 1 , 1 , 0 );
		addComponent( across , 6 , 3 , 1 , 1 , 1 , 0 );
		addComponent( new Label("Down") , 8 , 3 , 2 , 1 , 1 , 0 );
		addComponent( down , 10 , 3 , 1 , 1 , 1 , 0 );	

        addComponent( compose , 0 , 4 , 3 , 1 , 1 , 0 );
        addComponent( new Label("Filled Cells") , 4 , 4 , 2 , 1 , 1 , 0 );
        addComponent( minFilledCells , 6 , 4 , 1 , 1 , 1 , 0 );
        addComponent( interrupt , 8 , 4 , 3 , 1 , 1 , 0 );
        
        addComponent( new Label("Pattern:") , 0 , 5 , 2 , 1 , 1 , 0 );
        addComponent( singleSectorCandidates , 4 , 5 , 3 , 1 , 1 , 0 );
        addComponent( singleSectorCandidatesEliminations , 10 , 5 , 1 , 1 , 1 , 0 );
        addComponent( disjointSubsets , 4 , 6 , 3 , 1 , 1 , 0 );
        addComponent( disjointSubsetsEliminations , 10 , 6 , 1 , 1 , 1 , 0 );
        addComponent( xWings , 4 , 7 , 3 , 1 , 1 , 0 );
        addComponent( xWingsEliminations , 10 , 7 , 1 , 1 , 1 , 0 );
        addComponent( swordfish , 4 , 8 , 3 , 1 , 1 , 0 );
        addComponent( swordfishEliminations , 10 , 8 , 1 , 1 , 1 , 0 );
        addComponent( nishio , 4 , 9 , 3 , 1 , 1 , 0 );
        addComponent( nishioEliminations , 10 , 9 , 1 , 1 , 1 , 0 );
        
        addComponent( guess , 4 , 10 , 3 , 1 , 1 , 0 );
        addComponent( nGuesses , 10 , 10 , 1 , 1 , 1 , 0 );
                
        addComponent( reasoningArea , 0 , 11 , 11 , 5 , 1 , 1 );
          
		write();	
    }    

    /**
     * The ControlContainer looks best at 300x500.
     */
	
    public Dimension getBestSize() {
		return new Dimension( 300 , 500 );
	}

	/**
	 * Reacts to button presses. 
	 */
    
	public void actionPerformed( ActionEvent evt ){
        
		if( evt.getSource() == solve ){
			grid.solve();
            if( grid.getStrategy().explainsReasoning() ){
                reasoningArea.setText( null );
                reasoningArea.append("START\n"); 
                int i = 0 ;
                while( i < grid.getStrategy().getThreadLength() ){
                    reasoningArea.append( ( 1 + i ) + ". " + grid.getStrategy().getReason( i ) );
                    ++ i ;
                }
            }
            write();
		} else if( evt.getSource() == unsolve ) {
			grid.unsolve();
		} else if( evt.getSource() == reset ){
			grid.reset();
            reasoningArea.setText( null );
            write();
        } else if( evt.getSource() == copy ) {
            if( isApplet ){
                clipboard.show();
                clipboard.setText( grid.toString() );
            } else {
                getToolkit().getSystemClipboard().setContents( new StringSelection( grid.toString() ) , this );
            }
        } else if( evt.getSource() == paste ){
            String pasteText = null ;
            if( isApplet ){
                if( clipboard instanceof SuDokuClipboard ){
                    pasteText = clipboard.getText();
                }
            } else {
                Transferable transferable = getToolkit().getSystemClipboard().getContents( this );
                if( transferable instanceof Transferable ){
                    try {
                        pasteText = (String) transferable.getTransferData( DataFlavor.stringFlavor );
                    } catch( Exception e ) {
                    }
                }
            }
            if( pasteText instanceof String ){
                try {
                    grid.paste( pasteText );
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
		}
	}
	
    /**
     * 
     * When a mouse is clicked in the Reasoning Area, the puzzle will reset itself 
     * to the appropriate partial-solution. 
     */
    
    public void mouseClicked( MouseEvent evt ){
        int lastMove = 0 ,
            fullStopIndex ;
        String reasonText = reasoningArea.getText().substring( 0 , reasoningArea.getCaretPosition() ) ,
               reason = "";
        StringTokenizer st = new StringTokenizer( reasonText , "\n" );
        while( st.hasMoreTokens() ){
            try {
                reason = st.nextToken();
                if( ( fullStopIndex = reason.indexOf('.') ) >= 0 ){
                    lastMove = Integer.parseInt( reason.substring( 0 , fullStopIndex ) );
                }
            } catch( NumberFormatException e ) {
            }
        }
        grid.unsolve( lastMove );
    }
    
    /**
     * Mouse motion is ignored.
     */
    
    public void mouseEntered( MouseEvent evt ){
    }
    
    /**
     * Mouse motion is ignored.
     */
    
    public void mouseExited( MouseEvent evt ){
    }
    
    /**
     * Mouse presses are ignored.
     */
    
    public void mousePressed( MouseEvent evt ){
    }

    /**
     * Mouse releases are ignored.
     */    
    
    public void mouseReleased( MouseEvent evt ){
    }
    
    /**
     * Reacts to changes to the strategy list.
     */

    public void itemStateChanged( ItemEvent evt ){
        if( evt.getSource() == singleSectorCandidates ){
            grid.getStrategy().useSingleSectorCandidates = singleSectorCandidates.getState();
        } else if( evt.getSource() == disjointSubsets ){
            grid.getStrategy().useDisjointSubsets = disjointSubsets.getState();
        } else if( evt.getSource() == xWings ){
            grid.getStrategy().useXWings = xWings.getState();
        } else if( evt.getSource() == swordfish ){
            grid.getStrategy().useSwordfish = swordfish.getState();
        } else if( evt.getSource() == nishio ){
            grid.getStrategy().useNishio = nishio.getState();
        } else if( evt.getSource() == guess ) {
            grid.getStrategy().useGuesses = guess.getState();
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
        singleSectorCandidatesEliminations.setText( Integer.toString( grid.getStrategy().singleSectorCandidatesEliminations ) );
        disjointSubsetsEliminations.setText( Integer.toString( grid.getStrategy().disjointSubsetsEliminations ) );
        xWingsEliminations.setText( Integer.toString( grid.getStrategy().xWingsEliminations ) );
        swordfishEliminations.setText( Integer.toString( grid.getStrategy().swordfishEliminations ) );
        nishioEliminations.setText( Integer.toString( grid.getStrategy().nishioEliminations ) );
        nGuesses.setText( Integer.toString( grid.getStrategy().nGuesses ) );
	}
    
    /**
     * Nothing happens if ownership of the clipboard contents is lost.
     */
    
    public void lostOwnership( Clipboard clipboard , Transferable transferable ){
    }
}
