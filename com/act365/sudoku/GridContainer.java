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
import java.util.Date ;

/**
 * The GridContainer class displays a Su Doku grid.
 */

public class GridContainer extends com.act365.awt.Container {

    // Members
    
    Grid grid ;
    
    IStrategy strategy ;
    
    TextField[][] textFields ;
    
    double solveTime ;
    
    int hintX , 
        hintY ,
        hintValue ;
    
    Composer composer ;
    
    /**
     * Creates a new GridContainer instance. 
     */
    
    public GridContainer( Grid grid ) {
        this.grid = grid ;
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
        long now = new Date().getTime();
    	grid.solve( strategy , 1 );
        solveTime = ( new Date().getTime() - now )/ 1000. ;
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
    	int nSolns = grid.solve( strategy , 2 );
    	strategy.reset();
    	
    	return nSolns ;
    }
    
    /**
     * Solves the grid and returns the first step of the solution.
     * @return whether a solution could be found
     */
    
    public boolean hint(){
        read();
        if( grid.solve( strategy , 1 ) == 1 ){
            hintX = strategy.getThreadX( 0 );
            hintY = strategy.getThreadY( 0 );
            hintValue = grid.data[hintX][hintY];
            strategy.reset();
            return true ;
        }
        return false ;
    }
    
    /**
     * Unsolves the grid (reverts its state to that prior to the 
     * most recent solve).
     */
    
    public void unsolve(){
    	strategy.reset();
    	write();
    }
    
    /**
     * Resets the grid.
     */
    
    public void reset(){
    	grid.reset();
        solveTime = 0 ;
    	write();
    }
    
    /**
     * Shuffles the grid.
     */
    
    public void shuffle(){
        grid.shuffle();
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
                                     grid.cellsInRow >= 12 );
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
     * Returns the time (in milliseconds) taken to complete the most recent solve.
     */
    
    public double getSolveTime(){
        return solveTime ;
    }
    
    /**
     * Returns hint x-coordinate in the range [1,cellsInRow]. 
     */
    
    public int getHintX(){
        return hintX + 1 ;
    }
    
    /**
     * Returns hint y-coordinate in the range [1,cellsInRow]. 
     */
    
    public int getHintY(){
        return hintY + 1 ;
    }
    
    /**
     * Returns hint value in the range [1,cellsInRow]. 
     */
    
    public int getHintValue(){
        return hintValue ;
    }
    
    /**
     * Sets the strategy to be used to solve the grid.
     * @see Strategy
     * @param strategyCode - strategy code as defined in Strategy class
     * 
     */
    
    public void setStrategy( int strategyCode ){
        strategy = Strategy.create( strategyCode );
    }

    /**
     * Returns the currently selected stratey object.
     */
    
    public IStrategy getStrategy(){
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
    
    /**
     * A GridContainer returns a string representation of the 
     * underlying grid.
     */
    
    public String toString(){
        return grid.toString();
    }
}
