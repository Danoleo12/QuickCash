package com.example.development_01.core.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;

import com.google.android.material.button.MaterialButton;

public class ResumeViewerActivity extends AppCompatActivity {

    protected String resumeUrl;
    protected String applicantName;
    protected WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_viewer);

        resumeUrl = getIntent().getStringExtra("RESUME_URL");
        applicantName = getIntent().getStringExtra("APPLICANT_NAME");

        TextView tvHeader = findViewById(R.id.tvResumeHeader);
        webView = findViewById(R.id.resumeWebView);
        MaterialButton btnDownload = findViewById(R.id.btnDownloadResume);
        ImageButton btnBack = findViewById(R.id.btnBackResume);

        String headerText = getString(R.string.resume_header_format, (applicantName != null ? applicantName : "Applicant"));
        tvHeader.setText(headerText);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadResume();
            }
        });

        this.setupWebView();
    }

    /**
     * load the resume in webview
     */
    protected void setupWebView() {
        if (resumeUrl == null || resumeUrl.isEmpty()) {
            Toast.makeText(this, "Error: No link", Toast.LENGTH_SHORT).show();
            return;
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient());

        String googleDocsUrl = "https://docs.google.com/viewer?embedded=true&url=" + resumeUrl;
        webView.loadUrl(googleDocsUrl);
    }

    /**
     * trigger download manager for the resume
     */
    protected void downloadResume() {
        if (resumeUrl == null || resumeUrl.isEmpty()) {
            Toast.makeText(this, "Error: URL missing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse(resumeUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            
            String fileName = applicantName != null ? 
                    applicantName.replaceAll("\\s+", "_") + "_Resume.pdf" : 
                    URLUtil.guessFileName(resumeUrl, null, "application/pdf");
            
            request.setTitle(getString(R.string.downloading_resume));
            request.setDescription(getString(R.string.downloading_resume_desc, applicantName));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}