package in.duplicatevoter.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class VoterRepository {
    private final List<VoterRecord> records = new ArrayList<>();

    void replaceAll(List<VoterRecord> imported) {
        records.clear();
        records.addAll(imported);
    }

    int size() {
        return records.size();
    }

    List<VoterRecord> search(String query, int limit) {
        List<VoterRecord> matches = new ArrayList<>();
        for (VoterRecord record : records) {
            if (DuplicateMatcher.containsQuery(record, query)) {
                matches.add(record);
                if (matches.size() >= limit) {
                    break;
                }
            }
        }
        return matches;
    }

    List<DuplicateCase> findDuplicateCases(VoterRecord query) {
        List<DuplicateCase> cases = new ArrayList<>();
        for (VoterRecord candidate : records) {
            if (candidate == query) {
                continue;
            }
            DuplicateMatcher.MatchResult result = DuplicateMatcher.score(query, candidate);
            if (result.isMatch()) {
                cases.add(new DuplicateCase(candidate, result.score, result.reasonLabel()));
            }
        }
        Collections.sort(cases, new Comparator<DuplicateCase>() {
            @Override
            public int compare(DuplicateCase left, DuplicateCase right) {
                return Integer.compare(right.score, left.score);
            }
        });
        return cases;
    }

    Map<String, List<VoterRecord>> exactEpicDuplicates() {
        Map<String, List<VoterRecord>> byEpic = new HashMap<>();
        for (VoterRecord record : records) {
            if (record.epic.isEmpty()) {
                continue;
            }
            List<VoterRecord> bucket = byEpic.get(record.epic);
            if (bucket == null) {
                bucket = new ArrayList<>();
                byEpic.put(record.epic, bucket);
            }
            bucket.add(record);
        }

        Map<String, List<VoterRecord>> duplicates = new HashMap<>();
        for (Map.Entry<String, List<VoterRecord>> entry : byEpic.entrySet()) {
            if (entry.getValue().size() > 1 && hasMultipleLocations(entry.getValue())) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }
        return duplicates;
    }

    private boolean hasMultipleLocations(List<VoterRecord> values) {
        String first = null;
        for (VoterRecord record : values) {
            if (first == null) {
                first = record.locationKey();
            } else if (!first.equals(record.locationKey())) {
                return true;
            }
        }
        return false;
    }

    static final class DuplicateCase {
        final VoterRecord record;
        final int score;
        final String reason;

        DuplicateCase(VoterRecord record, int score, String reason) {
            this.record = record;
            this.score = score;
            this.reason = reason;
        }
    }
}
