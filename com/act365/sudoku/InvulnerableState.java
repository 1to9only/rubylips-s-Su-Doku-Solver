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
        cellsInRow ,
        maxScore ;
        
    // State variables
    
    boolean[][][] eliminated ;
    
    int[][][] nInvulnerable ;
    
    // Thread
    
    boolean[][][][] threadEliminated ;
    
    int[][][][] threadNInvulnerable ;
    
    // Temporary vars used to store partially-calculated 
    // values for efficiency reasons.
    
    transient int lowerX , upperX , lowerY , upperY ;
    
    /**
     * Sets the state grid to the appropriate size.
     * @see com.act365.sudoku.IState#setup(int,int)
     */

	public void setup(int boxesAcross, int boxesDown ) {

        this.boxesAcross = boxesAcross ;
        this.boxesDown = boxesDown ;

        final boolean resize = cellsInRow != boxesAcross * boxesDown ;

        cellsInRow = boxesAcross * boxesDown ;
        maxScore = 2 * cellsInRow + boxesAcross * boxesDown - boxesAcross - boxesDown ;
        
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

	public void addMove(int x, int y, int value ) throws Exception {
        int i , j , v , cx , cy ;
        // Check that it's a valid candidate.
        if( eliminated[x][y][value] ){
            throw new Exception("The move (" + ( 1 + x ) + "," + ( 1 + y ) + "):=" + ( 1 + value ) + " has already been eliminated");
        }
        // Calc temp values.
        lowerX = ( x / boxesAcross )* boxesAcross ;
        upperX = ( x / boxesAcross + 1 )* boxesAcross ;
        lowerY = ( y / boxesDown )* boxesDown ;
        upperY = ( y / boxesDown + 1 )* boxesDown ;
        // Update nInvulnerable for (x,y).
        v = 0 ;
        while( v < cellsInRow ){
            nInvulnerable[x][y][v] = maxScore ;
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
                    nInvulnerable[i][y][v] = maxScore ;
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
                    nInvulnerable[x][j][v] = maxScore ;
                } else {
                    ++ nInvulnerable[x][j][v];
                }
            }
            // Shared subgrid
            i = lowerX - 1 ;
            while( ++ i < upperX ){
                if( i == x ){
                    continue ;
                }
                j = lowerY - 1 ;
                while( ++ j < upperY ){
                    if( j == y || eliminated[i][j][v] ){
                        continue ;
                    }
                    if( v == value ){
                        nInvulnerable[i][j][v] = maxScore ;
                    } else {
                        ++ nInvulnerable[i][j][v];
                    }
                }
            }
            ++ v ;
        }
        // Update nInvulnerable for the entire grid.
        int lowerCX , upperCX , lowerCY , upperCY ;
        cx = 0 ;
        while( cx < cellsInRow ){
            if( cx == x ){
                ++ cx ;
                continue ;
            }
            cy = 0 ;
            while( cy < cellsInRow ){
                if( eliminated[cx][cy][value] || cy == y || lowerX <= cx && cx < upperX && lowerY <= cy && cy < upperY ){
                    ++ cy ;
                    continue ;
                }
                lowerCX = cx / boxesAcross * boxesAcross ;
                upperCX = ( cx / boxesAcross + 1 )* boxesAcross ;
                lowerCY = cy / boxesDown * boxesDown ;
                upperCY = ( cy / boxesDown + 1 )* boxesDown ;
                i = 0 ;
                while( i < cellsInRow ){
                    if( i == x ){
                        j = 0 ;
                        while( j < cellsInRow ){
                            if( ! eliminated[i][j][value] ){
                                if( i == cx || j == cy || lowerCX <= i && i < upperCX && lowerCY <= j && j < upperCY ){
                                    ++ nInvulnerable[cx][cy][value];
                                }
                            }
                            ++ j ;
                        }
                    } else if( lowerX <= i && i < upperX ){
                        j = lowerY ;
                        while( j < upperY ){
                            if( ! eliminated[i][j][value] ){
                                if( i == cx || j == cy || lowerCX <= i && i < upperCX && lowerCY <= j && j < upperCY ){
                                    ++ nInvulnerable[cx][cy][value];
                                }
                            }
                            ++ j ;
                        }
                    } else if( ! eliminated[i][y][value] ){
                        if( i == cx || y == cy || lowerCX <= i && i < upperCX && lowerCY <= y && y < upperCY ){
                            ++ nInvulnerable[cx][cy][value];
                        }
                    }
                    ++ i ;
                }                
                ++ cy ;
            }
            ++ cx ;
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
        i = lowerX - 1 ;
        while( ++ i < upperX ){
            if( i == x ){
                continue ;
            }
            j = lowerY - 1 ;
            while( ++ j < upperY ){
                if( j == y ){
                    continue ;
                }
                if( ! eliminated[i][j][value] ){
                    eliminated[i][j][value] = true ;
                }
            }
        }
	}

    /**
     * Eliminates the move (x,y):=v from the current state grid.
     * @param value is in the range [0,cellsInRow), not [1,cellsInRow]. 
     * @see com.act365.sudoku.IState#eliminateMove(int, int, int)
     */
     
	public void eliminateMove(int x, int y, int value ) {
        int i , j , partial ;
        // Calc temp values.
        lowerX = ( x / boxesAcross )* boxesAcross ;
        upperX = ( x / boxesAcross + 1 )* boxesAcross ;
        lowerY = ( y / boxesDown )* boxesDown ;
        upperY = ( y / boxesDown + 1 )* boxesDown ;
        i = 0 ;
        while ( i < cellsInRow ){
            partial = inDomainPartial( x , i );
            j = 0 ;
            while( j < cellsInRow ){
                if( i == x && j == y ){
                    eliminated[x][y][value] = true ;
                    nInvulnerable[i][j][value] = maxScore ;
                } else if( ! eliminated[i][j][value] && inDomain( partial , y , j ) ){
                    ++ nInvulnerable[i][j][value];
                }
                ++ j ;
            }
            ++ i ;
        }
	}
    
    /** 
     * The next two functions split inDomain(), which calculates whether 
     * (p,q) is in the domain of (x,y), i.e. whether it shares a column, 
     * row or subgrid, in order to allow more efficient calculation.
     */ 
    
    int inDomainPartial( int x , int p ){
        if( x == p ){
            return 2 ;
        } else if( p >= lowerX && p < upperX ) {
            return 1 ;
        } else {
            return 0 ;
        }
    }
    
    boolean inDomain( int partial , int y , int q ){
        switch( partial ){
            case 2 :
            return true ;

            case 1 :
            return q >= lowerY && q < upperY ;
            
            case 0 :
            return q == y ;
            
            default:
            return false ;
        }
    }

    /**
     * Produces a string representation of the state grid.
     */
    
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        int i , j , k , v ;
        int number = maxScore , fieldWidth = 1 , numberWidth ;
        while( ( number /= 10 ) >= 1 ){
            ++ fieldWidth ;
        }
        v = 0 ;
        while( v < cellsInRow ){
            sb.append( v + 1 );
            sb.append(".\n");
            i = 0 ;
            while( i < cellsInRow ){
                if( i > 0 && i % boxesAcross == 0 ){
                    k = 0 ;
                    while( k < ( fieldWidth + 1 )* cellsInRow + ( boxesAcross - 1 )* 2 ){
                        sb.append('*');
                        ++ k ;
                    }
                    sb.append(" \n");
                }
                j = 0 ;
                while( j < cellsInRow ){
                    if( j > 0 && j % boxesDown == 0 ){
                        sb.append(" *");
                    }
                    k = 0 ;
                    if( nInvulnerable[i][j][v] > 0 ){
                        numberWidth = 1 ;
                        number = nInvulnerable[i][j][v];
                        while( ( number /= 10 ) >= 1 ){
                            ++ numberWidth ;
                        }
                        while( k < 1 + fieldWidth - numberWidth ){
                            sb.append(' ');
                            ++ k ;
                        }
                        sb.append( nInvulnerable[i][j][v] );
                    } else {
                        sb.append(' ');
                        while( k < fieldWidth ){
                            sb.append('.');
                            ++ k ;
                        }
                    }
                    ++ j ;
                }
                sb.append(" \n");
                ++ i ;
            }
            ++ v ;        
        }
        
        return sb.toString();
    }
}
