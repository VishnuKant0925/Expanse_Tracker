package com.vishnu.expensetracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "categories")
public class Category {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "icon")
    private String icon;
    
    @ColumnInfo(name = "color")
    private String color;
    
    @ColumnInfo(name = "type")
    private String type; // "income" or "expense"

    // Constructors
    public Category() {}

    @Ignore
    public Category(String name, String icon, String color, String type) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}