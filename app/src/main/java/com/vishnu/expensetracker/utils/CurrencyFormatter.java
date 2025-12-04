package com.vishnu.expensetracker.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrencyFormatter {
    
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    public static String formatCurrency(double amount) {
        return "₹" + currencyFormat.format(amount);
    }
    
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }
    
    public static String formatTime(Date date) {
        return timeFormat.format(date);
    }
    
    public static String formatDateTime(Date date) {
        return formatDate(date) + " at " + formatTime(date);
    }
    
    public static String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "food": return "🍽️";
            case "transport": return "🚗";
            case "entertainment": return "🎬";
            case "shopping": return "🛍️";
            case "bills": return "📄";
            case "health": return "🏥";
            case "education": return "📚";
            case "salary": return "💰";
            case "business": return "💼";
            case "investment": return "📈";
            default: return "💳";
        }
    }
    
    public static int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "food": return android.graphics.Color.parseColor("#FF5722");
            case "transport": return android.graphics.Color.parseColor("#2196F3");
            case "entertainment": return android.graphics.Color.parseColor("#E91E63");
            case "shopping": return android.graphics.Color.parseColor("#9C27B0");
            case "bills": return android.graphics.Color.parseColor("#FF9800");
            case "health": return android.graphics.Color.parseColor("#4CAF50");
            case "education": return android.graphics.Color.parseColor("#3F51B5");
            case "salary": return android.graphics.Color.parseColor("#4CAF50");
            case "business": return android.graphics.Color.parseColor("#607D8B");
            case "investment": return android.graphics.Color.parseColor("#795548");
            default: return android.graphics.Color.parseColor("#9E9E9E");
        }
    }
}