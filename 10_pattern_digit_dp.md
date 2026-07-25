# 10 — Pattern: Digit DP

## 1. Pattern Signature
The problem asks you to **count or find numbers in a range `[0, N]` (or `[L, R]`) satisfying some digit-level property** — and `N` can be astronomically large (up to 10^9, 10^18, or even given as a string), so you **cannot iterate through every number**. Instead, you build numbers **digit by digit** and use DP over the "position in the number + some running constraint state."

## 2. Recognition Checklist
- Range bound `N` is huge (too large to iterate one-by-one: `10^9` or more), or given directly as a string of digits.
- Property depends on the **digits themselves**, not the number's magnitude directly: "no two adjacent digits are the same", "digit sum equals X", "contains no digit 4", "at most k distinct digits".
- Phrases: "count numbers ≤ N with property P", "numbers at most N given a digit set".

## 3. The Template

**Convert `N` to its digit array** (as a string or `char[]`), then recurse position by position with this state:

- `pos`: current digit position being decided (0-indexed from the most significant digit).
- `tight`: boolean — are we still bound by `N`'s actual digits at this position (`true`), or have we already placed a strictly smaller digit earlier (`false`, meaning we're now free to place any digit 0-9)?
- `started`: boolean (often needed) — have we placed a nonzero digit yet, to correctly handle leading zeros?
- Problem-specific extra state: running digit sum, last digit placed, count of distinct digits used, etc.

```java
class Solution {
    String num;
    Integer[][] memo; // memo[pos][extraState] — tight is NOT memoized (see trap below)

    public int countNumbers(String n /* digits of N */) {
        num = n;
        memo = new Integer[num.length()][/* size of extra state dimension */ 10];
        return dp(0, /* initial extra state */ 0, true, false);
    }

    private int dp(int pos, int extraState, boolean tight, boolean started) {
        if (pos == num.length()) {
            return started ? 1 : 0; // or apply final validity check on extraState
        }

        // Only memoize when NOT tight and NOT still in leading-zero mode —
        // tight/started paths are "unique" per call and shouldn't share a cache slot
        if (!tight && started && memo[pos][extraState] != null) {
            return memo[pos][extraState];
        }

        int limit = tight ? (num.charAt(pos) - '0') : 9;
        int count = 0;

        for (int digit = 0; digit <= limit; digit++) {
            boolean newTight = tight && (digit == limit);
            boolean newStarted = started || digit != 0;
            int newExtraState = /* update extraState based on digit, e.g. digit sum, last digit, etc. */ extraState;

            count += dp(pos + 1, newExtraState, newTight, newStarted);
        }

        if (!tight && started) memo[pos][extraState] = count;
        return count;
    }
}
```

**Why `tight` cannot be blindly memoized:** when `tight == true`, the set of allowed digits at this position is restricted by `N`'s actual digit (`limit = num.charAt(pos) - '0'` instead of `9`) — this makes the subproblem's answer genuinely different from the "free" (`tight == false`) version, even for the same `pos` and `extraState`. Memoizing across both would silently conflate two different subproblems. The standard fix: only cache when `tight == false` (and typically also `started == true`), since the "free" case is where subproblems actually repeat across different digit prefixes.

## 4. Worked Example — Count Numbers With Unique Digits (LeetCode 357)

Actually more naturally solved with direct combinatorics rather than full digit-DP machinery, but it's the standard "gentle intro" problem for this pattern family:

```java
class Solution {
    public int countNumbersWithUniqueDigits(int n) {
        if (n == 0) return 1;
        int total = 10; // covers all 1-digit numbers (0-9), including 0 itself
        int uniqueDigits = 9, availableDigits = 9;
        for (int i = 2; i <= n && availableDigits > 0; i++) {
            uniqueDigits *= availableDigits; // multiply by shrinking choices for each new digit position
            total += uniqueDigits;
            availableDigits--;
        }
        return total;
    }
}
```
This isn't full digit-DP (no `tight`/`started` state) because the constraint ("all digits unique") is symmetric across positions and doesn't depend on comparing against a specific bound `N` — a good example of recognizing when a *simpler* combinatorial approach beats the general digit-DP template.

## 5. Worked Example — Numbers At Most N Given Digit Set (LeetCode 902) — Full Digit DP

**Problem:** given a sorted set of allowed digits, count how many positive integers ≤ `N` can be formed using only those digits (repetition allowed).

```java
class Solution {
    public int atMostNGivenDigitSet(String[] digits, int n) {
        String num = Integer.toString(n);
        int len = num.length();
        int total = 0;

        // Case 1: numbers with FEWER digits than N — always valid, pure combinatorics
        for (int L = 1; L < len; L++) {
            total += Math.pow(digits.length, L);
        }

        // Case 2: numbers with EXACTLY the same number of digits as N — digit DP with tight constraint
        for (int i = 0; i < len; i++) {
            boolean matched = false;
            char curDigit = num.charAt(i);
            for (String d : digits) {
                if (d.charAt(0) < curDigit) {
                    total += Math.pow(digits.length, len - i - 1); // free choice for all remaining positions
                } else if (d.charAt(0) == curDigit) {
                    matched = true;
                }
            }
            if (!matched) return total; // no allowed digit matches this position exactly — can't continue the tight path
        }
        total += 1; // N itself is achievable (all digits matched exactly through the whole tight path)
        return total;
    }
}
```
**Lesson:** this solution splits into "definitely-shorter numbers" (trivial combinatorics) + "same-length, tight-constrained numbers" (the real digit-DP part) — a very common structural split in digit-DP problems, worth explicitly separating in your solution narrative during an interview.

## 6. Problem Set

### Medium
| # | Problem | Key twist |
|---|---|---|
| 357 | Count Numbers With Unique Digits | Simpler combinatorial variant — good intro |
| 902 | Numbers At Most N Given Digit Set | Full `tight` constraint template, split by length |
| 233 | Number of Digit One | Count occurrences of digit '1' across all numbers ≤ n — extra state tracks count of '1's seen |
| 1067 | Digit Count in Range | Generalizes 233 to arbitrary digit `d` and a range `[low, high]` via `f(high) - f(low-1)` |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 1397 | Find All Good Strings | Digit DP combined with KMP automaton state (avoid a forbidden substring pattern) — a genuinely advanced fusion of two techniques |
| 2376 | Count Special Integers | Digit DP + bitmask extra state (track which digits have been used, to enforce "all digits distinct") |
| 233/1067 revisited at scale | — | Extending to 64-bit ranges tests overflow handling |

## 7. Companies Known to Ask This Pattern
Google, Bloomberg, Two Sigma, Citadel — Digit DP is a rarer but recurring "advanced" interview topic, more common at companies with a quant/algorithmic bent, or as a Google "hard" onsite question when the interviewer wants to test genuinely novel state-design skills beyond memorized templates (since digit DP has far less "template recognition" carryover from common practice problems than other patterns).

## 8. Edge Cases & Traps

1. **The `tight` memoization trap (already emphasized in §3)** — this is THE defining bug of this entire pattern; if you memoize across different `tight` values, you'll get silently wrong answers on some inputs while passing others, which is especially dangerous because it *looks* correct on quick manual tests.
2. **Leading zeros not handled (`started` flag)** — without tracking whether a nonzero digit has been placed yet, numbers like `007` get miscounted as 3-digit numbers instead of correctly being treated as the 1-digit number `7`. This matters heavily for problems where the *number of digits* affects the extra state (e.g., digit sum, count of a specific digit).
3. **Range queries `[L, R]` instead of `[0, N]`** — compute as `f(R) - f(L-1)` where `f(x)` = count in `[0, x]`; be careful with `L = 0` (then `L - 1 = -1` needs a defined `f(-1) = 0` base case) and with very large numbers where `L-1` requires careful string-based decrement rather than naive integer subtraction if `L` is given as a string.
4. **`N` given as a string with up to 10^15+ digits worth of magnitude (or even the number of digits itself being huge)** — always confirm whether `N` fits in a `long` or must be handled as a string throughout; digit DP naturally handles both since it never actually parses `N` as a numeric value beyond individual digit characters.
5. **Combinatorial "shorter numbers" pre-pass forgetting to exclude leading zero cases** when digits allowed include `0` — e.g., in LC 902, if `0` were an allowed digit, "numbers with fewer digits" combinatorics would need adjustment to not count numbers with leading zeros as if they were valid shorter numbers (LC 902 sidesteps this since `0` is guaranteed not in the input digit set, but it's an important trap to flag if solving a generalized variant).
6. **Extra-state dimension size explosion** — if the extra state is something like "digit sum so far," it can range up to `9 × (number of digits)`, which is manageable, but combining multiple extra states (e.g., digit sum AND last digit AND count of a specific digit) can blow up the memo table size — always compute the state space size explicitly before committing to an approach.

## 9. Counter-Questions

1. *"Why can't you just memoize digit-DP purely on `pos`, ignoring `tight` and `started` entirely?"* → Because the set of valid digit choices (and hence the count of valid completions) genuinely differs between a `tight` path (bounded by N's actual digits) and a `free` path (any digit 0-9) — collapsing them into the same cache slot conflates two different subproblems with different answers.
2. *"How would you extend the template to count numbers in `[L, R]` instead of `[0, N]`?"* → Compute `f(R) - f(L - 1)` using the same `f(x) = count in [0, x]` digit-DP function twice, handling the `L - 1` edge case (especially if `L` is given as a string and could be `0`).
3. *"What if the property depended on the number's digits appearing in a specific ORDER (e.g., digits must be non-decreasing), not just counts?"* → The extra state would need to track "the last digit placed" (to enforce `digit >= lastDigit` going forward), rather than an aggregate like a sum or count — a good test of whether you understand that the "extra state" slot is flexible and problem-specific, not a fixed formula.
4. *"At what point would digit DP become infeasible, and what would you use instead?"* → If the extra state space itself becomes exponential (e.g., tracking an unbounded subset of digits used, or a complex automaton state with many nodes), you might need to combine digit DP with automaton-based techniques (e.g., building a DFA for the forbidden-pattern constraint, as in "Find All Good Strings") or accept a higher-complexity but still polynomial hybrid solution.

Proceed to `11_pattern_state_machine_stock_dp.md`.
