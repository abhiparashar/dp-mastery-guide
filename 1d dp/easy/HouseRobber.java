/**
 * LeetCode 198: House Robber
 * https://leetcode.com/problems/house-robber/
 *
 * You are a professional robber planning to rob houses along a street.
 * Each house has a certain amount of money stashed, the only constraint
 * stopping you from robbing each of them is that adjacent houses have
 * security systems connected and it will automatically contact the police
 * if two adjacent houses were broken into on the same night.
 *
 * Given an integer array nums representing the amount of money of each
 * house, return the maximum amount of money you can rob tonight without
 * alerting the police.
 *
 * Key twist: Classic skip-or-take -> dp[i] = max(dp[i-1], dp[i-2] + nums[i])
 *
 * Constraints:
 * 1 <= nums.length <= 100
 * 0 <= nums[i] <= 400
 */
public class HouseRobber {

    public int rob(int[] nums) {
        // TODO: implement
        return 0;
    }

    public static void main(String[] args) {
        HouseRobber sol = new HouseRobber();

        // Test case 1: nums = [1,2,3,1] -> expected 4
        int[] nums1 = {1, 2, 3, 1};
        System.out.println("Test 1: " + sol.rob(nums1) + " (expected 4)");

        // Test case 2: nums = [2,7,9,3,1] -> expected 12
        int[] nums2 = {2, 7, 9, 3, 1};
        System.out.println("Test 2: " + sol.rob(nums2) + " (expected 12)");

        // Test case 3: nums = [2,1,1,2] -> expected 4
        int[] nums3 = {2, 1, 1, 2};
        System.out.println("Test 3: " + sol.rob(nums3) + " (expected 4)");

        // Test case 4: nums = [5] -> expected 5
        int[] nums4 = {5};
        System.out.println("Test 4: " + sol.rob(nums4) + " (expected 5)");
    }
}
