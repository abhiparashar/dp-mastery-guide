# 13 — Company-Wise DP Question Bank

> These company associations reflect **commonly and repeatedly reported patterns** in interview experience aggregators (LeetCode Discuss, Glassdoor, Blind) over the past several years, not a guaranteed or official list from any company — question banks rotate constantly, and no source (including this one) can promise a specific question will appear in your interview. Use this to prioritize **which patterns** to master most deeply for a given company, not as a literal checklist to memorize verbatim.

---

## Google
Google's DP questions skew toward **hard, less-templated problems** that test whether you can derive a novel state design live, rather than recognize a memorized pattern. Interval DP and tree DP with the "global vs return value" subtlety are especially favored.

| Pattern file | Problems commonly reported |
|---|---|
| 02 Grid DP | Cherry Pickup, Dungeon Game, Minimum Falling Path Sum II |
| 05 LCS/Sequence | Edit Distance, Longest Increasing Subsequence (with O(n log n) follow-up) |
| 06 String/Palindrome | Palindrome Partitioning II, Scramble String |
| 07 Interval DP | Burst Balloons, Minimum Cost to Merge Stones, Remove Boxes |
| 08 Tree DP | Binary Tree Maximum Path Sum, Binary Tree Cameras |
| 09 Bitmask | Traveling Salesman variants, Shortest Path Visiting All Nodes |
| 10 Digit DP | Numbers At Most N Given Digit Set (less common, but appears in "hard" rotations) |

## Amazon
Amazon leans toward **knapsack-family and linear DP problems**, often framed around real-world resource-allocation stories (inventory, delivery routes, scheduling) even when the underlying pattern is a standard template.

| Pattern file | Problems commonly reported |
|---|---|
| 01 Linear 1D | House Robber, House Robber II, Maximum Subarray, Jump Game |
| 03 0/1 Knapsack | Partition Equal Subset Sum, Last Stone Weight II |
| 04 Unbounded Knapsack | Coin Change, Coin Change II |
| 05 LCS/Sequence | Longest Common Subsequence, Longest Increasing Subsequence |
| 11 State Machine | Best Time to Buy/Sell Stock (all variants — a very frequent Amazon family) |

## Meta (Facebook)
Meta favors **string/palindrome DP and tree DP**, plus a strong emphasis on the "can you also reconstruct the actual answer, not just its value/count" follow-up.

| Pattern file | Problems commonly reported |
|---|---|
| 06 String/Palindrome | Longest Palindromic Substring, Word Break, Word Break II |
| 07 Interval DP | Predict the Winner, Stone Game |
| 08 Tree DP | House Robber III, Diameter of Binary Tree |
| 05 LCS/Sequence | Edit Distance, Distinct Subsequences |
| 11 State Machine | Best Time to Buy/Sell Stock with Cooldown, with Transaction Fee |

## Microsoft
Microsoft's DP questions are often **medium difficulty with a strong emphasis on clean code and edge-case handling** rather than exotic patterns — a good fit for methodically working through the 5-step framework live.

| Pattern file | Problems commonly reported |
|---|---|
| 01 Linear 1D | Climbing Stairs, House Robber, Decode Ways |
| 02 Grid DP | Unique Paths, Minimum Path Sum, Maximal Square |
| 03 0/1 Knapsack | Partition Equal Subset Sum |
| 05 LCS/Sequence | Longest Common Subsequence |

## Bloomberg
Bloomberg frequently asks **knapsack and coin-change-style problems**, reflecting the financial-computation flavor of many of their systems, plus digit-DP-adjacent counting problems occasionally.

| Pattern file | Problems commonly reported |
|---|---|
| 03/04 Knapsack | Coin Change, Coin Change II, Target Sum |
| 01 Linear 1D | Maximum Subarray, House Robber |
| 05 LCS/Sequence | Edit Distance |
| 10 Digit DP | Occasionally, in "hard" rounds |

## Apple
Apple's DP questions tend toward **grid DP and linear DP**, often embedded in a slightly more applied/systems-flavored problem statement.

| Pattern file | Problems commonly reported |
|---|---|
| 02 Grid DP | Unique Paths II, Minimum Path Sum |
| 01 Linear 1D | Climbing Stairs, House Robber |
| 04 Unbounded Knapsack | Coin Change |

## Uber / Lyft
These often lean into **bitmask DP and graph-adjacent DP**, unsurprising given routing/logistics business context.

| Pattern file | Problems commonly reported |
|---|---|
| 09 Bitmask | Shortest Path Visiting All Nodes, TSP-style variants |
| 02 Grid DP | Minimum Path Sum variants |

## Two Sigma / Citadel / Jane Street (quant-adjacent)
These firms more frequently reach for **digit DP, bitmask DP, and interval DP with an optimization follow-up** (Knuth's/D&C optimization awareness), reflecting a stronger algorithms/competitive-programming bar.

| Pattern file | Problems commonly reported |
|---|---|
| 09 Bitmask | TSP, Partition to K Equal Sum Subsets |
| 10 Digit DP | Numbers At Most N Given Digit Set, Count of Integers |
| 07 Interval DP | Matrix Chain Multiplication + Knuth's optimization follow-up |
| 12 Advanced Optimizations | Frequently explicitly asked as a live follow-up |

---

## Cross-Company "Always Know Cold" List

Regardless of which company you're interviewing with, these problems are reported frequently enough across **every** major tech company that they should be considered baseline, non-negotiable preparation:

1. Climbing Stairs (LC 70)
2. House Robber (LC 198) and House Robber II (LC 213)
3. Maximum Subarray / Kadane's (LC 53)
4. Coin Change (LC 322)
5. Longest Common Subsequence (LC 1143)
6. Longest Increasing Subsequence (LC 300) — including the O(n log n) optimization
7. Edit Distance (LC 72)
8. Unique Paths (LC 62) and Minimum Path Sum (LC 64)
9. Word Break (LC 139)
10. Partition Equal Subset Sum (LC 416)
11. Best Time to Buy/Sell Stock (LC 121) and at least one multi-transaction variant (LC 122 or 309)
12. Longest Palindromic Substring (LC 5)

If you can solve all 12 of these from scratch, out loud, explaining your state/recurrence/base-case reasoning as you go (not just typing a memorized solution), you are at a solid baseline for any FAANG+ DP round.

Proceed to `14_roadmap_and_practice_plan.md`.
