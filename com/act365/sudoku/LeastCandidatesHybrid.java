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

    int[][] mask ;
    
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
        if( state instanceof IState ){
            mask = new int[grid.cellsInRow][grid.cellsInRow];
        }
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
        // The code is only executed for Least Candidates Hybrid II. 
        if( state instanceof IState && better.getScore() > 1 ){
            movesEliminated = true ;
            while( movesEliminated ){
                // Look for linked values.
                movesEliminated = false ;
                boolean anyMoveEliminated ;
                CellState cellState = (CellState) lcc.state ;
                NumberState numberState = (NumberState) lcn.state ; 
                int s , i , j , k , l , subsetSize , unionSize , nUnfilled ;
                int[] x = new int[grid.cellsInRow];
                int[] y = new int[grid.cellsInRow];
                int[] linkedValues = new int[grid.cellsInRow];
                int[] linkedCells = new int[grid.cellsInRow];
                boolean[] union = new boolean[grid.cellsInRow];
                s = 0 ;
                while( s < 3 * grid.cellsInRow ){
                    nUnfilled = 0 ;
                    i = 0 ;
                    while( i < grid.cellsInRow ){
                        if( ! numberState.isFilled[i][s] ){
                            ++ nUnfilled ;
                        }
                        ++ i ;
                    }
                    linkedValues[0] = 0 ;
                    subsetSize = 1 ;
                    // Ensure that the last value in the subset is sensible.
                    while( linkedValues[subsetSize-1] < grid.cellsInRow &&
                           ( numberState.nEliminated[linkedValues[subsetSize-1]][s] == 0 ||
                             numberState.nEliminated[linkedValues[subsetSize-1]][s] == grid.cellsInRow - 1 ) ){
                           ++ linkedValues[subsetSize-1];
                    }
                    if( linkedValues[subsetSize-1] == grid.cellsInRow ){
                        ++ s ;
                        continue ;
                    }
                    // Calculate union size
                    unionSize = grid.cellsInRow - numberState.nEliminated[linkedValues[0]][s] ;                    
                    while(true){
                        anyMoveEliminated = false ;
                        // Check the union size.
                        if( unionSize < subsetSize ){
                            return ( nCandidates = 0 );
                        } else if( unionSize == subsetSize && unionSize > 1 && unionSize < nUnfilled ){
                            i = 0 ;
                            j = 0 ;
                            while( j < grid.cellsInRow ){
                                if( union[j] ){
                                    linkedCells[i++] = j ;
                                }
                                ++ j ;
                            }
                            i = 0 ;
                            while( i < subsetSize ){
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
                                    while( k < subsetSize ){
                                        if( j == linkedValues[k] ){
                                            ++ j ;
                                            continue eliminate ;
                                        }
                                        ++ k ;
                                    }
                                    numberState.eliminateMove( x[i] , y[i] , j );
                                    cellState.eliminateMove( x[i] , y[i] , j );
                                    state.eliminateMove( x[i] , y[i] , j );
                                    anyMoveEliminated = movesEliminated = true ;
                                    ++ j ;
                                }
                                ++ i ;
                            }
                            if( explain && anyMoveEliminated ){
                                sb.append("The values ");
                                sb.append( 1 + linkedValues[0] );
                                i = 1 ;
                                while( i < subsetSize - 1 ){
                                    sb.append(",");
                                    sb.append( 1 + linkedValues[i++] );
                                }
                                sb.append(" and ");
                                sb.append( 1 + linkedValues[i] );
                                sb.append(" occupy the cells (");
                                sb.append( 1 + x[0] );
                                sb.append(",");
                                sb.append( 1 + y[0] );
                                sb.append(")");
                                i = 1 ;
                                while( i < subsetSize - 1 ){
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
                                sb.append(") in some order.\n");
                            }
                            if( linkedValues[0] < grid.cellsInRow - 1 ){
                                ++ linkedValues[0];
                                subsetSize = 1 ;
                            } else {
                                break ;
                            }
                        } else if( unionSize < nUnfilled ){
                            linkedValues[subsetSize] = linkedValues[subsetSize-1] + 1 ;
                            ++ subsetSize ;
                        } else {
                            ++ linkedValues[subsetSize-1];
                        }
                        // Ensure that the last value in the subset is sensible.
                        while( subsetSize > 0 ){
                            while( linkedValues[subsetSize-1] < grid.cellsInRow &&
                                   ( numberState.nEliminated[linkedValues[subsetSize-1]][s] == 0 ||
                                     numberState.nEliminated[linkedValues[subsetSize-1]][s] == grid.cellsInRow - 1 ) ){
                                   ++ linkedValues[subsetSize-1];
                            }
                            if( linkedValues[subsetSize-1] == grid.cellsInRow ){
                                if( -- subsetSize > 0 ){
                                    ++ linkedValues[subsetSize-1];
                                }
                            } else {
                                break;
                            }
                        }
                        if( subsetSize == 0 ){
                            break;
                        }                            
                        // Calculate the union size for the new subset.
                        unionSize = 0 ;
                        i = 0 ;
                        while( i < grid.cellsInRow ){
                            union[i++] = false ;
                        }
                        i = 0 ;
                        while( i < grid.cellsInRow ){
                            j = 0 ;
                            while( j < subsetSize ){
                                if( ! numberState.eliminated[linkedValues[j]][s][i] ){
                                    union[i] = true ;
                                    ++ unionSize ;
                                    break;
                                }
                                ++ j ;
                            }
                            ++ i ;
                        }
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
                                        state.eliminateMove( x0 , y0 , value );
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
                        if( numberState.nEliminated[value][s] == grid.cellsInRow ){
                            return ( nCandidates = 0 );
                        } else if( numberState.nEliminated[value][s] == grid.cellsInRow - 1 ){
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
                                    state.eliminateMove( x0 , y0 , value );
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
                // Check whether any candidate moves would lead to impossible
                // situations for the remaining values on the grid.
                movesEliminated = false ;
                int v , r , c , nCandidates ;
                boolean candidateNominated ;
                v = 0 ;
                while( v < grid.cellsInRow ){
                    // Consider each possible candidate move in turn to see
                    // whether it would lead to a contradiction.
                    i = 0 ;
                    while( i < grid.cellsInRow ){
                        j = 0 ;
                        while( j < grid.cellsInRow ){
                            if( cellState.eliminated[i][j][v] || cellState.nEliminated[i][j] == grid.cellsInRow - 1 ){
                                ++ j ;
                                continue ;
                            }
                            x0 = i ;
                            y0 = j ;
                            candidateNominated = true ;
                            // Initiate the mask.
                            r = 0 ;
                            while( r < grid.cellsInRow ){
                                c = 0 ;
                                while( c < grid.cellsInRow ){
                                    if( cellState.eliminated[r][c][v] ){
                                        mask[r][c] = 0 ;
                                    } else {
                                        if( cellState.nEliminated[r][c] == grid.cellsInRow - 1 ){
                                            mask[r][c] = 1 ; // Definite
                                        } else {
                                            mask[r][c] = 2 ; // Candidate
                                        }
                                    }
                                    ++ c ;
                                }
                                ++ r ;
                            }
                            // Promote the nominated candidate, remove dependent candidates
                            // and check the consistency of the resulting grid.
                            anyMoveEliminated = false ;
                            checkConsistency:
                            while( candidateNominated && ! anyMoveEliminated ){
                                candidateNominated = false ;
                                // Make definite the possibile move (x0,y0):=v.
                                mask[x0][y0] = 1 ;
                                // Remove dependent candidates ...
                                // ... from the row,
                                c = 0 ;
                                while( c < grid.cellsInRow ){
                                    if( mask[x0][c] == 2 ){
                                        mask[x0][c] = 0 ;
                                    }
                                    ++ c ;
                                }
                                // ... the column
                                r = 0 ;
                                while( r < grid.cellsInRow ){
                                    if( mask[r][y0] == 2 ){
                                        mask[r][y0] = 0 ;
                                    }
                                    ++ r ;
                                }
                                // ... and the box.
                                xLower = ( x0 / grid.boxesAcross )* grid.boxesAcross ;
                                xUpper = ( x0 / grid.boxesAcross + 1 )* grid.boxesAcross ;
                                yLower = ( y0 / grid.boxesDown )* grid.boxesDown ;
                                yUpper = ( y0 / grid.boxesDown + 1 )* grid.boxesDown ;
                                r = xLower ;
                                while( r < xUpper ){
                                    c = yLower ;
                                    while( c < yUpper ){
                                        if( mask[r][c] == 2 ){
                                            mask[r][c] = 0 ;
                                        }
                                        ++ c ;
                                    }
                                    ++ r ;
                                }
                                // Check the consistency of
                                // ... each row,
                                r = 0 ;
                                considerRow:
                                while( r < grid.cellsInRow ){
                                    nCandidates = 0 ;
                                    c = 0 ;
                                    while( c < grid.cellsInRow ){
                                        if( mask[r][c] == 1 ){
                                            ++ r ;
                                            continue considerRow ;
                                        } else if( mask[r][c] == 2 ){
                                            if( ++ nCandidates > 1 ){
                                                ++ r ;
                                                continue considerRow ;
                                            }
                                        }
                                        ++ c ;
                                    }
                                    if( nCandidates == 0 ){
                                        movesEliminated = anyMoveEliminated = true ;
                                        continue checkConsistency ;
                                    } else if( ! candidateNominated ){
                                        c = 0 ;
                                        while( mask[r][c] != 2 ){
                                            ++ c ;
                                        }
                                        x0 = r ;
                                        y0 = c ;
                                        candidateNominated = true ;
                                    }
                                    ++ r ;
                                }
                                // ... column 
                                c = 0 ;
                                considerColumn:
                                while( c < grid.cellsInRow ){
                                    nCandidates = 0 ;
                                    r = 0 ;
                                    while( r < grid.cellsInRow ){
                                        if( mask[r][c] == 1 ){
                                            ++ c ;
                                            continue considerColumn ;
                                        } else if( mask[r][c] == 2 ){
                                            if( ++ nCandidates > 1 ){
                                                ++ c ;
                                                continue considerColumn ;
                                            }
                                        }
                                        ++ r ;
                                    }
                                    if( nCandidates == 0 ){
                                        movesEliminated = anyMoveEliminated = true ;
                                        continue checkConsistency ;
                                    } else if( ! candidateNominated ){
                                        r = 0 ;
                                        while( mask[r][c] != 2 ){
                                            ++ r ;
                                        }
                                        x0 = r ;
                                        y0 = c ;
                                        candidateNominated = true ;
                                    }
                                    ++ c ;
                                }
                                // ... and box.
                                box = 0 ;
                                considerBox:
                                while( box < grid.cellsInRow ){
                                    xLower = box / grid.boxesAcross * grid.boxesAcross ;
                                    xUpper = ( box / grid.boxesAcross + 1 )* grid.boxesAcross ;
                                    yLower = box % grid.boxesAcross * grid.boxesDown ;
                                    yUpper = ( box % grid.boxesAcross + 1 )* grid.boxesDown ;
                                    nCandidates = 0 ;
                                    r = xLower ;
                                    while( r < xUpper ){
                                        c = yLower ;
                                        while( c < yUpper ){
                                            if( mask[r][c] == 1 ){
                                                ++ box ;
                                                continue considerBox ;
                                            } else if( mask[r][c] == 2 ){
                                                if( ++ nCandidates > 1 ){
                                                    ++ box ;
                                                    continue considerBox ;
                                                }
                                            }
                                            ++ c ;
                                        }
                                        ++ r ;
                                    }
                                    if( nCandidates == 0 ){
                                        movesEliminated = anyMoveEliminated = true ;
                                        continue checkConsistency ;
                                    } else if( ! candidateNominated ){
                                        r = xLower ;
                                        findSolitaryCandidateInBox:
                                        while( r < xUpper ){
                                            c = yLower ;
                                            while( c < yUpper ){
                                                if( mask[r][c] == 2 ){
                                                    x0 = r ;
                                                    y0 = c ;
                                                    r = xUpper ;
                                                    continue findSolitaryCandidateInBox ;
                                                }
                                                ++ c ;
                                            }
                                            ++ r ;
                                        }
                                        candidateNominated = true ;
                                    }
                                    ++ box ;
                                }
                            }             
                            if( anyMoveEliminated ){
                                cellState.eliminateMove( i , j , v );
                                numberState.eliminateMove( i , j , v );
                                state.eliminateMove( i , j , v );               
                                if( explain ){
                                    sb.append("The move (");
                                    sb.append( i + 1 );
                                    sb.append(",");
                                    sb.append( j + 1 );
                                    sb.append("):= ");
                                    sb.append( v + 1 );
                                    sb.append(" would make it impossible to place the remaining ");
                                    sb.append( v + 1 );
                                    sb.append("s.\n");
                                }
                            }
                            ++ j ;
                        }
                        ++ i ;
                    }
                    ++ v ;
                }
                // Repeat candidate search if moves have been eliminated.
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
        nCandidates = 0 ;        
        while( nCandidates < better.getNumberOfCandidates() ){
            xCandidates[nCandidates] = better.getXCandidate( nCandidates );    
            yCandidates[nCandidates] = better.getYCandidate( nCandidates );    
            valueCandidates[nCandidates] = better.getValueCandidate( nCandidates );
            if( explain ){
                reasonCandidates[nCandidates] = new StringBuffer();
                reasonCandidates[nCandidates].append( sb.toString() );
                reasonCandidates[nCandidates].append( better.getReasonCandidate( nCandidates ) );
            }
            ++ nCandidates ;    
        }        
        score = better.getScore();
        
        if( state instanceof IState ){
            InvulnerableState invulnerableState = (InvulnerableState) state ;
            int i , minInvulnerable = Integer.MAX_VALUE ;
            i = 0 ;
            while( i < better.getNumberOfCandidates() ){
                if( invulnerableState.nInvulnerable[valueCandidates[i]-1][xCandidates[i]][yCandidates[i]] < minInvulnerable ){
                    minInvulnerable = invulnerableState.nInvulnerable[valueCandidates[i]-1][xCandidates[i]][yCandidates[i]];    
                }
                ++ i ;
            }
            nCandidates = 0 ;
            i = 0 ;
            while( i < better.getNumberOfCandidates() ){
                if( invulnerableState.nInvulnerable[valueCandidates[i]-1][xCandidates[i]][yCandidates[i]] == minInvulnerable ){
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
    
	public boolean updateState(int x , int y , int value , String reason , boolean writeState ) throws Exception {
        if( nMoves == -1 ){
            return false ;
        }
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
            reasons[nMoves].append( reason );
        }
        ++ nMoves ;
        // Update state variables
        if( state instanceof IState ){
            state.addMove( x , y , value - 1 );
        }        
        // Underlying state variables
		lcn.updateState( x , y , value , reason , writeState );
        lcc.updateState( x , y , value , reason , writeState );
        return true ;
	}

	/**
     * Unwind the stack.
	 * @see com.act365.sudoku.IStrategy#unwind(int,boolean)
	 */
    
	public boolean unwind( int newNMoves , boolean reset ){
        // Unwind thread.
        if( newNMoves >= 0 ){
            if( explain && reset ){
                reasons[newNMoves].append("The move (");
                reasons[newNMoves].append( 1 + xMoves[newNMoves] );
                reasons[newNMoves].append(",");
                reasons[newNMoves].append( 1 + yMoves[newNMoves] );
                reasons[newNMoves].append("):=");
                reasons[newNMoves].append( grid.data[xMoves[newNMoves]][yMoves[newNMoves]] );
                reasons[newNMoves].append(" would lead to a contradiction.\n");
                int i = newNMoves + 1 ;
                while( i < nMoves ){
                    reasons[i++] = new StringBuffer();
                }
            }
            if( state instanceof IState ){
                state.popState( newNMoves );
                state.eliminateMove( xMoves[newNMoves] , yMoves[newNMoves] , grid.data[xMoves[newNMoves]][yMoves[newNMoves]] - 1 );
            }
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
