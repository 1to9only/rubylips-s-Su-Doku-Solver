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

#include "MostCandidates.hpp"

#include <cstdlib>

void MostCandidates::setup( const unsigned char boxesAcross ,
                            const unsigned char boxesDown ,
                            std::vector<unsigned char>* pData )
{
    StrategyBase::setup( boxesAcross , boxesDown , pData );
    _state.setup( boxesAcross , boxesDown );
}

int MostCandidates::findCandidates()
{
    // Find the unpopulated cells with the smallest number of candidates.       
    unsigned char i , j , v , minEliminated = 255 ;
    _nCandidates = 0 ;
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < _cellsInRow ){
            if( grid( i , j ) > 0 || ! isMasked( i , j ) ){
                ++ j ;
                continue ;
            } 
            v = 0 ;
            while( v < _cellsInRow ){
                if( ! _state.isEliminated( i , j , v ) && _state.nInvulnerable( i , j , v ) < minEliminated ){
                    _nCandidates = 1 ;
                    minEliminated = _state.nInvulnerable( i , j , v );
                }
                ++ v ;
            }
            ++ j ;
        }
        ++ i ;
    }
    if( _nCandidates == 0 ){
        return 0 ;
    }
    _score = 2 * _cellsInRow + _boxesAcross * _boxesDown 
                 - _boxesAcross - _boxesDown - minEliminated ;
    _nCandidates = 0 ;
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < _cellsInRow ){
            if( grid( i , j ) > 0 || ! isMasked( i , j ) ){
                ++ j ;
                continue ;
            }
            v = 0 ;
            while( v < _cellsInRow ){
                if( ! _state.isEliminated( i , j , v ) && _state.nInvulnerable( i , j , v ) == minEliminated ){
                    _xCandidates[_nCandidates] = i ;
                    _yCandidates[_nCandidates] = j ;
                    _valueCandidates[_nCandidates] = v + 1 ;
                    ++ _nCandidates ;
                    if( _randomize ){
                        return _nCandidates ;
                    }
                }
                ++ v ;
            }
            ++ j ;
        }
        ++ i ;  
    }           
        
    return _nCandidates ;
}

void MostCandidates::updateState( const unsigned char x , 
                                  const unsigned char y , 
                                  const unsigned char value , 
                                  const bool writeState )
{
    // Store current state variables on thread.
    _state.pushState( _nMoves );
    _stateWrite[_nMoves] = true ;
    // Store move to thread
    _xMoves[_nMoves] = x ;
    _yMoves[_nMoves] = y ;
    ++ _nMoves ;
    // Update state variables
    _state.addMove( x , y , value - 1 );
}

bool MostCandidates::unwind( const int newNMoves , 
                             const bool reset ) {
    if( newNMoves >= 0 ){
        _state.popState( newNMoves );
        _state.eliminateMove( _xMoves[newNMoves] , 
                              _yMoves[newNMoves] , 
                              grid( _xMoves[newNMoves] , _yMoves[newNMoves] ) - 1 );
    }
    if( reset ){
        int i = std::max( newNMoves , 0 );
        while( i < _nMoves ){
            grid( _xMoves[i] , _yMoves[i] ) = 0 ;
            ++ i ;
        }
    }
    _nMoves = newNMoves ;
    return _nMoves >= 1 ;
}

int MostCandidates::getLastWrittenMove() const
{
    int i = _nMoves ; 
    while( -- i >= 0 ){
        if( _stateWrite[i] ){
            break ;
        }
    }
    return i ;
}
