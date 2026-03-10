import java.util.*;

public class SocialMediaUsernameAvailabilityChecker {

    private Map<String, Integer> usernameMap = new HashMap<>();
    private Map<String, Integer> attemptMap = new HashMap<>();

    public boolean checkAvailability(String username) {
        attemptMap.put(username, attemptMap.getOrDefault(username, 0) + 1);
        return !usernameMap.containsKey(username);
    }

    public void register(String username, int userId) {
        if (usernameMap.containsKey(username)) {
            System.out.println("Username already taken.");
            return;
        }

        usernameMap.put(username, userId);
        System.out.println(username + " registered successfully.");
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;

            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String dotVersion = username.replace("_", ".");

        if (!usernameMap.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String result = "";
        int max = 0;

        for (String username : attemptMap.keySet()) {

            int count = attemptMap.get(username);

            if (count > max) {
                max = count;
                result = username;
            }
        }

        return result + " (" + max + " attempts)";
    }

    public static void main(String[] args) {

        SocialMediaUsernameAvailabilityChecker checker =
                new SocialMediaUsernameAvailabilityChecker();

        checker.register("john_doe", 1001);

        System.out.println("john_doe available: " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available: " + checker.checkAvailability("jane_smith"));

        System.out.println("Suggestions: " + checker.suggestAlternatives("john_doe"));

        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");

        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}
