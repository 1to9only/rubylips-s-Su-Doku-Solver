/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com February 2005
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
 * InvulnerableState records the number of cells that would remain
 * unresolved for each given move (ie each cell/value pair).
 */

public class InvulnerableState implements IState {

    // Grid size
    
    int boxesAcross ,
        boxesDown ,
        cellsInRow ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][][] nInvulnerable ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][][] threadNInvulnerable ;
    
    /**
     * Sets the state grid to the appropriate size.
     * @see com.act365.sudoku.IState#setup(int,int)
     */

	public void setup(int boxesAcross, int boxesDown ) {

        this.boxesAcross = boxesAcross ;
        this.boxesDown = boxesDown ;

        final boolean resize = cellsInRow != boxesAcross * boxesDown ;

        cellsInRow = boxesAcross * boxesDown ;
        
        int i , j , k ;
        if( resize ){
            eliminated = new boolean[cellsInRow][cellsInRow][cellsInRow];
            nInvulnerable = new int[cellsInRow][cellsInRow][cellsInRow];
    
            threadEliminated = new boolean[cellsInRow*cellsInRow][cellsInRow][cellsInRow][cellsInRow];
            threadNInvulnerable = new int[cellsInRow*cellsInRow][cellsInRow][cellsInRow][cellsInRow];
        } else {
            i = 0 ;
            while( i < cellsInRow ){
                j = 0 ;
                while( j < cellsInRow ){
                    k = 0 ;
                    while( k < cellsInRow ){
                        eliminated[i][j][k] = false ;
                        nInvulnerable[i][j][k] = 0 ;
                        ++ k ;
                    }
                    ++ j ;
                }
                ++ i ;
            }
        }
	}

    /**
     * Writes the state grid to the stack at the appropriate position.
     * @see com.act365.sudoku.IState#pushState(int)
     */
          
	public void pushState( int nMoves ) {
        int i, j , v ;
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                v = 0 ;
                while( v < cellsInRow ){
                    threadEliminated[nMoves][i][j][v] = eliminated[i][j][v];
                    threadNInvulnerable[nMoves][i][j][v] = nInvulnerable[i][j][v];
                    ++ v ;
                }
                ++ j ;
            }
            ++ i ;
        }
	}

    /**
     * Reads the state gris from the stack at the appropriate position.
     * @see com.act365.sudoku.IState#popState(int)
     */
          
	public void popState(int nMoves ) {
        int i , j , v ;
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                v = 0 ;
                while( v < cellsInRow ){
                    eliminated[i][j][v] = threadEliminated[nMoves][i][j][v];
                    nInvulnerable[i][j][v] = threadNInvulnerable[nMoves][i][j][v];
                    ++ v ;
                }
                ++ j ;
            }
            ++ i ;
        }
	}

    /**
     * Adds the move (x,y):=v to the state grid.
     * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
     * @see com.act365.sudoku.IState#addMove(int, int, int)
     */

	public boolean addMove(int x, int y, int value ) {
        int i , j , v , cx , cy ;
        // Check that it's a valid candidate.
        if( eliminated[x][y][value] ){
            return false ;
        }
        // Update nInvulnerable for (x,y).
        v = 0 ;
        while( v < cellsInRow ){
            nInvulnerable[x][y][v] = 2 * cellsInRow 
             + boxesAcross * boxesDown - boxesAcross - boxesDown ;
            ++ v ; 
        }
        // Update nInvulnerable for the domain of (x,y).
        v = 0 ;
        while( v < cellsInRow ){
            if( eliminated[x][y][v] ){
                ++ v ;
                continue ;
            }
            // Shared column
            i = -1 ;
            while( ++ i < cellsInRow ){
                if( i == x || eliminated[i][y][v] ){
                    continue ;
                }
                if( v == value ){
                    nInvulnerable[i][y][v] = 2 * cellsInRow 
                                            + boxesAcross * boxesDown - boxesAcross - boxesDown ;
                } else {
                    ++ nInvulnerable[i][y][v];
                }
            }
            // Shared row
            j = -1 ;
            while( ++ j < cellsInRow ){
                if( j == y || eliminated[x][j][v] ){
                    continue ;
                }
                if( v == value ){
                    nInvulnerable[x][j][v] = 2 * cellsInRow 
                                            + boxesAcross * boxesDown - boxesAcross - boxesDown ;
                } else {
                    ++ nInvulnerable[x][j][v];
                }
            }
            // Shared subgrid
            i = x / boxesAcross * boxesAcross - 1 ;
            while( ++ i < ( x / boxesAcross + 1 )* boxesAcross ){
                if( i == x ){
                    continue ;
                }
                j = y / boxesDown * boxesDown - 1 ;
                while( ++ j < ( y / boxesDown + 1 )* boxesDown ){
                    if( j == y || eliminated[i][j][v] ){
                        continue ;
                    }
                    if( v == value ){
                        nInvulnerable[i][j][v] = 2 * cellsInRow 
                                                + boxesAcross * boxesDown - boxesAcross - boxesDown ;
                    } else {
                        ++ nInvulnerable[i][j][v];
                    }
                }
            }
            ++ v ;
        }
        // Update nInvulnerable for the entire grid.
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                if( ! eliminated[i][j][value] && inDomain( x , y , i , j ) ){
                    cx= 0 ;
                    while( cx < cellsInRow ){
                        cy = 0 ;
                        while( cy < cellsInRow ){
                            if( ! eliminated[cx][cy][value] && ! inDomain( x , y , cx , cy ) && inDomain( cx , cy , i , j ) ){
                                ++ nInvulnerable[cx][cy][value];
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
        while( i < cellsInRow ){
            if( i != value && ! eliminated[x][y][i] ){
                eliminated[x][y][i] = true ;
            }
            ++ i ;
        }
        // Eliminate other candidates for the current row.
        j = 0 ;
        while( j < cellsInRow ){
            if( j != y && ! eliminated[x][j][value] ){
                eliminated[x][j][value] = true ;
            }
            ++ j ;
        }
        // Eliminate other candidates for the current column.
        i = 0 ;
        while( i < cellsInRow ){
            if( i != x && ! eliminated[i][y][value] ){
                eliminated[i][y][value] = true ;
            }
            ++ i ;
        }
        // Eliminate other candidates for the current subgrid.
        i = x / boxesAcross * boxesAcross - 1 ;
        while( ++ i < ( x / boxesAcross + 1 )* boxesAcross ){
            if( i == x ){
                continue ;
            }
            j = y / boxesDown * boxesDown - 1 ;
            while( ++ j < ( y / boxesDown + 1 )* boxesDown ){
                if( j == y ){
                    continue ;
                }
                if( ! eliminated[i][j][value] ){
                    eliminated[i][j][value] = true ;
                }
            }
        }
		return true;
	}

    /**
     * Eliminates the move (x,y):=v from the current state grid.
     * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
     * @see com.act365.sudoku.IState#eliminateMove(int, int, int)
     */
     
	public boolean eliminateMove(int x, int y, int value ) {
        int i , j ;
        i = 0 ;
        while ( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                if( i == x && j == y ){
                    eliminated[x][y][value] = true ;
                    nInvulnerable[i][j][value] = 2 * cellsInRow + boxesAcross * boxesDown - boxesAcross - boxesDown ;
                } else if( ! eliminated[i][j][value] && inDomain( x , y , i , j ) ){
                    ++ nInvulnerable[i][j][value];
                }
                ++ j ;
            }
            ++ i ;
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
        } else if( p >= x / boxesAcross * boxesAcross &&
                   p < ( x / boxesAcross + 1 )* boxesAcross &&
                   q >= y / boxesDown * boxesDown && 
                   q < ( y / boxesDown + 1 )* boxesDown ){                   
            return true ;
        } else {
            return false ;
        }
    }
}
