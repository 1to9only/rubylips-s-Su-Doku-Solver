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
 * LeastCandidatesHybrid combines the Least Candidates Cell and Least 
 * Candidates Number strategies. 
 */

public class LeastCandidatesHybrid extends StrategyBase implements IStrategy {

    LeastCandidatesNumber lcn ;
    
    LeastCandidatesCell lcc ;

    /**
     * Sets up a LeastCandidatesHybrid strategy with an optional random element.
     * @param randomize whether the final candidates should be chosen randomly from its peers
     * @param checkInvulnerable indicates whether the moves should be post-filtered using the Invulnerable state grid.
     * @param explain whether explanatory debug should be produced
     */    
    
    public LeastCandidatesHybrid( boolean randomize , 
                                  boolean checkInvulnerable ,
                                  boolean explain ){
        super( randomize , explain );
        lcn = new LeastCandidatesNumber( randomize || checkInvulnerable , randomize , explain );
        lcc = new LeastCandidatesCell( randomize || checkInvulnerable , randomize , explain );        
        if( checkInvulnerable ){
            state = new InvulnerableState();
        }
    }

    /**
     * Sets up a LeastCandidatesHybrid strategy with an optional random element.
     */    
    
    public LeastCandidatesHybrid( boolean randomize ){
        this( randomize , false , true );
    }

    /**
     * Sets up the strategy to solve the given grid.
     * @see com.act365.sudoku.IStrategy#setup(Grid)
     */
        
    public boolean setup( Grid grid ){
        super.setup( grid );
        if( state instanceof IState ){
            state.setup( grid.boxesAcross , grid.boxesDown );
        }
        return  lcn.setup( grid ) && lcc.setup( grid );
    }
    
	/**
     * Finds candidates for the next move.
     * The LeastCandidatesCell search is performed before the LeastCandidatesNumber
     * search because it will find a result much quicker in the very common case
     * that very few candidates exist.  
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
     
	public int findCandidates() {
        
        IStrategy better ;
        
        if( lcc.findCandidates() == 0 || lcc.getScore() > 1 && lcn.findCandidates() == 0 ){
            score = 0 ;
            return ( nCandidates = 0 );
        }
        
        if( lcc.getScore() == 1 || lcc.getScore() < lcn.getScore() ){
            better = lcc ;
        } else {
            better = lcn ;
        }
        
        nCandidates = 0 ;        
        while( nCandidates < better.getNumberOfCandidates() ){
            xCandidates[nCandidates] = better.getXCandidate( nCandidates );    
            yCandidates[nCandidates] = better.getYCandidate( nCandidates );    
            valueCandidates[nCandidates] = better.getValueCandidate( nCandidates );
            if( explain ){
                reasonCandidates[nCandidates] = better.getReasonCandidate( nCandidates );
            }
            ++ nCandidates ;    
        }        
        score = better.getScore();
        
        if( state instanceof IState ){
            InvulnerableState invulnerableState = (InvulnerableState) state ;
            int i , minInvulnerable = Integer.MAX_VALUE ;
            i = 0 ;
            while( i < better.getNumberOfCandidates() ){
                if( invulnerableState.nInvulnerable[xCandidates[i]][yCandidates[i]][valueCandidates[i]-1] < minInvulnerable ){
                    minInvulnerable = invulnerableState.nInvulnerable[xCandidates[i]][yCandidates[i]][valueCandidates[i]-1];    
                }
                ++ i ;
            }
            nCandidates = 0 ;
            i = 0 ;
            while( i < better.getNumberOfCandidates() ){
                if( invulnerableState.nInvulnerable[xCandidates[i]][yCandidates[i]][valueCandidates[i]-1] == minInvulnerable ){
                    xCandidates[nCandidates] = xCandidates[i];    
                    yCandidates[nCandidates] = yCandidates[i];    
                    valueCandidates[nCandidates] = valueCandidates[i];
                    if( explain ){
                        reasonCandidates[nCandidates] = reasonCandidates[i];
                    }
                    ++ nCandidates ;    
                }
                ++ i ;
            }
        }
        
        return nCandidates ;
	}

	/** 
     * Updates state variables.
	 * @see com.act365.sudoku.IStrategy#updateState(int,int,int,String,boolean)
	 */
    
	public boolean updateState(int x , int y , int value , String reason , boolean writeState ) {
        // Store current state variables on thread.
        if( state instanceof IState ){
            if( writeState ){
                state.pushState( nMoves );
                stateWrite[nMoves] = true ;
            } else {
                stateWrite[nMoves] = false ;
            }        
        }
        // Store move to thread
        xMoves[nMoves] = x ;
        yMoves[nMoves] = y ;
        if( explain ){
            reasons[nMoves] = reason ;
        }
        ++ nMoves ;
        // Update state variables
        if( state instanceof IState ){
            state.addMove( x , y , value - 1 );
        }        
        // Underlying state variables
		return lcn.updateState( x , y , value , reason , writeState ) && lcc.updateState( x , y , value , reason , writeState );
	}

	/**
     * Unwind the stack.
	 * @see com.act365.sudoku.IStrategy#unwind(int,boolean)
	 */
    
	public boolean unwind( int newNMoves , boolean reset ){
        // Unwind thread.
        if( state instanceof IState && newNMoves >= 0 ){
            state.popState( newNMoves );
            state.eliminateMove( xMoves[newNMoves] , yMoves[newNMoves] , grid.data[xMoves[newNMoves]][yMoves[newNMoves]] - 1 );
        }
		lcn.unwind( newNMoves , false );
        lcc.unwind( newNMoves , false );
        // Remove the most recent moves from the grid.
        if( reset ){
            int i = Math.max( newNMoves , 0 );
            while( i < nMoves ){
                grid.data[xMoves[i]][yMoves[i]] = 0 ;
                ++ i ;
            }
        }
        nMoves = newNMoves ;
        return nMoves >= 0 ;
	}
    
    /**
     * Determines the last move for which two or more alternatives existed.
     */
    
    public int getLastWrittenMove(){
        int lcnMove = lcn.getLastWrittenMove() ,
            lccMove = lcc.getLastWrittenMove() ;
            
        if( lcnMove < lccMove ){
            return lcnMove ; 
        } else {
            return lccMove ;
        }
    }
}
