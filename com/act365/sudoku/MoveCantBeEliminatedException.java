/*
 * Su Doku Solver
 * 
 * Copyright (C) act365.com July 2005
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

/**
 * A MoveCantBeEliminatedException exception is thrown by a state grid
 * if the given move cannot be eliminated.
 */

public class MoveCantBeEliminatedException extends MoveException {
    private static final long serialVersionUID = 4410598816969242677L;
    /**
     * Creates a new exception for the move (x,y):=v.
     */

    public MoveCantBeEliminatedException( int x , int y , int value ){
        super("The move (" + ( 1 + x ) + "," + ( 1 + y ) + "):=" + SuDokuUtils.valueToString( value ) + " cannot be eliminated" , x , y , value );        
    }
}
