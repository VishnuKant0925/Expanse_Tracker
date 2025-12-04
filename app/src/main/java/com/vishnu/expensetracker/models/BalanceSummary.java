package com.vishnu.expensetracker.models;

public class BalanceSummary {
    private double totalIncome;
    private double totalExpenses;
    private double currentBalance;
    private double savingsRate; // percentage of income saved
    
    public BalanceSummary() {}
    
    public BalanceSummary(double totalIncome, double totalExpenses) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = totalIncome - totalExpenses;
        this.savingsRate = totalIncome > 0 ? ((totalIncome - totalExpenses) / totalIncome) * 100 : 0;
    }
    
    // Getters and Setters
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { 
        this.totalIncome = totalIncome;
        updateCalculations();
    }
    
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { 
        this.totalExpenses = totalExpenses;
        updateCalculations();
    }
    
    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
    
    public double getSavingsRate() { return savingsRate; }
    public void setSavingsRate(double savingsRate) { this.savingsRate = savingsRate; }
    
    private void updateCalculations() {
        this.currentBalance = totalIncome - totalExpenses;
        this.savingsRate = totalIncome > 0 ? ((totalIncome - totalExpenses) / totalIncome) * 100 : 0;
    }
    
    public boolean isBalancePositive() {
        return currentBalance >= 0;
    }
    
    public double getExpenseRatio() {
        return totalIncome > 0 ? (totalExpenses / totalIncome) * 100 : 0;
    }
}