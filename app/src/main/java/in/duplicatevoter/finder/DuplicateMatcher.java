package in.duplicatevoter.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DuplicateMatcher {
    private DuplicateMatcher() {
    }

    static MatchResult score(VoterRecord query, VoterRecord candidate) {
        if (candidate.locationKey().equals(query.locationKey())) {
            return MatchResult.noMatch();
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (!query.epic.isEmpty() && query.epic.equalsIgnoreCase(candidate.epic)) {
            score += 100;
            reasons.add("same EPIC number");
        }

        double nameScore = similarity(normalize(query.name), normalize(candidate.name));
        if (nameScore >= 0.92) {
            score += 42;
            reasons.add("name matches");
        } else if (nameScore >= 0.78) {
            score += 24;
            reasons.add("similar name");
        }

        double relativeScore = similarity(normalize(query.relativeName), normalize(candidate.relativeName));
        if (relativeScore >= 0.90 && !query.relativeName.isEmpty() && !candidate.relativeName.isEmpty()) {
            score += 30;
            reasons.add("relative name matches");
        } else if (relativeScore >= 0.76 && !query.relativeName.isEmpty() && !candidate.relativeName.isEmpty()) {
            score += 16;
            reasons.add("similar relative name");
        }

        if (!query.gender.isEmpty() && query.gender.equalsIgnoreCase(candidate.gender)) {
            score += 8;
            reasons.add("same gender");
        }

        int ageDelta = ageDelta(query.age, candidate.age);
        if (ageDelta == 0) {
            score += 12;
            reasons.add("same age");
        } else if (ageDelta > 0 && ageDelta <= 2) {
            score += 7;
            reasons.add("age within 2 years");
        }

        if (score >= 62) {
            return new MatchResult(score, reasons);
        }
        return MatchResult.noMatch();
    }

    static boolean containsQuery(VoterRecord record, String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String haystack = normalize(record.name + " " + record.relativeName + " " + record.epic + " "
                + record.state + " " + record.district + " " + record.assemblyConstituency + " "
                + record.partNo + " " + record.serialNo + " " + record.address);
        return haystack.contains(normalizedQuery);
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int ageDelta(String first, String second) {
        try {
            int left = Integer.parseInt(first.trim());
            int right = Integer.parseInt(second.trim());
            return Math.abs(left - right);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static double similarity(String first, String second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }
        if (first.equals(second)) {
            return 1;
        }
        int distance = levenshtein(first, second);
        int max = Math.max(first.length(), second.length());
        return 1.0 - ((double) distance / (double) max);
    }

    private static int levenshtein(String first, String second) {
        int[] costs = new int[second.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            costs[0] = i;
            int previous = i - 1;
            for (int j = 1; j <= second.length(); j++) {
                int current = costs[j];
                int replacement = first.charAt(i - 1) == second.charAt(j - 1) ? previous : previous + 1;
                costs[j] = Math.min(Math.min(costs[j] + 1, costs[j - 1] + 1), replacement);
                previous = current;
            }
        }
        return costs[second.length()];
    }

    static final class MatchResult {
        final int score;
        final List<String> reasons;

        MatchResult(int score, List<String> reasons) {
            this.score = score;
            this.reasons = reasons;
        }

        boolean isMatch() {
            return score > 0;
        }

        String reasonLabel() {
            return reasons.isEmpty() ? "" : join(reasons);
        }

        static MatchResult noMatch() {
            return new MatchResult(0, new ArrayList<String>());
        }

        private static String join(List<String> values) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(values.get(i));
            }
            return builder.toString();
        }
    }
}
