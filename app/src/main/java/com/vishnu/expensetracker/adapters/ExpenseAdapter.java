package com.vishnu.expensetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.models.Expense;
import com.vishnu.expensetracker.utils.CurrencyFormatter;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    
    private List<Expense> expenses;
    private Context context;
    private OnExpenseClickListener listener;
    private boolean showDeleteConfirmation = true; // Flag to enable/disable confirmation dialog
    
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
    
    /**
     * Enable or disable the delete confirmation dialog
     * @param show true to show confirmation, false to delete immediately
     */
    public void setShowDeleteConfirmation(boolean show) {
        this.showDeleteConfirmation = show;
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
    
    /**
     * Get the current list of expenses
     * @return List of expenses
     */
    public List<Expense> getExpenses() {
        return expenses;
    }
    
    /**
     * Remove an expense at a specific position (for swipe-to-delete animation)
     * @param position Position to remove
     */
    public void removeAt(int position) {
        if (position >= 0 && position < expenses.size()) {
            expenses.remove(position);
            notifyItemRemoved(position);
        }
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
                        Expense expense = expenses.get(position);
                        // Show delete confirmation on long click
                        showDeleteConfirmationDialog(expense);
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
            
            // Delete button click listener with confirmation
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Expense expense = expenses.get(position);
                        if (showDeleteConfirmation) {
                            showDeleteConfirmationDialog(expense);
                        } else {
                            listener.onDeleteClick(expense);
                        }
                    }
                }
            });
        }
        
        /**
         * Show a confirmation dialog before deleting a transaction
         */
        private void showDeleteConfirmationDialog(Expense expense) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?\n\n" +
                            "📝 " + expense.getTitle() + "\n" +
                            "💰 " + CurrencyFormatter.formatCurrency(expense.getAmount()) + "\n" +
                            "📁 " + expense.getCategory())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (listener != null) {
                            listener.onDeleteClick(expense);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
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