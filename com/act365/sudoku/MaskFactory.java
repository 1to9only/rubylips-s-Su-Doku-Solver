/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com January 2005
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
import java.util.* ;

/**
 * The MaskFactory class iterates through the complete set of possible
 * masks (a mask is a boolean[][] array that indicates whether the 
 * corresponding cells in Su Doku grids should have their initial values 
 * exposed) of a given size. The masks are guaranteed to have rotational
 * symmetry of order two, as demanded by the Su Doku puzzle. No mask 
 * will be a reflection of a mask that appeared earlier in the sequence.
 */

public class MaskFactory implements Enumeration {
    
    int nBalls ,
        nSlots ,
        cellsInRow ,
        filledCells ;
        
    int[] g0 , g ;
    
    boolean fillCentreCell ,
            haveIterated ;
    
    boolean[][] mask ,
                previousMask ;
    
    /**
     * Creates a new MaskFactory. The sequence starts in its natural start
     * position, which is usually some distance from the masks that are 
     * likely to lead to successful puzzles.
     * @param cellsInRow size of grid
     * @param filledCells number of true elements to appear in mask
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( int cellsInRow , int filledCells ) throws Exception {
        resize( cellsInRow , filledCells );
        initiate( false );
    }

    /**
     * Creates a new MaskFactory. The sequence starts at an optionally
     * random position.
     * @param cellsInRow size of grid
     * @param filledCells number of true elements to appear in mask
     * @param randomize whether the start position should be randomized
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( int cellsInRow , 
                        int filledCells ,
                        boolean randomize ) throws Exception {
        this( cellsInRow , filledCells );
        initiate( randomize );
    }

    /**
     * Creates a new MaskFactory. The sequence starts at the given
     * mask.
     * @param cellsInRow size of grid
     * @param filledCells number of true elements to appear in mask
     * @param mask start mask
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( int cellsInRow , 
                        int filledCells ,
                        boolean[][] mask ) throws Exception {
        this( cellsInRow , filledCells );
        initiate( mask );
    }

    /**
     * Creates a new MaskFactory. The sequence starts at the mask 
     * specified by a given gap sequence.
     * @param cellsInRow size of grid
     * @param filledCells number of true elements to appear in mask
     * @param gaps gap sequence that defines the start mask
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( int cellsInRow , 
                        int filledCells ,
                        int[] gaps ) throws Exception {
        this( cellsInRow , filledCells );
        initiate( gaps );
    }

    /**
     * Creates a new MaskFactory. The sequence starts at the mask specified
     * by the string, which should be in the format used by
     * <code>toString()</code>.
     * @param s string representation of start mask
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( String s ) throws Exception {
        initiate( s );
    }

    /**
     * Creates a new MaskFactory. The sequence will start from a mask with
     * uniformly-distributed filled cells.
     * @param cellsInRow size of grid
     * @param filledCells number of true elements to appear in mask
     * @param boxesAcross grid dimension
     * @throws Exception thrown if cellsInRow and filledCells are incompatible
     */
    
    public MaskFactory( int cellsInRow , 
                        int filledCells ,
                        int boxesAcross ) throws Exception {
        this( cellsInRow , filledCells );
        initiate( boxesAcross );
    }

    /**
     * Resizes the masks produced by the factory.
     * @param cellsInRow number of cell per row on the grid 
     * @param filledCells number of cells on the grid that will be initially filled
     * @throws Exception throw when cells in row is even and filled cells is odd
     */
    
    void resize( int cellsInRow , int filledCells ) throws Exception {
        if( this.cellsInRow == cellsInRow && this.filledCells == filledCells ){
            return ;
        }
        this.cellsInRow = cellsInRow ;
        this.filledCells = filledCells ;
        mask = new boolean[cellsInRow][cellsInRow];
        previousMask = new boolean[cellsInRow][cellsInRow];
        // Calculate the number of 'balls' to be fitted into the 'slots'.
        if( cellsInRow % 2 == 1 ){
            if( filledCells % 2 == 1 ){
                fillCentreCell = true ; 
                nBalls = ( filledCells - 1 )/ 2 ;
            } else {
                fillCentreCell = false ;                     
                nBalls = filledCells / 2 ;                       
            }
            nSlots = ( cellsInRow * cellsInRow - 1 )/ 2 ;
        } else {
            if( filledCells % 2 == 1 ){
                throw new Exception("Number of filled cells should be even");
            } else {
                fillCentreCell = false ; // Default setting - there's no centre cell
                nBalls = filledCells / 2 ;
                nSlots = cellsInRow * cellsInRow / 2 ;
            }
        }        
        g = new int[ nBalls + 1 ];
        g0 = new int[ nBalls + 1 ];        
    }
    
    /**
     * Generates the next mask. It's guaranteed that the new mask
     * will not simply be a reflection of an earlier mask. When the
     * dimension of the associated grid is given, the mask will
     * iterate until the balls are uniformly-distributed across the grid.
     */
    
    void iterate( int boxesAcross ){
        int i ;
        int[] reflection = new int[nBalls+1];
        while(true){
            iterateGaps();
            generateMask();
            // Check top/bottom
            i = 0 ;
            while( i < reflection.length ){
                reflection[i++] = 0 ;
            }
            reflectTopBottom( reflection );
            if( ! precedes( reflection ) ){
                continue ;
            }
            // Check left/right
            i = 0 ;
            while( i < reflection.length ){
                reflection[i++] = 0 ;
            }
            reflectLeftRight( reflection );
            if( ! precedes( reflection ) ){
                continue ;
            }
            // Check top-left/bottom-right
            i = 0 ;
            while( i < reflection.length ){
                reflection[i++] = 0 ;
            }
            reflectTopLeftBottomRight( reflection );
            if( ! precedes( reflection ) ){
                continue ;
            }
            // Check top-right/bottom-left
            i = 0 ;
            while( i < reflection.length ){
                reflection[i++] = 0 ;
            }
            reflectTopRightBottomLeft( reflection );
            if( ! precedes( reflection ) ){
                continue ;
            }
            // Check for uniformity.
            if( ! uniform( boxesAcross ) ){
                continue ;
            }
            // All tests passed.
            return ;
        }
    }
    
    /**
     * Generates the next set of gaps. NB The new gaps will be rejected
     * if they are merely a reflection of a set seen before.
     */
    
    void iterateGaps(){
        int i , s = 0 ;
        while( ++ s <= nBalls && g[s] == 0 );
        if( s <= nBalls ){
            -- g[s];
            g[s-1] = 0 ;
            i = nBalls + 1 ;
            while( i > s ){
                g[s-1] -= g[--i];
            }
            g[--i] += nSlots - nBalls ;
            while( i > 0 ){
                g[--i] = 0 ;
            }
        } else {
            g[nBalls] = nSlots - nBalls ;
            i = nBalls ;
            while( i > 0 ){
                g[--i] = 0 ;
            }
        }
    }
    
    /**
     * Generates a mask from the g[] array.
     */
    
    void generateMask(){    
        int i , j , k ;
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                mask[i][j] = false ;
                ++ j ;
            }
            ++ i ;
        }
        i = j = k = 0 ;
        while( k < nBalls ){
            j += g[k++];
            i += j / cellsInRow ;
            j = j % cellsInRow ;
            mask[i][j] = mask[cellsInRow-1-i][cellsInRow-1-j] = true ; 
            ++ j ; 
        }
        if( fillCentreCell ){
            mask[(cellsInRow-1)/2][(cellsInRow-1)/2]= true ;
        }
    }
       
    /**
     * Writes the mask as a string.
     */
    
    public String toString(){
        StringBuffer sb = new StringBuffer();        
        int i , j ;        
        i = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                if( mask[i][j] ){
                    sb.append('*');
                } else {
                    sb.append('.');
                }
                ++ j ;
            }
            sb.append('\n');
            ++ i ;
        }
        return sb.toString();
    }
    
    /**
     * Indicates whether the MaskFactory has cycled through the complete
     * set of all possible masks.
     */
    
    public boolean hasMoreElements(){
        if( ! haveIterated ){
            return true ;
        }
        int i = 0 ;
        while( i < nBalls + 1 ){
            if( g[i] != g0[i] ){
                return true ;
            }
            ++ i ;
        }
        return false ;
    }
    
    /**
     * Returns the next mask. The returned mask will simply be
     * a reference to a member of the MaskFactory object, so the
     * caller might well have to clone. 
     */
    
    public Object nextElement(){
        if( hasMoreElements() ){
            int r , c ;
            r = 0 ;
            while( r < cellsInRow ){
                c = 0 ;
                while( c < cellsInRow ){
                    previousMask[r][c] = mask[r][c];
                    ++ c ;
                }
                ++ r ;
            }
            iterate( 0 );
            haveIterated = true ;
            return previousMask ;
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Calculates whether the current mask precedes the mask 
     * represented by the gaps h[] in the iterative sequence.
     * Note that true will be returned if the two masks are equal. 
     */

    boolean precedes( int[] h ){
        int i = g.length ;
        while( --i >= 0 ){
            if( h[i] > g[i] ){
                return false ;
            } else if( h[i] < g[i] ){
                return true ;
            }
        }
        return true ;
    }
    
    /**
     * Calculates the gaps that characterize the reflection 
     * of the current mask in the line that runs from 
     * grid top to bottom. The array h[] should match g in length
     * and contain zeros.
     */
    
    void reflectTopBottom( int[] h ) {
        int i = 0 , j = 0 , k = 0 , b = 0 ;
        while( b <= nBalls ){
            if( mask[i][cellsInRow-1-j] ){
                ++ b ;
            } else {
                ++ h[b];
            }
            if( ++ k == nSlots ){
                return ;
            }
            if( ++ j == cellsInRow ){
                ++ i ;
                j = 0 ;
            }
        }
    }
    
    /**
     * Calculates the gaps that characterize the reflection 
     * of the current mask in the line that runs from 
     * grid left to right. The array h[] should match g in length
     * and contain zeros.
     */
    
    void reflectLeftRight( int[] h ) {
        int i = 0 , j = 0 , k = 0 , b = 0 ;
        while( b <= nBalls ){
            if( mask[cellsInRow-1-i][j] ){
                ++ b ;
            } else {
                ++ h[b];
            }
            if( ++ k == nSlots ){
                return ;
            }
            if( ++ j == cellsInRow ){
                ++ i ;
                j = 0 ;
            }
        }
    }
    
    /**
     * Calculates the gaps that characterize the reflection 
     * of the current mask in the line that runs from 
     * grid top-left to bottom-right. The array h[] should match g in length
     * and contain zeros.
     */
    
    void reflectTopLeftBottomRight( int[] h ) {
        int i = 0 , j = 0 , k = 0 , b = 0 ;
        while( b <= nBalls ){
            if( mask[j][i] ){
                ++ b ;
            } else {
                ++ h[b];
            }
            if( ++ k == nSlots ){
                return ;
            }
            if( ++ j == cellsInRow ){
                ++ i ;
                j = 0 ;
            }
        }
    }
    
    /**
     * Calculates the gaps that characterize the reflection 
     * of the current mask in the line that runs from 
     * grid top-right to bottom-left. The array h[] should match g in length
     * and contain zeros.
     */
    
    void reflectTopRightBottomLeft( int[] h ) {
        int i = 0 , j = 0 , k = 0 , b = 0 ;
        while( b <= nBalls ){
            if( mask[cellsInRow-1-j][cellsInRow-1-i] ){
                ++ b ;
            } else {
                ++ h[b];
            }
            if( ++ k == nSlots ){
                return ;
            }
            if( ++ j == cellsInRow ){
                ++ i ;
                j = 0 ;
            }
        }
    }
    
    /**
     * Where necessary, rectify() replaces the current mask with
     * a reflection that appears in the iterative sequence. 
     * (The random generator usually creates masks that don't belong
     * on the iterative sequence and it sometimes takes quite a while
     * for the random mask to iterate through to a valid mask). 
     */
    
    void rectify(){
        int i ;
        int[] h = new int[ nBalls + 1 ];    
        generateMask();    
        reflectTopBottom( h );
        if( ! precedes( h ) ){
            i = 0 ;
            while( i < nBalls + 1 ){
                g[i] = h[i];
                ++ i ;
            }
        }
        i = 0 ;
        while( i < nBalls + 1 ){
            h[i++] = 0 ;
        }
        generateMask();
        reflectLeftRight( h );
        if( ! precedes( h ) ){
            i = 0 ;
            while( i < nBalls + 1 ){
                g[i] = h[i];
                ++ i ;
            }
        }
        i = 0 ;
        while( i < nBalls + 1 ){
            h[i++] = 0 ;
        }
        generateMask();
        reflectTopLeftBottomRight( h );
        if( ! precedes( h ) ){
            i = 0 ;
            while( i < nBalls + 1 ){
                g[i] = h[i];
                ++ i ;
            }
        }
        i = 0 ;
        while( i < nBalls + 1 ){
            h[i++] = 0 ;
        }
        generateMask();
        reflectTopRightBottomLeft( h );
        if( ! precedes( h ) ){
            i = 0 ;
            while( i < nBalls + 1 ){
                g[i] = h[i];
                ++ i ;
            }
        }
        generateMask();
    }
    
    /**
     * Calculates whether the balls are uniformly distributed on
     * a grid of the given dimensions.
     */
    
    boolean uniform( int boxesAcross ){
        if( boxesAcross == 0 ){
            return true ;
        }
        int boxesDown = cellsInRow / boxesAcross ,
            target = filledCells / cellsInRow ;
        int r , c , s , count ;
        //
        // Check each row.        
        r = 0 ;
        while( r < cellsInRow ){
            count = 0 ;
            c = 0 ;
            while( c < cellsInRow ){
                if( mask[r][c] ){
                    if( ++ count > target + 1 ){
                        return false ;
                    }
                }
                ++ c ;    
            }
            if( count < target ){
                return false ;            
            }
            ++ r ;
        }
        // Check each column.        
        c = 0 ;
        while( c < cellsInRow ){
            count = 0 ;
            r = 0 ;
            while( r < cellsInRow ){
                if( mask[r][c] ){
                    if( ++ count > target + 1 ){
                        return false ;
                    }
                }
                ++ r ;    
            }
            if( count < target ){
                return false ;            
            }
            ++ c ;
        }
        // Check each subgrid.
        int sr , sc ;
        s = 0 ;
        while( s < cellsInRow ){
            count = 0 ;
            sr = s / boxesAcross ;
            sc = s % boxesAcross ;
            r = sr * boxesAcross ;
            while( r < ( sr + 1 )* boxesAcross ){
                c = sc * boxesDown ;
                while( c < ( sc + 1 )* boxesDown ){
                    if( mask[r][c] ){
                        if( ++ count > target + 1 ){
                            return false ;
                        }
                    }
                    ++ c ;
                }
                ++ r ;
            }
            if( count < target ){
                return false ;
            }
            ++ s ;
        }
        return true ;        
    }
    
    /**
     * Starts the iterative sequence at the mask specified by an array of gaps.
     */    
    
    void initiate( int[] h ){
        int i = 0 ;
        while( i < nBalls + 1 ){
            g[i] = h[i];
            ++ i ;
        }
        rectify();
        i = 0 ;
        while( i < nBalls + 1 ){
            g0[i] = g[i];
            ++ i ;
        }
        generateMask();
        haveIterated = false ;
    }
    
    /**
     * Starts the iterative sequence at the given mask.
     */    
    
    void initiate( boolean[][] mask ){
        int i , j = 0 , k = 0 , b = 0 ;
        i = 0 ;
        while( i < nBalls + 1 ){
            g[i++] = 0 ;
        }
        i = 0 ;
        while( b <= nBalls ){
            if( mask[i][j] ){
                ++ b ;
            } else {
                ++ g[b];
            }
            if( ++ k == nSlots ){
                break ;
            }
            if( ++ j == cellsInRow ){
                ++ i ;
                j = 0 ;
            }
        }
        rectify();
        i = 0 ;
        while( i < nBalls + 1 ){
            g0[i] = g[i];
            ++ i ;
        }
        generateMask();
        haveIterated = false ;
    }

    /**
     * Starts the iterative sequence at the mask specified by
     * a string in the format produced by <code>toString()</code>.
     */    
    
    void initiate( String s ) throws Exception {       
        int i , j , p , cellsInRow = s.indexOf('\n') , filledCells = 0 ;
        char c ;
        boolean[][] mask = new boolean[cellsInRow][cellsInRow];
        i = p = 0 ;
        while( i < cellsInRow ){
            j = 0 ;
            while( j < cellsInRow ){
                c = s.charAt( p ++ );
                if( c == '*' ){
                    mask[i][j] = true ;
                    ++ filledCells ;
                } else if( c == '.' ){
                    mask[i][j] = false ;
                } else {
                    continue ;
                }
                ++ j ;
            }
            ++ i ;
        }
        resize( cellsInRow , filledCells );
        initiate( mask );
    }

    /**
     * Starts the iterative sequence at an optionally random position. 
     */
    
    void initiate( boolean randomize ){
        Random random = null ;    
        if( randomize ){
            random = new Random(); 
        }           
        int i = 0 ;
        while( i < 1 + nBalls ){
            g[i++] = 0 ;
        }
        i = 0 ;
        while( i < nSlots - nBalls ){
            if( randomize ){
                ++ g[ random.nextInt( nBalls + 1 ) ];
            } else {
                ++ g[ i %( nBalls + 1 ) ];
            }
            ++ i ;
        }
        rectify(); // Ensure it's not a reflection.
        i = 0 ;
        while( i < nBalls + 1 ){
            g0[i] = g[i];
            ++ i ;
        }
        generateMask();
        haveIterated = false ;
    }
    
    /**
     * Starts the iterative sequence at a position such that the
     * filled cells will be uniformly distributed with respect to 
     * the grid size.
     * @param boxesAcross
     */
    
    void initiate( int boxesAcross ){
        initiate( false );    
        iterate( boxesAcross );  
    }        
    
    /**
     * Returns the mask dimension.
     */
    
    public int getCellsInRow(){
        return cellsInRow ;
    }

    /**
     * Returns the number of filled cells in the mask.
     */
    
    public int getFilledCells(){
        return filledCells ;    
    }

    /**
     * Determines whether the mask is symmetric in a line that 
     * extends from the left centre of the grid to the right centre.
     */

    public static boolean isSymmetricLeftRight( boolean[][] mask ){
        int i , j ;
        i = 0 ;
        while( i < mask.length / 2 ){
            j = 0 ;
            while( j < mask.length ){
                if( mask[i][j] != mask[mask.length-1-i][j] ){
                    return false ;
                }
                ++ j ;
            }
            ++ i ;
        }
        if( mask.length % 2 == 1 ){
            j = 0 ;
            while( j < mask.length / 2 ){
                if( mask[i][j] != mask[mask.length-1-i][j] ){
                    return false ;
                }
                ++ j ;
            }
        }
        return true ;        
    }
    
    /**
     * Determines whether the mask is symmetric in a line that 
     * extends from the top centre of the grid to the bottom centre.
     */

    public static boolean isSymmetricTopBottom( boolean[][] mask ){
        int i , j ;
        j = 0 ;
        while( j < mask.length / 2 ){
            i = 0 ;
            while( i < mask.length ){
                if( mask[i][j] != mask[i][mask.length-1-j] ){
                    return false ;
                }
                ++ i ;
            }
            ++ j ;
        }
        if( mask.length % 2 == 1 ){
            i = 0 ;
            while( i < mask.length / 2 ){
                if( mask[i][j] != mask[i][mask.length-1-j] ){
                    return false ;
                }
                ++ i ;
            }
        }
        return true ;
    }
    
    /**
     * Determines whether the mask is symmetric in a line that 
     * extends from the top-left corner of the grid to the bottom-right.
     */

    public static boolean isSymmetricTopLeftBottomRight( boolean[][] mask ){
        int i , j ;
        i = 0 ;
        while( i < mask.length ){
            j = i + 1 ;
            while( j < mask.length ){
                if( mask[i][j] != mask[j][i] ){
                    return false ;
                }
                ++ j ;
            }
            ++ i ;
        }
        return true ;
    }
    
    /**
     * Determines whether the mask is symmetric in a line that extends
     * from the top-right corner of the grid to the bottom-left.
     */

    public static boolean isSymmetricTopRightBottomLeft( boolean[][] mask ){
        int i , j ;
        i = 0 ;
        while( i < mask.length ){
            j = 0 ;
            while( j < mask.length - 1 - i ){
                if( mask[i][j] != mask[mask.length-1-j][mask.length-1-i] ){
                    return false ;
                }
                ++ j ;
            }
            ++ i ;
        }
        return true ;
    }

    /**
     * Determines whether a mask has rotational symmetry of order 2.
     */

    public static boolean isSymmetricOrder2( boolean[][] mask ){
        int i , j ;
        i = 0 ;
        while( i < mask.length / 2 ){
            j = 0 ;
            while( j < mask.length ){
                if( mask[i][j] != mask[mask.length-1-i][mask.length-1-j] ){
                    return false ;
                }
                ++ j ;
            }
            ++ i ;
        }
        if( mask.length % 2 == 1 ){
            j = 0 ;
            while( j < mask.length / 2 ){
                if( mask[i][j] != mask[mask.length-1-i][mask.length-1-j] ){
                    return false ;
                }
                ++ j ;
            }
        }
        return true ;
    }

    /**
     * Determines whether a mask has rotational symmetry of order 4.
     */

    public static boolean isSymmetricOrder4( boolean[][] mask ){
        int i , j ;
        final int jMax = mask.length / 2 + ( mask.length % 2 == 1 ? 1 : 0 ); 
        i = 0 ;
        while( i < mask.length / 2 ){
            j = 0 ;
            while( j < jMax ){
                if( mask[i][j] != mask[j][mask.length-1-i] ||
                    mask[j][mask.length-1-i] != mask[mask.length-1-i][mask.length-1-j] ||
                    mask[mask.length-1-i][mask.length-1-j] != mask[mask.length-1-j][i] ){
                    return false ;
                }
                ++ j ;
            }
            ++ i ;
        }
        return true ;
    }
    
    /**
     * Class test program takes the form MaskFactory [-c cellsInRow]
     * [-r|-i|-a boxes across] [-d] filledCells. When the -a option 
     * is stipulated, the factory will ensure that the sequence starts 
     * at a mask where the filled cells are distributed evenly across
     * the grid. When -r is selected, the sequence will start at a random 
     * position while when -i is selected, the initial mask will be read from
     * standard input. The option -t requests trace output. Defaults are nine cells 
     * in a row and no debug. 
     */
    
    public static void main( String[] args ){
       int i , 
           size = 9 , 
           filledCells = 0 ,
           boxesAcross = 0 ;
           
       boolean debug = false ,
               random = false ,
               standardInput = false ;
               
       final String usage = "Usage: MaskFactory [-c cellsInRow] [-r|-a boxes across] [-v] -i|filledCells";
       
       // Parse command-line arguments. 
       if( args.length == 0 ){
           System.err.println( usage );
           System.exit( 1 );
       }           
       i = 0 ;
       while( i < args.length - 1 ){
           if( args[i].equals("-v") ){
               debug = true ;
           } else if( args[i].equals("-r") ){
               random = true ;
           } else if( args[i].equals("-c") ){
               try {
                   size = Integer.parseInt( args[++i] );
               } catch ( NumberFormatException e ) {
                   System.err.println( usage );
                   System.exit( 1 );
               }
           } else if( args[i].equals("-a") ){
               try {
                   boxesAcross = Integer.parseInt( args[++i] );
               } catch ( NumberFormatException e ) {
                   System.err.println( usage );
                   System.exit( 1 );
               }
           } else {
               System.err.println( usage );
               System.exit( 1 );
           }
           ++ i ; 
       }
       if( boxesAcross > 0 && size % boxesAcross != 0 ){
           System.err.println("Numbers of boxes across and cells per row are incompatible");
           System.exit( 2 );
       }
       if( random && boxesAcross != 0 ){
           System.err.println("The -a and -r options are mutually exclusive");
           System.exit( 2 );
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
       // Initiate the mask sequence.
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
           } else if( boxesAcross > 0 ){
               maskFactory = new MaskFactory( size , filledCells , boxesAcross );
           } else if( random ) {
               maskFactory = new MaskFactory( size , filledCells , true );
           } else {
               maskFactory = new MaskFactory( size , filledCells );               
           }
       } catch ( Exception e ) {
           System.err.println( e.getMessage() );
           System.exit( 2 );
       }
       // Iterate through.
       i = 0 ;
       while( maskFactory.hasMoreElements() ){
           ++ i ;
           if( debug ){
               System.out.println( i + "." );
               System.out.println( maskFactory.toString() );    
           }
           maskFactory.nextElement();
       }
       
       System.out.println( i + " distinct masks found");
    }
}