package com.vishnu.expensetracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

@Entity(tableName = "subcategories",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.CASCADE
        ))
public class Subcategory {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "category_id")
    private int categoryId;
    
    @ColumnInfo(name = "icon")
    private String icon;
    
    @ColumnInfo(name = "type")
    private String type; // "income" or "expense"

    // Constructors
    public Subcategory() {}

    @Ignore
    public Subcategory(String name, int categoryId, String icon, String type) {
        this.name = name;
        this.categoryId = categoryId;
        this.icon = icon;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}