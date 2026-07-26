# 01 — Pattern: Linear 1D DP ("Fibonacci-style")

## 1. Pattern Signature
You're scanning a **single sequence left to right**, and the decision at position `i` depends on a **constant, small number of previous positions** (usually `i-1` and/or `i-2`). No 2D grid, no second string, no subsets — just one array/string and one moving index.

## 2. Recognition Checklist
- Problem gives **one array or string**.
- You're asked for a min/max/count/boolean over "ways to reach the end" or "best result up to position i."
- The recurrence for position `i` only needs a **fixed lookback window** (1, 2, or k previous states) — not "all previous states" (that would push you toward a different pattern, see file 05).
- Phrases like: "climbing stairs", "rob houses", "ways to decode", "maximum subarray", "jump game", "delete and earn".

---

## 3. Build the Feel First — Visualize Before Any Code

Before you look at a single `int[] dp`, do this by hand, on paper, every time:

1. **Draw one empty box per index.** `n = 5` → draw 5 boxes in a row: `[ ] [ ] [ ] [ ] [ ]`.
2. **Fill in the base case boxes by hand** (usually index 0, sometimes 0 and 1).
3. **Fill the NEXT box by literally re-doing the brute-force decision using the numbers already sitting in the earlier boxes** — not code, just arithmetic on paper.
4. **Draw an arrow from each box you used into the box you just filled.** This arrow *is* the recurrence. The code is just this arrow, written as `dp[i] = f(dp[i-1], dp[i-2], ...)`.

The array only exists so you don't have to redo step 3 from scratch every time — it's a row of "answers I've already worked out by hand." That's it. Let's do this fully by hand for three problems below, numbers and all, before touching a template.

---

## 4. Worked Example #1 — Climbing Stairs (LeetCode 70) — The Simplest Possible Case

**Problem:** You're at the ground, climbing `n` stairs. Each move you take 1 or 2 steps. How many distinct ways to reach the top?

**Think of it as a literal staircase.** To stand on step `i`, your *last move* was either a 1-step from step `i-1`, or a 2-step from step `i-2`. So:

```
ways(i) = ways(i-1) + ways(i-2)
```

**Step-by-step hand trace (n = 5):**

| Step (i) | Ways to reach it | Why (arithmetic, using boxes already filled) |
|---|---|---|
| 0 (ground) | **1** | Base case — you're already standing here, 1 way (do nothing) |
| 1 | **1** | Only reachable from step 0, one +1 move → `1` |
| 2 | **2** | From step 1 (+1) → `1`, or from step 0 (+2) → `1`. Total = `1 + 1 = 2` |
| 3 | **3** | From step 2 (+1) → `2`, or from step 1 (+2) → `1`. Total = `2 + 1 = 3` |
| 4 | **5** | From step 3 (+1) → `3`, or from step 2 (+2) → `2`. Total = `3 + 2 = 5` |
| 5 | **8** | From step 4 (+1) → `5`, or from step 3 (+2) → `3`. Total = `5 + 3 = 8` |

Visually, the boxes and arrows look like this — every box is just "the box to its left" plus "the box two to its left":

```
index:   0     1     2     3     4     5
value:  [1]   [1]   [2]   [3]   [5]   [8]
               ↑     ↑↖    ↑↖    ↑↖    ↑↖
              (1)  (1+1) (2+1) (3+2) (5+3)
```

**Why memoize at all? See the duplicated work in the raw recursion tree.** If you solved `ways(4)` by pure recursion (no array, no memory), the call tree looks like this:

```
ways(4)
├── ways(3)
│   ├── ways(2)
│   │   ├── ways(1)
│   │   └── ways(0)
│   └── ways(1)
└── ways(2)          <-- SAME subproblem as above — recomputed from scratch!
    ├── ways(1)
    └── ways(0)
```

`ways(2)` gets computed twice, `ways(1)` gets computed three times. For `ways(40)` this duplication explodes exponentially. **The `dp` array is nothing more than "write the answer in the box once, so the next time you need it you just look at the box instead of recomputing the whole tree underneath it."**

```java
class Solution {
    public int climbStairs(int n) {
        if (n <= 1) return 1;
        int[] dp = new int[n + 1];
        dp[0] = 1; // ground: 1 way (do nothing)
        dp[1] = 1; // one step: 1 way
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2]; // exactly the arrow diagram above, nothing more
        }
        return dp[n];
    }
}
```
Read that loop line next to the boxes-and-arrows picture above — `dp[i-1] + dp[i-2]` **is** the two incoming arrows into box `i`. There is no more hidden meaning in the array than "a row of boxes holding answers you already worked out by hand."

---

## 5. Worked Example #2 — House Robber (LeetCode 198) — Same Boxes, Now With a Decision

**Problem:** `nums = [2, 7, 9, 3, 1]`. Rob houses for max money; can't rob two adjacent houses.

**Step 1 (brute force, in words):** at house `i`, you have exactly two options — **skip it** (take whatever the best answer was up through house `i-1`), or **rob it** (add `nums[i]` to the best answer through house `i-2`, since house `i-1` is now forbidden). Take whichever option gives more money.

**Step-by-step hand trace — this time as a decision table, so you can *see* the skip-vs-take choice at every box, not just the final number:**

| i | nums[i] | Option A — SKIP house i → keep `dp[i-1]` | Option B — ROB house i → `nums[i] + dp[i-2]` | dp[i] = max(A, B) | What actually happened |
|---|---|---|---|---|---|
| 0 | 2 | — | — | **2** | base case: rob house 0 |
| 1 | 7 | 2 | `7 + 0 = 7` | **7** | rob house 1 instead (7 > 2) |
| 2 | 9 | 7 | `9 + 2 = 11` | **11** | rob house 2 **and** house 0 |
| 3 | 3 | 11 | `3 + 7 = 10` | **11** | skip house 3 — 11 already beats 10 |
| 4 | 1 | 11 | `1 + 11 = 12` | **12** | rob house 4 **and** house 2 **and** house 0 |

**Final answer: 12** — matches robbing houses at indices 0, 2, 4 → `2 + 9 + 1 = 12`.

The boxes-and-arrows picture (every box pulls from the box directly to its left AND the box two to its left, same shape as Climbing Stairs, just with a `max` and an added value instead of a plain sum):

```
index:     0     1     2     3     4
nums:      2     7     9     3     1
dp:       [2]   [7]  [11]  [11]  [12]
                 ↑     ↑↖    ↑↖    ↑↖
              max(2,   max(7,  max(11,  max(11,
                  7+0)     9+2)    3+7)    1+11)
```

Now the code — every line below is just one row of the table above, written as arithmetic:

```java
class Solution {
    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 0) return 0;
        if (n == 1) return nums[0];

        int prev2 = nums[0];                          // this is "dp[0]" — column 0 of the table
        int prev1 = Math.max(nums[0], nums[1]);        // this is "dp[1]" — column 1 of the table
        for (int i = 2; i < n; i++) {
            int cur = Math.max(prev1, nums[i] + prev2); // Option A vs Option B, exactly the two table columns
            prev2 = prev1;                              // slide the window: today's prev1 becomes tomorrow's prev2
            prev1 = cur;
        }
        return prev1;
    }
}
```
**Complexity:** O(n) time, O(1) space (we only ever needed the *last two* boxes, so we don't need to keep the whole row — we just slide two variables along it).

**Variant — House Robber II (LeetCode 213):** houses are in a **circle** (first and last are adjacent). Trick: run House Robber I twice — once excluding the last house, once excluding the first house — and take the max. Visually: cut the circle open at two different points and solve the resulting straight line each time. This "break the circle into two lines" trick reappears constantly in circular-array DP problems.

**Variant — House Robber III (LeetCode 337):** houses form a **binary tree** — see file `08_pattern_tree_dp.md`.

---

## 6. Worked Example #3 — Maximum Subarray / Kadane's Algorithm (LeetCode 53) — Watch the "Reset"

**Problem:** `nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]`. Find the maximum sum of a *contiguous* subarray.

**State, in plain words:** at each index, ask "if my subarray has to END exactly here, what's the best sum I can get?" That's `currentMax`. Separately, keep a running record of the best `currentMax` ever seen — that's `globalMax`, your actual answer.

**The one decision at every box:** either **extend** the subarray that ended at the previous box (`currentMax_prev + nums[i]`), or **start fresh** right here (`nums[i]` alone) — whichever is bigger. You start fresh exactly when dragging the old subarray along would only drag your sum down.

**Full hand trace, box by box:**

| i | nums[i] | Extend: `currentMax + nums[i]` | Start fresh: `nums[i]` | currentMax (bigger of the two) | globalMax so far |
|---|---|---|---|---|---|
| 0 | -2 | — | -2 | **-2** | -2 |
| 1 | 1 | `-2+1=-1` | 1 | **1** ← starts fresh | 1 |
| 2 | -3 | `1-3=-2` | -3 | **-2** ← extend wins | 1 |
| 3 | 4 | `-2+4=2` | 4 | **4** ← starts fresh | 4 |
| 4 | -1 | `4-1=3` | -1 | **3** ← extend wins | 4 |
| 5 | 2 | `3+2=5` | 2 | **5** ← extend wins | 5 |
| 6 | 1 | `5+1=6` | 1 | **6** ← extend wins | **6** |
| 7 | -5 | `6-5=1` | -5 | **1** ← extend wins | 6 |
| 8 | 4 | `1+4=5` | 4 | **5** ← extend wins | 6 |

**Answer: 6**, achieved by the subarray `[4, -1, 2, 1]` (indices 3-6) — you can literally see it in the table: `currentMax` climbs 4 → 3 → 5 → 6 across exactly those four rows before the streak breaks at index 7.

Notice the picture here is subtly different from Climbing Stairs / House Robber: instead of drawing arrows from two *fixed* earlier boxes, every box only ever looks at the **one box directly before it** — but a *second*, independent running value (`globalMax`) is watching over your shoulder recording the best box seen so far, because the best answer might not be sitting in the very last box.

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int currentMax = nums[0]; // seed with nums[0], NOT 0 — see trap below
        int globalMax = nums[0];
        for (int i = 1; i < nums.length; i++) {
            currentMax = Math.max(nums[i], currentMax + nums[i]); // "start fresh" vs "extend" — the two table columns
            globalMax = Math.max(globalMax, currentMax);           // separately track the best box seen so far
        }
        return globalMax;
    }
}
```
**Trap:** initializing `currentMax`/`globalMax` to `0` instead of `nums[0]` breaks the all-negative-numbers case (e.g., `[-3, -1, -2]` should return `-1`, not `0`). Trace it by hand exactly like the table above with an all-negative array and you'll see immediately why seeding with `0` silently produces a wrong answer that "looks" plausible.

---

## 7. The General Template (Now That You've Seen It Three Times By Hand)

**State:** `dp[i]` = the answer (min/max/count) considering the prefix ending at (or up to) index `i`.

**Recurrence (general shape):** `dp[i] = f(dp[i-1], dp[i-2], ..., a[i])` — literally "the arrows in the box diagram."

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
    // last 1-2 boxes, not the whole row. This is a very common interview
    // follow-up: "Can you do it in O(1) space?" — House Robber above already does this.
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

**Before you write this template on any new problem, do this first, every time:** draw the boxes, fill in the first 4-5 by hand with real numbers from a small example, draw the arrows, and only THEN translate the arrows into the `dp[i] = ...` line. If you can't fill the table by hand, you're not ready to write the code yet — go back and re-derive Step 1 (the brute-force decision) first.

---

## 8. Problem Set (ordered by difficulty)

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

## 9. Companies Known to Ask This Pattern
Google, Amazon, Meta, Microsoft, Bloomberg, Apple, Adobe — House Robber and Decode Ways variants are extremely common **first DP question** in phone screens because they cleanly test Steps 1-5 of the framework in ~15-20 minutes.

## 10. Edge Cases & Traps Specific to This Pattern

1. **`n == 0` and `n == 1`** — always handle explicitly before your main loop reads `a[1]` or `dp[i-2]`, or you'll `ArrayIndexOutOfBounds`.
2. **"dp[i] ends at i" vs "dp[i] is the best using first i elements"** — pick one and be consistent. Maximum Subarray needs the *first* definition (answer = max over table); House Robber needs the *second* (answer = `dp[n-1]`). Go back and look at the two trace tables above side by side — this is exactly why House Robber's table only ever grows or holds steady, while Kadane's `currentMax` column visibly drops back down at index 7.
3. **All-negative arrays** breaking sum-reset-to-zero logic (see Kadane's trap above). Try hand-tracing `[-3, -1, -2]` yourself using the table format from §6 — it's the fastest way to *feel* why the seed value matters.
4. **Decode Ways (LC 91) leading zero trap:** `"0"` alone is invalid (0 ways), and `"06"` is invalid as a 2-digit code (must be 10-26) even though `"6"` alone is valid. Many submissions fail on `"10"`, `"100"`, `"110"` because of leading-zero mishandling.
5. **Maximum Product Subarray:** a single negative number flips max↔min, so you must carry **both** running max and running min forward, updating both every step *before* overwriting either (use temp variables).
6. **Circular array problems (House Robber II, Paint House variants with circular constraint):** always double check whether "first and last are adjacent" applies — this single sentence changes the whole solution shape.

## 11. Counter-Questions (to test real understanding, not memorization)

1. *"Can you solve House Robber in O(1) space? Walk me through why that's possible here but not always."* → Because `dp[i]` only ever depends on `dp[i-1]` and `dp[i-2]` — a fixed lookback window, i.e., only the last two boxes ever matter. Contrast with LIS (file 05) where `dp[i]` can depend on *any* earlier `dp[j]`, so you can't drop the array — you'd need every box, not just the last two.
2. *"What if houses had weights AND you could skip at most one house between robs instead of adjacency-only?"* → State must expand to include "how many skips used" — `dp[i][skipsUsed]`. This tests whether you understand *why* the state was `i` alone in the first place (because adjacency was the only constraint).
3. *"In Maximum Subarray, what if I asked for the subarray with maximum **product** instead of sum?"* → Sign flips require tracking both min and max simultaneously (see §6/§10 above) — tests whether you can extend a known template to a new constraint rather than pattern-matching blindly.
4. *"Prove why greedy fails on Jump Game II if you tried to always jump the farthest reachable index"* — actually greedy IS optimal here (BFS-level argument); good trap question to see if you overthink and assume DP is always required.

Proceed to `02_pattern_2d_grid_dp.md`.
