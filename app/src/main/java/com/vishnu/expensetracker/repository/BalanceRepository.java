package com.vishnu.expensetracker.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.vishnu.expensetracker.database.ExpenseDao;
import com.vishnu.expensetracker.models.BalanceSummary;

public class BalanceRepository {
    private ExpenseDao expenseDao;
    private MediatorLiveData<BalanceSummary> balanceSummaryLiveData;
    
    public BalanceRepository(ExpenseDao expenseDao) {
        this.expenseDao = expenseDao;
        balanceSummaryLiveData = new MediatorLiveData<>();
        setupBalanceCalculation();
    }
    
    private void setupBalanceCalculation() {
        LiveData<Double> incomeLiveData = expenseDao.getTotalIncome();
        LiveData<Double> expensesLiveData = expenseDao.getTotalExpenses();
        
        balanceSummaryLiveData.addSource(incomeLiveData, income -> {
            Double currentIncome = income != null ? income : 0.0;
            Double currentExpenses = expensesLiveData.getValue() != null ? expensesLiveData.getValue() : 0.0;
            balanceSummaryLiveData.setValue(new BalanceSummary(currentIncome, currentExpenses));
        });
        
        balanceSummaryLiveData.addSource(expensesLiveData, expenses -> {
            Double currentExpenses = expenses != null ? expenses : 0.0;
            Double currentIncome = incomeLiveData.getValue() != null ? incomeLiveData.getValue() : 0.0;
            balanceSummaryLiveData.setValue(new BalanceSummary(currentIncome, currentExpenses));
        });
    }
    
    public LiveData<BalanceSummary> getBalanceSummary() {
        return balanceSummaryLiveData;
    }
    
    public LiveData<Double> getTotalIncome() {
        return expenseDao.getTotalIncome();
    }
    
    public LiveData<Double> getTotalExpenses() {
        return expenseDao.getTotalExpenses();
    }
    
    public LiveData<Double> getCurrentBalance() {
        return expenseDao.getCurrentBalance();
    }
}