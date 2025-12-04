package com.vishnu.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.adapters.ExpenseAdapter;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.models.BalanceSummary;
import com.vishnu.expensetracker.repository.BalanceRepository;
import com.vishnu.expensetracker.utils.CurrencyFormatter;
import com.vishnu.expensetracker.utils.ThemeManager;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    
    private static final int REQUEST_CODE_EDIT_TRANSACTION = 100;
    
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpense;
    private FloatingActionButton fabAddExpense;
    private BottomNavigationView bottomNavigation;
    private ExpenseDatabase database;
    private BalanceRepository balanceRepository;
    private ThemeManager themeManager;
    
    // Variables to store current values for real-time calculation
    private double currentIncome = 0.0;
    private double currentExpenses = 0.0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_expenses);
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        fabAddExpense = findViewById(R.id.fab_add_expense);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        database = ExpenseDatabase.getInstance(this);
        balanceRepository = new BalanceRepository(database.expenseDao());
    }
    
    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Set up expense item click listeners
        adapter.setOnExpenseClickListener(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                // Handle expense item click (optional - could show details)
            }
            
            @Override
            public void onExpenseLongClick(Expense expense) {
                // Handle long click (optional - could show context menu)
            }
            
            @Override
            public void onEditClick(Expense expense) {
                try {
                    Intent intent = new Intent(MainActivity.this, EditTransactionActivity.class);
                    intent.putExtra(EditTransactionActivity.EXTRA_EXPENSE_ID, expense.getId());
                    startActivityForResult(intent, REQUEST_CODE_EDIT_TRANSACTION);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error opening edit screen", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onDeleteClick(Expense expense) {
                showDeleteConfirmationDialog(expense);
            }
        });
    }
    
    private void setupClickListeners() {
        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already on home
                return true;
            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
    
    private void loadData() {
        // Load expenses
        database.expenseDao().getAllExpenses().observe(this, expenses -> {
            if (expenses != null) {
                adapter.updateExpenses(expenses);
            }
        });
        
        // Load comprehensive balance summary with real-time updates
        balanceRepository.getBalanceSummary().observe(this, balanceSummary -> {
            if (balanceSummary != null) {
                updateBalanceDisplay(balanceSummary);
            }
        });
        
        // Also observe individual components for immediate updates
        balanceRepository.getTotalIncome().observe(this, income -> {
            currentIncome = income != null ? income : 0.0;
            tvTotalIncome.setText(CurrencyFormatter.formatCurrency(currentIncome));
        });
        
        balanceRepository.getTotalExpenses().observe(this, expenses -> {
            currentExpenses = expenses != null ? expenses : 0.0;
            tvTotalExpense.setText(CurrencyFormatter.formatCurrency(currentExpenses));
        });
    }
    
    private void updateBalanceDisplay(BalanceSummary balanceSummary) {
        // Update balance amount
        tvTotalBalance.setText(CurrencyFormatter.formatCurrency(balanceSummary.getCurrentBalance()));
        
        // Update balance color based on positive/negative
        if (balanceSummary.isBalancePositive()) {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        
        // Update individual amounts with color coding
        tvTotalIncome.setText(CurrencyFormatter.formatCurrency(balanceSummary.getTotalIncome()));
        tvTotalExpense.setText(CurrencyFormatter.formatCurrency(balanceSummary.getTotalExpenses()));
        
        // Optional: Show additional balance information via toast or log
        if (balanceSummary.getTotalIncome() > 0) {
            double savingsRate = balanceSummary.getSavingsRate();
            // You could show this in a TextView or as a notification
            // For now, we'll just log it
            android.util.Log.d("Balance", "Savings Rate: " + String.format("%.1f%%", savingsRate));
        }
    }
    
    private void updateBalance() {
        // Calculate real-time balance: Income - Expenses
        double balance = currentIncome - currentExpenses;
        tvTotalBalance.setText(CurrencyFormatter.formatCurrency(balance));
        
        // Change color based on balance (positive = white, negative = red)
        if (balance >= 0) {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Implement search functionality
            return true;
        } else if (id == R.id.action_filter) {
            // Implement filter functionality
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_EDIT_TRANSACTION && resultCode == RESULT_OK) {
            // Transaction was updated successfully, refresh the data
            loadData();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadData();
    }
    
    /**
     * Show confirmation dialog before deleting a transaction
     */
    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?\n\n" +
                        "Title: " + expense.getTitle() + "\n" +
                        "Amount: " + CurrencyFormatter.formatCurrency(expense.getAmount()) + "\n" +
                        "Category: " + expense.getCategory())
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(expense);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    /**
     * Delete transaction from database
     */
    private void deleteTransaction(Expense expense) {
        // Show a progress toast
        Toast.makeText(this, "Deleting transaction...", Toast.LENGTH_SHORT).show();
        
        // Execute delete operation in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                database.expenseDao().delete(expense);
                
                // Show success message on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                            "Transaction deleted successfully", 
                            Toast.LENGTH_SHORT).show();
                    // Data will refresh automatically through LiveData observers
                });
            } catch (Exception e) {
                // Show error message on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                            "Error deleting transaction: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}