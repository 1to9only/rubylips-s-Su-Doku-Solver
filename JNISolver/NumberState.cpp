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

#include "NumberState.hpp"

#include <algorithm>
#include <cassert>

// The following function implementations use the STL copy operations for 
// sake of clarity where memcpy() might be more efficient. It's hoped that 
// the local STL library implementation will be good enough to ensure 
// adequate performance.

NumberState::NumberState():_boxesAcross(0),_boxesDown(0),_cellsInRow(0){}

void NumberState::setup( const unsigned char boxesAcross , 
                         const unsigned char boxesDown ) {

    _boxesAcross = boxesAcross ;
    _boxesDown = boxesDown ;

    if( _cellsInRow != boxesAcross * boxesDown ){
        _cellsInRow = boxesAcross * boxesDown ;
        _eliminated.resize( 3 * _cellsInRow * _cellsInRow * _cellsInRow );
        _isFilled.resize( 3 * _cellsInRow * _cellsInRow );
        _nEliminated.resize( 3 * _cellsInRow * _cellsInRow );

        _threadEliminated.resize( _cellsInRow * _cellsInRow );
        _threadIsFilled.resize( _cellsInRow * _cellsInRow );
        _threadNEliminated.resize( _cellsInRow * _cellsInRow );
        int i = 0 ;
        while( i < _cellsInRow * _cellsInRow ){
            _threadEliminated[i].resize( 3 * _cellsInRow * _cellsInRow * _cellsInRow );
            _threadIsFilled[i].resize( 3 * _cellsInRow * _cellsInRow );
            _threadNEliminated[i].resize( 3 * _cellsInRow * _cellsInRow );
            ++ i ;
        }
    } else {
        std::fill( _eliminated.begin() , _eliminated.end() , false );
        std::fill( _isFilled.begin() , _isFilled.end() , false );
        std::fill( _nEliminated.begin() , _nEliminated.end() , 0 );
	}
}

void NumberState::pushState( const int nMoves ) {
    _threadEliminated[nMoves] = _eliminated ;
    _threadNEliminated[nMoves] = _nEliminated ;
    _threadIsFilled[nMoves] = _isFilled ;
}

void NumberState::popState( const int nMoves ) {
    _eliminated = _threadEliminated[nMoves];
    _nEliminated = _threadNEliminated[nMoves];
    _isFilled = _threadIsFilled[nMoves];
}


void NumberState::addMove( const unsigned char r , 
                           const unsigned char c , 
                           const unsigned char v ) {
    unsigned char i , j ;
    // Check that it's a valid candidate.
    assert( ! isEliminated( v , r , c ) );
    assert( ! isEliminated( v , _cellsInRow + c , r ) );  
    assert( ! isEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , r % _boxesAcross * _boxesDown + c % _boxesDown ) );
    // Note which sectors have been filled.
    fill( v , r , true );
    fill( v , _cellsInRow + c , true );
    fill( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , true );
    // Eliminate the current value from other cells in its 
    // ... row (r,i)
    i = 0 ;
    while( i < _cellsInRow ){
        if( i == c ){
            ++ i ;
            continue ;
        }
        if( ! isEliminated( v , r , i ) ){
            eliminate( v , r , i );
            ++ nEliminated( v , r );
        }
        if( ! isEliminated( v , _cellsInRow + i , r ) ){
            eliminate( v , _cellsInRow + i , r );
            ++ nEliminated( v , _cellsInRow + i );
        }
        if( ! isEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + i / _boxesDown , r % _boxesAcross * _boxesDown + i % _boxesDown ) ){
            eliminate( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + i / _boxesDown , r % _boxesAcross * _boxesDown + i % _boxesDown );
            ++ nEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + i / _boxesDown );
        }
        ++ i ;
    }
    assert( nEliminated( v , r ) == _cellsInRow - 1 );
    // ... column (i,c) 
    i = 0 ;
    while( i < _cellsInRow ){
        if( i == r ){
            ++ i ;
            continue ;
        }
        if( ! isEliminated( v , i , c ) ){
            eliminate( v , i , c );
            ++ nEliminated( v , i );
        }
        if( ! isEliminated( v , _cellsInRow + c , i ) ){
            eliminate( v , _cellsInRow + c , i );
            ++ nEliminated( v , _cellsInRow + c );
        }
        if( ! isEliminated( v , 2 * _cellsInRow + i / _boxesAcross * _boxesAcross + c / _boxesDown , i % _boxesAcross * _boxesDown + c % _boxesDown ) ){
            eliminate( v , 2 * _cellsInRow + i / _boxesAcross * _boxesAcross + c / _boxesDown , i % _boxesAcross * _boxesDown + c % _boxesDown );
            ++ nEliminated( v , 2 * _cellsInRow + i / _boxesAcross * _boxesAcross + c / _boxesDown );
        }
        ++ i ;
    }
    assert( nEliminated( v , _cellsInRow + c ) == _cellsInRow - 1 );
    // ... subgrid
    i = r / _boxesAcross * _boxesAcross ;
    while( i < ( r / _boxesAcross + 1 )* _boxesAcross ){
        j = c / _boxesDown * _boxesDown ;
        while( j < ( c / _boxesDown + 1 )* _boxesDown ){
            if( i == r && j == c ){
                ++ j ;
                continue ;
            }
            if( ! isEliminated( v , i , j ) ){
                eliminate( v , i , j );
                ++ nEliminated( v , i );
            }
            if( ! isEliminated( v , _cellsInRow + j , i ) ){
                eliminate( v , _cellsInRow + j , i );
                ++ nEliminated( v , _cellsInRow + j );
            }
            ++ j ;
        }
        ++ i ;
    }
    i = 0 ;
    while( i < _cellsInRow ){
        if( i == r % _boxesAcross * _boxesDown + c % _boxesDown ){
            ++ i ;
            continue ;    
        }
        if( ! isEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , i ) ){
            eliminate( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , i );
            ++ nEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown );
        }
        ++ i ;
    }
    assert( nEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown == _cellsInRow - 1 ) );
    // Eliminate other values as candidates for the current row.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != v && ! isEliminated( i , r , c ) ){
            eliminate( i , r , c );
            ++ nEliminated( i , r );
        }
        ++ i ;
    }
    // Eliminate other values as candidates for the current column.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != v && ! isEliminated( i , _cellsInRow + c  , r ) ){
            eliminate( i , _cellsInRow + c , r );
            ++ nEliminated( i , _cellsInRow + c );
        }
        ++ i ;
    }
    // Eliminate other values as candidates for the current subgrid.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != v && ! isEliminated( i , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , r % _boxesAcross * _boxesDown + c % _boxesDown ) ){
            eliminate( i , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , r % _boxesAcross * _boxesDown + c % _boxesDown );
            ++ nEliminated( i , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown );
        }
        ++ i ;
    }
}
     
void NumberState::eliminateMove( const unsigned char r , 
                                 const unsigned char c , 
                                 const unsigned char v ) 
{
    eliminate( v , r , c );
    ++ nEliminated( v , r );
    eliminate( v , _cellsInRow + c , r );
    ++ nEliminated( v , _cellsInRow + c );
    eliminate( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , r % _boxesAcross * _boxesDown + c % _boxesDown );
    ++ nEliminated( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown );
    fill( v , r , false );
    fill( v , _cellsInRow + c , false );
    fill( v , 2 * _cellsInRow + r / _boxesAcross * _boxesAcross + c / _boxesDown , false );
}


