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

#ifndef INCLUDED_JSOLVER
#define INCLUDED_JSOLVER

#include "Solver.hpp"

#include <jni.h>

#include <vector>

class JSolver
{
    int _boxesAcross ,
        _boxesDown ,
        _index ;

    bool _randomize ,
         _composeSolverRandomize ;

    jobject _composer ,
            _solver ;

    jfieldID _indexID ,
             _nSolnsID ,
             _nUnwindsID ,
             _complexityID ;

    jmethodID _addSolutionID ;

    std::vector<unsigned char> _data ;

    std::vector<jboolean> _isGridCopy , _isMaskCopy ;

    std::vector<jintArray> _gridData ;

    std::vector<jint*> _pGridData ;

    std::vector<bool> _mask ;

    std::vector<jbooleanArray> _maskData ;

    std::vector<jboolean*> _pMaskData ;

    JNIEnv* _pEnv ;

    Solver<JSolver> _theSolver ;

public:

    JSolver( JNIEnv* pEnv ,
             const jobject& solver ,
             const jobject& strategy ,
             const jobject& composeSolverStrategy );

    ~JSolver();

    int solve( const jint composeSolverThreshold , 
               const jint maxSolns , 
               const jboolean countUnwinds , 
               const jint maxUnwinds , 
               const jint maxComplexity );

    void operator()();
};

#endif
