package com.localise.translate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

/** @noinspection MismatchedQueryAndUpdateOfCollection*/
public class MainActivity extends AppCompatActivity {

    private TextView status;
    private BufferedReader reader;
    private final ArrayList<String> lines = new ArrayList<>();
    private final ArrayList<String> titles = new ArrayList<>();
    private final ArrayList<String> strings = new ArrayList<>();
    private final ArrayList<String> translations = new ArrayList<>();
    private final String suffix = "</string>";
    private String path;
    private int lineCount;
    private final ArrayList<String> results = new ArrayList<>();
    private final String[] languages = new String[]{
            "ar",
            "bg", "bn",
            "ca", "zh-rCN", "zh-rTW", "hr", "cs",
            "da", "nl",
            "en",
            "fi", "fr",
            "de", "el",
            "iw", "hi", "hu",
            "in", "it",
            "ja", "ko",
            "lv", "lt",
            "no",
            "pl", "pt",
            "ro", "ru",
            "sr", "sk", "sl", "es", "sv",
            "ta", "te", "th", "tr",
            "uk", "ur",
            "vi"};


    /*
    ar - arabic,
    bg - bulgarian, bn - bengali,
    ca - catalan, zh-rCN - chinese-simplified, zh-eTW - chinese-traditional,
    hr-croatian, cs-czech,
    da-danish, nl-dutch,
    en-english,
    fi-finnish, fr-french,
    de-german, el-greek,
    iw-hebrew, hi-hindi, hu-hungarian,
    in-indonesian, it-italian,
    ja-japanese
    ko-korean
    lv-latvian, lt-lithuvian
    no-norwagian
    pl-polish, pt-portugese
    ro-romanian, ru-russian
    sr-serbian, sk-slovak, sl-slovenian, es-spanish, sv-swedish
    ta-tamil, te-telugu, th-thai, tr-turkish
    uk-ukranian, ur-urdu,
    vi-viatnamese
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        path = Objects.requireNonNull(this.getExternalFilesDir(DOWNLOAD_SERVICE)).getAbsolutePath();
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        status = findViewById(R.id.status);

        readSourceFile();


    }

    @SuppressLint("SetTextI18n")
    private void storeValues() {
        status.setText(status.getText().toString()+" Checking Write Permission ✔\n");
        Log.d("MainActivity", "storeValues: started");
        int count = 0;
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                status.setText(status.getText().toString()+" Permission Granted ✔\n");
                status.setText(status.getText().toString()+" Writing Files to Storage ✔\n");
                for (String s : languages) {
                    String prefix = "values-";
                    new File(path, prefix + s);
                    try {
                        File outPut = new File(path, prefix + s + File.separator + "strings.xml");
                        File parent = outPut.getParentFile();
                        if(parent!=null && (parent.exists() || parent.mkdirs())) {
                            Log.d("storeValues", "storeValues: " + outPut.getAbsolutePath());
                            FileWriter out = new FileWriter(outPut);
                            out.write(results.get(count));
                            out.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                status.setText(status.getText().toString()+" Files stored to storage ✔\n");
                status.setText(status.getText().toString()+" \n\nfind files from \n\n"+path);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Manifest.permission.WRITE_EXTERNAL_STORAGE"}, 101);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void startTranslating() {
        status.setText(status.getText().toString()+" Generating translated String.xml files ✔\n");
        int count = 0;
        StringBuilder builder = new StringBuilder("<resources>\n");
        for (String s : translations) {
            count++;
            if (count < lineCount)
                builder.append(titles.get(count-1)).append(s).append(suffix).append("\n");
            else if (count == lineCount) {
                builder.append(titles.get(count-1)).append(s).append(suffix).append("\n").append("</resources>");
                count = 0;
                results.add(builder.toString());
                builder = new StringBuilder("<resources>\n");
            }

        }
        storeValues();
    }

    @SuppressLint("SetTextI18n")
    private void readTranslationFile() {
        status.setText(status.getText().toString()+" Reading Translated Strings ✔\n");
        try {
            final InputStream file = getAssets().open("translations.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while (line != null) {
                translations.add(line);
                line = reader.readLine();
                Log.d("MainActivity", "readTranslationFile: ");
            }
            //Toast.makeText(this, "total translations found" + lineCount + " strings found in Source", Toast.LENGTH_SHORT).show();
            startTranslating();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private void readSourceFile() {
        status.setText("Reading Source Strings ✔\n");
        try {
            final InputStream file = getAssets().open("source");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while (line != null) {
                lineCount++;
                lines.add(line);
                String sorted = line.replace(suffix, "").substring(line.indexOf("\">")).replace("\">", "");
                strings.add(sorted);
                titles.add(line.replace(suffix, "").replace(sorted, ""));
                Log.d("reading source", ": " + sorted);
                line = reader.readLine();
            }
            //Toast.makeText(this, "total " + lineCount + " strings found in Source", Toast.LENGTH_SHORT).show();
            readTranslationFile();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            storeValues();
        }
    }
}
