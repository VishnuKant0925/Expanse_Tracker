package com.vishnu.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.adapters.ExpenseAdapter;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.models.BalanceSummary;
import com.vishnu.expensetracker.models.MonthlySummary;
import com.vishnu.expensetracker.repository.BalanceRepository;
import com.vishnu.expensetracker.utils.CurrencyFormatter;
import com.vishnu.expensetracker.utils.MonthlyUtils;
import com.vishnu.expensetracker.utils.SwipeToDeleteCallback;
import com.vishnu.expensetracker.utils.ThemeManager;
import com.vishnu.expensetracker.viewmodel.ExpenseViewModel;
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
    private ExpenseViewModel expenseViewModel;
    
    // Monthly Overview Card Views
    private View monthlyOverviewCard;
    private TextView tvMonthName, tvDaysRemaining, tvMonthlyComparison;
    private TextView tvMonthlyIncome, tvMonthlyExpenses, tvNetBalance;
    private TextView tvBudgetPercentage, tvBudgetStatus, tvDailyLimit;
    private ProgressBar progressBudget;
    
    // Variables to store current values for real-time calculation
    private double currentIncome = 0.0;
    private double currentExpenses = 0.0;
    
    // Snackbar for undo functionality
    private Snackbar undoSnackbar;
    
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
        
        // Initialize Monthly Overview Card views
        monthlyOverviewCard = findViewById(R.id.monthly_overview_card);
        tvMonthName = monthlyOverviewCard.findViewById(R.id.tv_month_name);
        tvDaysRemaining = monthlyOverviewCard.findViewById(R.id.tv_days_remaining);
        tvMonthlyComparison = monthlyOverviewCard.findViewById(R.id.tv_monthly_comparison);
        tvMonthlyIncome = monthlyOverviewCard.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpenses = monthlyOverviewCard.findViewById(R.id.tv_monthly_expenses);
        tvNetBalance = monthlyOverviewCard.findViewById(R.id.tv_net_balance);
        tvBudgetPercentage = monthlyOverviewCard.findViewById(R.id.tv_budget_percentage);
        tvBudgetStatus = monthlyOverviewCard.findViewById(R.id.tv_budget_status);
        tvDailyLimit = monthlyOverviewCard.findViewById(R.id.tv_daily_limit);
        progressBudget = monthlyOverviewCard.findViewById(R.id.progress_budget);
        
        database = ExpenseDatabase.getInstance(this);
        balanceRepository = new BalanceRepository(database.expenseDao());
        
        // Initialize ViewModel
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Set up swipe-to-delete functionality
        setupSwipeToDelete();
        
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
                deleteTransaction(expense);
            }
        });
    }
    
    /**
     * Set up swipe-to-delete gesture for RecyclerView items
     * Swipe left or right to delete a transaction
     */
    private void setupSwipeToDelete() {
        SwipeToDeleteCallback swipeCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && adapter.getExpenses() != null 
                        && position < adapter.getExpenses().size()) {
                    Expense expense = adapter.getExpenses().get(position);
                    deleteTransaction(expense);
                }
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
        
        // Observe monthly summary for Monthly Overview Card
        balanceRepository.getMonthlySummary().observe(this, this::updateMonthlyOverview);
        
        // Observe undo snackbar trigger from ViewModel
        expenseViewModel.getShowUndoSnackbar().observe(this, showUndo -> {
            if (showUndo != null && showUndo) {
                showUndoSnackbar();
            }
        });
        
        // Observe delete messages
        expenseViewModel.getDeleteMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                // Message is shown via snackbar, no need for toast
            }
        });
        
        // Observe error messages
        expenseViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
        
        // Initial load of monthly data
        balanceRepository.refreshMonthlyData();
    }
    
    /**
     * Update the Monthly Overview Card with summary data
     */
    private void updateMonthlyOverview(MonthlySummary summary) {
        if (summary == null) return;
        
        // Month name and days remaining
        tvMonthName.setText(summary.getMonthName());
        int daysRemaining = MonthlyUtils.getDaysRemainingInMonth();
        tvDaysRemaining.setText(daysRemaining + " days left");
        
        // Monthly comparison message
        tvMonthlyComparison.setText(summary.getComparisonMessage());
        
        // Income and expenses
        tvMonthlyIncome.setText(CurrencyFormatter.formatCurrency(summary.getTotalIncome()));
        tvMonthlyExpenses.setText(CurrencyFormatter.formatCurrency(summary.getTotalExpenses()));
        
        // Net balance with color
        double netBalance = summary.getNetBalance();
        tvNetBalance.setText(CurrencyFormatter.formatCurrency(Math.abs(netBalance)));
        if (netBalance >= 0) {
            tvNetBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvNetBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Budget progress
        int budgetPercent = summary.getBudgetUsagePercent();
        progressBudget.setProgress(Math.min(budgetPercent, 100));
        tvBudgetPercentage.setText(budgetPercent + "%");
        
        // Budget status message and color
        tvBudgetStatus.setText(summary.getBudgetStatusMessage());
        updateBudgetProgressColor(budgetPercent);
        
        // Daily spending limit
        double dailyLimit = summary.getDailySpendingLimit();
        if (dailyLimit > 0 && daysRemaining > 0) {
            tvDailyLimit.setText("💡 Daily spending limit: " + 
                    CurrencyFormatter.formatCurrency(dailyLimit) + " to stay on budget");
            tvDailyLimit.setVisibility(View.VISIBLE);
        } else if (summary.getBudgetRemaining() < 0) {
            tvDailyLimit.setText("❌ You've exceeded your budget!");
            tvDailyLimit.setVisibility(View.VISIBLE);
        } else {
            tvDailyLimit.setVisibility(View.GONE);
        }
    }
    
    /**
     * Update budget progress bar color based on usage percentage
     */
    private void updateBudgetProgressColor(int percent) {
        int colorRes;
        if (percent < 50) {
            colorRes = R.color.budget_safe;
        } else if (percent < 75) {
            colorRes = R.color.budget_warning;
        } else if (percent < 100) {
            colorRes = R.color.budget_danger;
        } else {
            colorRes = R.color.budget_over;
        }
        progressBudget.setProgressTintList(
                android.content.res.ColorStateList.valueOf(getResources().getColor(colorRes)));
    }
    
    /**
     * Show snackbar with undo option after deletion
     */
    private void showUndoSnackbar() {
        undoSnackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Transaction deleted",
                Snackbar.LENGTH_LONG
        );
        undoSnackbar.setAction("UNDO", v -> {
            expenseViewModel.undoDelete();
        });
        undoSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                    // User didn't click undo, clear the undo state
                    expenseViewModel.clearUndoState();
                }
            }
        });
        undoSnackbar.show();
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
        // Also refresh monthly data
        balanceRepository.refreshMonthlyData();
    }
    
    /**
     * Show confirmation dialog before deleting a transaction
     * Note: This is now handled in the adapter with MaterialAlertDialogBuilder
     * This method is kept for backward compatibility
     */
    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?\n\n" +
                        "📝 " + expense.getTitle() + "\n" +
                        "💰 " + CurrencyFormatter.formatCurrency(expense.getAmount()) + "\n" +
                        "📁 " + expense.getCategory())
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
     * Delete transaction using soft delete (can be undone)
     * Uses ViewModel for proper lifecycle management and undo functionality
     */
    private void deleteTransaction(Expense expense) {
        // Use ViewModel's soft delete with undo capability
        expenseViewModel.softDeleteTransaction(expense);
        
        // Refresh monthly data after deletion
        balanceRepository.refreshMonthlyData();
    }
}