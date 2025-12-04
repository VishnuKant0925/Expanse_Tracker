package com.vishnu.expensetracker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.utils.ThemeManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {
    
    private Switch switchTheme, switchNotifications, switchAutoBackup;
    private LinearLayout llThemeSelection, llDataManagement, llExportData, 
                        llImportData, llClearData, llAbout, llPrivacyPolicy;
    private TextView tvThemeStatus, tvCurrentTheme, tvAppVersion;
    private Toolbar toolbar;
    
    private ThemeManager themeManager;
    private ExpenseDatabase database;
    private ExecutorService executor;
    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        try {
            initViews();
            setupToolbar();
            setupThemeManager();
            setupClickListeners();
            loadSettings();
        } catch (Exception e) {
            e.printStackTrace();
            finish(); // Close activity if initialization fails
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchTheme = findViewById(R.id.switch_theme);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchAutoBackup = findViewById(R.id.switch_auto_backup);
        llThemeSelection = findViewById(R.id.ll_theme_selection);
        llDataManagement = findViewById(R.id.ll_data_management);
        llExportData = findViewById(R.id.ll_export_data);
        llImportData = findViewById(R.id.ll_import_data);
        llClearData = findViewById(R.id.ll_clear_data);
        llAbout = findViewById(R.id.ll_about);
        llPrivacyPolicy = findViewById(R.id.ll_privacy_policy);
        tvThemeStatus = findViewById(R.id.tv_theme_status);
        tvCurrentTheme = findViewById(R.id.tv_current_theme);
        tvAppVersion = findViewById(R.id.tv_app_version);
        
        database = ExpenseDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }
    
    private void setupThemeManager() {
        try {
            themeManager = ThemeManager.getInstance(this);
            updateThemeStatus();
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if theme manager fails
            tvCurrentTheme.setText("System Default");
            tvThemeStatus.setText("Theme: System Default");
        }
    }
    
    private void setupClickListeners() {
        // Theme toggle switch
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                themeManager.setTheme(ThemeManager.THEME_DARK);
            } else {
                themeManager.setTheme(ThemeManager.THEME_LIGHT);
            }
            updateThemeStatus();
            recreate(); // Recreate activity to apply theme
        });
        
        // Theme selection dialog
        llThemeSelection.setOnClickListener(v -> showThemeSelectionDialog());
        
        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", 
                         Toast.LENGTH_SHORT).show();
        });
        
        // Auto backup toggle
        switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("auto_backup_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Auto backup enabled" : "Auto backup disabled", 
                         Toast.LENGTH_SHORT).show();
        });
        
        // Data management options
        llExportData.setOnClickListener(v -> exportData());
        llImportData.setOnClickListener(v -> importData());
        llClearData.setOnClickListener(v -> showClearDataDialog());
        
        // About and privacy
        llAbout.setOnClickListener(v -> showAboutDialog());
        llPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
        
        // Back button
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadSettings() {
        // Load theme setting
        int currentTheme = themeManager.getCurrentTheme();
        switchTheme.setChecked(currentTheme == ThemeManager.THEME_DARK);
        
        // Load other settings
        switchNotifications.setChecked(preferences.getBoolean("notifications_enabled", true));
        switchAutoBackup.setChecked(preferences.getBoolean("auto_backup_enabled", false));
        
        // Set app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0");
        }
        
        updateThemeStatus();
    }
    
    private void updateThemeStatus() {
        try {
            if (themeManager != null) {
                String themeName = themeManager.getThemeName();
                tvCurrentTheme.setText(themeName);
                tvThemeStatus.setText("Current theme: " + themeName);
            } else {
                tvCurrentTheme.setText("System Default");
                tvThemeStatus.setText("Current theme: System Default");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvCurrentTheme.setText("System Default");
            tvThemeStatus.setText("Current theme: System Default");
        }
    }
    
    private void showThemeSelectionDialog() {
        String[] themeOptions = {"Light Mode", "Dark Mode", "System Default"};
        int currentSelection = 0;
        
        int currentTheme = themeManager.getCurrentTheme();
        switch (currentTheme) {
            case ThemeManager.THEME_LIGHT:
                currentSelection = 0;
                break;
            case ThemeManager.THEME_DARK:
                currentSelection = 1;
                break;
            case ThemeManager.THEME_SYSTEM:
                currentSelection = 2;
                break;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themeOptions, currentSelection, (dialog, which) -> {
                    int selectedTheme;
                    switch (which) {
                        case 0:
                            selectedTheme = ThemeManager.THEME_LIGHT;
                            switchTheme.setChecked(false);
                            break;
                        case 1:
                            selectedTheme = ThemeManager.THEME_DARK;
                            switchTheme.setChecked(true);
                            break;
                        case 2:
                            selectedTheme = ThemeManager.THEME_SYSTEM;
                            break;
                        default:
                            selectedTheme = ThemeManager.THEME_SYSTEM;
                    }
                    
                    themeManager.setTheme(selectedTheme);
                    updateThemeStatus();
                    dialog.dismiss();
                    recreate(); // Recreate activity to apply theme
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void exportData() {
        Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
        // TODO: Implement data export functionality
    }
    
    private void importData() {
        Toast.makeText(this, "Import feature coming soon!", Toast.LENGTH_SHORT).show();
        // TODO: Implement data import functionality
    }
    
    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all expenses and categories? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllData())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    private void clearAllData() {
        executor.execute(() -> {
            database.expenseDao().deleteAllExpenses();
            runOnUiThread(() -> {
                Toast.makeText(this, "All data cleared successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }
    
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About ExpenseTracker Pro")
                .setMessage("ExpenseTracker Pro v1.0\n\n" +
                           "A comprehensive expense tracking application with real-time balance calculation, " +
                           "category management, and detailed analytics.\n\n" +
                           "Features:\n" +
                           "• Real-time balance tracking\n" +
                           "• Category & subcategory management\n" +
                           "• Dark/Light theme support\n" +
                           "• Detailed analytics and reports\n" +
                           "• Data export/import capabilities\n\n" +
                           "Developed with ❤️ for better financial management.")
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_info)
                .show();
    }
    
    private void showPrivacyPolicyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("ExpenseTracker Pro Privacy Policy\n\n" +
                           "Your privacy is important to us. This app:\n\n" +
                           "• Stores all data locally on your device\n" +
                           "• Does not collect personal information\n" +
                           "• Does not share data with third parties\n" +
                           "• Does not require internet connection\n" +
                           "• Uses device storage only for app functionality\n\n" +
                           "All your financial data remains private and secure on your device.")
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_security)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}