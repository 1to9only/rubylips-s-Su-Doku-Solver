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

/**
 * A Grid object represents a partially-filled Su Doku grid.
 */

public class Grid {

    // Constants that define the grid size. The nomenclature is taken from
    // the Sudoku XML schema.
    
    int cellsInRow ,
        boxesAcross ,
        boxesDown ;
    
    // Grid data
    
    int[][] data ;

    // Thread data
    
    int x , y ; // Cursor postion
    
    int[] xMoves , yMoves ; 
    
    int nMoves ; // Thread length

    /**
     * Creates a Su Doku grid with the given number of boxes
     * (aka subgrids) in each dimension.
     */
    
    public Grid( int boxesAcross ,
                 int boxesDown ){
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
     * Sets the number of boxes in the Su Doku grid.
     */
    
    public void resize( int boxesAcross ,
                        int boxesDown ){
        
        this.boxesAcross = boxesAcross ;
        this.boxesDown = boxesDown ;
        
	    cellsInRow = boxesAcross * boxesDown ;

	    data = new int[cellsInRow][cellsInRow];
	  
	    xMoves = new int[cellsInRow*cellsInRow] ; 
	    yMoves = new int[cellsInRow*cellsInRow] ;
		
	    nMoves = 0 ;                   	
    }
    
    /**
     * Determines whether a given column in the grid is sound, i.e. whether
     * it contains no duplicates.
     *  
     * @param i - column to be tested
     * @return true if the column is sound
     */
    
    boolean isColumnSound( int i ){
        
     boolean[] check = new boolean[cellsInRow];
  
     int j = 0 ;
     
     while( j < cellsInRow ){
      if( data[i][j] > 0 ){
       if( check[data[i][j]-1] ){
        return false ;
       } else {
        check[data[i][j]-1] = true ;
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
        
     boolean[] check = new boolean[cellsInRow];
  
     int i = 0 ;
     
     while( i < cellsInRow ){
      if( data[i][j] > 0 ){
       if( check[data[i][j]-1] ){
        return false ;
       } else {
        check[data[i][j]-1] = true ;
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
     
        boolean[] check = new boolean[cellsInRow];
        
        int k = 0 ;
        
        while( k < cellsInRow ){
            if( data[i*boxesAcross+k%boxesAcross][j*boxesDown+k/boxesAcross] > 0 ){
                if( check[data[i*boxesAcross+k%boxesAcross][j*boxesDown+k/boxesAcross]-1] ){
                    return false ;
                } else {
                    check[data[i*boxesAcross+k%boxesAcross][j*boxesDown+k/boxesAcross]-1] = true ;   
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
        
        while( i < cellsInRow ){
           if( ! isColumnSound( i ) ){
               return false ;  
           } else if( ! isRowSound( i ) ) {
               return false ;  
           } else if( ! isSubgridSound( i % boxesDown , i / boxesDown ) ){
               return false ;  
           }
           ++ i ;
        }
        
        return true ;
    }
    
    /**
     * Advances the cursor to the next blank square in the grid.
     * @return whether the cursor has been advanced 
     */
    
    boolean advance(){

        while( x < cellsInRow && data[x][y] > 0 ){
            while( y < cellsInRow && data[x][y] > 0 ){
                ++ y ;    
            }
            if( y == cellsInRow ){
                ++ x ;
                y = 0 ;
            }
        }
        return x < cellsInRow ;
    }
    
    /**
     * Attempts to solve the current grid.
     * @return - whether a solution has been found
     */
    
    public boolean solve() {
        // Reset stack.
        nMoves = 0 ;
        // Reset cursor.
        x = y = 0 ;
        // Move the cursor onto the first blank square.
        if( ! advance() ){
            return isSound();
        }
        while( true ){
            // Fill the current square with the next valid value.
            ++ data[x][y];
            while( data[x][y] <= cellsInRow && ! isSound() ){
                ++ data[x][y];   
            }
            if( data[x][y] <= cellsInRow ){
                // Add current move to the stack.
                xMoves[nMoves] = x ;
                yMoves[nMoves] = y ;
                ++ nMoves ;
                if( ! advance() ){
                    return true ;
                }
                continue;
            } else {
                // Unwind from the stack.
                data[x][y] = 0 ;
                if( nMoves > 0 ){
                    -- nMoves ;
                    x = xMoves[nMoves];
                    y = yMoves[nMoves];
                } else {
                    return false ;
                }
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
    }
    
    /**
     * Reverts the grid to its state prior to the last call to solve().
     * @see Grid#solve()
     */
    
    public void unsolve(){
        while( nMoves > 0 ){
            -- nMoves ;
            data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;   
        }
    }
}
