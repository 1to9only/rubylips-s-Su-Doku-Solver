#include "com_act365_sudoku_solver.h"

#include "JSolver.hpp"

JNIEXPORT jint JNICALL Java_com_act365_sudoku_Solver__1solve( JNIEnv* pEnv , 
                                                              jobject solver , 
                                                              jobject strategy ,
                                                              jobject composeSolverStrategy ,
                                                              jint composeSolverThreshold , 
                                                              jint maxSolns , 
                                                              jboolean countUnwinds , 
                                                              jint maxUnwinds , 
                                                              jint maxComplexity )
{
    JSolver jSolver( pEnv , solver , strategy , composeSolverStrategy );

    return jSolver.solve( composeSolverThreshold , 
                          maxSolns , 
                          countUnwinds , 
                          maxUnwinds , 
                          maxComplexity );
}
