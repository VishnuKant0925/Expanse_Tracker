package com.vishnu.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.dialogs.CategorySelectionDialog;
import com.vishnu.expensetracker.utils.ThemeManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends AppCompatActivity {
    
    private EditText etTitle, etAmount, etDescription, etDate;
    private LinearLayout llCategorySelection;
    private TextView tvSelectedCategory, tvSelectedSubcategory;
    private Spinner spinnerPaymentMethod;
    private RadioGroup rgType;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnCancel;
    private MaterialCardView cardEssential;
    private SwitchMaterial switchEssential;
    
    private ExpenseDatabase database;
    private ExecutorService executor;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private String selectedCategory = "";
    private String selectedSubcategory = "";
    private ThemeManager themeManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        
        initViews();
        setupSpinners();
        setupClickListeners();
        
        database = ExpenseDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        // Set current date
        etDate.setText(dateFormat.format(selectedDate.getTime()));
    }
    
    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        etDate = findViewById(R.id.et_date);
        llCategorySelection = findViewById(R.id.ll_category_selection);
        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        tvSelectedSubcategory = findViewById(R.id.tv_selected_subcategory);
        spinnerPaymentMethod = findViewById(R.id.spinner_payment_method);
        rgType = findViewById(R.id.rg_type);
        rbExpense = findViewById(R.id.rb_expense);
        rbIncome = findViewById(R.id.rb_income);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        cardEssential = findViewById(R.id.card_essential);
        switchEssential = findViewById(R.id.switch_essential);
    }
    
    private void setupSpinners() {
        // Payment method spinner
        String[] paymentMethods = {"Cash", "Card", "UPI", "Bank Transfer"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentAdapter);
    }
    
    private void setupClickListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        
        llCategorySelection.setOnClickListener(v -> showCategorySelectionDialog());
        
        btnSave.setOnClickListener(v -> saveExpense());
        
        btnCancel.setOnClickListener(v -> finish());
        
        // Show/hide essential toggle based on type selection
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            // Show essential toggle only for expenses
            cardEssential.setVisibility(checkedId == R.id.rb_expense ? View.VISIBLE : View.GONE);
            // Reset category selection when type changes
            selectedCategory = "";
            selectedSubcategory = "";
            tvSelectedCategory.setText("Select Category");
            tvSelectedSubcategory.setVisibility(View.GONE);
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showCategorySelectionDialog() {
        String type = rbIncome.isChecked() ? "income" : "expense";
        CategorySelectionDialog dialog = new CategorySelectionDialog(this, type, (category, subcategory) -> {
            selectedCategory = category;
            selectedSubcategory = subcategory;
            updateCategoryDisplay();
        });
        dialog.show();
    }
    
    private void updateCategoryDisplay() {
        tvSelectedCategory.setText(selectedCategory);
        if (selectedSubcategory != null && !selectedSubcategory.isEmpty()) {
            tvSelectedSubcategory.setText(selectedSubcategory);
            tvSelectedSubcategory.setVisibility(View.VISIBLE);
        } else {
            tvSelectedSubcategory.setVisibility(View.GONE);
        }
    }
    
    private void saveExpense() {
        // Validate inputs
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString().toLowerCase();
        
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }
        
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }
        
        String type = rbIncome.isChecked() ? "income" : "expense";
        
        // Create expense object with subcategory
        Expense expense = new Expense(title, amount, selectedCategory, selectedSubcategory, 
                                    description, selectedDate.getTime(), type, paymentMethod);
        
        // Set essential flag (only relevant for expenses)
        if (type.equals("expense")) {
            expense.setEssential(switchEssential.isChecked());
        }
        
        // Save to database
        executor.execute(() -> {
            database.expenseDao().insert(expense);
            runOnUiThread(() -> {
                Toast.makeText(this, type.equals("income") ? 
                    "Income added successfully" : "Expense added successfully", 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}