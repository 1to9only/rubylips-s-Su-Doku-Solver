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

    public int[][] data = new int[9][9];

    int x , y ; // Cursor postion
    
    int[] xMoves = new int[81] , yMoves = new int[81] ; // Stack
    
    int nMoves = 0 ; // Stack size
    
    /**
     * Determines whether a given column in the grid is sound, i.e. whether
     * it contains no duplicates.
     *  
     * @param i - column to be tested
     * @return true if the column is sound
     */
    
    boolean isColumnSound( int i ){
        
     boolean[] check = new boolean[9];
  
     int j = 0 ;
     
     while( j < 9 ){
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
        
     boolean[] check = new boolean[9];
  
     int i = 0 ;
     
     while( i < 9 ){
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
     
        boolean[] check = new boolean[9];
        
        int k = 0 ;
        
        while( k < 9 ){
         if( data[3*i+k%3][3*j+k/3] > 0 ){
            if( check[data[3*i+k%3][3*j+k/3]-1] ){
                return false ;
            } else {
             check[data[3*i+k%3][3*j+k/3]-1] = true ;   
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
        
        while( i < 9 ){
         if( ! isColumnSound( i ) ){
          return false ;  
         } else if( ! isRowSound( i ) ) {
          return false ;  
         } else if( ! isSubgridSound( i % 3 , i / 3 ) ){
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

        while( x < 9 && data[x][y] > 0 ){
            while( y < 9 && data[x][y] > 0 ){
                ++ y ;    
            }
            if( y == 9 ){
                ++ x ;
                y = 0 ;
            }
        }
        return x < 9 ;
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
            while( data[x][y] <= 9 && ! isSound() ){
                ++ data[x][y];   
            }
            if( data[x][y] <= 9 ){
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
        while( i < 9 ){
            j = 0 ;
            while( j < 9 ){
                data[i][j] = 0 ;
                ++ j ;
            }
            ++ i ;
        }
    }
    
    /**
     * Reverts the grid to its state prior to the last call to solve().
     * @see solve()
     */
    
    public void unsolve(){
        while( nMoves > 0 ){
            -- nMoves ;
            data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;   
        }
    }
}
