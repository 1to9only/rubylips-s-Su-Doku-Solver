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

#ifndef INCLUDED_INVULNERABLESTATE
#define INCLUDED_INVULNERABLESTATE

#include <vector>

/**
 * InvulnerableState records the number of cells that would remain
 * unresolved for each given move (ie each cell/value pair).
 */

class InvulnerableState 
{

    // Grid size
    
    int _boxesAcross ,
        _boxesDown ,
        _cellsInRow ;
        
    // State variables
    
    std::vector<bool> _eliminated ;
    
    std::vector<unsigned char> _nInvulnerable ;
    
    // Thread
    
    std::vector< std::vector<bool> > _threadEliminated ;
    
    std::vector< std::vector<unsigned char> > _threadNInvulnerable ;
    
    /**
     * Calculates whether (p,q) is in the domain of (x,y), 
     * i.e. whether it shares a column, row or subgrid.
     */
    
    bool inDomain( const unsigned char x , 
                   const unsigned char y , 
                   const unsigned char p , 
                   const unsigned char q ) const ;

public:

    /**
     * Creates an empty state grid.
     */

    InvulnerableState():_boxesAcross( 0 ), _boxesDown( 0 ), _cellsInRow( 0 ) {}

    /**
     * Eliminates the move (x,y):=v as a possibility.
     */

    void eliminate( const unsigned char x ,
                    const unsigned char y ,
                    const unsigned char v ) 
    {
        _eliminated[(x*_cellsInRow+y)*_cellsInRow+v] = true ;    
    }

    /**
     * Indicates whether the move (x,y):=v has been eliminated.
     */

    bool isEliminated( const unsigned char x ,
                       const unsigned char y ,
                       const unsigned char v ) const 
    {
        return _eliminated[(x*_cellsInRow+y)*_cellsInRow+v];    
    }

    /**
     * Returns the number of values eliminated as candidates for the move (x,y):=v.
     */

    unsigned char& nInvulnerable( const unsigned char x ,
                                  const unsigned char y ,
                                  const unsigned char v )
    {
        return _nInvulnerable[(x*_cellsInRow+y)*_cellsInRow+v];
    }

    /**
     * Returns the number of values eliminated as candidates for the move (x,y):=v.
     */

    const unsigned char& nInvulnerable( const unsigned char x ,
                                        const unsigned char y ,
                                        const unsigned char v ) const
    {
        return _nInvulnerable[(x*_cellsInRow+y)*_cellsInRow+v];
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
     * Reads the state gris from the stack at the appropriate position.
     */
          
	void popState( const int nMoves );

    /**
     * Adds the move (x,y):=v to the state grid.
     */

	void addMove( const unsigned char x , 
                  const unsigned char y , 
                  const unsigned char value );

    /**
     * Eliminates the move (x,y):=v from the current state grid.
     */
     
	void eliminateMove( const unsigned char x , 
                        const unsigned char y , 
                        const unsigned char value );
};

#endif