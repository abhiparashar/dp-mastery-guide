/**
 * LeetCode 70: Climbing Stairs
 * https://leetcode.com/problems/climbing-stairs/
 *
 * You are climbing a staircase. It takes n steps to reach the top.
 * Each time you can either climb 1 or 2 steps.
 * In how many distinct ways can you climb to the top?
 *
 * Key twist: Pure Fibonacci recurrence -> dp[i] = dp[i-1] + dp[i-2]
 *
 * Constraints:
 * 1 <= n <= 45
 */
public class ClimbingStairs {

  public int climbStairs(int n) {
    if (n <= 1)
      return 1;
    int[] dp = new int[n + 1];
    dp[0] = 1;
    dp[1] = 1;
    for (int i = 2; i <= n; i++) {
      dp[i] = dp[i - 1] + dp[i - 2];
    }
    return dp[n];
  }

  public static void main(String[] args) {
    ClimbingStairs sol = new ClimbingStairs();

    // Test case 1: n = 2 -> expected 2
    System.out.println("Test 1: " + sol.climbStairs(2) + " (expected 2)");

    // Test case 2: n = 3 -> expected 3
    System.out.println("Test 2: " + sol.climbStairs(3) + " (expected 3)");

    // Test case 3: n = 1 -> expected 1
    System.out.println("Test 3: " + sol.climbStairs(1) + " (expected 1)");

    // Test case 4: n = 5 -> expected 8
    System.out.println("Test 4: " + sol.climbStairs(5) + " (expected 8)");

    // Test case 5: n = 45 -> expected 1836311903
    System.out.println("Test 5: " + sol.climbStairs(45) + " (expected 1836311903)");
  }
}
