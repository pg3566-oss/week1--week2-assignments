import java.util.*;

public class PlagiarismDetectionSystem {

    // n-gram size (5 words)
    private static final int N = 5;

    // nGram -> set of document IDs containing it
    private Map<String, Set<String>> index = new HashMap<>();

    // documentId -> list of its n-grams
    private Map<String, List<String>> documentNGrams = new HashMap<>();

    // Add a document to the database
    public void addDocument(String documentId, String text) {
        List<String> ngrams = generateNGrams(text);
        documentNGrams.put(documentId, ngrams);

        for (String gram : ngrams) {
            index.computeIfAbsent(gram, k -> new HashSet<>()).add(documentId);
        }
    }

    // Analyze a document for plagiarism
    public void analyzeDocument(String documentId) {

        List<String> ngrams = documentNGrams.get(documentId);

        if (ngrams == null) {
            System.out.println("Document not found.");
            return;
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        Map<String, Integer> matchCount = new HashMap<>();

        // Find matching documents
        for (String gram : ngrams) {
            Set<String> docs = index.getOrDefault(gram, new HashSet<>());

            for (String doc : docs) {
                if (!doc.equals(documentId)) {
                    matchCount.put(doc, matchCount.getOrDefault(doc, 0) + 1);
                }
            }
        }

        // Calculate similarity
        for (String doc : matchCount.keySet()) {
            int matches = matchCount.get(doc);
            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("Found " + matches + " matching n-grams with \"" + doc + "\"");
            System.out.printf("Similarity: %.1f%%", similarity);

            if (similarity > 60) {
                System.out.println(" (PLAGIARISM DETECTED)");
            } else if (similarity > 10) {
                System.out.println(" (suspicious)");
            } else {
                System.out.println();
            }
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {
        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            ngrams.add(gram.toString().trim());
        }

        return ngrams;
    }

    public static void main(String[] args) {

        PlagiarismDetectionSystem detector = new PlagiarismDetectionSystem();

        String essay1 = "Artificial intelligence is transforming the world with new innovations and technology advancements";
        String essay2 = "Artificial intelligence is transforming the world with powerful technology and new innovations";
        String essay3 = "Climate change affects global temperatures and ocean levels significantly";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);
        detector.addDocument("essay_150.txt", essay3);

        detector.analyzeDocument("essay_092.txt");
    }
}

