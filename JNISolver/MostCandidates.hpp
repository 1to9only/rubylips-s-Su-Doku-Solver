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

#ifndef INCLUDED_MOSTCANDIDATES
#define INCLUDED_MOSTCANDIDATES

#include "InvulnerableState.hpp"
#include "StrategyBase.hpp"

#include <vector>

/**
 * The MostCandidates strategy makes the move at any timestep that
 * will eliminate the greatest number of remaining candidates.
 * It is intended to be used in order to compose puzzles.
 */

class MostCandidates : public StrategyBase 
{
    const std::vector<bool>& _mask ;

    InvulnerableState _state ;

    /**
     * Determines whether cell (x,y) is covered by the mask.
     */

    bool isMasked( const unsigned char x ,
                   const unsigned char y ) const
    {
        return _mask[x*_cellsInRow+y];
    }

public:

    /**
     * Creates an optionally random MostCandidates strategy in order 
     * to create problems with the given mask.
     */
    
    MostCandidates( const std::vector<bool>& mask , 
                    const bool randomize )
                    :StrategyBase( randomize ),
                     _mask( mask )
    {}

    /**
     * Sets the state variables to solve the given grid.
     */
    
    virtual void setup( const unsigned char boxesAcross ,
                        const unsigned char boxesDown ,
                        std::vector<unsigned char>* pData );

	/**
     * Finds the candidates for which nInvunerable is lowest.
	 */
    
	virtual int findCandidates();

    /**
     * Updates state variables. MostCandidates always writes
     * its state to the stack.
     */    
    
    virtual void updateState( const unsigned char x , 
                              const unsigned char y , 
                              const unsigned char value , 
                              const bool writeState );

    /**
     * Unwinds the the thread and reinstates state variables.
     * Note that when a puzzle is created, the first value
     * is set without loss of generality. Therefore the thread 
     * is only ever unwound until a single move remains.
     */
    
    virtual bool unwind( const int newNMoves , 
                         const bool reset );
    
    /**
     * Returns the number of moves that had been made at the
     * last point where two alternative moves existed.
     */

    virtual int getLastWrittenMove() const ;
};

#endif

