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

#include "LeastCandidatesHybrid.hpp"

#include <cstdlib>

LeastCandidatesHybrid::LeastCandidatesHybrid( const bool randomize )
                      :StrategyBase( randomize ),
                       _lcn( randomize ),
                       _lcc( randomize )
{}


void LeastCandidatesHybrid::setup( const unsigned char boxesAcross ,
                                   const unsigned char boxesDown ,
                                   std::vector<unsigned char>* pData )
{
    StrategyBase::setup( boxesAcross , boxesDown , pData );
    _lcc.setup( boxesAcross , boxesDown , pData );
    _lcn.setup( boxesAcross , boxesDown , pData );
}
    
int LeastCandidatesHybrid::findCandidates() 
{     
    if( _lcc.findCandidates() == 0 || _lcn.findCandidates() == 0 ){
        _score = 0 ;
        return ( _nCandidates = 0 );
    }

    StrategyBase& better = _lcc.getScore() < _lcn.getScore() ? (StrategyBase&) _lcc : (StrategyBase&) _lcn ;
               
    _nCandidates = 0 ;        
    while( _nCandidates < better.getNumberOfCandidates() ){
        _xCandidates[_nCandidates] = better.getXCandidate( _nCandidates );    
        _yCandidates[_nCandidates] = better.getYCandidate( _nCandidates );    
        _valueCandidates[_nCandidates] = better.getValueCandidate( _nCandidates );
        ++ _nCandidates ;    
    }        
    _score = better.getScore();

    return _nCandidates ;
}

void LeastCandidatesHybrid::updateState( const unsigned char x , 
                                         const unsigned char y , 
                                         const unsigned char value , 
                                         const bool writeState ) 
{
    // Store move to thread
    _xMoves[_nMoves] = x ;
    _yMoves[_nMoves] = y ;
    ++ _nMoves ;
    // Underlying state variables
    _lcn.updateState( x , y , value , writeState );
    _lcc.updateState( x , y , value , writeState );
}
   
bool LeastCandidatesHybrid::unwind( const int newNMoves , 
                                    const bool reset )
{
    // Unwind thread.
	_lcn.unwind( newNMoves , false );
    _lcc.unwind( newNMoves , false );
    // Remove the most recent moves from the grid.
    if( reset ){
        int i = std::max( newNMoves , 0 );
        while( i < _nMoves ){
            grid( _xMoves[i] , _yMoves[i] ) = 0 ;
            ++ i ;
        }
    }
    _nMoves = newNMoves ;
    return _nMoves >= 0 ;
}
    
int LeastCandidatesHybrid::getLastWrittenMove() const
{
    int lcnMove = _lcn.getLastWrittenMove() ,
        lccMove = _lcc.getLastWrittenMove() ;
            
    if( lcnMove < lccMove ){
        return lcnMove ; 
    } else {
        return lccMove ;
    }
}
