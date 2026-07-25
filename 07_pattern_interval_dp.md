# 07 — Pattern: Interval DP ("DP on Ranges")

## 1. Pattern Signature
The state is a **range/interval** `[i, j]` over an array or string, and the recurrence involves picking a **split point `k`** inside the range and combining the results of the two (or more) resulting sub-ranges. This is the natural generalization of what you saw with the `isPal[i][j]` table in file 06 — but now the "split point" itself is a variable you optimize over, not fixed.

## 2. Recognition Checklist
- You must decide "what happens LAST" (or first) within a range, and that choice splits the range into independent sub-ranges.
- Phrases: "minimum cost to merge/multiply a sequence of things", "burst all balloons for max coins", "predict the winner of a game where players pick from either end", "merge stones into piles".
- **The defining structural tell:** the recurrence has the shape `dp[i][j] = optimize over k in (i, j) of dp[i][k] + dp[k][j] + cost(i, k, j)`.

## 3. The Template

**Always iterate by increasing interval length** (exactly like the `isPal` table in file 06 §3) — smaller ranges must be fully computed before any larger range that depends on them.

```java
class Solution {
    public int solve(int[] arr) {
        int n = arr.length;
        int[][] dp = new int[n][n];
        // base case: dp[i][i] = 0 or arr[i], depending on problem — single element, no split possible

        for (int len = 2; len <= n; len++) {          // increasing interval length
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                dp[i][j] = /* worst-case sentinel, e.g. Integer.MAX_VALUE for min problems */ Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {           // try every split point
                    int cost = dp[i][k] + dp[k + 1][j] + /* cost of combining at this split */ 0;
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        }
        return dp[0][n - 1];
    }
}
```
**Complexity is typically O(n³)**: O(n²) ranges × O(n) split points to try per range. This is noticeably more expensive than the O(n²) patterns you've seen so far — always mention this complexity explicitly in interviews, since interval DP is often the point where naive recursion (O(2^n) or worse due to overlapping AND non-trivial branching) most dramatically improves with memoization.

## 4. Worked Example — Matrix Chain Multiplication (classic; not directly on LeetCode but foundational — appears disguised in several LeetCode Hard problems)

**Problem:** given matrices with dimensions `p[0] x p[1], p[1] x p[2], ..., p[n-1] x p[n]`, find the minimum number of scalar multiplications to compute their product (parenthesization affects total cost).

**State:** `dp[i][j]` = min cost to multiply matrices `i` through `j`.

**Recurrence:** `dp[i][j] = min over k in [i, j) of dp[i][k] + dp[k+1][j] + p[i-1]*p[k]*p[j]`.

```java
class Solution {
    public int matrixChainOrder(int[] p) {
        int n = p.length - 1; // number of matrices
        int[][] dp = new int[n + 1][n + 1]; // 1-indexed matrices

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + p[i - 1] * p[k] * p[j];
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        }
        return dp[1][n];
    }
}
```
This is the **canonical teaching example** for interval DP — nearly every other problem in this file is a variation on "pick a split point, pay a cost for that split, recurse on both sides."

## 5. Worked Example — Burst Balloons (LeetCode 312) — The "Think Backward" Trick

**Problem:** burst balloons one at a time; bursting balloon `i` gives `nums[i-1] * nums[i] * nums[i+1]` coins (neighbors at the time of bursting). Maximize total coins.

**The critical insight (this is a famous "aha"):** don't think about which balloon to burst **first** — think about which balloon to burst **LAST** within a range. If balloon `k` is the last one burst in range `(i, j)`, then its neighbors at burst time are guaranteed to be `nums[i]` and `nums[j]` (the boundary balloons, since everything strictly between `i` and `k`, and between `k` and `j`, has already been burst). This turns an seemingly-order-dependent problem into a clean interval DP.

**Setup:** pad `nums` with `1` on both ends (`nums[-1] = nums[n] = 1`) to avoid boundary special-casing.

**State:** `dp[i][j]` = max coins from bursting all balloons strictly between `i` and `j` (exclusive boundaries).

**Recurrence:** `dp[i][j] = max over k in (i, j) of dp[i][k] + dp[k][j] + nums[i]*nums[k]*nums[j]`.

```java
class Solution {
    public int maxCoins(int[] nums) {
        int n = nums.length;
        int[] balloons = new int[n + 2];
        balloons[0] = balloons[n + 1] = 1;
        for (int i = 0; i < n; i++) balloons[i + 1] = nums[i];

        int[][] dp = new int[n + 2][n + 2];
        for (int len = 2; len < n + 2; len++) {
            for (int i = 0; i + len < n + 2; i++) {
                int j = i + len;
                for (int k = i + 1; k < j; k++) {
                    int coins = balloons[i] * balloons[k] * balloons[j] + dp[i][k] + dp[k][j];
                    dp[i][j] = Math.max(dp[i][j], coins);
                }
            }
        }
        return dp[0][n + 1];
    }
}
```
**Lesson:** "think about what happens LAST, not first" is a recurring unlock across interval DP problems (also used in Remove Boxes, Predict the Winner-adjacent problems). When a naive "first move" framing makes the state space explode or the recurrence circular, try re-framing around the *last* action instead.

## 6. Worked Example — Predict the Winner (LeetCode 486) / Stone Game (LeetCode 877) — Two-Player Range Games

**Problem:** two players alternately pick from either end of an array; each maximizes their own score. Can player 1 win (or guarantee ≥ half the total)?

**State:** `dp[i][j]` = the maximum score difference (current player's score minus opponent's score) achievable from the subarray `[i, j]`, assuming **both players play optimally**.

**Recurrence:** `dp[i][j] = max( nums[i] - dp[i+1][j], nums[j] - dp[i][j-1] )` — "if I take the left end, I gain `nums[i]` but then my opponent becomes the 'current player' on `[i+1,j]`, so I subtract their optimal difference."

```java
class Solution {
    public boolean predictTheWinner(int[] nums) {
        int n = nums.length;
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) dp[i][i] = nums[i]; // base case: one element, take it

        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                dp[i][j] = Math.max(nums[i] - dp[i + 1][j], nums[j] - dp[i][j - 1]);
            }
        }
        return dp[0][n - 1] >= 0;
    }
}
```
**Lesson (the "score difference" trick):** modeling adversarial turn-based games as "difference in score from the current player's perspective" (rather than tracking each player's score separately) is a hugely reusable trick — it collapses what looks like a 2-player minimax problem into a single-perspective DP with a clean recurrence.

## 7. Problem Set

### Medium
| # | Problem | Key twist |
|---|---|---|
| 486 | Predict the Winner | Score-difference trick |
| 877 | Stone Game | Same as above; also solvable by a parity/greedy proof (first player always wins with even-length piles) — good to mention both |
| 1130 | Minimum Cost Tree From Leaf Values | Interval DP where the "cost" is `max(leaves in range) * max(leaves in other range)` — also has a monotonic-stack O(n) solution worth mentioning as a follow-up |
| 5 / 516 / 132 | (revisit file 06) | Interval-flavored palindrome problems belong conceptually to this family too |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 312 | Burst Balloons | "Last balloon burst" reframing |
| 1547 | Minimum Cost to Cut a Stick | Same interval-DP shape as Matrix Chain Multiplication, disguised as a "cutting" problem |
| 1000 | Minimum Cost to Merge Stones | Adds a modular constraint (`k` piles merged at once) — must check `(j - i) % (k - 1) == 0` for a merge to even be possible at a given range, a genuinely tricky feasibility condition layered onto the standard interval DP |
| 546 | Remove Boxes | 3D state `dp[i][j][k]` where `k` = number of boxes of the same color as `boxes[j]` attached to its right — one of the hardest DP problems commonly asked, requires the "extra dimension for extendable context" trick |
| 664 | Strange Printer | `dp[i][j]` = min turns to print `s[i..j]`; merges intervals when boundary characters match, similar spirit to Remove Boxes |

## 8. Companies Known to Ask This Pattern
Google, Meta — Burst Balloons and Predict the Winner/Stone Game are classic **onsite "hard" bar-raiser** questions specifically because the "obvious" first-move framing fails and candidates must discover the reframing trick live. Minimum Cost to Merge Stones and Remove Boxes appear at Google as some of the hardest DP questions in their rotation.

## 9. Edge Cases & Traps

1. **Off-by-one in interval bounds** — is `j` inclusive or exclusive? Is the split point `k` in `[i, j)` or `[i, j]`? **Pick a convention explicitly before coding** (this guide uses `k` as the last index of the left sub-range, so the right sub-range starts at `k+1`) and stay consistent, or you will misalign base cases.
2. **Padding tricks (Burst Balloons) changing all your indices by 1** — if you pad the array with sentinel values, every subsequent index reference shifts; a common bug is mixing padded and unpadded indices in the same recurrence.
3. **O(n³) complexity surprising people who expect O(n²) from "it's just a 2D table"** — always state the complexity explicitly; interval DP's extra "try every split point `k`" inner loop is what pushes it to cubic, unlike the O(n²) patterns in files 02, 05, 06.
4. **Merge Stones' modular feasibility condition** — a range can only be merged into fewer piles if `(length - 1) % (k - 1) == 0`; skipping this check causes you to compute nonsensical merges for infeasible ranges.
5. **Score-difference sign confusion (Predict the Winner)** — it's easy to accidentally compute "my score" and "opponent's score" as two separate positive quantities instead of a signed difference, which breaks the clean recurrence; always frame it as "current player's advantage," a single signed number.
6. **Remove Boxes' extra dimension** — students often try to solve it with just `dp[i][j]` and get stuck because the optimal choice genuinely depends on "how many same-colored boxes are trailing behind this range from a previous merge" — an example where the state space must be *expanded* beyond the obvious `[i,j]` to capture necessary context.

## 10. Counter-Questions

1. *"Why must interval DP iterate by increasing length rather than increasing `i`?"* → Because `dp[i][j]` depends on `dp[i][k]` and `dp[k+1][j]` for various `k` — both are strictly *shorter* ranges than `[i,j]`, so all shorter ranges must be fully resolved first; iterating by length guarantees this regardless of where `i` and `j` fall.
2. *"In Burst Balloons, why does thinking about the LAST balloon burst (instead of the first) make the recurrence work?"* → Because the last balloon burst in a range is the only one whose neighbor-at-burst-time is *guaranteed* to be the range's boundary elements (everything else inside has already been removed) — this removes the order-dependency that made a "first balloon" framing intractable.
3. *"Can you always convert a 'two-player optimal game' problem into a single score-difference DP? What has to be true?"* → Yes, whenever both players have the *same* scoring objective (zero-sum, symmetric optimization) and perfect information — the "difference" framing exploits the symmetry so you don't need to track two separate player states.
4. *"How would you reduce Matrix Chain Multiplication's O(n³) to something faster?"* → Knuth's optimization can reduce certain interval DPs (including MCM) to O(n²) when the "optimal split point is monotonic" condition holds — worth naming as an advanced technique (see file 12) even if you don't derive it live.

Proceed to `08_pattern_tree_dp.md`.
