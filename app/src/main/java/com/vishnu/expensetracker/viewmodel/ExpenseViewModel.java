package com.vishnu.expensetracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.database.ExpenseDao;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.models.MonthlySummary;
import com.vishnu.expensetracker.utils.MonthlyUtils;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing expense data and operations
 * Handles deletion with undo capability and monthly tracking
 */
public class ExpenseViewModel extends AndroidViewModel {
    
    private final ExpenseDao expenseDao;
    private final ExecutorService executor;
    
    // LiveData for all expenses
    private final LiveData<List<Expense>> allExpenses;
    
    // LiveData for balance tracking
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpenses;
    private final LiveData<Double> currentBalance;
    
    // Monthly tracking LiveData
    private final MutableLiveData<MonthlySummary> monthlySummary;
    private final MutableLiveData<Double> currentMonthExpenses;
    private final MutableLiveData<Double> currentMonthIncome;
    private final MutableLiveData<Double> previousMonthExpenses;
    private final MutableLiveData<String> monthComparisonMessage;
    
    // Undo functionality
    private Expense lastDeletedExpense;
    private final MutableLiveData<Boolean> showUndoSnackbar;
    private final MutableLiveData<String> deleteMessage;
    
    // Operation status
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        
        ExpenseDatabase database = ExpenseDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        executor = Executors.newSingleThreadExecutor();
        
        // Initialize LiveData from DAO
        allExpenses = expenseDao.getAllExpenses();
        totalIncome = expenseDao.getTotalIncome();
        totalExpenses = expenseDao.getTotalExpenses();
        currentBalance = expenseDao.getCurrentBalance();
        
        // Initialize mutable LiveData
        monthlySummary = new MutableLiveData<>();
        currentMonthExpenses = new MutableLiveData<>(0.0);
        currentMonthIncome = new MutableLiveData<>(0.0);
        previousMonthExpenses = new MutableLiveData<>(0.0);
        monthComparisonMessage = new MutableLiveData<>("");
        showUndoSnackbar = new MutableLiveData<>(false);
        deleteMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        
        // Load initial monthly data
        refreshMonthlyData();
    }
    
    // ========== GETTERS FOR LIVEDATA ==========
    
    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }
    
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }
    
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }
    
    public LiveData<Double> getCurrentBalance() {
        return currentBalance;
    }
    
    public LiveData<MonthlySummary> getMonthlySummary() {
        return monthlySummary;
    }
    
    public LiveData<Double> getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }
    
    public LiveData<Double> getCurrentMonthIncome() {
        return currentMonthIncome;
    }
    
    public LiveData<String> getMonthComparisonMessage() {
        return monthComparisonMessage;
    }
    
    public LiveData<Boolean> getShowUndoSnackbar() {
        return showUndoSnackbar;
    }
    
    public LiveData<String> getDeleteMessage() {
        return deleteMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Insert a new expense
     */
    public void insert(Expense expense) {
        executor.execute(() -> {
            try {
                expenseDao.insert(expense);
                refreshMonthlyData();
            } catch (Exception e) {
                errorMessage.postValue("Error adding transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Update an existing expense
     */
    public void update(Expense expense) {
        executor.execute(() -> {
            try {
                expenseDao.update(expense);
                refreshMonthlyData();
            } catch (Exception e) {
                errorMessage.postValue("Error updating transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Soft delete a transaction (can be undone)
     * This marks the transaction as deleted without permanently removing it
     */
    public void softDeleteTransaction(Expense expense) {
        isLoading.setValue(true);
        
        // Store for undo
        lastDeletedExpense = expense;
        
        executor.execute(() -> {
            try {
                // Perform soft delete
                expenseDao.softDelete(expense.getId(), new Date());
                
                // Post updates
                isLoading.postValue(false);
                deleteMessage.postValue("Transaction deleted");
                showUndoSnackbar.postValue(true);
                
                // Refresh monthly data after deletion
                refreshMonthlyData();
                
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error deleting transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Permanently delete a transaction (cannot be undone)
     */
    public void permanentlyDelete(Expense expense) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                expenseDao.delete(expense);
                isLoading.postValue(false);
                deleteMessage.postValue("Transaction permanently deleted");
                refreshMonthlyData();
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error deleting transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Undo the last soft delete operation
     */
    public void undoDelete() {
        if (lastDeletedExpense != null) {
            executor.execute(() -> {
                try {
                    // Restore the soft-deleted transaction
                    expenseDao.restoreTransaction(lastDeletedExpense.getId());
                    
                    // Clear undo state
                    lastDeletedExpense = null;
                    showUndoSnackbar.postValue(false);
                    deleteMessage.postValue("Transaction restored");
                    
                    // Refresh data
                    refreshMonthlyData();
                    
                } catch (Exception e) {
                    errorMessage.postValue("Error restoring transaction: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Clear the undo snackbar state (called when snackbar is dismissed)
     */
    public void clearUndoState() {
        showUndoSnackbar.setValue(false);
        // Optionally permanently delete after undo timeout
        if (lastDeletedExpense != null) {
            // Keep the soft-deleted state - user chose not to undo
            lastDeletedExpense = null;
        }
    }
    
    // ========== MONTHLY TRACKING ==========
    
    /**
     * Refresh all monthly tracking data
     */
    public void refreshMonthlyData() {
        executor.execute(() -> {
            try {
                // Get current month range
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
                
                // Update LiveData
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
                        0.0, // Default budget - can be set by user
                        currentRange.getDisplayName()
                );
                monthlySummary.postValue(summary);
                
            } catch (Exception e) {
                errorMessage.postValue("Error loading monthly data: " + e.getMessage());
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
                
                monthlySummary.postValue(summary);
                currentMonthExpenses.postValue(currentExpense);
                currentMonthIncome.postValue(currentIncome);
                monthComparisonMessage.postValue(summary.getComparisonMessage());
                
            } catch (Exception e) {
                errorMessage.postValue("Error loading monthly data: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get expense by ID (synchronous - use in background)
     */
    public Expense getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }
    
    /**
     * Get deleted transactions for recovery
     */
    public LiveData<List<Expense>> getDeletedTransactions() {
        return expenseDao.getDeletedTransactions();
    }
    
    /**
     * Restore a specific deleted transaction
     */
    public void restoreTransaction(Expense expense) {
        executor.execute(() -> {
            try {
                expenseDao.restoreTransaction(expense.getId());
                deleteMessage.postValue("Transaction restored");
                refreshMonthlyData();
            } catch (Exception e) {
                errorMessage.postValue("Error restoring transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Permanently delete all soft-deleted transactions
     */
    public void emptyTrash() {
        executor.execute(() -> {
            try {
                expenseDao.permanentlyDeleteAllSoftDeleted();
                deleteMessage.postValue("Trash emptied");
            } catch (Exception e) {
                errorMessage.postValue("Error emptying trash: " + e.getMessage());
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
