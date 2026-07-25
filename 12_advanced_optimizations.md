# 12 — Advanced DP Optimization Techniques

> This file is different from the previous ones: it's not a new "problem pattern" but a set of **techniques you layer on top of an already-correct DP solution** to reduce its time or space complexity. Interviewers use these as follow-ups after you've gotten a correct O(n²) or O(n·capacity) solution — "can you do better?"

---

## 1. Space Optimization (Rolling Arrays) — Applies Almost Everywhere

**The core observation:** if `dp[i][...]` only ever depends on `dp[i-1][...]` (the immediately previous row/layer), you never need to keep more than the last 1-2 rows in memory.

**Generic technique:**
```java
// BEFORE: O(n * m) space
int[][] dp = new int[n + 1][m + 1];
for (int i = 1; i <= n; i++)
    for (int j = 1; j <= m; j++)
        dp[i][j] = f(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]);

// AFTER: O(m) space — collapse to a single 1D rolling array
int[] dp = new int[m + 1];
for (int i = 1; i <= n; i++) {
    int prevDiag = 0; // holds what would have been dp[i-1][j-1] before this row overwrites it
    for (int j = 1; j <= m; j++) {
        int temp = dp[j];               // this is still the "old" (row i-1) value before we overwrite it
        dp[j] = f(dp[j], dp[j-1], prevDiag); // dp[j] = old (i-1,j), dp[j-1] = new (i,j-1), prevDiag = old (i-1,j-1)
        prevDiag = temp;
    }
}
```
This exact "temp variable holding the diagonal" trick is needed whenever the recurrence needs THREE neighbors (like LCS, Edit Distance) rather than just two (like Unique Paths, which only needs `dp[i-1][j]` and `dp[i][j-1]` and can skip the diagonal-tracking complexity entirely).

**When to bring this up in an interview:** always mention it as a natural follow-up once you have a correct 2D solution — *"since row i only depends on row i-1, I can reduce space from O(nm) to O(m)"* — even if you don't fully implement it unless asked.

---

## 2. Monotonic Deque Optimization — "Sliding Window Max/Min Inside a DP Transition"

**When it applies:** your recurrence has the shape `dp[i] = a[i] + max(dp[j] for j in [i-k, i-1])` (or `min`) — a fixed- or variable-size **window** of previous DP values, and you'd naively recompute the max/min over that window at every step (O(n·k) total).

**The fix:** maintain a **monotonic deque** of indices, where the front of the deque always holds the index of the current maximum (or minimum) within the valid window — each element enters and leaves the deque at most once, giving O(n) total instead of O(n·k).

### Worked Example — Constrained Subsequence Sum (LeetCode 1425)

**Problem:** choose a subsequence where consecutive chosen elements are at most `k` apart in the original array, maximizing the sum.

**Naive recurrence:** `dp[i] = nums[i] + max(0, dp[i-k], dp[i-k+1], ..., dp[i-1])` — an O(n·k) sliding window max.

```java
class Solution {
    public int constrainedSubsetSum(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        Deque<Integer> deque = new ArrayDeque<>(); // stores INDICES, monotonically decreasing dp-values front-to-back
        int result = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            // Remove indices that have fallen out of the valid window [i-k, i-1]
            while (!deque.isEmpty() && deque.peekFirst() < i - k) {
                deque.pollFirst();
            }

            int bestPrev = deque.isEmpty() ? 0 : Math.max(0, dp[deque.peekFirst()]);
            dp[i] = nums[i] + bestPrev;
            result = Math.max(result, dp[i]);

            // Maintain monotonic decreasing order: pop smaller values from the back
            // before pushing dp[i], since they can never be the max again while dp[i] is in the window
            while (!deque.isEmpty() && dp[deque.peekLast()] < dp[i]) {
                deque.pollLast();
            }
            deque.offerLast(i);
        }
        return result;
    }
}
```
**Related problems using this exact idea:** LeetCode 239 (Sliding Window Maximum — the "pure" version without the DP wrapper), LeetCode 1696 (Jump Game VI, nearly identical structure to Constrained Subsequence Sum).

**Recognition signal:** if you find yourself writing an inner loop that recomputes `max`/`min` over a shrinking-and-growing window of previous `dp` values, stop and ask "can a monotonic deque maintain this window's max/min incrementally instead?"

---

## 3. Knuth's Optimization — Interval DP from O(n³) to O(n²)

**When it applies:** interval DP (file 07) where the recurrence is `dp[i][j] = min over k of (dp[i][k] + dp[k+1][j]) + cost(i,j)`, AND the cost function satisfies the **quadrangle inequality** (a technical monotonicity condition). Under this condition, you can prove the **optimal split point `k` is monotonic** — i.e., `opt[i][j-1] <= opt[i][j] <= opt[i+1][j]` — which lets you restrict the search range for `k` at each `(i,j)`, collapsing the total work from O(n³) to O(n²).

```java
// Conceptual sketch — NOT a drop-in template, since the quadrangle inequality
// must be verified for your specific cost function before this is valid.
int[][] opt = new int[n][n]; // opt[i][j] = the best split point k found for range [i,j]

for (int len = 2; len <= n; len++) {
    for (int i = 0; i + len - 1 < n; i++) {
        int j = i + len - 1;
        int lo = opt[i][j - 1];         // lower bound from a smaller-by-one-column range
        int hi = opt[i + 1][j];          // upper bound from a smaller-by-one-row range
        dp[i][j] = Integer.MAX_VALUE;
        for (int k = lo; k <= hi; k++) { // restricted search range, not the full [i, j)
            int cost = dp[i][k] + dp[k + 1][j] + costFn(i, j);
            if (cost < dp[i][j]) { dp[i][j] = cost; opt[i][j] = k; }
        }
    }
}
```
**Interview framing:** you are very unlikely to be asked to derive or prove the quadrangle inequality live. The expected signal is **awareness**: *"Matrix Chain Multiplication / this interval DP is O(n³); if the cost function is well-behaved (satisfies the quadrangle inequality), Knuth's optimization can bring this down to O(n²) by exploiting monotonicity of the optimal split point."* Naming this correctly, and knowing roughly why it works, is usually sufficient for "advanced optimization awareness" credit — full derivation is graduate-algorithms-course territory, not standard interview territory.

---

## 4. Divide and Conquer Optimization — Similar Spirit, Different Shape

**When it applies:** DP of the shape `dp[i][j] = min over k < j of (dp[i-1][k] + cost(k, j))` for a **fixed number of "layers" `i`** (as opposed to Knuth's interval-splitting shape) — again requiring the cost function to satisfy a monotonicity property (the optimal `k` for `dp[i][j]` is monotonic in `j`).

**The technique:** solve for the middle `j` first, find its optimal `k` via brute-force search, then **recursively solve the left half of `j`'s range using only `k`'s in `[lo, optK]`, and the right half using only `k`'s in `[optK, hi]`** — a divide-and-conquer recursion over the `j` dimension, reducing each layer's total work from O(n²) to O(n log n).

**When you'd realistically bring this up:** almost exclusively for problems explicitly framed as "partition an array into exactly `m` groups minimizing some cost" at a "hard" or competitive-programming-adjacent difficulty — again, **naming it correctly** ("this could use divide-and-conquer DP optimization since the cost function likely satisfies the monotonicity condition") is the realistic interview bar; full implementation is rarely expected live.

---

## 5. Matrix Exponentiation — For Linear Recurrences Over Huge `n`

**When it applies:** a *linear* recurrence (like Fibonacci: `f(n) = f(n-1) + f(n-2)`) where `n` is enormous (e.g., `10^18`), so even O(n) DP is too slow.

**The technique:** express the recurrence as a matrix multiplication, then use **fast exponentiation** (repeated squaring) to compute the `n`-th term in O(log n) matrix multiplications.

```java
// Fibonacci via matrix exponentiation: [[1,1],[1,0]]^n gives F(n+1), F(n) in its entries
class Solution {
    public long fib(long n) {
        if (n == 0) return 0;
        long[][] base = {{1, 1}, {1, 0}};
        long[][] result = matrixPower(base, n - 1);
        return result[0][0];
    }

    private long[][] matrixPower(long[][] m, long p) {
        long[][] result = {{1, 0}, {0, 1}}; // identity matrix
        while (p > 0) {
            if ((p & 1) == 1) result = multiply(result, m);
            m = multiply(m, m);
            p >>= 1;
        }
        return result;
    }

    private long[][] multiply(long[][] a, long[][] b) {
        int n = a.length, m = b[0].length, k = b.length;
        long[][] res = new long[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                for (int l = 0; l < k; l++)
                    res[i][j] += a[i][l] * b[l][j];
        return res;
    }
}
```
**Recognition signal:** if a problem's recurrence is **linear** (each term is a fixed linear combination of a fixed number of previous terms) and `n` is given as absurdly large (way beyond what O(n) could handle in the time limit), matrix exponentiation is the tool. This is a relatively rare but recognizable "gotcha" constraint.

---

## 6. Bitset Optimization — Speeding Up Boolean Knapsack-Style DP

**When it applies:** a boolean subset-sum-style DP (`dp[j] |= dp[j - w]`) where you're processing many items against a large capacity — Java's `BitSet` (or a `long[]` used as a manual bitset) can process **64 capacity values at once per machine word**, turning an O(n × capacity) loop into roughly O(n × capacity / 64).

```java
// Conceptual: subset-sum feasibility using BitSet instead of boolean[]
BitSet dp = new BitSet(target + 1);
dp.set(0);
for (int num : nums) {
    BitSet shifted = new BitSet(target + 1);
    for (int i = dp.nextSetBit(0); i >= 0 && i + num <= target; i = dp.nextSetBit(i + 1)) {
        shifted.set(i + num);
    }
    dp.or(shifted);
}
// Faster in practice: dp.or(dp.get(0, target - num + 1) shifted left by num) using a hand-rolled shift,
// since BitSet doesn't expose a native "shift" operation — often implemented with raw long[] words for speed.
```
**Interview framing:** this is a genuinely advanced, somewhat rare optimization — bring it up only if explicitly asked "how would you speed this up further" after an already-correct O(n × capacity) subset-sum solution, as a demonstration of low-level optimization awareness (bit-parallelism), not as a default approach.

---

## 7. Summary Table — When To Reach For Each Optimization

| Technique | Reduces | Signal to look for |
|---|---|---|
| Rolling array / space optimization | Space only (same time) | `dp[i][...]` only depends on `dp[i-1][...]` |
| Monotonic deque | O(n·k) → O(n) | Recurrence has a sliding-window max/min over previous `dp` values |
| Knuth's optimization | O(n³) → O(n²) | Interval DP + cost function is "well-behaved" (quadrangle inequality) |
| Divide & conquer optimization | O(n²) per layer → O(n log n) per layer | Layered partition DP + monotonic optimal split point |
| Matrix exponentiation | O(n) → O(log n) | Recurrence is linear AND `n` is astronomically large |
| Bitset optimization | Constant-factor speedup (÷64) | Boolean subset-sum-style DP with large capacity |

## 8. Counter-Questions

1. *"You have an O(nm) space DP. Walk me through exactly how you'd reduce it to O(m), including how you'd handle a recurrence that needs the diagonal value `dp[i-1][j-1]`."* → Use a single rolling 1D array plus one extra scalar variable (`prevDiag`) to hold the "about to be overwritten" diagonal value before each row's update, as shown in §1.
2. *"How do you know when a sliding-window-max-inside-DP problem needs a monotonic deque versus just a simple running max?"* → A simple running max works if old values never need to be evicted (window only grows, or is unbounded on the left); a monotonic deque is needed specifically when the window has a **shrinking left boundary** (values can expire), requiring eviction of stale (and provably suboptimal) candidates.
3. *"Why can't you always apply Knuth's optimization to any interval DP?"* → It requires the cost function to satisfy the quadrangle inequality (a specific monotonicity property) — without it, the optimal split point isn't guaranteed to be monotonic across ranges, and restricting the search range for `k` could silently skip the true optimum.
4. *"When would matrix exponentiation NOT help, even for a huge `n`?"* → If the recurrence is non-linear (e.g., involves a `max`/`min` or a nested conditional, not a fixed linear combination), it can't be expressed as matrix multiplication — matrix exponentiation only applies to genuinely linear recurrences.

Proceed to `13_company_wise_question_bank.md`.
