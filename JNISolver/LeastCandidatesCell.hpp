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

#ifndef INCLUDED_LEASTCANDIDATESCELL
#define INCLUDED_LEASTCANDIDATESCELL

#include "CellState.hpp"
#include "StateStrategy.hpp"

/**
 * The LeastCandidatesCell strategy calculates the number of seemingly valid
 * candidates (of course, for a problem with a unique solution, there is only
 * one strictly valid candidate for each cell - a candidate is deemed
 * 'seemingly valid' if it isn't blatantly contradicted by another cell in
 * that row, column or subgrid) for each cell in the grid, and fills the cells
 * with the least number of possible candidates first.
 */

class LeastCandidatesCell : public StateStrategy<CellState>
{
    bool _findMany ;

public:
    
    /**
     * Creates a new LeastCandidatesCell strategy instance that optionally finds many
     * candidates (rather than just one) and selects from that set randomly.
     */
    
    LeastCandidatesCell( const bool findMany , 
                         const bool randomize ): 
                         StateStrategy<CellState>( randomize ), 
                         _findMany( findMany ) {}

    /**
     * Creates a new LeastCandidatesCell strategy instance that optionally selects 
     * from the set of candidates randomly.
     */
    
    LeastCandidatesCell( const bool randomize ): 
                         StateStrategy<CellState>( randomize ), 
                         _findMany( randomize ) {}

	/** 
	 * Finds the cells that have the least number of candidates. 
	 */

    virtual int findCandidates();
};

#endif

