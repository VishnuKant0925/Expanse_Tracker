package com.vishnu.expensetracker.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.utils.ThemeManager;

/**
 * Analytics Activity - Coming Soon
 * This activity will display charts and analytics for expense tracking
 */
public class AnalyticsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private ThemeManager themeManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Apply theme before setContentView
            themeManager = ThemeManager.getInstance(this);
            if (themeManager != null) {
                themeManager.applyTheme();
            }
            
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_analytics);
            
            setupToolbar();
            
            // Show message that advanced features are coming soon
            Toast.makeText(this, "Analytics features coming soon!", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading Analytics", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Analytics");
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
