## The Solver command-line app

### Introduction

`Solver` solves Su Doku puzzles from the command-line, which gives it some possible advantages over the [SuDoku](SuDoku.md) app:

* It will display all solutions to a given puzzle.
* It is easier to incorporate within scripts.
* It is easier to profile.

---

### Syntax
```
Solver [-m max solutions] [-s strategy] [-v] [-p]
```

The `[-m max solutions]` option stipulates that the app should exit once a given number of puzzle solutions has been found. The default behaviour is for the app to continue until all possible solutions have been found.

The `[-s strategy]` option stipulates the strategy for the solver to use. The default strategy is Least Candidates Hybrid.

The `[-v]` option enables verbose output.

The `[-p]` option provides basic profiling statistics.

The puzzle to solve will be read from standard input. The puzzle should be entered in the format used by the SuDoku app's **Copy** button

---

### Examples

`Solver -v < puzzle.txt` finds all solutions to the puzzle stored in the file `puzzle.txt` and writes them to standard output.

`Solver -m 1 -s "First Available" -v < puzzle.txt` finds a single solution to the problem using the First Available strategy.

&nbsp;

