/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com January 2005
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
 * The MostCandidates strategy makes the move at any timestep that
 * will eliminate the greatest number of remaining candidates.
 * It is intended to be used in order to compose puzzles.
 */

public class MostCandidates extends StrategyBase implements IStrategy {

    boolean randomize ;
    
    boolean[][] mask ;

    Random generator ;
    
    Traversal traversal ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][][] nInvunerable ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][][] threadNInvunerable ;
    
    /**
     * 
     * @param mask
     * @param randomize
     */
    
    public MostCandidates( boolean[][] mask , boolean randomize ){
        this.mask = mask ;
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
            xCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            yCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];
            valueCandidates = new int[grid.cellsInRow*grid.cellsInRow*grid.cellsInRow];

            eliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow];
            nInvunerable = new int[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow];
    
            threadEliminated = new boolean[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
            threadNInvunerable = new int[grid.cellsInRow][grid.cellsInRow][grid.cellsInRow][grid.cellsInRow*grid.cellsInRow];
        }
        
        int i , j , k ;
        if( ! resize ){
            i = 0 ;
            while( i < grid.cellsInRow ){
                j = 0 ;
                while( j < grid.cellsInRow ){
                    k = 0 ;
                    while( k < grid.cellsInRow ){
                        eliminated[i][j][k] = false ;
                        nInvunerable[i][j][k] = 0 ;
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
     * Finds the candidates for which nInvunerable is lowest.
	 * @see com.act365.sudoku.IStrategy#findCandidates()
	 */
    
	public int findCandidates(){
        // Find the unpopulated cells with the smallest number of candidates.       
        int i , j , v , minEliminated = Integer.MAX_VALUE ;
        nCandidates = 0 ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                if( grid.data[i][j] > 0 || mask != null && ! mask[i][j] ){
                    ++ j ;
                    continue ;
                } 
                v = 0 ;
                while( v < grid.cellsInRow ){
                    if( ! eliminated[i][j][v] && nInvunerable[i][j][v] < minEliminated ){
                        nCandidates = 1 ;
                        minEliminated = nInvunerable[i][j][v];
                    }
                    ++ v ;
                }
                ++ j ;
            }
            ++ i ;
        }
        if( nCandidates == 0 ){
            return 0 ;
        }
        score = 2 * grid.cellsInRow + grid.boxesAcross * grid.boxesDown 
                  - grid.boxesAcross - grid.boxesDown - minEliminated ;
        nCandidates = 0 ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                if( grid.data[i][j] > 0 || mask != null && ! mask[i][j] ){
                    ++ j ;
                    continue ;
                }
                v = 0 ;
                while( v < grid.cellsInRow ){
                    if( ! eliminated[i][j][v] && ! eliminated[i][j][v] && nInvunerable[i][j][v] == minEliminated ){
                        xCandidates[nCandidates] = i ;
                        yCandidates[nCandidates] = j ;
                        valueCandidates[nCandidates] = v + 1 ;
                        ++ nCandidates ;
                    }
                    ++ v ;
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
        int i, j , v ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                v = 0 ;
                while( v < grid.cellsInRow ){
                    threadEliminated[i][j][v][nMoves] = eliminated[i][j][v];
                    threadNInvunerable[i][j][v][nMoves] = nInvunerable[i][j][v];
                    ++ v ;
                }
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
     * Note that when a puzzle is created, the first value
     * is set without loss of generality. Therefore the thread 
     * is only ever unwound until a single move remains.
     * @see com.act365.sudoku.IStrategy#unwind(boolean)
     */
    
    public boolean unwind( boolean resetCurrent ) {
        if( nMoves == ( mask == null ? 0 : 1 ) ){
            return false ;
        }
        // Unwind thread.
        -- nMoves ;
        // Reinstate state variables.
        int i , j , v ;
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                v = 0 ;
                while( v < grid.cellsInRow ){
                    eliminated[i][j][v] = threadEliminated[i][j][v][nMoves];
                    nInvunerable[i][j][v] = threadNInvunerable[i][j][v][nMoves];
                    ++ v ;
                }
                ++ j ;
            }
            ++ i ;
        }
        // Current value is no longer a candidate.
        eliminate( xMoves[nMoves] , yMoves[nMoves] , grid.data[xMoves[nMoves]][yMoves[nMoves]] - 1 );
        if( resetCurrent ){
            grid.data[xMoves[nMoves]][yMoves[nMoves]] = 0 ;
        }
        
        return true;
    }

    /**
     * Updates the state grids as a new cell has been filled.
     * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
     */
    
    boolean fillCell( int x , int y , int value ){
        int i , j , v , cx , cy ;
        // Check that it's a valid candidate.
        if( eliminated[x][y][value] ){
            return false ;
        }
        // Update nInvunerable for (x,y).
        v = 0 ;
        while( v < grid.cellsInRow ){
            nInvunerable[x][y][v] = 2 * grid.cellsInRow 
             + grid.boxesAcross * grid.boxesDown - grid.boxesAcross - grid.boxesDown ;
            ++ v ; 
        }
        // Update nInvunerable for the domain of (x,y).
        v = 0 ;
        while( v < grid.cellsInRow ){
            if( eliminated[x][y][v] ){
                ++ v ;
                continue ;
            }
            // Shared column
            i = -1 ;
            while( ++ i < grid.cellsInRow ){
                if( i == x || eliminated[i][y][v] ){
                    continue ;
                }
                if( v == value ){
                    nInvunerable[i][y][v] = 2 * grid.cellsInRow 
                                            + grid.boxesAcross * grid.boxesDown - grid.boxesAcross - grid.boxesDown ;
                } else {
                    ++ nInvunerable[i][y][v];
                }
            }
            // Shared row
            j = -1 ;
            while( ++ j < grid.cellsInRow ){
                if( j == y || eliminated[x][j][v] ){
                    continue ;
                }
                if( v == value ){
                    nInvunerable[x][j][v] = 2 * grid.cellsInRow 
                                            + grid.boxesAcross * grid.boxesDown - grid.boxesAcross - grid.boxesDown ;
                } else {
                    ++ nInvunerable[x][j][v];
                }
            }
            // Shared subgrid
            i = x / grid.boxesAcross * grid.boxesAcross - 1 ;
            while( ++ i < ( x / grid.boxesAcross + 1 )* grid.boxesAcross ){
                if( i == x ){
                    continue ;
                }
                j = y / grid.boxesDown * grid.boxesDown - 1 ;
                while( ++ j < ( y / grid.boxesDown + 1 )* grid.boxesDown ){
                    if( j == y || eliminated[i][j][v] ){
                        continue ;
                    }
                    if( v == value ){
                        nInvunerable[i][j][v] = 2 * grid.cellsInRow 
                                                + grid.boxesAcross * grid.boxesDown - grid.boxesAcross - grid.boxesDown ;
                    } else {
                        ++ nInvunerable[i][j][v];
                    }
                }
            }
            ++ v ;
        }
        // Update nInvunerable for the entire grid.
        i = 0 ;
        while( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                if( ! eliminated[i][j][value] && inDomain( x , y , i , j ) ){
                    cx= 0 ;
                    while( cx < grid.cellsInRow ){
                        cy = 0 ;
                        while( cy < grid.cellsInRow ){
                            if( ! eliminated[cx][cy][value] && ! inDomain( x , y , cx , cy ) && inDomain( cx , cy , i , j ) ){
                                ++ nInvunerable[cx][cy][value];
                            }
                            ++ cy ;
                        }
                        ++ cx ;
                    }
                }
                ++ j ;
            }
            ++ i ;
        } 
        // Update eliminated.        
        // Eliminate other candidates for the current cell.
        i = 0 ;
        while( i < grid.cellsInRow ){
            if( i != value && ! eliminated[x][y][i] ){
                eliminated[x][y][i] = true ;
            }
            ++ i ;
        }
        // Eliminate other candidates for the current row.
        j = 0 ;
        while( j < grid.cellsInRow ){
            if( j != y && ! eliminated[x][j][value] ){
                eliminated[x][j][value] = true ;
            }
            ++ j ;
        }
        // Eliminate other candidates for the current column.
        i = 0 ;
        while( i < grid.cellsInRow ){
            if( i != x && ! eliminated[i][y][value] ){
                eliminated[i][y][value] = true ;
            }
            ++ i ;
        }
        // Eliminate other candidates for the current subgrid.
        i = x / grid.boxesAcross * grid.boxesAcross - 1 ;
        while( ++ i < ( x / grid.boxesAcross + 1 )* grid.boxesAcross ){
            if( i == x ){
                continue ;
            }
            j = y / grid.boxesDown * grid.boxesDown - 1 ;
            while( ++ j < ( y / grid.boxesDown + 1 )* grid.boxesDown ){
                if( j == y ){
                    continue ;
                }
                if( ! eliminated[i][j][value] ){
                    eliminated[i][j][value] = true ;
                }
            }
        }
                
        return true ;
    }
    
    /**
     * Calculates whether (p,q) is in the domain of (x,y), 
     * i.e. whether it shares a column, row or subgrid.
     */
    
    boolean inDomain( int x , int y , int p , int q ){
        if( x == p || y == q ){
            return true ;
        } else if( p >= x / grid.boxesAcross * grid.boxesAcross &&
                   p < ( x / grid.boxesAcross + 1 )* grid.boxesAcross &&
                   q >= y / grid.boxesDown * grid.boxesDown && 
                   q < ( y / grid.boxesDown + 1 )* grid.boxesDown ){                   
            return true ;
        } else {
            return false ;
        }
    }

    /**
     * Eliminates the move (x,y):=v as a candidate.
     */    

    void eliminate( int x , int y , int v ){
        int i , j ;
        i = 0 ;
        while ( i < grid.cellsInRow ){
            j = 0 ;
            while( j < grid.cellsInRow ){
                if( i == x && j == y ){
                    eliminated[x][y][v] = true ;
                    nInvunerable[i][j][v] = 2 * grid.cellsInRow + grid.boxesAcross * grid.boxesDown - grid.boxesAcross - grid.boxesDown ;
                } else if( ! eliminated[i][j][v] && inDomain( x , y , i , j ) ){
                    ++ nInvunerable[i][j][v];
                }
                ++ j ;
            }
            ++ i ;
        }
    }
}
