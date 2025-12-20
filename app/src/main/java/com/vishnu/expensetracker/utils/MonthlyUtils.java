package com.vishnu.expensetracker.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for monthly date calculations
 * Provides methods to calculate start/end timestamps for current and previous months
 */
public class MonthlyUtils {
    
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    
    /**
     * Represents a date range with start and end timestamps
     */
    public static class DateRange {
        private final long startTimestamp;
        private final long endTimestamp;
        private final String monthYear; // Format: "2025-12"
        private final String displayName; // Format: "December 2025"
        
        public DateRange(long startTimestamp, long endTimestamp, String monthYear, String displayName) {
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.monthYear = monthYear;
            this.displayName = displayName;
        }
        
        public long getStartTimestamp() { return startTimestamp; }
        public long getEndTimestamp() { return endTimestamp; }
        public String getMonthYear() { return monthYear; }
        public String getDisplayName() { return displayName; }
        
        public Date getStartDate() { return new Date(startTimestamp); }
        public Date getEndDate() { return new Date(endTimestamp); }
    }
    
    /**
     * Get the date range for the current month
     * @return DateRange containing start and end timestamps for current month
     */
    public static DateRange getCurrentMonthRange() {
        Calendar calendar = Calendar.getInstance();
        return getMonthRange(calendar);
    }
    
    /**
     * Get the date range for the previous month
     * @return DateRange containing start and end timestamps for previous month
     */
    public static DateRange getPreviousMonthRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return getMonthRange(calendar);
    }
    
    /**
     * Get the date range for a specific month offset from current month
     * @param monthOffset Negative for past months, positive for future months
     * @return DateRange for the specified month
     */
    public static DateRange getMonthRange(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);
        return getMonthRange(calendar);
    }
    
    /**
     * Get the date range for a specific calendar month
     * @param calendar Calendar set to the desired month
     * @return DateRange containing start and end timestamps
     */
    public static DateRange getMonthRange(Calendar calendar) {
        // Clone to avoid modifying original
        Calendar cal = (Calendar) calendar.clone();
        
        // Get month year string before modifying
        String monthYear = MONTH_YEAR_FORMAT.format(cal.getTime());
        String displayName = DISPLAY_FORMAT.format(cal.getTime());
        
        // Set to first day of month at 00:00:00.000
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTimestamp = cal.getTimeInMillis();
        
        // Set to last day of month at 23:59:59.999
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endTimestamp = cal.getTimeInMillis();
        
        return new DateRange(startTimestamp, endTimestamp, monthYear, displayName);
    }
    
    /**
     * Get the date range for a specific year and month
     * @param year The year (e.g., 2025)
     * @param month The month (1-12)
     * @return DateRange for the specified month
     */
    public static DateRange getMonthRange(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Calendar months are 0-indexed
        return getMonthRange(calendar);
    }
    
    /**
     * Get the current month-year string in "yyyy-MM" format
     * @return Current month-year string
     */
    public static String getCurrentMonthYear() {
        return MONTH_YEAR_FORMAT.format(new Date());
    }
    
    /**
     * Get the previous month-year string in "yyyy-MM" format
     * @return Previous month-year string
     */
    public static String getPreviousMonthYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return MONTH_YEAR_FORMAT.format(calendar.getTime());
    }
    
    /**
     * Format a date to display format (e.g., "December 2025")
     * @param date The date to format
     * @return Formatted display string
     */
    public static String formatMonthDisplay(Date date) {
        return DISPLAY_FORMAT.format(date);
    }
    
    /**
     * Calculate the percentage change between two values
     * @param currentValue Current month's value
     * @param previousValue Previous month's value
     * @return Percentage change (positive means increase, negative means decrease)
     */
    public static double calculatePercentageChange(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }
    
    /**
     * Generate a comparison message for monthly spending
     * @param currentSpending Current month's spending
     * @param previousSpending Previous month's spending
     * @return Human-readable comparison message
     */
    public static String getSpendingComparisonMessage(double currentSpending, double previousSpending) {
        double percentChange = calculatePercentageChange(currentSpending, previousSpending);
        
        if (previousSpending == 0 && currentSpending == 0) {
            return "No spending recorded yet";
        }
        
        if (previousSpending == 0) {
            return "First month of tracking";
        }
        
        String absPercent = String.format(Locale.getDefault(), "%.0f%%", Math.abs(percentChange));
        
        if (percentChange > 0) {
            return "📈 You spent " + absPercent + " more than last month";
        } else if (percentChange < 0) {
            return "📉 You spent " + absPercent + " less than last month";
        } else {
            return "➡️ Same spending as last month";
        }
    }
    
    /**
     * Calculate budget usage percentage
     * @param spent Amount spent
     * @param budget Total budget
     * @return Percentage of budget used (0-100+)
     */
    public static int calculateBudgetUsagePercent(double spent, double budget) {
        if (budget <= 0) {
            return 0;
        }
        return (int) Math.min(100, (spent / budget) * 100);
    }
    
    /**
     * Get days remaining in the current month
     * @return Number of days remaining
     */
    public static int getDaysRemainingInMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return lastDay - currentDay;
    }
    
    /**
     * Get the daily average spending needed to stay within budget
     * @param remainingBudget Remaining budget amount
     * @return Daily spending limit
     */
    public static double getDailySpendingLimit(double remainingBudget) {
        int daysRemaining = getDaysRemainingInMonth();
        if (daysRemaining <= 0) {
            return remainingBudget;
        }
        return remainingBudget / daysRemaining;
    }
    
    /**
     * Check if a date is within the current month
     * @param date Date to check
     * @return true if the date is in the current month
     */
    public static boolean isInCurrentMonth(Date date) {
        DateRange currentMonth = getCurrentMonthRange();
        long timestamp = date.getTime();
        return timestamp >= currentMonth.getStartTimestamp() && timestamp <= currentMonth.getEndTimestamp();
    }
    
    /**
     * Get elapsed days in current month (including today)
     * @return Number of days elapsed
     */
    public static int getElapsedDaysInMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Get total days in current month
     * @return Total days in the month
     */
    public static int getTotalDaysInMonth() {
        return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
