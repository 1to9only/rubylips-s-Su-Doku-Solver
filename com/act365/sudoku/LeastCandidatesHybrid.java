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
 * LeastCandidatesHybrid combines the Least Candidates Cell and Least 
 * Candidates Number strategies. 
 */

public class LeastCandidatesHybrid extends StrategyBase implements IStrategy {

    LeastCandidatesNumber lcn ;
    
    LeastCandidatesCell lcc ;


    Random generator ;
    
    boolean lcnChosen ,
            randomize ;
        
    /**
     * Sets up a LeastCandidatesHybrid strategy with an optional random element.
     */    
    
    public LeastCandidatesHybrid( boolean randomize ){
        this.randomize = randomize ;
        lcn = new LeastCandidatesNumber( randomize );
        lcc = new LeastCandidatesCell( randomize );        
        if( randomize ){
            generator = new Random();
        }
    }

    /**
     * Sets up the strategy to solve the given grid.
     * @see com.act365.sudoku.IStrategy#setup(Grid)
     */
        
    public boolean setup( Grid grid ){
        super.setup( grid );

        if( resize ){
            xCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            yCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            valueCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
        }

        return lcn.setup( grid ) && lcc.setup( grid );
    }
    
	/**
     * Finds candidates for the next move.
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
     
	public int findCandidates() {
        
        IStrategy better ;
        
        if( lcc.findCandidates() == 0 || lcn.findCandidates() == 0 ){
            score = 0 ;
            return ( nCandidates = 0 );
        }
        
        if( lcc.getScore() > lcn.getScore() ){
            better = lcc ;
        } else {
            better = lcn ;
        }
        
        nCandidates = 0 ;        
        while( nCandidates < better.getNumberOfCandidates() ){
            xCandidates[nCandidates] = better.getXCandidate( nCandidates );    
            yCandidates[nCandidates] = better.getYCandidate( nCandidates );    
            valueCandidates[nCandidates] = better.getValueCandidate( nCandidates );
            ++ nCandidates ;    
        }        
        score = better.getScore();
        
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
	 * @see com.act365.sudoku.IStrategy#updateState(int, int, int)
	 */
    
	public boolean updateState(int x, int y, int value) {
        // Own thread
        xMoves[nMoves] = x ;
        yMoves[nMoves] = y ;
        ++ nMoves ;
        // Underlying state variables
		return lcn.updateState( x , y , value ) && lcc.updateState( x , y , value );
	}

	/**
     * Unwind the stack.
	 * @see com.act365.sudoku.IStrategy#unwind(boolean)
	 */
    
	public boolean unwind( boolean resetCurrent ){
        if( nMoves == 0 ){
            return false ;
        }
        // Unwind thread.
        -- nMoves ;
        // Underlying unwinds
		if( ! lcn.unwind( false ) || ! lcc.unwind( false ) ){
            return false ;
		}
        // Remove the most recent move from the grid.
        if( resetCurrent ){
            grid.data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;
        }
        return true ;
	}
}
