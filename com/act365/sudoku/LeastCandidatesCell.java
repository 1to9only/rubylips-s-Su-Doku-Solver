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

import java.util.Random ;

/**
 * The LeastCandidatesCell strategy calculates the number of seemingly valid
 * candidates (of course, for a problem with a unique solution, there is only
 * one strictly valid candidate for each cell - a candidate is deemed
 * 'seemingly valid' if it isn't blatantly contradicted by another cell in
 * that row, column or subgrid) for each cell in the grid, and fills the cells
 * with the least number of possible candidates first.
 */

public class LeastCandidatesCell extends StrategyBase implements IStrategy {

    boolean randomize ;

    Random generator ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][] nEliminated ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][] threadNEliminated ;
    
    /**
     * Creates a new LeastCandidatesCell instance to solve the given grid.
     */
    
    public LeastCandidatesCell( boolean randomize ){
    	this.randomize = randomize ;
		
        if( randomize ){
        	generator = new Random();	
        }
    }
    
    /**
     * Sets the state variables.
     */
    
    public boolean setup( Grid grid ){

        super.setup( grid );
        
        eliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow];
        nEliminated = new int[grid.cellsInRow][grid.cellsInRow];
    
        threadEliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        threadNEliminated = new int[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        
		int i , j ;
		i = 0 ;
		while( i < grid.cellsInRow ){
			j = 0 ;
			while( j < grid.cellsInRow ){
				if( grid.data[i][j] > 0 ){
					if( ! fillCell( i , j , grid.data[i][j] - 1 ) ){
						return false ;
					}
				}
				++ j ;
			}
			++ i ;
		}
		return true ;
    }
    
	/** 
	 * Chooses from those cells that have the least number of candidates. 
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
	
	public boolean findCandidates() {
		// Find the unpopulated cells with the smallest number of candidates.		
		int i , j , bestX = grid.cellsInRow , bestY = grid.cellsInRow , countBest = 0 , maxEliminated = -1 , nFilled = 0 ;
		i = 0 ;
		while( i < grid.cellsInRow ){
			j = 0 ;
			while( j < grid.cellsInRow ){
				if( grid.data[i][j] > 0 ){
					++ nFilled ;
				} else if( nEliminated[i][j] > maxEliminated ){
					countBest = 1 ;
					bestX = i ;
					bestY = j ;
					maxEliminated = nEliminated[i][j];
				} else if( randomize && nEliminated[i][j] == maxEliminated ) {
					++ countBest ;
				}
				++ j ;
			}
			++ i ;
		}
		// Test whether the grid is complete.
		if( nFilled == grid.cellsInRow * grid.cellsInRow ){
			return false ;
		}
		// Select from the candidates.
		if( ! randomize || countBest == 1 ){
			grid.x = bestX ;
			grid.y = bestY ;
			return true ;
		} else {
			int pick = Math.abs( generator.nextInt() % countBest );
			i = 0 ;
			while( i < grid.cellsInRow ){
				j = 0 ;
				while( j < grid.cellsInRow ){
					if( grid.data[i][j] == 0 && nEliminated[i][j] == maxEliminated && -- pick < 0 ){
						grid.x = i ;
						grid.y = j ;
						return true ;
					}
					++ j ;
				}
				++ i ;	
			}			
		}
		// Shouldn't reach here.
		return false;
	}

	/**
	 * Chooses a value for the current cell from the available candidates.
	 * @see com.act365.sudoku.IStrategy#selectCandidate()
	 */
	public boolean selectCandidate() {
		int i = 0 ;
        final int score = grid.cellsInRow - nEliminated[grid.x][grid.y];
		// Ascertain the value to write.
		if( score == 0 ){
			return false ;
		} else if( ! randomize || score == 1 ){
			while( i < grid.cellsInRow ){
				if( ! eliminated[grid.x][grid.y][i] ){
					break ;
				}
				++ i ;
			}			
		} else {
			int pick = Math.abs( generator.nextInt() % score );
			while( i < grid.cellsInRow ){
				if( ! eliminated[grid.x][grid.y][i] && -- pick < 0 ){
					break;
				}
				++ i ;
			}
		}
		// Write to the grid.
		grid.data[grid.x][grid.y] = i + 1 ;
		// Store state variables.
		int j , k ;
		i = 0 ;
		while( i < grid.cellsInRow ){
			j = 0 ;
			while( j < grid.cellsInRow ){
				k = 0 ;
				while( k < grid.cellsInRow ){
					threadEliminated[i][j][k][nMoves] = eliminated[i][j][k];
					++ k ;
				}
				threadNEliminated[i][j][nMoves] = nEliminated[i][j];
				++ j ;
			}
			++ i ;
		}
		xMoves[nMoves] = grid.x ;
		yMoves[nMoves] = grid.y ;
		// Update state variables.
		if( ! fillCell( grid.x , grid.y , grid.data[grid.x][grid.y] - 1 ) ){
			return false ;
		}
		++ nMoves ;
		
		return true;
	}

	/**
	 * Unwinds the the thread and reinstates state variables.
	 * @see com.act365.sudoku.IStrategy#unwind(boolean)
	 */
	
	public boolean unwind(boolean resetCurrent) {
		if( resetCurrent ){
			// Remove it from the grid.
			grid.data[grid.x][grid.y] = 0 ;
		}
		// Unwind thread.
		if( nMoves > 0 ){
			-- nMoves ;
			// Reinstate state variables.
			int i , j , k ;
			i = 0 ;
			while( i < grid.cellsInRow ){
				j = 0 ;
				while( j < grid.cellsInRow ){
					k = 0 ;
					while( k < grid.cellsInRow ){
						eliminated[i][j][k] = threadEliminated[i][j][k][nMoves];
						++ k ;
					}
					nEliminated[i][j] = threadNEliminated[i][j][nMoves];
					++ j ;
				}
				++ i ;
			}
			// Reset cursor.
			grid.x = xMoves[nMoves];
			grid.y = yMoves[nMoves];
			// Current value is no longer a candidate.
			eliminated[grid.x][grid.y][grid.data[grid.x][grid.y]-1] = true ;
			++ nEliminated[grid.x][grid.y];
			return true;
		} else {
			return false ;
		}		
	}

	/**
	 * Updates the state grids as a new cell has been filled.
	 * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
	 */
	
	boolean fillCell( int x , int y , int value ){
		int i , j ;
		// Check that it's a valid candidate.
		if( eliminated[x][y][value] ){
			return false ;
		}
		// Eliminate other candidates for the current cell.
		i = 0 ;
		while( i < grid.cellsInRow ){
			if( i != value && ! eliminated[x][y][i] ){
				eliminated[x][y][i] = true ;
				++ nEliminated[x][y];
			}
			++ i ;
		}
		if( nEliminated[x][y] != grid.cellsInRow - 1 ){
			return false ;
		}
		// Eliminate other candidates for the current row.
        j = 0 ;
        while( j < grid.cellsInRow ){
        	if( j != y && ! eliminated[x][j][value] ){
        		eliminated[x][j][value] = true ;
        		++ nEliminated[x][j];
        	}
        	++ j ;
        }
		// Eliminate other candidates for the current column.
		i = 0 ;
		while( i < grid.cellsInRow ){
			if( i != x && ! eliminated[i][y][value] ){
				eliminated[i][y][value] = true ;
				++ nEliminated[i][y];
			}
			++ i ;
		}
		// Eliminate other candidates for the current subgrid.
		i = x / grid.boxesAcross * grid.boxesAcross - 1 ;
		while( ++ i < ( x / grid.boxesAcross + 1 )* grid.boxesAcross ){
			if( i == x ){
				continue ;
			}
			j = y / grid.boxesDown * grid.boxesDown - 1 ;
			while( ++ j < ( y / grid.boxesDown + 1 )* grid.boxesDown ){
				if( j == y ){
					continue ;
				}
				if( ! eliminated[i][j][value] ){
					eliminated[i][j][value] = true ;
					++ nEliminated[i][j];
				}
			}
		}
				
		return true ;
	}
}
