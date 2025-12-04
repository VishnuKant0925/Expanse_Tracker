package com.vishnu.expensetracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.utils.CategoryManager;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<CategoryManager.CategoryData> categories;
    private Context context;
    private OnCategorySelectedListener listener;
    private int selectedPosition = -1;
    
    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryManager.CategoryData category, int position);
    }
    
    public CategoryAdapter(Context context, OnCategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.categories = new ArrayList<>();
    }
    
    public void updateCategories(List<CategoryManager.CategoryData> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    public void setSelectedPosition(int position) {
        int previousSelection = selectedPosition;
        selectedPosition = position;
        
        if (previousSelection != -1) {
            notifyItemChanged(previousSelection);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryManager.CategoryData category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryIcon;
        private TextView tvCategoryName;
        private View categoryContainer;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            categoryContainer = itemView.findViewById(R.id.category_container);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(position);
                    listener.onCategorySelected(categories.get(position), position);
                }
            });
        }
        
        public void bind(CategoryManager.CategoryData category, boolean isSelected) {
            tvCategoryIcon.setText(category.icon);
            tvCategoryName.setText(category.name);
            
            // Set background color based on selection and category color
            if (isSelected) {
                categoryContainer.setBackgroundColor(Color.parseColor(category.color));
                tvCategoryName.setTextColor(Color.WHITE);
                tvCategoryIcon.setAlpha(1.0f);
            } else {
                categoryContainer.setBackgroundColor(Color.TRANSPARENT);
                tvCategoryName.setTextColor(Color.parseColor("#212121"));
                tvCategoryIcon.setAlpha(0.7f);
            }
        }
    }
}