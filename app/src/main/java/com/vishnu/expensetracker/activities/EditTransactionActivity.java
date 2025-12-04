package com.vishnu.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.database.ExpenseDatabase;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.dialogs.CategorySelectionDialog;
import com.vishnu.expensetracker.utils.ThemeManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Robust Edit Transaction Activity with comprehensive error handling
 * Features:
 * - Pre-filled form fields with existing transaction data
 * - Full validation and error handling
 * - Smooth user experience with progress indicators
 * - Safe database operations
 */
public class EditTransactionActivity extends AppCompatActivity {
    
    public static final String EXTRA_EXPENSE_ID = "expense_id";
    
    // UI Components
    private TextInputEditText etTitle, etAmount, etDescription, etDate;
    private LinearLayout llCategorySelection;
    private TextView tvSelectedCategory, tvSelectedSubcategory;
    private Spinner spinnerPaymentMethod;
    private RadioGroup rgType;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnCancel;
    
    // Data and Business Logic
    private ExpenseDatabase database;
    private ExecutorService executor;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private String selectedCategory = "";
    private String selectedSubcategory = "";
    private ThemeManager themeManager;
    private Expense currentExpense;
    private int expenseId;
    private boolean isDataLoaded = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Apply theme before setContentView
            themeManager = ThemeManager.getInstance(this);
            if (themeManager != null) {
                themeManager.applyTheme();
            }
            
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_expense); // Reuse existing layout
            
            // Set activity title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Transaction");
            } else {
                setTitle("Edit Transaction");
            }
            
            // Validate intent data
            if (!validateIntentData()) {
                return;
            }
            
            initializeComponents();
            loadExpenseData();
            
        } catch (Exception e) {
            handleError("Error initializing edit screen", e);
            finish();
        }
    }
    
    /**
     * Validate that we received a valid expense ID
     */
    private boolean validateIntentData() {
        expenseId = getIntent().getIntExtra(EXTRA_EXPENSE_ID, -1);
        if (expenseId == -1) {
            Toast.makeText(this, "Error: Invalid transaction ID", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }
    
    /**
     * Initialize all components with error handling
     */
    private void initializeComponents() {
        try {
            // Initialize views safely
            initializeViews();
            setupDatabase();
            setupDatePicker();
            setupSpinner();
            setupClickListeners();
            
        } catch (Exception e) {
            handleError("Error setting up interface", e);
            throw e; // Re-throw to be caught by onCreate
        }
    }
    
    /**
     * Initialize all UI views with null checks
     */
    private void initializeViews() {
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
        
        // Validate critical views
        if (etTitle == null || etAmount == null || btnSave == null) {
            throw new RuntimeException("Critical UI components not found");
        }
        
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        selectedDate = Calendar.getInstance();
    }
    
    /**
     * Setup database connection safely
     */
    private void setupDatabase() {
        try {
            database = ExpenseDatabase.getInstance(this);
            executor = Executors.newSingleThreadExecutor();
            if (database == null || executor == null) {
                throw new RuntimeException("Failed to initialize database components");
            }
        } catch (Exception e) {
            handleError("Error connecting to database", e);
            throw e;
        }
    }
    
    /**
     * Setup date picker with current date
     */
    private void setupDatePicker() {
        try {
            if (etDate != null) {
                etDate.setOnClickListener(v -> showDatePicker());
                etDate.setText(dateFormat.format(selectedDate.getTime()));
            }
        } catch (Exception e) {
            handleError("Error setting up date picker", e);
        }
    }
    
    /**
     * Setup payment method spinner
     */
    private void setupSpinner() {
        try {
            if (spinnerPaymentMethod != null) {
                String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "UPI", "Net Banking", "Wallet"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, paymentMethods);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPaymentMethod.setAdapter(adapter);
            }
        } catch (Exception e) {
            handleError("Error setting up payment methods", e);
        }
    }
    
    /**
     * Setup click listeners with error handling
     */
    private void setupClickListeners() {
        try {
            if (llCategorySelection != null) {
                llCategorySelection.setOnClickListener(v -> showCategoryDialog());
            }
            
            if (btnSave != null) {
                btnSave.setText("Update Transaction");
                btnSave.setOnClickListener(v -> updateTransaction());
            }
            
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            handleError("Error setting up click listeners", e);
        }
    }
    
    /**
     * Load expense data from database
     */
    private void loadExpenseData() {
        if (executor == null || database == null) {
            handleError("Database not initialized", null);
            finish();
            return;
        }
        
        executor.execute(() -> {
            try {
                currentExpense = database.expenseDao().getExpenseById(expenseId);
                
                runOnUiThread(() -> {
                    if (currentExpense != null) {
                        populateFields();
                        isDataLoaded = true;
                    } else {
                        Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    handleError("Error loading transaction data", e);
                    finish();
                });
            }
        });
    }
    
    /**
     * Populate form fields with transaction data
     */
    private void populateFields() {
        try {
            if (currentExpense == null) {
                Toast.makeText(this, "No transaction data available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Populate text fields safely
            safeSetText(etTitle, currentExpense.getTitle());
            safeSetText(etAmount, String.valueOf(currentExpense.getAmount()));
            safeSetText(etDescription, currentExpense.getDescription());
            
            // Set date
            if (currentExpense.getDate() != null) {
                selectedDate.setTime(currentExpense.getDate());
                safeSetText(etDate, dateFormat.format(currentExpense.getDate()));
            }
            
            // Set categories
            selectedCategory = safeGetString(currentExpense.getCategory());
            selectedSubcategory = safeGetString(currentExpense.getSubcategory());
            updateCategoryDisplay();
            
            // Set transaction type
            setTransactionType(currentExpense.getType());
            
            // Set payment method
            setPaymentMethod(currentExpense.getPaymentMethod());
            
        } catch (Exception e) {
            handleError("Error populating fields", e);
        }
    }
    
    /**
     * Safely set text to TextInputEditText
     */
    private void safeSetText(TextInputEditText editText, String text) {
        if (editText != null) {
            editText.setText(text != null ? text : "");
        }
    }
    
    /**
     * Safely get string value
     */
    private String safeGetString(String value) {
        return value != null ? value : "";
    }
    
    /**
     * Set transaction type radio buttons
     */
    private void setTransactionType(String type) {
        try {
            if ("income".equals(type) && rbIncome != null) {
                rbIncome.setChecked(true);
            } else if (rbExpense != null) {
                rbExpense.setChecked(true);
            }
        } catch (Exception e) {
            handleError("Error setting transaction type", e);
        }
    }
    
    /**
     * Set payment method in spinner
     */
    private void setPaymentMethod(String paymentMethod) {
        try {
            if (paymentMethod != null && spinnerPaymentMethod != null && 
                spinnerPaymentMethod.getAdapter() != null) {
                
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerPaymentMethod.getAdapter();
                int position = adapter.getPosition(paymentMethod);
                if (position >= 0) {
                    spinnerPaymentMethod.setSelection(position);
                }
            }
        } catch (Exception e) {
            handleError("Error setting payment method", e);
        }
    }
    
    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    if (etDate != null) {
                        etDate.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        } catch (Exception e) {
            handleError("Error showing date picker", e);
        }
    }
    
    /**
     * Show category selection dialog
     */
    private void showCategoryDialog() {
        try {
            String transactionType = (rbIncome != null && rbIncome.isChecked()) ? "income" : "expense";
            CategorySelectionDialog dialog = new CategorySelectionDialog(this, transactionType, 
                (category, subcategory) -> {
                    selectedCategory = category != null ? category : "";
                    selectedSubcategory = subcategory != null ? subcategory : "";
                    updateCategoryDisplay();
                });
            dialog.show();
        } catch (Exception e) {
            handleError("Error showing category dialog", e);
        }
    }
    
    /**
     * Update category display in UI
     */
    private void updateCategoryDisplay() {
        try {
            if (tvSelectedCategory != null) {
                tvSelectedCategory.setText(selectedCategory.isEmpty() ? 
                    "Tap to select category" : selectedCategory);
            }
            
            if (tvSelectedSubcategory != null) {
                tvSelectedSubcategory.setText(selectedSubcategory.isEmpty() ? 
                    "Tap to select subcategory" : selectedSubcategory);
                tvSelectedSubcategory.setVisibility(selectedCategory.isEmpty() ? 
                    View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            handleError("Error updating category display", e);
        }
    }
    
    /**
     * Update transaction with comprehensive validation
     */
    private void updateTransaction() {
        if (!isDataLoaded) {
            Toast.makeText(this, "Please wait for data to load", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Validate and get form data
            TransactionData data = validateAndGetFormData();
            if (data == null) {
                return; // Validation failed, error already shown
            }
            
            // Update expense object
            updateExpenseObject(data);
            
            // Save to database
            saveToDatabase();
            
        } catch (Exception e) {
            handleError("Error updating transaction", e);
        }
    }
    
    /**
     * Validate form data and return transaction data object
     */
    private TransactionData validateAndGetFormData() {
        try {
            String title = etTitle != null ? etTitle.getText().toString().trim() : "";
            String amountStr = etAmount != null ? etAmount.getText().toString().trim() : "";
            String description = etDescription != null ? etDescription.getText().toString().trim() : "";
            String paymentMethod = spinnerPaymentMethod != null ? 
                spinnerPaymentMethod.getSelectedItem().toString() : "Cash";
            
            // Validation
            if (title.isEmpty()) {
                showFieldError(etTitle, "Title is required");
                return null;
            }
            
            if (amountStr.isEmpty()) {
                showFieldError(etAmount, "Amount is required");
                return null;
            }
            
            if (selectedCategory.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return null;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showFieldError(etAmount, "Amount must be greater than 0");
                    return null;
                }
            } catch (NumberFormatException e) {
                showFieldError(etAmount, "Invalid amount format");
                return null;
            }
            
            String type = (rbIncome != null && rbIncome.isChecked()) ? "income" : "expense";
            
            return new TransactionData(title, amount, description, paymentMethod, type);
            
        } catch (Exception e) {
            handleError("Error validating form data", e);
            return null;
        }
    }
    
    /**
     * Show field-specific error
     */
    private void showFieldError(TextInputEditText field, String message) {
        if (field != null) {
            field.setError(message);
            field.requestFocus();
        }
    }
    
    /**
     * Update expense object with new data
     */
    private void updateExpenseObject(TransactionData data) {
        if (currentExpense != null) {
            currentExpense.setTitle(data.title);
            currentExpense.setAmount(data.amount);
            currentExpense.setCategory(selectedCategory);
            currentExpense.setSubcategory(selectedSubcategory);
            currentExpense.setDescription(data.description);
            currentExpense.setDate(selectedDate.getTime());
            currentExpense.setType(data.type);
            currentExpense.setPaymentMethod(data.paymentMethod);
        }
    }
    
    /**
     * Save updated expense to database
     */
    private void saveToDatabase() {
        if (executor == null || database == null || currentExpense == null) {
            Toast.makeText(this, "Error: Database not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(() -> {
            try {
                database.expenseDao().update(currentExpense);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    handleError("Error saving transaction", e);
                });
            }
        });
    }
    
    /**
     * Central error handling method
     */
    private void handleError(String message, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    /**
     * Helper class to hold transaction data
     */
    private static class TransactionData {
        final String title;
        final double amount;
        final String description;
        final String paymentMethod;
        final String type;
        
        TransactionData(String title, double amount, String description, 
                       String paymentMethod, String type) {
            this.title = title;
            this.amount = amount;
            this.description = description;
            this.paymentMethod = paymentMethod;
            this.type = type;
        }
    }
}