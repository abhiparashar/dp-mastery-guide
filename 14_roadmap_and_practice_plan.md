# 14 — Roadmap & Practice Plan

## 1. How To Actually Use These 15 Files

Reading is not practicing. For every problem you attempt, follow this exact loop — it is the same 5-step framework from file 00, applied as a **workflow**, not just theory:

1. **Read the problem. Close the editor. Talk through the 5 steps out loud (or on paper) before writing any code:** brute force → state → recurrence → base case → iteration order.
2. **Code the top-down memoized version first.** It's the fastest path to a correct solution.
3. **Convert to bottom-up.** Say out loud why the iteration order you chose is valid (what must be computed before what).
4. **Ask yourself the pattern file's "counter-questions" section** as if an interviewer asked them. If you can't answer confidently, re-read that section.
5. **Attempt the space optimization**, even if not required, as a habit-building exercise.
6. **Log the problem** (see tracking sheet, §4) with which pattern it belongs to and which trap (if any) you fell into.

---

## 2. Suggested 8-Week Study Order

This order is deliberately sequenced so each week's pattern reuses a mental tool from the week before, building compounding intuition rather than 15 disconnected topics.

| Week | Files | Focus |
|---|---|---|
| 1 | 00, 01 | Framework internalization + linear 1D DP. Goal: 5-step framework becomes automatic. |
| 2 | 02, 03 | Grid DP + 0/1 Knapsack. Goal: comfortable with 2D state tables and the decreasing-loop trap. |
| 3 | 04 | Unbounded Knapsack, deeply. Goal: can derive the combinations-vs-permutations loop order from scratch every time, not from memory. |
| 4 | 05 | LCS family. Goal: comfortable with two-string DP tables and the "all previous states" lookback (LIS). |
| 5 | 06, 07 | String/Palindrome DP + Interval DP. Goal: internalize "iterate by increasing length," and the "think about what happens LAST" reframing trick. |
| 6 | 08 | Tree DP. Goal: never again conflate "global answer" with "return value" in recursive tree problems. |
| 7 | 09, 10 | Bitmask DP + Digit DP. Goal: recognize `n <= 20` and "huge N, count property" constraint signals instantly. |
| 8 | 11, 12, 13 | State machine DP + optimizations + company-targeted practice. Goal: full-speed pattern recognition across everything, plus fluency in "can you optimize further" follow-ups. |

**After week 8:** cycle back through the "Always Know Cold" list in file 13 once more, timing yourself (aim for under 20-25 minutes per medium problem, including talking through the framework out loud).

---

## 3. How To Practice Pattern *Recognition* Specifically (Not Just Solving)

A common failure mode: you can solve a problem once you're told it's "a knapsack problem," but you freeze on an unlabeled problem in a real interview. To train recognition specifically:

- Take a random mix of 10-15 problems from across files 01-11 (shuffle them — don't practice pattern-by-pattern once you've done each file's problem set once).
- For each, spend **at most 3 minutes** deciding: which pattern file does this belong to, and why? Write down the specific "signature" phrase or constraint that tipped you off (per each file's §2 Recognition Checklist).
- Only after committing to a pattern guess, start solving. If your guess was wrong, note *what* in the problem statement should have redirected you, and add it to your own personal "trap list."

This "guess the pattern in under 3 minutes" drill is the single highest-leverage practice exercise for interview readiness, because real interviews never tell you which pattern to use.

---

## 4. Problem Tracking Sheet (Recreate This In A Spreadsheet Or Notion)

| Column | Purpose |
|---|---|
| Problem name + LC # | Identification |
| Pattern file | Which of files 01-11 it belongs to |
| Attempt 1 date/result | Solved unaided / solved with hints / couldn't solve |
| Trap encountered | Which specific bug from that file's §9/§10 "Edge Cases & Traps" you fell into, if any |
| Counter-question answered? | Could you answer that file's counter-questions after solving? |
| Re-attempt date (spaced repetition) | Revisit after 3 days, then 2 weeks, then 6 weeks if you struggled initially |

**Spaced repetition matters more than volume for DP specifically**, because the patterns are few (roughly 11 in this guide) but each has several non-obvious traps — repetition against the *same* trap in different problem dressings is what converts "I solved this once" into "I recognize this pattern instantly."

---

## 5. Full LeetCode Practice List By Pattern (Consolidated Index)

This consolidates every problem referenced across files 01-11 into one master checklist, organized by file, for easy reference without re-opening every file.

**01 — Linear 1D:** 70, 746, 198, 213, 91, 152, 53, 55, 45, 740, 1218, 256, 265, 887

**02 — Grid DP:** 62, 63, 64, 120, 221, 1277, 931, 1594, 1301, 1463, 174, 741, 1289

**03 — 0/1 Knapsack:** 416, 494, 1049, 474, 879, 956, 1155

**04 — Unbounded Knapsack:** 322, 518, 377, 279, 139, 983, 39

**05 — LCS/Sequence:** 1143, 583, 1092, 300, 673, 1218, 1027, 718, 97, 72, 115, 727, 1216

**06 — String/Palindrome:** 5, 647, 516, 139, 91, 1745, 132, 140, 87, 1278

**07 — Interval DP:** 486, 877, 1130, 312, 1547, 1000, 546, 664

**08 — Tree DP:** 543, 337, 1372, 979, 1339, 124, 968, 1245, 2246

**09 — Bitmask DP:** 698, 847, 1723, 526, 943, 1349, 1595

**10 — Digit DP:** 357, 902, 233, 1067, 1397, 2376

**11 — State Machine (Stock):** 121, 122, 123, 188, 309, 714

**Total: ~95 distinct problems.** Solving all of these — with the 5-step-framework talk-aloud discipline from §1 — genuinely covers the overwhelming majority of DP questions you will encounter in FAANG+ interviews, including problems you've never seen before, because you'll be pattern-matching against principles rather than memorized solutions.

---

## 6. Final Self-Check Before Calling Yourself "Interview Ready"

You should be able to, without looking anything up:

1. State the 5-step framework and apply it live to a brand-new medium-difficulty problem within 5 minutes of reading it.
2. Explain, from first principles (not memory), why 0/1 knapsack's space-optimized loop goes backward and unbounded knapsack's goes forward.
3. Explain the difference between "combinations" and "permutations" counting and which loop order produces each.
4. Spot, within the first two sentences of a new problem, at least a rough guess at which of the 11 patterns applies — and articulate *why*, using the specific recognition-checklist language from the relevant file.
5. Convert any top-down memoized solution to bottom-up tabulation, and explain the correct iteration order out loud, for any of the 11 patterns.
6. Name (even without deriving) all six optimization techniques from file 12 and roughly when each applies.

If any of these six feel shaky, that's your signal for exactly which file to revisit — not a signal to grind more random unrelated problems.

---

This completes the guide: 15 files, 00 through 14, covering DP from first principles through advanced optimizations, with Java templates, recognition heuristics, worked examples, ~95 tagged LeetCode problems, company associations, and a structured practice plan. Good luck.
