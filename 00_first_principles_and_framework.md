# 00 — Dynamic Programming: First Principles & The Universal Framework

> Goal of this file: build the *mental model* you will reuse for every single problem in this guide. Everything after this file is pattern-specific vocabulary layered on top of this one framework.

---

## 1. What Dynamic Programming Actually Is

DP is **recursion + memory**, applied when a problem has two properties:

1. **Optimal Substructure** — the optimal (or count, or boolean feasibility) answer to a problem can be built from optimal answers to its subproblems.
   - Example: shortest path from A→C via B = shortest(A→B) + shortest(B→C).
2. **Overlapping Subproblems** — the same subproblem is recomputed many times if you do plain recursion.
   - Example: `fib(5)` calls `fib(3)` twice, `fib(2)` three times, etc.

If a problem has (1) but **not** (2), plain recursion/divide-and-conquer (e.g., merge sort) is enough — DP won't add value.
If a problem has (2) but **not** (1) (rare), DP doesn't apply — you need something else (e.g., graph algorithms without the optimal-substructure guarantee).

**One-line definition:** *DP = Brute-force recursion, where you cache the answer to each distinct subproblem so you never solve it twice.*

---

## 2. The Four Stages Every DP Solution Passes Through

You should be able to name which stage you're in at all times.

| Stage | What it looks like | Time Complexity | When to use |
|---|---|---|---|
| **Stage 1: Brute-force recursion** | Plain recursive function, no cache | Exponential (e.g. O(2^n)) | Always write this first, mentally or on paper |
| **Stage 2: Top-down memoization** | Same recursion + a cache (HashMap/array) keyed by state | O(states × work-per-state) | Great for irregular state spaces, easier to derive from Stage 1 |
| **Stage 3: Bottom-up tabulation** | Iterative, fill a DP table in dependency order | Same as Stage 2, but no recursion overhead | Preferred in interviews once you're confident — avoids stack overflow |
| **Stage 4: Space optimization** | Reduce table dimensions using rolling arrays / variables | Same time, less space | Do this *last*, after correctness, and only if asked or if it's easy |

**Critical interview habit:** Always derive Stage 3 *from* Stage 1/2. Never try to "pattern match" straight to a tabulation table — that's how people get stuck on unfamiliar problems. The recursion is the source of truth; the table is just a cache.

---

## 3. The Universal 5-Step Framework

For **every** DP problem, answer these five questions **in this order**:

### Step 1 — Can I brute force it with recursion (try all choices)?
Ask: *"At each step, what choices do I have?"* Write a recursive function that tries every choice and combines results (min/max/sum/OR of booleans). Don't worry about efficiency yet.

### Step 2 — What is the "state"? (i.e., what parameters fully describe a subproblem?)
A state is the **minimum set of variables** such that, given their values, the answer to the subproblem is fixed regardless of how you got there.

- Ask yourself: *"If I re-enter this function with the same parameter values from a different path, will I get the same answer?"* If yes, those parameters are your state, and you can memoize on them.
- Common state variables: current index, remaining capacity/sum/count, a boolean flag (e.g., "have I used my one skip?"), previous element chosen, current row/col in a grid.

### Step 3 — What is the recurrence (transition)?
Express `dp[state]` in terms of `dp[smaller/earlier states]`. This is usually a direct translation of the recursive branches from Step 1.

### Step 4 — What are the base cases?
The smallest states you can answer directly without recursing further (e.g., `dp[0] = ...`, empty string, index out of bounds).

### Step 5 — What is the iteration order (if bottom-up)?
Every state must be computed **after** all states it depends on. Usually: increasing index, increasing capacity, or increasing subsequence length (for interval DP). This is where most "off-by-one" bugs live.

---

## 4. Java Templates You Will Reuse Constantly

### 4.1 Top-down memoization skeleton
```java
class Solution {
    // -1 (or Integer.MIN_VALUE) means "not computed yet" — pick a sentinel
    // that can never be a valid answer.
    private Integer[][] memo;

    public int solve(int[] input, int n) {
        memo = new Integer[n][/* other dimension size */];
        return dp(input, 0 /* start state */);
    }

    private int dp(int[] input, int i /*, other state vars */) {
        // 1. Base case
        if (i == input.length) return 0; // or 1, or true/false depending on problem

        // 2. Check cache
        if (memo[i][/*...*/] != null) return memo[i][/*...*/];

        // 3. Try all choices, combine with min/max/sum
        int best = Integer.MIN_VALUE; // or MAX_VALUE, or 0, depending on problem
        // best = Math.max(best, choice1 + dp(input, i + 1));
        // best = Math.max(best, choice2 + dp(input, i + skip));

        // 4. Cache and return
        return memo[i][/*...*/] = best;
    }
}
```

### 4.2 Bottom-up tabulation skeleton
```java
class Solution {
    public int solve(int[] input) {
        int n = input.length;
        int[] dp = new int[n + 1]; // +1 to hold the base case cleanly

        // 1. Base case(s)
        dp[0] = /* ... */;

        // 2. Fill table in dependency order
        for (int i = 1; i <= n; i++) {
            dp[i] = /* recurrence using dp[i-1], dp[i-2], etc. */;
        }

        // 3. Answer usually lives at dp[n] (or max/min over the table)
        return dp[n];
    }
}
```

### 4.3 State encoding for multi-dimensional memoization
Java has no tuples as map keys out of the box. Two clean options:

```java
// Option A: multi-dimensional array (fastest, use when state vars are small ints)
Integer[][] memo = new Integer[n + 1][capacity + 1];

// Option B: encode composite state into a single long key (use when dims are sparse/huge)
Map<Long, Integer> memo = new HashMap<>();
long key = (long) i * 100000L + j; // ensure no collisions — multiply by a safe bound
```
Prefer **Option A** (arrays) in interviews — it's O(1) access, no boxing/hashing overhead, and signals cleaner thinking. Reach for HashMap only when the state space is sparse (e.g., bitmask + huge index) or has more than ~3 dimensions.

---

## 5. How To *Recognize* a DP Problem (Pattern-Recognition Checklist)

Ask these questions when you first read a problem:

1. **Does it ask for an optimum?** ("minimum/maximum number of...", "longest/shortest...") → likely DP or greedy. Check if greedy fails on a counter-example (see §7) → if greedy fails, it's DP.
2. **Does it ask to count something?** ("number of ways to...", "how many distinct...") → almost always DP (sum over choices instead of max/min over choices).
3. **Does it ask for feasibility?** ("can you reach...", "is it possible to partition...") → DP with boolean states (OR over choices).
4. **Are there sequential decisions with constraints that depend on earlier decisions?** (e.g., "you can't rob two adjacent houses") → DP, because the choice at step `i` interacts with the choice at step `i-1`.
5. **Would brute force be exponential, but the input size is small-to-medium (n ≤ 10⁴, or n ≤ 20 for bitmask, or two strings of length ≤ 10³)?** → strong DP signal. If n ≤ 20-25, think **bitmask DP**. If n ≤ 10³ with a grid/2 strings, think **O(n²) DP**. If n ≤ 10⁵-10⁶, think **O(n) or O(n log n) DP**.
6. **Can you draw the "choices" as a tree where subtrees repeat?** → overlapping subproblems confirmed.

**Explicit anti-patterns (things that LOOK like DP but aren't, or need a twist):**
- If the problem only ever needs *one* greedy locally-optimal choice with no need to reconsider — it's greedy, not DP (e.g., interval scheduling maximizing count of non-overlapping intervals is greedy; interval scheduling maximizing *weighted* sum is DP).
- If subproblems don't overlap — it's divide & conquer (e.g., merge sort, quickselect).
- If the "state" needed is unbounded/continuous — DP may not apply directly; look for a different structure (binary search on the answer, math, greedy + exchange argument).

---

## 6. Top-Down vs Bottom-Up — How To Choose in an Interview

| | Top-down (memoization) | Bottom-up (tabulation) |
|---|---|---|
| Easier to derive from brute force | ✅ Yes — minimal code change | ❌ Requires figuring out iteration order |
| Avoids computing unreachable states | ✅ Yes | ❌ No — computes entire table |
| Risk of stack overflow (Java default stack ~512KB-1MB) | ⚠️ Yes, for n > ~10,000 depth | ✅ No recursion |
| Easier to space-optimize | ❌ Harder | ✅ Easier (rolling arrays) |
| What most interviewers want to see first | Either — but be ready to convert to the other on request | |

**Recommended interview strategy:** Say the recurrence out loud, code the **top-down** version first (fastest to get correct), then say *"I can convert this to bottom-up tabulation to avoid recursion overhead and enable space optimization"* — and do it if time allows. This demonstrates full mastery of Stages 1-4 from §2.

---

## 7. Greedy vs DP — The Classic Trap

Interviewers love asking you to justify *why* greedy fails so DP is needed. Two canonical counter-examples to have ready:

**Coin Change (minimum coins) with weird denominations:**
Coins = `{1, 3, 4}`, target = `6`.
- Greedy (always take the largest coin ≤ remaining): 4 + 1 + 1 = **3 coins**.
- Optimal: 3 + 3 = **2 coins**.
- Greedy fails because a locally-optimal large coin can block a better global combination. → Must use DP (try every coin at every step).

**0/1 Knapsack vs Fractional Knapsack:**
- Fractional knapsack (you can take a fraction of an item) → greedy by value/weight ratio works.
- 0/1 knapsack (all-or-nothing per item) → greedy by ratio can fail (e.g., one heavy high-ratio item blocks two lighter items with higher combined value) → must use DP.

**Rule of thumb:** If taking the "obviously best" choice right now can ever *lock you out* of a better future option, it's DP, not greedy. If you're not sure, try to construct a 3-4 element counter-example — if you can, it's DP.

---

## 8. Traps, Edge Cases, and Bugs That Cost Interview Points

These are the mistakes that separate "solved it" from "solved it correctly under pressure." Keep this list close — every pattern-file later will reference back to it.

1. **Off-by-one in array sizing.** DP tables are usually sized `n+1` so `dp[0]` can represent "zero elements considered" as a clean base case. Forgetting this forces awkward index shifting everywhere.
2. **Wrong sentinel value causing silent overflow.** `dp[i] = Integer.MAX_VALUE; ... dp[i] = dp[i-1] + 1;` → if `dp[i-1]` is still `MAX_VALUE` (unreachable state) and you add 1, you overflow to `Integer.MIN_VALUE` and corrupt all downstream comparisons. Fix: check reachability explicitly (`if (dp[i-1] == MAX_VALUE) continue;`) or use a large-but-safe sentinel like `Integer.MAX_VALUE / 2`.
3. **Confusing subarray/substring (contiguous) with subsequence (not necessarily contiguous).** These require *completely different* recurrences. Always re-read the problem statement for the word "contiguous" / "subarray" / "substring" vs "subsequence".
4. **0/1 knapsack vs unbounded knapsack loop-order bug.** Iterating capacity in the wrong direction (or items in the wrong nested order) silently turns a 0/1 knapsack into an unbounded one or vice versa. Covered in detail in file 03/04.
5. **Counting "combinations" (order doesn't matter) vs "permutations" (order matters).** Swapping the outer/inner loop between "items" and "capacity" changes which one you compute. Also covered in file 04 (Coin Change II vs Combination Sum IV).
6. **Not handling `n == 0` / empty input / empty string as a base case explicitly** — many solutions crash or give wrong answers on empty input because the base case was assumed rather than coded.
7. **Negative numbers breaking "reset to 0 if negative" logic** (classic in Kadane's / max subarray) — when *all* numbers are negative, the "reset running sum to 0" trick breaks; you must explicitly track the max single element too.
8. **Recursion depth / stack overflow in Java** for large `n` (Java's default thread stack is small, unlike some competitive judges). For `n` > ~10⁴ with top-down recursion, prefer converting to bottom-up, or increase stack size by running the solve in a new `Thread` with a larger stack (`new Thread(null, runnable, "solve", 1 << 26).start()`), which is a real technique used in Java competitive programming.
9. **Multiple valid DP definitions for the same problem** (e.g., "dp[i] = answer ending at i" vs "dp[i] = best answer using first i elements") give *different* recurrences and *different* final-answer extraction (`dp[n-1]` vs `max(dp)`). Always state explicitly which one you're using before coding.
10. **Reusing a mutable memo array across multiple top-level calls** (e.g., a `static` field or unreset instance field between test cases) — leaks stale answers. Reinitialize per call.
11. **Two-string DP index confusion** — when comparing `s1[0..i)` vs `s2[0..j)`, mixing up whether `dp[i][j]` means "first i chars of s1 vs first j chars of s2" or "up to and including index i" causes subtle off-by-ones. Pick one convention (this guide uses "first i characters," 0-indexed strings, 1-indexed dp table) and never deviate mid-solution.
12. **Forgetting to handle unreachable states in path-reconstruction problems** — if you're asked to also output *which* choices were made (not just the optimal value), you need a parallel "parent"/"choice" table, and must handle the case where no path exists.

---

## 9. How This Guide Is Organized

Each subsequent file = one **pattern**. Every pattern file follows the same structure so you can build muscle memory:

1. **Pattern signature** — the "smell" that tells you this pattern applies.
2. **Recognition checklist** — bullet points to scan a problem statement against.
3. **The template** — a reusable Java skeleton for the whole pattern family.
4. **Worked example** — one problem solved from Stage 1 → Stage 4.
5. **Problem set** — LeetCode problems tagged by difficulty and by company.
6. **Edge cases & traps specific to this pattern.**
7. **Counter-questions** — interview follow-ups designed to test if you *understand* vs *memorized*.

### File map (created sequentially):
- `00_first_principles_and_framework.md` ← you are here
- `01_pattern_linear_1d_dp.md`
- `02_pattern_2d_grid_dp.md`
- `03_pattern_01_knapsack.md`
- `04_pattern_unbounded_knapsack.md`
- `05_pattern_lcs_and_sequence_dp.md`
- `06_pattern_string_and_palindrome_dp.md`
- `07_pattern_interval_dp.md`
- `08_pattern_tree_dp.md`
- `09_pattern_bitmask_dp.md`
- `10_pattern_digit_dp.md`
- `11_pattern_state_machine_stock_dp.md`
- `12_advanced_optimizations.md`
- `13_company_wise_question_bank.md`
- `14_roadmap_and_practice_plan.md`

Proceed to `01_pattern_linear_1d_dp.md` once you can answer, from memory: *"What are the 5 steps of the DP framework, and what's the difference between top-down and bottom-up?"*
