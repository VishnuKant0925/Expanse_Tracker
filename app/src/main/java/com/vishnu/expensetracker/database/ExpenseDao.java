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
    void insert(Expense expense);
    
    @Update
    void update(Expense expense);
    
    @Delete
    void delete(Expense expense);
    
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();
    
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    Expense getExpenseById(int id);
    
    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByType(String type);
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByCategory(String category);
    
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense'")
    LiveData<Double> getTotalExpenses();
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income'")
    LiveData<Double> getTotalIncome();
    
    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'income') - (SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'expense') as balance")
    LiveData<Double> getCurrentBalance();
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpensesByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'expense' GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryExpenseSum>> getExpensesByCategory();
    
    // Month-wise expense tracking queries
    @Query("SELECT * FROM expenses WHERE strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesByMonth(String monthYear);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear")
    LiveData<Double> getTotalExpensesByMonth(String monthYear);
    
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'expense' AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryExpenseSum>> getExpensesByCategoryForMonth(String monthYear);
    
    @Query("SELECT subcategory, SUM(amount) as total FROM expenses WHERE type = 'expense' AND category = :category AND strftime('%Y-%m', date/1000, 'unixepoch') = :monthYear GROUP BY subcategory ORDER BY total DESC")
    LiveData<List<SubcategoryExpenseSum>> getExpensesBySubcategoryForMonth(String category, String monthYear);
    
    @Query("SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month, SUM(amount) as total FROM expenses WHERE type = 'expense' GROUP BY strftime('%Y-%m', date/1000, 'unixepoch') ORDER BY month DESC")
    LiveData<List<MonthlyExpenseSum>> getMonthlyExpenseTotals();
    
    @Query("DELETE FROM expenses")
    void deleteAllExpenses();
    
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