# 09 — Pattern: Bitmask DP

## 1. Pattern Signature
The state needs to track **which subset of a small set of items has been used/visited**, and the natural way to encode "a subset of up to ~20-25 items" is a single integer where **bit `i` represents whether item `i` is included**. This lets you use an `int` (or `long` for >31 items) as a dictionary key / array index for an entire subset, instead of an actual `Set` object.

## 2. Recognition Checklist
- Constraint `n ≤ 20` (sometimes up to `~25`, since `2^25 ≈ 33M` is still often feasible) — **this specific constraint bound is the single strongest signal for bitmask DP** in the entire guide. If you see `n <= 20` in a problem with no other obvious pattern fitting, try bitmask DP first.
- "Visit all nodes/cities", "partition into groups", "assign tasks to workers", "each item must be used exactly once across several categories."
- The order in which items are chosen usually doesn't matter for the final answer, only *which* items have been used so far — a classic sign you can compress "history" into a subset instead of a sequence.

## 3. The Template

**State:** `dp[mask][i]` = best answer when the subset of used items is exactly `mask`, and (if relevant) you're currently "at" item/position `i`.

```java
class Solution {
    public int solve(int[][] cost, int n) {
        int FULL = (1 << n) - 1;
        int[][] dp = new int[1 << n][n];
        for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE);

        // base cases: starting at each single node with just itself visited
        for (int i = 0; i < n; i++) dp[1 << i][i] = 0;

        for (int mask = 1; mask <= FULL; mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0) continue;   // 'last' must be in the current mask
                if (dp[mask][last] == Integer.MAX_VALUE) continue; // unreachable state — skip

                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) continue; // 'next' must NOT already be visited
                    int newMask = mask | (1 << next);
                    int newCost = dp[mask][last] + cost[last][next];
                    dp[newMask][next] = Math.min(dp[newMask][next], newCost);
                }
            }
        }

        int best = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (dp[FULL][i] != Integer.MAX_VALUE) best = Math.min(best, dp[FULL][i]);
        }
        return best;
    }
}
```
**Key bit-manipulation idioms you must have memorized cold:**
```java
mask | (1 << i)        // add item i to the subset
mask & ~(1 << i)        // remove item i from the subset
(mask & (1 << i)) != 0   // check if item i is in the subset
Integer.bitCount(mask)   // count how many items are in the subset
(1 << n) - 1             // the "full set" mask (all n items included)
```

## 4. Worked Example — Traveling Salesman Problem (TSP), Minimum Cost Hamiltonian Path

Exactly the template in §3 — `dp[mask][last]` = min cost to visit exactly the cities in `mask`, ending at city `last`. Complexity: **O(2^n × n²)** — for `n=20`, that's roughly `20² × 2^20 ≈ 4 × 10^8`, at the edge of feasible in a tight time limit, which is why TSP-style problems cap at `n ≈ 15-20` in practice.

## 5. Worked Example — Partition to K Equal Sum Subsets (LeetCode 698)

**Problem:** can `nums` be partitioned into `k` subsets each summing to `sum(nums)/k`?

**State:** `dp[mask]` = does the subset of used numbers represented by `mask` form some number of **complete** groups (each summing exactly to `target`), possibly with one partially-filled group in progress?

```java
class Solution {
    public boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = 0;
        for (int n : nums) sum += n;
        if (sum % k != 0) return false;
        int target = sum / k;

        Arrays.sort(nums);
        int n = nums.length;
        if (nums[n - 1] > target) return false; // a single element bigger than target: impossible

        Integer[] memo = new Integer[1 << n]; // null = not computed, using Integer for 3-state (null/true/false) via boolean encoding
        Boolean[] dp = new Boolean[1 << n];
        int[] currentSum = new int[1 << n];    // currentSum[mask] = sum of elements in mask, mod target implicitly tracked via recursion

        return backtrack(nums, 0, k, target, 0, dp, currentSum);
    }

    private boolean backtrack(int[] nums, int mask, int k, int target, int cur, Boolean[] dp, int[] currentSum) {
        if (k == 0) return true; // all groups successfully formed
        if (dp[mask] != null) return dp[mask];

        for (int i = 0; i < nums.length; i++) {
            if ((mask & (1 << i)) != 0) continue;   // already used
            if (cur + nums[i] > target) continue;    // would overshoot this group's target

            int newCur = cur + nums[i];
            int newMask = mask | (1 << i);
            boolean result;
            if (newCur == target) {
                result = backtrack(nums, newMask, k - 1, target, 0, dp, currentSum); // this group is complete, start a new one
            } else {
                result = backtrack(nums, newMask, k, target, newCur, dp, currentSum);
            }
            if (result) return dp[mask] = true;
        }
        return dp[mask] = false;
    }
}
```
**Lesson:** not every bitmask DP problem needs a full `dp[mask][extra dimension]` table upfront — sometimes memoizing purely on `mask` (with the "current group's running sum" passed as a recursion parameter rather than part of the memo key, since it's determined by `mask` and `k` remaining together) is enough, if you can argue the running sum is uniquely determined by the other state. Always double check this determinism assumption carefully, though — it's a subtle correctness argument, not just an optimization.

## 6. Worked Example — Shortest Path Visiting All Nodes (LeetCode 847)

**Problem:** given an undirected graph, find the shortest path that visits every node at least once (can revisit nodes/edges).

**This is BFS + bitmask, not pure DP** — but the state space `(node, mask)` is identical to bitmask DP, and it's commonly grouped with this pattern in interview prep because the *state design* is the hard part, shared with TSP.

```java
class Solution {
    public int shortestPathLength(int[][] graph) {
        int n = graph.length;
        int FULL = (1 << n) - 1;
        Queue<int[]> queue = new LinkedList<>(); // {node, mask}
        boolean[][] visited = new boolean[n][1 << n];

        for (int i = 0; i < n; i++) {
            queue.offer(new int[]{i, 1 << i});
            visited[i][1 << i] = true;
        }

        int steps = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int s = 0; s < size; s++) {
                int[] cur = queue.poll();
                int node = cur[0], mask = cur[1];
                if (mask == FULL) return steps;

                for (int next : graph[node]) {
                    int newMask = mask | (1 << next);
                    if (!visited[next][newMask]) {
                        visited[next][newMask] = true;
                        queue.offer(new int[]{next, newMask});
                    }
                }
            }
            steps++;
        }
        return -1; // unreachable in a connected graph, included for completeness
    }
}
```
**Lesson:** bitmask state design isn't exclusive to "pure DP" solutions — it's a general technique for compressing "which subset of things has happened so far" into a state, useful in BFS/Dijkstra just as much as in tabulation-style DP.

## 7. Problem Set

### Medium
| # | Problem | Key twist |
|---|---|---|
| 698 | Partition to K Equal Sum Subsets | Mask + running-sum-in-recursion-params trick |
| 847 | Shortest Path Visiting All Nodes | BFS + bitmask state, not pure DP table |
| 1723 | Find Minimum Time to Finish All Jobs | Bitmask over jobs, distributed across `k` workers — combines bitmask DP with an outer search/binary-search-on-answer |
| 526 | Beautiful Arrangement | Bitmask counting: number of permutations satisfying a divisibility constraint |

### Hard
| # | Problem | Key twist |
|---|---|---|
| classic | Traveling Salesman Problem | The canonical `dp[mask][last]` template |
| 943 | Find the Shortest Superstring | Bitmask DP where the "cost" between items is precomputed string-overlap length — TSP variant with a nontrivial cost function |
| 1349 | Maximum Students Taking Exam | Bitmask DP per row, where each row's mask must be compatible with the row above (no adjacent cheating) — 2D bitmask compatibility check |
| 1595 | Minimum Cost to Connect Two Groups of Points | Bitmask over one group, with careful handling of "unmatched" points in the other group |

## 8. Companies Known to Ask This Pattern
Google, Amazon, Meta, Uber, Two Sigma — Bitmask DP appears heavily in **Google onsite "hard" rounds** and quant-adjacent interviews (Two Sigma, Citadel) because it requires recognizing a non-obvious state compression; Shortest Path Visiting All Nodes is a well-known Google/Meta hard question that combines two data structures/paradigms (BFS + bitmask) that candidates don't usually think to combine.

## 9. Edge Cases & Traps

1. **`n` too large for bitmask DP** — if `n > ~25`, `2^n` becomes computationally infeasible; always sanity-check the problem's constraints before committing to this approach. If you see `n` up to `10^5` but a *different* small parameter `k ≤ 20`, the mask might be over `k`, not `n` — read constraints carefully to find which quantity is actually small.
2. **Off-by-one in bit indexing** — bit `i` represents item `i` (0-indexed) — mixing up 0-indexed items with 1-indexed bit positions is a frequent source of bugs; always sanity check with a tiny example (`n=3`, verify `mask=0b101` means items 0 and 2 are included).
3. **Using `int` for masks when `n` could reach 31+ items** — `1 << 31` overflows a signed 32-bit int into a negative number; switch to `long` and `1L << i` if `n` can exceed 30.
4. **Forgetting to check `dp[mask][...]` reachability before using it in a transition** (as flagged with the `Integer.MAX_VALUE` sentinel check in the TSP template) — using an "unreachable" sentinel value in arithmetic silently corrupts downstream states, same trap as file 00 §8.2 and file 04 §9.2, now in a bitmask context.
5. **Memory blowup** — `dp[1 << n][n]` for `n=20` is `2^20 × 20 ≈ 21M` ints — about 84MB, borderline but often fine; for `n=25` this becomes infeasible (over 800M states) — always compute expected memory footprint explicitly before coding, and mention it as a constraint-driven design decision in the interview.
6. **Assuming bitmask DP is always about Hamiltonian-path-style "visit everything" problems** — it also applies to "partition/assignment" problems (Partition to K Equal Subsets, job-to-worker assignment) where the mask represents "which items have been assigned so far," not a path/tour.

## 10. Counter-Questions

1. *"Why is TSP's bitmask DP O(2^n × n²) and not O(2^n × n) or O(n!)?"* → `2^n` masks, times `n` choices for "last visited node," times another `n` for "which next node to try transitioning to" — the two nested `n` loops (last, next) give the extra factor of `n` beyond just `2^n × n`. Compare to brute-force permutations at O(n!), which bitmask DP improves upon by reusing overlapping subproblems (same mask+last reached via different orderings).
2. *"How would you reconstruct the actual optimal tour, not just its cost, in TSP?"* → Keep a parallel `parent[mask][last]` table storing which `(prevMask, prevLast)` produced the optimal value, then backtrack from `(FULL, bestEndNode)`.
3. *"At what point does bitmask DP stop being feasible, and what would you do instead for larger n?"* → Once `n` exceeds ~25-ish (memory/time), you'd need approximation algorithms (e.g., nearest-neighbor + 2-opt for TSP), branch-and-bound with pruning, or problem-specific heuristics — bitmask DP is an *exact* method that trades exponential complexity for correctness, appropriate only when `n` is provably small.
4. *"In Partition to K Equal Sum Subsets, why is it valid to memoize on `mask` alone, without including the current group's running sum in the memo key?"* → Because sorting `nums` descending first and always filling the current group greedily in a fixed traversal order makes the running sum a deterministic function of `mask` and how many complete groups have been formed — not an independent piece of state. This is worth being able to justify carefully since it's easy to get wrong in a slightly different problem variant.

Proceed to `10_pattern_digit_dp.md`.
