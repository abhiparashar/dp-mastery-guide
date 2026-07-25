# 03 — Pattern: 0/1 Knapsack

## 1. Pattern Signature
You have a set of **items**, each usable **at most once**, and a **capacity/target constraint** (weight limit, sum target, count limit). You choose a subset to optimize value or hit an exact target. The word "each item used once" is the defining feature — this is what separates it from Unbounded Knapsack (file 04).

## 2. Recognition Checklist
- A list of items, each with a weight/value/cost, **used 0 or 1 times**.
- A capacity or target number you must respect or hit exactly.
- Phrases: "subset", "partition into two equal subsets", "can you achieve a sum of exactly X using each number once", "maximum value without exceeding weight W".

## 3. The Template

**State:** `dp[i][cap]` = best achievable value/count/boolean using the **first `i` items**, with capacity `cap` remaining.

**Recurrence:** for item `i` (0-indexed, `weight[i]`, `value[i]`):
```
dp[i][cap] = max( dp[i-1][cap],                          // don't take item i
                   dp[i-1][cap - weight[i]] + value[i] )  // take item i (only if weight[i] <= cap)
```

```java
class Solution {
    public int knapsack(int[] weight, int[] value, int capacity) {
        int n = weight.length;
        int[][] dp = new int[n + 1][capacity + 1]; // dp[0][*] = 0 (no items → 0 value): implicit base case

        for (int i = 1; i <= n; i++) {
            for (int cap = 0; cap <= capacity; cap++) {
                dp[i][cap] = dp[i - 1][cap]; // don't take item i-1 (0-indexed array, 1-indexed dp)
                if (weight[i - 1] <= cap) {
                    dp[i][cap] = Math.max(dp[i][cap], dp[i - 1][cap - weight[i - 1]] + value[i - 1]);
                }
            }
        }
        return dp[n][capacity];
    }

    // SPACE OPTIMIZATION — THE MOST IMPORTANT TRAP IN THIS ENTIRE PATTERN:
    // Collapse to 1D array, but you MUST iterate capacity DECREASING (right to
    // left). If you iterate increasing, dp[cap - weight[i]] might already have
    // been updated for item i in THIS pass, letting you use item i twice —
    // silently turning 0/1 knapsack into unbounded knapsack.
    public int knapsackOptimized(int[] weight, int[] value, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < weight.length; i++) {
            for (int cap = capacity; cap >= weight[i]; cap--) { // DECREASING — critical
                dp[cap] = Math.max(dp[cap], dp[cap - weight[i]] + value[i]);
            }
        }
        return dp[capacity];
    }
}
```

## 4. Worked Example — Partition Equal Subset Sum (LeetCode 416)

**Reformulation:** can we pick a subset summing to exactly `totalSum / 2`? This is 0/1 knapsack with `value[i] = weight[i] = nums[i]`, asking for **feasibility** (boolean), not max value.

```java
class Solution {
    public boolean canPartition(int[] nums) {
        int sum = 0;
        for (int n : nums) sum += n;
        if (sum % 2 != 0) return false;         // odd total sum: impossible to split evenly — easy miss
        int target = sum / 2;

        boolean[] dp = new boolean[target + 1];
        dp[0] = true; // sum of 0 is always achievable (empty subset) — base case
        for (int num : nums) {
            for (int cap = target; cap >= num; cap--) { // decreasing — same 0/1 trap as above
                dp[cap] = dp[cap] || dp[cap - num];
            }
        }
        return dp[target];
    }
}
```

## 5. Worked Example — Target Sum (LeetCode 494) — Recognizing a Disguised Knapsack

**Problem:** assign `+` or `-` to each number so the expression evaluates to `target`. Count the number of ways.

**The key insight (this is the hard part):** split numbers into a "positive" subset `P` and "negative" subset `N`. Then `sum(P) - sum(N) = target` and `sum(P) + sum(N) = totalSum`. Adding these: `2*sum(P) = target + totalSum`, so `sum(P) = (target + totalSum) / 2`.

This transforms the problem into: **"count subsets of `nums` that sum to `(target + totalSum) / 2`"** — a pure 0/1 knapsack **counting** problem.

```java
class Solution {
    public int findTargetSumWays(int[] nums, int target) {
        int totalSum = 0;
        for (int n : nums) totalSum += n;

        // Impossible cases — MUST check both:
        if (Math.abs(target) > totalSum) return 0;      // target unreachable even with all +
        if ((target + totalSum) % 2 != 0) return 0;      // must be even to split into an integer subset sum

        int subsetSum = (target + totalSum) / 2;
        int[] dp = new int[subsetSum + 1];
        dp[0] = 1; // exactly one way to make sum 0: pick nothing
        for (int num : nums) {
            for (int cap = subsetSum; cap >= num; cap--) {
                dp[cap] += dp[cap - num]; // COUNTING: sum, not max/or
            }
        }
        return dp[subsetSum];
    }
}
```
**Lesson:** many problems don't *look* like knapsack on the surface (assigning signs, splitting into two groups) — recognizing the algebraic transformation into "subset sums to X" is a huge unlock. Practice spotting: "split into two groups," "assign +/-," "difference between two subsets" → almost always reduces to subset-sum knapsack.

## 6. Worked Example — Ones and Zeroes (LeetCode 474) — 2D Capacity Knapsack

**Problem:** each string costs some number of `0`s and some number of `1`s; you have a budget of `m` zeros and `n` ones total; maximize the count of strings you can form (each used at most once).

**This is 0/1 knapsack with TWO capacity dimensions instead of one.**

```java
class Solution {
    public int findMaxForm(String[] strs, int m, int n) {
        int[][] dp = new int[m + 1][n + 1]; // dp[zeros][ones] = max strings achievable

        for (String s : strs) {
            int zeros = 0, ones = 0;
            for (char c : s.toCharArray()) { if (c == '0') zeros++; else ones++; }

            // Both capacity dimensions must iterate DECREASING — same 0/1 trap, just in 2D now
            for (int i = m; i >= zeros; i--) {
                for (int j = n; j >= ones; j--) {
                    dp[i][j] = Math.max(dp[i][j], dp[i - zeros][j - ones] + 1);
                }
            }
        }
        return dp[m][n];
    }
}
```
**Lesson:** the "iterate items in outer loop, capacity in inner loop(s), decreasing" template generalizes cleanly to multiple capacity dimensions — don't be intimidated by 2D/3D capacity, it's the same rule applied per dimension.

## 7. Problem Set

### Easy / Medium
| # | Problem | Key twist |
|---|---|---|
| 416 | Partition Equal Subset Sum | Boolean feasibility knapsack |
| 494 | Target Sum | Algebraic reduction to subset-sum counting |
| 1049 | Last Stone Weight II | Reduces to "minimize \|sum(P) - sum(N)\|" → subset sum closest to totalSum/2 |
| 474 | Ones and Zeroes | 2D capacity dimension |
| 879 | Profitable Schemes | 2D capacity (group size AND minimum profit) + counting mod 1e9+7 |
| charge | Partition to K equal sum subsets (698) | Actually bitmask DP (file 09) — commonly mis-filed here |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 956 | Tallest Billboard | Subset-sum-difference variant with a HashMap-keyed state (difference can be negative, needs offset or map) |
| 1155 | Number of Dice Rolls With Target Sum | Bounded-count knapsack: each "item" (die) contributes 1-6 to the sum, used exactly `n` times |

## 8. Companies Known to Ask This Pattern
Google, Amazon, Meta, Bloomberg, Goldman Sachs (quant-adjacent roles love subset-sum framing), Microsoft — Partition Equal Subset Sum and Target Sum are extremely common Meta/Amazon phone screen questions because the "aha" reduction step is a strong signal of algebraic problem-solving, not just template regurgitation.

## 9. Edge Cases & Traps

1. **THE loop-order trap** (already flagged above, repeating because it's the #1 bug in this pattern): 1D space-optimized 0/1 knapsack **must** iterate capacity decreasing. This is the single most common DP bug in interviews — always narrate out loud *why* you're going backward, to prove it's not accidental.
2. **Odd total sum in Partition Equal Subset Sum** → return `false` immediately; don't let the loop run on a fractional target.
3. **Target Sum: both feasibility checks needed** (`|target| > totalSum` AND `(target+totalSum) % 2 != 0`) — missing either causes wrong answers or array-index-out-of-bounds on a negative/fractional subset sum.
4. **Zero-weight or zero-value items** — an item with `weight = 0` can technically be "taken for free" in every capacity slot; make sure your recurrence doesn't accidentally loop infinitely or double count (it won't in the standard 0/1 template since each item is visited once in the outer loop, but it's worth explicitly reasoning about out loud in an interview).
5. **Counting problems needing modulo arithmetic** (e.g., Profitable Schemes) — apply `% MOD` at every addition, not just at the end, to avoid integer overflow.
6. **Multi-dimensional capacity problems (Ones and Zeroes)** — easy to forget that BOTH inner loops must go decreasing, not just the first one.
7. **Distinguishing "exact target" (boolean/count) from "maximum value within budget" (optimization)** — same table shape, different base case value (`dp[0]=true`/`1` vs `dp[0]=0`) and different combination operator (`||`/`+=` vs `max`).

## 10. Counter-Questions

1. *"Why must the 1D optimized loop go backward for 0/1 knapsack, but forward for unbounded knapsack (file 04)?"* → Going backward ensures `dp[cap - weight[i]]` still reflects the state **before** item `i` was considered this round (i.e., using items `1..i-1` only), preserving "at most once." Going forward would let `dp[cap - weight[i]]` already include item `i` from earlier in the same pass, allowing reuse.
2. *"How would you also return WHICH items were chosen, not just the max value?"* → Keep the full 2D `dp[i][cap]` table (don't space-optimize) and backtrack from `dp[n][capacity]`: if `dp[i][cap] == dp[i-1][cap]`, item `i` wasn't taken; otherwise it was — move to `dp[i-1][cap-weight[i-1]]` and record it.
3. *"What if each item could be taken up to `k` times (bounded knapsack), not just 0 or 1?"* → Either (a) duplicate the item `k` times and run 0/1 knapsack (simple but slower), or (b) use binary/binary-representation splitting of `k` into powers of two to reduce the number of "duplicate items" from `k` to `O(log k)` for efficiency.
4. *"Can Target Sum be solved without the algebraic reduction, directly via recursion + memoization on (index, currentSum)?"* → Yes — state `(i, runningSum)`, but `runningSum` can be negative, so you need a HashMap or an offset-shifted array; this is a valid but less elegant alternative, good to mention both.

Proceed to `04_pattern_unbounded_knapsack.md`.
