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

    Random generator ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][] nEliminated ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][] threadNEliminated ;
    
    int chosenValue ,
        chosenSector ;
    
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
        
        eliminated = new boolean[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow];
        nEliminated = new int[grid.cellsInRow][3*grid.cellsInRow];
    
        threadEliminated = new boolean[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        threadNEliminated = new int[grid.cellsInRow][3*grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        
        chosenValue = grid.cellsInRow ;
        chosenSector = grid.cellsInRow ;

        int i , j ;
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
     * Chooses from those values and sectors that have the least number of candidates. 
     * @see com.act365.sudoku.IStrategy#findCandidates()
     */
    
    public boolean findCandidates() {
        // Find the unpopulated cells with the smallest number of candidates.       
        int i , j , k , bestValue = grid.cellsInRow , bestSector = grid.cellsInRow , countBest = 0 , maxEliminated = -1 , nFilled = 0 ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < 3 * grid.cellsInRow ){
                if( nEliminated[i][j] == grid.cellsInRow - 1 ){
                    k = -1 ;
                    while( eliminated[i][j][++k] );
                    if( j < grid.cellsInRow ){
                        grid.x = j ;
                        grid.y = k ;
                    } else if( j < 2 * grid.cellsInRow ){
                        grid.x = k ;
                        grid.y = j - grid.cellsInRow ;
                    } else {
                        grid.x = ( j - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + k / grid.boxesDown ;
                        grid.y = ( j - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + k % grid.boxesDown ;
                    }
                    if( grid.data[grid.x][grid.y] > 0 ){
                        if( j < grid.cellsInRow ){
                            ++ nFilled ;
                        }
                    } else if( maxEliminated < grid.cellsInRow - 1 ){
                        countBest = 1 ;
                        bestValue = i ;
                        bestSector = j ;
                        maxEliminated = grid.cellsInRow - 1 ;                    
                    }
                } else if( nEliminated[i][j] > maxEliminated ){
                    countBest = 1 ;
                    bestValue = i ;
                    bestSector = j ;
                    maxEliminated = nEliminated[i][j];
                } else if( randomize && nEliminated[i][j] == maxEliminated ) {
                    ++ countBest ;
                }
                ++ j ;
            }
            ++ i ;
        }
        // Test whether the grid is complete.
        if( nFilled == grid.cellsInRow * grid.cellsInRow ){
            return false ;
        }
        // Select from the candidates.
        if( ! randomize || countBest == 1 ){
            chosenValue = bestValue ;
            chosenSector = bestSector ;
            return true ;
        } else {
            int pick = Math.abs( generator.nextInt() % countBest );
            i = 0 ;
            while( i < grid.cellsInRow ){
                j = 0 ;
                while( j < 3 * grid.cellsInRow ){
                    if( nEliminated[i][j] == maxEliminated && -- pick < 0 ){
                        chosenValue = i ;
                        chosenSector = j ;
                        return true ;
                    }
                    ++ j ;
                }
                ++ i ;  
            }           
        }
        // Shouldn't reach here.
        return false;
    }

    /**
     * Chooses a value for the current cell from the available candidates.
     * @see com.act365.sudoku.IStrategy#selectCandidate()
     */

    public boolean selectCandidate() {
        int i = 0 ;
        final int score = grid.cellsInRow - nEliminated[chosenValue][chosenSector];
        // Ascertain the value to write.
        if( score == 0 ){
            return false ;
        } else if( ! randomize || score == 1 ){
            while( i < grid.cellsInRow ){
                if( ! eliminated[chosenValue][chosenSector][i] ){
                    break ;
                }
                ++ i ;
            }           
        } else {
            int pick = Math.abs( generator.nextInt() % score );
            while( i < grid.cellsInRow ){
                if( ! eliminated[chosenValue][chosenSector][i] && -- pick < 0 ){
                    break;
                }
                ++ i ;
            }
        }
        // Write to the grid.
        if( chosenSector < grid.cellsInRow ){
            grid.x = chosenSector ;
            grid.y = i ;
        } else if( chosenSector < 2 * grid.cellsInRow ){
            grid.x = i ;
            grid.y = chosenSector - grid.cellsInRow ;
        } else {
            grid.x = ( chosenSector - 2 * grid.cellsInRow )/ grid.boxesAcross * grid.boxesAcross + i / grid.boxesDown ;
            grid.y = ( chosenSector - 2 * grid.cellsInRow )% grid.boxesAcross * grid.boxesDown + i % grid.boxesDown ;
        }
        grid.data[grid.x][grid.y] = chosenValue + 1 ;
        // Store state variables.
        int j , k ;
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
                ++ j ;
            }
            ++ i ;
        }
        xMoves[nMoves] = grid.x ;
        yMoves[nMoves] = grid.y ;
        // Update state variables.
        if( ! fillCell( grid.x , grid.y , grid.data[grid.x][grid.y] - 1 ) ){
            return false ;
        }
        ++ nMoves ;
        
        return true;
    }


    /**
     * Unwinds the the thread and reinstates state variables.
     * @see com.act365.sudoku.IStrategy#unwind(boolean)
     */
    
    public boolean unwind(boolean resetCurrent) {
        // Unwind thread.
        if( nMoves > 0 ){
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
                    ++ j ;
                }
                ++ i ;
            }
            // Reset cursor.
            grid.x = xMoves[nMoves];
            grid.y = yMoves[nMoves];
            // Current value is no longer a candidate.
            eliminated[grid.data[grid.x][grid.y]-1][grid.x][grid.y] = true ;
            ++ nEliminated[grid.data[grid.x][grid.y]-1][grid.x];
            eliminated[grid.data[grid.x][grid.y]-1][grid.cellsInRow+grid.y][grid.x] = true ;
            ++ nEliminated[grid.data[grid.x][grid.y]-1][grid.cellsInRow+grid.y];
            eliminated[grid.data[grid.x][grid.y]-1][2*grid.cellsInRow+grid.x/grid.boxesAcross*grid.boxesAcross+grid.y/grid.boxesDown][grid.x%grid.boxesAcross*grid.boxesDown+grid.y%grid.boxesDown] = true ;
            ++ nEliminated[grid.data[grid.x][grid.y]-1][2*grid.cellsInRow+grid.x/grid.boxesAcross*grid.boxesAcross+grid.y/grid.boxesDown];
        } else {
            return false ;      
        }
        if( resetCurrent ){
            // Remove it from the grid.
            grid.data[grid.x][grid.y] = 0 ;
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
