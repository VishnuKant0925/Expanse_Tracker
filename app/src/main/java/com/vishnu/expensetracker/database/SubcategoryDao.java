package com.vishnu.expensetracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.vishnu.expensetracker.models.Subcategory;
import java.util.List;

@Dao
public interface SubcategoryDao {
    
    @Insert
    void insert(Subcategory subcategory);
    
    @Insert
    void insertAll(List<Subcategory> subcategories);
    
    @Update
    void update(Subcategory subcategory);
    
    @Delete
    void delete(Subcategory subcategory);
    
    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    LiveData<List<Subcategory>> getAllSubcategories();
    
    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId ORDER BY name ASC")
    LiveData<List<Subcategory>> getSubcategoriesByCategoryId(int categoryId);
    
    @Query("SELECT * FROM subcategories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Subcategory>> getSubcategoriesByType(String type);
    
    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId AND type = :type ORDER BY name ASC")
    LiveData<List<Subcategory>> getSubcategoriesByCategoryIdAndType(int categoryId, String type);
    
    @Query("DELETE FROM subcategories")
    void deleteAllSubcategories();
    
    @Query("DELETE FROM subcategories WHERE category_id = :categoryId")
    void deleteSubcategoriesByCategoryId(int categoryId);
}