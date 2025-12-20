package com.vishnu.expensetracker.models;

import com.vishnu.expensetracker.utils.MonthlyUtils;

/**
 * Model class representing monthly financial summary
 * Contains income, expenses, budget tracking, and comparison data
 */
public class MonthlySummary {
    
    private double totalIncome;
    private double totalExpenses;
    private double previousMonthExpenses;
    private double monthlyBudget;
    private String monthName; // e.g., "December 2025"
    
    // Calculated fields
    private double netBalance;
    private double budgetRemaining;
    private int budgetUsagePercent;
    private double percentChangeFromPrevious;
    private String comparisonMessage;
    
    public MonthlySummary() {}
    
    public MonthlySummary(double totalIncome, double totalExpenses, double previousMonthExpenses, 
                          double monthlyBudget, String monthName) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.previousMonthExpenses = previousMonthExpenses;
        this.monthlyBudget = monthlyBudget;
        this.monthName = monthName;
        
        // Calculate derived values
        calculateDerivedValues();
    }
    
    private void calculateDerivedValues() {
        // Net balance for the month
        this.netBalance = totalIncome - totalExpenses;
        
        // Budget calculations
        if (monthlyBudget > 0) {
            this.budgetRemaining = monthlyBudget - totalExpenses;
            this.budgetUsagePercent = MonthlyUtils.calculateBudgetUsagePercent(totalExpenses, monthlyBudget);
        } else {
            // If no budget set, use income as budget
            this.budgetRemaining = totalIncome - totalExpenses;
            this.budgetUsagePercent = totalIncome > 0 ? 
                    MonthlyUtils.calculateBudgetUsagePercent(totalExpenses, totalIncome) : 0;
        }
        
        // Monthly comparison
        this.percentChangeFromPrevious = MonthlyUtils.calculatePercentageChange(totalExpenses, previousMonthExpenses);
        this.comparisonMessage = MonthlyUtils.getSpendingComparisonMessage(totalExpenses, previousMonthExpenses);
    }
    
    // ========== GETTERS ==========
    
    public double getTotalIncome() {
        return totalIncome;
    }
    
    public double getTotalExpenses() {
        return totalExpenses;
    }
    
    public double getPreviousMonthExpenses() {
        return previousMonthExpenses;
    }
    
    public double getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public String getMonthName() {
        return monthName;
    }
    
    public double getNetBalance() {
        return netBalance;
    }
    
    public double getBudgetRemaining() {
        return budgetRemaining;
    }
    
    public int getBudgetUsagePercent() {
        return budgetUsagePercent;
    }
    
    public double getPercentChangeFromPrevious() {
        return percentChangeFromPrevious;
    }
    
    public String getComparisonMessage() {
        return comparisonMessage;
    }
    
    // ========== SETTERS ==========
    
    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
        calculateDerivedValues();
    }
    
    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
        calculateDerivedValues();
    }
    
    public void setPreviousMonthExpenses(double previousMonthExpenses) {
        this.previousMonthExpenses = previousMonthExpenses;
        calculateDerivedValues();
    }
    
    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
        calculateDerivedValues();
    }
    
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Check if spending is within budget
     */
    public boolean isWithinBudget() {
        return budgetRemaining >= 0;
    }
    
    /**
     * Check if spending increased compared to last month
     */
    public boolean isSpendingIncreased() {
        return percentChangeFromPrevious > 0;
    }
    
    /**
     * Check if there's a positive net balance this month
     */
    public boolean isNetPositive() {
        return netBalance >= 0;
    }
    
    /**
     * Get the effective budget (user-set budget or income)
     */
    public double getEffectiveBudget() {
        return monthlyBudget > 0 ? monthlyBudget : totalIncome;
    }
    
    /**
     * Get daily spending limit to stay within budget
     */
    public double getDailySpendingLimit() {
        return MonthlyUtils.getDailySpendingLimit(budgetRemaining);
    }
    
    /**
     * Get days remaining in the month
     */
    public int getDaysRemaining() {
        return MonthlyUtils.getDaysRemainingInMonth();
    }
    
    /**
     * Get savings rate (percentage of income saved)
     */
    public double getSavingsRate() {
        if (totalIncome <= 0) {
            return 0;
        }
        return ((totalIncome - totalExpenses) / totalIncome) * 100;
    }
    
    /**
     * Get a status message based on budget usage
     */
    public String getBudgetStatusMessage() {
        if (budgetUsagePercent < 50) {
            return "🎯 Great! You're on track";
        } else if (budgetUsagePercent < 75) {
            return "⚠️ Watch your spending";
        } else if (budgetUsagePercent < 100) {
            return "🔴 Approaching budget limit";
        } else {
            return "❌ Over budget!";
        }
    }
    
    /**
     * Get color resource based on budget status
     * Returns a color identifier: 0 = green, 1 = yellow, 2 = orange, 3 = red
     */
    public int getBudgetStatusColor() {
        if (budgetUsagePercent < 50) {
            return 0; // Green
        } else if (budgetUsagePercent < 75) {
            return 1; // Yellow
        } else if (budgetUsagePercent < 100) {
            return 2; // Orange
        } else {
            return 3; // Red
        }
    }
    
    @Override
    public String toString() {
        return "MonthlySummary{" +
                "monthName='" + monthName + '\'' +
                ", totalIncome=" + totalIncome +
                ", totalExpenses=" + totalExpenses +
                ", netBalance=" + netBalance +
                ", budgetUsagePercent=" + budgetUsagePercent +
                ", comparisonMessage='" + comparisonMessage + '\'' +
                '}';
    }
}
