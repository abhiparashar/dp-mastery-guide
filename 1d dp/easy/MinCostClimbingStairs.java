/**
 * LeetCode 746: Min Cost Climbing Stairs
 * https://leetcode.com/problems/min-cost-climbing-stairs/
 *
 * You are given an integer array cost where cost[i] is the cost of the i-th
 * step on a staircase. Once you pay the cost, you can either climb one or
 * two steps. You can either start from the step with index 0, or the step
 * with index 1. Return the minimum cost to reach the top of the floor
 * (i.e. one step past the last index).
 *
 * Key twist: Min instead of count -> dp[i] = cost[i] + min(dp[i-1], dp[i-2])
 *
 * Constraints:
 * 2 <= cost.length <= 1000
 * 0 <= cost[i] <= 999
 */
public class MinCostClimbingStairs {

    public int minCostClimbingStairs(int[] cost) {
        // TODO: implement
        return 0;
    }

    public static void main(String[] args) {
        MinCostClimbingStairs sol = new MinCostClimbingStairs();

        // Test case 1: cost = [10,15,20] -> expected 15
        int[] cost1 = {10, 15, 20};
        System.out.println("Test 1: " + sol.minCostClimbingStairs(cost1) + " (expected 15)");

        // Test case 2: cost = [1,100,1,1,1,100,1,1,100,1] -> expected 6
        int[] cost2 = {1, 100, 1, 1, 1, 100, 1, 1, 100, 1};
        System.out.println("Test 2: " + sol.minCostClimbingStairs(cost2) + " (expected 6)");

        // Test case 3: cost = [0,0,0,1] -> expected 0
        int[] cost3 = {0, 0, 0, 1};
        System.out.println("Test 3: " + sol.minCostClimbingStairs(cost3) + " (expected 0)");

        // Test case 4: cost = [1,2] -> expected 1
        int[] cost4 = {1, 2};
        System.out.println("Test 4: " + sol.minCostClimbingStairs(cost4) + " (expected 1)");
    }
}
