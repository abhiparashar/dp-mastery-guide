/**
 * LeetCode 121: Best Time to Buy and Sell Stock
 * https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
 *
 * You are given an array prices where prices[i] is the price of a given
 * stock on the i-th day. You want to maximize your profit by choosing a
 * single day to buy one stock and choosing a different day in the future
 * to sell that stock. Return the maximum profit you can achieve from this
 * transaction. If you cannot achieve any profit, return 0.
 *
 * Key twist: Track min-so-far while scanning (also the seed of state-machine
 * DP, see file 11 in the mastery guide: 11_pattern_state_machine_stock_dp.md)
 *
 * Constraints:
 * 1 <= prices.length <= 10^5
 * 0 <= prices[i] <= 10^4
 */
public class BestTimeToBuySellStock {

    public int maxProfit(int[] prices) {
        // TODO: implement
        return 0;
    }

    public static void main(String[] args) {
        BestTimeToBuySellStock sol = new BestTimeToBuySellStock();

        // Test case 1: prices = [7,1,5,3,6,4] -> expected 5
        int[] prices1 = {7, 1, 5, 3, 6, 4};
        System.out.println("Test 1: " + sol.maxProfit(prices1) + " (expected 5)");

        // Test case 2: prices = [7,6,4,3,1] -> expected 0
        int[] prices2 = {7, 6, 4, 3, 1};
        System.out.println("Test 2: " + sol.maxProfit(prices2) + " (expected 0)");

        // Test case 3: prices = [2,4,1] -> expected 2
        int[] prices3 = {2, 4, 1};
        System.out.println("Test 3: " + sol.maxProfit(prices3) + " (expected 2)");

        // Test case 4: prices = [1] -> expected 0
        int[] prices4 = {1};
        System.out.println("Test 4: " + sol.maxProfit(prices4) + " (expected 0)");
    }
}
