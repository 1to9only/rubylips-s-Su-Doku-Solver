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

package com.act365.sudoku ;

import java.util.Random ;

/**
 * A Grid object represents a partially-filled Su Doku grid.
 */

public class Grid implements Cloneable {

    // Constants that define the grid size. The nomenclature is taken from
    // the Sudoku XML schema.
    
    int cellsInRow ,
        boxesAcross ,
        boxesDown ;
    
    // Grid data
    
    int[][] data ;

    // Thread data
    
    transient int x , y ; // Cursor postion
    
    transient int nUnwinds , // How many times the thread has been unwound
                  minThreadLength ; // Minimum thread length observed during solve

    /**
     * Creates a Su Doku grid with the given number of boxes
     * (aka subgrids) in each dimension.
     */
    
    public Grid( int boxesAcross , int boxesDown ){
        resize( boxesAcross , boxesDown );
    }

    /**
     * Creates a classic Su Doku grid, with the boxes (aka subgrids)
     * in each dimension.
     */    
    
    public Grid(){
    	resize( 3 , 3 );
    }
    
    /**
     * Clones the grid.
     */
    
    public Object clone() {
        
        Grid copy = new Grid( boxesAcross , boxesDown );
        
        int i, j ;
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                copy.data[i][j] = data[i][j];
                ++ j ;
            }
            ++ i ;
        }
        
        return copy ;
    }
    
    /**
     * Sets the number of boxes in the Su Doku grid.
     */
    
    public void resize( int boxesAcross ,
                        int boxesDown ){
        
        this.boxesAcross = boxesAcross ;
        this.boxesDown = boxesDown ;
        
	    cellsInRow = boxesAcross * boxesDown ;

	    data = new int[cellsInRow][cellsInRow];
	  
	    nUnwinds = 0 ;                	
    }
    
    /**
     * Counts the number of filled cells in the grid.
     */
    
    public int countFilledCells(){
        int i , j , count = 0 ;
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                if( data[i][j] > 0 ){
                    ++ count ;
                }
                ++ j ;
            }
            ++ i ;
        }
        
        return count ;
    }
    
    /**
     * Attempts to solve the current grid. When the requested number of 
     * solutions has been found, the function will exit.
     * @param maxSolns maximum number of solution to find (0 for no limit)  
     * @return - number of solutions found 
     */
    
    public int solve( IStrategy strategy , int maxSolns ) {
        return solve( strategy , maxSolns , true );
    }
    
    int solve( IStrategy strategy , int maxSolns , boolean firstPass ) {
    	int i , j , nSolns = 0 ;
        minThreadLength = strategy.getThreadLength();
        // Reset values if it's the first time through.
        if( firstPass ){
            x = y = nUnwinds = 0 ;
            if( ! strategy.setup( this ) || ! strategy.findCandidates() ){
                return 0 ;
            }
        }            
        // Move the cursor onto the first blank cell.
        while( true ){
            // Try to fill the current cell with a valid value.
            if( strategy.selectCandidate() ){
                if( ! strategy.findCandidates() ){
                    // Grid has been solved.
                    if( ++ nSolns == maxSolns || ! strategy.unwind( false ) ){
                    	return nSolns ;
                    }
                }
            } else {
				++ nUnwinds ;
            	if( ! strategy.unwind( true ) ){
            		return nSolns ;
            	}
                if( strategy.getThreadLength() < minThreadLength ){
                    minThreadLength = strategy.getThreadLength();
                }
            }
        }
    }

    /**
     * Attempts to build the grid into a puzzle that has rotational 
     * symmetry, a unique solution and at least the specified number 
     * of filled cells.
     * @param strategy the strategy to  be used
     * @param minFilledCells the minimum number of cells to appear in the composed puzzle
     * @return whether a suitable puzzle has been composed
     */
    
    public boolean compose( IStrategy strategy ,
                            int minFilledCells ){
                            
        int i , j , nSolns , nFilledCells ;
        Grid solution ;        
		// Solve the grid.
        strategy.setup( this );
		nSolns = solve( strategy , 1 );
        solution = (Grid) clone();
		strategy.reset();
		if( nSolns == 0 ){
			return false ;
		}
        // Count the number of filled cells.
        nFilledCells = 0 ;
        i = 0 ;
        while( i < cellsInRow ){
        	j = 0 ;
        	while( j < cellsInRow ){
        		if( data[i][j] > 0 ){
        			++ nFilledCells ;
        		}
        		++ j ;
        	}
        	++ i ;
        }
        // Calculate cells to be filled in order to enforce rotational symmetry.
        i = 0 ;
        while( i < cellsInRow ){
        	j = 0 ;
        	while( j < cellsInRow ){
        		if( data[i][j] > 0 && data[cellsInRow-1-i][cellsInRow-1-j] == 0 ){
        			data[cellsInRow-1-i][cellsInRow-1-j] = solution.data[cellsInRow-1-i][cellsInRow-1-j];
        			++ nFilledCells ;
        		}
        		++ j ;
        	}
        	++ i ;
        } 
        // Ensure that at least the minimum number of cells is in place.
        Random generator = null ;
        if( nFilledCells < minFilledCells ){
            generator = new Random();
        }
        while( nFilledCells < minFilledCells ){
            i = Math.abs( generator.nextInt() % cellsInRow );
            j = Math.abs( generator.nextInt() % cellsInRow );
            if( data[i][j] > 0 ){
                continue;
            }
            data[i][j] = solution.data[i][j];
            ++ nFilledCells ;
            if( cellsInRow % 2 == 0 ||
                i != ( cellsInRow - 1 )/ 2 ||
                j != ( cellsInRow - 1 )/ 2 ){
                    data[cellsInRow-1-i][cellsInRow-1-j]=solution.data[cellsInRow-1-i][cellsInRow-1-j];
                    ++ nFilledCells ;
            }
        }
        // Add cells until the grid has a unique solution.
        int bestX , bestY ;
        while( true ) {
            if( ( nSolns = solve( strategy , 1 ) ) == 0 || ! strategy.unwind( false ) ){
                return false ;
            }
            solution = (Grid) clone();
            // Place a new cell where the first and second solutions diverge.
            solve( strategy , 1 , false );
            if( strategy.getThreadLength() > 0 ){
                bestX = strategy.getThreadX( minThreadLength );
                bestY = strategy.getThreadY( minThreadLength );
                strategy.reset();
            } else {
                return true ;
            }
            // Add cells to grid.
            strategy.reset();
            data[bestX][bestY] = solution.data[bestX][bestY];
            ++ nFilledCells ;
            if( cellsInRow % 2 == 0 ||
                bestX != ( cellsInRow - 1 )/ 2 ||
                bestY != ( cellsInRow - 1 )/ 2 ){
                    data[cellsInRow-1-bestX][cellsInRow-1-bestY]=solution.data[cellsInRow-1-bestX][cellsInRow-1-bestY];
                    ++ nFilledCells ;
            }
        }
    }
    
    /**
     * Resets the value of each grid square.
     */
    
    public void reset(){
        int i , j ;
        
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                data[i][j] = 0 ;
                ++ j ;
            }
            ++ i ;
        }
        nUnwinds = 0 ;
    }
    
    /**
     * Dumps grid to an output stream.
     */
    
    public void dump( java.io.PrintStream out ){
        int i ,j ;
		i = 0 ;
		while( i < cellsInRow ){
			j = 0 ;
			while( j < cellsInRow ){
				out.print( data[i][j] + " " );
				++ j ;
			}
			out.println();
			++ i ;
		}
		out.println();
    }
}
