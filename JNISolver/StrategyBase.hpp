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
 * StrategyBase implements functions common to many strategy types.
 */

#ifndef INCLUDED_STRATEGYBASE
#define INCLUDED_STRATEGYBASE

#include <vector>

class StrategyBase {

protected:

    unsigned char _boxesAcross ,
                  _boxesDown ,
                  _cellsInRow ;
    
    std::vector<unsigned char>* _pData ;
    
    // Thread variables
    
    std::vector<bool> _stateWrite ;
    
    std::vector<unsigned char> _xMoves , 
                               _yMoves ;
          
    int _nMoves ;
    
    // Candidates selected by findCandidates()
    
    std::vector<unsigned char> _xCandidates ,
                               _yCandidates ,
                               _valueCandidates ;
                    
    int _nCandidates ;
    
    // Whether the selection should be random.
    
    bool _randomize ;
    
    // Score
    
    int _score ;
    
    // Best candidate selected by selectCandidate()
    
    unsigned char _bestX ,
                  _bestY ,
                  _bestValue ;
    
    // Whether the underlying grid has been resized.
    
    bool _resize ;
    
public:
    
    /**
     * Creates a new base class with an optional random number generator.
     */

    StrategyBase( const bool randomize );
    
    unsigned char& grid( const unsigned char r , 
                         const unsigned char c )
    {
        return (*_pData)[ r * _cellsInRow + c ];
    }

    const unsigned char& grid( const unsigned char r , 
                               const unsigned char c ) const
    {
        return (*_pData)[ r * _cellsInRow + c ];
    }

    /**
     * Sets up the thread.
     */
    
    virtual void setup( const unsigned char boxesAcross ,
                        const unsigned char boxesDown ,
                        std::vector<unsigned char>* pData );

    /**
     * Selects a single candidate from the available list.
     */
    
    void selectCandidate();
    
    /**
     * Sets the value chosen by findCandidates().
     */

    void setCandidate() {
        grid( _bestX , _bestY ) = _bestValue ;
    }
    
    /**
     * Resets each cell that appears on the thread.
     */
    
    void reset();

    /**
     * Returns the x-coordinate of the best candidate move.
     */
    
    unsigned char getBestX() const { return _bestX ; }
    
    /**
     * Returns the y-coordinate of the best candidate move.
     */
    
    unsigned char getBestY() const { return _bestY ; }
    
    /**
     * Returns the value of the best candidate move.
     */
    
    unsigned char getBestValue() const { return _bestValue ; }
    
    /**
     * Returns the x-coordinate of the given candidate.
     */
    
    unsigned char getXCandidate( const int index ) const { return _xCandidates[ index ]; }

    /**
     * Returns the y-coordinate of the given candidate.
     */
    
    unsigned char getYCandidate( const int index ) const { return _yCandidates[ index ]; }

    /**
     * Returns the value-coordinate of the given candidate.
     */
    
    unsigned char getValueCandidate( const int index ) const { return _valueCandidates[ index ]; }

    /**
     * Returns the number of candidates.
     */    
    
    unsigned char getNumberOfCandidates() const { return _nCandidates ; }
    
    /**
     * Returns thread length.
     */
    
    unsigned char getThreadLength() const { return _nMoves ; }
    
    /**
     * Returns x-coordinate of move at given thread position.
     */
    
    unsigned char getThreadX( const int move ) const { return _xMoves[ move ]; }
    
    /**
     * Returns y-coordinate of move at given thread position.
     */
    
    unsigned char getThreadY( const int move ) const { return _yMoves[ move ]; }
    
    /**
     * Returns a measure of the confidence the strategy holds in its candidates.
     */
    
    int getScore() const { return _score ;}

    /**
     * Finds the candidates for which nInvunerable is lowest.
	 */
    
	virtual int findCandidates() = 0 ;

    /**
     * Updates state variables. MostCandidates always writes
     * its state to the stack.
     */    
    
    virtual void updateState( const unsigned char x , 
                              const unsigned char y , 
                              const unsigned char value , 
                              const bool writeState ) = 0 ;

    /**
     * Unwinds the the thread and reinstates state variables.
     * Note that when a puzzle is created, the first value
     * is set without loss of generality. Therefore the thread 
     * is only ever unwound until a single move remains.
     */
    
    virtual bool unwind( const int newNMoves , 
                         const bool reset ) = 0 ;
    
    /**
     * Returns the number of moves that had been made at the
     * last point where two alternative moves existed.
     */

    virtual int getLastWrittenMove() const = 0 ;
};
    
/**
 * Dumps the thread to the given output stream.
 */

 class ostream ;

std::ostream& operator << ( std::ostream& out , const StrategyBase& strategy );

#endif
