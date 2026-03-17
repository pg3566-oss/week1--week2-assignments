
import java.util.*;

public class AutocompleteSystemforSearchEngine {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children;
        Map<String, Integer> queryFrequencyMap; // stores queries passing through this node

        public TrieNode() {
            children = new HashMap<>();
            queryFrequencyMap = new HashMap<>();
        }
    }

    private TrieNode root;
    private Map<String, Integer> globalFrequencyMap;
    private final int TOP_K = 10;

    public AutocompleteSystemforSearchEngine() {
        root = new TrieNode();
        globalFrequencyMap = new HashMap<>();
    }

    // Insert or update query
    public void updateFrequency(String query) {
        globalFrequencyMap.put(query,
                globalFrequencyMap.getOrDefault(query, 0) + 1);

        int freq = globalFrequencyMap.get(query);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // update frequency at each prefix node
            node.queryFrequencyMap.put(query, freq);
        }
    }

    // Get top 10 suggestions for prefix
    public List<String> search(String prefix) {
        TrieNode node = root;

        // Traverse Trie
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        // Min-Heap for Top K
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : node.queryFrequencyMap.entrySet()) {
            minHeap.offer(entry);

            if (minHeap.size() > TOP_K) {
                minHeap.poll();
            }
        }

        // Extract results
        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll().getKey() +
                    " (" + globalFrequencyMap.get(minHeap.peek() == null ? "" : minHeap.peek().getKey()) + ")");
        }

        Collections.reverse(result); // highest freq first
        return result;
    }

    // Simple typo handling (edit distance = 1)
    public List<String> suggestWithTypo(String input) {
        List<String> results = new ArrayList<>();

        for (String query : globalFrequencyMap.keySet()) {
            if (isOneEditAway(input, query)) {
                results.add(query + " (" + globalFrequencyMap.get(query) + ")");
            }
        }

        return results;
    }

    // Check if strings differ by 1 edit
    private boolean isOneEditAway(String s1, String s2) {
        if (Math.abs(s1.length() - s2.length()) > 1) return false;

        int i = 0, j = 0, edits = 0;

        while (i < s1.length() && j < s2.length()) {
            if (s1.charAt(i) != s2.charAt(j)) {
                if (++edits > 1) return false;

                if (s1.length() > s2.length()) i++;
                else if (s1.length() < s2.length()) j++;
                else {
                    i++;
                    j++;
                }
            } else {
                i++;
                j++;
            }
        }
        return true;
    }

    // Testing
    public static void main(String[] args) {
        AutocompleteSystemforSearchEngine system =
                new AutocompleteSystemforSearchEngine();

        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java download");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java tutorial");

        System.out.println("Search results for 'jav':");
        List<String> results = system.search("jav");
        for (String res : results) {
            System.out.println(res);
        }

        System.out.println("\nTypo suggestions for 'jva':");
        List<String> typo = system.suggestWithTypo("jva");
        for (String res : typo) {
            System.out.println(res);
        }
    }
}