import java.util.*;

public class DNSCachewithTTL {

    class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, int ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int MAX_CACHE_SIZE = 5;

    private Map<String, DNSEntry> cache =
            new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };

    private int hits = 0;
    private int misses = 0;

    public DNSCachewithTTL() {
        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        if (cache.containsKey(domain)) {
            DNSEntry entry = cache.get(domain);

            if (!entry.isExpired()) {
                hits++;
                return "Cache HIT → " + entry.ipAddress;
            } else {
                cache.remove(domain);
                System.out.println("Cache EXPIRED for " + domain);
            }
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new DNSEntry(domain, ip, 10));

        return "Cache MISS → Queried upstream → " + ip;
    }

    // Simulate upstream DNS query
    private String queryUpstreamDNS(String domain) {

        try {
            Thread.sleep(100); // simulate 100ms network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Background thread to remove expired entries
    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {

            while (true) {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (this) {

                    Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();

                    while (iterator.hasNext()) {

                        Map.Entry<String, DNSEntry> entry = iterator.next();

                        if (entry.getValue().isExpired()) {
                            iterator.remove();
                        }
                    }
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Cache statistics
    public void getCacheStats() {

        int total = hits + misses;

        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);

        System.out.println("Cache Hits: " + hits);
        System.out.println("Cache Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    public static void main(String[] args) throws Exception {

        DNSCachewithTTL dnsCache = new DNSCachewithTTL();

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("openai.com"));

        Thread.sleep(11000);

        System.out.println(dnsCache.resolve("google.com"));

        dnsCache.getCacheStats();
    }
}
