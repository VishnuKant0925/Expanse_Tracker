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
import java.util.ArrayList;
import java.util.List;

public class SubcategoryAdapter extends RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder> {
    
    private List<String> subcategories;
    private Context context;
    private OnSubcategorySelectedListener listener;
    private int selectedPosition = -1;
    
    public interface OnSubcategorySelectedListener {
        void onSubcategorySelected(String subcategory, int position);
    }
    
    public SubcategoryAdapter(Context context, OnSubcategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.subcategories = new ArrayList<>();
    }
    
    public void updateSubcategories(List<String> subcategories) {
        this.subcategories = subcategories;
        this.selectedPosition = -1; // Reset selection
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
    
    public String getSelectedSubcategory() {
        if (selectedPosition != -1 && selectedPosition < subcategories.size()) {
            return subcategories.get(selectedPosition);
        }
        return null;
    }
    
    @NonNull
    @Override
    public SubcategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subcategory, parent, false);
        return new SubcategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SubcategoryViewHolder holder, int position) {
        String subcategory = subcategories.get(position);
        holder.bind(subcategory, position == selectedPosition);
    }
    
    @Override
    public int getItemCount() {
        return subcategories.size();
    }
    
    public class SubcategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubcategoryName;
        private View subcategoryContainer;
        
        public SubcategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubcategoryName = itemView.findViewById(R.id.tv_subcategory_name);
            subcategoryContainer = itemView.findViewById(R.id.subcategory_container);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(position);
                    listener.onSubcategorySelected(subcategories.get(position), position);
                }
            });
        }
        
        public void bind(String subcategory, boolean isSelected) {
            tvSubcategoryName.setText(subcategory);
            
            // Set background and text color based on selection
            if (isSelected) {
                subcategoryContainer.setBackgroundColor(Color.parseColor("#E3F2FD"));
                tvSubcategoryName.setTextColor(Color.parseColor("#1976D2"));
            } else {
                subcategoryContainer.setBackgroundColor(Color.TRANSPARENT);
                tvSubcategoryName.setTextColor(Color.parseColor("#424242"));
            }
        }
    }
}