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

#ifndef INCLUDED_SOLVER
#define INCLUDED_SOLVER

#include "StrategyBase.hpp"

#include <vector>

template <class NullOp >
class Solver 
{
    int _nSolns ,
        _nUnwinds ,
        _complexity ,
        _boxesAcross ,
        _boxesDown ;

    std::vector<unsigned char>* _pData ;

    /**
     * Counts the number of filled cells on the grid.
     */

    int countFilledCells() const ;

public:

    /**
     * Creates a Solver.
     */

    Solver():_nSolns( 0 ), _nUnwinds( 0 ), _complexity( 0 ) {}

    /**
     * Sets up the solver to solve the given grid.
     */

    void setup( const unsigned char boxesAcross ,
                const unsigned char boxesDown ,
                std::vector<unsigned char>* pData );

    /**
     * Finds and counts solutions for the grid.
     */

    int solve( StrategyBase* strategy , 
               StrategyBase* pComposeSolverStrategy , 
               NullOp& callback ,
               const int composeSolverThreshold ,
               const int maxSolns ,
               const bool countUnwinds ,
               const int maxUnwinds ,
               const int maxComplexity );

    /**
     * Returns the complexity of the most recently-solved puzzle.
     */

    int getComplexity() const { return _complexity ;}

    /**
     * Returns the number of unwinds tht had to be performed 
     * in order to solve the most recent puzzle.
     */
     
    int getNumberOfUnwinds() const { return _nUnwinds ;}

    /**
     * Returns the number of solutions found for the most
     * recent puzzle.
     */

    int getNumberOfSolutions() const { return _nSolns ;}
};

template <class NullOp>
void Solver<NullOp>::setup( const unsigned char boxesAcross ,
                            const unsigned char boxesDown ,
                            std::vector<unsigned char>* pData )
{
    _boxesAcross = boxesAcross ;
    _boxesDown = boxesDown ;
    _pData = pData ;
}

template <class NullOp >
int Solver<NullOp>::solve( StrategyBase* pStrategy , 
                           StrategyBase* pComposeSolverStrategy , 
                           NullOp& callback ,
                           const int composeSolverThreshold ,
                           const int maxSolns ,
                           const bool countUnwinds ,
                           const int maxUnwinds ,
                           const int maxComplexity )
{
    int nSolns = 0 , nComposeSolns = 2 , count , lastWrittenMove ;
    if( countUnwinds ){
        _nUnwinds = _complexity = 0 ;
    }
    pStrategy -> setup( _boxesAcross , _boxesDown , _pData );
    // Try to find a valid move.
    while( true ){
        if( pStrategy -> findCandidates() > 0 ){
            pStrategy -> selectCandidate();
            pStrategy -> setCandidate();
            pStrategy -> updateState( pStrategy -> getBestX() , 
                                      pStrategy -> getBestY() , 
                                      pStrategy -> getBestValue() , 
                                      pStrategy -> getScore() > 1 );
            count = countFilledCells();
            if( pComposeSolverStrategy && count >= composeSolverThreshold ){
                nComposeSolns = solve( pComposeSolverStrategy , NULL , callback , 0 , 2 , false , 0 , 0 );
                if( nComposeSolns == 0 ){
                    nComposeSolns = 2 ;
                    // No solutions exist - that's no good.
                    pComposeSolverStrategy -> reset();
                    lastWrittenMove = pStrategy -> getLastWrittenMove();
                    _complexity += pStrategy -> getThreadLength() - lastWrittenMove ;
                    if( countUnwinds && ( ++ _nUnwinds == maxUnwinds || _complexity >= maxComplexity ) || ! pStrategy -> unwind( lastWrittenMove , true ) ){
                        return _nSolns = nSolns ;
                    }
                }
            }
            if( count == _boxesAcross * _boxesAcross * _boxesDown * _boxesDown || nComposeSolns == 1 ){
                nComposeSolns = 2 ;
                // Grid has been solved.
                if( pComposeSolverStrategy ){
                    callback();
                }
                if( ++ nSolns == maxSolns ){ 
                    return _nSolns = nSolns ;
                }
                lastWrittenMove = pStrategy -> getLastWrittenMove();
                _complexity += pStrategy -> getThreadLength() - lastWrittenMove ;
                if( countUnwinds && ( ++ _nUnwinds == maxUnwinds || _complexity >= maxComplexity ) || ! pStrategy -> unwind( lastWrittenMove , true ) ){
                    return _nSolns = nSolns ;
                }
            } else if( pComposeSolverStrategy && count >= composeSolverThreshold ){
                pComposeSolverStrategy -> reset();
            }
        } else {
            // Stuck
            lastWrittenMove = pStrategy -> getLastWrittenMove();
            _complexity += pStrategy -> getThreadLength() - lastWrittenMove ;
            if( countUnwinds && ( ++ _nUnwinds == maxUnwinds || _complexity >= maxComplexity ) || ! pStrategy -> unwind( lastWrittenMove , true ) ){
                return _nSolns = nSolns ;
            }
        }
    }
}

template <class NullOp >
int Solver<NullOp>::countFilledCells() const
{
    int count = 0 ;
    std::vector<unsigned char>::const_iterator it = _pData -> begin();
    while( it != _pData -> end() ){
        count += *( it ++) > 0 ;
    }
    return count ;
}

#endif
