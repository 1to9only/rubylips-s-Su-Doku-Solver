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
 * StrategyBase handles several thread-related function common to
 * most implementors of IStrategy.
 * @see IStrategy
 */

public class StrategyBase {

    protected Grid grid ;
    
    protected int[] xMoves ,
                    yMoves ;
          
    protected int nMoves ;
    
    /**
     * Sets up the thread.
     */
    
    protected boolean setup( Grid grid ){
        this.grid = grid ;
        
        xMoves = new int[grid.cellsInRow*grid.cellsInRow];
        yMoves = new int[grid.cellsInRow*grid.cellsInRow];
        nMoves = 0 ;
        
        return true ;
    }
    
    /**
     * Resets each cell that appears on the thread.
     * @see com.act365.sudoku.IStrategy#reset()
     */
    
    public void reset() {
        while( nMoves > 0 ){
            -- nMoves ;
            grid.data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;   
        }       
    }

    /**
     * Returns thread length.
     * @see com.act365.sudoku.IStrategy#getThreadLength()
     */
    
    public int getThreadLength(){
        return nMoves ;
    }
    
    /**
     * Returns x-coordinate of move at given thread position.
     * @see com.act365.sudoku.IStrategy#getThreadX(int)
     */
    
    public int getThreadX( int move ){
        return xMoves[ move ];
    }
    
    /**
     * Returns y-coordinate of move at given thread position.
     * @see com.act365.sudoku.IStrategy#getThreadY(int)
     */
    
    public int getThreadY( int move ){
        return yMoves[ move ];    
    }
    
    /**
     * Dumps the thread to the given output stream.
     */
    
    public void dump( java.io.PrintStream out ){
        int i = 0 ;
        while( i < nMoves ){
            out.println( i + ". (" + xMoves[i] + "," + yMoves[i] + ")");
            ++ i ;
        }
        out.println();
    }        
}
