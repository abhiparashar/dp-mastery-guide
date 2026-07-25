# 05 — Pattern: LCS Family & Sequence DP (Two Strings / Subsequences)

## 1. Pattern Signature
You're comparing **two sequences** (usually two strings), or finding an optimal subsequence within **one** sequence where the recurrence needs to look back at **any** earlier position (not a fixed window like file 01). The hallmark: `dp[i][j]` where `i` indexes one string/array and `j` indexes another (or `dp[i]` where the recurrence is `dp[i] = f(dp[0], dp[1], ..., dp[i-1])` — an "all previous states" lookback, not fixed-k).

## 2. Recognition Checklist
- Two strings/arrays being compared: "longest common...", "edit distance between...", "interleaving of two strings", "shortest common supersequence".
- One sequence, but the recurrence needs an unbounded look-back: "longest increasing subsequence", "longest arithmetic subsequence".
- Distinguish subsequence (skip allowed, order preserved, NOT contiguous) from substring/subarray (must be contiguous) — re-read the problem statement every time.

## 3. The Core Template — Longest Common Subsequence (LCS)

**State:** `dp[i][j]` = length of LCS of `text1[0..i)` and `text2[0..j)` (first `i` chars of text1, first `j` chars of text2 — 1-indexed dp table over 0-indexed strings, per the convention fixed in file 00 §8.11).

**Recurrence:**
```
if text1[i-1] == text2[j-1]:  dp[i][j] = dp[i-1][j-1] + 1
else:                          dp[i][j] = max(dp[i-1][j], dp[i][j-1])
```

```java
class Solution {
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1]; // dp[0][*] = dp[*][0] = 0 — empty string base case

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }
}
```
This exact table is the **skeleton for an enormous fraction of two-string DP problems** — Edit Distance, Distinct Subsequences, Interleaving String, Shortest Common Supersequence all reuse this `dp[i][j]` grid shape with a modified recurrence.

## 4. Worked Example — Edit Distance (LeetCode 72)

**State:** `dp[i][j]` = minimum operations to convert `word1[0..i)` into `word2[0..j)`.

**Recurrence:**
```
if word1[i-1] == word2[j-1]: dp[i][j] = dp[i-1][j-1]                     // chars match, no op needed
else: dp[i][j] = 1 + min(dp[i-1][j-1],   // replace
                          dp[i-1][j],     // delete from word1
                          dp[i][j-1])     // insert into word1
```

```java
class Solution {
    public int minDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i; // converting to empty string: i deletions
        for (int j = 0; j <= n; j++) dp[0][j] = j; // building from empty string: j insertions

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[m][n];
    }
}
```
**Trap:** the base-case rows/columns (`dp[i][0] = i`, `dp[0][j] = j`) are frequently coded backward or forgotten — they represent "cost of turning a length-i string into empty" (i deletions) and vice versa. Missing these gives wrong answers only on inputs where one string is empty or very short — an easy way to fail a hidden test case.

## 5. Worked Example — Longest Increasing Subsequence (LIS) — LeetCode 300 — Single Sequence, Unbounded Lookback

**O(n²) DP version — state:** `dp[i]` = length of the LIS **ending exactly at index `i`**.

**Recurrence:** `dp[i] = 1 + max(dp[j] for all j < i where nums[j] < nums[i])`, else `dp[i] = 1` if no such `j`.

```java
class Solution {
    public int lengthOfLIS(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1); // every element is a subsequence of length 1 by itself
        int maxLen = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }
        return maxLen;
    }
}
```
**Why this ISN'T file-01-style linear DP:** `dp[i]` depends on **all** `dp[j]` for `j < i`, not a fixed lookback window — this makes it O(n²), fundamentally different in character even though it's a single array.

**O(n log n) optimization — patience sorting / binary search:** maintain a `tails` array where `tails[k]` = smallest possible tail value of an increasing subsequence of length `k+1`. For each number, binary search for its position in `tails` and replace (or append).

```java
class Solution {
    public int lengthOfLIS(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;
        for (int num : nums) {
            int lo = 0, hi = size;
            while (lo < hi) { // lower_bound: first index where tails[idx] >= num
                int mid = (lo + hi) / 2;
                if (tails[mid] < num) lo = mid + 1; else hi = mid;
            }
            tails[lo] = num;
            if (lo == size) size++;
        }
        return size;
    }
}
```
**Important nuance:** `tails` is NOT necessarily a real subsequence from the input — it's a bookkeeping array representing "best possible tail for each length seen so far." This confuses many candidates who try to read the LIS directly out of `tails`; you can't, without extra parent-pointer bookkeeping.

**Always mention both versions in an interview:** start with O(n²) DP (easy to derive, easy to explain), then say *"this can be optimized to O(n log n) using binary search over a `tails` array — patience sorting,"* and implement it if asked or if time allows. This is one of the most common "can you optimize it further?" follow-ups in FAANG interviews.

## 6. Worked Example — Distinct Subsequences (LeetCode 115) — Counting Variant

**Problem:** count how many distinct subsequences of `s` equal `t`.

**State:** `dp[i][j]` = number of ways `s[0..i)` forms `t[0..j)` as a subsequence.

**Recurrence:**
```
if s[i-1] == t[j-1]: dp[i][j] = dp[i-1][j-1] + dp[i-1][j]   // use this char of s to match, OR skip it
else:                 dp[i][j] = dp[i-1][j]                  // must skip this char of s
```

```java
class Solution {
    public int numDistinct(String s, String t) {
        int m = s.length(), n = t.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = 1; // empty t: exactly 1 way (match nothing) for any prefix of s

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = dp[i - 1][j]; // always allowed: skip s[i-1]
                if (s.charAt(i - 1) == t.charAt(j - 1)) {
                    dp[i][j] += dp[i - 1][j - 1]; // additionally allowed: match s[i-1] to t[j-1]
                }
            }
        }
        return dp[m][n];
    }
}
```
**Trap:** `dp[i][0] = 1` for **all** `i` (matching empty target has exactly one way regardless of how long the source prefix is) — forgetting this base case, or setting `dp[i][0] = i` (confusing it with Edit Distance's base case) is a very common cross-contamination bug between these two similar-looking tables.

## 7. Problem Set

### Medium
| # | Problem | Key twist |
|---|---|---|
| 1143 | Longest Common Subsequence | The base template |
| 583 | Delete Operation for Two Strings | `m + n - 2*LCS(s1,s2)` — reduces directly to LCS |
| 1092 | Shortest Common Supersequence | Build from LCS: length = `m + n - LCS`; reconstructing the actual string requires backtracking through the LCS table |
| 300 | Longest Increasing Subsequence | O(n²) → O(n log n) optimization |
| 673 | Number of Longest Increasing Subsequences | Track BOTH length and count arrays in parallel |
| 1218 | Longest Arithmetic Subsequence of Given Difference | 1D DP keyed by value via HashMap, not index |
| 1027 | Longest Arithmetic Subsequence | `dp[i][diff]` — state includes the common difference itself |
| 718 | Maximum Length of Repeated Subarray | Same table as LCS, but requires **contiguous** match, so recurrence resets to 0 on mismatch instead of taking max of neighbors — great trap to test subsequence vs subarray understanding |
| 97 | Interleaving String | `dp[i][j]` = can `s1[0..i)` + `s2[0..j)` interleave to form `s3[0..i+j)` |
| 132 / 1130 | (see file 06/07 — palindrome & interval variants) | |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 72 | Edit Distance | 3-way min recurrence |
| 115 | Distinct Subsequences | Counting variant of LCS-style table |
| 727 | Minimum Window Subsequence | LCS-adjacent but asks for a contiguous window of `s` containing `t` as subsequence — subtle two-pointer + DP hybrid |
| 1216 | Valid Palindrome III | LCS with reversed string to find min deletions for palindrome (bridges to file 06) |

## 8. Companies Known to Ask This Pattern
Google, Amazon, Meta, Microsoft, Bloomberg, Two Sigma — Edit Distance and LCS are among the **most frequently asked hard-DP** questions at Google onsite loops; LIS (with the O(n log n) follow-up) is a Meta/Amazon favorite specifically because it tests whether you can optimize past the "obvious" O(n²) solution.

## 9. Edge Cases & Traps

1. **Empty string base cases** — `dp[i][0]` and `dp[0][j]` mean different things in different problems (LCS: `0`; Edit Distance: `i`/`j`; Distinct Subsequences: `1` for `dp[i][0]`, `0` for `dp[0][j]` when `i>0`). **Never assume — re-derive the base case meaning from the problem definition every time.**
2. **Subsequence vs subarray/substring confusion** (LCS vs Maximum Length of Repeated Subarray) — the recurrence's "else" branch differs completely: LCS takes `max(dp[i-1][j], dp[i][j-1])` on mismatch (skip either side), while contiguous-match problems reset to `dp[i][j] = 0` on mismatch (a real substring cannot have gaps).
3. **LIS `tails` array misread as an actual subsequence** — it isn't; if the problem needs the *actual* LIS elements (not just length), you need auxiliary parent pointers, or fall back to the O(n²) version which is easier to reconstruct from.
4. **Strictly increasing vs non-decreasing** — always confirm whether duplicates count as "increasing" (`nums[j] < nums[i]` vs `nums[j] <= nums[i]`) — a one-character condition flip that's a classic hidden-test-case trap.
5. **Distinct Subsequences integer overflow** — counts can exceed `int` range for long strings with many repeated characters; LeetCode's constraints keep it in `int` range, but always ask about the expected magnitude in a live interview, and default to `long` if unsure.
6. **Shortest Common Supersequence reconstruction** — computing the *length* is easy (`m+n-LCS`), but building the actual *string* requires backtracking through the full LCS table (not the space-optimized version) — don't space-optimize prematurely if reconstruction is required.
7. **Interleaving String (LC 97) 3-string index confusion** — `dp[i][j]` should represent progress into `s1` and `s2` only; `s3`'s corresponding index is *derived* as `i+j`, not tracked separately — many buggy solutions try to add a third dimension unnecessarily.

## 10. Counter-Questions

1. *"LCS is O(mn) time and space. How would you reduce space to O(min(m,n))?"* → `dp[i][j]` only depends on the row above and the current row so far, so keep two 1D rolling arrays of size `min(m,n)+1` (put the shorter string on the inner loop dimension for max savings).
2. *"How would you reconstruct the actual LCS string, not just its length?"* → Keep the full table, then walk backward from `dp[m][n]`: if chars match, that char is part of the LCS, move diagonally; else move toward whichever of `dp[i-1][j]`/`dp[i][j-1]` is larger.
3. *"Prove why the O(n log n) LIS algorithm's `tails` array is always sorted."* → Each new element either extends the array (appended at the end, necessarily larger than the current max since it passed all binary-search comparisons) or replaces the first element ≥ it (making that slot's value strictly smaller, preserving order) — invariant maintained by induction.
4. *"Edit Distance only allows insert/delete/replace, each cost 1. What if replace costs 2 and insert/delete cost 1?"* → Simply change the recurrence's weights: `dp[i][j] = min(dp[i-1][j-1] + 2, dp[i-1][j] + 1, dp[i][j-1] + 1)` on mismatch — tests whether you understand the recurrence as a *weighted* graph shortest-path in disguise, not a fixed formula.
5. *"What's the relationship between Edit Distance and LCS?"* → If only insert/delete are allowed (no replace), Edit Distance reduces exactly to `m + n - 2*LCS(s1,s2)` (delete everything not in the LCS from each string) — same idea as Delete Operation for Two Strings (LC 583).

Proceed to `06_pattern_string_and_palindrome_dp.md`.
