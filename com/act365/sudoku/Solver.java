/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com January 2004
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

package com.act365.sudoku;

import java.io.* ;

/**
 * A Solver instance solves a grid on a thread that exits
 * gracefully if interrupted.
 */

public class Solver extends Thread {

    Grid grid ;
    
    IStrategy strategy ,
              composeSolver ;
    
    int maxSolns ,
        maxUnwinds ,
        composeSolverThreshold ,
        index ;
    
    Composer composer ;
    
    PrintWriter debug ;
    
    transient int nUnwinds ,
                  nSolns ;
    
    /**
     * Creates a Solver instance.
     * @param threadGroup thread group to which the solver will belong
     * @param threadName thread name
     * @param composer composer object to which to report
     * @param index composer-defined index number for solver
     * @param grid grid to be solved
     * @param strategy strategy to be used to complete the grid
     * @param composeSolver solver to be used at each step by the composer in order to check solution uniqueness
     * @param maxSolns the maximum number of solutions to find before exit (0 for unlimited solutions)
     * @param maxUnwinds the maximum permitted number of unwinds (0 for no limit)
     * @param debug (optional) destination for debug info
     */
    
    public Solver( ThreadGroup threadGroup ,
                   String threadName ,
                   Composer composer ,
                   int index ,
                   Grid grid ,
                   IStrategy strategy ,
                   IStrategy composeSolver ,
                   int composeSolverThreshold ,
                   int maxSolns ,
                   int maxUnwinds ,
                   PrintStream debug ){
        super( threadGroup , threadName );
        this.composer = composer ;
        this.index = index ;
        this.grid = grid ;
        this.strategy = strategy ;
        this.composeSolver = composeSolver ;
        this.composeSolverThreshold = composeSolverThreshold ;
        this.maxSolns = maxSolns ;
        this.maxUnwinds = maxUnwinds ;
        this.debug = debug instanceof PrintStream ? new PrintWriter( debug ) : null ; 
    }
    
    /**
     * Creates a Solver instance.
     * @param grid grid to be solved
     * @param strategy strategy to be used to complete the grid
     * @param composeSolver solver to be used at each step by the composer in order to check solution uniqueness
     * @param maxSolns the maximum number of solution to find before exit (0 for no limit)
     * @param debug (optional) destination for debug info
     */
    
    public Solver( Grid grid ,
                   IStrategy strategy ,
                   IStrategy composeSolver ,
                   int composeSolverThreshold ,
                   int maxSolns ,
                   PrintStream debug ){
        this.composer = null ;
        this.index = 0 ;                     
        this.grid = grid ;
        this.strategy = strategy ;
        this.composeSolver = composeSolver ;
        this.composeSolverThreshold = composeSolverThreshold ;
        this.maxSolns = maxSolns ;
        this.maxUnwinds = 0 ;
        this.debug = debug instanceof PrintStream ? new PrintWriter( debug ) : null ;
    }

    /**
     * Runs the solver on a thread.
     */    
    
    public void run(){
        nSolns = solve( strategy , composeSolver , composeSolverThreshold , maxSolns , true , maxUnwinds );
        if( composer instanceof Composer ){
            composer.solverFinished( index );
        }
    }
    
    /**
     * Returns the number of solutions found.
     */
    
    public int getNumberOfSolutions(){
        return nSolns ;
    }
    
    /**
     * Returns the number of times the tree had to be unwound 
     * in order to solve the grid.
     */
    
    public int getNumberOfUnwinds(){
        return nUnwinds ;
    }
    
    /**
     * Solves the grid.
     * @param strategy main strategy to use
     * @param composeSolver (optional) strategy used at each step in order to check for uniqueness
     * @param maxSolns (optional) maximum number of solutions to be found before exit
     * @param maskSize the number of initially-filled cells 
     * @param countUnwinds whether the number of unwinds should be counted
     * @return the number of solutions found
     */
    
    int solve( IStrategy strategy , 
               IStrategy composeSolver , 
               int composeSolverThreshold ,
               int maxSolns ,
               boolean countUnwinds ,
               int maxUnwinds ){
        int i , nSolns = 0 , nComposeSolns = 2 , count ;
        if( countUnwinds ){
            nUnwinds = 0 ;
        }
        if( ! strategy.setup( grid ) ){
            return nSolns ;
        }
        // Solve the grid.
        while( ! isInterrupted() ){
            // Try to find a valid move.
            if( strategy.findCandidates() > 0 ){
                strategy.selectCandidate();
                strategy.setCandidate();
                if( ! strategy.updateState( strategy.getBestX() , strategy.getBestY() , strategy.getBestValue() ) ){
                    return nSolns ;
                }
                count = grid.countFilledCells();
                if( composeSolver instanceof IStrategy && count >= composeSolverThreshold ){
                    nComposeSolns = solve( composeSolver , null , 0 , 2 , false , 0 );
                    if( nComposeSolns == 0 ){
                        nComposeSolns = 2 ;
                        // No solutions exist - that's no good.
                        composeSolver.reset();
                        if( countUnwinds && ++ nUnwinds == maxUnwinds ){
                            return nSolns ;
                        }
                        if( ! strategy.unwind( true ) ){
                            return nSolns ;
                        }  
                        continue ;                      
                    }
                }
                if( count == grid.cellsInRow * grid.cellsInRow || nComposeSolns == 1 ){
                    nComposeSolns = 2 ;         
                    // Grid has been solved.
                    if( composeSolver instanceof IStrategy && composer instanceof Composer ){
                        composer.addSolution( index );
                    }
                    if( debug instanceof PrintWriter ){
                        debug.println( ( 1 + nSolns ) + ".");
                        debug.println( grid.toString() );
                        debug.flush();
                    }
                    if( ++ nSolns == maxSolns ){ 
                        return nSolns ;
                    }
                    if( countUnwinds && ++ nUnwinds == maxUnwinds ){
                        return nSolns ;
                    }
                    if( ! strategy.unwind( true ) ){
                        return nSolns ;
                    }
                } else if( composeSolver instanceof IStrategy ){
                    composeSolver.reset();
                }
            } else {
                // Stuck
                if( countUnwinds && ++ nUnwinds == maxUnwinds ){
                    return nSolns ;
                }
                if( ! strategy.unwind( true ) ){
                    return nSolns ;
                }
            }
        }
        
        return nSolns ;
    }
    
    /**
     * Command-line app to solve Su Doku puzzles.
     * <br><code>Solver [-m max solutions] [-s strategy] [-v] [-u max unwinds]</code>
     * <br><code>[-m max solutions]</code> stipulates the maximum number of solutions to be reported. 
     * The default is for all solutions to be reported.
     * <br><code>[-s strategy]</code> stipulates the strategy to be used. the default is Least Candidates Hybrid.
     * <br><code>[-v]</code> stipulates whether the app should execute in verbose mode. The default is no.
     * <br><code>[-u max unwinds]</code> stipulates the maximum number of unwinds to be performed. 
     * The default is for there to be no limit.
     * <br> The puzzle will be read from standard input.  
     */
    
    public static void main( String[] args ){
        
        final String usage = "Usage: Solver [-m max solutions] [-s strategy] [-v] [-u max unwinds]";
        
        boolean debug = false ;
        
        int i , maxSolns = 0 , maxUnwinds = 0 ;
        
        String strategyLabel = "Least Candidates Hybrid";
        
        i = 0 ;
        while( i < args.length ){
            if( args[i].equals("-m") ){
                try {
                    maxSolns = Integer.parseInt( args[++i] );
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-u") ) {
                try {
                    maxUnwinds = Integer.parseInt( args[++i] );
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-v") ) {
                debug = true ;
            } else if( args[i].equals("-s") ){
                strategyLabel = args[++i];
            } else {
                System.err.println( usage );
                System.exit( 1 );
            }
            ++ i ;
        }
        // Create the strategy.
        IStrategy strategy ;       
        if( ( strategy = Strategy.create( strategyLabel ) ) == null ){
            System.err.println("Unsupported strategy");
            System.exit( 2 );
        }
        // Read the grid from standard input. A blank line will terminate
        // the read.
        Grid grid = new Grid();
        String text ;
        StringBuffer gridText = new StringBuffer();
        BufferedReader standardInputReader = new BufferedReader( new InputStreamReader( System.in ) );
        try {
            while( ( text = standardInputReader.readLine() ) != null ){
                if( text.length() == 0 ){
                    break ;
                }
                gridText.append( text );
                gridText.append('\n');
            }
            grid.populate( gridText.toString() );
        } catch ( IOException e ) {
            System.err.println( e.getMessage() );
            System.exit( 3 );
        }
        // Solve.
        Solver solver = new Solver( grid , strategy , null , 0 , maxSolns , debug ? System.out : null );
        solver.start();
        try {
            solver.join();
        } catch ( InterruptedException e ){
            System.out.println("Solver interrupted");
        }
        System.out.println( solver.getNumberOfSolutions() + " solutions found.");
    }
}
