package com.vishnu.expensetracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.vishnu.expensetracker.models.Expense;
import java.util.Date;
import java.util.List;

@Dao
public interface ExpenseDao {
    
    @Insert
    long insert(Expense expense);
    
    @Update
    void update(Expense expense);
    
    @Delete
    void delete(Expense expense);
    
    // ========== SOFT DELETE OPERATIONS ==========
    
    /**
     * Soft delete a transaction by marking it as deleted
     */
    @Query("UPDATE expenses SET is_deleted = 1, deleted_at = :deletedAt WHERE id = :id")
    void softDelete(int id, Date deletedAt);
    
    /**
     * Restore a soft-deleted transaction
     */
    @Query("UPDATE expenses SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    void restoreTransaction(int id);
    
    /**
     * Permanently delete all soft-deleted transactions (cleanup)
     */
    @Query("DELETE FROM expenses WHERE is_deleted = 1")
    void permanentlyDeleteAllSoftDeleted();
    
    // ========== BASIC QUERIES (Exclude soft-deleted) ==========
    
    @Query("SELECT * FROM expenses WHERE is_deleted = 0 ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();
    
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    Expense getExpenseById(int id);
    
    @Query("SELECT * FROM expenses WHERE type = :type AND is_deleted = 0 ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByType(String type);
    
    @Query("SELECT * FROM expenses WHERE category = :category AND is_deleted = 0 ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByCategory(String category);
    
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate AND is_deleted = 0 ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByDateRange(Date startDate, Date endDate);
    
    // ========== TOTAL CALCULATIONS (Exclude soft-deleted) ==========
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND is_deleted = 0")
    LiveData<Double> getTotalExpenses();
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income' AND is_deleted = 0")
    LiveData<Double> getTotalIncome();
    
    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'income' AND is_deleted = 0) - (SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'expense' AND is_deleted = 0) as balance")
    LiveData<Double> getCurrentBalance();
    
    // ========== MONTHLY TRACKING QUERIES ==========
    
    /**
     * Get sum of expenses for a specific time range (using timestamps in milliseconds)
     * This is the core method for monthly totals
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND date >= :startOfMonth AND date <= :endOfMonth")
    LiveData<Double> getMonthlyExpenseTotal(long startOfMonth, long endOfMonth);
    
    /**
     * Get sum of income for a specific time range (using timestamps in milliseconds)
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'income' AND is_deleted = 0 AND date >= :startOfMonth AND date <= :endOfMonth")
    LiveData<Double> getMonthlyIncomeTotal(long startOfMonth, long endOfMonth);
    
    /**
     * Synchronous version for calculations - Get monthly expense total
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND date >= :startOfMonth AND date <= :endOfMonth")
    double getMonthlyExpenseTotalSync(long startOfMonth, long endOfMonth);
    
    /**
     * Synchronous version for calculations - Get monthly income total
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'income' AND is_deleted = 0 AND date >= :startOfMonth AND date <= :endOfMonth")
    double getMonthlyIncomeTotalSync(long startOfMonth, long endOfMonth);
    
    /**
     * Get all transactions for a specific month (using timestamp range)
     */
    @Query("SELECT * FROM expenses WHERE is_deleted = 0 AND date >= :startOfMonth AND date <= :endOfMonth ORDER BY date DESC")
    LiveData<List<Expense>> getTransactionsByDateRange(long startOfMonth, long endOfMonth);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpensesByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income' AND is_deleted = 0 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'expense' AND is_deleted = 0 GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryExpenseSum>> getExpensesByCategory();
    
    // Month-wise expense tracking queries (string-based for backwards compatibility)
    @Query("SELECT * FROM expenses WHERE is_deleted = 0 AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByMonth(String monthYear);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear")
    LiveData<Double> getTotalExpensesByMonth(String monthYear);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income' AND is_deleted = 0 AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear")
    LiveData<Double> getTotalIncomeByMonth(String monthYear);
    
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryExpenseSum>> getExpensesByCategoryForMonth(String monthYear);
    
    @Query("SELECT subcategory, SUM(amount) as total FROM expenses WHERE type = 'expense' AND is_deleted = 0 AND category = :category AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear GROUP BY subcategory ORDER BY total DESC")
    LiveData<List<SubcategoryExpenseSum>> getExpensesBySubcategoryForMonth(String category, String monthYear);
    
    @Query("SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month, SUM(amount) as total FROM expenses WHERE type = 'expense' AND is_deleted = 0 GROUP BY strftime('%Y-%m', date/1000, 'unixepoch') ORDER BY month DESC")
    LiveData<List<MonthlyExpenseSum>> getMonthlyExpenseTotals();
    
    // ========== DELETED TRANSACTIONS (for recovery) ==========
    
    @Query("SELECT * FROM expenses WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    LiveData<List<Expense>> getDeletedTransactions();
    
    @Query("DELETE FROM expenses")
    void deleteAllExpenses();
    
    // ========== HELPER CLASSES ==========
    
    public class CategoryExpenseSum {
        public String category;
        public double total;
    }
    
    public class SubcategoryExpenseSum {
        public String subcategory;
        public double total;
    }
    
    public class MonthlyExpenseSum {
        public String month;
        public double total;
    }
}