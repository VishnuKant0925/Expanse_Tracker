package com.vishnu.expensetracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.models.Category;
import com.vishnu.expensetracker.models.Subcategory;
import com.vishnu.expensetracker.utils.DateConverter;

@Database(
    entities = {Expense.class, Category.class, Subcategory.class},
    version = 4,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class ExpenseDatabase extends RoomDatabase {
    
    private static ExpenseDatabase instance;
    
    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract SubcategoryDao subcategoryDao();
    
    /**
     * Migration from version 2 to 3:
     * Adds soft delete columns (is_deleted, deleted_at) to expenses table
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add is_deleted column with default value 0 (false)
            database.execSQL("ALTER TABLE expenses ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
            // Add deleted_at column (nullable)
            database.execSQL("ALTER TABLE expenses ADD COLUMN deleted_at INTEGER");
        }
    };
    
    /**
     * Migration from version 3 to 4:
     * Adds is_essential column for Needs vs Wants analytics
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add is_essential column with default value 1 (true = essential/need)
            database.execSQL("ALTER TABLE expenses ADD COLUMN is_essential INTEGER NOT NULL DEFAULT 1");
        }
    };
    
    public static synchronized ExpenseDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                ExpenseDatabase.class,
                "expense_database"
            )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}