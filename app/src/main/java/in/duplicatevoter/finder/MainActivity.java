package in.duplicatevoter.finder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int PICK_CSV_REQUEST = 1001;
    private static final int BLUE = Color.rgb(21, 94, 239);
    private static final int GREEN = Color.rgb(22, 163, 74);
    private static final int INK = Color.rgb(15, 23, 42);
    private static final int MUTED = Color.rgb(71, 85, 105);
    private static final int SURFACE = Color.rgb(248, 250, 252);

    private final VoterRepository repository = new VoterRepository();
    private LinearLayout results;
    private TextView status;
    private EditText queryInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildLayout());
        updateStatus("Import a CSV voter roll to begin.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importCsv(data.getData());
        }
    }

    private View buildLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(SURFACE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(28));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("Duplicate Voter Finder", 26, INK, true);
        root.addView(title);

        TextView subtitle = text("Import electoral-roll CSV files and check whether a voter appears in more than one place.", 15, MUTED, false);
        subtitle.setPadding(0, dp(6), 0, dp(14));
        root.addView(subtitle);

        Button importButton = button("Import CSV", BLUE);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCsvPicker();
            }
        });
        root.addView(importButton, matchWrap());

        Button eciButton = button("Open ECI Voter Search", GREEN);
        eciButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEciPortal();
            }
        });
        LinearLayout.LayoutParams eciParams = matchWrap();
        eciParams.setMargins(0, dp(10), 0, 0);
        root.addView(eciButton, eciParams);

        status = text("", 14, MUTED, false);
        status.setPadding(0, dp(12), 0, dp(12));
        root.addView(status);

        queryInput = new EditText(this);
        queryInput.setSingleLine(false);
        queryInput.setMinLines(1);
        queryInput.setMaxLines(3);
        queryInput.setHint("Search name, EPIC, relative, district, AC...");
        queryInput.setTextColor(INK);
        queryInput.setHintTextColor(Color.rgb(100, 116, 139));
        queryInput.setTextSize(16);
        queryInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        queryInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        queryInput.setPadding(dp(14), dp(10), dp(14), dp(10));
        root.addView(queryInput, matchWrap());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(10), 0, dp(14));
        root.addView(actions, matchWrap());

        Button searchButton = button("Search", GREEN);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        actions.addView(searchButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button scanButton = button("Exact EPIC Scan", BLUE);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExactEpicDuplicates();
            }
        });
        LinearLayout.LayoutParams scanParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        scanParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(scanButton, scanParams);

        results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results, matchWrap());

        addInfoCard();
        return scrollView;
    }

    private void openCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_CSV_REQUEST);
    }

    private void openEciPortal() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(LiveVoterService.OFFICIAL_VOTER_SERVICES_URL));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            showToast("No browser app found to open ECI Voter Search.");
        }
    }

    private void importCsv(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                showToast("Could not open the selected file.");
                return;
            }
            List<VoterRecord> imported = CsvParser.parse(inputStream);
            inputStream.close();
            repository.replaceAll(imported);
            results.removeAllViews();
            updateStatus("Imported " + repository.size() + " voter records.");
            addResultHeader("Ready", "Search by name or EPIC number, then tap a result to find other possible registrations.");
        } catch (Exception exception) {
            showToast("CSV import failed: " + exception.getMessage());
        }
    }

    private void search() {
        if (repository.size() == 0) {
            showToast("Import a CSV first.");
            return;
        }

        results.removeAllViews();
        List<VoterRecord> matches = repository.search(queryInput.getText().toString(), 50);
        addResultHeader(matches.size() + " search results", "Tap a voter to check for matches in other places.");
        for (final VoterRecord record : matches) {
            TextView card = card(record.primaryLabel(), record.detailLabel());
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDuplicateCases(record);
                }
            });
            results.addView(card, cardParams());
        }
        if (matches.isEmpty()) {
            addResultHeader("No records found", "Try a shorter spelling, EPIC number, district, or relative name.");
        }
    }

    private void showDuplicateCases(VoterRecord record) {
        results.removeAllViews();
        List<VoterRepository.DuplicateCase> cases = repository.findDuplicateCases(record);
        addResultHeader("Checking " + record.primaryLabel(), record.detailLabel());
        if (cases.isEmpty()) {
            addResultHeader("No multi-place match found", "No exact or probable duplicate was found outside this voter's location.");
            return;
        }

        for (VoterRepository.DuplicateCase duplicateCase : cases) {
            VoterRecord candidate = duplicateCase.record;
            String detail = "Confidence score: " + duplicateCase.score + "\n"
                    + "Why: " + duplicateCase.reason + "\n\n"
                    + candidate.detailLabel();
            results.addView(card(candidate.primaryLabel(), detail), cardParams());
        }
    }

    private void showExactEpicDuplicates() {
        if (repository.size() == 0) {
            showToast("Import a CSV first.");
            return;
        }

        results.removeAllViews();
        Map<String, List<VoterRecord>> duplicates = repository.exactEpicDuplicates();
        addResultHeader(duplicates.size() + " exact EPIC duplicate groups", "Only EPIC numbers appearing in multiple locations are shown.");
        if (duplicates.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<VoterRecord>> entry : duplicates.entrySet()) {
            StringBuilder detail = new StringBuilder();
            for (VoterRecord record : entry.getValue()) {
                if (detail.length() > 0) {
                    detail.append("\n\n");
                }
                detail.append(record.primaryLabel()).append("\n").append(record.detailLabel());
            }
            results.addView(card("EPIC " + entry.getKey(), detail.toString()), cardParams());
        }
    }

    private void addInfoCard() {
        results.removeAllViews();
        addResultHeader("CSV format", "Recommended headers: name, relative_name, age, gender, epic, state, district, assembly_constituency, part_no, serial_no, address.");
        addResultHeader("Official ECI search", LiveVoterService.integrationStatus());
    }

    private void addResultHeader(String heading, String body) {
        TextView header = card(heading, body);
        results.addView(header, cardParams());
    }

    private TextView card(String heading, String body) {
        TextView view = text(heading + "\n" + body, 15, INK, false);
        view.setLineSpacing(dp(2), 1.0f);
        view.setPadding(dp(14), dp(12), dp(14), dp(12));
        view.setBackgroundColor(Color.WHITE);
        view.setTextColor(INK);
        return view;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private Button button(String value, int color) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundColor(color);
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private void updateStatus(String message) {
        status.setText(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
