import java.util.*;

public class TwoSumProblemVariantsforFinancialTransactions {

    // Transaction Model
    static class Transaction {
        int id;
        int amount;
        String merchant;
        long timestamp; // epoch time
        String account;

        public Transaction(int id, int amount, String merchant, long timestamp, String account) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.timestamp = timestamp;
            this.account = account;
        }

        @Override
        public String toString() {
            return "(id:" + id + ", amount:" + amount + ")";
        }
    }

    // ------------------ 1. CLASSIC TWO-SUM ------------------
    public List<String> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add("(" + map.get(complement).id + ", " + t.id + ")");
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // ------------------ 2. TWO-SUM WITH TIME WINDOW ------------------
    public List<String> findTwoSumWithTimeWindow(List<Transaction> transactions, int target, long windowMillis) {
        List<String> result = new ArrayList<>();

        // Sort by time
        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        Map<Integer, List<Transaction>> map = new HashMap<>();
        int left = 0;

        for (int right = 0; right < transactions.size(); right++) {
            Transaction current = transactions.get(right);

            // Remove old transactions outside window
            while (current.timestamp - transactions.get(left).timestamp > windowMillis) {
                Transaction old = transactions.get(left);
                map.get(old.amount).remove(old);
                left++;
            }

            int complement = target - current.amount;
            if (map.containsKey(complement)) {
                for (Transaction t : map.get(complement)) {
                    result.add("(" + t.id + ", " + current.id + ")");
                }
            }

            map.putIfAbsent(current.amount, new ArrayList<>());
            map.get(current.amount).add(current);
        }

        return result;
    }

    // ------------------ 3. K-SUM ------------------
    public List<List<Integer>> findKSum(List<Transaction> transactions, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        for (Transaction t : transactions) {
            amounts.add(t.amount);
        }

        Collections.sort(amounts);
        kSumHelper(amounts, 0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(List<Integer> nums, int start, int k, int target,
                            List<Integer> current, List<List<Integer>> result) {

        if (k == 2) {
            int left = start, right = nums.size() - 1;

            while (left < right) {
                int sum = nums.get(left) + nums.get(right);

                if (sum == target) {
                    List<Integer> temp = new ArrayList<>(current);
                    temp.add(nums.get(left));
                    temp.add(nums.get(right));
                    result.add(temp);

                    left++;
                    right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
            return;
        }

        for (int i = start; i < nums.size(); i++) {
            current.add(nums.get(i));
            kSumHelper(nums, i + 1, k - 1, target - nums.get(i), current, result);
            current.remove(current.size() - 1);
        }
    }

    // ------------------ 4. DUPLICATE DETECTION ------------------
    public List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;

            map.putIfAbsent(key, new HashSet<>());
            map.get(key).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate → " + entry.getKey()
                        + " Accounts: " + entry.getValue());
            }
        }

        return result;
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {

        TwoSumProblemVariantsforFinancialTransactions system =
                new TwoSumProblemVariantsforFinancialTransactions();

        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "Store A", System.currentTimeMillis(), "acc1"),
                new Transaction(2, 300, "Store B", System.currentTimeMillis() + 1000, "acc2"),
                new Transaction(3, 200, "Store C", System.currentTimeMillis() + 2000, "acc3"),
                new Transaction(4, 500, "Store A", System.currentTimeMillis() + 3000, "acc4")
        );

        System.out.println("Two-Sum:");
        System.out.println(system.findTwoSum(transactions, 500));

        System.out.println("\nTwo-Sum with 1 hour window:");
        System.out.println(system.findTwoSumWithTimeWindow(transactions, 500, 3600000));

        System.out.println("\nK-Sum (k=3, target=1000):");
        System.out.println(system.findKSum(transactions, 3, 1000));

        System.out.println("\nDuplicate Detection:");
        System.out.println(system.detectDuplicates(transactions));
    }
}
