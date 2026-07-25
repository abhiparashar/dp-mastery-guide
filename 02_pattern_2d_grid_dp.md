# 02 — Pattern: 2D Grid DP

## 1. Pattern Signature
You're given an actual **m × n grid** (matrix), and you move through it with restricted directions (usually right/down, sometimes all 4 directions with no revisits, sometimes any of up/down/left/right for a "falling path"). The state is naturally `(row, col)`.

## 2. Recognition Checklist
- Input is explicitly a 2D grid/matrix.
- Movement is restricted (typically only right and down, or only down/diag for "falling path" problems) — if movement is unrestricted in all 4 directions this becomes a **graph/BFS/Dijkstra** problem instead, not DP (unless it's a DAG-like structure, e.g., strictly decreasing values).
- Asked for: min/max path sum, number of distinct paths, largest square/rectangle of some property, min cost to traverse.

## 3. The Template

**State:** `dp[i][j]` = the answer considering the sub-grid ending at cell `(i, j)`.

**Recurrence (right/down movement only):** `dp[i][j] = f(dp[i-1][j], dp[i][j-1], grid[i][j])`

```java
class Solution {
    public int solve(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dp = new int[m][n];

        dp[0][0] = grid[0][0];                       // base case: top-left corner
        for (int j = 1; j < n; j++)                   // first row: only reachable from the left
            dp[0][j] = dp[0][j - 1] + grid[0][j];
        for (int i = 1; i < m; i++)                   // first column: only reachable from above
            dp[i][0] = dp[i - 1][0] + grid[i][0];

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + grid[i][j]; // min/max/sum per problem
            }
        }
        return dp[m - 1][n - 1];
    }

    // SPACE OPTIMIZATION: dp[i][j] only depends on the row above and the
    // current row so far → collapse to a single 1D array of size n.
    public int solveOptimized(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dp = new int[n];
        dp[0] = grid[0][0];
        for (int j = 1; j < n; j++) dp[j] = dp[j - 1] + grid[0][j];

        for (int i = 1; i < m; i++) {
            dp[0] += grid[i][0]; // update first column in place
            for (int j = 1; j < n; j++) {
                dp[j] = Math.min(dp[j], dp[j - 1]) + grid[i][j]; // dp[j] here is still "old" (row above)
            }
        }
        return dp[n - 1];
    }
}
```

## 4. Worked Example — Unique Paths (LeetCode 62) & Unique Paths II (LeetCode 63)

**Unique Paths:** count paths from top-left to bottom-right moving only right/down.

`dp[i][j] = dp[i-1][j] + dp[i][j-1]` (sum, not min, because we're **counting ways**, not optimizing a cost).

```java
class Solution {
    public int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1); // first row: exactly 1 way to reach any cell (all rights)
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1]; // dp[j] (old, from row above) + dp[j-1] (new, current row, left cell)
            }
        }
        return dp[n - 1];
    }
}
```

**Unique Paths II** adds obstacles: `if (grid[i][j] == 1) dp[i][j] = 0;` — obstacle cells contribute zero paths, and this must be applied **before** using `dp[i][j]` as a source for `dp[i][j+1]` or `dp[i+1][j]`. Also handle: if `grid[0][0]` is itself an obstacle, answer is immediately 0.

## 5. Worked Example — Dungeon Game (LeetCode 174) — The "Reverse Direction" Trap

**Why this is tricky:** naive forward DP (`dp[i][j]` = min HP needed to reach here from start) **doesn't work**, because "minimum HP entering a cell" isn't well-defined without knowing the HP you'll need for the *rest* of the path — the constraint is "HP must never drop to ≤ 0 at any point," which is a *suffix* constraint, not a prefix one.

**Fix:** define `dp[i][j]` = minimum HP needed **upon entering** cell `(i,j)` to survive the rest of the path to the bottom-right, and fill the table **backwards** (from bottom-right to top-left).

```java
class Solution {
    public int calculateMinimumHP(int[][] dungeon) {
        int m = dungeon.length, n = dungeon[0].length;
        int[][] dp = new int[m + 1][n + 1];
        for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE);
        dp[m][n - 1] = 1; // sentinel base cases so real cells compute cleanly
        dp[m - 1][n] = 1;

        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                int need = Math.min(dp[i + 1][j], dp[i][j + 1]) - dungeon[i][j];
                dp[i][j] = Math.max(1, need); // HP can never be < 1
            }
        }
        return dp[0][0];
    }
}
```
**Lesson:** when a constraint is about the *whole future path* rather than the *path so far*, try reversing the DP direction (compute from the end backward). This "reverse the recurrence direction" trick reappears in several hard grid problems.

## 6. Worked Example — Maximal Square (LeetCode 221)

**State:** `dp[i][j]` = side length of the **largest square** whose bottom-right corner is at `(i, j)`, assuming `matrix[i][j] == '1'`.

**Recurrence:** `dp[i][j] = min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]) + 1` if `matrix[i][j] == '1'`, else `0`.

**Why min of three neighbors?** A square of side `k` at `(i,j)` requires squares of side ≥ `k-1` immediately above, to the left, AND diagonally above-left — the smallest of the three is the bottleneck.

```java
class Solution {
    public int maximalSquare(char[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[][] dp = new int[m + 1][n + 1]; // +1 padding avoids boundary checks
        int maxSide = 0;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (matrix[i - 1][j - 1] == '1') {
                    dp[i][j] = Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1])) + 1;
                    maxSide = Math.max(maxSide, dp[i][j]);
                }
            }
        }
        return maxSide * maxSide; // problem asks for AREA, not side — common miss
    }
}
```

## 7. Problem Set

### Easy
| # | Problem | Key twist |
|---|---|---|
| 62 | Unique Paths | Pure combinatorics — can also be solved with `C(m+n-2, m-1)`, good to mention as an alternative closed-form |
| 63 | Unique Paths II | Obstacles zero out cells |
| 64 | Minimum Path Sum | min instead of count |
| 120 | Triangle | Grid is jagged (triangular) — index bounds shift per row |

### Medium
| # | Problem | Key twist |
|---|---|---|
| 221 | Maximal Square | min-of-3-neighbors +1 recurrence |
| 1277 | Count Square Submatrices with All Ones | Same recurrence as 221, but sum `dp[i][j]` itself (every valid square counted) instead of just tracking max |
| 63 | (revisit) Unique Paths II | — |
| 931 | Minimum Falling Path Sum | Movement includes diagonals, not just down |
| 1594 | Max Non Negative Product in a Matrix | Track both min and max product per cell (sign-flip trap, like Maximum Product Subarray) |
| 1301 | Number of Paths with Max Score | Combine two DP tables (score AND count) simultaneously |
| 1463 | Cherry Pickup II | Two agents traverse simultaneously → state becomes `(row, col1, col2)`, a 3D DP |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 174 | Dungeon Game | Reverse-direction DP (see §5) |
| 741 | Cherry Pickup | Equivalent to two agents going forward simultaneously (not one agent round trip) — clever reformulation required |
| 1289 | Minimum Falling Path Sum II | Must avoid picking from the same column in consecutive rows — track top-2 minimums per row to stay O(n²) instead of O(n³) |

## 8. Companies Known to Ask This Pattern
Google, Amazon, Microsoft, Bloomberg — Unique Paths / Minimum Path Sum are extremely common warm-up questions; Cherry Pickup / Dungeon Game appear at Google and Meta onsite loops as "hard" bar-raiser questions.

## 9. Edge Cases & Traps

1. **First row / first column base cases** — these can only be reached from one direction, not two; forgetting to special-case them causes an out-of-bounds read at `dp[-1][j]` or `dp[i][-1]`. The `+1` padded-array trick (as in Maximal Square) elegantly avoids this by making index `0` mean "empty/boundary."
2. **Grid with a blocked start or end cell** (Unique Paths II) — must return 0 immediately, don't let the loop silently produce a wrong nonzero answer.
3. **Falling Path Sum problems**: neighbors are diagonal (`i-1, j-1`), (`i-1, j`), (`i-1, j+1`) — must bound-check `j-1 >= 0` and `j+1 < n` per cell, not just at the array edges once.
4. **Confusing "largest square" (must be a perfect square, uses min-of-3) with "largest rectangle"** (a different, harder problem solved with a monotonic stack — see file 12) — don't conflate these two visually-similar-sounding problems.
5. **Returning side length instead of area** (Maximal Square asks for area).
6. **When "movement" secretly allows all 4 directions** (rare, but sometimes phrased ambiguously) — check whether the grid values are strictly increasing/decreasing along any valid path (making it a DAG, so DP still works with memoization + cycle-safety), versus truly unconstrained movement (which needs Dijkstra/BFS instead — not a DP problem at all).
7. **3D-state grid problems (Cherry Pickup II, two-agent problems)** — the extra dimension often represents a *second* moving agent's column, not a second grid; students frequently misinterpret it as needing two separate DP tables when really it's ONE table with an extra dimension synchronized by row.

## 10. Counter-Questions

1. *"Can you compute Unique Paths without any DP table at all?"* → Yes, it's `C(m+n-2, m-1)` combinatorially, since any path is a sequence of `(m-1)` downs and `(n-1)` rights in some order. Good test of whether you see DP as one tool among several, not a hammer for everything.
2. *"Why does Dungeon Game need backward DP but Minimum Path Sum doesn't?"* → Minimum Path Sum's constraint (total cost) is a simple prefix sum — forward DP works fine. Dungeon Game's constraint (HP must stay positive throughout) depends on the entire *remaining* path, which isn't known until you've seen the suffix — hence backward DP.
3. *"How would you reconstruct the actual path, not just the min cost?"* → Keep a parallel `parent[i][j]` table storing which neighbor was chosen, then backtrack from `(m-1, n-1)` to `(0,0)`.
4. *"What changes if diagonal moves are also allowed in Minimum Path Sum?"* → Recurrence adds a third term `dp[i-1][j-1]`; conceptually identical, just widens the "min of neighbors" set — tests whether you understand the recurrence is really "min/max over all valid predecessor cells," not a hardcoded two-term formula.

Proceed to `03_pattern_01_knapsack.md`.
