package in.duplicatevoter.finder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CsvParser {
    private CsvParser() {
    }

    static List<VoterRecord> parse(InputStream inputStream) throws IOException {
        List<VoterRecord> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return records;
        }

        List<String> headers = parseLine(stripBom(headerLine));
        Map<String, Integer> index = buildIndex(headers);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            List<String> values = parseLine(line);
            records.add(new VoterRecord(
                    value(values, index, "name", "voter_name", "elector_name"),
                    value(values, index, "relative_name", "father_name", "husband_name", "relation_name"),
                    value(values, index, "age"),
                    value(values, index, "gender", "sex"),
                    value(values, index, "epic", "epic_no", "voter_id", "voter_no"),
                    value(values, index, "state"),
                    value(values, index, "district"),
                    value(values, index, "assembly_constituency", "ac", "constituency"),
                    value(values, index, "part_no", "part", "booth_no"),
                    value(values, index, "serial_no", "serial", "sl_no"),
                    value(values, index, "address", "house_no", "house")
            ));
        }
        return records;
    }

    private static Map<String, Integer> buildIndex(List<String> headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            index.put(DuplicateMatcher.normalize(headers.get(i)).replace(" ", "_"), i);
        }
        return index;
    }

    private static String value(List<String> values, Map<String, Integer> index, String... aliases) {
        for (String alias : aliases) {
            Integer column = index.get(alias);
            if (column != null && column < values.size()) {
                return values.get(column);
            }
        }
        return "";
    }

    private static String stripBom(String value) {
        if (value != null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }
        return value;
    }

    private static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }
}
