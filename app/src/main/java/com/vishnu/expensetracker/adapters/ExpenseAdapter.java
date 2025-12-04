package com.vishnu.expensetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.utils.CurrencyFormatter;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    
    private List<Expense> expenses;
    private Context context;
    private OnExpenseClickListener listener;
    
    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
        void onEditClick(Expense expense);
        void onDeleteClick(Expense expense);
    }
    
    public ExpenseAdapter(List<Expense> expenses, Context context) {
        this.expenses = expenses;
        this.context = context;
    }
    
    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }
    
    @Override
    public int getItemCount() {
        return expenses.size();
    }
    
    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }
    
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDate, tvPaymentMethod;
        MaterialButton btnEdit, btnDelete;
        View colorIndicator;
        
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_expense_title);
            tvCategory = itemView.findViewById(R.id.tv_expense_category);
            tvAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvDate = itemView.findViewById(R.id.tv_expense_date);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            btnEdit = itemView.findViewById(R.id.btn_edit_transaction);
            btnDelete = itemView.findViewById(R.id.btn_delete_transaction);
            colorIndicator = itemView.findViewById(R.id.color_indicator);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExpenseClick(expenses.get(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExpenseLongClick(expenses.get(position));
                        return true;
                    }
                }
                return false;
            });
            
            // Edit button click listener
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(expenses.get(position));
                    }
                }
            });
            
            // Delete button click listener
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(expenses.get(position));
                    }
                }
            });
        }
        
        public void bind(Expense expense) {
            tvTitle.setText(expense.getTitle());
            tvCategory.setText(CurrencyFormatter.getCategoryIcon(expense.getCategory()) + " " + expense.getCategory());
            tvDate.setText(CurrencyFormatter.formatDate(expense.getDate()));
            tvPaymentMethod.setText(expense.getPaymentMethod().toUpperCase());
            
            // Set amount with appropriate color
            String amountText = CurrencyFormatter.formatCurrency(expense.getAmount());
            if ("income".equals(expense.getType())) {
                tvAmount.setText("+ " + amountText);
                tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvAmount.setText("- " + amountText);
                tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }
            
            // Set category color indicator
            colorIndicator.setBackgroundColor(CurrencyFormatter.getCategoryColor(expense.getCategory()));
        }
    }
}