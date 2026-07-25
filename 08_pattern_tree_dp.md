# 08 — Pattern: Tree DP

## 1. Pattern Signature
Input is a **tree** (usually binary, sometimes general n-ary). The DP state lives at each **node**, and the recurrence combines results from a node's **children** via a post-order traversal (compute children first, then combine at the parent). This is DP over a recursive/hierarchical structure instead of a linear index or 2D grid.

## 2. Recognition Checklist
- Explicit tree structure (`TreeNode` with `left`/`right`, or a general graph that's stated to be a tree/forest — no cycles).
- Asked for something that requires combining child subtree results: "maximum path sum", "can you select nodes without selecting adjacent ones", "minimum cameras to cover all nodes", "diameter".
- The natural traversal order is **post-order** (children before parent) because the parent's answer needs the children's answers.

## 3. The Template

**State:** define a recursive function that returns **one or more values summarizing the subtree rooted at this node** — often a small fixed-size tuple, since Java doesn't have native multi-return, use a small helper class or `int[]`.

```java
class Solution {
    // Common shape: return {includingThisNode, excludingThisNode} or similar paired values
    private int[] dp(TreeNode node) {
        if (node == null) return new int[]{0, 0}; // base case: empty subtree contributes 0 either way

        int[] left = dp(node.left);
        int[] right = dp(node.right);

        int include = node.val + left[1] + right[1]; // taking this node forces children to be "excluded"
        int exclude = Math.max(left[0], left[1]) + Math.max(right[0], right[1]); // skip this node: children free to be either

        return new int[]{include, exclude};
    }

    public int solve(TreeNode root) {
        int[] result = dp(root);
        return Math.max(result[0], result[1]);
    }
}
```
This exact shape (`{include, exclude}` pair returned bottom-up) directly answers House Robber III below and is the most common tree-DP template you'll reuse.

## 4. Worked Example — House Robber III (LeetCode 337)

Directly the template from §3: can't rob a node and its direct child simultaneously; maximize total.

```java
class Solution {
    public int rob(TreeNode root) {
        int[] result = dp(root);
        return Math.max(result[0], result[1]);
    }

    private int[] dp(TreeNode node) {
        if (node == null) return new int[]{0, 0};
        int[] left = dp(node.left);
        int[] right = dp(node.right);
        int robThis = node.val + left[1] + right[1];
        int skipThis = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        return new int[]{robThis, skipThis};
    }
}
```
**Note the direct conceptual link to file 01's House Robber I:** same "take-or-skip with adjacency constraint" idea, just the "adjacency" relationship is now parent-child in a tree instead of consecutive-index in an array. Recognizing this transfer (linear adjacency → tree adjacency) is exactly the kind of pattern-generalization this guide is training.

## 5. Worked Example — Binary Tree Maximum Path Sum (LeetCode 124) — The "Global Answer vs Return Value" Split

**The critical subtlety:** a valid path can go **through** a node (using both left and right children), but what you **return** to the parent can only be a **single downward branch** (a path can't fork twice in a valid tree path). This means the "best path using this node" (tracked globally) and "best extension a parent can use" (the return value) are **two different quantities** — a very common conceptual trap.

```java
class Solution {
    private int globalMax = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        maxGain(root);
        return globalMax;
    }

    private int maxGain(TreeNode node) {
        if (node == null) return 0;

        // Negative contributions should be discarded (treated as 0), not subtracted
        int leftGain = Math.max(maxGain(node.left), 0);
        int rightGain = Math.max(maxGain(node.right), 0);

        // This is where the path THROUGH this node (using both children) is considered —
        // but this value is NEVER returned upward, only used to update the global answer.
        int priceThroughNode = node.val + leftGain + rightGain;
        globalMax = Math.max(globalMax, priceThroughNode);

        // What we RETURN to the parent: the best single-branch extension (can't use both children)
        return node.val + Math.max(leftGain, rightGain);
    }
}
```
**Trap:** returning `node.val + leftGain + rightGain` (both children) to the parent is WRONG — it would let the parent's parent effectively use a "forked" path, which isn't a valid tree path (a path can only pass through each node once, entering from one side and exiting the other, or being an endpoint). **Always ask yourself in tree DP: "is the value I compute here the same as the value I return upward?"** Often they're not, and conflating them is the #1 bug in this entire pattern.

## 6. Worked Example — Diameter of Binary Tree (LeetCode 543) — Same Split, Simpler Version

Same "global answer vs return value" split as §5, but simpler: diameter = number of edges on the longest path, which may or may not pass through the root.

```java
class Solution {
    private int diameter = 0;

    public int diameterOfBinaryTree(TreeNode root) {
        height(root);
        return diameter;
    }

    private int height(TreeNode node) {
        if (node == null) return 0;
        int leftHeight = height(node.left);
        int rightHeight = height(node.right);
        diameter = Math.max(diameter, leftHeight + rightHeight); // path THROUGH this node
        return 1 + Math.max(leftHeight, rightHeight);              // height RETURNED upward
    }
}
```

## 7. Worked Example — Binary Tree Cameras (LeetCode 968) — Three-State Tree DP

**Problem:** place minimum cameras on nodes so every node is covered (a camera covers itself, its parent, and its children).

**State (per node), one of three):**
- `0` = node is **not covered**, needs a camera from its parent.
- `1` = node is covered, but **has no camera itself** (covered by a child).
- `2` = node **has a camera**.

**Greedy-via-DP logic (post-order, bottom-up):**
```java
class Solution {
    private int cameras = 0;
    private static final int NOT_COVERED = 0, COVERED_NO_CAMERA = 1, HAS_CAMERA = 2;

    public int minCameraCover(TreeNode root) {
        if (dp(root) == NOT_COVERED) cameras++; // root itself ends up uncovered: needs one more camera
        return cameras;
    }

    private int dp(TreeNode node) {
        if (node == null) return COVERED_NO_CAMERA; // null nodes count as "covered" so leaves don't force cameras unnecessarily

        int left = dp(node.left);
        int right = dp(node.right);

        if (left == NOT_COVERED || right == NOT_COVERED) {
            cameras++;
            return HAS_CAMERA;
        }
        if (left == HAS_CAMERA || right == HAS_CAMERA) {
            return COVERED_NO_CAMERA;
        }
        return NOT_COVERED;
    }
}
```
**Why `null` returns `COVERED_NO_CAMERA` (state 1), not `NOT_COVERED` (state 0):** if null returned "not covered," every leaf node would be forced to think its (nonexistent) children need coverage, placing unnecessary cameras at every leaf. Treating absent children as "already covered" lets leaves correctly report themselves as `NOT_COVERED` (since a leaf has no children to cover it) — this then correctly forces a camera one level up, which is the actual optimal placement.

## 8. Problem Set

### Easy / Medium
| # | Problem | Key twist |
|---|---|---|
| 543 | Diameter of Binary Tree | Global-vs-return split, simplest version |
| 337 | House Robber III | `{include, exclude}` pair template |
| 1372 | Longest ZigZag Path in a Binary Tree | Track two states per node: "longest zigzag ending here going left" / "...going right" |
| 979 | Distribute Coins in Binary Tree | Post-order: track surplus/deficit of coins per subtree, accumulate `abs(surplus)` as moves |
| 1339 | Maximum Product of Splitting a Binary Tree | Compute total sum first, then find best subtree-sum split via a second traversal |

### Hard
| # | Problem | Key twist |
|---|---|---|
| 124 | Binary Tree Maximum Path Sum | Global-vs-return split, classic hard version |
| 968 | Binary Tree Cameras | 3-state greedy-DP hybrid |
| 1245 | Tree Diameter (n-ary tree, via adjacency list) | Same diameter idea generalized beyond binary trees |
| 2246 | Longest Path With Different Adjacent Characters (n-ary tree) | Diameter-style pattern with a value-based constraint on edges |

## 9. Companies Known to Ask This Pattern
Google, Amazon, Meta, Microsoft, Bloomberg — Diameter of Binary Tree and House Robber III are extremely common **medium** tree questions; Binary Tree Maximum Path Sum and Binary Tree Cameras are classic **Google/Meta hard onsite** questions specifically because of the global-vs-return-value subtlety, which is a strong signal of whether a candidate deeply understands recursive state versus just pattern-matching a template.

## 10. Edge Cases & Traps

1. **Global-vs-return-value confusion** (§5, §6) — the single most important trap in this entire pattern. Always explicitly ask: *"what do I return to my parent, and is that the same as (or different from) the value I use to update my global/local answer?"*
2. **Null node base cases returning the WRONG "neutral" value** — as shown in Binary Tree Cameras, the "right" neutral value for a null node depends on the specific semantics of the problem (sometimes `0`, sometimes a specific enum state) — don't default to `0`/`false` without checking what it means in context.
3. **Negative node values** (Binary Tree Max Path Sum) — must clamp child contributions to `max(gain, 0)`, since a negative subtree contribution should simply be excluded from a path, not dragged along and subtracted.
4. **Single-node tree edge case** — root with no children should still produce correct answers (e.g., diameter = 0, max path sum = root.val) — always trace through this manually as a sanity check.
5. **Using instance/static mutable fields for the "global" answer across multiple calls** without resetting them — if `solve()` is called more than once on the same `Solution` instance (common in test harnesses), stale state from a previous call corrupts the new answer. Reset global fields at the start of the public method, not just at class construction.
6. **Recursion depth on skewed/degenerate trees** — a tree that's actually a linked list (all left children, e.g.) can have depth equal to `n`, risking Java stack overflow for large `n`; mention this as a known limitation, and that an iterative post-order traversal with an explicit stack is the fix if truly needed.
7. **Forgetting that a "path" in tree problems usually means "no repeated nodes, connects via edges, can bend at most once (at the LCA-like point)"** — misunderstanding what counts as a valid path is a frequent source of wrong recurrences in path-sum style problems.

## 11. Counter-Questions

1. *"In Binary Tree Max Path Sum, why do we clamp negative child gains to 0 instead of just taking the max of leftGain/rightGain directly?"* → Because a negative contribution would only ever hurt the sum if included; clamping to 0 correctly models "I can choose not to extend the path into a child if it doesn't help," which a plain `max()` without clamping wouldn't capture (it would still force you to add a negative number in `priceThroughNode`).
2. *"How would you modify House Robber III if you could rob a node and skip exactly one generation (grandparent-grandchild constraint instead of parent-child)?"* → State must expand to `{robbedThisLevel, robbedOneLevelUp, notRobbedRecently}` or similar — a good test of whether you can generalize the `{include, exclude}` template to a more complex adjacency rule.
3. *"Binary Tree Cameras returns one of 3 states — walk through why 3 states are necessary, and whether 2 would suffice."* → 2 states (camera / no-camera) can't distinguish "covered by a child's camera" from "not covered at all," which is exactly the information the parent needs to decide whether it must place a camera — hence the third state is structurally necessary, not just convenient.
4. *"Can Tree DP problems always be converted to bottom-up (iterative) solutions, or must they stay top-down recursive?"* → Yes, via an explicit post-order traversal (e.g., using a stack, or processing nodes in reverse-BFS/topological order from leaves to root) — useful to mention if recursion depth is a concern on very large/skewed trees.

Proceed to `09_pattern_bitmask_dp.md`.
