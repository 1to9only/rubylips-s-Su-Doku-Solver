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

#ifndef INCLUDED_STATESTRATEGY
#define INCLUDED_STATESTRATEGY

#include "StrategyBase.hpp"

#include <cstdlib>

/**
 * The StateStrategy class provides a template for a Su Doku strategy that
 * makes its decision based upon the contents of a single State object.
 */

template <class State>
class StateStrategy : public StrategyBase
{
protected:

    // State variables
    
    State _state ;

public:

    StateStrategy( const bool randomize ): StrategyBase( randomize ) {}

    virtual void setup( const unsigned char boxesAcross ,
                        const unsigned char boxesDown ,
                        std::vector<unsigned char>* pData );

    virtual void updateState( const unsigned char x , 
                              const unsigned char y , 
                              const unsigned char value , 
                              const bool writeState );

    virtual bool unwind( const int newNMoves , const bool reset ); 
    
    /**
     * Returns the number of moves that had been made at the
     * last point where two alternative moves existed.
     */

    virtual int getLastWrittenMove() const ;
};

template <class State>
void StateStrategy<State>::setup( const unsigned char boxesAcross ,
                                  const unsigned char boxesDown ,
                                  std::vector<unsigned char>* pData )
{
    StrategyBase::setup( boxesAcross , boxesDown , pData );

    _state.setup( boxesAcross , boxesDown );        
    unsigned char i , j ;
    i = 0 ;
    while( i < _cellsInRow ){
        j = 0 ;
        while( j < _cellsInRow ){
            if( grid( i , j ) > 0 ){
                _state.addMove( i , j , grid( i , j ) - 1 );
            }
            ++ j ;
        }
        ++ i ;
    }
}

template <class State>
void StateStrategy<State>::updateState( const unsigned char x , 
                                        const unsigned char y , 
                                        const unsigned char value , 
                                        const bool writeState )
{
    // Store current state variables on thread.
    if( writeState ){
        _state.pushState( _nMoves );
    }        
    _stateWrite[_nMoves] = writeState ;
    // Store move to thread
    _xMoves[_nMoves] = x ;
    _yMoves[_nMoves] = y ;
    ++ _nMoves ;
    // Update state variables
    _state.addMove( x , y , value - 1 );
}

template <class State>
bool StateStrategy<State>::unwind( const int newNMoves , const bool reset ) 
{
    if( newNMoves >= 0 ){
        _state.popState( newNMoves );
        _state.eliminateMove( _xMoves[newNMoves] , _yMoves[newNMoves] , grid( _xMoves[newNMoves] , _yMoves[newNMoves] ) - 1 );
    }
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

template <class State>
int StateStrategy<State>::getLastWrittenMove() const
{
    int i = _nMoves ; 
    while( -- i >= 0 ){
        if( _stateWrite[i] ){
            break ;
        }
    }
    return i ;
}

#endif
