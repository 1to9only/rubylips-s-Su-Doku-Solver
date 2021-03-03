## The Composer command-line app

### Introduction

`Composer` generates Su Doku puzzles.

---

### Syntax
```
Composer [-a boxes across] [-d boxes down] [-ms max solns|-mm max masks]
         [-mu max unwinds] [-mc max complexity] [-s solvers] [-c threshold]
         [-r] [-v] [-n] ([+|-][ssc|ds|xwings|swordfish|nishio])* -i|#cells
```

The options `[-a boxes across]` and `[-d boxes down` define the size of the composed puzzles. The default puzzle size is the classic 9x9 grid, which has three boxes across and three boxes down.

The option `[-ms max solns|-mm max masks]` allows the composer to exit after a given number of puzzles has been found or a given number of masks has been considered. The default behaviour is to find as many puzzles as possible and to consider all possible masks.

The option `[-mc max complexity]` stipulates that a mask should no longer be considered once a given complexity level has been reached. The default behaviour is for there to be no limit on the complexity level.

The option `[-mc max unwinds]` stipulates that a mask should no longer be considered once a given number of unwinds has been performed. The default behaviour is for there to be no limit on the number of unwinds.

The option `[-s solvers]` stipulates the maximum number of solver threads to run at one time. Each solver thread will consider its own unique mask. The default value is three. Less than the stipulated number of threads will execute if a smaller number of masks has been specified by the `[-mm max masks]` options.

The option `[-c threshold]` stipulates that the compose solver should only be invoked once the search depth has reached a certain value. The compose solver is the solver used by the composer to check whether a partially-constructed puzzle has a unique solution. For instance, a puzzle on the classic Su Doku grid often has a unique solution once the initial values of 24 cells have been specified. A user who wished to compose a 32-cell puzzle might think that there would be no benefit in checking for non-uniqueness of solution before 24 of the 32 cell had been filled, in which case he could specify the option `[-c 24]`. The default behaviour is for the compose solver to be invoked at each step, regardless of search depth.

The option `[-r]` stipulates that the mask sequence should be started from a random position.

The option `[-v]` enables verbose output.

The option `[-n]` stipulates that a native library should be used. Such a library is likely to lead to very little performance improvement, if any at all. The use of native libraries is not recommended.

An option such as `[+swordfish]` stipulates that the Composer should only generate puzzles that feature the Swordfish pattern. An option such as `[-nishio]` stipulates that generated puzzles should avoid the Nishio pattern. It's possible to stipulate several such conditions.

The option `-i` stipulates that the initial mask will be read from standard input. The mask should be entered in the format used by [MaskFactory](Mask Factory.md). When this option is used, the `[-a boxes across]` and `[-d boxes down]` options will be ignored.

The option `#cells` stipulates the number of filled cells to appear in the puzzle.

---

### Examples

`Composer -mu 50 -ms 1 32` is equivalent to the process invoked by the **Compose** button on the [SuDoku](SuDoku.md) app (assuming, of course, that 32 filled cells have been requested).

`Composer -v -mu 1000 -c 20 20` attempts to find 20-cell puzzles. The compose solver is only invoked once the search depth has reached 20, which should ensure that the composer runs quickly. Furthermore, each mask will be rejected once 1000 unwinds ( a relatively small number) have been performed, which should ensure that many different masks are considered. This run could be used to identify feasible masks to be studied in more detail later.

`Composer -v -mu 50 +swordfish -nishio -c 18 23` generates 23-cell puzzles that admit a logical solution, feature the Swordfish pattern but avoid Nishio.

`Composer -v -mm 1 -c 20 -i < mask.txt` concentrates on puzzles using just a single mask (perhaps identified by the previous run). It will terminate once all possible puzzles have been generated (which won't happen in practice - the user will have to abort the process.)

Larger Su Doku puzzles (i.e. 4x4 or bigger) might require the Java VM to execute with a larger stack than the 64MB provided by default. Try the `-Xmx` option, which works for JDK 1.4.2, but remember that the `-X` options aren't guaranteed to remain in future Java releases.

`java -Xmx128m Composer` will run the composer with a maximum heap size of 128MB.

&nbsp;

