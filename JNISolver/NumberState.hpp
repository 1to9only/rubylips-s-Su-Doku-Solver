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

#ifndef INCLUDED_NUMBERSTATE
#define INCLUDED_NUMBERSTATE 

#include <vector>

/**
 * NumberState records the number of candidates remain for each
 * value/sector on the grid, where a sector is any row, column
 * or subgrid.
 */

class NumberState {

    // Grid size
    
    unsigned char _boxesAcross ,
                  _boxesDown ,
                  _cellsInRow ;
        
    // State variables
    
    std::vector<bool> _eliminated ,
                      _isFilled ;
    
    std::vector<unsigned char> _nEliminated ;
    
    // Thread
    
    std::vector< std::vector<bool> > _threadEliminated ,
                                     _threadIsFilled ;
    
    std::vector< std::vector<unsigned char> > _threadNEliminated ;
    
public:

    /**
     * Creates a blank state grid.
     */

    NumberState();

    /**
     * Eliminates the move (x,y):=v as a possibility.
     */

    void eliminate( const unsigned char v ,
                    const unsigned char x ,
                    const unsigned char y ) 
    {
        _eliminated[(v*3*_cellsInRow+x)*_cellsInRow+y] = true ;    
    }

    /**
     * Indicates whether the move (x,y):=v has been eliminated.
     */

    bool isEliminated( const unsigned char v ,
                       const unsigned char x ,
                       const unsigned char y ) const 
    {
        return _eliminated[(v*3*_cellsInRow+x)*_cellsInRow+y];    
    }

    /**
     * Fills the move (x,*):=v.
     */

    void fill( const unsigned char v ,
               const unsigned char x ,
               const bool filled )
    {
        _isFilled[v*3*_cellsInRow+x] = filled ;
    }
    
    /**
     * Indicates whether the move (x,*):=v has been filled.
     */

    bool isFilled( const unsigned char v ,
                   const unsigned char x ) const
    {
        return _isFilled[v*3*_cellsInRow+x];
    }
    
    /**
     * Returns the number of values eliminated as candidates for the move (x,*):=v.
     */

    unsigned char& nEliminated( const unsigned char v ,
                                const unsigned char x )
    {
        return _nEliminated[v*3*_cellsInRow+x];
    }

    /**
     * Returns the number of values eliminated as candidates for the move (x,*):=v.
     */

    const unsigned char& nEliminated( const unsigned char v ,
                                      const unsigned char x ) const
    {
        return _nEliminated[v*3*_cellsInRow+x];
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
