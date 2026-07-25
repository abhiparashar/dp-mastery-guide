# 11 — Pattern: State Machine DP (Stock Buy/Sell Family)

## 1. Pattern Signature
You move through a sequence (usually prices over time) and at each step you're in one of a **small, fixed number of "states"** (e.g., "holding a stock" vs "not holding"), with defined transitions between states. This is DP where the state explicitly models a **finite state machine**, and the recurrence is "for each state, what's the best value achievable, considering I could have transitioned from any compatible previous state."

## 2. Recognition Checklist
- "Buy and sell stock" problems — the canonical example family (there are 6 LeetCode variants).
- The decision at each time step is naturally categorical (buy / sell / hold / cooldown / do nothing), not numeric.
- You can literally **draw a state diagram** with arrows for valid transitions — if you can draw it, you can template it.

## 3. The Universal Stock-Problem Template

**Core idea:** define one DP array/variable **per state**, updated simultaneously each day using **previous day's values** (never the just-updated value from the same day, since that would create an impossible same-day transition — this is the #1 trap in this pattern, covered in §8).

```java
class Solution {
    public int maxProfit(int[] prices) {
        int n = prices.length;
        if (n == 0) return 0;

        // States: hold = currently holding a stock; cash = currently not holding
        int hold = -prices[0]; // day 0: buy immediately, "profit" is negative the price paid
        int cash = 0;           // day 0: do nothing

        for (int i = 1; i < n; i++) {
            int prevHold = hold, prevCash = cash; // CRITICAL: snapshot before updating either
            hold = Math.max(prevHold, prevCash - prices[i]);   // keep holding, OR buy today
            cash = Math.max(prevCash, prevHold + prices[i]);    // stay in cash, OR sell today
        }
        return cash; // end in cash, never end while still holding a stock (assuming no unsold value)
    }
}
```
This exact `hold`/`cash` pair (with the snapshot-before-update trap flagged) is the atomic unit you'll extend for every variant below.

## 4. Worked Example — Best Time to Buy and Sell Stock with Cooldown (LeetCode 309)

**Extra state needed:** after selling, you must "cooldown" for one day before buying again — so `cash` alone isn't enough; you need to distinguish "just sold, in cooldown" from "free to buy."

**Three states:** `hold`, `sold` (just sold today, must cool down tomorrow), `rest` (in cash, free to buy).

```java
class Solution {
    public int maxProfit(int[] prices) {
        int n = prices.length;
        if (n == 0) return 0;

        int hold = -prices[0];
        int sold = 0;
        int rest = 0;

        for (int i = 1; i < n; i++) {
            int prevHold = hold, prevSold = sold, prevRest = rest;
            hold = Math.max(prevHold, prevRest - prices[i]); // keep holding, or buy from "rest" (NOT from "sold" — cooldown!)
            sold = prevHold + prices[i];                       // sell today (can only come from holding)
            rest = Math.max(prevRest, prevSold);                // stay resting, or cooldown finishes from yesterday's sale
        }
        return Math.max(sold, rest); // final answer: must not be holding
    }
}
```
**The key transition rule that encodes the cooldown constraint:** `hold` can only transition from `prevRest` (NOT `prevSold`) — this single edge omission is what correctly forbids buying the very next day after selling. Drawing the 3-state diagram explicitly (hold → sold → rest → hold, with rest having a self-loop and being the only state that can feed into `hold`) makes this rule visually obvious and is highly recommended to sketch out loud in an interview.

## 5. Worked Example — Best Time to Buy and Sell Stock with Transaction Fee (LeetCode 714)

Same 2-state (`hold`/`cash`) template as §3, just subtract the fee once per transaction (convention: charge it on sell):

```java
class Solution {
    public int maxProfit(int[] prices, int fee) {
        int hold = -prices[0];
        int cash = 0;
        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold, prevCash = cash;
            hold = Math.max(prevHold, prevCash - prices[i]);
            cash = Math.max(prevCash, prevHold + prices[i] - fee); // fee applied once, on the sell transition
        }
        return cash;
    }
}
```
**Note:** the fee could equally be charged on the buy transition instead (`hold = max(prevHold, prevCash - prices[i] - fee)`) — both are mathematically equivalent as long as you charge it exactly once per complete buy-sell cycle; charging it on both, or on neither, is the trap.

## 6. Worked Example — Best Time to Buy and Sell Stock IV (LeetCode 188) — At Most `k` Transactions

**Extra state dimension:** how many transactions have been used so far, `dp[i][k][hold/cash]`.

```java
class Solution {
    public int maxProfit(int k, int[] prices) {
        int n = prices.length;
        if (n == 0 || k == 0) return 0;

        // Optimization: if k >= n/2, unlimited transactions are effectively allowed —
        // reduces to LeetCode 122 (Best Time to Buy/Sell II), avoiding a huge k dimension
        if (k >= n / 2) {
            int profit = 0;
            for (int i = 1; i < n; i++) {
                if (prices[i] > prices[i - 1]) profit += prices[i] - prices[i - 1];
            }
            return profit;
        }

        int[][] hold = new int[n][k + 1];
        int[][] cash = new int[n][k + 1];
        for (int t = 0; t <= k; t++) hold[0][t] = -prices[0];

        for (int i = 1; i < n; i++) {
            for (int t = 1; t <= k; t++) {
                hold[i][t] = Math.max(hold[i - 1][t], cash[i - 1][t] - prices[i]);
                cash[i][t] = Math.max(cash[i - 1][t], hold[i - 1][t - 1] + prices[i]); // completing a transaction consumes one "t"
            }
        }
        return cash[n - 1][k];
    }
}
```
**Trap:** the transaction count `t` should be decremented (consumed) either on **buy** or on **sell**, but pick exactly one convention consistently — here it's consumed on **sell** (`hold[i-1][t-1]` feeding into `cash[i][t]`), meaning a transaction = one full buy-sell cycle counted at completion.

## 7. Problem Set — The Full Stock Family

| # | Problem | State machine complexity |
|---|---|---|
| 121 | Best Time to Buy/Sell Stock (1 transaction) | Simplest — just track min-price-so-far and max-profit-so-far (2 running variables, no full state machine needed, though it CAN be framed as `hold`/`cash` with `k=1`) |
| 122 | Best Time to Buy/Sell Stock II (unlimited transactions) | Base `hold`/`cash` template from §3 |
| 123 | Best Time to Buy/Sell Stock III (at most 2 transactions) | Special case of LC 188 with `k=2` — 4 explicit states (`buy1, sell1, buy2, sell2`) is a common alternative framing |
| 188 | Best Time to Buy/Sell Stock IV (at most k transactions) | Full `dp[i][k][hold/cash]`, §6 |
| 309 | Best Time to Buy/Sell Stock with Cooldown | 3-state machine, §4 |
| 714 | Best Time to Buy/Sell Stock with Transaction Fee | Base template + fee on one transition, §5 |

## 8. Companies Known to Ask This Pattern
Amazon, Meta, Google, Microsoft, Bloomberg — this is one of the **highest-frequency DP families across all of FAANG+**, precisely because there are 6 closely related variants that let interviewers test incremental complexity within a single 45-minute interview (start with LC 121, escalate to 122, then 309 or 714 as a follow-up).

## 9. Edge Cases & Traps

1. **Same-day update-order bug (THE defining trap of this pattern):** updating `cash` using the *already-updated* `hold` from the same iteration (instead of the previous day's `hold`) creates an impossible same-day buy-then-sell chain. **Always snapshot previous values into temp variables before computing new ones** — shown explicitly in every template above; never skip this step even though it looks redundant for simple 2-state cases.
2. **Empty price array / single price** — `prices.length == 0` or `1` should return `0` profit; guard explicitly before accessing `prices[0]`.
3. **Cooldown's transition restriction** (`hold` can't come from `sold`, only from `rest`) — omitting this "missing edge" silently allows buying the day immediately after selling, violating the cooldown constraint while still producing plausible-looking (but wrong) answers on many test cases.
4. **Transaction fee charged twice (or never)** — must be applied exactly once per complete cycle; pick buy-side or sell-side consistently.
5. **LC 188's `k >= n/2` optimization being skipped** — without it, a huge `k` (e.g., `k = 10^9` conceptually, though LeetCode bounds it, or just `k` close to `n`) creates a huge and wasteful DP table; recognizing "when k is large enough, this degenerates to the unlimited-transactions problem" is an important complexity-awareness signal in an interview.
6. **Off-by-one in "which day is the transaction consumed on"** (LC 188/123) — consuming the transaction counter on buy vs. sell changes the indexing of `hold[i-1][t]` vs `hold[i-1][t-1]`; pick one and verify it against a tiny manual example (e.g., `k=1`, 2 prices) before trusting the general table.
7. **LC 123 (at most 2 transactions) implemented as 4 named variables (`buy1, sell1, buy2, sell2`) instead of the generalized k-loop** — both are valid and this specific problem is small enough that the explicit-4-variable version is arguably clearer and faster to write live; know both framings and choose based on what's asked.

## 10. Counter-Questions

1. *"Why must you snapshot `prevHold`/`prevCash` before updating either state, even in the simplest 2-state template?"* → Without the snapshot, `cash`'s update might read the just-updated `hold` (same day), effectively allowing a buy and sell on the same day using tomorrow's already-known price — an impossible/incorrect transaction sequence. This tests whether you understand *why* the "day" boundary in an FSM DP is a hard synchronization barrier, not an implementation nicety.
2. *"How would you handle stock problems where you must also pay a cooldown of `c` days (not just 1), generalized?"* → Extend the "cooldown" state into `c` sub-states representing "days remaining in cooldown," or track "the day you last sold" explicitly and gate the buy transition on `currentDay - lastSellDay > c`.
3. *"Can you solve LC 121 (single transaction) using the general `hold`/`cash` template from LC 122, and does it give the same answer?"* → Yes — running the unlimited-transaction template with an implicit cap of `k=1` (either via the full LC 188 machinery with `k=1`, or by reasoning that with a single allowed transaction the running-min/running-max-profit approach is a simplified special case) produces the same result; recognizing this hierarchy (121 ⊂ 188 with k=1, 122 = 188 with k=∞) shows you see these as one family, not six memorized templates.
4. *"What's the time/space complexity of LC 188, and how would you reduce space?"* → O(n·k) time and space for the 2D table version; since `hold[i][*]` and `cash[i][*]` only depend on day `i-1`'s values, you can roll this down to O(k) space using two 1D arrays updated in place (iterate `t` in a direction that doesn't overwrite values needed later in the same day's pass — worth reasoning through carefully, similar in spirit to the knapsack loop-direction traps in files 03/04).

Proceed to `12_advanced_optimizations.md`.
