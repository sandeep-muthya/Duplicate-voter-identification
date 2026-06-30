package in.duplicatevoter.finder;

final class VoterRecord {
    final String name;
    final String relativeName;
    final String age;
    final String gender;
    final String epic;
    final String state;
    final String district;
    final String assemblyConstituency;
    final String partNo;
    final String serialNo;
    final String address;

    VoterRecord(
            String name,
            String relativeName,
            String age,
            String gender,
            String epic,
            String state,
            String district,
            String assemblyConstituency,
            String partNo,
            String serialNo,
            String address
    ) {
        this.name = clean(name);
        this.relativeName = clean(relativeName);
        this.age = clean(age);
        this.gender = clean(gender);
        this.epic = clean(epic).toUpperCase();
        this.state = clean(state);
        this.district = clean(district);
        this.assemblyConstituency = clean(assemblyConstituency);
        this.partNo = clean(partNo);
        this.serialNo = clean(serialNo);
        this.address = clean(address);
    }

    String locationKey() {
        return DuplicateMatcher.normalize(state + "|" + district + "|" + assemblyConstituency + "|" + partNo);
    }

    String locationLabel() {
        StringBuilder builder = new StringBuilder();
        append(builder, state);
        append(builder, district);
        append(builder, assemblyConstituency);
        if (!partNo.isEmpty()) {
            append(builder, "Part " + partNo);
        }
        return builder.length() == 0 ? "Unknown location" : builder.toString();
    }

    String primaryLabel() {
        StringBuilder builder = new StringBuilder(name.isEmpty() ? "Unnamed voter" : name);
        if (!age.isEmpty()) {
            builder.append(", age ").append(age);
        }
        if (!gender.isEmpty()) {
            builder.append(", ").append(gender.toUpperCase());
        }
        return builder.toString();
    }

    String detailLabel() {
        StringBuilder builder = new StringBuilder();
        if (!epic.isEmpty()) {
            builder.append("EPIC: ").append(epic).append("\n");
        }
        if (!relativeName.isEmpty()) {
            builder.append("Relative: ").append(relativeName).append("\n");
        }
        builder.append("Place: ").append(locationLabel());
        if (!serialNo.isEmpty()) {
            builder.append("\nSerial: ").append(serialNo);
        }
        if (!address.isEmpty()) {
            builder.append("\nAddress: ").append(address);
        }
        return builder.toString();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" / ");
        }
        builder.append(value.trim());
    }
}
