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

#include "LeastCandidatesNumber.hpp"

#include <algorithm>

void LeastCandidatesNumber::setup( const unsigned char boxesAcross ,
                                   const unsigned char boxesDown ,
                                   std::vector<unsigned char>* pData )
{
    StateStrategy<NumberState>::setup( boxesAcross , boxesDown , pData );

    if( _findMany ){
        if( _resize ){
            _considered.resize( _cellsInRow * _cellsInRow * _cellsInRow );
        } else {
            std::fill( _considered.begin() , _considered.end() , false );
        }
    }
}

int LeastCandidatesNumber::findCandidates()
{
    // Find the unpopulated cells with the smallest number of candidates.       
    unsigned char i , j , k , x , y ;
    int maxEliminated = -1 ;
    _nCandidates = 0 ;
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < 3 * _cellsInRow ){
            if( _state.nEliminated( i , j ) == _cellsInRow ){
                _score = 0 ;
                return ( _nCandidates = 0 );
            } else if( _state.isFilled( i , j ) ){
            } else if( _state.nEliminated( i , j ) > maxEliminated ){
                _nCandidates = 1 ;
                maxEliminated = _state.nEliminated( i , j );
            }
            ++ j ;
        }
        ++ i ;
    }
    if( _nCandidates == 0 ){
        return 0 ;
    }
    _score = _cellsInRow - maxEliminated ;
    _nCandidates = 0 ;
    // Blank out the grid of considered values.
    if( _findMany ){
        std::fill( _considered.begin() , _considered.end() , false );
    }
    // Convert into standard (x,y):=value coordinate system
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < 3 * _cellsInRow ){
            if( ! _state.isFilled( i , j ) && _state.nEliminated( i , j ) == maxEliminated ){
                k = 0 ;
                while( k < _cellsInRow ){
                    if( ! _state.isEliminated( i , j , k ) ){
                        if( j < _cellsInRow ){
                            x = j ;
                            y = k ;
                        } else if( j < 2 * _cellsInRow ){
                            x = k ;
                            y = j - _cellsInRow ;
                        } else {
                            x = ( j - 2 * _cellsInRow )/ _boxesAcross * _boxesAcross + k / _boxesDown ;
                            y = ( j - 2 * _cellsInRow )% _boxesAcross * _boxesDown + k % _boxesDown ;
                        }
                        if( _findMany ){
                            if( isConsidered( x , y , i ) ){
                                ++ k ;
                                continue;
                            }
                            consider( x , y , i );
                        }
                        _xCandidates[_nCandidates] = x ;
                        _yCandidates[_nCandidates] = y ;
                        _valueCandidates[_nCandidates] = i + 1 ;
                        ++ _nCandidates ;
                        if( ! _findMany ){
                            return _nCandidates ;
                        }
                    }
                    ++ k ;
                }
            }
            ++ j ;
        }
        ++ i ;  
    }
        
    return _nCandidates ;
}
