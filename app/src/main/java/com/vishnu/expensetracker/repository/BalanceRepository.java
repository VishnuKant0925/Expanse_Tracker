package com.vishnu.expensetracker.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.vishnu.expensetracker.database.ExpenseDao;
import com.vishnu.expensetracker.models.BalanceSummary;
import com.vishnu.expensetracker.models.MonthlySummary;
import com.vishnu.expensetracker.utils.MonthlyUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing balance and monthly summary data
 * Acts as a single source of truth for financial calculations
 */
public class BalanceRepository {
    private final ExpenseDao expenseDao;
    private final MediatorLiveData<BalanceSummary> balanceSummaryLiveData;
    private final MutableLiveData<MonthlySummary> monthlySummaryLiveData;
    private final ExecutorService executor;
    
    // Monthly tracking LiveData
    private final MutableLiveData<Double> currentMonthExpenses;
    private final MutableLiveData<Double> currentMonthIncome;
    private final MutableLiveData<Double> previousMonthExpenses;
    private final MutableLiveData<String> monthComparisonMessage;
    
    public BalanceRepository(ExpenseDao expenseDao) {
        this.expenseDao = expenseDao;
        this.balanceSummaryLiveData = new MediatorLiveData<>();
        this.monthlySummaryLiveData = new MutableLiveData<>();
        this.executor = Executors.newSingleThreadExecutor();
        
        this.currentMonthExpenses = new MutableLiveData<>(0.0);
        this.currentMonthIncome = new MutableLiveData<>(0.0);
        this.previousMonthExpenses = new MutableLiveData<>(0.0);
        this.monthComparisonMessage = new MutableLiveData<>("");
        
        setupBalanceCalculation();
    }
    
    private void setupBalanceCalculation() {
        LiveData<Double> incomeLiveData = expenseDao.getTotalIncome();
        LiveData<Double> expensesLiveData = expenseDao.getTotalExpenses();
        
        balanceSummaryLiveData.addSource(incomeLiveData, income -> {
            Double currentIncome = income != null ? income : 0.0;
            Double currentExpenses = expensesLiveData.getValue() != null ? expensesLiveData.getValue() : 0.0;
            balanceSummaryLiveData.setValue(new BalanceSummary(currentIncome, currentExpenses));
            // Also refresh monthly data when income changes
            refreshMonthlyData();
        });
        
        balanceSummaryLiveData.addSource(expensesLiveData, expenses -> {
            Double currentExpenses = expenses != null ? expenses : 0.0;
            Double currentIncome = incomeLiveData.getValue() != null ? incomeLiveData.getValue() : 0.0;
            balanceSummaryLiveData.setValue(new BalanceSummary(currentIncome, currentExpenses));
            // Also refresh monthly data when expenses change
            refreshMonthlyData();
        });
    }
    
    public LiveData<BalanceSummary> getBalanceSummary() {
        return balanceSummaryLiveData;
    }
    
    public LiveData<Double> getTotalIncome() {
        return expenseDao.getTotalIncome();
    }
    
    public LiveData<Double> getTotalExpenses() {
        return expenseDao.getTotalExpenses();
    }
    
    public LiveData<Double> getCurrentBalance() {
        return expenseDao.getCurrentBalance();
    }
    
    // ========== MONTHLY TRACKING ==========
    
    public LiveData<MonthlySummary> getMonthlySummary() {
        return monthlySummaryLiveData;
    }
    
    public LiveData<Double> getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }
    
    public LiveData<Double> getCurrentMonthIncome() {
        return currentMonthIncome;
    }
    
    public LiveData<Double> getPreviousMonthExpenses() {
        return previousMonthExpenses;
    }
    
    public LiveData<String> getMonthComparisonMessage() {
        return monthComparisonMessage;
    }
    
    /**
     * Refresh all monthly tracking data
     */
    public void refreshMonthlyData() {
        executor.execute(() -> {
            try {
                // Get current and previous month ranges
                MonthlyUtils.DateRange currentRange = MonthlyUtils.getCurrentMonthRange();
                MonthlyUtils.DateRange previousRange = MonthlyUtils.getPreviousMonthRange();
                
                // Calculate current month totals
                double currentExpense = expenseDao.getMonthlyExpenseTotalSync(
                        currentRange.getStartTimestamp(),
                        currentRange.getEndTimestamp()
                );
                
                double currentIncome = expenseDao.getMonthlyIncomeTotalSync(
                        currentRange.getStartTimestamp(),
                        currentRange.getEndTimestamp()
                );
                
                // Calculate previous month expense for comparison
                double prevExpense = expenseDao.getMonthlyExpenseTotalSync(
                        previousRange.getStartTimestamp(),
                        previousRange.getEndTimestamp()
                );
                
                // Update individual LiveData
                currentMonthExpenses.postValue(currentExpense);
                currentMonthIncome.postValue(currentIncome);
                previousMonthExpenses.postValue(prevExpense);
                
                // Generate comparison message
                String comparisonMsg = MonthlyUtils.getSpendingComparisonMessage(currentExpense, prevExpense);
                monthComparisonMessage.postValue(comparisonMsg);
                
                // Create and post MonthlySummary
                MonthlySummary summary = new MonthlySummary(
                        currentIncome,
                        currentExpense,
                        prevExpense,
                        0.0, // Default budget
                        currentRange.getDisplayName()
                );
                monthlySummaryLiveData.postValue(summary);
                
            } catch (Exception e) {
                android.util.Log.e("BalanceRepository", "Error refreshing monthly data", e);
            }
        });
    }
    
    /**
     * Get monthly summary with a custom budget
     */
    public void refreshMonthlyDataWithBudget(double budget) {
        executor.execute(() -> {
            try {
                MonthlyUtils.DateRange currentRange = MonthlyUtils.getCurrentMonthRange();
                MonthlyUtils.DateRange previousRange = MonthlyUtils.getPreviousMonthRange();
                
                double currentExpense = expenseDao.getMonthlyExpenseTotalSync(
                        currentRange.getStartTimestamp(),
                        currentRange.getEndTimestamp()
                );
                
                double currentIncome = expenseDao.getMonthlyIncomeTotalSync(
                        currentRange.getStartTimestamp(),
                        currentRange.getEndTimestamp()
                );
                
                double prevExpense = expenseDao.getMonthlyExpenseTotalSync(
                        previousRange.getStartTimestamp(),
                        previousRange.getEndTimestamp()
                );
                
                MonthlySummary summary = new MonthlySummary(
                        currentIncome,
                        currentExpense,
                        prevExpense,
                        budget,
                        currentRange.getDisplayName()
                );
                
                monthlySummaryLiveData.postValue(summary);
                currentMonthExpenses.postValue(currentExpense);
                currentMonthIncome.postValue(currentIncome);
                monthComparisonMessage.postValue(summary.getComparisonMessage());
                
            } catch (Exception e) {
                android.util.Log.e("BalanceRepository", "Error refreshing monthly data with budget", e);
            }
        });
    }
    
    /**
     * Get LiveData for monthly income using timestamp range
     */
    public LiveData<Double> getMonthlyIncome(long startTimestamp, long endTimestamp) {
        return expenseDao.getMonthlyIncomeTotal(startTimestamp, endTimestamp);
    }
    
    /**
     * Get LiveData for monthly expenses using timestamp range
     */
    public LiveData<Double> getMonthlyExpenses(long startTimestamp, long endTimestamp) {
        return expenseDao.getMonthlyExpenseTotal(startTimestamp, endTimestamp);
    }
}