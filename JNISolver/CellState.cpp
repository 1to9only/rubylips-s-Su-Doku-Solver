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

#include "CellState.hpp"

#include <algorithm>
#include <cassert>

// The following function implementations use the STL copy operations for 
// sake of clarity where memcpy() might be more efficient. It's hoped that 
// the local STL library implementation will be good enough to ensure 
// adequate performance.

CellState::CellState():_boxesAcross(0),_boxesDown(0),_cellsInRow(0) {}

void CellState::setup( const unsigned char boxesAcross , 
		               const unsigned char boxesDown ) 
{
    _boxesAcross = boxesAcross ;
    _boxesDown = boxesDown ;

    if( _cellsInRow != boxesAcross * boxesDown ){
        _cellsInRow = boxesAcross * boxesDown ;
        _eliminated.resize( _cellsInRow * _cellsInRow * _cellsInRow );
        _nEliminated.resize( _cellsInRow * _cellsInRow );

        _threadEliminated.resize( _cellsInRow * _cellsInRow );
        _threadNEliminated.resize( _cellsInRow * _cellsInRow );
        int i = 0 ;
        while( i < _cellsInRow * _cellsInRow ){
            _threadEliminated[i].resize( _cellsInRow * _cellsInRow );
            _threadNEliminated[i].resize( _cellsInRow * _cellsInRow );
            ++ i ;
        }
    } else {
        std::fill( _eliminated.begin() , _eliminated.end() , false );
        std::fill( _nEliminated.begin() , _nEliminated.end() , 0 );
    }
}

void CellState::pushState( const int nMoves ) 
{
    _threadEliminated[nMoves] = _eliminated ;
    _threadNEliminated[nMoves] = _nEliminated ;
}

void CellState::popState( const int nMoves ) 
{
    _eliminated = _threadEliminated[nMoves];
    _nEliminated = _threadNEliminated[nMoves];
}

void CellState::addMove( const unsigned char r , 
                         const unsigned char c , 
                         const unsigned char v ) 
{
    unsigned char i , j ;
    // Check that it's a valid candidate.
    assert( ! isEliminated( r , c , v ) );
    // Eliminate other candidates for the current cell.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != v && ! isEliminated( r , c , i ) ){
            eliminate( r , c , i );
            ++ nEliminated( r , c );
        }
        ++ i ;
    }
    assert( nEliminated( r , c ) == _cellsInRow - 1 );
    // Eliminate other candidates for the current row.
    j = 0 ;
    while( j < _cellsInRow ){
        if( j != c && ! isEliminated( r , j , v ) ){
            eliminate( r , j , v );
            ++ nEliminated( r , j );
        }
        ++ j ;
    }
    // Eliminate other candidates for the current column.
    i = 0 ;
    while( i < _cellsInRow ){
        if( i != r && ! isEliminated( i , c , v ) ){
            eliminate( i , c , v );
            ++ nEliminated( i , c );
        }
        ++ i ;
    }
    // Eliminate other candidates for the current subgrid.
    i = r / _boxesAcross * _boxesAcross - 1 ;
    while( ++ i < ( r / _boxesAcross + 1 )* _boxesAcross ){
        if( i == r ){
            continue ;
        }
        j = c / _boxesDown * _boxesDown - 1 ;
        while( ++ j < ( c / _boxesDown + 1 )* _boxesDown ){
            if( j == c ){
                continue ;
            }
            if( ! isEliminated( i , j , v ) ){
                eliminate( i , j , v );
                ++ nEliminated( i , j );
            }
        }
    }
}

void CellState::eliminateMove( const unsigned char r , 
                               const unsigned char c , 
                               const unsigned char v ) 
{
    eliminate( r , c , v );
    ++ nEliminated( r , c );
}
