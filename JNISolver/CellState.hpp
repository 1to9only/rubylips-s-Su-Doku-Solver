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

/**
 * CellState records the number of candidates remain for each
 * separate cell on the grid.
 */

#ifndef INCLUDED_CELLSTATE
#define INCLUDED_CELLSTATE

#include <vector>

class CellState {

    // Grid size
    
    unsigned char _boxesAcross ,
                  _boxesDown ,
                  _cellsInRow ;
        
    // State variables
    
	std::vector<bool> _eliminated ;
    
	std::vector<unsigned char> _nEliminated ;
    
    // Thread
    
	std::vector< std::vector<bool> > _threadEliminated ;
    
	std::vector< std::vector<unsigned char> > _threadNEliminated ;
    
public:

    /**
     * Constructs an empty grid.
     */

    CellState();

    /**
     * Eliminates the move (r,c):=v as a possibility.
     */

    void eliminate( const unsigned char r ,
                    const unsigned char c ,
                    const unsigned char v ) 
    {
        _eliminated[(r*_cellsInRow+c)*_cellsInRow+v] = true ;    
    }

    /**
     * Indicates whether the move (r,c):=v has been eliminated.
     */

    bool isEliminated( const unsigned char r ,
                       const unsigned char c ,
                       const unsigned char v ) const 
    {
        return _eliminated[(r*_cellsInRow+c)*_cellsInRow+v];    
    }

    /**
     * Returns the number of values eliminated as candidates for the cell (r,c).
     */

    unsigned char& nEliminated( const unsigned char r ,
                                const unsigned char c )
    {
        return _nEliminated[r*_cellsInRow+c];
    }

    /**
     * Returns the number of values eliminated as candidates for the cell (r,c).
     */

    const unsigned char& nEliminated( const unsigned char r ,
                                      const unsigned char c ) const
    {
        return _nEliminated[r*_cellsInRow+c];
    }

	/**
     * Sets the state grid to the appropriate size.
	 */
     
	void setup( const unsigned char boxesAcross , 
	            const unsigned char boxesDown );

	/**
     * Writes the state grid to the stack at the appropriate position.
	 */
     
	void pushState( const int nMoves );

	/**
     * Reads the state grid from the stack at the appropriate position.
	 */
     
	void popState( const int nMoves );

	/**
     * Adds the move (r,c):=v to the state grid.
	 */
	
    void addMove( const unsigned char r , 
		          const unsigned char c , 
				  const unsigned char v );

	/**
     * Eliminates the move (r,c):=v from the current state grid.
	 */
     
	void eliminateMove( const unsigned char r , 
		                const unsigned char c , 
						const unsigned char v );
};

#endif
