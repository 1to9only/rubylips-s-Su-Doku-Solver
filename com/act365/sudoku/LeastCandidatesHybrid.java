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
 * LeastCandidatesHybrid combines the Least Candidates Cell and Least 
 * Candidates Number strategies. 
 */

public class LeastCandidatesHybrid extends StrategyBase implements IStrategy {

    // Link types 
    
    final static int NONE = 0 ,
                     MATCH = 1 ,
                     STRONG = 2 ,
                     WEAK = 3 ,
                     CELL = 4 ;
                     
    final static int LEFT_LEFT   = 0 ,
                     LEFT_RIGHT  = 1 ,
                     RIGHT_LEFT  = 2 ,
                     RIGHT_RIGHT = 3 ;
    
    // End-point booleans for weak linkage
    
    final static int DONT_KNOW     = -1 ,
                     FALSE         = 0 ,
                     TRUE          = 1 ,
                     CONTRADICTION = 2 ;
    
    // Forced chain actions.
    
    final static int NO_ACTION = 0 ,
                     ELIMINATE = 1 ,
                     ADD_CHAIN = 2 ;
                     
    // Members
    
    LeastCandidatesNumber lcn ;
    
    LeastCandidatesCell lcc ;

    IStrategy better ;

    InvulnerableState invulnerableState ;
    
    LinearSystemState linearSystemState ;
    
    boolean useDisjointSubsets ,
            useSingleSectorCandidates ,
            useSingleValuedChains ,
            useManyValuedChains ,
            useNishio ,
            useGuesses ,
            checkInvulnerable ;
            
    int singleCandidatureCalls ,
        disjointSubsetsCalls ,
        disjointSubsetsEliminations ,
        maxDisjointSubsetsSize ,
        singleSectorCandidatesCalls ,
        singleSectorCandidatesEliminations ,
        chainsEliminations ,
        singleValuedChainsCalls ,
        singleValuedChainsEliminations ,
        manyValuedChainsCalls ,
        manyValuedChainsEliminations ,
        nishioCalls ,
        nishioEliminations ,
        nGuesses ,
        maxChains ,
        maxChainLength ,
        nEliminated ;
    
    byte[] eliminatedX ,
           eliminatedY ,
           eliminatedValues ;
          
    // Arrays defined as members in order to improve performance.

    transient byte[] x , y , linkedValues , linkedCells ;
    
    transient byte[] chainR0 , 
                     chainC0 , 
                     chainR1 , 
                     chainC1 ,
                     chainV0 ,
                     chainV1 ,
                     chainLength ,
                     chainNComponents ;
    
    transient byte[][] chainOtherEnd0 ,
                       chainOtherEnd1 ;

    transient boolean[][] isLinkAscending ;
    
    transient boolean[] union ,
                        isLinkStrong ;
            
    transient int[][] mask ,
                      chainRoute ,
                      chainComponents ; 

    /**
     * Sets up a LeastCandidatesHybrid II strategy with an optional random element.
     * @param randomize whether the final candidates should be chosen randomly from its peers
     * @param checkInvulnerable indicates whether the moves should be post-filtered using the Invulnerable state grid.
     * @param useAllLogicalMethods whether the solver should look for X-Wings and Nishio
     * @param explain whether explanatory debug should be produced
     */    
    
    public LeastCandidatesHybrid( boolean randomize , 
                                  boolean checkInvulnerable ,
                                  boolean useAllLogicalMethods ,
                                  boolean explain ){
        super( randomize , explain );
        lcn = new LeastCandidatesNumber( randomize || checkInvulnerable , randomize , explain );
        lcc = new LeastCandidatesCell( randomize || checkInvulnerable , randomize , explain );        
        if( ( this.checkInvulnerable = checkInvulnerable ) ){
            invulnerableState = new InvulnerableState();
            linearSystemState = new LinearSystemState();
        }
        useDisjointSubsets = true ;
        useSingleSectorCandidates = true ;
        useSingleValuedChains = useManyValuedChains = useNishio = useAllLogicalMethods ;
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
        if( checkInvulnerable ){
            invulnerableState.setup( grid.boxesAcross , grid.boxesDown );
            linearSystemState.setup( grid.boxesAcross , grid.boxesDown );
            int i , j ;
            i = 0 ;
            while( i < grid.cellsInRow ){
                j = 0 ;
                while( j < grid.cellsInRow ){
                    if( grid.data[i][j] > 0 ){
                        invulnerableState.addMove( i , j , grid.data[i][j] - 1 );
                        linearSystemState.addMove( i , j , grid.data[i][j] - 1 );
                    }
                    ++ j ;
                }
                ++ i ;
            }
            if( explain ){
                invulnerableState.pushState( 0 );
//                linearSystemState.pushState( 0 );
                eliminatedX = new byte[grid.cellsInRow];
                eliminatedY = new byte[grid.cellsInRow];
                eliminatedValues = new byte[grid.cellsInRow];
            }
            if( useDisjointSubsets ){
                x = new byte[grid.cellsInRow];
                y = new byte[grid.cellsInRow];
                linkedValues = new byte[grid.cellsInRow];
                linkedCells = new byte[grid.cellsInRow];
                union = new boolean[grid.cellsInRow];
            }
            if( useSingleValuedChains || useManyValuedChains ){
                // The following array size isn't a theoretical upper limit 
                // but should prove adequate.
                maxChains = grid.cellsInRow * grid.cellsInRow * grid.cellsInRow * grid.cellsInRow ;
                maxChainLength = 20 ; // Could be made final
                chainR0 = new byte[maxChains];
                chainC0 = new byte[maxChains];
                chainR1 = new byte[maxChains];
                chainC1 = new byte[maxChains];
                chainV0 = new byte[maxChains];
                chainV1 = new byte[maxChains];
                chainLength = new byte[maxChains];
                chainOtherEnd0 = new byte[maxChains][2];
                chainOtherEnd1 = new byte[maxChains][2];
                isLinkStrong = new boolean[maxChains];
                
                if( explain ){
                    chainRoute = new int[maxChains][maxChainLength];
                    isLinkAscending = new boolean[maxChains][maxChainLength];
                    chainNComponents = new byte[maxChains];
                    chainComponents = new int[maxChains][maxChainLength];
                }
            }
            if( useNishio ){
                mask = new int[grid.cellsInRow][grid.cellsInRow];
            }
        }
        maxDisjointSubsetsSize = explain ? grid.cellsInRow : 6 /* Rule-of-thumb */ ;
        singleCandidatureCalls = 0 ;
        disjointSubsetsCalls = disjointSubsetsEliminations = 0 ;
        singleSectorCandidatesCalls = singleSectorCandidatesEliminations = 0 ;
        singleValuedChainsCalls = singleValuedChainsEliminations = 0 ;
        manyValuedChainsCalls = manyValuedChainsEliminations = 0 ;
        nishioCalls = nishioEliminations = 0 ;
        nGuesses = 0 ;
        nEliminated = 0 ;
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
        if( checkInvulnerable && score > 1 ){
            nEliminated = 0 ;
            try {
               while( true ){
                    if( useSingleSectorCandidates && singleSectorCandidates( sb ) ){
                        if( explain && nEliminated > 0 ){
                            appendEliminations( sb );
                        }
                        if( singleCandidature() ){
                            break ;
                        } else {
                            continue ;
                        }
                    }
                    if( useDisjointSubsets && disjointSubsets( sb ) ){
                        if( explain && nEliminated > 0 ){
                            appendEliminations( sb );
                        }
                        if( singleCandidature() ){
                            break ;
                        } else {
                            continue ;
                        }
                    }
                    if( useSingleValuedChains && singleValuedChains( sb ) ){
                        if( explain && nEliminated > 0 ){
                            appendEliminations( sb );
                        }
                        if( singleCandidature() ){
                            break ;
                        } else {
                            continue ;
                        }
                    }
                   if( useManyValuedChains && manyValuedChains( sb ) ){
                       if( explain && nEliminated > 0 ){
                           appendEliminations( sb );
                       }
                       if( singleCandidature() ){
                           break ;
                       } else {
                           continue ;
                       }
                   }
                   if( useNishio && nishio( sb ) ){
                       if( explain && nEliminated > 0 ){
                           appendEliminations( sb );
                       }
                       if( singleCandidature() ){
                           break ;
                       } else {
                           continue ;
                       }
                   }
                   break ;
                }
            } catch ( Exception e ) {
                e.printStackTrace(); // Temp
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
        
        if( checkInvulnerable ){
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
     * Eliminates the move (x,y):=v from all state grids.
     */

    void eliminateMove( int x , int y , int v ){
        ((NumberState) lcn.state ).eliminateMove( x , y , v );
        ((CellState) lcc.state ).eliminateMove( x , y , v );
        invulnerableState.eliminateMove( x , y , v );
        linearSystemState.eliminateMove( x , y , v );
        if( explain ){
            eliminatedX[nEliminated] = (byte) x ;
            eliminatedY[nEliminated] = (byte) y ;
            eliminatedValues[nEliminated] = (byte) v ;
            ++ nEliminated ;            
        }
    }
    
    /**
     * Adds the move (x,y):=v to all state grids.
     */

    int addMove( int x , int y , int v ) {
        CellState cellState = (CellState) lcc.state ; 
        int i = 0 , nEliminated = 0 ;
        while( i < grid.cellsInRow ){
            if( i != v && ! cellState.eliminated[x][y][i] ){
                eliminateMove( x , y , i );
                ++ nEliminated ;
            }
            ++ i ;
        }
        return nEliminated ;
    }
    
    /**
     * Appends a summary of moves eliminated in the current 
     * cycle to the given string buffer.
     */

    void appendEliminations( StringBuffer sb ){
        sb.append("- The move");
        if( nEliminated > 1 ){
            sb.append("s");
        }
        int i = 0 ;
        while( i < nEliminated ){
            if( i == 0 ){
                sb.append(" (");
            } else if( i < nEliminated - 1 ) {
                sb.append(", (");
            } else {
                sb.append(" and (");
            }
            sb.append( 1 + eliminatedX[i] );
            sb.append(",");
            sb.append( 1 + eliminatedY[i] );
            sb.append("):=");
            sb.append( SuDokuUtils.toString( 1 + eliminatedValues[i] ));
            ++ i ;
        }
        sb.append(" ha");
        if( nEliminated == 1 ){
            sb.append("s");
        } else {
            sb.append("ve");
        }
        sb.append(" been eliminated.\n");
        nEliminated = 0 ;
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
                            linkedCells[i++] = (byte) j ;
                        }
                        ++ j ;
                    }
                    i = 0 ;
                    while( i < subsetSize ){
                        if( s < grid.cellsInRow ){
                            x[i] = (byte) s ;
                            y[i] = linkedCells[i] ;
                        } else if( s < 2 * grid.cellsInRow ){
                            x[i] = linkedCells[i] ;
                            y[i] = (byte)( s - grid.cellsInRow );
                        } else {
                            x[i] = (byte)( ( s - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + linkedCells[i] / grid.boxesDown );
                            y[i] = (byte)( ( s - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + linkedCells[i] % grid.boxesDown );
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
                            eliminateMove( x[i] , y[i] , j );
                            anyMoveEliminated = true ;
                            ++ disjointSubsetsEliminations ; 
                            ++ j ;
                        }
                        ++ i ;
                    }
                    if( anyMoveEliminated ){
                        if( explain ){
                            sb.append("The values ");
                            sb.append( SuDokuUtils.toString( 1 + linkedValues[0] ) );
                            i = 1 ;
                            while( i < subsetSize - 1 ){
                                sb.append(", ");
                                sb.append( SuDokuUtils.toString( 1 + linkedValues[i++] ) );
                            }
                            sb.append(" and ");
                            sb.append( SuDokuUtils.toString( 1 + linkedValues[i] ) );
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
                    linkedValues[subsetSize] = (byte)( linkedValues[subsetSize-1] + 1 );
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
        value = 0 ;
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
                                eliminateMove( x0 , y0 , value );
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
                            sb.append( SuDokuUtils.toString( 1 + value ) );
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
                            eliminateMove( x0 , y0 , value );
                            anyMoveEliminated = true ;
                            ++ singleSectorCandidatesEliminations ;
                        }
                        ++ j ;
                    }
                    if( anyMoveEliminated ){
                        if( explain ){
                            sb.append("The value ");
                            sb.append( SuDokuUtils.toString( 1 + value ) );
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
                        return true ;
                    }
                }
                ++ s ;
            }
            ++ value ;
        }
        return false ;
    }
    
    boolean singleValuedChains( StringBuffer sb ) throws Exception {
        boolean anyMoveEliminated = false ;
        int v ;
        ++ singleValuedChainsCalls ;
        v = 0 ;
        while( ! anyMoveEliminated && v < grid.cellsInRow ){
            chainsEliminations = 0 ;
            anyMoveEliminated = longChains( sb , unitChains( v , 0 ) );
            singleValuedChainsEliminations += chainsEliminations ; 
            ++ v ;   
        }
        return anyMoveEliminated ;
    }
    
    boolean manyValuedChains( StringBuffer sb ) throws Exception {
        boolean anyMoveEliminated = false ;
        int v = 0 , nChains = 0 ;
        ++ manyValuedChainsCalls ;
        while( v < grid.cellsInRow ){
            nChains = unitChains( v , nChains );
            ++ v ;   
        }
        chainsEliminations = 0 ;
        anyMoveEliminated = longChains( sb , nChains );
        manyValuedChainsEliminations += chainsEliminations ;
        return anyMoveEliminated ;           
    }
    
    /**
     * Finds unit chains.
     */    

    int unitChains( int v , int nChains ){
        int s , t0 , t1 , x0 , x1 , y0 , y1 ;
        NumberState numberState = (NumberState) lcn.state ;
        s = 0 ;
        considerSector :
        while( s < 3 * grid.cellsInRow && nChains < maxChains ){
            if( numberState.nEliminated[v][s] == grid.cellsInRow - 1 ){
                ++ s ;
                continue ;
            }
            t0 = 0 ;
            while( t0 < grid.cellsInRow ){
                while( t0 < grid.cellsInRow && numberState.eliminated[v][s][t0] ){
                    ++ t0 ;
                }
                if( t0 == grid.cellsInRow ){
                    continue ;
                }
                t1 = t0 + 1 ;
                while( t1 < grid.cellsInRow ){
                    while( t1 < grid.cellsInRow && numberState.eliminated[v][s][t1] ){
                        ++ t1 ;
                    }
                    if( t1 == grid.cellsInRow ){
                        continue ;
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
                            continue considerSector ;
                        }
                    }
                    chainR0[nChains] = (byte) x0 ;
                    chainC0[nChains] = (byte) y0 ;
                    chainR1[nChains] = (byte) x1 ;
                    chainC1[nChains] = (byte) y1 ;
                    chainV0[nChains] = chainV1[nChains] = (byte) v ;
                    chainLength[nChains] = 1 ;
                    if( ( isLinkStrong[nChains] = numberState.nEliminated[v][s] == grid.cellsInRow - 2 ) ){
                        chainOtherEnd0[nChains][FALSE] = chainOtherEnd1[nChains][FALSE] = TRUE ;
                        chainOtherEnd0[nChains][TRUE]  = chainOtherEnd1[nChains][TRUE]  = FALSE ;
                    } else {
                        chainOtherEnd0[nChains][FALSE] = chainOtherEnd1[nChains][FALSE] = DONT_KNOW ;
                        chainOtherEnd0[nChains][TRUE]  = chainOtherEnd1[nChains][TRUE]  = FALSE ;
                    }
                    if( explain && maxChainLength >= 1 ){
                        chainRoute[nChains][0] = nChains ;
                        isLinkAscending[nChains][0] = true ;
                        chainNComponents[nChains] = 1 ;
                        chainComponents[nChains][0] = nChains ;
                    }
                    ++ nChains ;
                    ++ t1 ;
                }
                ++ t0 ;
            }
            ++ s ;
        }
        return nChains ;
    }
    
    /**
     * Establishes whether there is a strong link at the point (r,c)
     * for the values v0 and v1. 
     */

    boolean strongLink( int r , int c , int v0 , int v1 ){
        if( v0 == v1 ){
            return true ;   
        } else {
            CellState cellState = (CellState) lcc.state ;
            return cellState.nEliminated[r][c] == grid.cellsInRow - 2 && 
                 ! cellState.eliminated[r][c][v0] && 
                 ! cellState.eliminated[r][c][v1] ;               
        }
    }
    
    /**
     * Establishes whether there is a weak link between the points (r0,c0)
     * and (r1,c1) for the values v0 and v1. 
     */

    boolean weakLink( int r0 , int c0 , int v0 , int r1 , int c1 , int v1 ){
        if( v0 == v1 ){
            NumberState numberState = (NumberState) lcn.state ;
            if( r0 == r1 ){
                return numberState.nEliminated[v0][r0] < grid.cellsInRow - 2 ;
            } else if( c0 == c1 ) {
                return numberState.nEliminated[v0][grid.cellsInRow+c0] < grid.cellsInRow - 2 ;
            } else {
                final int b0 = r0 / grid.boxesAcross * grid.boxesAcross + c0 / grid.boxesDown ,
                          b1 = r1 / grid.boxesAcross * grid.boxesAcross + c1 / grid.boxesDown ;
                return b0 == b1 && numberState.nEliminated[v0][2*grid.cellsInRow+b0] < grid.cellsInRow - 2 ;                    
            }
        } else if( r0 == r1 && c0 == c1 ){
            return cellLink( r0 , c0 , v0 , v1 );
        } else {
            return false ;
        }
    }
    
    /**
     * Establishes whether there is a weak cell link at the point (r,c).
     */

    boolean cellLink( int r , int c , int v0 , int v1 ){
        if( v0 != v1 ){
            CellState cellState = (CellState) lcc.state ;
            return cellState.nEliminated[r][c] < grid.cellsInRow - 2 && 
                 ! cellState.eliminated[r][c][v0] && 
                 ! cellState.eliminated[r][c][v1] ;                                           
        }
        return false ;
    }

    /**
     * Establishes the type of link (if any) that exists between two given strings.
     */

    int linkType( int i , int j ){
        if( chainR0[i] == chainR0[j] && chainC0[i] == chainC0[j] &&
            chainR1[i] == chainR1[j] && chainC1[i] == chainC1[j] ){
                return 4 * MATCH ;
        } else if( chainR0[i] == chainR0[j] && chainC0[i] == chainC0[j] && 
                !( chainR1[i] == chainR1[j] && chainC1[i] == chainC1[j] ) ){
            if( strongLink( chainR0[i] , chainC0[i] , chainV0[i] , chainV0[j] ) ){
                return 4 * STRONG + LEFT_LEFT ;
            } else if( cellLink( chainR0[i] , chainC0[i] , chainV0[i] , chainV0[j] ) ){
                return 4 * CELL + LEFT_LEFT ;
            } else {
                return NONE ;   
            }
        } else if( chainR1[i] == chainR1[j] && chainC1[i] == chainC1[j] && 
                !( chainR0[i] == chainR0[j] && chainC0[i] == chainC0[j] ) ){
            if( strongLink( chainR1[i] , chainC1[i] , chainV1[i] , chainV1[j] ) ){
                return 4 * STRONG + RIGHT_RIGHT ;   
            } else if( cellLink( chainR1[i] , chainC1[i] , chainV1[i] , chainV1[j] ) ){
                return 4 * CELL + RIGHT_RIGHT ;
            } else {
                return NONE ;   
            }
        } else if( chainR0[i] == chainR1[j] && chainC0[i] == chainC1[j] && 
                !( chainR1[i] == chainR0[j] && chainC1[i] == chainC0[j] ) ){
            if( strongLink( chainR0[i] , chainC0[i] , chainV0[i] , chainV1[j] ) ){
                return 4 * STRONG + LEFT_RIGHT ;   
            } else if( cellLink( chainR0[i] , chainC0[i] , chainV0[i] , chainV1[j] ) ){
                return 4 * CELL + LEFT_RIGHT ;
            } else {
                return NONE ;   
            }
        } else if( chainR1[i] == chainR0[j] && chainC1[i] == chainC0[j] && 
                !( chainR0[i] == chainR1[j] && chainC0[i] == chainC1[j] ) ){
            if( strongLink( chainR1[i] , chainC1[i] , chainV1[i] , chainV0[j] ) ){
                return 4 * STRONG + RIGHT_LEFT ;   
            } else if( cellLink( chainR1[i] , chainC1[i] , chainV1[i] , chainV0[j] ) ){
                return 4 * CELL + RIGHT_LEFT ;
            } else {
                return NONE ;   
            }    
        } else {
            return NONE ;
        }   
    }

    /**
     * Constructs weakly-linked strings.
     */
    
    boolean longChains( StringBuffer sb , int nChains ){
        int i , j , type , chainsBegin , chainsEnd ;
        chainsBegin = 0 ;
        chainsEnd   = nChains ;
        while( chainsBegin < chainsEnd ){
            // Test for linkage to others.            
            i = 0 ;
            while( i < chainsEnd ){
                j = Math.max( i + 1 , chainsBegin );
                while( j < chainsEnd ){
                    type = linkType( i , j );
                    if( type / 4 == STRONG || type / 4 == CELL ){
                        if( ( nChains = connect( i , j , type , nChains ) ) == maxChains ){
                            System.err.println("Chain buffer is full with " + nChains + " elements");
                            return false ;
                        }                  
                        // Test for weak linkage to self.
                        if( weakLink( chainR0[nChains-1] , chainC0[nChains-1] , chainV0[nChains-1] , chainR1[nChains-1] , chainC1[nChains-1] , chainV1[nChains-1] ) && cyclicChain( nChains - 1 , sb ) ){
                            return true ;
                        }
                    } else if( type / 4 == MATCH ){
                        switch( forcedChains( i , j , sb , nChains ) ){
                            case ELIMINATE:
                                return true ;
                            case ADD_CHAIN:
                                ++ nChains ;
                                // Test for weak linkage to self.
                                if( weakLink( chainR0[nChains-1] , chainC0[nChains-1] , chainV0[nChains-1] , chainR1[nChains-1] , chainC1[nChains-1] , chainV1[nChains-1] ) && cyclicChain( nChains - 1 , sb ) ){
                                    return true ;
                                }
                        }
                    }                    
                    ++ j ;
                }
                ++ i ;
            }
            // Consider any newly-created links.
            chainsBegin = chainsEnd ;
            chainsEnd = nChains ;
        }
        return false ;
    }
    
    /**
     * Tests for forced chains.
     */

    int forcedChains( int i , int j , StringBuffer sb , int nChains ){
/*        
        if( chainV0[i] != chainV0[j] || chainV1[i] != chainV1[j] ){
            return NO_ACTION ;
        }
*/        
        boolean contains , contradiction , regardless ;
        int r0 , c0 , v0 , n0 , r1 , c1 , v1 , n1 , mappedBoolean ;
        int otherEnd0True , otherEnd0False , otherEnd1True , otherEnd1False ;
        CellState cellState = (CellState) lcc.state ;
        
        // Map the logic table for chain i onto chain j.
        
        n0 = grid.cellsInRow - cellState.nEliminated[chainR0[i]][chainC0[i]];
        n1 = grid.cellsInRow - cellState.nEliminated[chainR1[i]][chainC1[i]];
        
        if( ( mappedBoolean = mapBoolean( TRUE , chainV0[i] , chainV0[j] , n0 ) ) != DONT_KNOW ){
            otherEnd0True = sumBooleans( mapBoolean( chainOtherEnd0[i][mappedBoolean] , chainV1[i] , chainV1[j] , n1 ) , chainOtherEnd0[j][TRUE] );
        } else {
            otherEnd0True = chainOtherEnd0[j][TRUE] ;            
        }
        if( ( mappedBoolean = mapBoolean( FALSE , chainV0[i] , chainV0[j] , n0 ) ) != DONT_KNOW ){
            otherEnd0False = sumBooleans( mapBoolean( chainOtherEnd0[i][mappedBoolean] , chainV1[i] , chainV1[j] , n1 ) , chainOtherEnd0[j][FALSE] );
        } else {
            otherEnd0False = chainOtherEnd0[j][FALSE] ;            
        }
        if( ( mappedBoolean = mapBoolean( TRUE , chainV1[i] , chainV1[j] , n1 ) ) != DONT_KNOW ){
            otherEnd1True = sumBooleans( mapBoolean( chainOtherEnd1[i][mappedBoolean] , chainV0[i] , chainV0[j] , n0 ) , chainOtherEnd1[j][TRUE] );
        } else {
            otherEnd1True = chainOtherEnd1[j][TRUE] ;            
        }
        if( ( mappedBoolean = mapBoolean( FALSE , chainV1[i] , chainV1[j] , n1 ) ) != DONT_KNOW ){
            otherEnd1False = sumBooleans( mapBoolean( chainOtherEnd1[i][mappedBoolean] , chainV0[i] , chainV0[j] , n0 ) , chainOtherEnd1[j][FALSE] );
        } else {
            otherEnd1False = chainOtherEnd1[j][FALSE] ;            
        }

        // Check whether any new infomation has been made available.
        if( otherEnd0True == chainOtherEnd0[j][TRUE] && 
            otherEnd0False == chainOtherEnd0[j][FALSE] &&
            otherEnd1True == chainOtherEnd1[j][TRUE] &&
            otherEnd1False == chainOtherEnd1[j][FALSE] ){
            return NO_ACTION ;
        }

        // Process the new information.
        if( otherEnd0True == CONTRADICTION ){
            contradiction = true ;
            contains = true ;
            r0 = chainR0[j];
            c0 = chainC0[j];
            v0 = chainV0[j];
            r1 = chainR1[j];
            c1 = chainC1[j];
            v1 = chainV1[j];
        } else if( otherEnd0False == CONTRADICTION ) {
            contradiction = true ;
            contains = false ;
            r0 = chainR0[j];
            c0 = chainC0[j];
            v0 = chainV0[j];
            r1 = chainR1[j];
            c1 = chainC1[j];
            v1 = chainV1[j];
        } else if( otherEnd1True == CONTRADICTION ) {
            contradiction = true ;
            contains = true ;
            r0 = chainR1[j];
            c0 = chainC1[j];
            v0 = chainV1[j];
            r1 = chainR0[j];
            c1 = chainC0[j];
            v1 = chainV0[j];
        } else if( otherEnd1False == CONTRADICTION ){
            contradiction = true ;
            contains = false ;
            r0 = chainR1[j];
            c0 = chainC1[j];
            v0 = chainV1[j];
            r1 = chainR0[j];
            c1 = chainC0[j];
            v1 = chainV0[j];
        } else {
            contradiction = false ;
            contains = false ;
            r0 = c0 = v0 = r1 = c1 = v1 = 0 ;
        }
        if( contradiction ){        
            if( explain ){
                sb.append("Consider the chains ");
                appendChain( sb , j );
                sb.append(" and ");
                appendChain( sb , i );
                sb.append(".\nWhen the cell (");
                sb.append( 1 + r0 );
                sb.append(",");
                sb.append( 1 + c0 );
                sb.append(") ");
                if( contains ){
                    sb.append("contains");
                } else {
                    sb.append("doesn't contain");
                }
                sb.append(" the value ");
                sb.append( SuDokuUtils.toString( 1 + v0 ) );
                sb.append(", one chain states that the cell (");
                sb.append( 1 + r1 );
                sb.append(",");
                sb.append( 1 + c1 );
                sb.append(") contains the value ");
                sb.append( SuDokuUtils.toString( 1 + v1 ) );
                sb.append(" while the other says it doesn't - a situation that is clearly illegal.\nTherefore, the cell (");
                sb.append( 1 + r0 );
                sb.append(",");
                sb.append( 1 + c0 );
                sb.append(") ");
                if( contains ){
                    sb.append("cannot");
                } else {
                    sb.append("must");
                }
                sb.append(" contain the value ");
                sb.append( SuDokuUtils.toString( 1 + v0 ) );
                sb.append(".\n");
            }
            if( contains ){
                eliminateMove( r0 , c0 , v0 );
                ++ chainsEliminations ;
            } else {
                chainsEliminations += addMove( r0 , c0 , v0 );
            }
            return ELIMINATE ;            
        }                
        if( otherEnd0True != DONT_KNOW && otherEnd0True == otherEnd0False ){
            r0 = chainR0[j];
            c0 = chainC0[j];
            v0 = chainV0[j];
            r1 = chainR1[j];
            c1 = chainC1[j];
            v1 = chainV1[j];
            contains = otherEnd0True == TRUE ;
            regardless = true ;
        } else if( otherEnd1True != DONT_KNOW && otherEnd1True == otherEnd1False ) {
            r0 = chainR1[j];
            c0 = chainC1[j];
            v0 = chainV1[j];
            r1 = chainR0[j];
            c1 = chainC0[j];
            v1 = chainV0[j];
            contains = otherEnd1True == TRUE ;
            regardless = true ;
        } else {
            regardless = false ;
        }
        if( regardless ){
            if( explain ){
                sb.append("Consider the chains ");
                appendChain( sb , j );
                sb.append(" and ");
                appendChain( sb , i );
                sb.append(".\nRegardless of whether the cell (");
                sb.append( 1 + r0 );
                sb.append(",");
                sb.append( 1 + c0 );
                sb.append(") contains the value ");
                sb.append( SuDokuUtils.toString( 1 + v0 ) );
                sb.append(", the cell (");
                sb.append( 1 + r1 );
                sb.append(",");
                sb.append( 1 + c1 );
                sb.append(") ");
                if( contains ){
                    sb.append("contains ");
                } else {
                    sb.append("doesn't contain ");
                }
                sb.append("the value ");
                sb.append( SuDokuUtils.toString( 1 + v1 ) );
                sb.append(".\n");
                    
            }        
            if( contains ){
                chainsEliminations += addMove( r1 , c1 , v1 );
            } else {
                eliminateMove( r1 , c1 , v1 );
                ++ chainsEliminations ;
            }
            return ELIMINATE ;
        }
        // When the logic table for the combined chain is superior to that for
        // the constituent chains, store a new chain.
        if( ( otherEnd0True != chainOtherEnd0[i][TRUE] || otherEnd0False != chainOtherEnd0[i][FALSE] ||
              otherEnd1True != chainOtherEnd1[i][TRUE] || otherEnd1False != chainOtherEnd1[i][FALSE] ) &&
            ( otherEnd0True != chainOtherEnd0[j][TRUE] || otherEnd0False != chainOtherEnd0[j][FALSE] ||
              otherEnd1True != chainOtherEnd1[j][TRUE] || otherEnd1False != chainOtherEnd1[j][FALSE] ) ){

            chainR0[nChains] = chainR0[i];
            chainC0[nChains] = chainC0[i];
            chainV0[nChains] = chainV0[i];
            chainOtherEnd0[nChains][TRUE] = (byte) otherEnd0True;   
            chainOtherEnd0[nChains][FALSE] = (byte) otherEnd0False;   
            chainR1[nChains] = chainR1[i];
            chainC1[nChains] = chainC1[i];
            chainV1[nChains] = chainV1[i];
            chainOtherEnd1[nChains][TRUE] = (byte) otherEnd1True;   
            chainOtherEnd1[nChains][FALSE] = (byte) otherEnd1False;   
            chainLength[nChains] = chainLength[i];
            // Record the route.
            if( explain && chainLength[nChains] <= maxChainLength ){
                chainNComponents[nChains] = (byte)( chainNComponents[i] + chainNComponents[j] );
                int k = 0 ;
                while( k < chainNComponents[i] ){
                    chainComponents[nChains][k] = chainComponents[i][k];
                    ++ k ;
                }
                while( k < chainNComponents[nChains] ){
                    chainComponents[nChains][k] = chainComponents[j][k-chainNComponents[i]];
                    ++ k ;
                }
            }

            return ADD_CHAIN ;                 
        }
                      
        return NO_ACTION ;
    }
    
    /**
     * Tests for a cyclic chain at position s.
     */

    boolean cyclicChain( int s , StringBuffer sb ){   
        boolean anyValueEliminated = false ;
        if( chainOtherEnd0[s][TRUE]  == TRUE ||
            chainOtherEnd0[s][FALSE] == TRUE ||
            chainOtherEnd1[s][TRUE]  == TRUE ||
            chainOtherEnd1[s][FALSE] == TRUE ){
            if( explain ){
                sb.append("Consider the chain ");
                appendChain( sb , s );
                sb.append(".\n");
            }
            if( chainOtherEnd0[s][TRUE] == TRUE ){
                anyValueEliminated |= cyclicElimination1( sb , chainR0[s] , chainC0[s] , chainV0[s] , chainR1[s] , chainC1[s] , chainV1[s] );
            }
            if( chainOtherEnd1[s][TRUE] == TRUE ){
                anyValueEliminated |= cyclicElimination1( sb , chainR1[s] , chainC1[s] , chainV1[s] , chainR0[s] , chainC0[s] , chainV0[s] );
            }                
            if( chainOtherEnd0[s][FALSE] == TRUE ){
                anyValueEliminated |= cyclicElimination2( sb , chainR0[s] , chainC0[s] , chainV0[s] , chainR1[s] , chainC1[s] , chainV1[s] );
            } else if( chainOtherEnd1[s][FALSE] == TRUE ){
                anyValueEliminated |= cyclicElimination2( sb , chainR1[s] , chainC1[s] , chainV1[s] , chainR0[s] , chainC0[s] , chainV0[s] );
            }                
        }
        return anyValueEliminated ;
    }
    
    boolean cyclicElimination1( StringBuffer sb , int r0 , int c0 , int v0 , int r1 , int c1 , int v1 ){
        if( explain ){
            sb.append("When the cell (");
            sb.append( 1 + r0 );
            sb.append(",");
            sb.append( 1 + c0 );
            sb.append(") contains the value ");
            sb.append( SuDokuUtils.toString( 1 + v0 ) );
            sb.append(", ");
            if( v0 == v1 ){
                sb.append("so does the cell (");
                sb.append( 1 + r1 );
                sb.append(",");
                sb.append( 1 + c1 );
                sb.append(")");
            } else {
                sb.append("it likewise contains the value ");
                sb.append( SuDokuUtils.toString( 1 + v1 ) );
            }
            sb.append(" - a situation that is clearly illegal.\nTherefore, the cell (");
            sb.append( 1 + r0 );
            sb.append(",");
            sb.append( 1 + c0 );
            sb.append(") cannot contain the value ");
            sb.append( SuDokuUtils.toString( 1 + v0 ) );
            sb.append(".\n");
        }
        eliminateMove( r0 , c0 , v0 );
        ++ chainsEliminations ;
        return true ;    
    }
    
    boolean cyclicElimination2( StringBuffer sb , int r0 , int c0 , int v0 , int r1 , int c1 , int v1 ){
        int i ;
        if( explain ){
            sb.append("The cell (");
            sb.append( 1 + r1 );
            sb.append(",");
            sb.append( 1 + c1 );
            sb.append(") must contain the value ");
            sb.append( SuDokuUtils.toString( 1 + v1 ) );
            sb.append(" if ");
            if( v0 == v1 ){
                sb.append("the cell (");
                sb.append( 1 + r0 );
                sb.append(",");
                sb.append( 1 + c0 );
                sb.append(") doesn't.\n");
            } else {
                sb.append("it doesn't contain the value ");
                sb.append( SuDokuUtils.toString( 1 + v0 ) );
                sb.append(".\n");
            }
            sb.append("Therefore, these two ");
            if( v0 == v1 ){
                sb.append("cells");
            } else {
                sb.append("values");
            }
            sb.append(" are the only candidates for ");
            if( v0 == v1 ){
                sb.append("the value ");
                sb.append( SuDokuUtils.toString( 1 + v1 ) );
                sb.append(" in ");                        
                if( r0 == r1 ){
                    sb.append("Row ");
                    sb.append( 1 + r0 );
                } else if( c0 == c1 ) {
                    sb.append("Column ");
                    sb.append( 1 + c0 );
                } else {
                    sb.append("Box [");
                    sb.append( 1 + r0 / grid.boxesAcross );
                    sb.append(",");
                    sb.append( 1 + c0 / grid.boxesDown );
                    sb.append("]");
                }
            } else {
                sb.append("the cell (");
                sb.append( 1 + r0 );
                sb.append(",");
                sb.append( 1 + c0 );
                sb.append(")");
            }
            sb.append(".\n");
        }                
        // Eliminate.
        if( v0 == v1 ){
            NumberState numberState = (NumberState) lcn.state ;
            if( r0 == r1 ){
                i = 0 ;
                while( i < grid.cellsInRow ){
                    if( ! numberState.eliminated[v0][r0][i] && i != c0 && i != c1 ){
                        eliminateMove( r0 , i , v0 );
                        ++ chainsEliminations ;
                    }
                    ++ i ;
                }
            } else if( c0 == c1 ) {
                i = 0 ;
                while( i < grid.cellsInRow ){
                    if( ! numberState.eliminated[v0][grid.cellsInRow+c0][i] && i != r0 && i != r1 ){
                        eliminateMove( i , c0 , v0 );
                        ++ chainsEliminations ;
                    }
                    ++ i ;
                }
            } else {
                int x , y ;
                final int b = r0 / grid.boxesAcross * grid.boxesAcross + c0 / grid.boxesDown ;
                i = 0 ;
                while( i < grid.cellsInRow ){
                    x = b / grid.boxesAcross * grid.boxesAcross + i / grid.boxesDown ;
                    y = b % grid.boxesAcross * grid.boxesDown + i % grid.boxesDown ;                                    
                    if( ! numberState.eliminated[v0][2*grid.cellsInRow+b][i] && !( x == r0 && y == c0 || x == r1 && y == c1 ) ){
                        eliminateMove( x , y , v0 );
                        ++ chainsEliminations ;
                    }
                    ++ i ;
                }
            }
        } else {
            CellState cellState = (CellState) lcc.state ;
            i = 0 ;
            while( i < grid.cellsInRow ){
                if( ! cellState.eliminated[r0][c0][i] && i != v0 && i != v1 ){
                    eliminateMove( r0 , c0 , i );
                    ++ chainsEliminations ;
                }
                ++ i ;
            }
        }
        return true ;
    }
    
    /**
     * Connects string pairs.
     */

    int connect( int i , int j , int type , int nChains ){
        int r0 = 0 , c0 = 0 , v0 = 0 , r1 = 0 , c1 = 0 , v1 = 0 , k , kOffset ;
        boolean isIReversed = false , 
                isJReversed = false , 
                isOrderReversed ;
        int otherEnd0True = 0 ,
            otherEnd0False = 0 ,
            otherEnd1True = 0 ,
            otherEnd1False = 0 ;
        switch( type % 4 ){
            case LEFT_LEFT :
            r0 = chainR1[i];
            c0 = chainC1[i];
            v0 = chainV1[i];
            r1 = chainR1[j];
            c1 = chainC1[j];
            v1 = chainV1[j];
            if( type / 4 == STRONG ){
                if( chainV0[i] == chainV0[j] ){
                    otherEnd0True  = chainOtherEnd1[i][TRUE] != DONT_KNOW ? chainOtherEnd0[j][chainOtherEnd1[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd1[i][FALSE] != DONT_KNOW ? chainOtherEnd0[j][chainOtherEnd1[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd1[j][TRUE] != DONT_KNOW ? chainOtherEnd0[i][chainOtherEnd1[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd1[j][FALSE] != DONT_KNOW ? chainOtherEnd0[i][chainOtherEnd1[j][FALSE]] : DONT_KNOW ;
                } else {
                    otherEnd0True  = chainOtherEnd1[i][TRUE] != DONT_KNOW ? chainOtherEnd0[j][1-chainOtherEnd1[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd1[i][FALSE] != DONT_KNOW ? chainOtherEnd0[j][1-chainOtherEnd1[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd1[j][TRUE] != DONT_KNOW ? chainOtherEnd0[i][1-chainOtherEnd1[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd1[j][FALSE] != DONT_KNOW ? chainOtherEnd0[i][1-chainOtherEnd1[j][FALSE]] : DONT_KNOW ;                            
                }
            } else {
                otherEnd0True  = chainOtherEnd1[i][TRUE] == TRUE ? chainOtherEnd0[j][FALSE] : DONT_KNOW ;
                otherEnd0False = chainOtherEnd1[i][FALSE] == TRUE ? chainOtherEnd0[j][FALSE] : DONT_KNOW ;
                otherEnd1True  = chainOtherEnd1[j][TRUE] == TRUE ? chainOtherEnd0[i][FALSE] : DONT_KNOW ;
                otherEnd1False = chainOtherEnd1[j][FALSE] == TRUE ? chainOtherEnd0[i][FALSE] : DONT_KNOW ;
            }
            isIReversed = true ;
            isJReversed = false ;
            break ;
        case RIGHT_RIGHT :
            r0 = chainR0[i];
            c0 = chainC0[i];
            v0 = chainV0[i];
            r1 = chainR0[j];
            c1 = chainC0[j];
            v1 = chainV0[j];
            if( type / 4 == STRONG ){
                if( chainV1[i] == chainV1[j] ){
                    otherEnd0True  = chainOtherEnd0[i][TRUE] != DONT_KNOW ? chainOtherEnd1[j][chainOtherEnd0[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd0[i][FALSE] != DONT_KNOW ? chainOtherEnd1[j][chainOtherEnd0[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd0[j][TRUE] != DONT_KNOW ? chainOtherEnd1[i][chainOtherEnd0[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd0[j][FALSE] != DONT_KNOW ? chainOtherEnd1[i][chainOtherEnd0[j][FALSE]] : DONT_KNOW ;
                } else {
                    otherEnd0True  = chainOtherEnd0[i][TRUE] != DONT_KNOW ? chainOtherEnd1[j][1-chainOtherEnd0[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd0[i][FALSE] != DONT_KNOW ? chainOtherEnd1[j][1-chainOtherEnd0[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd0[j][TRUE] != DONT_KNOW ? chainOtherEnd1[i][1-chainOtherEnd0[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd0[j][FALSE] != DONT_KNOW ? chainOtherEnd1[i][1-chainOtherEnd0[j][FALSE]] : DONT_KNOW ;
                }
            } else {
                otherEnd0True  = chainOtherEnd0[i][TRUE] == TRUE ? chainOtherEnd1[j][FALSE] : DONT_KNOW ;
                otherEnd0False = chainOtherEnd0[i][FALSE] == TRUE ? chainOtherEnd1[j][FALSE] : DONT_KNOW ;
                otherEnd1True  = chainOtherEnd0[j][TRUE] == TRUE ? chainOtherEnd1[i][FALSE] : DONT_KNOW ;
                otherEnd1False = chainOtherEnd0[j][FALSE] == TRUE ? chainOtherEnd1[i][FALSE] : DONT_KNOW ;
            }
            isIReversed = false ;
            isJReversed = true ;
            break ;
        case LEFT_RIGHT :
            r0 = chainR1[i];
            c0 = chainC1[i];
            v0 = chainV1[i];
            r1 = chainR0[j];
            c1 = chainC0[j];
            v1 = chainV0[j];
            if( type / 4 == STRONG ){
                if( chainV0[i] == chainV1[j] ){
                    otherEnd0True  = chainOtherEnd1[i][TRUE] != DONT_KNOW ? chainOtherEnd1[j][chainOtherEnd1[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd1[i][FALSE] != DONT_KNOW ? chainOtherEnd1[j][chainOtherEnd1[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd0[j][TRUE] != DONT_KNOW ? chainOtherEnd0[i][chainOtherEnd0[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd0[j][FALSE] != DONT_KNOW ? chainOtherEnd0[i][chainOtherEnd0[j][FALSE]] : DONT_KNOW ;
                } else {
                    otherEnd0True  = chainOtherEnd1[i][TRUE] != DONT_KNOW ? chainOtherEnd1[j][1-chainOtherEnd1[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd1[i][FALSE] != DONT_KNOW ? chainOtherEnd1[j][1-chainOtherEnd1[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd0[j][TRUE] != DONT_KNOW ? chainOtherEnd0[i][1-chainOtherEnd0[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd0[j][FALSE] != DONT_KNOW ? chainOtherEnd0[i][1-chainOtherEnd0[j][FALSE]] : DONT_KNOW ;
                }
            } else {
                otherEnd0True  = chainOtherEnd1[i][TRUE] == TRUE ? chainOtherEnd1[j][FALSE] : DONT_KNOW ;
                otherEnd0False = chainOtherEnd1[i][FALSE] == TRUE ? chainOtherEnd1[j][FALSE] : DONT_KNOW ;
                otherEnd1True  = chainOtherEnd0[j][TRUE] == TRUE ? chainOtherEnd0[i][FALSE] : DONT_KNOW ;
                otherEnd1False = chainOtherEnd0[j][FALSE] == TRUE ? chainOtherEnd0[i][FALSE] : DONT_KNOW ;
            }
            isIReversed = true ;
            isJReversed = true ;
            break ;
        case RIGHT_LEFT :
            r0 = chainR0[i];
            c0 = chainC0[i];
            v0 = chainV0[i];
            r1 = chainR1[j];
            c1 = chainC1[j]; 
            v1 = chainV1[j];
            if( type / 4 == STRONG ){
                if( chainV1[i] == chainV0[j] ){
                    otherEnd0True  = chainOtherEnd0[i][TRUE] != DONT_KNOW ? chainOtherEnd0[j][chainOtherEnd0[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd0[i][FALSE] != DONT_KNOW ? chainOtherEnd0[j][chainOtherEnd0[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd1[j][TRUE] != DONT_KNOW ? chainOtherEnd1[i][chainOtherEnd1[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd1[j][FALSE] != DONT_KNOW ? chainOtherEnd1[i][chainOtherEnd1[j][FALSE]] : DONT_KNOW ;
                } else {
                    otherEnd0True  = chainOtherEnd0[i][TRUE] != DONT_KNOW ? chainOtherEnd0[j][1-chainOtherEnd0[i][TRUE]] : DONT_KNOW ;
                    otherEnd0False = chainOtherEnd0[i][FALSE] != DONT_KNOW ? chainOtherEnd0[j][1-chainOtherEnd0[i][FALSE]] : DONT_KNOW ;
                    otherEnd1True  = chainOtherEnd1[j][TRUE] != DONT_KNOW ? chainOtherEnd1[i][1-chainOtherEnd1[j][TRUE]] : DONT_KNOW ;
                    otherEnd1False = chainOtherEnd1[j][FALSE] != DONT_KNOW ? chainOtherEnd1[i][1-chainOtherEnd1[j][FALSE]] : DONT_KNOW ;
                }
            } else {
                otherEnd0True  = chainOtherEnd0[i][TRUE] == TRUE ? chainOtherEnd0[j][FALSE] : DONT_KNOW ;
                otherEnd0False = chainOtherEnd0[i][FALSE] == TRUE ? chainOtherEnd0[j][FALSE] : DONT_KNOW ;
                otherEnd1True  = chainOtherEnd1[j][TRUE] == TRUE ? chainOtherEnd1[i][FALSE] : DONT_KNOW ;
                otherEnd1False = chainOtherEnd1[j][FALSE] == TRUE ? chainOtherEnd1[i][FALSE] : DONT_KNOW ;
            }
            isIReversed = false ;
            isJReversed = false ;
            break ;
        }
        // Check that some information is retained.
        if( otherEnd0True == DONT_KNOW &&
            otherEnd0False == DONT_KNOW &&
            otherEnd1True == DONT_KNOW &&
            otherEnd1False == DONT_KNOW ){
                return nChains ;
        }
        // Check whether the chain direction should be reversed.
        if( r0 > r1 || r0 == r1 && c0 > c1 ){
            isOrderReversed = true ;
            isIReversed = ! isIReversed ;
            isJReversed = ! isJReversed ;
            int tmp ;
            tmp = r0 ; r0 = r1 ; r1 = tmp ;
            tmp = c0 ; c0 = c1 ; c1 = tmp ;
            tmp = v0 ; v0 = v1 ; v1 = tmp ;
            tmp = otherEnd0True ; otherEnd0True = otherEnd1True ; otherEnd1True = tmp ;
            tmp = otherEnd0False ; otherEnd0False = otherEnd1False ; otherEnd1False = tmp ;            
        } else {
            isOrderReversed = false ;
        }
        // Check for cyclic dependencies.
        k = 0 ;
        while( k < nChains ){
            if( r0 == chainR0[k] && c0 == chainC0[k] && v0 == chainV0[k] && 
                otherEnd0True == chainOtherEnd0[k][TRUE] && otherEnd0False == chainOtherEnd0[k][FALSE] &&
                r1 == chainR1[k] && c1 == chainC1[k] && v1 == chainV1[k] && 
                otherEnd1True == chainOtherEnd1[k][TRUE] && otherEnd1False == chainOtherEnd1[k][FALSE] ){
                    return nChains ;
            }
            ++ k ;
        }
        // Record the endpoint details. (isLinkStrong is only meaningful for unit links so isn't recorded here).
        chainR0[nChains] = (byte) r0 ;
        chainC0[nChains] = (byte) c0 ;
        chainV0[nChains] = (byte) v0 ;
        chainOtherEnd0[nChains][TRUE] = (byte) otherEnd0True;   
        chainOtherEnd0[nChains][FALSE] = (byte) otherEnd0False;   
        chainR1[nChains] = (byte) r1 ;
        chainC1[nChains] = (byte) c1 ;
        chainV1[nChains] = (byte) v1 ;
        chainOtherEnd1[nChains][TRUE] = (byte) otherEnd1True;   
        chainOtherEnd1[nChains][FALSE] = (byte) otherEnd1False;   
        chainLength[nChains] = (byte)( chainLength[i] + chainLength[j] );       
        // Record the route, if necessary
        if( explain && chainLength[nChains] <= maxChainLength ){
            chainNComponents[nChains] = 1 ;
            chainComponents[nChains][0] = nChains ;
            // Write 'i' string.
            kOffset = isOrderReversed ? chainLength[j] : 0 ;
            k = 0 ;
            while( k < chainLength[i] ){
                if( isIReversed ){
                    chainRoute[nChains][k+kOffset] = chainRoute[i][chainLength[i]-1-k];
                    isLinkAscending[nChains][k+kOffset] = ! isLinkAscending[i][chainLength[i]-1-k];
                } else {
                    chainRoute[nChains][k+kOffset] = chainRoute[i][k];
                    isLinkAscending[nChains][k+kOffset] = isLinkAscending[i][k];
                }
                ++ k ;
            }
            // Write 'j' string.
            kOffset = isOrderReversed ? 0 : chainLength[i] ;
            k = 0 ;
            while( k < chainLength[j] ){
                if( isJReversed ){
                    chainRoute[nChains][k+kOffset] = chainRoute[j][chainLength[j]-1-k];
                    isLinkAscending[nChains][k+kOffset] = ! isLinkAscending[j][chainLength[j]-1-k];
                } else {
                    chainRoute[nChains][k+kOffset] = chainRoute[j][k];
                    isLinkAscending[nChains][k+kOffset] = isLinkAscending[j][k];
                }
                ++ k ;
            }
        }
        return ++ nChains ;
    }
    
    /**
     * Appends a description of the given string to the given string buffer.
     */

    void appendChain( StringBuffer sb , int s ){
        appendChain( sb , s , false );
    }
    
    void appendChain( StringBuffer sb , int s , boolean displayBooleans ){
        if( chainNComponents[s] > 1 ){
            appendComponentChain( sb , chainComponents[s][0] );
            int i = 1 ;
            while( i < chainNComponents[s] ){
                sb.append(" X ");
                appendComponentChain( sb , chainComponents[s][i] );
                ++ i ;
            }
        } else {   
            appendComponentChain( sb , s );
        }
        if( displayBooleans ){
            sb.append("\nT: ");
            sb.append( booleanChar( chainOtherEnd0[s][TRUE] ));
            sb.append(" ");
            sb.append( booleanChar( chainOtherEnd1[s][TRUE] ));
            sb.append("\n");
            sb.append("F: ");
            sb.append( booleanChar( chainOtherEnd0[s][FALSE] ));
            sb.append(" ");
            sb.append( booleanChar( chainOtherEnd1[s][FALSE] ));
        }
    }
    
    void appendComponentChain( StringBuffer sb , int s ){
        if( chainLength[s] > maxChainLength ){
            sb.append("Chain length exceeds ");
            sb.append( maxChainLength );
            return ;
        }
        int l = chainRoute[s][0] ;
        sb.append("(");
        sb.append( 1 + ( isLinkAscending[s][0] ? chainR0[l] : chainR1[l] ) );
        sb.append(",");
        sb.append( 1 + ( isLinkAscending[s][0] ? chainC0[l] : chainC1[l] ) );
        sb.append(")");
        int i = 0 ;
        while( i < chainLength[s] ){
            l = chainRoute[s][i] ;
            sb.append( isLinkStrong[l] ? "-" : "~" );
            sb.append( isLinkAscending[s][i] ? SuDokuUtils.toString( 1 + chainV1[l] ) : SuDokuUtils.toString( 1 + chainV0[l] ) );
            sb.append( isLinkStrong[l] ? "-" : "~" );
            sb.append("(");
            sb.append( 1 + ( isLinkAscending[s][i] ? chainR1[l] : chainR0[l] ) );
            sb.append(",");
            sb.append( 1 + ( isLinkAscending[s][i] ? chainC1[l] : chainC0[l] ) );
            sb.append(")");
            ++ i ;
        }
    }
    
    String booleanChar( int bool ){
        switch( bool ){
            case TRUE:
                return "T";
            case FALSE:
                return "F";
            case DONT_KNOW:
                return "?";
            case CONTRADICTION:
                return "X";
            default:
                return "!";            
        }
    }
    
    /**
     * Combines the information stored in two Boolean variables.
     */
    
    int sumBooleans( int bool1 , int bool2 ){
        if( bool1 == DONT_KNOW ){
            return bool2 ;
        } else if( bool2 == DONT_KNOW || bool1 == bool2 ) {
            return bool1 ;
        } else {
            return CONTRADICTION ;
        }
    }

    /**
     * Maps a boolean variable from one chain to the other.
     */

    int mapBoolean( int bool , int vi , int vj , int n ){
        if( vi == vj ){
            return bool ;   
        } else if( bool == TRUE ){
            return FALSE ;   
        } else if( bool == FALSE && n == 2 ){
            return TRUE ;
        } else {
            return DONT_KNOW ;
        }
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
                        eliminateMove( i , j , v );
                        ++ nishioEliminations ;
                        if( explain ){
                            sb.append("The move (");
                            sb.append( i + 1 );
                            sb.append(",");
                            sb.append( j + 1 );
                            sb.append("):= ");
                            sb.append( SuDokuUtils.toString( v + 1 ) );
                            sb.append(" would make it impossible to place the remaining ");
                            sb.append( SuDokuUtils.toString( v + 1 ) );
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
        if( checkInvulnerable ){
            if( writeState ){
                if( ! explain ){
                    invulnerableState.pushState( nMoves );
                }
                linearSystemState.pushState( nMoves ); 
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
        if( checkInvulnerable ){
            invulnerableState.addMove( x , y , value - 1 );
            linearSystemState.addMove( x , y , value - 1 );
            if( explain && nMoves < grid.cellsInRow * grid.cellsInRow ){
                invulnerableState.pushState( nMoves );
//                linearSystemState.pushState( nMoves ); 
            }
        }        
        // Underlying state variables
		lcn.updateState( x , y , value , reason , writeState );
        lcc.updateState( x , y , value , reason , writeState );
        return true ;
	}

	/**
     * Unwind the stack.
	 * @see com.act365.sudoku.IStrategy#unwind(int,boolean,boolean)
	 */
    
	public boolean unwind( int newNMoves , boolean reset , boolean eliminate ){
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
            reasons[newNMoves].append( SuDokuUtils.toString( grid.data[xMoves[newNMoves]][yMoves[newNMoves]] ) );
            reasons[newNMoves].append(" would lead to a contradiction.\n");
            int i = newNMoves + 1 ;
            while( i < nMoves ){
                reasons[i++] = new StringBuffer();
            }
        }
        if( checkInvulnerable ){
            invulnerableState.popState( newNMoves );
            linearSystemState.popState( newNMoves );
            if( eliminate ){
                invulnerableState.eliminateMove( xMoves[newNMoves] , yMoves[newNMoves] , grid.data[xMoves[newNMoves]][yMoves[newNMoves]] - 1 );
                linearSystemState.eliminateMove( xMoves[newNMoves] , yMoves[newNMoves] , grid.data[xMoves[newNMoves]][yMoves[newNMoves]] - 1 );
            }
        }
		lcn.unwind( newNMoves , false , eliminate );
        lcc.unwind( newNMoves , false , eliminate );
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
    
    /**
     * Prints the current state grid of the given type.
     */
    
    public String printState( int stateType ){
        
        switch( stateType ){            
            case SuDokuUtils.CELL_STATE :
                return lcc.state.toString();
            case SuDokuUtils.NUMBER_STATE :
                return lcn.state.toString();
            case SuDokuUtils.NEIGHBOUR_STATE :
                return invulnerableState.toString();
            default:
                return new String();
        }
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
        sb.append("\n");      
        sb.append("Cell State:\n");
        sb.append( lcn.state.toString() );
        sb.append("Number State:\n");
        sb.append( lcc.state.toString() );
        sb.append("Neighbourhood State:\n");
        sb.append( invulnerableState.toString() );
        sb.append("Linear System State:\n");
        sb.append( linearSystemState.toString() );
        
        return sb.toString(); 
    }  
}
