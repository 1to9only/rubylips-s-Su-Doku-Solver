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
        
        if( resize ){
            xCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            yCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            valueCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];

            eliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow];
            nEliminated = new int[grid.cellsInRow][grid.cellsInRow];
    
            threadEliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
            threadNEliminated = new int[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        }
        
        int i , j , k ;
        if( ! resize ){
            i = 0 ;
            while( i < grid.cellsInRow ){
                j = 0 ;
                while( j < grid.cellsInRow ){
                    nEliminated[i][j] = 0 ;
                    k = 0 ;
                    while( k < grid.cellsInRow ){
                        eliminated[i][j][k] = false ;
                        ++ k ;
                    }
                    ++ j ;
                }
                ++ i ;
            }
        }
        
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
	 * Finds the cells that have the least number of candidates. 
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
	
	public int findCandidates() {
		// Find the unpopulated cells with the smallest number of candidates.		
		int i , j , k , maxEliminated = -1 ;
        nCandidates = 0 ;
		i = 0 ;
		while( i < grid.cellsInRow ){
			j = 0 ;
			while( j < grid.cellsInRow ){
                if( nEliminated[i][j] == grid.cellsInRow ){
                    score = 0 ;
                    return ( nCandidates = 0 );
                } else if( grid.data[i][j] > 0 ){
				} else if( nEliminated[i][j] > maxEliminated ){
					nCandidates = 1 ;
					maxEliminated = nEliminated[i][j];
				}
				++ j ;
			}
			++ i ;
		}
        score = maxEliminated ;
        if( nCandidates == 0 ){
            return 0 ;
        }
        nCandidates = 0 ;
		i = 0 ;
		while( i < grid.cellsInRow ){
			j = 0 ;
			while( j < grid.cellsInRow ){
				if( grid.data[i][j] == 0 && nEliminated[i][j] == maxEliminated ){
                    k = 0 ;
                    while( k < grid.cellsInRow ){
                        if( ! eliminated[i][j][k] ){
                            xCandidates[nCandidates] = i ;
                            yCandidates[nCandidates] = j ;
                            valueCandidates[nCandidates] = k + 1 ;
                            ++ nCandidates ;
                        }
                        ++ k ;
                    }
                }
				++ j ;
			}
            ++ i ;	
		}			
        
        return nCandidates ;
	}

    /**
     * Selects a single candidate from the available list.
     */
    
    public void selectCandidate() {
        int pick = randomize && nCandidates > 1 ? Math.abs( generator.nextInt() % nCandidates ) : 0 ;
        bestX = xCandidates[pick];
        bestY = yCandidates[pick];
        bestValue = valueCandidates[pick];     
    }
    
    /**
     * Updates state variables.
     * @see com.act365.sudoku.IStrategy#updateState(int,int,int)
     */    
    
    public boolean updateState( int x , int y , int value ){
		// Store current state variables on thread.
		int i, j , k ;
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
        // Update state variables
        if( ! fillCell( x , y , value - 1 ) ){
            return false ;
        }
        // Store move to thread
		xMoves[nMoves] = x ;
		yMoves[nMoves] = y ;
		++ nMoves ;
		
		return true;
	}

	/**
	 * Unwinds the the thread and reinstates state variables.
	 * @see com.act365.sudoku.IStrategy#unwind(boolean)
	 */
	
	public boolean unwind( boolean resetCurrent ) {
        if( nMoves == 0 ){
            return false ;
        }
		// Unwind thread.
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
		// Current value is no longer a candidate.
		eliminated[xMoves[nMoves]][yMoves[nMoves]][grid.data[xMoves[nMoves]][yMoves[nMoves]]-1] = true ;
		++ nEliminated[xMoves[nMoves]][yMoves[nMoves]];
        if( resetCurrent ){
            grid.data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;
        }
		
        return true;
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
