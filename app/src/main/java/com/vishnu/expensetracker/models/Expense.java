package com.vishnu.expensetracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import java.util.Date;

@Entity(tableName = "expenses")
public class Expense {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "amount")
    private double amount;
    
    @ColumnInfo(name = "category")
    private String category;
    
    @ColumnInfo(name = "subcategory")
    private String subcategory;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "date")
    private Date date;
    
    @ColumnInfo(name = "type")
    private String type; // "income" or "expense"
    
    @ColumnInfo(name = "payment_method")
    private String paymentMethod; // "cash", "card", "upi"
    
    @ColumnInfo(name = "created_at")
    private Date createdAt;
    
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    private boolean isDeleted;
    
    @ColumnInfo(name = "deleted_at")
    private Date deletedAt;
    
    @ColumnInfo(name = "is_essential", defaultValue = "1")
    private boolean isEssential; // true = Need (essential), false = Want (non-essential)

    // Constructors
    public Expense() {
        this.createdAt = new Date();
        this.isDeleted = false;
        this.isEssential = true; // Default to essential
    }

    @Ignore
    public Expense(String title, double amount, String category, String description, Date date, String type, String paymentMethod) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.subcategory = ""; // Default empty subcategory
        this.description = description;
        this.date = date;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.createdAt = new Date();
    }
    
    @Ignore
    public Expense(String title, double amount, String category, String subcategory, String description, Date date, String type, String paymentMethod) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.subcategory = subcategory;
        this.description = description;
        this.date = date;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    
    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
    
    public boolean isEssential() { return isEssential; }
    public void setEssential(boolean essential) { this.isEssential = essential; }
    
    /**
     * Soft delete this expense (marks as deleted without removing from DB)
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = new Date();
    }
    
    /**
     * Restore a soft-deleted expense
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}