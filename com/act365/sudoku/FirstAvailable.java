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
 * The 'FirstAvailable' strategy always selects the first valid choice
 * it finds in any situation - it doesn't make any attempt to rank the
 * various alternatives. It is the only strategy implemented in 
 * Releases 1.0 and 1.1.
 */

public class FirstAvailable extends StrategyBase implements IStrategy {

    /**
     * The strategy has no state variables.
     */
    
    public boolean setup( Grid grid ){        
        return super.setup( grid );
    }
    
    /**
     * Advances the cursor to the next empty cell.
     * @see com.act365.sudoku.IStrategy#findCandidates()
     */
    
	public boolean findCandidates() {
		while( grid.x < grid.cellsInRow && grid.data[grid.x][grid.y] > 0 ){
			while( grid.y < grid.cellsInRow && grid.data[grid.x][grid.y] > 0 ){
				++ grid.y ;    
			}
			if( grid.y == grid.cellsInRow ){
				++ grid.x ;
				grid.y = 0 ;
			}
		}
		return grid.x < grid.cellsInRow ;
	}

    /**
     * Increases the value of the current cell until a 
     * suitable value has been found, whereupon
     * the cell coordinates are stored on the thread.
     * @see com.act365.sudoku.IStrategy#selectCandidate()
     */
    
	public boolean selectCandidate() {
		++ grid.data[grid.x][grid.y];
		while( grid.data[grid.x][grid.y] <= grid.cellsInRow && ! isSound() ){
			++ grid.data[grid.x][grid.y];   
		}
		if( grid.data[grid.x][grid.y] <= grid.cellsInRow ){
			xMoves[nMoves] = grid.x ;
			yMoves[nMoves] = grid.y ;
			++ nMoves ;
			return true ;
		} else {
			return false ;
		}
	}

    /**
     * Removes the current cell coordinates from the thread
     * and moves back to the previous cell.
     * @see com.act365.sudoku.IStrategy#unwind(boolean)
     */
    
	public boolean unwind( boolean resetCurrent ) {
		if( resetCurrent ){
			grid.data[grid.x][grid.y] = 0 ;
		}
		if( nMoves > 0 ){
			-- nMoves ;
			grid.x = xMoves[nMoves];
			grid.y = yMoves[nMoves];
			return true;
		} else {
			return false ;
		}
	}
	
	/**
	 * Determines whether a given column in the grid is sound, i.e. whether
	 * it contains no duplicates.
	 *  
	 * @param i - column to be tested
	 * @return true if the column is sound
	 */
    
	boolean isColumnSound( int i ){
        
	 boolean[] check = new boolean[grid.cellsInRow];
  
	 int j = 0 ;
     
	 while( j < grid.cellsInRow ){
	  if( grid.data[i][j] > 0 ){
	   if( check[grid.data[i][j]-1] ){
		return false ;
	   } else {
		check[grid.data[i][j]-1] = true ;
	   }
	  }
	  ++ j ;
	 }
     
	 return true ;
	}

	/**
	 * Determines whether a given row in the grid is sound, i.e. whether
	 * it contains no duplicates.
	 *  
	 * @param j - row to be tested
	 * @return true if the row is sound
	 */
    
	boolean isRowSound( int j ){
        
	 boolean[] check = new boolean[grid.cellsInRow];
  
	 int i = 0 ;
     
	 while( i < grid.cellsInRow ){
	  if( grid.data[i][j] > 0 ){
	   if( check[grid.data[i][j]-1] ){
		return false ;
	   } else {
		check[grid.data[i][j]-1] = true ;
	   }
	  }
	  ++ i ;
	 }
     
	 return true ;
	}
    
	/**
	 * Determines whether a given subgrid is sound, i.e. whether
	 * it contains no duplicates.
	 *  
	 * @param i - row coordinate of subgrid to be tested
	 * @param j - column coordinate of subgrid to be tested
	 * @return true if the subgrid is sound
	 */
    
	boolean isSubgridSound( int i , int j ){
     
		boolean[] check = new boolean[grid.cellsInRow];
        
		int k = 0 ;
        
		while( k < grid.cellsInRow ){
			if( grid.data[i*grid.boxesAcross+k%grid.boxesAcross][j*grid.boxesDown+k/grid.boxesAcross] > 0 ){
				if( check[grid.data[i*grid.boxesAcross+k%grid.boxesAcross][j*grid.boxesDown+k/grid.boxesAcross]-1] ){
					return false ;
				} else {
					check[grid.data[i*grid.boxesAcross+k%grid.boxesAcross][j*grid.boxesDown+k/grid.boxesAcross]-1] = true ;   
				}
			 }
			 ++ k ;
		}
        
		return true ;    
	}

	/**
	 * Determines whether the grid is sound - i.e. whether each row, column
	 * and subgrid within the grid is itself sound.
	 * 
	 * @return true if the grid is sound
	 */
    
	boolean isSound(){
     
		int i = 0 ;
        
		while( i < grid.cellsInRow ){
		   if( ! isColumnSound( i ) ){
			   return false ;  
		   } else if( ! isRowSound( i ) ) {
			   return false ;  
		   } else if( ! isSubgridSound( i % grid.boxesDown , i / grid.boxesDown ) ){
			   return false ;  
		   }
		   ++ i ;
		}
        
		return true ;
	}

}
