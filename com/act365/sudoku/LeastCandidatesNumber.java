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
 * The LeastCandidatesNumber strategy calculates, for each combination of
 * number and sector (where a sector is a generic term that covers rows,
 * columns and subgrids), the number of valid candidate cells and fills 
 * the the sectors with the least number of possible candidate cells first.
 */

public class LeastCandidatesNumber extends StrategyBase implements IStrategy {

    boolean randomize ;

    boolean[][][] considered ;
    
    Random generator ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][] nEliminated ;
    
    boolean[][] isFilled ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][] threadNEliminated ;
    
    boolean[][][] threadIsFilled ;
    
    /**
     * Creates a new LeastCandidatesNumber instance to solve the given grid.
     */
    
    public LeastCandidatesNumber( boolean randomize ){
        this.randomize = randomize ;
        
        if( randomize ){
            generator = new Random();   
        }
    }
    
    /**
     * Sets the state variables.
     */
    
    public boolean setup( Grid grid ){

        super.setup( grid );

        if( resize ){        
            xCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            yCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            valueCandidates = new int[3*grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];

            eliminated = new boolean[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow];
            nEliminated = new int[grid.cellsInRow][3*grid.cellsInRow];
            isFilled = new boolean[grid.cellsInRow][3*grid.cellsInRow];
        
            threadEliminated = new boolean[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
            threadNEliminated = new int[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
            threadIsFilled = new boolean[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
    
            considered = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow];
        }

        int i , j , k ;
        if( ! resize ){
            i = 0 ;
            while( i < grid.cellsInRow ){
                j = 0 ;
                while( j < 3 * grid.cellsInRow ){
                    nEliminated[i][j] = 0 ;
                    isFilled[i][j] = false ;
                    k = 0 ;
                    while( k < grid.cellsInRow ){
                        eliminated[i][j][k] = false ;
                        if( j < grid.cellsInRow ){
                            considered[i][j][k] = false ;
                        }
                        ++ k ;
                    }
                    ++ j ;
                }
                
                ++ i ;
            }
        }
        
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                if( grid.data[i][j] > 0 ){
                    if( ! fillCell( i , j , grid.data[i][j] - 1 ) ){
                        return false ;
                    }
                }
                ++ j ;
            }
            ++ i ;
        }
        return true ;
    }
    
    /** 
     * Find the values and sectors that have the least number of candidates.
     * As the code stands, there might well be duplicates. 
     * @see com.act365.sudoku.IStrategy#findCandidates()
     */
    
    public int findCandidates() {
        // Find the unpopulated cells with the smallest number of candidates.       
        int i , j , k , x , y , maxEliminated = -1 ;
        nCandidates = 0 ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < 3 * grid.cellsInRow ){
                if( nEliminated[i][j] == grid.cellsInRow ){
                    score = 0 ;
                    return ( nCandidates = 0 );
                } else if( isFilled[i][j] ){
                } else if( nEliminated[i][j] > maxEliminated ){
                    nCandidates = 1 ;
                    maxEliminated = nEliminated[i][j];
                }
                ++ j ;
            }
            ++ i ;
        }
        if( nCandidates == 0 ){
            return 0 ;
        }
        score = maxEliminated ;
        nCandidates = 0 ;
        // Blank out the grid of considered values.
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                k = 0 ;
                while( k < grid.cellsInRow ){
                    considered[i][j][k] = false ;
                    ++ k ;
                }
                ++ j ;
            }
            ++ i ;
        }
        // Convert into standard x,y:=value coordinate system
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < 3 * grid.cellsInRow ){
                if( ! isFilled[i][j] && nEliminated[i][j] == maxEliminated ){
                    k = 0 ;
                    while( k < grid.cellsInRow ){
                        if( ! eliminated[i][j][k] ){
                            if( j < grid.cellsInRow ){
                                x = j ;
                                y = k ;
                            } else if( j < 2 * grid.cellsInRow ){
                                x = k ;
                                y = j - grid.cellsInRow ;
                            } else {
                                x = ( j - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + k / grid.boxesDown ;
                                y = ( j - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + k % grid.boxesDown ;
                            }
                            if( considered[x][y][i] ){
                                ++ k ;
                                continue;
                            }
                            considered[x][y][i] = true ;
                            xCandidates[nCandidates] = x ;
                            yCandidates[nCandidates] = y ;
                            valueCandidates[nCandidates] = i + 1 ;
                            ++ nCandidates ;
                        }
                        ++ k ;
                    }
                }
                ++ j ;
            }
            ++ i ;  
        }
        
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
     * @see com.act365.sudoku.IStrategy#updateState(int,int,int)
     */    
    
    public boolean updateState( int x , int y , int value ){
        // Store current state variables on thread.
        int i, j , k ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < 3 * grid.cellsInRow ){
                k = 0 ;
                while( k < grid.cellsInRow ){
                    threadEliminated[i][j][k][nMoves] = eliminated[i][j][k];
                    ++ k ;
                }
                threadNEliminated[i][j][nMoves] = nEliminated[i][j];
                threadIsFilled[i][j][nMoves] = isFilled[i][j];
                ++ j ;
            }
            ++ i ;
        }
        // Update state variables
        if( ! fillCell( x , y , value - 1 ) ){
            return false ;
        }
        // Store move to thread
        xMoves[nMoves] = x ;
        yMoves[nMoves] = y ;
        ++ nMoves ;
        
        return true;
    }

    /**
     * Unwinds the the thread and reinstates state variables.
     * @see com.act365.sudoku.IStrategy#unwind(boolean)
     */
    
    public boolean unwind( boolean resetCurrent ) {
        
        if( nMoves == 0 ){
            return false ;
        }
        // Unwind thread.
        -- nMoves ;
        // Reinstate state variables.
        int i , j , k ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < 3 * grid.cellsInRow ){
                k = 0 ;
                while( k < grid.cellsInRow ){
                    eliminated[i][j][k] = threadEliminated[i][j][k][nMoves];
                    ++ k ;
                }
                nEliminated[i][j] = threadNEliminated[i][j][nMoves];
                isFilled[i][j] = threadIsFilled[i][j][nMoves];
                ++ j ;
            }
            ++ i ;
        }
        // Current value is no longer a candidate.
        eliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][xMoves[nMoves]][yMoves[nMoves]] = true ;
        ++ nEliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][xMoves[nMoves]];
        eliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][grid.cellsInRow+yMoves[nMoves]][xMoves[nMoves]] = true ;
        ++ nEliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][grid.cellsInRow+yMoves[nMoves]];
        eliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][2*grid.cellsInRow+xMoves[nMoves]/grid.boxesAcross*grid.boxesAcross+yMoves[nMoves]/grid.boxesDown][xMoves[nMoves]%grid.boxesAcross*grid.boxesDown+yMoves[nMoves]%grid.boxesDown] = true ;
        ++ nEliminated[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][2*grid.cellsInRow+xMoves[nMoves]/grid.boxesAcross*grid.boxesAcross+yMoves[nMoves]/grid.boxesDown];
        isFilled[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][xMoves[nMoves]] = false ;
        isFilled[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][grid.cellsInRow+yMoves[nMoves]] = false ;
        isFilled[grid.data[xMoves[nMoves]][yMoves[nMoves]]-1][2*grid.cellsInRow+xMoves[nMoves]/grid.boxesAcross*grid.boxesAcross+yMoves[nMoves]/grid.boxesDown] = false ;
        // Remove the most recent move from the grid.
        if( resetCurrent ){
            grid.data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;
        }

        return true ;
    }

    /**
     * Updates the state grids as a new cell has been filled.
     * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
     */
    
    boolean fillCell( int x , int y , int value ){
        int i , j ;
        // Check that it's a valid candidate.
        if( eliminated[value][x][y] || 
            eliminated[value][grid.cellsInRow+y][x] || 
            eliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][x%grid.boxesAcross*grid.boxesDown+y%grid.boxesDown] ){
                return false ;
        }
        // Note which sectors have been filled.
        isFilled[value][x] = true ;
        isFilled[value][grid.cellsInRow+y] = true ;
        isFilled[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown] = true ;
        // Eliminate the current value from other cells in its 
        // ... row (x,i)
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i == y ){
                continue ;
            }
            if( ! eliminated[value][x][i] ){
                eliminated[value][x][i] = true ;
                ++ nEliminated[value][x];
            }
            if( ! eliminated[value][grid.cellsInRow+i][x] ){
                eliminated[value][grid.cellsInRow+i][x] = true ;
                ++ nEliminated[value][grid.cellsInRow+i];
            }
            if( ! eliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+i/grid.boxesDown][x%grid.boxesAcross*grid.boxesDown+i%grid.boxesDown] ){
                eliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+i/grid.boxesDown][x%grid.boxesAcross*grid.boxesDown+i%grid.boxesDown] = true ;
                ++ nEliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+i/grid.boxesDown];
            }
        }
        if( nEliminated[value][x] != grid.cellsInRow - 1 ){
            return false ;
        }
        // ... column (i,y) 
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i == x ){
                continue ;
            }
            if( ! eliminated[value][i][y] ){
                eliminated[value][i][y] = true ;
                ++ nEliminated[value][i];
            }
            if( ! eliminated[value][grid.cellsInRow+y][i] ){
                eliminated[value][grid.cellsInRow+y][i] = true ;
                ++ nEliminated[value][grid.cellsInRow+y];
            }
            if( ! eliminated[value][2*grid.cellsInRow+i/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][i%grid.boxesAcross*grid.boxesDown+y%grid.boxesDown] ){
                eliminated[value][2*grid.cellsInRow+i/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][i%grid.boxesAcross*grid.boxesDown+y%grid.boxesDown] = true ;
                ++ nEliminated[value][2*grid.cellsInRow+i/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown];
            }
        }
        if( nEliminated[value][grid.cellsInRow+y] != grid.cellsInRow - 1 ){
            return false ;
        }
        // ... subgrid
        i = x / grid.boxesAcross * grid.boxesAcross - 1 ;
        while( ++ i < ( x / grid.boxesAcross + 1 )* grid.boxesAcross ){
            j = y / grid.boxesDown * grid.boxesDown - 1 ;
            while( ++ j < ( y / grid.boxesDown + 1 )* grid.boxesDown ){
                if( i == x && j == y ){
                    continue ;
                }
                if( ! eliminated[value][i][j] ){
                    eliminated[value][i][j] = true ;
                    ++ nEliminated[value][i];
                }
                if( ! eliminated[value][grid.cellsInRow+j][i] ){
                    eliminated[value][grid.cellsInRow+j][i] = true ;
                    ++ nEliminated[value][grid.cellsInRow+j];
                }
            }
        }
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i == x % grid.boxesAcross * grid.boxesDown + y % grid.boxesDown ){
                continue ;    
            }
            if( ! eliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][i] ){
                eliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][i] = true ;
                ++ nEliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown];
            }
        }
        if( nEliminated[value][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown] != grid.cellsInRow - 1 ){
            return false ;
        }
        // Eliminate other values as candidates for the current row.
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i != value && ! eliminated[i][x][y] ){
                eliminated[i][x][y] = true ;
                ++ nEliminated[i][x];
            }
        }
        // Eliminate other values as candidates for the current column.
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i != value && ! eliminated[i][grid.cellsInRow+y][x] ){
                eliminated[i][grid.cellsInRow+y][x] = true ;
                ++ nEliminated[i][grid.cellsInRow+y];
            }
        }
        // Eliminate other values as candidates for the current subgrid.
        i = -1 ;
        while( ++ i < grid.cellsInRow ){
            if( i != value && ! eliminated[i][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][x%grid.boxesAcross*grid.boxesDown+y%grid.boxesDown] ){
                eliminated[i][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown][x%grid.boxesAcross*grid.boxesDown+y%grid.boxesDown] = true ;
                ++ nEliminated[i][2*grid.cellsInRow+x/grid.boxesAcross*grid.boxesAcross+y/grid.boxesDown];
            }
        }
                
        return true ;
    }
}
