package com.vishnu.expensetracker.utils;

import android.content.Context;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Category;
import com.vishnu.expensetracker.models.Subcategory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    
    public static void initializeDatabase(Context context) {
        ExpenseDatabase database = ExpenseDatabase.getInstance(context);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            // Check if categories already exist
            try {
                List<Category> existingCategories = database.categoryDao().getAllCategories().getValue();
                if (existingCategories == null || existingCategories.isEmpty()) {
                    // Initialize with predefined categories
                    List<Category> categories = CategoryManager.getAllCategories();
                    for (Category category : categories) {
                        database.categoryDao().insert(category);
                    }
                    
                    // Initialize with predefined subcategories
                    List<Subcategory> subcategories = CategoryManager.getAllSubcategories();
                    for (Subcategory subcategory : subcategories) {
                        database.subcategoryDao().insert(subcategory);
                    }
                }
            } catch (Exception e) {
                // First time initialization - categories don't exist yet
                List<Category> categories = CategoryManager.getAllCategories();
                for (Category category : categories) {
                    database.categoryDao().insert(category);
                }
                
                List<Subcategory> subcategories = CategoryManager.getAllSubcategories();
                for (Subcategory subcategory : subcategories) {
                    database.subcategoryDao().insert(subcategory);
                }
            }
        });
    }
}