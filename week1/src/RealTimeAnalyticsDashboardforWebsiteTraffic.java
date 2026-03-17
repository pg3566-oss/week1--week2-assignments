import java.util.*;

// Class to store page data
class PageData {
    String url;
    int visits;

    PageData(String url, int visits) {
        this.url = url;
        this.visits = visits;
    }
}

// Modified PUBLIC CLASS NAME
public class RealTimeAnalyticsDashboardforWebsiteTraffic {

    private HashMap<String, Integer> pageVisitCount = new HashMap<>();
    private HashMap<String, HashSet<String>> uniqueVisitors = new HashMap<>();
    private HashMap<String, Integer> sourceCount = new HashMap<>();

    // Process event
    public void processEvent(String url, String userId, String source) {

        // Update visit count
        pageVisitCount.put(url, pageVisitCount.getOrDefault(url, 0) + 1);

        // Update unique visitors
        uniqueVisitors.putIfAbsent(url, new HashSet<>());
        uniqueVisitors.get(url).add(userId);

        // Update source count
        sourceCount.put(source, sourceCount.getOrDefault(source, 0) + 1);
    }

    // Get top 10 pages
    private List<PageData> getTopPages() {

        PriorityQueue<PageData> minHeap =
                new PriorityQueue<>((a, b) -> a.visits - b.visits);

        for (String url : pageVisitCount.keySet()) {

            minHeap.offer(new PageData(url, pageVisitCount.get(url)));

            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<PageData> result = new ArrayList<>();

        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll());
        }

        Collections.reverse(result);
        return result;
    }

    // Display dashboard
    public void getDashboard() {

        System.out.println("Top Pages:");

        List<PageData> topPages = getTopPages();
        int rank = 1;

        for (PageData page : topPages) {
            int uniqueCount = uniqueVisitors.get(page.url).size();

            System.out.println(rank + ". " + page.url +
                    " - " + page.visits + " views (" +
                    uniqueCount + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");

        int total = 0;
        for (int count : sourceCount.values()) {
            total += count;
        }

        for (String source : sourceCount.keySet()) {
            double percentage = (sourceCount.get(source) * 100.0) / total;

            System.out.println(source + ": " +
                    String.format("%.2f", percentage) + "%");
        }
    }

    // Main method
    public static void main(String[] args) {

        RealTimeAnalyticsDashboardforWebsiteTraffic analytics =
                new RealTimeAnalyticsDashboardforWebsiteTraffic();

        // Sample events
        analytics.processEvent("/article/breaking-news", "user_123", "google");
        analytics.processEvent("/article/breaking-news", "user_456", "facebook");
        analytics.processEvent("/sports/championship", "user_789", "direct");
        analytics.processEvent("/article/breaking-news", "user_123", "google");

        analytics.getDashboard();
    }
}
