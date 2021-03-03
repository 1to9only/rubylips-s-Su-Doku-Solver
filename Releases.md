## Release History

---

 * [Release 1.0](#R1_0)
 * [Release 1.1](#R1_1)
 * [Release 1.2](#R1_2)
 * [Release 1.3](#R1_3)
 * [Release 1.4](#R1_4)
 * [Release 1.5](#R1_5)
 * [Release 1.6](#R1_6)
 * [Release 1.7](#R1_7)
 * [Release 1.8](#R1_8)
 * [Release 1.9](#R1_9)
 * [Release 1.10](#R1_10)
 * [Release 1.11](#R1_11)
 * [Release 1.12](#R1_12)
 * [Release 1.13](#R1_13)
 * [Release 1.14](#R1_14)
 * [Release 1.15](#R1_15)
 * [Release 1.16](#R1_16)
 * [Release 1.17](#R1_17)
 * [Release 1.18](#R1_18)

---

### [Release 1.0]() - November 23, 2004

Solved the classical 3x3 Su Doku puzzle.

---

### [Release 1.1]() - November 25, 2004

Added support for variable grid sizes.

---

### [Release 1.2]() - November 30, 2004

Introduced selectable strategies (including the Least Candidates strategies), hints, puzzle evaluations and puzzle composition.

---

### [Release 1.3]() - February 4, 2005

Introduced the Least Candidates Hybrid strategies, clipboard support, random shuffles, vastly improved puzzle composition and three new command-line apps (Solver, MaskFactory and Composer in addition to the existing SuDoku).

---

### [Release 1.4]() - February 9, 2005

Introduced the Least Candidates Hybrid II strategy types and much improved performance.

---

### [Release 1.5]() - February 9, 2005

Fixed in a bug in the R1.4 composer.

---

### [Release 1.6]() - February 22, 2005

Displayed the reasoning used by the Least Candidates strategy types and introduced a native library, which merely illustrated the excellent performance provided by the Java Virtual Machine.

---

### [Release 1.7]() - February 23, 2005

Rewrote all calls to `Random.nextInt(int)` , which isn't supported by the Microsoft VM. Further performance enhancements to the deterministic Least Candidates methods.

---

### [Release 1.8]() - February 23, 2005

The threads created by the composer no longer reside in their own `ThreadGroup` in order to work around security restrictions imposed upon applets.

---

### [Release 1.9]() - March 3, 2005

The Least Candidates Hybrid strategy type has been improved so that, in the absence of an indisputable candidate, it will search for linked values and restricted regions in order to eliminate candidate moves. As a result, less unwinds are performed. In particular, the 'fiendish' puzzles from The Times are solved entirely without unwinds.

---

### [Release 1.10]() - March 11, 2005

The applet version of the solver now implements a separate clipboard window in order to work around security restrictions. The changes to the Least Candidates Hybrid types in Release 1.9 have been restricted to Least Candidates Hybrid II in order to improve the performance of the composer for 3x3 grids.

---

### [Release 1.11]() - April 2, 2005

Release 1.11 features two significant improvements to the reduction rules - the generalization of the 'values v1, v2 and v3 must occupy cells c1, c2 and c3 in some order' rule to cover partial subsets and the introduction of the Nishio rule. A major bug in the reporting code for guesses has been fixed. The default MaskFactory algorithm has been altered in an attempt to ensure a more uniform coverage of the grid but the new algorithm is still not entirely satisfactory.

---

### [Release 1.12]() - April 2, 2005

Features a trivial amendment to 1.11 in order to avoid the API functions not supported by the Microsoft VM (and, therefore, Internet Explorer).

---

### [Release 1.13]() - April 2, 2005

The X-Wings technique has been implemented.

---

### [Release 1.14]() - April 5, 2005

Several new features:

* Single Sector Candidates search is now applied before Disjoint Subsets for performance reasons.
* Bug-fix in X-Wings.
* Mouse clicks in the Reasoning Area now allow partial solutions to be browsed.
* The ASCII art has improved.
* The Solver app now provides optional profiling statistics.
* The Composer now categorizes the puzzles it creates.

---

### [Release 1.15]() - April 8, 2005

* Full support for the Swordfish pattern.
* Performance bug fix for the Single Sector Candidates search.

---

### [Release 1.16]() - April 20, 2005

* A new GUI makes it possible to choose precisely which patterns the solver will attempt to find.
* The numbers of eliminations and guesses made by the solver are displayed.
* Partial solutions will be displayed where necessary if the 'Guesses' option isn't enabled.
* The command-line Composer app now offers the option to create puzzles only of a certain pattern.

---

### [Release 1.17]() - April 25, 2005

* Bug fixes for the Single Sector Candidates and Disjoint Subsets methods.
* Bug fix for Composer threads.
* Composer now optionally provides XML output.

---

### [Release 1.18]() - May 4, 2005

* The labelling system used by the puzzles in The Independent is now supported.
* The Copy button optionally provides output in Pappocom Library Book XML format.

&nbsp;

