package com.vishnu.expensetracker.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.database.ExpenseDao;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.utils.DateUtils;
import com.vishnu.expensetracker.utils.ThemeManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Analytics Activity - Comprehensive spending analytics with visualizations
 * Features:
 * - Daily spending trend (Line Chart with budget limit line)
 * - Weekly comparison (Bar Chart)
 * - Needs vs Wants (Pie Chart)
 * - Top spending categories
 * - Quick insights
 * - Month-over-month comparison
 */
public class AnalyticsActivity extends AppCompatActivity {
    
    // Views
    private Toolbar toolbar;
    private TextView tvCurrentMonth;
    private ImageButton btnPreviousMonth, btnNextMonth;
    
    // Financial Overview
    private TextView tvTotalIncome, tvTotalExpenses, tvCurrentBalance;
    private TextView tvSavingsRate;
    private ProgressBar progressSavings;
    
    // Charts
    private LineChart chartDailySpending;
    private BarChart chartWeeklyComparison;
    private PieChart chartNeedsWants;
    
    // Daily Spending Card
    private TextView tvDailyAvg, tvSafeZoneLabel;
    
    // Weekly Comparison Card
    private TextView tvBestWeek, tvHighestWeek;
    
    // Needs vs Wants Card
    private TextView tvNeedsAmount, tvWantsAmount, tvSpendingTip;
    
    // Top Categories
    private LinearLayout layoutTopCategories;
    private TextView tvNoCategories;
    
    // Quick Insights
    private TextView tvTransactionCount, tvHighestExpense, tvHighestExpenseCategory;
    private TextView tvAvgDailyExpense, tvDaysRemaining;
    
    // Month-over-Month
    private TextView tvThisMonthExpense, tvLastMonthExpense, tvMomChange;
    
    // Loading
    private FrameLayout layoutLoading;
    
    // Data
    private ThemeManager themeManager;
    private ExpenseDao expenseDao;
    private ExecutorService executorService;
    
    // Current viewing month
    private int currentYear;
    private int currentMonth;
    
    // Colors
    private int colorIncome;
    private int colorExpense;
    private int colorPrimary;
    private int colorTextSecondary;
    
    // Budget (can be set by user later)
    private double monthlyBudget = 10000.0; // Default budget
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    
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
            
            // Initialize colors
            initColors();
            
            // Initialize database
            expenseDao = ExpenseDatabase.getInstance(this).expenseDao();
            executorService = Executors.newSingleThreadExecutor();
            
            // Initialize current month
            Calendar calendar = Calendar.getInstance();
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH);
            
            // Initialize views
            initViews();
            setupToolbar();
            setupMonthNavigation();
            setupCharts();
            
            // Load data
            loadAnalyticsData();
            
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            // Don't finish, show what we can
        }
    }
    
    private void initColors() {
        colorIncome = ContextCompat.getColor(this, R.color.income_color);
        colorExpense = ContextCompat.getColor(this, R.color.expense_color);
        colorPrimary = ContextCompat.getColor(this, R.color.primary_color);
        colorTextSecondary = ContextCompat.getColor(this, R.color.text_secondary);
    }
    
    private void initViews() {
        // Month Selector
        tvCurrentMonth = findViewById(R.id.tv_current_month);
        btnPreviousMonth = findViewById(R.id.btn_previous_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        
        // Financial Overview
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        tvCurrentBalance = findViewById(R.id.tv_current_balance);
        tvSavingsRate = findViewById(R.id.tv_savings_rate);
        progressSavings = findViewById(R.id.progress_savings);
        
        // Charts
        chartDailySpending = findViewById(R.id.chart_daily_spending);
        chartWeeklyComparison = findViewById(R.id.chart_weekly_comparison);
        chartNeedsWants = findViewById(R.id.chart_needs_wants);
        
        // Daily Spending Card
        tvDailyAvg = findViewById(R.id.tv_daily_avg);
        tvSafeZoneLabel = findViewById(R.id.tv_safe_zone_label);
        
        // Weekly Comparison Card
        tvBestWeek = findViewById(R.id.tv_best_week);
        tvHighestWeek = findViewById(R.id.tv_highest_week);
        
        // Needs vs Wants Card
        tvNeedsAmount = findViewById(R.id.tv_needs_amount);
        tvWantsAmount = findViewById(R.id.tv_wants_amount);
        tvSpendingTip = findViewById(R.id.tv_spending_tip);
        
        // Top Categories
        layoutTopCategories = findViewById(R.id.layout_top_categories);
        tvNoCategories = findViewById(R.id.tv_no_categories);
        
        // Quick Insights
        tvTransactionCount = findViewById(R.id.tv_transaction_count);
        tvHighestExpense = findViewById(R.id.tv_highest_expense);
        tvHighestExpenseCategory = findViewById(R.id.tv_highest_expense_category);
        tvAvgDailyExpense = findViewById(R.id.tv_avg_daily_expense);
        tvDaysRemaining = findViewById(R.id.tv_days_remaining);
        
        // Month-over-Month
        tvThisMonthExpense = findViewById(R.id.tv_this_month_expense);
        tvLastMonthExpense = findViewById(R.id.tv_last_month_expense);
        tvMomChange = findViewById(R.id.tv_mom_change);
        
        // Loading
        layoutLoading = findViewById(R.id.layout_loading);
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Analytics");
                }
            } catch (Exception e) {
                // Theme already has action bar, just setup click listener
                e.printStackTrace();
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
    
    private void setupMonthNavigation() {
        updateMonthDisplay();
        
        if (btnPreviousMonth != null) {
            btnPreviousMonth.setOnClickListener(v -> {
                currentMonth--;
                if (currentMonth < 0) {
                    currentMonth = 11;
                    currentYear--;
                }
                updateMonthDisplay();
                loadAnalyticsData();
            });
        }
        
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();
                // Don't allow future months
                if (currentYear == now.get(Calendar.YEAR) && currentMonth >= now.get(Calendar.MONTH)) {
                    return;
                }
                currentMonth++;
                if (currentMonth > 11) {
                    currentMonth = 0;
                    currentYear++;
                }
                updateMonthDisplay();
                loadAnalyticsData();
            });
        }
    }
    
    private void updateMonthDisplay() {
        if (tvCurrentMonth == null) return;
        
        String monthName = DateUtils.getMonthName(currentMonth);
        tvCurrentMonth.setText(monthName + " " + currentYear);
        
        // Disable next button if current month
        Calendar now = Calendar.getInstance();
        boolean isCurrentMonth = currentYear == now.get(Calendar.YEAR) && currentMonth == now.get(Calendar.MONTH);
        if (btnNextMonth != null) {
            btnNextMonth.setAlpha(isCurrentMonth ? 0.3f : 1.0f);
            btnNextMonth.setEnabled(!isCurrentMonth);
        }
    }
    
    private void setupCharts() {
        // Line Chart Setup
        setupLineChart();
        
        // Bar Chart Setup
        setupBarChart();
        
        // Pie Chart Setup
        setupPieChart();
    }
    
    private void setupLineChart() {
        if (chartDailySpending == null) return;
        
        chartDailySpending.getDescription().setEnabled(false);
        chartDailySpending.setTouchEnabled(true);
        chartDailySpending.setDragEnabled(true);
        chartDailySpending.setScaleEnabled(false);
        chartDailySpending.setPinchZoom(false);
        chartDailySpending.setDrawGridBackground(false);
        chartDailySpending.getLegend().setEnabled(false);
        
        XAxis xAxis = chartDailySpending.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(colorTextSecondary);
        
        YAxis leftAxis = chartDailySpending.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(colorTextSecondary);
        leftAxis.setAxisMinimum(0f);
        
        chartDailySpending.getAxisRight().setEnabled(false);
        chartDailySpending.setExtraBottomOffset(10f);
    }
    
    private void setupBarChart() {
        if (chartWeeklyComparison == null) return;
        
        chartWeeklyComparison.getDescription().setEnabled(false);
        chartWeeklyComparison.setTouchEnabled(true);
        chartWeeklyComparison.setDragEnabled(false);
        chartWeeklyComparison.setScaleEnabled(false);
        chartWeeklyComparison.setPinchZoom(false);
        chartWeeklyComparison.setDrawGridBackground(false);
        chartWeeklyComparison.getLegend().setEnabled(false);
        chartWeeklyComparison.setFitBars(true);
        
        XAxis xAxis = chartWeeklyComparison.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(colorTextSecondary);
        
        YAxis leftAxis = chartWeeklyComparison.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(colorTextSecondary);
        leftAxis.setAxisMinimum(0f);
        
        chartWeeklyComparison.getAxisRight().setEnabled(false);
        chartWeeklyComparison.setExtraBottomOffset(10f);
    }
    
    private void setupPieChart() {
        if (chartNeedsWants == null) return;
        
        chartNeedsWants.getDescription().setEnabled(false);
        chartNeedsWants.setUsePercentValues(true);
        chartNeedsWants.setDrawHoleEnabled(true);
        chartNeedsWants.setHoleColor(Color.TRANSPARENT);
        chartNeedsWants.setHoleRadius(60f);
        chartNeedsWants.setTransparentCircleRadius(65f);
        chartNeedsWants.setDrawCenterText(true);
        chartNeedsWants.setCenterTextSize(14f);
        chartNeedsWants.setCenterTextColor(colorTextSecondary);
        chartNeedsWants.setRotationEnabled(true);
        chartNeedsWants.setHighlightPerTapEnabled(true);
        chartNeedsWants.getLegend().setEnabled(false);
        chartNeedsWants.setEntryLabelColor(Color.WHITE);
        chartNeedsWants.setEntryLabelTextSize(10f);
    }
    
    private void loadAnalyticsData() {
        showLoading(true);
        
        executorService.execute(() -> {
            try {
                // Get date range for selected month
                long startOfMonth = DateUtils.getStartOfMonth(currentYear, currentMonth);
                long endOfMonth = DateUtils.getEndOfMonth(currentYear, currentMonth);
                
                // Load all analytics data
                double totalIncome = expenseDao.getTotalByTypeAndDateRange("income", startOfMonth, endOfMonth);
                double totalExpenses = expenseDao.getTotalByTypeAndDateRange("expense", startOfMonth, endOfMonth);
                double balance = totalIncome - totalExpenses;
                
                // Daily spending data
                List<ExpenseDao.DailyExpenseSum> dailyExpenses = expenseDao.getDailyExpenseTotals(startOfMonth, endOfMonth);
                double avgDaily = totalExpenses > 0 ? expenseDao.getAverageDailyExpense(startOfMonth, endOfMonth) : 0;
                
                // Weekly data
                long fourWeeksAgo = DateUtils.getStartOfFourWeeksAgo();
                List<ExpenseDao.WeeklyExpenseSum> weeklyExpenses = expenseDao.getWeeklyExpenseTotals(fourWeeksAgo, endOfMonth);
                
                // Needs vs Wants
                List<ExpenseDao.NeedsWantsSum> needsWants = expenseDao.getNeedsVsWantsTotals(startOfMonth, endOfMonth);
                
                // Top categories
                List<ExpenseDao.CategoryExpenseSum> topCategories = expenseDao.getTopSpendingCategories(startOfMonth, endOfMonth, 5);
                
                // Insights
                int transactionCount = expenseDao.getTransactionCount("expense", startOfMonth, endOfMonth);
                Expense highestExpense = expenseDao.getHighestExpense(startOfMonth, endOfMonth);
                
                // Last month data for comparison
                Calendar lastMonthCal = Calendar.getInstance();
                lastMonthCal.set(Calendar.YEAR, currentYear);
                lastMonthCal.set(Calendar.MONTH, currentMonth);
                lastMonthCal.add(Calendar.MONTH, -1);
                long startOfLastMonth = DateUtils.getStartOfMonth(lastMonthCal.get(Calendar.YEAR), lastMonthCal.get(Calendar.MONTH));
                long endOfLastMonth = DateUtils.getEndOfMonth(lastMonthCal.get(Calendar.YEAR), lastMonthCal.get(Calendar.MONTH));
                double lastMonthExpenses = expenseDao.getTotalByTypeAndDateRange("expense", startOfLastMonth, endOfLastMonth);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateFinancialOverview(totalIncome, totalExpenses, balance);
                    updateDailySpendingChart(dailyExpenses, avgDaily);
                    updateWeeklyComparisonChart(weeklyExpenses);
                    updateNeedsWantsChart(needsWants, totalExpenses);
                    updateTopCategories(topCategories, totalExpenses);
                    updateQuickInsights(transactionCount, highestExpense, avgDaily);
                    updateMonthOverMonth(totalExpenses, lastMonthExpenses);
                    showLoading(false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Error loading analytics data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateFinancialOverview(double income, double expenses, double balance) {
        if (tvTotalIncome != null) tvTotalIncome.setText(formatCurrency(income));
        if (tvTotalExpenses != null) tvTotalExpenses.setText(formatCurrency(expenses));
        if (tvCurrentBalance != null) tvCurrentBalance.setText(formatCurrency(balance));
        
        // Calculate savings rate
        double savingsRate = income > 0 ? ((income - expenses) / income) * 100 : 0;
        savingsRate = Math.max(0, Math.min(100, savingsRate)); // Clamp between 0-100
        
        if (tvSavingsRate != null) {
            tvSavingsRate.setText(String.format(Locale.getDefault(), "%.1f%%", savingsRate));
            // Color based on savings rate
            if (savingsRate >= 20) {
                tvSavingsRate.setTextColor(colorIncome);
            } else if (savingsRate > 0) {
                tvSavingsRate.setTextColor(colorPrimary);
            } else {
                tvSavingsRate.setTextColor(colorExpense);
            }
        }
        if (progressSavings != null) progressSavings.setProgress((int) savingsRate);
    }
    
    private void updateDailySpendingChart(List<ExpenseDao.DailyExpenseSum> dailyExpenses, double avgDaily) {
        // Update average display
        tvDailyAvg.setText(String.format("Avg: %s/day", formatCurrency(avgDaily)));
        
        // Calculate daily budget
        int daysInMonth = DateUtils.getDaysInMonth(currentYear, currentMonth);
        double dailyBudget = monthlyBudget / daysInMonth;
        tvSafeZoneLabel.setText(String.format("Safe Zone: %s/day budget limit", formatCurrency(dailyBudget)));
        
        // Prepare data entries
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Create entries for all days
        for (int day = 1; day <= daysInMonth; day++) {
            float amount = 0f;
            for (ExpenseDao.DailyExpenseSum dailySum : dailyExpenses) {
                int dayNum = DateUtils.parseDayFromDateString(dailySum.dateStr);
                if (dayNum == day) {
                    amount = (float) dailySum.total;
                    break;
                }
            }
            entries.add(new Entry(day, amount));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Daily Spending");
        dataSet.setColor(colorExpense);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(colorExpense);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colorExpense);
        dataSet.setFillAlpha(30);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        // Add limit line for budget
        YAxis leftAxis = chartDailySpending.getAxisLeft();
        leftAxis.removeAllLimitLines();
        
        LimitLine budgetLine = new LimitLine((float) dailyBudget, "Budget");
        budgetLine.setLineWidth(2f);
        budgetLine.setLineColor(colorIncome);
        budgetLine.enableDashedLine(10f, 10f, 0f);
        budgetLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        budgetLine.setTextSize(10f);
        budgetLine.setTextColor(colorIncome);
        leftAxis.addLimitLine(budgetLine);
        
        // Set X-axis labels
        XAxis xAxis = chartDailySpending.getXAxis();
        xAxis.setLabelCount(7, true);
        
        LineData lineData = new LineData(dataSet);
        chartDailySpending.setData(lineData);
        chartDailySpending.invalidate();
        chartDailySpending.animateX(1000);
    }
    
    private void updateWeeklyComparisonChart(List<ExpenseDao.WeeklyExpenseSum> weeklyExpenses) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        double bestWeek = Double.MAX_VALUE;
        double highestWeek = 0;
        
        int index = 0;
        for (int i = Math.max(0, weeklyExpenses.size() - 4); i < weeklyExpenses.size(); i++) {
            ExpenseDao.WeeklyExpenseSum weekSum = weeklyExpenses.get(i);
            entries.add(new BarEntry(index, (float) weekSum.total));
            labels.add("Week " + (index + 1));
            
            if (weekSum.total > 0 && weekSum.total < bestWeek) {
                bestWeek = weekSum.total;
            }
            if (weekSum.total > highestWeek) {
                highestWeek = weekSum.total;
            }
            index++;
        }
        
        // Fill remaining weeks if less than 4
        while (entries.size() < 4) {
            entries.add(new BarEntry(entries.size(), 0));
            labels.add("Week " + (entries.size()));
        }
        
        // Update stats
        tvBestWeek.setText(bestWeek < Double.MAX_VALUE ? formatCurrency(bestWeek) : "₹0");
        tvHighestWeek.setText(formatCurrency(highestWeek));
        
        BarDataSet dataSet = new BarDataSet(entries, "Weekly Spending");
        
        // Create gradient colors (light to dark based on value)
        int[] colors = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            float ratio = highestWeek > 0 ? entries.get(i).getY() / (float) highestWeek : 0;
            colors[i] = blendColors(Color.parseColor("#81C784"), colorExpense, ratio);
        }
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(colorTextSecondary);
        dataSet.setValueTextSize(10f);
        
        XAxis xAxis = chartWeeklyComparison.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        chartWeeklyComparison.setData(barData);
        chartWeeklyComparison.invalidate();
        chartWeeklyComparison.animateY(1000);
    }
    
    private void updateNeedsWantsChart(List<ExpenseDao.NeedsWantsSum> needsWants, double totalExpenses) {
        double needsTotal = 0;
        double wantsTotal = 0;
        
        for (ExpenseDao.NeedsWantsSum sum : needsWants) {
            if (sum.isEssential) {
                needsTotal = sum.total;
            } else {
                wantsTotal = sum.total;
            }
        }
        
        // If no data, use total as needs (default behavior)
        if (needsTotal == 0 && wantsTotal == 0 && totalExpenses > 0) {
            needsTotal = totalExpenses;
        }
        
        double total = needsTotal + wantsTotal;
        double needsPercent = total > 0 ? (needsTotal / total) * 100 : 0;
        double wantsPercent = total > 0 ? (wantsTotal / total) * 100 : 0;
        
        // Update text displays
        tvNeedsAmount.setText(String.format("%s (%.0f%%)", formatCurrency(needsTotal), needsPercent));
        tvWantsAmount.setText(String.format("%s (%.0f%%)", formatCurrency(wantsTotal), wantsPercent));
        
        // Update center text
        chartNeedsWants.setCenterText(String.format("Total\n%s", formatCurrency(total)));
        
        // Generate spending tip
        updateSpendingTip(needsPercent, wantsPercent);
        
        // Create pie entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (needsTotal > 0) {
            entries.add(new PieEntry((float) needsTotal, "Needs"));
        }
        if (wantsTotal > 0) {
            entries.add(new PieEntry((float) wantsTotal, "Wants"));
        }
        
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Data"));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Needs vs Wants");
        
        // Set colors
        ArrayList<Integer> colors = new ArrayList<>();
        if (needsTotal > 0) colors.add(colorIncome);
        if (wantsTotal > 0) colors.add(colorExpense);
        if (colors.isEmpty()) colors.add(Color.GRAY);
        dataSet.setColors(colors);
        
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(false);
        
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chartNeedsWants));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);
        
        chartNeedsWants.setData(pieData);
        chartNeedsWants.invalidate();
        chartNeedsWants.animateY(1000);
    }
    
    private void updateSpendingTip(double needsPercent, double wantsPercent) {
        String tip;
        if (wantsPercent > 50) {
            tip = "Consider reducing non-essential spending to increase savings!";
        } else if (wantsPercent > 30) {
            tip = "Good balance! Try to keep wants under 30% for better savings.";
        } else if (wantsPercent > 0) {
            tip = "Excellent! You're managing your wants vs needs very well!";
        } else {
            tip = "Mark expenses as 'Essential' or 'Non-Essential' when adding them.";
        }
        tvSpendingTip.setText(tip);
    }
    
    private void updateTopCategories(List<ExpenseDao.CategoryExpenseSum> categories, double totalExpenses) {
        layoutTopCategories.removeAllViews();
        
        if (categories == null || categories.isEmpty()) {
            tvNoCategories.setVisibility(View.VISIBLE);
            return;
        }
        
        tvNoCategories.setVisibility(View.GONE);
        
        int[] rankColors = {
                Color.parseColor("#FFD700"), // Gold
                Color.parseColor("#C0C0C0"), // Silver
                Color.parseColor("#CD7F32"), // Bronze
                colorPrimary,
                colorPrimary
        };
        
        for (int i = 0; i < categories.size(); i++) {
            ExpenseDao.CategoryExpenseSum category = categories.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_category_analytics, layoutTopCategories, false);
            
            TextView tvRank = itemView.findViewById(R.id.tv_rank);
            TextView tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            TextView tvCategoryAmount = itemView.findViewById(R.id.tv_category_amount);
            ProgressBar progressCategory = itemView.findViewById(R.id.progress_category);
            
            tvRank.setText(String.valueOf(i + 1));
            tvRank.setTextColor(rankColors[i]);
            tvCategoryName.setText(category.category);
            tvCategoryAmount.setText(formatCurrency(category.total));
            
            int percentage = totalExpenses > 0 ? (int) ((category.total / totalExpenses) * 100) : 0;
            progressCategory.setProgress(percentage);
            
            layoutTopCategories.addView(itemView);
        }
    }
    
    private void updateQuickInsights(int transactionCount, Expense highestExpense, double avgDaily) {
        tvTransactionCount.setText(String.valueOf(transactionCount));
        tvAvgDailyExpense.setText(formatCurrency(avgDaily));
        
        if (highestExpense != null) {
            tvHighestExpense.setText(formatCurrency(highestExpense.getAmount()));
            tvHighestExpenseCategory.setText(highestExpense.getCategory());
        } else {
            tvHighestExpense.setText("₹0");
            tvHighestExpenseCategory.setText("No expenses");
        }
        
        // Days remaining (only for current month)
        Calendar now = Calendar.getInstance();
        if (currentYear == now.get(Calendar.YEAR) && currentMonth == now.get(Calendar.MONTH)) {
            tvDaysRemaining.setText(String.valueOf(DateUtils.getDaysRemainingInMonth()));
        } else {
            tvDaysRemaining.setText("0");
        }
    }
    
    private void updateMonthOverMonth(double thisMonthExpenses, double lastMonthExpenses) {
        tvThisMonthExpense.setText(formatCurrency(thisMonthExpenses));
        tvLastMonthExpense.setText(formatCurrency(lastMonthExpenses));
        
        double change = 0;
        if (lastMonthExpenses > 0) {
            change = ((thisMonthExpenses - lastMonthExpenses) / lastMonthExpenses) * 100;
        }
        
        String changeText;
        int changeColor;
        if (change > 0) {
            changeText = String.format("+%.1f%%", change);
            changeColor = colorExpense; // Higher spending = red
        } else if (change < 0) {
            changeText = String.format("%.1f%%", change);
            changeColor = colorIncome; // Lower spending = green
        } else {
            changeText = "0%";
            changeColor = colorTextSecondary;
        }
        
        tvMomChange.setText(changeText);
        tvMomChange.setTextColor(changeColor);
    }
    
    private void showLoading(boolean show) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private String formatCurrency(double amount) {
        // Format with Indian Rupee symbol
        if (amount >= 100000) {
            return String.format("₹%.1fL", amount / 100000);
        } else if (amount >= 1000) {
            return String.format("₹%.1fK", amount / 1000);
        } else {
            return String.format("₹%.0f", amount);
        }
    }
    
    private int blendColors(int color1, int color2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio);
        int g = (int) (Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio);
        int b = (int) (Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio);
        return Color.rgb(r, g, b);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
