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

public abstract class StrategyBase {

    protected Grid grid ;
    
    // Thread variables
    
    protected int[] xMoves ,
                    yMoves ;
          
    protected int nMoves ;
    
    // Candidates selected by findCandidates()
    
    protected int[] xCandidates ,
                    yCandidates ,
                    valueCandidates ;
                    
    protected int nCandidates ;
    
    // Score
    
    protected int score ;
    
    // Best candidate selected by selectCandidate()
    
    protected int bestX ,
                  bestY ,
                  bestValue ;
    
    // Whether the underlying grid has been resized.
    
    transient protected boolean resize ;
                  
    /**
     * Sets up the thread.
     */
    
    protected boolean setup( Grid grid ){
    
        resize = this.grid == null || this.grid.cellsInRow != grid.cellsInRow ;
            
        this.grid = grid ;
        
        if( resize ){
            xMoves = new int[grid.cellsInRow*grid.cellsInRow];
            yMoves = new int[grid.cellsInRow*grid.cellsInRow];
        }
        nMoves = 0 ;

        if( resize ){
            xCandidates = new int[grid.cellsInRow*grid.cellsInRow];
            yCandidates = new int[grid.cellsInRow*grid.cellsInRow];
            valueCandidates = new int[grid.cellsInRow*grid.cellsInRow];
        }
        nCandidates = 0 ;
    
        score = 0 ;
            
        bestX = grid.cellsInRow ; 
        bestY = grid.cellsInRow ;
        bestValue = grid.cellsInRow ;
        
        return true ;
    }
    
    /**
     * Sets the value chosen by findCandidates().
     * @see com.act365.sudoku.IStrategy#setCandidate()
     */

    public void setCandidate() {
        grid.data[bestX][bestY] = bestValue ;
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
     * Returns the x-coordinate of the best candidate move.
     * @see com.act365.sudoku.IStrategy#getBestX()
     */
    
    public int getBestX(){
        return bestX ;
    }
    
    /**
     * Returns the y-coordinate of the best candidate move.
     * @see com.act365.sudoku.IStrategy#getBestY()
     */
    
    public int getBestY(){
        return bestY ;
    }
    
    /**
     * Returns the value of the best candidate move.
     * @see com.act365.sudoku.IStrategy#getBestValue()
     */
    
    public int getBestValue(){
        return bestValue ;
    }
    
    /**
     * Returns the x-coordinate of the given candidate.
     */
    
    public int getXCandidate( int index ){
        return xCandidates[ index ];    
    }

    /**
     * Returns the y-coordinate of the given candidate.
     */
    
    public int getYCandidate( int index ){
        return yCandidates[ index ];    
    }

    /**
     * Returns the value-coordinate of the given candidate.
     */
    
    public int getValueCandidate( int index ){
        return valueCandidates[ index ];    
    }

    /**
     * Returns the umber of candidates.
     */    
    
    public int getNumberOfCandidates(){
        return nCandidates ;
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
     * Returns a measure of the confidence the strategy holds
     * in its candidates.
     * @see IStrategy#getScore()
     */
    
    public int getScore(){
        return score ;
    }
    
    /**
     * Dumps the thread to the given output stream.
     */
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        int i = 0 ;
        while( i < nMoves ){
            sb.append( ( 1 + i ) + ". (" + ( 1 + xMoves[i] ) + "," + ( 1 + yMoves[i] ) + "):=" + grid.data[xMoves[i]][yMoves[i]] + "\n");
            ++ i ;
        }
        sb.append('\n');
        return sb.toString(); 
    }  
}
