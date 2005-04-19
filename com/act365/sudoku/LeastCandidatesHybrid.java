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

    IStrategy better ;

    boolean useDisjointSubsets ,
            useSingleSectorCandidates ,
            useXWings ,
            useSwordfish ,
            useNishio ,
            useGuesses ;
            
    int singleCandidatureCalls ,
        disjointSubsetsCalls ,
        disjointSubsetsEliminations ,
        maxDisjointSubsetsSize ,
        singleSectorCandidatesCalls ,
        singleSectorCandidatesEliminations ,
        xWingsCalls ,
        xWingsEliminations ,
        swordfishCalls ,
        swordfishEliminations ,
        nishioCalls ,
        nishioEliminations ,
        nGuesses ,
        maxStrings ;
    
    // Arrays defined as members in order to improve performance.

    transient int disjointSubsetsStartSector ,
                  singleSectorCandidatesStartValue ;
        
    transient int[] x , y , linkedValues , linkedCells ;
    
    transient int[] stringR0 , stringC0 , stringR1 , stringC1 , stringLength ;
    
    transient boolean[] union ;
            
    transient int[][] mask ;

    /**
     * Sets up a LeastCandidatesHybrid II strategy with an optional random element.
     * @param randomize whether the final candidates should be chosen randomly from its peers
     * @param checkInvulnerable indicates whether the moves should be post-filtered using the Invulnerable state grid.
     * @param useAllLogicalMethods whether the solver should look for X-Wings  and Nishio
     * @param explain whether explanatory debug should be produced
     */    
    
    public LeastCandidatesHybrid( boolean randomize , 
                                  boolean checkInvulnerable ,
                                  boolean useAllLogicalMethods ,
                                  boolean explain ){
        super( randomize , explain );
        lcn = new LeastCandidatesNumber( randomize || checkInvulnerable , randomize , explain );
        lcc = new LeastCandidatesCell( randomize || checkInvulnerable , randomize , explain );        
        if( checkInvulnerable ){
            state = new InvulnerableState();
        }
        useDisjointSubsets = true ;
        useSingleSectorCandidates = true ;
        useXWings = useAllLogicalMethods ;
        useSwordfish = useAllLogicalMethods ;
        useNishio = useAllLogicalMethods ;
        useGuesses = true ;
    }

    /**
     * Sets up a LeastCandidatesHybrid I strategy with an optional random element.
     */    
    
    public LeastCandidatesHybrid( boolean randomize ,
                                  boolean explain ){
        this( randomize , false , false , explain );
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
            if( useDisjointSubsets ){
                x = new int[grid.cellsInRow];
                y = new int[grid.cellsInRow];
                linkedValues = new int[grid.cellsInRow];
                linkedCells = new int[grid.cellsInRow];
                union = new boolean[grid.cellsInRow];
            }
            if( useXWings || useSwordfish ){
                // The following array size isn't a theoretical upper limit 
                // but should prove adequate.
                maxStrings = grid.cellsInRow * grid.cellsInRow * grid.cellsInRow ;
                stringR0 = new int[maxStrings];
                stringC0 = new int[maxStrings];
                stringR1 = new int[maxStrings];
                stringC1 = new int[maxStrings];
                stringLength = new int[maxStrings];
            }
            if( useNishio ){
                mask = new int[grid.cellsInRow][grid.cellsInRow];
            }
        }
        maxDisjointSubsetsSize = explain ? grid.cellsInRow : 6 /* Rule-of-thumb */ ;
        singleCandidatureCalls = 0 ;
        disjointSubsetsCalls = disjointSubsetsEliminations = 0 ;
        singleSectorCandidatesCalls = singleSectorCandidatesEliminations = 0 ;
        xWingsCalls = xWingsEliminations = 0 ;
        swordfishCalls = swordfishEliminations = 0 ;
        nishioCalls = nishioEliminations = 0 ;
        nGuesses = 0 ;
    }
    
	/**
     * Finds candidates for the next move.
     * The LeastCandidatesCell search is performed before the LeastCandidatesNumber
     * search because it will find a result much quicker in the very common case
     * that very few candidates exist.  
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
     
	public int findCandidates() {
        
        StringBuffer sb = explain ? new StringBuffer() : null ;

        try {
            singleCandidature();
        } catch ( Exception e ){
            score = 0 ;
            return ( nCandidates = 0 );            
        }
        
        // When no indisputable candidate exists, employ the various
        // rules in order to try to eliminate candidates.
        // The code is only executed for Least Candidates Hybrid II. 
        if( state instanceof IState && score > 1 ){
            try {
                disjointSubsetsStartSector = 0 ;
                singleSectorCandidatesStartValue = 0 ;
                while( true ){
                    if( useSingleSectorCandidates && singleSectorCandidates( sb ) ){
                        if( singleCandidature() ){
                            break ;
                        } else {
                            disjointSubsetsStartSector = 0 ;
                            continue ;
                        }
                    }
                    if( useDisjointSubsets && disjointSubsets( sb ) ){
                        if( singleCandidature() ){
                            break ;
                        } else {
                            singleSectorCandidatesStartValue = 0 ;
                            continue ;
                        }
                    }
                    if( useXWings && xWings( sb ) ){
                        if( singleCandidature() ){
                            break ;
                        } else {
                            singleSectorCandidatesStartValue = 0 ;
                            disjointSubsetsStartSector = 0 ;
                            continue ;
                        }
                    }
                    if( useSwordfish && swordfish( sb ) ){
                        if( singleCandidature() ){
                            break ;
                        } else {
                            singleSectorCandidatesStartValue = 0 ;
                            disjointSubsetsStartSector = 0 ;
                            continue ;
                        }
                    }
                    if( useNishio && nishio( sb ) ){
                        if( singleCandidature() ){
                            break ;
                        } else {
                            singleSectorCandidatesStartValue = 0 ;
                            disjointSubsetsStartSector = 0 ;
                            continue ;
                        }
                    }
                    break ;
                }
            } catch ( Exception e ) {
                score = 0 ;
                return ( nCandidates = 0 );
            }
        }
        if( score > 1 ){
            if( useGuesses ){
                ++ nGuesses ;
            } else {
                score = 0 ;
                return ( nCandidates = 0 );
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
     * Determines which underlying strategy to prefer.  
     * @return whether an undisputed candidate has been found
     * @throws Exception bad grid state
     */
    
    boolean singleCandidature() throws Exception {
        ++ singleCandidatureCalls ;
        if( lcc.findCandidates() == 0 || lcc.getScore() > 1 && lcn.findCandidates() == 0 ){
            throw new Exception("Bad grid state");
        }
        if( lcc.getScore() == 1 || lcc.getScore() < lcn.getScore() ){
            better = lcc ;
        } else {
            better = lcn ;
        }
        return ( score = better.getScore() ) == 1 ;
    }
    
    /**
     * Checks whether some strictly smaller subset of the candidates for a 
     * row, column or box will fit into some subset of the available cells,
     * in which case eliminations will be possible. 
     * @param sb explanation
     * @return whether eliminations have been performed
     * @throws Exception the grid is in a bad state
     */
    
    boolean disjointSubsets( StringBuffer sb ) throws Exception {
        ++ disjointSubsetsCalls ;
        boolean anyMoveEliminated ;
        CellState cellState = (CellState) lcc.state ;
        NumberState numberState = (NumberState) lcn.state ; 
        int s , i , j , k , l , subsetSize , unionSize , nUnfilled , nUnconsideredValues ;
        s = disjointSubsetsStartSector ;
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
            // Count the number of unconsidered values.
            nUnconsideredValues = 0 ;
            i = linkedValues[subsetSize-1] + 1 ;
            while( i < grid.cellsInRow ){
                if( ! numberState.isFilled[i][s] ){
                    ++ nUnconsideredValues ;
                }
                ++ i ;
            }
            // Calculate union size
            unionSize = grid.cellsInRow - numberState.nEliminated[linkedValues[0]][s] ;                    
            while( true ){
                anyMoveEliminated = false ;
                // Check the union size.
                if( unionSize < subsetSize ){
                    throw new Exception("Bad grid state");
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
                            anyMoveEliminated = true ;
                            ++ disjointSubsetsEliminations ; 
                            ++ j ;
                        }
                        ++ i ;
                    }
                    if( anyMoveEliminated ){
                        if( explain ){
                            sb.append("The values ");
                            sb.append( 1 + linkedValues[0] );
                            i = 1 ;
                            while( i < subsetSize - 1 ){
                                sb.append(", ");
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
                        disjointSubsetsStartSector = s ;
                        return true ;
                    }
                    if( linkedValues[0] < grid.cellsInRow - 1 ){
                        ++ linkedValues[0];
                        subsetSize = 1 ;
                    } else {
                        break ;
                    }
                } else if( unionSize >= maxDisjointSubsetsSize || unionSize > subsetSize + nUnconsideredValues || unionSize >= nUnfilled ) {
                    ++ linkedValues[subsetSize-1];
                } else {
                    linkedValues[subsetSize] = linkedValues[subsetSize-1] + 1 ;
                    ++ subsetSize ;
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
                // Count the number of unconsidered values.
                nUnconsideredValues = 0 ;
                i = linkedValues[subsetSize-1] + 1 ;
                while( i < grid.cellsInRow ){
                    if( ! numberState.isFilled[i][s] ){
                        ++ nUnconsideredValues ;
                    }
                    ++ i ;
                }
                // Calculate the union size for the new subset.
                unionSize = 0 ;
                i = 0 ;
                while( i < grid.cellsInRow ){
                    union[i++] = false ;
                }
                j = 0 ;
                while( j < subsetSize ){
                    i = 0 ;
                    while( i < grid.cellsInRow ){
                        if( ! union[i] && ! numberState.eliminated[linkedValues[j]][s][i] ){
                            union[i] = true ;
                            ++ unionSize ;
                        }
                        ++ i ;
                    }
                    ++ j ;
                }
            }
            ++ s ;
        }
        disjointSubsetsStartSector = 0 ;
        return false ;
    }
    
    /**
     * Checks whether the candidates for a row, box or column are restricted to
     * a single sector, in which case eliminations might be possible.
     * @param sb explanation
     * @return whether eliminations have been performed
     * @throws Exception the grid is in a bad state
     */
    
    boolean singleSectorCandidates( StringBuffer sb ) throws Exception {
        ++ singleSectorCandidatesCalls ;
        CellState cellState = (CellState) lcc.state ;
        NumberState numberState = (NumberState) lcn.state ; 
        boolean anyMoveEliminated ;
        int i , j , s , value , box , row , column , x0 , y0 , xLower , xUpper , yLower , yUpper ;
        value = singleSectorCandidatesStartValue ;
        while( value < grid.cellsInRow ){
            s = 0 ;
            while( s < 2 * grid.cellsInRow ){
                if( numberState.nEliminated[value][s] == grid.cellsInRow ){
                    throw new Exception("Bad grid state");
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
                                anyMoveEliminated = true ;
                                ++ singleSectorCandidatesEliminations ;
                            }
                            ++ y0 ;                                
                        }
                        ++ x0 ;
                    }
                    if( anyMoveEliminated ){
                        if( explain ){
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
                        singleSectorCandidatesStartValue = value ;
                        return true ;
                    }
                }
                ++ s ;
            }
            while( s < 3 * grid.cellsInRow ){
                if( numberState.nEliminated[value][s] == grid.cellsInRow ){
                    throw new Exception("Bad grid state");
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
                            anyMoveEliminated = true ;
                            ++ singleSectorCandidatesEliminations ;
                        }
                        ++ j ;
                    }
                    if( anyMoveEliminated ){
                        if( explain ){
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
                        singleSectorCandidatesStartValue = value ;
                        return true ;
                    }
                }
                ++ s ;
            }
            ++ value ;
        }
        singleSectorCandidatesStartValue = 0 ;
        return false ;
    }
    
    /**
     * Searches for the X-Wings pattern.
     */
    
    boolean xWings( StringBuffer sb ){
        ++ xWingsCalls ;
        int i , j , v , nUnitStrings ; 
        v = 0 ;
        while( v < grid.cellsInRow ){
            nUnitStrings = unitStrings( v ) ;
            i = 0 ;
            while( i < nUnitStrings - 1 ){
                j = i + 1 ;
                while( j < nUnitStrings ){
                    if( xWings( v , i , j , sb ) ){
                        return true ;
                    }
                    ++ j ;
                }
                ++ i ;
            }
            ++ v ;
        }
        return false ;
    }

    /**
     * Finds unit strings - sectors with two candidates for the given value.
     */    

    int unitStrings( int v ){
        int nStrings , s , t0 , t1 , x0 , x1 , y0 , y1 ;
        NumberState numberState = (NumberState) lcn.state ;
        nStrings = 0 ;
        s = 0 ;
        while( s < 3 * grid.cellsInRow && nStrings < maxStrings ){
            if( numberState.nEliminated[v][s] != grid.cellsInRow - 2 ){
                ++ s ;
                continue ;
            }
            t0 = 0 ;
            while( numberState.eliminated[v][s][t0] ){
                ++ t0 ;
            }
            t1 = t0 + 1 ;
            while( numberState.eliminated[v][s][t1] ){
                ++ t1 ;
            }
            if( s < grid.cellsInRow ){
                x0 = x1 = s ;
                y0 = t0 ;
                y1 = t1 ;
            } else if( s < 2 * grid.cellsInRow ){
                x0 = t0 ;
                x1 = t1 ;
                y0 = y1 = s - grid.cellsInRow ;
            } else {                    
                x0 = ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + t0 / grid.boxesDown ;
                x1 = ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + t1 / grid.boxesDown ;
                y0 = ( s - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + t0 % grid.boxesDown ;
                y1 = ( s - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + t1 % grid.boxesDown ;
                if( x0 == x1 || y0 == y1 ){
                    ++ s ;
                    continue ;
                }
            }
            stringR0[nStrings] = x0 ;
            stringC0[nStrings] = y0 ;
            stringR1[nStrings] = x1 ;
            stringC1[nStrings] = y1 ;
            stringLength[nStrings] = 1 ;
            ++ nStrings ;
            ++ s ;
        }
        return nStrings ;
    }
    
    /**
     * Looks for the swordfish pattern.
     */        
    
    boolean swordfish( StringBuffer sb ){
        ++ swordfishCalls ;
        int v , i , j , k , x0 , y0 , x1 , y1 , r0 , c0 , r1 , c1 ;
        int nStrings , nUnitStrings , longStringsBegin , longStringsEnd ;
        v = 0 ;
        while( v < grid.cellsInRow ){
            nStrings = nUnitStrings = unitStrings( v );
            // Attempt to build longer strings.
            longStringsBegin = 0 ;
            longStringsEnd = nStrings ;
            while( longStringsBegin < longStringsEnd ){
                i = 0 ;
                while( i < nUnitStrings ){
                    j = Math.max( 1 + i , longStringsBegin );
                    considerLongStrings:
                    while( j < longStringsEnd && nStrings < maxStrings ){
                        if( stringR0[i] == stringR0[j] && stringC0[i] == stringC0[j] && 
                         !( stringR1[i] == stringR1[j] && stringC1[i] == stringC1[j] ) ){
                            x0 = stringR1[i];
                            y0 = stringC1[i];
                            x1 = stringR1[j];
                            y1 = stringC1[j];
                        } else if( stringR1[i] == stringR1[j] && stringC1[i] == stringC1[j] && 
                                !( stringR0[i] == stringR0[j] && stringC0[i] == stringC0[j] ) ){
                            x0 = stringR0[i];
                            y0 = stringC0[i];
                            x1 = stringR0[j];
                            y1 = stringC0[j];
                        } else if( stringR0[i] == stringR1[j] && stringC0[i] == stringC1[j] && 
                                !( stringR1[i] == stringR0[j] && stringC1[i] == stringC0[j] ) ){
                            x0 = stringR1[i];
                            y0 = stringC1[i];
                            x1 = stringR0[j];
                            y1 = stringC0[j];
                        } else if( stringR1[i] == stringR0[j] && stringC1[i] == stringC0[j] && 
                                !( stringR0[i] == stringR1[j] && stringC0[i] == stringC1[j] ) ){
                            x0 = stringR0[i];
                            y0 = stringC0[i];
                            x1 = stringR1[j];
                            y1 = stringC1[j];
                        } else {
                            ++ j ;
                            continue ;
                        }
                        if( x0 < x1 || x0 == x1 && y0 < y1 ){
                            r0 = x0 ;
                            c0 = y0 ; 
                            r1 = x1 ;
                            c1 = y1 ; 
                        } else {
                            r0 = x1 ;
                            c0 = y1 ; 
                            r1 = x0 ;
                            c1 = y0 ; 
                        }
                        // Check for cyclic dependencies.
                        k = 0 ;
                        while( k < nStrings ){
                            if( r0 == stringR0[k] && c0 == stringC0[k] && r1 == stringR1[k] && c1 == stringC1[k] ){
                                ++ j ;
                                continue considerLongStrings ;
                            }
                            ++ k ;
                        }
                        stringR0[nStrings] = r0 ;
                        stringC0[nStrings] = c0 ; 
                        stringR1[nStrings] = r1 ;
                        stringC1[nStrings] = c1 ; 
                        stringLength[nStrings++] = 1 + stringLength[j];
                        ++ j ;
                    }
                    ++ i ;
                }
                longStringsBegin = longStringsEnd ;
                longStringsEnd = nStrings ;
            }
            if( nStrings == maxStrings ){
                System.err.println("String buffer is full with " + nStrings + " elements");
            }
            // Look for swordfish patterns.
            i = 0 ;
            while( i < nStrings - 1 ){
                if( stringLength[i] % 2 == 0 ){
                    ++ i ;
                    continue ;
                }
                j = i + 1 ;
                while( j < nStrings ){
                    if( stringLength[j] == 1 || stringLength[j] % 2 == 0 ){
                        ++ j ;
                        continue ;
                    }
                    if( xWings( v , i , j , sb ) ){
                        return true ;
                    }                    
                    ++ j ;
                }
                ++ i ;
            }
            ++ v ;
        }
        return false ;
    }

    /**
     * Establishes whether the two two-candidate sector indexed i and j form an
     * X-Wing or Swordfish.
     */
    
    boolean xWings( int v , int i , int j , StringBuffer sb ){
        int k ;
        boolean anyMoveEliminated = false ;
        CellState cellState = (CellState) lcc.state ;
        NumberState numberState = (NumberState) lcn.state ;
        if( stringR0[i] == stringR0[j] && stringC0[i] == stringC0[j] ||
            stringR0[i] == stringR1[j] && stringC0[i] == stringC1[j] ||
            stringR1[i] == stringR0[j] && stringC1[i] == stringC0[j] ||
            stringR1[i] == stringR1[j] && stringC1[i] == stringC1[j] ){
                return anyMoveEliminated ;
        }
        if( stringR0[i] == stringR0[j] && stringR1[i] == stringR1[j] ){
            k = 0 ;
            while( k < grid.cellsInRow ){
                if( k == stringC0[i] || k == stringC0[j] ){
                    ++ k ;
                    continue ;
                }
                if( ! cellState.eliminated[stringR0[i]][k][v] ){
                    numberState.eliminateMove( stringR0[i] , k , v );
                    cellState.eliminateMove( stringR0[i] , k , v );
                    state.eliminateMove( stringR0[i] , k , v );
                    anyMoveEliminated = true ;
                    if( stringLength[i] == 1 && stringLength[j] == 1 ){
                        ++ xWingsEliminations ;
                    } else {
                        ++ swordfishEliminations ;
                    }
                }
                ++ k ;
            }
            k = 0 ;
            while( k < grid.cellsInRow ){
                if( k == stringC1[i] || k == stringC1[j] ){
                    ++ k ;
                    continue ;
                }
                if( ! cellState.eliminated[stringR1[i]][k][v] ){
                    numberState.eliminateMove( stringR1[i] , k , v );
                    cellState.eliminateMove( stringR1[i] , k , v );
                    state.eliminateMove( stringR1[i] , k , v );
                    anyMoveEliminated = true ;
                    if( stringLength[i] == 1 && stringLength[j] == 1 ){
                        ++ xWingsEliminations ;
                    } else {
                        ++ swordfishEliminations ;
                    }
                }
                ++ k ;
            }
        }
        if( stringC0[i] == stringC0[j] && stringC1[i] == stringC1[j] ){
            k = 0 ;
            while( k < grid.cellsInRow ){
                if( k == stringR0[i] || k == stringR0[j] ){
                    ++ k ;
                    continue ;
                }
                if( ! cellState.eliminated[k][stringC0[i]][v] ){
                    numberState.eliminateMove( k , stringC0[i] , v );
                    cellState.eliminateMove( k , stringC0[i] , v );
                    state.eliminateMove( k , stringC0[i] , v );
                    anyMoveEliminated = true ;
                    if( stringLength[i] == 1 && stringLength[j] == 1 ){
                        ++ xWingsEliminations ;
                    } else {
                        ++ swordfishEliminations ;
                    }
                }
                ++ k ;
            }
            k = 0 ;
            while( k < grid.cellsInRow ){
                if( k == stringR1[i] || k == stringR1[j] ){
                    ++ k ;
                    continue ;
                }
                if( ! cellState.eliminated[k][stringC1[i]][v] ){
                    numberState.eliminateMove( k , stringC1[i] , v );
                    cellState.eliminateMove( k , stringC1[i] , v );
                    state.eliminateMove( k , stringC1[i] , v );
                    anyMoveEliminated = true ;
                    if( stringLength[i] == 1 && stringLength[j] == 1 ){
                        ++ xWingsEliminations ;
                    } else {
                        ++ swordfishEliminations ;
                    }
                }
                ++ k ;
            }
        }                    
        if( anyMoveEliminated ){
            if( explain ){
                sb.append( 1 + v );
                sb.append("s must appear in the cells (");
                sb.append( 1 + stringR0[i] );
                sb.append(",");
                sb.append( 1 + stringC0[i] );
                sb.append(") and (");
                sb.append( 1 + stringR1[j] );
                sb.append(",");
                sb.append( 1 + stringC1[j] );
                sb.append(") or the cells (");
                sb.append( 1 + stringR0[j] );
                sb.append(",");
                sb.append( 1 + stringC0[j] );
                sb.append(") and (");
                sb.append( 1 + stringR1[i] );
                sb.append(",");
                sb.append( 1 + stringC1[i] );
                sb.append("). [");
                if( stringLength[j] == 1 ){
                    sb.append("X-Wing");
                } else {
                    sb.append( stringLength[j] );
                    sb.append("-leg Swordfish");
                }
                sb.append("]\n");                            
            }
            return true ;
        }
        return false ;
    }
            
    /**
     * Searches for moves that would make it impossible to place the remaining values.
     * @param sb explanation
     * @return whether eliminations have been performed
     * @throws Exception the grid is in a bad state
     */
    
    boolean nishio( StringBuffer sb ){
        ++ nishioCalls ;
        boolean anyMoveEliminated , candidateNominated ;
        CellState cellState = (CellState) lcc.state ;
        NumberState numberState = (NumberState) lcn.state ; 
        int i , j , v , r , c , box , x0 , y0 , xLower , xUpper , yLower , yUpper ;
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
                                anyMoveEliminated = true ;
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
                                anyMoveEliminated = true ;
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
                                anyMoveEliminated = true ;
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
                        ++ nishioEliminations ;
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
                        return true ;
                    }
                    ++ j ;
                }
                ++ i ;
            }
            ++ v ;
        }
        return false ;
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
        values[nMoves] = value - 1 ;
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
        if( newNMoves < 0 ){
            return false ;
        }
        // Unwind thread.
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
		lcn.unwind( newNMoves , false );
        lcc.unwind( newNMoves , false );
        // Remove the most recent moves from the grid.
        if( reset ){
            int i = newNMoves ;
            while( i < nMoves ){
                grid.data[xMoves[i]][yMoves[i]] = 0 ;
                ++ i ;
            }
        }
        nMoves = newNMoves ;
        return true ;
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
