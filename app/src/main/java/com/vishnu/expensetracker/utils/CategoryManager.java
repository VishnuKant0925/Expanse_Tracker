package com.vishnu.expensetracker.utils;

import com.vishnu.expensetracker.models.Category;
import com.vishnu.expensetracker.models.Subcategory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManager {
    
    public static class CategoryData {
        public String name;
        public String icon;
        public String color;
        public List<String> subcategories;
        
        public CategoryData(String name, String icon, String color, List<String> subcategories) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.subcategories = subcategories;
        }
    }
    
    // Predefined expense categories with subcategories
    private static final Map<String, CategoryData> EXPENSE_CATEGORIES = new HashMap<String, CategoryData>() {{
        put("Home & Utilities", new CategoryData(
            "Home & Utilities", 
            "🏠", 
            "#FF5722", 
            List.of("Rent", "Electricity", "Water", "Internet", "Gas", "Maintenance")
        ));
        
        put("Food & Dining", new CategoryData(
            "Food & Dining", 
            "🍽️", 
            "#FF9800", 
            List.of("Groceries", "Restaurants", "Snacks", "Beverages", "Fast Food", "Delivery")
        ));
        
        put("Transportation", new CategoryData(
            "Transportation", 
            "🚗", 
            "#2196F3", 
            List.of("Fuel", "Bus", "Train", "Cab", "Auto", "Parking", "Vehicle Maintenance")
        ));
        
        put("Shopping", new CategoryData(
            "Shopping", 
            "🛍️", 
            "#E91E63", 
            List.of("Clothes", "Electronics", "Gifts", "Books", "Home Decor", "Cosmetics")
        ));
        
        put("Health & Fitness", new CategoryData(
            "Health & Fitness", 
            "💊", 
            "#4CAF50", 
            List.of("Medicine", "Gym", "Doctor Visits", "Health Insurance", "Fitness Equipment", "Wellness")
        ));
        
        put("Education", new CategoryData(
            "Education", 
            "🎓", 
            "#3F51B5", 
            List.of("Tuition Fees", "Books", "Courses", "Training", "Stationery", "Online Learning")
        ));
        
        put("Work / Office", new CategoryData(
            "Work / Office", 
            "💼", 
            "#607D8B", 
            List.of("Supplies", "Travel", "Client Meetings", "Office Rent", "Equipment", "Software")
        ));
        
        put("Entertainment", new CategoryData(
            "Entertainment", 
            "🎉", 
            "#9C27B0", 
            List.of("Movies", "Events", "Subscriptions", "Games", "Sports", "Hobbies")
        ));
        
        put("Bills & EMIs", new CategoryData(
            "Bills & EMIs", 
            "💸", 
            "#F44336", 
            List.of("Credit Card", "Loans", "Insurance", "Phone Bill", "EMI", "Tax")
        ));
        
        put("Savings & Investments", new CategoryData(
            "Savings & Investments", 
            "💰", 
            "#2E7D32", 
            List.of("Mutual Funds", "Fixed Deposit", "Stocks", "Gold", "Real Estate", "Emergency Fund")
        ));
        
        put("Personal / Others", new CategoryData(
            "Personal / Others", 
            "❤️", 
            "#795548", 
            List.of("Miscellaneous", "Charity", "Donations", "Personal Care", "Family", "Pets")
        ));
    }};
    
    // Predefined income categories
    private static final Map<String, CategoryData> INCOME_CATEGORIES = new HashMap<String, CategoryData>() {{
        put("Salary & Wages", new CategoryData(
            "Salary & Wages", 
            "💼", 
            "#4CAF50", 
            List.of("Salary", "Overtime", "Bonus", "Commission", "Tips")
        ));
        
        put("Business & Freelance", new CategoryData(
            "Business & Freelance", 
            "💼", 
            "#FF9800", 
            List.of("Business Income", "Freelance", "Consulting", "Contract Work", "Side Hustle")
        ));
        
        put("Investments", new CategoryData(
            "Investments", 
            "📈", 
            "#2196F3", 
            List.of("Dividends", "Interest", "Capital Gains", "Rental Income", "Royalties")
        ));
        
        put("Others", new CategoryData(
            "Others", 
            "💰", 
            "#9C27B0", 
            List.of("Gifts", "Refunds", "Cashback", "Prize Money", "Insurance Claims")
        ));
    }};
    
    public static List<CategoryData> getExpenseCategories() {
        return new ArrayList<>(EXPENSE_CATEGORIES.values());
    }
    
    public static List<CategoryData> getIncomeCategories() {
        return new ArrayList<>(INCOME_CATEGORIES.values());
    }
    
    public static CategoryData getCategoryData(String categoryName, String type) {
        if ("expense".equals(type)) {
            return EXPENSE_CATEGORIES.get(categoryName);
        } else {
            return INCOME_CATEGORIES.get(categoryName);
        }
    }
    
    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        
        // Add expense categories
        for (Map.Entry<String, CategoryData> entry : EXPENSE_CATEGORIES.entrySet()) {
            CategoryData data = entry.getValue();
            categories.add(new Category(data.name, data.icon, data.color, "expense"));
        }
        
        // Add income categories
        for (Map.Entry<String, CategoryData> entry : INCOME_CATEGORIES.entrySet()) {
            CategoryData data = entry.getValue();
            categories.add(new Category(data.name, data.icon, data.color, "income"));
        }
        
        return categories;
    }
    
    public static List<Subcategory> getAllSubcategories() {
        List<Subcategory> subcategories = new ArrayList<>();
        int categoryId = 1;
        
        // Add expense subcategories
        for (Map.Entry<String, CategoryData> entry : EXPENSE_CATEGORIES.entrySet()) {
            CategoryData data = entry.getValue();
            for (String subcat : data.subcategories) {
                subcategories.add(new Subcategory(subcat, categoryId, "📋", "expense"));
            }
            categoryId++;
        }
        
        // Add income subcategories
        for (Map.Entry<String, CategoryData> entry : INCOME_CATEGORIES.entrySet()) {
            CategoryData data = entry.getValue();
            for (String subcat : data.subcategories) {
                subcategories.add(new Subcategory(subcat, categoryId, "📋", "income"));
            }
            categoryId++;
        }
        
        return subcategories;
    }
    
    public static List<String> getSubcategoriesForCategory(String categoryName, String type) {
        CategoryData data = getCategoryData(categoryName, type);
        return data != null ? data.subcategories : new ArrayList<>();
    }
}