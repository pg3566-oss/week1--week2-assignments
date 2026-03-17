import java.util.*;

public class MultiLevelCacheSystemwithHashTables {

    // ---------------- VIDEO MODEL ----------------
    static class Video {
        String videoId;
        String content;

        public Video(String id, String content) {
            this.videoId = id;
            this.content = content;
        }
    }

    // ---------------- LRU CACHE ----------------
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // ---------------- CACHE LEVELS ----------------
    private LRUCache<String, Video> L1 = new LRUCache<>(10000);
    private LRUCache<String, Video> L2 = new LRUCache<>(100000);
    private Map<String, Video> L3Database = new HashMap<>();

    private Map<String, Integer> accessCount = new HashMap<>();

    // ---------------- STATISTICS ----------------
    private int L1Hits = 0;
    private int L2Hits = 0;
    private int L3Hits = 0;

    // ---------------- GET VIDEO ----------------
    public Video getVideo(String videoId) {

        // L1 lookup
        if (L1.containsKey(videoId)) {
            L1Hits++;
            return L1.get(videoId);
        }

        // L2 lookup
        if (L2.containsKey(videoId)) {
            L2Hits++;

            Video v = L2.get(videoId);

            promoteToL1(videoId, v);

            return v;
        }

        // L3 lookup
        if (L3Database.containsKey(videoId)) {
            L3Hits++;

            Video v = L3Database.get(videoId);

            promoteToL2(videoId, v);

            return v;
        }

        return null;
    }

    // ---------------- PROMOTION LOGIC ----------------
    private void promoteToL1(String videoId, Video v) {
        L1.put(videoId, v);
    }

    private void promoteToL2(String videoId, Video v) {
        L2.put(videoId, v);

        accessCount.put(videoId,
                accessCount.getOrDefault(videoId, 0) + 1);

        if (accessCount.get(videoId) > 3) {
            promoteToL1(videoId, v);
        }
    }

    // ---------------- CONTENT UPDATE ----------------
    public void updateVideo(String videoId, String newContent) {

        Video updated = new Video(videoId, newContent);

        L3Database.put(videoId, updated);

        L1.remove(videoId);
        L2.remove(videoId);
    }

    // ---------------- ADD VIDEO ----------------
    public void addVideoToDatabase(String id, String content) {
        L3Database.put(id, new Video(id, content));
    }

    // ---------------- STATISTICS ----------------
    public void getStatistics() {

        int total = L1Hits + L2Hits + L3Hits;

        double l1Rate = total == 0 ? 0 : (100.0 * L1Hits / total);
        double l2Rate = total == 0 ? 0 : (100.0 * L2Hits / total);
        double l3Rate = total == 0 ? 0 : (100.0 * L3Hits / total);

        System.out.println("L1 Hit Rate: " + String.format("%.2f", l1Rate) + "%");
        System.out.println("L2 Hit Rate: " + String.format("%.2f", l2Rate) + "%");
        System.out.println("L3 Hit Rate: " + String.format("%.2f", l3Rate) + "%");
    }

    // ---------------- TEST ----------------
    public static void main(String[] args) {

        MultiLevelCacheSystemwithHashTables cache =
                new MultiLevelCacheSystemwithHashTables();

        cache.addVideoToDatabase("video_123", "Movie A");
        cache.addVideoToDatabase("video_999", "Movie B");

        System.out.println("First request:");
        cache.getVideo("video_123");

        System.out.println("Second request:");
        cache.getVideo("video_123");

        System.out.println("Third request:");
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}
