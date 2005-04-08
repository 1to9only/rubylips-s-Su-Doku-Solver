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
import java.text.DecimalFormat ;
import java.util.* ;

/**
 * A Composer object attempts to compose valid Su Doku puzzles with the
 * MostCandidates strategy. It runs as a thread because composition is
 * a lengthy process that the caller might well choose to terminate.
 */

public class Composer extends Thread {

    Vector puzzles ;
    
    GridContainer gridContainer ;

    int maxSolns ,
        maxMasks ,
        maxUnwinds ,
        maxComplexity ,
        nSolvers ,
        composeSolverThreshold ;
    
    boolean useNative ;
    
    MaskFactory maskFactory ;

    Solver[] solvers ;
        
    IStrategy[] composeSolvers ;

    boolean[][][] solverMasks ;
    
    Grid[] solverGrids ;
    
    PrintWriter debug ;
            
    transient int solverIndex ,
                  cellsInRow ,
                  maskSize ,
                  nSolns ,
                  nMasks ,
                  nThreads ,
                  maxPuzzleComplexity ,
                  mostComplex ,
                  tempSolutions ;
    
    transient boolean allSolutionsFound ;
    
    transient LeastCandidatesHybrid lch ;
    
    transient long startTime ;
    
    
    /**
     * Generates a new thread that will attempt to construct a Su Doku
     * puzzle with 'maskSize' cells on initial display. The puzzle will
     * be copied into 'grid'. When 'randomize' is selected, the mask 
     * will be selected randomly and the RadomMostCandidates strategy 
     * will be used to complete it.
     * @param gridContainer object to be notified when a puzzle has been composed
     * @param boxesAcross 
     * @param maxSolns maximum number of solutions to find (0 for no limit)
     * @param maxMasks maximum number of masks to use (0 for no limit)
     * @param maxUnwinds maximum permitted number of unwinds (0 for no limit)
     * @param maxComplexity maximum permitted complexity (0 for no limit)
     * @param maskFactory factory to generate the masks
     * @param nSolvers number of solver threads to run
     * @param composeSolverThreshold tree depth beyond which the compose solver will be invoked
     * @param debug optional debug stream (set to null for no debug)
     */    
    
    public Composer( GridContainer gridContainer ,
                     int boxesAcross ,
                     int maxSolns ,
                     int maxMasks ,
                     int maxUnwinds ,
                     int maxComplexity ,
                     MaskFactory maskFactory ,
                     int nSolvers ,
                     int composeSolverThreshold ,
                     PrintStream debug ,
                     boolean useNative ,
                     boolean leastCandidatesHybridFilter ) throws Exception {
        this.gridContainer = gridContainer ;                       
        this.maxSolns = maxSolns ;
        this.maxMasks = maxMasks ;
        this.maxUnwinds = maxUnwinds ;
        this.maxComplexity = maxComplexity ;
        this.maskFactory = maskFactory ;
        this.nSolvers = nSolvers ;
        this.composeSolverThreshold = composeSolverThreshold ;
        this.debug = debug instanceof PrintStream ? new PrintWriter( debug ) : null ;
        this.useNative = useNative ;

        maskSize = maskFactory.getFilledCells();
        cellsInRow = maskFactory.getCellsInRow();
        solvers = new Solver[nSolvers];
        composeSolvers = new LeastCandidatesHybrid[nSolvers];
        solverMasks = new boolean[nSolvers][cellsInRow][cellsInRow];
        solverGrids = new Grid[nSolvers];
        puzzles = new Vector();
        lch = new LeastCandidatesHybrid( false , true , true , true );
            
        int i = 0 ;
        while( i < nSolvers ){
            composeSolvers[i] = new LeastCandidatesHybrid( false , leastCandidatesHybridFilter , false , false );
            solverGrids[i] = new Grid( boxesAcross , cellsInRow / boxesAcross );
            ++ i ;
        }
        
        startTime = new Date().getTime();
    }
    
    /**
     * Called by a Solver object in order to indicate that a
     * solution has been found.
     * @see Solver
     * @param solverIndex index of the reporting solver
     */
    
    public synchronized void addSolution( int solverIndex ){
        // The grid might have been completed by the composeSolver,
        // in which case only certain cells should be read from the
        // solver grid.
        Grid solution = (Grid) solverGrids[solverIndex].clone();
        if( solverGrids[solverIndex].countFilledCells() != maskSize ){
            int r , c ;
            solverGrids[solverIndex].solve( lch , 1 );
            r = 0 ;
            while( r < cellsInRow ){
                c = 0 ;
                while( c < cellsInRow ){
                    if( solverMasks[solverIndex][r][c] ){
                        solution.data[r][c] = solverGrids[solverIndex].data[r][c] ;
                    } else {
                        solution.data[r][c] = 0 ;
                    }
                    ++ c ;
                }
                ++ r ;
            }            
            lch.reset();
        }
        // Ensure that he puzzle appears in standard form.
        Grid puzzle = (Grid) solution.clone(); 
        if( maxSolns != 1 ){
            puzzle.rectify( solverMasks[solverIndex] );
        }
        // Store (and report) the puzzle if it hasn't been seen before.
        if( ! puzzles.contains( puzzle ) ){
            puzzles.addElement( puzzle );
            if( !( gridContainer instanceof GridContainer ) ){
                puzzle.solve( lch , 2 );
                if( puzzle.complexity > maxComplexity ){
                    mostComplex = nSolns ;
                    maxPuzzleComplexity = puzzle.complexity ;                
                }
                if( debug instanceof PrintWriter ){
                    double t = ( new Date().getTime() - startTime )/ 1000. ;
                    debug.println( "Puzzle " + ( 1 + nSolns ) +":\n");
                    debug.println( "Puzzle Complexity = " + puzzle.complexity );
                    debug.println( "Puzzle Unwinds = " + puzzle.nUnwinds );
                    debug.println( "Cumulative Composer Complexity = " + solvers[solverIndex].complexity );
                    debug.println( "Cumulative Composer Unwinds = " + solvers[solverIndex].nUnwinds );
                    debug.println( "Time = " + new DecimalFormat("#0.000").format( t ) + "s" );
                    boolean multipleCategories = false ,
                            logical = puzzle.nUnwinds == 1 ; 
                    StringBuffer sb = new StringBuffer();
                    puzzle.solve( lch , 1 );
                    if( logical ){
                        if( lch.singleSectorCandidatesEliminations > 0 ){
                            if( multipleCategories ){
                                sb.append(":");
                            }
                            sb.append("Single Sector Candidates");
                            multipleCategories = true ;
                        }
                        if( lch.disjointSubsetsEliminations > 0 ){
                            if( multipleCategories ){
                                sb.append(":");
                            }
                            sb.append("Disjoint Subsets");
                            multipleCategories = true ;
                        }
                        if( lch.xWingsEliminations > 0 ){
                            if( multipleCategories ){
                                sb.append(":");
                            }
                            sb.append("X-Wings");
                            multipleCategories = true ;
                        }
                        if( lch.swordfishEliminations > 0 ){
                            if( multipleCategories ){
                                sb.append(":");
                            }
                            sb.append("Swordfish");
                            multipleCategories = true ;
                        }
                        if( lch.nishioEliminations > 0 ){
                            if( multipleCategories ){
                                sb.append(":");
                            }
                            sb.append("Nishio");
                            multipleCategories = true ;
                        }
                        if( sb.length() > 0 ){
                            debug.println( sb.toString() );
                        }
                    }
                    int i = 0 ;
                    while( i < lch.getThreadLength() ){
                        debug.print( ( 1 + i ) + ". " + lch.getReason(i) );
                        ++ i ;
                    }
                }
                lch.reset();
                if( debug instanceof PrintWriter ){
                    debug.println( puzzle.toString() );
                    debug.flush();
                }
            }
            if( ++ nSolns == maxSolns ){
                allSolutionsFound = true ;
                notifyAll();
            }
        }
    }
    
    /**
     * Called by a Solver object in order to indicate that all
     * possible solutions to the puzzle have been considered.
     * @see Solver
     * @param solverIndex index of finished solver
     */
    
    public synchronized void solverFinished( int solverIndex ){
        this.solverIndex = solverIndex ;
        -- nThreads ;
        notifyAll();    
    }

    /**
     * Starts a new Solver thread to find puzzles for
     * a fixed initial mask.
     * @see Solver
     * @param solverIndex index of solver to restart with a new mask
     */
    
    synchronized void startThread( int solverIndex ){
        boolean[][] mask = null ;
        try {
            mask = (boolean[][]) maskFactory.nextElement();
        } catch ( NoSuchElementException e ) {
            return ;
        }
        ++ nMasks ;
        int r , c ;
        r = 0 ;
        while( r < cellsInRow ){
            c = 0 ;
            while( c < cellsInRow ){
                solverMasks[solverIndex][r][c] = mask[r][c];
                ++ c ;
            }
            ++ r ;
        }
        solverGrids[solverIndex].reset();
        solvers[solverIndex] = new Solver( "Solver-" + ( solverIndex + 1 ) , 
                                           this ,
                                           solverIndex ,
                                           solverGrids[solverIndex] , 
                                           new MostCandidates( solverMasks[solverIndex] , true ) , 
                                           composeSolvers[solverIndex] ,
                                           composeSolverThreshold , 
                                           maxSolns , 
                                           maxUnwinds ,
                                           maxComplexity ,
                                           null ,
                                           useNative );
        solvers[solverIndex].start();
        ++ nThreads ;
    }
    
    /**
     * Starts a Composer thread. The thread will start a number of Solvers
     * and collate the returned results.
     * @see Solver
     */
    
    public void run(){
        nSolns = 0 ;
        nMasks = 0 ;
        allSolutionsFound = false ;
        solverIndex = -1 ;
        nThreads = 0 ;
        // Set off the solver threads, one per mask.
        int i = 0 ;
        while( i < nSolvers && ( maxMasks == 0 || nMasks < maxMasks ) ){
            startThread( i ++ );
        }
        // Wait for the reports back.
        synchronized( this ){
            while( ! allSolutionsFound && nThreads > 0 && ( maxMasks == 0 || nMasks <= maxMasks ) ){
                try {
                    while( solverIndex == -1 && 
                           allSolutionsFound == false && 
                           nThreads > 0 && 
                           ( maxMasks == 0 || nMasks <= maxMasks ) ){
                        wait();
                    }                
                } catch ( InterruptedException e ) {
                    break ;
                }
                if( solverIndex >= 0 ){
                    startThread( solverIndex );
                    solverIndex = -1 ;
                }
            }
            // Interrupt the remaining threads.
            i = 0 ;
            while( i < nSolvers ){
                if( solvers[i] instanceof Solver ){
                    solvers[i].interrupt();
                }
                ++ i ;
            }
            if( gridContainer instanceof GridContainer ){
                if( puzzles.size() > 0 ){
                    gridContainer.setGrid( (Grid) puzzles.elementAt( 0 ) );
                }
            } else {
                System.out.println( nSolns + " solutions found");
                if( nSolns > 0 ){
                    System.out.println("Most complex: (" + maxPuzzleComplexity + ")");
                    System.out.println( ((Grid) puzzles.elementAt( mostComplex ) ).toString() );
                }
            }
        }
    }            
    
    /**
     * Command-line app to compose Su Doku puzzles.
     * <br><code>Composer [-a across] [-d down] [-ms max solns|-mm max masks] [-mu max unwinds] [-s solvers] [-c threshold] [-r] [-v] -i|#cells</code>
     * <br><code>[-a across] [-d down]</code> define the dimensions of the puzzles to be composed. The default values are 
     * three in each dimension.
     * <br><code>[-ms max solns|-mm max masks]</code> defines optional termination conditions. The app will exit if <code>max solns</code>
     * puzzles have been generated or <code>max masks</code> masks have been considered, whichever occurs sooner.
     * <br><code>[-mu max unwinds]</code> stipulates a limit on the number of unwinds permitted on a single mask.
     * <br><code>[-mu max unwinds]</code> stipulates a limit on the complexity permitted on a single mask.
     * <br><code>[-s solvers]</code> stipulates the number of solver (or, equivalently, threads) to execute simultaneously. The default is 3.
     * <br><code>[-c threshold]</code> stipulates the tree depth beyond which the compose solver will be invoked. The default value is 0.
     * <br><code>[-r]</code> stipulates whether a random initial mask should be used. The default is no.
     * <br><code>[-v]</code> stipualtes whether the Composer should run in verbose mode. The default is no.
     * <br><code>[-n]</code> stipulates that the native library should be loaded
     * <br><code>[-f]</code> stipulates that the output from the Least Candidates Hybrid compose solver should be filter, i.e. that LCH II should be used.
     * <br><code>-i</code> stipulates that the initial mask should be read from standard input.
     * <br><code>#cells</code> stipulates the number of initially-filled cells to appear in the puzzles.   
     */

    public static void main( String[] args ){
        final String usage = "Usage: Composer [-a across] [-d down] [-ms max solns|-mm max masks] [-mu max unwinds] [-mc max complexity] [-s solvers] [-c threshold] [-r] [-v] [-n] [-f] -i|#cells";
        
        int boxesAcross = 3 ,
            boxesDown = 3 ,
            maxSolns = 0 ,
            maxMasks = 0 ,
            maxUnwinds = 0 ,
            maxComplexity = Integer.MAX_VALUE ,
            nSolvers = defaultThreads ,
            filledCells = 0 ,
            composeSolverThreshold = 0 ;
            
        boolean randomize = false ,
                trace = false ,
                standardInput = false ,
                useNative = false ,
                leastCandidatesHybridFilter = false ;
        
        // Process command-line args.
        if( args.length == 0 ){
            System.err.println( usage );
            System.exit( 1 );
        }           
        int i = 0 ;
        while( i < args.length - 1 ){
            if( args[i].equals("-a") ){
                try {
                    boxesAcross = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-d") ){
                try {
                    boxesDown = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-ms") ){
                try {
                    maxSolns = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-mm") ){
                try {
                    maxMasks = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-mu") ){
                try {
                    maxUnwinds = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-mc") ){
                try {
                    maxComplexity = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-s") ){
                try {
                    nSolvers = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-c") ){
                try {
                    composeSolverThreshold = Integer.parseInt( args[++i] ); 
                } catch ( NumberFormatException e ) {
                    System.err.println( usage );
                    System.exit( 1 );
                }
            } else if( args[i].equals("-r") ){
                randomize = true ;
            } else if( args[i].equals("-v") ){
                trace = true ;
            } else if( args[i].equals("-f") ){
                leastCandidatesHybridFilter = true ;
            } else if( args[i].equals("-n") ){
                try {
                    System.loadLibrary("SuDoku");
                    useNative = true ;
                } catch ( Exception e ) {
                    System.err.println("Native library could not be loaded");
                }
            } else {
                System.err.println( usage );
                System.exit( 1 );                
            }
            ++ i ;
        }
        if( maxMasks > 0 && maxSolns > 0 ){
            System.err.println("The -ms and -mm options are mutually exclusive");
            System.exit( 1 );
        }
        try {
            filledCells = Integer.parseInt( args[i] ); 
        } catch ( NumberFormatException e ) {
            if( args[i].equals("-i") ){
                standardInput = true ;
            } else {
                System.err.println( usage );
                System.exit( 1 );
            }
        }
        // Read a mask from standard input.
        MaskFactory maskFactory = null ;
        try {
            if( standardInput ){
                String text ;
                StringBuffer maskText = new StringBuffer();
                BufferedReader standardInputReader = new BufferedReader( new InputStreamReader( System.in ) );
                try {
                    while( ( text = standardInputReader.readLine() ) != null ){
                        if( text.length() == 0 ){
                            break ;
                        }
                        maskText.append( text );
                        maskText.append('\n');
                    }
                } catch ( IOException e ) {
                    System.err.println( e.getMessage() );
                    System.exit( 3 );               
                }
                maskFactory = new MaskFactory( maskText.toString() );                                
            } else {
                maskFactory = new MaskFactory( boxesAcross * boxesDown , filledCells , boxesAcross );
            }
        } catch ( Exception e ) {
            System.err.println( e.getMessage() );
            System.exit( 2 );
        }
        // Create the puzzles.
        try {
            new Composer( null , 
                          boxesAcross , 
                          maxSolns ,
                          maxMasks , 
                          maxUnwinds ,
                          maxComplexity ,
                          maskFactory , 
                          nSolvers , 
                          composeSolverThreshold , 
                          trace ? System.out : null ,
                          useNative ,
                          leastCandidatesHybridFilter ).start();  
        } catch ( Exception e ) {
            System.out.println( e.getMessage() );
            System.exit( 3 );
        }
    }
    
    public final static int defaultThreads = 3 ;
}
