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
        
    public void setup( Grid grid ) throws Exception {
        super.setup( grid );
        lcn.setup( grid );
        lcc.setup( grid );
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

        String processing = "";
                
        StringBuffer sb = explain ? new StringBuffer() : null ;

        boolean movesEliminated ;
        
        if( lcc.findCandidates() == 0 || lcc.getScore() > 1 && lcn.findCandidates() == 0 ){
            score = 0 ;
            return ( nCandidates = 0 );
        }
        
        if( lcc.getScore() == 1 || lcc.getScore() < lcn.getScore() ){
            better = lcc ;
        } else {
            better = lcn ;
        }
        
        // When no indisputable candidate exists, look for linked values
        // or restricted candidate regions in order to eliminate moves.
        if( better.getScore() > 1 ){
            movesEliminated = true ;
            while( movesEliminated ){
                // Look for linked values.
                movesEliminated = false ;
                boolean anyMoveEliminated ;
                CellState cellState = (CellState) lcc.state ;
                NumberState numberState = (NumberState) lcn.state ; 
                int s , i , j , k , n , nMatches ;
                int[] v = new int[grid.cellsInRow];
                int[] x = new int[grid.cellsInRow];
                int[] y = new int[grid.cellsInRow];
                int[] linkedValues = new int[grid.cellsInRow];
                int[] linkedCells = new int[grid.cellsInRow];
                s = 0 ;
                while( s < 3 * grid.cellsInRow ){
                    anyMoveEliminated = false ;
                    v[0] = 0 ;
                    while( v[0] < grid.cellsInRow - 1 ){
                        n = grid.cellsInRow - numberState.nEliminated[v[0]][s] ;
                        if( n == 0 ){
                            return ( nCandidates = 0 );
                        } else if( n == 1 || n == grid.cellsInRow ){
                            ++ v[0];
                            continue ;
                        }
                        nMatches = 0 ;
                        linkedValues[nMatches++] = v[0];
                        i = 0 ;
                        while( i < n - 1 ){
                            v[i+1] = v[i] + 1 ;
                            findNextV:
                            while( v[i+1] < 2 + i + grid.cellsInRow - n ){
                                if( grid.cellsInRow - numberState.nEliminated[v[i+1]][s] != n ){
                                    ++ v[i+1] ;
                                    continue ;
                                }
                                // Confirm that the current value shares candidate positions with the first value. 
                                j = 0 ; 
                                while( j < grid.cellsInRow ){
                                    if( numberState.eliminated[v[0]][s][j] != numberState.eliminated[v[i+1]][s][j] ){
                                        ++ v[i+1];
                                        continue findNextV ;    
                                    }
                                    ++ j ;
                                }
                                linkedValues[nMatches++] = v[i+1];
                                break;
                            }
                            ++ i ;
                        }
                        if( nMatches == n ){
                            i = 0 ;
                            j = 0 ;
                            while( j < grid.cellsInRow ){
                                if( ! numberState.eliminated[v[0]][s][j] ){
                                    linkedCells[i++] = j ;
                                }
                                ++ j ;
                            }
                            i = 0 ;
                            while( i < n ){
                                if( s < grid.cellsInRow ){
                                    x[i] = s ;
                                    y[i] = linkedCells[i] ;
                                } else if( s < 2 * grid.cellsInRow ){
                                    x[i] = linkedCells[i] ;
                                    y[i] = s - grid.cellsInRow ;
                                } else {
                                    x[i] = ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + linkedCells[i] / grid.boxesDown ;
                                    y[i] = ( s - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + linkedCells[i] % grid.boxesDown ;
                                }
                                j = 0 ;
                                eliminate:
                                while( j < grid.cellsInRow ){
                                    if( cellState.eliminated[x[i]][y[i]][j] ){
                                        ++ j ;
                                        continue ;
                                    }                                    
                                    k = 0 ;
                                    while( k < n ){
                                        if( j == linkedValues[k] ){
                                            ++ j ;
                                            continue eliminate ;
                                        }
                                        ++ k ;
                                    }
                                    numberState.eliminateMove( x[i] , y[i] , j );
                                    cellState.eliminateMove( x[i] , y[i] , j );
                                    anyMoveEliminated = movesEliminated = true ;
                                    ++ j ;
                                }
                                ++ i ;
                            }
                            if( explain && anyMoveEliminated ){
                                sb.append("The values ");
                                sb.append( 1 + v[0] );
                                i = 1 ;
                                while( i < n - 1 ){
                                    sb.append(",");
                                    sb.append( 1 + v[i++] );
                                }
                                sb.append(" and ");
                                sb.append( 1 + v[i] );
                                sb.append(" in the cells (");
                                sb.append( 1 + x[0] );
                                sb.append(",");
                                sb.append( 1 + y[0] );
                                sb.append(")");
                                i = 1 ;
                                while( i < n - 1 ){
                                    sb.append(", (");
                                    sb.append( 1 + x[i] );
                                    sb.append(",");
                                    sb.append( 1 + y[i] );
                                    sb.append(")");
                                    ++ i ;
                                }
                                sb.append(" and (");
                                sb.append( 1 + x[i] );
                                sb.append(",");
                                sb.append( 1 + y[i] );
                                sb.append(") are linked.\n");
                            }
                        }
                        ++ v[0] ;
                    }
                    ++ s ;
                }
                // Repeat candidate search if subsectors have been found.
                if( movesEliminated ){
                    if( lcc.findCandidates() == 0 || lcc.getScore() > 1 && lcn.findCandidates() == 0 ){
                        score = 0 ;
                        return ( nCandidates = 0 );
                    }
        
                    if( lcc.getScore() == 1 || lcc.getScore() < lcn.getScore() ){
                        better = lcc ;
                    } else {
                        better = lcn ;
                    }
                    
                    if( better.getScore() == 1 ){
                        break ;
                    }
                }        
                // Check whether candidate positions are restricted to a single sector.
                movesEliminated = false ;
                int value , box , row , column , x0 , y0 , xLower , xUpper , yLower , yUpper ;
                value = 0 ;
                while( value < grid.cellsInRow ){
                    s = 0 ;
                    while( s < 2 * grid.cellsInRow ){
                        if( numberState.nEliminated[value][s] == grid.cellsInRow ){
                            return ( nCandidates = 0 );
                        } else if( numberState.nEliminated[value][s] == grid.cellsInRow - 1 ){
                            ++ s ;
                            continue ;
                        }
                        box = -1 ;
                        i = 0 ;
                        while( i < grid.cellsInRow ){
                            if( numberState.eliminated[value][s][i] ){
                                ++ i ;
                                continue ;
                            }
                            if( s < grid.cellsInRow ){
                                x0 = s ;
                                y0 = i ;
                            } else {
                                x0 = i ;
                                y0 = s - grid.cellsInRow ;
                            }
                            if( box == -1 ){
                                box = x0 / grid.boxesAcross * grid.boxesAcross + y0 / grid.boxesDown ;
                            } else if( box != x0 / grid.boxesAcross * grid.boxesAcross + y0 / grid.boxesDown ){
                                break;
                            }                        
                            ++ i ;
                        }
                        anyMoveEliminated = false ;
                        if( i == grid.cellsInRow ){
                            xLower = box / grid.boxesAcross * grid.boxesAcross ;
                            xUpper = ( box / grid.boxesAcross + 1 )* grid.boxesAcross ;
                            yLower = box % grid.boxesAcross * grid.boxesDown ;
                            yUpper = ( box % grid.boxesAcross + 1 )* grid.boxesDown ;
                            j = 0 ;
                            x0 = xLower ;
                            while( x0 < xUpper ){
                                if( s < grid.cellsInRow && s == x0 ){
                                    ++ x0 ;
                                    continue ;
                                }
                                y0 = yLower ;
                                while( y0 < yUpper ){
                                    if( s >= grid.cellsInRow && s - grid.cellsInRow == y0 ){
                                        ++ y0 ;
                                        continue ;
                                    }
                                    if( ! cellState.eliminated[x0][y0][value] ){
                                        numberState.eliminateMove( x0 , y0 , value );
                                        cellState.eliminateMove( x0 , y0 , value );
                                        anyMoveEliminated = movesEliminated = true ;
                                    }
                                    ++ y0 ;                                
                                }
                                ++ x0 ;
                            }
                            if( explain && anyMoveEliminated ){
                                sb.append("The value ");
                                sb.append( 1 + value );
                                sb.append(" in Box [");
                                sb.append( 1 + box / grid.boxesAcross );
                                sb.append(",");
                                sb.append( 1 + box % grid.boxesAcross );
                                sb.append("] must lie in ");
                                if( s < grid.cellsInRow ){
                                    sb.append("Row ");
                                    sb.append( 1 + s );
                                } else {
                                    sb.append("Column ");
                                    sb.append( 1 + s - grid.cellsInRow );
                                }
                                sb.append(".\n");
                            }
                        }
                        ++ s ;
                    }
                    while( s < 3 * grid.cellsInRow ){
                        if( numberState.nEliminated[value][s] == grid.cellsInRow - 1 ){
                            ++ s ;
                            continue ;
                        }
                        row = column = -1 ;
                        i = 0 ;
                        while( i < grid.cellsInRow ){
                            if( numberState.eliminated[value][s][i] ){
                                ++ i ;
                                continue ;
                            }
                            x0 = ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + i / grid.boxesDown ;
                            y0 = ( s - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + i % grid.boxesDown ;
                            if( row == -1 && column == -1 ){
                                row = x0 ;
                                column = y0 ;                        
                            } else if( row == -1 ){
                                if( y0 != column ){
                                    break ;
                                }
                            } else if( column == -1 ){
                                if( x0 != row ) {                                
                                    break ;
                                }
                            } else {
                                if( x0 == row ){
                                    column = -1 ;
                                } else if( y0 == column ){
                                    row = -1 ;
                                } else {
                                    break ;
                                }
                            }
                            ++ i ;
                        }
                        anyMoveEliminated = false ;
                        if( i == grid.cellsInRow ){
                            xLower = ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross ;
                            xUpper = ( ( s - 2 * grid.cellsInRow ) / grid.boxesAcross + 1 )* grid.boxesAcross ;
                            yLower = ( s - 2 * grid.cellsInRow ) % grid.boxesAcross * grid.boxesDown ;
                            yUpper = ( ( s - 2 * grid.cellsInRow ) % grid.boxesAcross + 1 )* grid.boxesDown ;
                            j = 0 ;
                            while( j < grid.cellsInRow ){
                                if( column == -1 ){
                                    x0 = row ;
                                    y0 = j ;
                                } else {
                                    x0 = j ;
                                    y0 = column ;
                                }
                                if( xLower <= x0 && x0 < xUpper && yLower <= y0 && y0 < yUpper ){
                                    ++ j ;
                                    continue ;
                                }
                                if( ! cellState.eliminated[x0][y0][value] ){
                                    numberState.eliminateMove( x0 , y0 , value );
                                    cellState.eliminateMove( x0 , y0 , value );
                                    anyMoveEliminated = movesEliminated = true ;
                                }
                                ++ j ;
                            }
                            if( explain && anyMoveEliminated ){
                                sb.append("The value ");
                                sb.append( 1 + value );
                                sb.append(" in ");
                                if( column == -1 ){
                                    sb.append("Row ");
                                    sb.append( 1 + row );
                                } else {
                                    sb.append("Column ");
                                    sb.append( 1 + column );
                                }
                                sb.append(" must lie in Box [");
                                sb.append( 1 + ( s - 2 * grid.cellsInRow )/ grid.boxesAcross );
                                sb.append(",");
                                sb.append( 1 + ( s - 2 * grid.cellsInRow )% grid.boxesAcross );
                                sb.append("].\n");
                            }
                        }
                        ++ s ;
                    }
                    ++ value ;
                }
                // Repeat candidate search if restricted positions have been found.
                if( movesEliminated ){
                    if( lcc.findCandidates() == 0 || lcc.getScore() > 1 && lcn.findCandidates() == 0 ){
                        score = 0 ;
                        return ( nCandidates = 0 );
                    }
        
                    if( lcc.getScore() == 1 || lcc.getScore() < lcn.getScore() ){
                        better = lcc ;
                    } else {
                        better = lcn ;
                    }
                    
                    if( better.getScore() == 1 ){
                        break;
                    }
                }        
            }
        }
        if( explain ){
            processing = sb.toString();
        }
        nCandidates = 0 ;        
        while( nCandidates < better.getNumberOfCandidates() ){
            xCandidates[nCandidates] = better.getXCandidate( nCandidates );    
            yCandidates[nCandidates] = better.getYCandidate( nCandidates );    
            valueCandidates[nCandidates] = better.getValueCandidate( nCandidates );
            if( explain ){
                reasonCandidates[nCandidates] = processing + better.getReasonCandidate( nCandidates );
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
                    if( ! randomize ){
                        return nCandidates ;
                    }
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
    
	public void updateState(int x , int y , int value , String reason , boolean writeState ) throws Exception {
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
		lcn.updateState( x , y , value , reason , writeState );
        lcc.updateState( x , y , value , reason , writeState );
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
