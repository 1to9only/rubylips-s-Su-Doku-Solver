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

#ifndef INCLUDED_LEASTCANDIDATESHYBRID
#define INCLUDED_LEASTCANDIDATESHYBRID

#include "LeastCandidatesCell.hpp"
#include "LeastCandidatesNumber.hpp"
#include "StrategyBase.hpp"

/**
 * LeastCandidatesHybrid combines the Least Candidates Cell and Least 
 * Candidates Number strategies. 
 */

class LeastCandidatesHybrid : public StrategyBase {

    LeastCandidatesNumber _lcn ;
    
    LeastCandidatesCell _lcc ;

public:

    /**
     * Sets up a LeastCandidatesHybrid strategy with an optional random element.
     */    
    
    LeastCandidatesHybrid( const bool randomize );

    /**
     * Sets up the strategy to solve the given grid.
     */
        
    virtual void setup( const unsigned char boxesAcross ,
                        const unsigned char boxesDown ,
                        std::vector<unsigned char>* pData );
    
	/**
     * Finds candidates for the next move.
	 */
     
	virtual int findCandidates();

	/** 
     * Updates state variables.
	 */
    
    virtual void updateState( const unsigned char x , 
                              const unsigned char y , 
                              const unsigned char value , 
                              const bool writeState );

	/**
     * Unwind the stack.
	 */
    
    virtual bool unwind( const int newNMoves , const bool reset ); 
    
    /**
     * Determines the last move for which two or more alternatives existed.
     */
    
    virtual int getLastWrittenMove() const ;
};

#endif