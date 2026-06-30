# Duplicate Voter Finder India

Native Android app for checking whether a person appears in more than one imported electoral roll location.

## What it does

- Imports CSV voter-roll data from Android's file picker.
- Searches by voter name, relative name, EPIC number, age, gender, state, district, assembly constituency, and part number.
- Flags exact duplicate EPIC numbers across different places.
- Flags probable duplicate people using normalized name, relative name, age, and gender.
- Runs offline on the device after CSV import.

## Data source note

The Election Commission of India provides public voter-search and electoral-roll access through official portals, but this project intentionally does not scrape private web endpoints. Use legally obtained electoral-roll exports and convert PDFs or tables to CSV before importing.

Recommended CSV headers:

```csv
name,relative_name,age,gender,epic,state,district,assembly_constituency,part_no,serial_no,address
```

Header aliases are accepted, for example `father_name`, `relation_name`, `voter_id`, `epic_no`, `ac`, `part`, and `house_no`.

## Open in Android Studio

1. Open this folder in Android Studio.
2. Let Gradle sync download the Android Gradle plugin.
3. Run the `app` configuration on an Android device or emulator.

This environment did not have Java or Gradle on PATH, so the project was scaffolded but not compiled here.
