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

    // Members
    
    Grid grid ;
    
    LeastCandidatesHybrid strategy ;
    
    TextField[][] textFields ;
    
    Composer composer ;
    
    /**
     * Creates a new GridContainer instance. 
     */
    
    public GridContainer( Grid grid ) {
        this.grid = grid ;
        strategy = (LeastCandidatesHybrid) Strategy.create( Strategy.LEAST_CANDIDATES_HYBRID_II );
        removeAll();
        layoutComponents();
        validate();
        write();
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
    	grid.solve( strategy , 1 );
    	write();  
    }

    /**
     * Evaluates the complexity of the grid. Establishes that just a single
     * solution exists and, if so, calculates the number of thread unwinds
     * used to solve the problem.
     * @return number of solutions 
     */
        
    public int evaluate(){
    	read();
        IStrategy strategy = Strategy.create( Strategy.LEAST_CANDIDATES_HYBRID_II );
    	int nSolns = grid.solve( strategy , 2 );
    	strategy.reset();
    	
    	return nSolns ;
    }
    
    /**
     * Unsolves the grid (reverts its state to that prior to the 
     * most recent solve).
     */
    
    public void unsolve(){
        unsolve( 0 );
    }
    
    /**
     * Resets the grid to the partial solution prior to the given move.
     */
    
    public void unsolve( int move ){
    	strategy.reset( move );
    	write();
    }
    
    /**
     * Resets the grid.
     */
    
    public void reset(){
    	grid.reset();
        try {
            strategy.setup( grid );
        } catch ( Exception e ) {
        }
    	write();
    }
    
    /**
     * Sets the underlying grid to be a clone of the given grid.   
     * @param grid new grid
     */

    public synchronized void setGrid( Grid grid ){
        final boolean redraw = this.grid.boxesAcross != grid.boxesAcross || 
                               this.grid.boxesDown != grid.boxesDown ;
        this.grid = (Grid) grid.clone();
        if( redraw ){
            removeAll();
            layoutComponents();
            validate();
        }
        write();
    }

    /**
     * Resizes the grid.
     * @param boxesAcross - number of boxes across one row of the Su Doku grid
     * @param boxesDown - number of boxes down one column of the Su Doku grid
     */
    
    public void setBoxes( int boxesAcross ,
                          int boxesDown ){
        final boolean redraw = boxesAcross != grid.boxesAcross || boxesDown != grid.boxesDown ;
        grid.resize( boxesAcross , boxesDown );
        if( redraw ){
            removeAll();
            layoutComponents();
            validate();
        }
    }
    
    /**
     * Pastes data onto the grid.
     * @param s data to be pasted, which should be in the form created by Copy
     */
    
    public void paste( String s ){
        int oldBoxesAcross = grid.boxesAcross ,
            oldBoxesDown = grid.boxesDown ;            
        grid.populate( s );
        if( grid.boxesAcross != oldBoxesAcross || grid.boxesDown != oldBoxesDown ){
            removeAll();
            layoutComponents();
            validate();                
        }
        write();
    }
    
    /**
     * Composes a puzzle, with rotational symmetry and a unique solution,
     * based upon the initial values in the grid.
     * @param filledCells - number of filled cells to appear in the puzzle
     */
    
    public synchronized void startComposer( int filledCells ){
        read();
        try {
            composer = new Composer( this , 
                                     grid.boxesAcross , 
                                     1 , 
                                     0 ,
                                     50 ,
                                     Integer.MAX_VALUE ,
                                     new MaskFactory( grid.cellsInRow , filledCells , grid.boxesAcross ) ,
                                     Composer.defaultThreads , 
                                     0 , 
                                     null ,
                                     false ,
                                     grid.cellsInRow >= 12 ,
                                     0 ,
                                     0 ,
                                     0 ,
                                     0 ,
                                     0 ,
                                     true ,
                                     false );
            composer.start();
        } catch ( Exception e ) {
        }
    }

    /**
     * Interrupts any ComposerThread that might have been started 
     * by startComposer().
     */
    
    public void stopComposer(){
        if( composer instanceof Composer ){
            composer.interrupt();
        }
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
     * Returns the number of unwinds used to solve the grid
     * as a measure of its complexity.
     */
    
    public int getComplexity(){
    	return grid.complexity ;
    }
    
    /**
     * Returns the current strategy.
     */
    
    public LeastCandidatesHybrid getStrategy(){
        return strategy ;    
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
                textFields[r][c].setText( SuDokuUtils.toString( grid.data[r][c] ) );   
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
        r = 0 ;
        while( r < grid.cellsInRow ){
            c = 0 ;
            while( c < grid.cellsInRow ){ 
                grid.data[r][c] = SuDokuUtils.parse( textFields[r][c].getText() );      
                ++ c ;
            }
            ++ r ;
        }
    }
    
    /**
     * A GridContainer returns a string representation of the 
     * underlying grid.
     */
    
    public String toString(){
        return grid.toString();
    }
}
