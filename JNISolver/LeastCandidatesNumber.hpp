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

#ifndef INCLUDED_LEASTCANDIDATESNUMBER
#define INCLUDED_LEASTCANDIDATESNUMBER

#include "NumberState.hpp"
#include "StateStrategy.hpp"

#include <vector>

/**
 * The LeastCandidatesNumber strategy calculates, for each combination of
 * number and sector (where a sector is a generic term that covers rows,
 * columns and subgrids), the number of valid candidate cells and fills 
 * the the sectors with the least number of possible candidate cells first.
 */

class LeastCandidatesNumber : public StateStrategy<NumberState>
{
    bool _findMany ;

    std::vector<bool> _considered ;

    /**
     * Determines whether the move (x,y):= v has been considered.
     */

    bool isConsidered( const unsigned char x ,
                       const unsigned char y ,
                       const unsigned char v ) const
    {
        return _considered[( x * _cellsInRow + y )* _cellsInRow + v ];
    }

    /**
     * Signals that the move (x,y):=v has been considered.
     */

    void consider( const unsigned char x ,
                   const unsigned char y ,
                   const unsigned char v )
    {
        _considered[( x * _cellsInRow + y )* _cellsInRow + v ] = true ;
    }

public:
    
    /**
     * Creates a new LeastCandidatesNumber strategy instance that optionally finds many
     * candidates (rather than just one) and selects from that set randomly.
     */
    
    LeastCandidatesNumber( const bool findMany , 
                           const bool randomize ): 
                           StateStrategy<NumberState>( randomize ), 
                           _findMany( findMany ) {}

    /**
     * Creates a new LeastCandidatesNumber strategy instance that optionally selects 
     * from the set of candidates randomly.
     */
    
    LeastCandidatesNumber( const bool randomize ): 
                           StateStrategy<NumberState>( randomize ), 
                           _findMany( randomize ) {}

    /**
     * Prepares the strategy to solve a grid of the given size.
     */

    virtual void setup( const unsigned char boxesAcross ,
                        const unsigned char boxesDown ,
                        std::vector<unsigned char>* pData );

	/** 
	 * Finds the cells that have the least number of candidates. 
	 */

    virtual int findCandidates();
};

#endif

