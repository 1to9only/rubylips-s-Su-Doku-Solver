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

#include "JSolver.hpp"

#include "LeastCandidatesHybrid.hpp" 
#include "MostCandidates.hpp"
#include "Solver.hpp"

#include <cassert>

JSolver::JSolver( JNIEnv* pEnv ,
                  const jobject& solver ,
                  const jobject& strategy ,
                  const jobject& composeSolverStrategy )
                  :_pEnv( pEnv ),
                   _solver( solver )
{
    jclass solverClass = pEnv -> GetObjectClass( solver );
    // Access grid data.
    jfieldID gridID = pEnv -> GetFieldID( solverClass , "grid" , "Lcom/act365/sudoku/Grid;" );
    jobject grid = pEnv -> GetObjectField( solver , gridID );
    jclass gridClass = pEnv -> GetObjectClass( grid );
    jfieldID boxesAcrossID = pEnv -> GetFieldID( gridClass , "boxesAcross" , "I" ),
             boxesDownID = pEnv -> GetFieldID( gridClass , "boxesDown" , "I" ),
             dataID = pEnv -> GetFieldID( gridClass , "data" , "[[I" );
    _boxesAcross = (int) pEnv -> GetIntField( grid , boxesAcrossID );
    _boxesDown = (int) pEnv -> GetIntField( grid , boxesDownID );
    jobjectArray dataArrayObject = (jobjectArray) pEnv -> GetObjectField( grid , dataID );
    assert( pEnv -> GetArrayLength( dataArrayObject ) == _boxesAcross * _boxesDown );
    _data.resize( _boxesAcross * _boxesDown * _boxesAcross * _boxesDown );
    _isGridCopy.resize( _boxesAcross * _boxesDown );
    _gridData.resize( _boxesAcross * _boxesDown );
    _pGridData.resize( _boxesAcross * _boxesDown );
    int i = 0 , j ;
    while( i < _boxesAcross * _boxesDown ){
        _gridData[i] = (jintArray) pEnv -> GetObjectArrayElement( dataArrayObject , i );
        assert( pEnv -> GetArrayLength( _gridData[i] ) == _boxesAcross * _boxesDown );
        _pGridData[i] = pEnv -> GetIntArrayElements( _gridData[i] , & _isGridCopy[i] );
        j = 0 ;
        while( j < _boxesAcross * _boxesDown ){
            _data[ i * _boxesAcross * _boxesDown + j ] = (unsigned char) _pGridData[i][j];
            ++ j ;
        }
        ++ i ;
    }
    pEnv -> DeleteLocalRef( grid );
    pEnv -> DeleteLocalRef( gridClass );
    pEnv -> DeleteLocalRef( dataArrayObject );
    // Read composer.
    jfieldID composerID = pEnv -> GetFieldID( solverClass , "composer" , "Lcom/act365/sudoku/Composer;" );
    _composer = pEnv -> GetObjectField( solver , composerID );
    jclass composerClass = pEnv -> GetObjectClass( _composer );
    _addSolutionID = pEnv -> GetMethodID( composerClass , "addSolution" , "(I)V" );
    pEnv -> DeleteLocalRef( composerClass );
    // Read index.
    _indexID = pEnv -> GetFieldID( solverClass , "index" , "I" );
    _index = pEnv -> GetIntField( solver , _indexID );
    // Read transient variables.
    _complexityID = pEnv -> GetFieldID( solverClass , "complexity" , "I" );
    _nUnwindsID = pEnv -> GetFieldID( solverClass , "nUnwinds" , "I" );
    _nSolnsID = pEnv -> GetFieldID( solverClass , "nSolns" , "I" );
    // Read strategy and mask.
    jclass strategyBaseClass = pEnv -> FindClass("Lcom/act365/sudoku/StrategyBase;");
    jfieldID randomizeID = pEnv -> GetFieldID( strategyBaseClass , "randomize" , "Z" );
    _randomize = (bool) pEnv -> GetBooleanField( strategy , randomizeID );
    jclass strategyClass = pEnv -> GetObjectClass( strategy );
    jfieldID maskID = pEnv -> GetFieldID( strategyClass , "mask" , "[[Z" );
    jobjectArray maskArrayObject = (jobjectArray) pEnv -> GetObjectField( strategy , maskID );
    assert( pEnv -> GetArrayLength( maskArrayObject ) == _boxesAcross * _boxesDown );
    _mask.resize( _boxesAcross * _boxesDown * _boxesAcross * _boxesDown );
    _isMaskCopy.resize( _boxesAcross * _boxesDown );
    _maskData.resize( _boxesAcross * _boxesDown );
    _pMaskData.resize( _boxesAcross * _boxesDown );
    i = 0 ;
    while( i < _boxesAcross * _boxesDown ){
        _maskData[i] = (jbooleanArray) pEnv -> GetObjectArrayElement( maskArrayObject , i );
        assert( pEnv -> GetArrayLength( _maskData[i] ) == _boxesAcross * _boxesDown );
        _pMaskData[i] = pEnv -> GetBooleanArrayElements( _maskData[i] , & _isMaskCopy[i] );
        j = 0 ;
        while( j < _boxesAcross * _boxesDown ){
            _mask[ i * _boxesAcross * _boxesDown + j ] = (bool) _pMaskData[i][j];
            ++ j ;
        }
        ++ i ;
    }
    pEnv -> DeleteLocalRef( maskArrayObject );
    pEnv -> DeleteLocalRef( strategyClass );
    // Read compose solver strategy.
    jfieldID composeSolverRandomizeID = pEnv -> GetFieldID( strategyBaseClass , "randomize" , "Z" );
    _composeSolverRandomize = (bool) pEnv -> GetBooleanField( composeSolverStrategy , composeSolverRandomizeID );
    jclass composeSolverClass = pEnv -> GetObjectClass( composeSolverStrategy );
    pEnv -> DeleteLocalRef( composeSolverClass );
    pEnv -> DeleteLocalRef( solverClass );
    pEnv -> DeleteLocalRef( strategyBaseClass );
    // Prepare the solver.
    _theSolver.setup( _boxesAcross , _boxesDown , & _data );
}
    
JSolver::~JSolver()
{
    int i = 0 ;
    while( i < _boxesAcross * _boxesDown ){
        if( _isGridCopy[i] == JNI_TRUE ){
            _pEnv -> ReleaseIntArrayElements( _gridData[i] , _pGridData[i] , 0 );
        }
        _pEnv -> DeleteLocalRef( _gridData[i] );
        if( _isMaskCopy[i] == JNI_TRUE ){
            _pEnv -> ReleaseBooleanArrayElements( _maskData[i] , _pMaskData[i] , 0 );
        }
        _pEnv -> DeleteLocalRef( _maskData[i] );
        ++ i ;
    }
    _pEnv -> DeleteLocalRef( _solver );
    _pEnv -> DeleteLocalRef( _composer );
}

void JSolver::operator()() 
{
    unsigned char i , j ;
    i = 0 ;
    while( i < _boxesAcross * _boxesDown ){
        j = 0 ;
        while( j < _boxesAcross * _boxesDown ){
            _pGridData[i][j] = _data[ i * _boxesAcross * _boxesDown + j ];
            ++ j ;
        }
        if( _isGridCopy[i] == JNI_TRUE ){
            _pEnv -> ReleaseIntArrayElements( _gridData[i] , _pGridData[i] , 0 );
            _pGridData[i] = _pEnv -> GetIntArrayElements( _gridData[i] , & _isGridCopy[i] );
        }
        ++ i ;
    }
    _pEnv -> SetIntField( _solver , _complexityID , _theSolver.getComplexity() );
    _pEnv -> SetIntField( _solver , _nSolnsID , _theSolver.getNumberOfSolutions() );
    _pEnv -> SetIntField( _solver , _nUnwindsID , _theSolver.getNumberOfUnwinds() );
    _pEnv -> CallVoidMethod( _composer , _addSolutionID , _index );
}

int JSolver::solve( const jint composeSolverThreshold , 
                    const jint maxSolns , 
                    const jboolean countUnwinds , 
                    const jint maxUnwinds , 
                    const jint maxComplexity )
{    
    MostCandidates mostCandidates( _mask , _randomize );

    LeastCandidatesHybrid leastCandidatesHybrid( _composeSolverRandomize );

    return _theSolver.solve( & mostCandidates , 
                             & leastCandidatesHybrid , 
                             * this ,
                             composeSolverThreshold ,
                             maxSolns ,
                             countUnwinds ,
                             maxUnwinds ,
                             maxComplexity );
}
