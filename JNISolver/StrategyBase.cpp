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

#include "StrategyBase.hpp"
    
#include <cassert>
#include <cstdlib>
#include <iostream>

StrategyBase::StrategyBase( const bool randomize )
             :_randomize( randomize ),
              _pData( NULL ),
              _boxesAcross( 0 ),
              _boxesDown( 0 ),
              _cellsInRow( 0 ),
              _resize( false )
{}

void StrategyBase::setup( const unsigned char boxesAcross ,
                          const unsigned char boxesDown ,
                          std::vector<unsigned char>* pData )
{
    _pData = pData ;
    _boxesAcross = boxesAcross ;
    _boxesDown = boxesDown ;
    _resize = _cellsInRow != boxesAcross * boxesDown ;
    _cellsInRow = boxesAcross * boxesDown ;
    
    if( _resize ){
        _xMoves.resize( _cellsInRow * _cellsInRow );
        _yMoves.resize( _cellsInRow * _cellsInRow );
        _stateWrite.resize( _cellsInRow * _cellsInRow );
        _xCandidates.resize( _cellsInRow * _cellsInRow * _cellsInRow );
        _yCandidates.resize( _cellsInRow * _cellsInRow * _cellsInRow );
        _valueCandidates.resize( _cellsInRow * _cellsInRow * _cellsInRow );
    }

    _nMoves = _nCandidates = _score = 0 ;
    _bestX = _bestY = _bestValue = _cellsInRow ; 
}

void StrategyBase::selectCandidate() 
{
    const int pick = _randomize && _nCandidates > 1 ? rand() % _nCandidates : 0 ;
    _bestX = _xCandidates[pick];
    _bestY = _yCandidates[pick];
    _bestValue = _valueCandidates[pick];     
}
    
void StrategyBase::reset() 
{
    while( -- _nMoves >= 0 ){
        grid( _xMoves[_nMoves] , _yMoves[_nMoves] ) = 0 ;   
    }       
}

std::ostream& operator << ( std::ostream& out , const StrategyBase& strategy )
{
    int i = 0 ;
    while( i < strategy.getThreadLength() ){
        out << ( 1 + i ) << ". (" << ( 1 + strategy.getThreadX(i) ) << "," << ( 1 + strategy.getThreadY(i) ) 
            << "):=" << strategy.grid( strategy.getThreadX(i) , strategy.getThreadY(i) ) << std::endl ;
        ++ i ;
    }
    return out ;
}
    
