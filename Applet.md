## &#25968;&#29420; - A Su Doku solver

The Times newspaper displays a [Su Doku](http://www.sudoku.com) problem in its T2 supplement each day. (Alternatively, visit [the website](http://www.timesonline.co.uk) and click on the **GAMES** button underneath the top advertising banner). Although the problem will be far from straightforward for most readers to solve, an average home computer will be able find the solution in less than a second.

Readers of The Times who are motivated less by intellectual challenges and more by daily offers of champagne should visit the [Sudoku solutions site](http://www.sudoku.com/solutions.asp). Select 'The Times (of London)' from the choice box in the bottom-right hand corner, enter the first five grid values from the paper and press 'Find Solution'. The site will very kindly pop up the answer to the current puzzle, ready to be e-mailed to the The Times. Thanks to Nigel 'Loadsabubbly' Greenwood for that tip!

The goal of a Su Doku problem is to complete the published grid so that no number appears more than once in any row, column or 3x3 subgrid. The rules are described more fully on the official Su Doku website, to which a link is provided above. To set up the solver, simply type the numbers printed in The Times into the corresponding squares in the grid below. (The Tab and Shift-Tab keys allow the grid to be navigated without use of the mouse). Squares that are blank in The Times should be left blank in the grid. Once all the values have been copied across from the newspaper, use the buttons beneath the grid.

Instructions appear underneath.

---

### Solve

There are three solve buttons:
* **Solve** completes the grid (provided that a solution exists).
* **Unsolve** resets any squares on the grid filled by a previous press of the **Solve** button.
* **Reset** resets the entire grid.

### Evaluate

**Evaluate** checks to see whether the current incomplete grid has a unique solution.

### Labelling Format

The drop-down list to the right of the **Evaluate** button allows the user to switch between the '*Numeric from 1*' format used by the majority of sources and the '*Alphanumeric from 0*' format used by the 4x4 puzzles in The Independent.

### The Clipboard

**Copy** transfers the current grid into a separate clipboard window (the system clipboard cannot be used for security reasons) in the format selected in the drop-down box on the far right of the row. The two supported formats are *Plain Text* and *Library Book* (the XML format used by the Pappocom Sudoku software). Once **Copy** has been pressed, simply select the displayed grid in the clipboard window, press Ctrl-Insert to copy it, then press Shift-Insert from within some other document, such as a new e-mail message. The grid will look its best rendered in a monospaced font such as Courier.

**Paste** performs the opposite operation, i.e. it transfers a grid from the clipboard window (which must have been previously opened by a press of the **Copy** button) to the solver. (Only the Plain Text format is supported by the Paste operation). Suppose you have received an e-mail that contains a Su Doku puzzle in the approved clipboard format. Select the grid from within the document, then press Ctrl-Insert in order to copy the grid to the system clipboard. Press Shift-Insert within the clipboard window to copy the text there, then press the **Paste** button to copy the grid from the clipboard window into the solver.

### Resize

Although classical Su Doku puzzles use a 3x3 grid of boxes, the Su Doku Solver supports the alternative box grids that are sometimes seen. Use the **Resize** button to change the grid size. Be aware that the grid might not display properly if a very large grid has been chosen.

### Compose

**Compose** composes a fresh puzzle. The new puzzle is certain to be symmetrical, have a unique solution and feature the requested number of filled cells. Note that complicated puzzles (in the case of the classic 3x3 Su Doku puzzle, 'complicated' usually means with less than 24 filled cells) might take a considerable time to generate. Press **Break** in order to interrupt the procedure.

### Patterns

The Patterns section dictates which patterns should be detected during the Solve phase. The meanings of the various patterns are discussed on the [Sudoku Programmers](http://www.setbb.com/phpbb/?mforum=sudoku) forum. The numbers to the right refer (except in the case of Guess) to the total number of candidate move eliminations performed due to the discovery of each type of pattern. In the case of Guess, the figure is the total number of guesses performed.

### Reasoning

The text box at the bottom displays the reasoning that has been used to solve the puzzle. Click part-way through the reasoning in order to view partial solutions.

&nbsp;

