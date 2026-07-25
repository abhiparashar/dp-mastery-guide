# 01 — Pattern: Linear 1D DP ("Fibonacci-style")

## 1. Pattern Signature
You're scanning a **single sequence left to right**, and the decision at position `i` depends on a **constant, small number of previous positions** (usually `i-1` and/or `i-2`). No 2D grid, no second string, no subsets — just one array/string and one moving index.

## 2. Recognition Checklist
- Problem gives **one array or string**.
- You're asked for a min/max/count/boolean over "ways to reach the end" or "best result up to position i."
- The recurrence for position `i` only needs a **fixed lookback window** (1, 2, or k previous states) — not "all previous states" (that would push you toward a different pattern, see file 05).
- Phrases like: "climbing stairs", "rob houses", "ways to decode", "maximum subarray", "jump game", "delete and earn".

## 3. The Template

**State:** `dp[i]` = the answer (min/max/count) considering the prefix ending at (or up to) index `i`.

**Recurrence (general shape):** `dp[i] = f(dp[i-1], dp[i-2], ..., a[i])`

```java
class Solution {
    public int solve(int[] a) {
        int n = a.length;
        if (n == 0) return 0;              // ALWAYS handle empty input explicitly
        if (n == 1) return a[0];           // often needed since dp[i-2] would be invalid

        int[] dp = new int[n];
        dp[0] = a[0];
        dp[1] = /* combine a[0], a[1] per problem rules */ 0;

        for (int i = 2; i < n; i++) {
            dp[i] = /* combine dp[i-1], dp[i-2], a[i] */ 0;
        }
        return dp[n - 1];
    }

    // SPACE-OPTIMIZED version (Stage 4): most linear-1D problems only need the
    // last 1-2 states, not the whole array. This is a very common interview
    // follow-up: "Can you do it in O(1) space?"
    public int solveOptimized(int[] a) {
        int n = a.length;
        if (n == 0) return 0;
        int prev2 = 0, prev1 = a[0]; // adjust base cases per problem
        for (int i = 1; i < n; i++) {
            int cur = /* combine prev1, prev2, a[i] */ 0;
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}
```

## 4. Worked Example — House Robber (LeetCode 198)

**Problem:** Rob houses in a row for maximum money; can't rob two adjacent houses.

**Step 1 (brute force):** at house `i`, either skip it (`solve(i+1)`) or rob it and skip the next (`a[i] + solve(i+2)`).

**Step 2 (state):** `i` — the index we're deciding on. That's the only variable needed.

**Step 3 (recurrence):** `dp[i] = max(dp[i-1], a[i] + dp[i-2])` — "dp[i] = best money robbing from houses[0..i]".

**Step 4 (base cases):** `dp[0] = a[0]`, `dp[1] = max(a[0], a[1])`.

**Step 5 (order):** left to right, `i` increasing — trivial here.

```java
class Solution {
    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 0) return 0;
        if (n == 1) return nums[0];

        int prev2 = nums[0];
        int prev1 = Math.max(nums[0], nums[1]);
        for (int i = 2; i < n; i++) {
            int cur = Math.max(prev1, nums[i] + prev2);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}
```
**Complexity:** O(n) time, O(1) space (after optimization).

**Variant — House Robber II (LeetCode 213):** houses are in a **circle** (first and last are adjacent). Trick: run House Robber I twice — once excluding the last house, once excluding the first house — and take the max. This "break the circle into two lines" trick reappears constantly in circular-array DP problems.

**Variant — House Robber III (LeetCode 337):** houses form a **binary tree** — see file `08_pattern_tree_dp.md`.

## 5. Second Worked Example — Maximum Subarray / Kadane's Algorithm (LeetCode 53)

**State:** `dp[i]` = max sum of a *contiguous* subarray **ending exactly at** index `i` (not "best up to i" — that distinction matters, see trap below).

**Recurrence:** `dp[i] = max(a[i], dp[i-1] + a[i])` — either start fresh at `i`, or extend the previous subarray.

**Answer:** `max(dp[0..n-1])`, NOT `dp[n-1]` — because the best subarray might end anywhere, not necessarily at the last index.

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int currentMax = nums[0];
        int globalMax = nums[0];
        for (int i = 1; i < nums.length; i++) {
            currentMax = Math.max(nums[i], currentMax + nums[i]);
            globalMax = Math.max(globalMax, currentMax);
        }
        return globalMax;
    }
}
```
**Trap:** initializing `currentMax`/`globalMax` to `0` instead of `nums[0]` breaks the all-negative-numbers case (e.g., `[-3, -1, -2]` should return `-1`, not `0`). **Always seed with the first element**, not zero, whenever negative numbers are allowed.

## 6. Problem Set (ordered by difficulty)

### Easy
| # | Problem | Key twist |
|---|---|---|
| 70 | Climbing Stairs | Pure Fibonacci recurrence |
| 746 | Min Cost Climbing Stairs | Min instead of count |
| 198 | House Robber | Classic skip-or-take |
| 121 | Best Time to Buy/Sell Stock (single transaction) | Track min-so-far while scanning (also the seed of state-machine DP, file 11) |

### Medium
| # | Problem | Key twist |
|---|---|---|
| 213 | House Robber II | Circular array → run linear version twice |
| 91 | Decode Ways | Recurrence depends on validity checks (1-digit / 2-digit), multiple base-case traps |
| 152 | Maximum Product Subarray | Must track running **min** AND **max** (negative × negative flips sign) |
| 53 | Maximum Subarray | Kadane's, or divide & conquer alt. approach |
| 55 / 45 | Jump Game / Jump Game II | DP formulation possible but greedy is optimal — good "justify greedy vs DP" interview question |
| 740 | Delete and Earn | Reduces to House Robber after bucketing by value |
| 322 | Coin Change | Actually unbounded knapsack (file 04) — commonly mis-filed as "linear DP" by beginners |
| 1218 | Longest Arithmetic Subsequence of Given Difference | 1D DP keyed by value using a HashMap instead of array index |
| 256 / 265 | Paint House I / II | Small fixed branching factor (k colors) per index |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 887 | Super Egg Drop | Classic hard DP; naive O(k·n²) → optimize to O(k·n log n) or O(k·n) with a clever reformulation |
| 1547 | Minimum Cost to Cut a Stick | Looks linear, is actually **interval DP** (file 07) — good pattern-misidentification trap |

## 7. Companies Known to Ask This Pattern
Google, Amazon, Meta, Microsoft, Bloomberg, Apple, Adobe — House Robber and Decode Ways variants are extremely common **first DP question** in phone screens because they cleanly test Steps 1-5 of the framework in ~15-20 minutes.

## 8. Edge Cases & Traps Specific to This Pattern

1. **`n == 0` and `n == 1`** — always handle explicitly before your main loop reads `a[1]` or `dp[i-2]`, or you'll `ArrayIndexOutOfBounds`.
2. **"dp[i] ends at i" vs "dp[i] is the best using first i elements"** — pick one and be consistent. Maximum Subarray needs the *first* definition (answer = max over table); House Robber needs the *second* (answer = `dp[n-1]`).
3. **All-negative arrays** breaking sum-reset-to-zero logic (see Kadane's trap above).
4. **Decode Ways (LC 91) leading zero trap:** `"0"` alone is invalid (0 ways), and `"06"` is invalid as a 2-digit code (must be 10-26) even though `"6"` alone is valid. Many submissions fail on `"10"`, `"100"`, `"110"` because of leading-zero mishandling.
5. **Maximum Product Subarray:** a single negative number flips max↔min, so you must carry **both** running max and running min forward, updating both every step *before* overwriting either (use temp variables).
6. **Circular array problems (House Robber II, Paint House variants with circular constraint):** always double check whether "first and last are adjacent" applies — this single sentence changes the whole solution shape.

## 9. Counter-Questions (to test real understanding, not memorization)

1. *"Can you solve House Robber in O(1) space? Walk me through why that's possible here but not always."* → Because `dp[i]` only ever depends on `dp[i-1]` and `dp[i-2]` — a fixed lookback window. Contrast with LIS (file 05) where `dp[i]` can depend on *any* earlier `dp[j]`, so you can't drop the array.
2. *"What if houses had weights AND you could skip at most one house between robs instead of adjacency-only?"* → State must expand to include "how many skips used" — `dp[i][skipsUsed]`. This tests whether you understand *why* the state was `i` alone in the first place (because adjacency was the only constraint).
3. *"In Maximum Subarray, what if I asked for the subarray with maximum **product** instead of sum?"* → Sign flips require tracking both min and max simultaneously (see §5/§8 above) — tests whether you can extend a known template to a new constraint rather than pattern-matching blindly.
4. *"Prove why greedy fails on Jump Game II if you tried to always jump the farthest reachable index"* — actually greedy IS optimal here (BFS-level argument); good trap question to see if you overthink and assume DP is always required.

Proceed to `02_pattern_2d_grid_dp.md`.
