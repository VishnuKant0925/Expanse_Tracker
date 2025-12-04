package com.vishnu.expensetracker.utils;

import android.content.Context;
import android.graphics.Color;
import com.vishnu.expensetracker.R;
import com.vishnu.expensetracker.models.BalanceSummary;

public class BalanceUtils {
    
    public static String getBalanceStatusText(BalanceSummary balanceSummary) {
        double balance = balanceSummary.getCurrentBalance();
        
        if (balance > 0) {
            return "Money left to spend";
        } else if (balance == 0) {
            return "Break even";
        } else {
            return "Budget exceeded";
        }
    }
    
    public static int getBalanceColor(BalanceSummary balanceSummary, Context context) {
        double balance = balanceSummary.getCurrentBalance();
        
        if (balance >= 0) {
            return Color.WHITE;
        } else {
            return context.getResources().getColor(android.R.color.holo_red_light);
        }
    }
    
    public static String getSavingsRateText(BalanceSummary balanceSummary) {
        return String.format("%.1f%%", balanceSummary.getSavingsRate());
    }
    
    public static boolean shouldShowSavingsInfo(BalanceSummary balanceSummary) {
        return balanceSummary.getTotalIncome() > 0;
    }
    
    public static String getBalanceTrendDescription(BalanceSummary balanceSummary) {
        double savingsRate = balanceSummary.getSavingsRate();
        
        if (savingsRate >= 20) {
            return "Excellent savings! You're on track.";
        } else if (savingsRate >= 10) {
            return "Good savings rate. Keep it up!";
        } else if (savingsRate >= 0) {
            return "Consider saving more for the future.";
        } else {
            return "You're spending more than you earn.";
        }
    }
    
    public static int getTrendIcon(BalanceSummary balanceSummary) {
        double savingsRate = balanceSummary.getSavingsRate();
        
        if (savingsRate >= 10) {
            return R.drawable.ic_arrow_drop_down; // You can replace with trend up icon
        } else if (savingsRate >= 0) {
            return R.drawable.ic_arrow_drop_down; // You can replace with trend neutral icon
        } else {
            return R.drawable.ic_arrow_drop_down; // You can replace with trend down icon
        }
    }
    
    /**
     * Formats a percentage with appropriate color coding
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * Gets color for percentage values (green for positive, red for negative)
     */
    public static int getPercentageColor(double percentage, Context context) {
        if (percentage >= 0) {
            return context.getResources().getColor(android.R.color.holo_green_light);
        } else {
            return context.getResources().getColor(android.R.color.holo_red_light);
        }
    }
}