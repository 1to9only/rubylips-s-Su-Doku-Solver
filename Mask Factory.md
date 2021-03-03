## The MaskFactory command-line app

### Introduction

`MaskFactory` cycles through all possible masks for a grid of a given size.Masks will be ignored if they are simply rotated or reflected forms of earlier masks in the sequence.

---

### Syntax
```
MaskFactory [-c cellsInRow] [-r|-a boxes across] [-v] -i|filledCells
```

The option `[-c cellsInRow]` stipulates the number of cells to appearing a single row or column of the grid. The default value is 9.

`MaskFactory` will cycle through all possible masks - however, the user has some control over where in the cycle the app will start. By default,the app will start at the beginning. When the `-r` option is specified,the app will start the cycle at a random position. When the `-a boxes across` ,the cycle will start at a position such that the filled cells are uniformly distributed across the grid of the given size.

The option `[-v]` enables verbose output.

The option `[-i]` stipulates that the initial mask will be read from standard input. The mask should be entered in the format that `MaskFactory` displays if the `-v` option has been specified.

`filledCells` stipulates the number of filled cells to appear on the masks.

---

### Examples

`MaskFactory -c 4 -v 4` displays all masks for a 2x2 Su Doku grid with4 initially-filled cells.

`MaskFactory 10` counts the number of distinct masks for the classic Su Doku grid with 10 initially-filled cells.

&nbsp;

