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

#include "InvulnerableState.hpp"

#include <algorithm>
#include <cassert>

void InvulnerableState::setup( const unsigned char boxesAcross , 
                               const unsigned char boxesDown ) {

    _boxesAcross = boxesAcross ;
    _boxesDown = boxesDown ;

    if( _cellsInRow != boxesAcross * boxesDown ){
        _cellsInRow = boxesAcross * boxesDown ;
        _eliminated.resize( _cellsInRow * _cellsInRow * _cellsInRow );
        _nInvulnerable.resize( _cellsInRow * _cellsInRow * _cellsInRow );
    
        _threadEliminated.resize( _cellsInRow * _cellsInRow );
        _threadNInvulnerable.resize( _cellsInRow * _cellsInRow );
        int i = 0 ;
        while( i < _cellsInRow * _cellsInRow ){
            _threadEliminated[i].resize( _cellsInRow * _cellsInRow * _cellsInRow );
            _threadNInvulnerable[i].resize( _cellsInRow * _cellsInRow * _cellsInRow );
            ++ i ;
        }
    } else {
        std::fill( _eliminated.begin() , _eliminated.end() , false );
        std::fill( _nInvulnerable.begin() , _nInvulnerable.end() , 0 );
    }
}
          
void InvulnerableState::pushState( const int nMoves ) 
{
    _threadEliminated[nMoves] = _eliminated ;
    _threadNInvulnerable[nMoves] = _nInvulnerable ;
}

void InvulnerableState::popState( const int nMoves ) 
{
    _eliminated = _threadEliminated[nMoves];
    _nInvulnerable = _threadNInvulnerable[nMoves];
}

void InvulnerableState::addMove( const unsigned char x , 
                                 const unsigned char y , 
                                 const unsigned char value ) 
{
    unsigned char i , j , v , cx , cy ;
    // Check that it's a valid candidate.
    assert( ! isEliminated( x , y , value ) );
    // Update nInvulnerable for (x,y).
    v = 0 ;
    while( v < _cellsInRow ){
        nInvulnerable( x , y , v ) = 2 * _cellsInRow 
                          + _boxesAcross * _boxesDown - _boxesAcross - _boxesDown ;
        ++ v ; 
    }
    // Update nInvulnerable for the domain of (x,y).
    v = 0 ;
    while( v < _cellsInRow ){
        if( isEliminated( x , y , v ) ){
            ++ v ;
            continue ;
        }
        // Shared column
        i = 0 ;
        while( i < _cellsInRow ){
            if( i == x || isEliminated( i , y , v ) ){
                ++ i ;
                continue ;
            }
            if( v == value ){
                nInvulnerable( i , y , v ) = 2 * _cellsInRow 
                                              + _boxesAcross * _boxesDown - _boxesAcross - _boxesDown ;
            } else {
                ++ nInvulnerable( i , y , v );
            }
            ++ i ;
        }
        // Shared row
        j = 0 ;
        while( j < _cellsInRow ){
            if( j == y || isEliminated( x , j , v ) ){
                ++ j ;
                continue ;
            }
            if( v == value ){
                nInvulnerable( x , j , v ) = 2 * _cellsInRow 
                                + _boxesAcross * _boxesDown - _boxesAcross - _boxesDown ;
            } else {
                ++ nInvulnerable( x , j , v );
            }
            ++ j ;
        }
        // Shared subgrid
        i = x / _boxesAcross * _boxesAcross ;
        while( i < ( x / _boxesAcross + 1 )* _boxesAcross ){
            if( i == x ){
                ++ i ;
                continue ;
            }
            j = y / _boxesDown * _boxesDown ;
            while( j < ( y / _boxesDown + 1 )* _boxesDown ){
                if( j == y || isEliminated( i , j , v ) ){
                    ++ j ;
                    continue ;
                }
                if( v == value ){
                    nInvulnerable( i , j , v ) = 2 * _cellsInRow 
                                            + _boxesAcross * _boxesDown - _boxesAcross - _boxesDown ;
                } else {
                    ++ nInvulnerable( i , j , v );
                }
                ++ j ;
            }
            ++ i ;
        }
        ++ v ;
    }
    // Update nInvulnerable for the entire grid.
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < _cellsInRow ){
            if( ! isEliminated( i , j , value ) && inDomain( x , y , i , j ) ){
                cx= 0 ;
                while( cx < _cellsInRow ){
                    cy = 0 ;
                    while( cy < _cellsInRow ){
                        if( ! isEliminated( cx , cy , value ) && ! inDomain( x , y , cx , cy ) && inDomain( cx , cy , i , j ) ){
                            ++ nInvulnerable( cx , cy , value );
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
    while( i < _cellsInRow ){
        if( i != value && ! isEliminated( x , y , i ) ){
            eliminate( x , y , i );
        }
        ++ i ;
    }
    // Eliminate other candidates for the current row.
    j = 0 ;
    while( j < _cellsInRow ){
        if( j != y && ! isEliminated( x , j , value ) ){
            eliminate( x , j , value );
        }
        ++ j ;
    }
    // Eliminate other candidates for the current column.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != x && ! isEliminated( i , y , value ) ){
            eliminate( i , y , value );
        }
        ++ i ;
    }
    // Eliminate other candidates for the current subgrid.
    i = x / _boxesAcross * _boxesAcross ;
    while( i < ( x / _boxesAcross + 1 )* _boxesAcross ){
        if( i == x ){
            ++ i ;
            continue ;
        }
        j = y / _boxesDown * _boxesDown ;
        while( j < ( y / _boxesDown + 1 )* _boxesDown ){
            if( j == y ){
                ++ j ;
                continue ;
            }
            if( ! isEliminated( i , j , value ) ){
                eliminate( i , j , value );
            }
            ++ j ;
        }
        ++ i ;
    }
}

void InvulnerableState::eliminateMove( const unsigned char x , 
                                       const unsigned char y , 
                                       const unsigned char value ) 
{
    unsigned char i , j ;
    i = 0 ;
    while ( i < _cellsInRow ){
        j = 0 ;
        while( j < _cellsInRow ){
            if( i == x && j == y ){
                eliminate( x , y , value );
                nInvulnerable( i , j , value ) = 2 * _cellsInRow + _boxesAcross * _boxesDown - _boxesAcross - _boxesDown ;
            } else if( ! isEliminated( i , j , value ) && inDomain( x , y , i , j ) ){
                ++ nInvulnerable( i , j , value );
            }
            ++ j ;
        }
        ++ i ;
    }
}
    
bool InvulnerableState::inDomain( const unsigned char x , 
                                  const unsigned char y , 
                                  const unsigned char p , 
                                  const unsigned char q ) const
{
    if( x == p || y == q ){
        return true ;
    } else if( p >= x / _boxesAcross * _boxesAcross &&
               p < ( x / _boxesAcross + 1 )* _boxesAcross &&
               q >= y / _boxesDown * _boxesDown && 
               q < ( y / _boxesDown + 1 )* _boxesDown ){                   
        return true ;
    } else {
        return false ;
    }
}
