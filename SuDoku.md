## The SuDoku command-line app

### Introduction

The SuDoku command line app presents the graphical interface familiar from the applet.

---

### Syntax
```
SuDoku [-a boxesAcross] [-d boxesDown] [-i]
```

The `[-a boxesAcross]` and `[-d boxesDown]` options allow the app to be started with a non-standard grid size. (Of course, it is possible to change the grid size from within the app once it has started.) The default values are three.

The `-i` option stipulates that an initial grid should be read from standard input. (In this case, the `-a` and `-d` options will be ignored.) The initial grid should be entered in the format used by the app's **Copy** button.

---

### Examples

`SuDoku` starts an app that displays an empty 3x3 grid.

`SuDoku -a 4 -d 4` starts an app that displays an empty 4x4 grid.

`SuDoku < puzzle.txt` starts an app that displays the puzzle stored in the file `puzzle.txt` .

&nbsp;

