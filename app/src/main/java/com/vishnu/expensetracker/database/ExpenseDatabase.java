package com.vishnu.expensetracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.models.Category;
import com.vishnu.expensetracker.models.Subcategory;
import com.vishnu.expensetracker.utils.DateConverter;

@Database(
    entities = {Expense.class, Category.class, Subcategory.class},
    version = 2,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class ExpenseDatabase extends RoomDatabase {
    
    private static ExpenseDatabase instance;
    
    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract SubcategoryDao subcategoryDao();
    
    public static synchronized ExpenseDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                ExpenseDatabase.class,
                "expense_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}