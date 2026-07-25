# 06 — Pattern: String DP & Palindrome DP

## 1. Pattern Signature
Single string, but the recurrence needs to reason about **ranges/substrings** `s[i..j]`, OR needs to check "can this prefix be decomposed into valid pieces" (Word Break style). Palindrome problems are a major, recurring sub-family here because "is `s[i..j]` a palindrome" is itself a small DP table you'll reuse constantly.

## 2. Recognition Checklist
- Palindrome-related: "longest palindromic substring/subsequence", "minimum cuts to partition into palindromes", "valid palindrome after k deletions".
- Decomposition-related: "word break", "can this string be segmented into dictionary words", "scramble string".
- State is often `dp[i][j]` representing a **substring range** `s[i..j]`, not two different strings (contrast with file 05).

## 3. The Foundational Sub-Table — "Is s[i..j] a Palindrome?"

This 2D boolean table underlies nearly every palindrome problem. Build it once, reuse everywhere.

**Recurrence:** `isPal[i][j] = (s[i] == s[j]) && (j - i < 2 || isPal[i+1][j-1])`

**Critical iteration order:** you need `isPal[i+1][j-1]` (a *smaller* range) before `isPal[i][j]` (a *larger* range) — so iterate by **increasing substring length**, not by increasing `i`. This "iterate by length" order is the single biggest structural difference from the grid/knapsack patterns you've seen so far, and it reappears in file 07 (Interval DP) as the defining feature of that entire pattern family.

```java
boolean[][] isPal = new boolean[n][n];
for (int len = 1; len <= n; len++) {
    for (int i = 0; i + len - 1 < n; i++) {
        int j = i + len - 1;
        if (len == 1) isPal[i][j] = true;
        else if (len == 2) isPal[i][j] = s.charAt(i) == s.charAt(j);
        else isPal[i][j] = s.charAt(i) == s.charAt(j) && isPal[i + 1][j - 1];
    }
}
```

## 4. Worked Example — Longest Palindromic Substring (LeetCode 5)

Using the table above, track the max-length window where `isPal[i][j]` is true.

```java
class Solution {
    public String longestPalindrome(String s) {
        int n = s.length();
        if (n == 0) return "";
        boolean[][] isPal = new boolean[n][n];
        int start = 0, maxLen = 1;

        for (int len = 1; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                if (s.charAt(i) == s.charAt(j) && (len <= 2 || isPal[i + 1][j - 1])) {
                    isPal[i][j] = true;
                    if (len > maxLen) { maxLen = len; start = i; }
                }
            }
        }
        return s.substring(start, start + maxLen);
    }
}
```
**Complexity:** O(n²) time and space. **Alternative worth mentioning in interviews:** "expand around center" achieves the same O(n²) time with O(1) space (2n-1 centers, expand outward) — a good follow-up to bring up proactively, since interviewers often ask "can you avoid the O(n²) space?"

## 5. Worked Example — Longest Palindromic Subsequence (LeetCode 516) — Subsequence, Not Substring

**Key distinction from §4:** subsequence allows skipping characters (not contiguous) — this reframes it as an **LCS of `s` and `reverse(s)`** (bridges directly to file 05), OR a direct interval-style recurrence:

**Recurrence:** `dp[i][j] = dp[i+1][j-1] + 2` if `s[i] == s[j]`, else `max(dp[i+1][j], dp[i][j-1])`.

```java
class Solution {
    public int longestPalindromeSubseq(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int i = n - 1; i >= 0; i--) {
            dp[i][i] = 1; // single character is always a palindrome of length 1
            for (int j = i + 1; j < n; j++) {
                if (s.charAt(i) == s.charAt(j)) {
                    dp[i][j] = dp[i + 1][j - 1] + 2;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[0][n - 1];
    }
}
```
**Note the iteration direction here:** `i` decreasing, `j` increasing from `i+1` — this achieves the same "smaller ranges before larger ranges" dependency ordering as the length-based loop in §3, just written differently. Both are valid; use whichever you find easier to reason about, but be able to justify the ordering either way.

## 6. Worked Example — Palindrome Partitioning II (LeetCode 132) — Minimum Cuts

**Problem:** minimum cuts needed to partition `s` so every piece is a palindrome.

**Step 1:** precompute the `isPal[i][j]` table from §3.
**Step 2:** `cuts[i]` = min cuts needed for `s[0..i]` (prefix ending at index `i`).

**Recurrence:** `cuts[i] = min over all j <= i where isPal[j][i] of (j == 0 ? 0 : cuts[j-1] + 1)`.

```java
class Solution {
    public int minCut(String s) {
        int n = s.length();
        boolean[][] isPal = new boolean[n][n];
        for (int len = 1; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                isPal[i][j] = s.charAt(i) == s.charAt(j) && (len <= 2 || isPal[i + 1][j - 1]);
            }
        }

        int[] cuts = new int[n];
        for (int i = 0; i < n; i++) {
            if (isPal[0][i]) { cuts[i] = 0; continue; } // whole prefix is already a palindrome: 0 cuts
            cuts[i] = i; // worst case: cut before every single character
            for (int j = 1; j <= i; j++) {
                if (isPal[j][i]) {
                    cuts[i] = Math.min(cuts[i], cuts[j - 1] + 1);
                }
            }
        }
        return cuts[n - 1];
    }
}
```
**Lesson:** this combines TWO DP tables — a helper table (`isPal`) built first, then a main 1D DP (`cuts`) that queries the helper. Recognizing when to build an auxiliary DP table as a "lookup service" for the main recurrence is an important structural skill, and it appears again in Word Break (below) and heavily in Interval DP (file 07).

## 7. Worked Example — Word Break (LeetCode 139)

**State:** `dp[i]` = can `s[0..i)` be segmented into dictionary words?

**Recurrence:** `dp[i] = OR over all j < i where dp[j] is true AND s[j..i) is in the dictionary`.

```java
class Solution {
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict); // O(1) lookup — critical for performance
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true; // empty prefix is trivially "breakable"

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && dict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break; // found one valid decomposition — no need to check further j for this i
                }
            }
        }
        return dp[n];
    }
}
```
**Trap:** using `List.contains()` instead of a `HashSet` turns dictionary lookups into O(k) instead of O(1), silently degrading the whole algorithm from O(n²) to O(n² × k) — a very common performance bug that passes small test cases but times out on large ones.

**Word Break II (LeetCode 140)** asks for *all* valid segmentations (not just feasibility) — this needs backtracking with memoization on the **remaining suffix string** (memoize `Map<String, List<String>>` of "all ways to break this suffix"), since you must build actual sentence strings, not just a boolean/count.

## 8. Problem Set

### Medium
| # | Problem | Key twist |
|---|---|---|
| 5 | Longest Palindromic Substring | Foundational `isPal[i][j]` table |
| 647 | Palindromic Substrings (count) | Same table, count `true` entries instead of tracking max length |
| 516 | Longest Palindromic Subsequence | Subsequence version — allows skipping |
| 139 | Word Break | 1D DP with dictionary-membership recurrence |
| 91 | Decode Ways (revisit from file 01) | Same "segmentation" flavor as Word Break, but fixed alphabet (digits) instead of a dictionary |
| 1745 | Palindrome Partitioning IV | Boolean variant of LC 132: can `s` be split into exactly 3 palindromes? |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 132 | Palindrome Partitioning II | Min cuts; combines helper table + main DP |
| 140 | Word Break II | Must enumerate all segmentations — backtracking + memo on suffix strings |
| 87 | Scramble String | `dp[i][j][len]` or memoized recursion over (start1, start2, length) — a genuinely hard 3-parameter state space |
| 336 | Palindrome Pairs | Not pure DP — trie/hashmap based, but commonly grouped with palindrome problems; good to know it's an exception |
| 1278 | Palindrome Partitioning III | Change min characters (not cut count) so each partition is a palindrome — combines edit-distance-style "cost to make palindrome" with partition DP |

## 9. Companies Known to Ask This Pattern
Amazon, Google, Meta, Microsoft, Bloomberg — Longest Palindromic Substring is an extremely common **easy-medium warm-up**; Word Break and Palindrome Partitioning II are common **medium-hard** questions at Google and Meta onsite loops, often as a two-part question (feasibility, then "now return all ways" / "now return min cuts").

## 10. Edge Cases & Traps

1. **Off-by-one in the `isPal` table for length-2 substrings** — `isPal[i+1][j-1]` is only valid when `j-1 >= i+1`, i.e., `len >= 3`; for `len == 2` you must special-case to just `s[i] == s[j]` (there's no inner range to check). Forgetting this special case causes array-index-out-of-bounds or incorrect results for 2-character palindromes.
2. **Empty string input** — `longestPalindrome("")` should return `""`, not crash; always guard `n == 0` before touching the table.
3. **Word Break dictionary as a `List` instead of `HashSet`** — silent O(n) performance degradation per lookup (see §7 trap).
4. **Word Break: forgetting `dp[0] = true`** — the empty prefix must be considered "already broken," or no recursion/iteration can ever bootstrap from index 0.
5. **Confusing "min cuts" (LC 132, answer = cuts, so a single palindrome needs 0 cuts) with "min partitions" (would be cuts + 1)** — always double check what unit the problem wants returned.
6. **Palindrome Partitioning II's O(n²) helper table becoming O(n³) if you recompute `isPal` naively inside the cuts loop** instead of precomputing it once — always separate "build the helper table" from "use the helper table" as two clearly distinct passes.
7. **Scramble String's exponential blowup without memoization** — the naive recursion branches into two different split configurations at every possible split point for every pair of substrings; without a memo table keyed on `(i, j, len)` or the actual substrings, this times out badly even on medium-length strings.

## 11. Counter-Questions

1. *"How would you find the longest palindromic substring in O(n) time?"* → Manacher's Algorithm — worth **naming** even if you don't derive it fully live, to show awareness that O(n²) isn't the theoretical floor.
2. *"What's the difference in recurrence between 'longest palindromic subsequence' and 'minimum deletions to make a string a palindrome'?"* → They're complementary: `minDeletions = n - longestPalindromicSubsequence(s)` — recognizing when two different-sounding problems are algebraic complements of each other is a high-value skill.
3. *"Can Word Break be solved top-down with memoization instead of bottom-up? What would the state be?"* → Yes — memoize on the starting index `i` (state = "can suffix starting at i be broken"), recursing forward and trying every dictionary word as a prefix of the remaining suffix; same complexity, often more intuitive to derive first.
4. *"In Palindrome Partitioning II, why do we iterate cuts[i] using cuts[j-1] and not cuts[j]?"* → Because `isPal[j][i]` describes the palindrome piece **starting at `j`**, so everything **before** `j` (i.e., `s[0..j)`, ending at index `j-1`) must already be optimally cut — hence `cuts[j-1]`, not `cuts[j]`.
5. *"How would you count the total number of palindromic substrings instead of just the longest?"* → Reuse the exact same `isPal[i][j]` table, just increment a counter every time `isPal[i][j]` is true, instead of tracking a running max (this is literally LeetCode 647).

Proceed to `07_pattern_interval_dp.md`.
