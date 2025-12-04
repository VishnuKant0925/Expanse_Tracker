package com.vishnu.expensetracker.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.adapters.CategoryAdapter;
import com.vishnu.expensetracker.adapters.SubcategoryAdapter;
import com.vishnu.expensetracker.utils.CategoryManager;
import java.util.List;

public class CategorySelectionDialog extends Dialog {
    
    private RecyclerView rvCategories, rvSubcategories;
    private TextView tvSubcategoryLabel;
    private CategoryAdapter categoryAdapter;
    private SubcategoryAdapter subcategoryAdapter;
    private OnCategorySelectedListener listener;
    private String transactionType; // "expense" or "income"
    
    private CategoryManager.CategoryData selectedCategory;
    private String selectedSubcategory;
    
    public interface OnCategorySelectedListener {
        void onCategorySelected(String category, String subcategory);
    }
    
    public CategorySelectionDialog(@NonNull Context context, String transactionType, OnCategorySelectedListener listener) {
        super(context);
        this.transactionType = transactionType;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_category_selection);
        
        initViews();
        setupRecyclerViews();
        loadCategories();
    }
    
    private void initViews() {
        rvCategories = findViewById(R.id.rv_categories);
        rvSubcategories = findViewById(R.id.rv_subcategories);
        tvSubcategoryLabel = findViewById(R.id.tv_subcategory_label);
    }
    
    private void setupRecyclerViews() {
        // Categories RecyclerView with Grid layout
        GridLayoutManager categoryLayoutManager = new GridLayoutManager(getContext(), 2);
        rvCategories.setLayoutManager(categoryLayoutManager);
        
        categoryAdapter = new CategoryAdapter(getContext(), (category, position) -> {
            selectedCategory = category;
            showSubcategories(category);
        });
        rvCategories.setAdapter(categoryAdapter);
        
        // Subcategories RecyclerView
        LinearLayoutManager subcategoryLayoutManager = new LinearLayoutManager(getContext());
        rvSubcategories.setLayoutManager(subcategoryLayoutManager);
        
        subcategoryAdapter = new SubcategoryAdapter(getContext(), (subcategory, position) -> {
            selectedSubcategory = subcategory;
            // Auto-close dialog and return selection
            if (listener != null && selectedCategory != null) {
                listener.onCategorySelected(selectedCategory.name, selectedSubcategory);
            }
            dismiss();
        });
        rvSubcategories.setAdapter(subcategoryAdapter);
    }
    
    private void loadCategories() {
        List<CategoryManager.CategoryData> categories;
        if ("expense".equals(transactionType)) {
            categories = CategoryManager.getExpenseCategories();
        } else {
            categories = CategoryManager.getIncomeCategories();
        }
        categoryAdapter.updateCategories(categories);
    }
    
    private void showSubcategories(CategoryManager.CategoryData category) {
        List<String> subcategories = category.subcategories;
        
        if (subcategories != null && !subcategories.isEmpty()) {
            tvSubcategoryLabel.setVisibility(View.VISIBLE);
            rvSubcategories.setVisibility(View.VISIBLE);
            subcategoryAdapter.updateSubcategories(subcategories);
        } else {
            // If no subcategories, return with just the category
            if (listener != null) {
                listener.onCategorySelected(category.name, "");
            }
            dismiss();
        }
    }
}