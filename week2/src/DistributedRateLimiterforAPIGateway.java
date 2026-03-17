import java.util.concurrent.ConcurrentHashMap;

public class DistributedRateLimiterforAPIGateway {

    // Token Bucket Class
    static class TokenBucket {
        private int tokens;
        private final int maxTokens;
        private final double refillRate; // tokens per second
        private long lastRefillTime;

        public TokenBucket(int maxTokens, double refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on elapsed time
        private synchronized void refill() {
            long now = System.currentTimeMillis();
            double tokensToAdd = ((now - lastRefillTime) / 1000.0) * refillRate;

            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + (int) tokensToAdd);
                lastRefillTime = now;
            }
        }

        // Try to consume a token
        public synchronized boolean allowRequest() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return tokens;
        }

        public synchronized long getRetryAfterSeconds() {
            if (tokens > 0) return 0;
            return (long) Math.ceil(1 / refillRate);
        }
    }

    // Store buckets per client
    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS = 1000;
    private final double REFILL_RATE = 1000.0 / 3600; // per second

    // Get or create bucket
    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(MAX_REQUESTS, REFILL_RATE));
    }

    // API: Check rate limit
    public String checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after "
                    + bucket.getRetryAfterSeconds() + "s)";
        }
    }

    // API: Get status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        int remaining = bucket.getRemainingTokens();
        int used = MAX_REQUESTS - remaining;

        long resetTime = System.currentTimeMillis() / 1000 + 3600;

        return "{used: " + used +
                ", limit: " + MAX_REQUESTS +
                ", reset: " + resetTime + "}";
    }

    // Main for testing
    public static void main(String[] args) {
        DistributedRateLimiterforAPIGateway limiter =
                new DistributedRateLimiterforAPIGateway();

        String client = "abc123";

        // Simulate requests
        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        // Print status
        System.out.println(limiter.getRateLimitStatus(client));
    }
}