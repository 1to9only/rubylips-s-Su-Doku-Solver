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
 * Defines the functions that a strategy will have to implement if it is to be
 * used by the Ariadne's Thread solver in order to complete Su Doku problems. 
 */

public interface IStrategy {
	
	/**
	 * Sets up the strategy state variables to solve the given grid.
     * @param grid grid to be solved
	 * @return whether the variables could be set up
	 */
	
	public boolean setup( Grid grid );
	
	/**
	 * Finds a set of candidates for the next move. When no candidates have
     * been found, the function will return false and the solver will assume 
     * that the grid has been completed. Typically, a cell-based strategy 
     * will identify the precise cell where the next move will be made but
     * leave a pool of numbers from which to select the cell value, whereas
     * a value-based strategy will identify the precise value to be inserted 
     * but will identify only a pool of cells in which the chosen value could 
     * be inserted.
	 * @return whether a pool of candidates could be found
	 */
	
	public boolean findCandidates();
	
	/**
	 * Selects a precise cell/value combination and inserts it in the grid.
	 * @return whether the chosen value could be inserted
	 */

	public boolean selectCandidate();

    /**
     * Unwinds the most recent move
     * @param resetCurrent - whether the value of the current cell should be reset.
     * @return whether the moved could be reverted
     */
    
    public boolean unwind( boolean resetCurrent );	
    
    /**
     * Resets the grid to its state before the strategy had been invoked.
     */
    
    public void reset();
    
    /**
     * Returns the number of moves stored on the thread.
     */
    
    public int getThreadLength();
    
    /**
     * Returns the x-coordinate of the move at the given thread position.
     */
    
    public int getThreadX( int move );
    
    /**
     * Returns the y-coordinate of the move at the given thread position.
     */
    
    public int getThreadY( int move );
    
    /**
     * Dumps the thread to the given output stream.
     */
    
    public void dump( java.io.PrintStream out );
}
