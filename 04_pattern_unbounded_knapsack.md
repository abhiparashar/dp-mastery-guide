# 04 — Pattern: Unbounded Knapsack

## 1. Pattern Signature
Same as 0/1 knapsack, but each item can be used **unlimited times**. This single difference changes the loop order and is the single most confused pattern-pair in all of DP interviews.

## 2. Recognition Checklist
- Items (coins, rod pieces, numbers) can be **reused any number of times**.
- Phrases: "minimum number of coins to make amount", "ways to make change" (careful — see §5 for the combinations-vs-permutations split within this pattern itself), "rod cutting for max profit", "minimum perfect squares summing to n".

## 3. The Template

**State:** `dp[cap]` = best value/count/boolean achievable with capacity exactly `cap`, items reusable.

```java
class Solution {
    // Minimum coins to make amount (LeetCode 322 style)
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // sentinel "impossible" — safely larger than any real answer
        dp[0] = 0; // base case: 0 coins needed to make amount 0

        for (int cap = 1; cap <= amount; cap++) {
            for (int coin : coins) {
                if (coin <= cap) {
                    dp[cap] = Math.min(dp[cap], dp[cap - coin] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }
}
```

**THE critical loop-order distinction vs 0/1 knapsack:**
```java
// 0/1 knapsack (file 03): capacity loop INSIDE, DECREASING
for (item i)
    for (cap = capacity downTo weight[i])
        dp[cap] = combine(dp[cap], dp[cap - weight[i]] + value[i]);

// Unbounded knapsack: capacity loop INSIDE, INCREASING
for (item i)
    for (cap = weight[i] to capacity)
        dp[cap] = combine(dp[cap], dp[cap - weight[i]] + value[i]);
```
**Why increasing works here and is *supposed* to allow reuse:** when computing `dp[cap]`, `dp[cap - weight[i]]` may have *already been updated in this same pass* using item `i` — which is exactly what you want, since item `i` is allowed to be reused. This is the mirror image of the 0/1 knapsack trap in file 03 — same code shape, opposite direction, opposite semantics. **Always be able to explain this out loud in an interview; it is the #1 "do you actually understand DP or just memorized templates" test.**

## 4. Worked Example — Coin Change II (LeetCode 518) vs Combination Sum IV (LeetCode 377) — Combinations vs Permutations

Both problems: "count ways to make `target` from `nums`/`coins`, reuse allowed." **Same recurrence shape, different loop nesting order** — because one counts combinations (order doesn't matter) and the other counts permutations (order matters, `[1,2]` and `[2,1]` are different ways).

**Coin Change II — combinations (order doesn't matter):** loop **coins outer, amount inner**.
```java
class Solution {
    public int change(int amount, int[] coins) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;
        for (int coin : coins) {               // OUTER: coin
            for (int cap = coin; cap <= amount; cap++) { // INNER: amount, increasing (unbounded reuse)
                dp[cap] += dp[cap - coin];
            }
        }
        return dp[amount];
    }
}
```
Why outer-coin works for combinations: by fixing the coin in the outer loop, we only ever build sums using coins **up to and including** the current one in a fixed relative order — `[1,2]` is counted once (never also as `[2,1]`), because by the time we process coin `2`, all sums already only reflect combinations that respect "1 before 2."

**Combination Sum IV — permutations (order matters):** loop **amount outer, coins inner**.
```java
class Solution {
    public int combinationSum4(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int cap = 1; cap <= target; cap++) {   // OUTER: amount
            for (int num : nums) {                    // INNER: every number tried at every amount
                if (num <= cap) dp[cap] += dp[cap - num];
            }
        }
        return dp[target];
    }
}
```
Why outer-amount works for permutations: at every amount, we try appending **every** number to **every** shorter valid sequence, regardless of what was used before — so `[1,2]` and `[2,1]` both get separately counted as distinct sequences ending in different last elements.

**This distinction (which loop goes outside) is one of the highest-value "aha" moments in all of DP — internalize it by re-deriving both from scratch, don't just memorize which loop goes where.**

## 5. Worked Example — Perfect Squares (LeetCode 279)

Same unbounded knapsack shape as Coin Change, where the "coins" are `1, 4, 9, 16, ...` (all perfect squares ≤ n).

```java
class Solution {
    public int numSquares(int n) {
        int[] dp = new int[n + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j * j <= i; j++) {
                dp[i] = Math.min(dp[i], dp[i - j * j] + 1);
            }
        }
        return dp[n];
    }
}
```
**Note:** there's also a pure math solution (Lagrange's four-square theorem + Legendre's three-square theorem) that's O(√n), but the DP solution is what's expected in an interview unless you're explicitly asked to optimize further.

## 6. Worked Example — Rod Cutting (classic, not directly on LeetCode but a canonical teaching problem, closely related to LeetCode 322/279)

Given a rod of length `n` and prices `price[1..n]` for each piece length, maximize total revenue from cutting the rod into pieces (any piece length can be reused/repeated).

```java
class Solution {
    public int rodCutting(int[] price, int n) {
        // price[i-1] = price for a piece of length i (1-indexed conceptually)
        int[] dp = new int[n + 1];
        for (int len = 1; len <= n; len++) {
            for (int cut = 1; cut <= len; cut++) {
                dp[len] = Math.max(dp[len], price[cut - 1] + dp[len - cut]);
            }
        }
        return dp[n];
    }
}
```
This is structurally identical to Coin Change / Perfect Squares — "reuse a piece length as many times as needed to build up to `n`, maximizing value." Recognizing that rod cutting, coin change, and perfect squares are **the same pattern wearing different costumes** is exactly the kind of transfer this guide is training you to do.

## 7. Problem Set

### Easy / Medium
| # | Problem | Key twist |
|---|---|---|
| 322 | Coin Change | Min count, unbounded |
| 518 | Coin Change II | Count combinations — outer-coin loop |
| 377 | Combination Sum IV | Count permutations — outer-amount loop |
| 279 | Perfect Squares | "Coins" are perfect squares |
| 139 | Word Break | Unbounded reuse of dictionary words — but state is over string prefixes, not a numeric capacity (bridges into file 06) |
| 983 | Minimum Cost For Tickets | Unbounded-ish: buy 1/7/30-day passes, but capacity axis is "day index," not a sum — good variant to test transfer |
| 39 | Combination Sum | Backtracking + memo hybrid; unbounded reuse but must enumerate actual subsets, not just count/optimize |

### Hard
| # | Problem | Key twist |
|---|---|---|
| depends | Rod Cutting (GfG/classic) | Canonical teaching problem underlying Coin Change/Perfect Squares |
| 1449 | Form Largest Integer With Digits That Add up to Target | Unbounded knapsack where you maximize digit COUNT first, then lexicographic value — two-tier optimization |

## 8. Companies Known to Ask This Pattern
Amazon, Google, Microsoft, Bloomberg — Coin Change is one of the single most-asked DP questions across all FAANG phone screens; Coin Change II / Combination Sum IV are common as **paired follow-up questions** in the same interview to test if you understand the combinations-vs-permutations loop-order distinction in real time.

## 9. Edge Cases & Traps

1. **The loop-order confusion is THE trap of this entire pattern** — mixing up increasing/decreasing between 0/1 and unbounded knapsack, or mixing up outer/inner between combinations and permutations. When in doubt, **re-derive from Stage 1 recursion** rather than guessing which template applies from memory.
2. **Sentinel value overflow** (flagged in file 00 §8.2): `dp[cap] = dp[cap-coin] + 1` when `dp[cap-coin]` is still the "impossible" sentinel can silently produce a small-looking-but-wrong number if the sentinel isn't large enough, or overflow if it's `Integer.MAX_VALUE`. Use `amount + 1` (a safely-large-but-not-overflowing sentinel) as shown in Coin Change, not `Integer.MAX_VALUE`.
3. **`amount == 0`** should return `0` (zero coins needed) — verify your base case handles this without special-casing outside the main logic.
4. **Coin Change II with `amount == 0`**: exactly 1 way (use no coins) — `dp[0] = 1`, not `0`.
5. **Empty coins array or all coins larger than amount** — should correctly return `-1` (Coin Change) or `0` (Coin Change II), not crash.
6. **Word Break (LC 139) is unbounded-reuse in *spirit* but the "capacity" axis is a string index, not a numeric sum** — don't force it into the exact same code shape; the loop structure becomes `dp[i] = OR over all valid word endings ≤ i` (see file 06 for the full treatment). This is a good example of the pattern generalizing beyond pure numeric knapsack.

## 10. Counter-Questions

1. *"Derive, from the raw recursion, why 0/1 knapsack needs decreasing capacity iteration and unbounded needs increasing — don't just recite it."* → In 0/1, `dp[cap]` before update represents "answer using items `1..i-1`"; decreasing iteration guarantees `dp[cap-weight[i]]` hasn't been touched by item `i` yet in this pass. In unbounded, we *want* `dp[cap-weight[i]]` to possibly already include item `i` (reused), so increasing iteration is correct and intentional.
2. *"If I gave you a NEGATIVE-cost coin (an item that lets you go over target and come back for a rebate), does this template still work?"* → No — the DP invariant relies on monotonic capacity progression; negative weights break the ordering guarantee and typically require a completely different approach (e.g., Bellman-Ford-style relaxation or rethinking the state space entirely). Good test of whether you understand *why* the template works, not just that it works.
3. *"How would you modify Coin Change to also return the actual set of coins used, not just the count?"* → Keep a parent/choice array `coinUsed[cap]` storing which coin achieved the min at each capacity, then backtrack from `amount` to `0`.
4. *"What's the time complexity of Coin Change, and how does it scale if `amount` is 10^9?"* → O(amount × coins.length) — infeasible for huge amounts; at that scale you'd need number-theoretic approaches (e.g., BFS on coin residues / Chicken McNugget theorem for feasibility, or matrix exponentiation for specific structured coin sets) — tests whether you understand the complexity ceiling of straightforward DP.

Proceed to `05_pattern_lcs_and_sequence_dp.md`.
