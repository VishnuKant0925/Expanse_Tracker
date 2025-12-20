package com.vishnu.expensetracker.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date calculations used in Analytics and other features
 * Provides helper methods for getting date ranges, formatting, and calculations
 */
public class DateUtils {
    
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat SHORT_MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("'Week' W", Locale.getDefault());
    
    // ========== TODAY ==========
    
    /**
     * Get the start of today (00:00:00.000)
     */
    public static long getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the end of today (23:59:59.999)
     */
    public static long getEndOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    // ========== CURRENT WEEK ==========
    
    /**
     * Get the start of current week (Monday 00:00:00.000)
     */
    public static long getStartOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the end of current week (Sunday 23:59:59.999)
     */
    public static long getEndOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    // ========== CURRENT MONTH ==========
    
    /**
     * Get the start of current month (1st day 00:00:00.000)
     */
    public static long getStartOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the end of current month (last day 23:59:59.999)
     */
    public static long getEndOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    // ========== SPECIFIC MONTH ==========
    
    /**
     * Get the start of a specific month
     * @param year The year
     * @param month The month (0-based, January = 0)
     */
    public static long getStartOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the end of a specific month
     * @param year The year
     * @param month The month (0-based, January = 0)
     */
    public static long getEndOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the number of days in a specific month
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Get the current day of month (1-31)
     */
    public static int getCurrentDayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
    
    // ========== WEEK CALCULATIONS (for Bar Chart) ==========
    
    /**
     * Get the start of a week N weeks ago
     * @param weeksAgo Number of weeks ago (0 = current week, 1 = last week, etc.)
     */
    public static long getStartOfWeekAgo(int weeksAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the end of a week N weeks ago
     * @param weeksAgo Number of weeks ago (0 = current week, 1 = last week, etc.)
     */
    public static long getEndOfWeekAgo(int weeksAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo);
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the start of 4 weeks ago (for weekly comparison chart)
     */
    public static long getStartOfFourWeeksAgo() {
        return getStartOfWeekAgo(3);
    }
    
    /**
     * Get the week label for a week N weeks ago
     * @param weeksAgo Number of weeks ago (0 = "This Week", 1 = "Last Week", etc.)
     */
    public static String getWeekLabel(int weeksAgo) {
        if (weeksAgo == 0) return "This Week";
        if (weeksAgo == 1) return "Last Week";
        return weeksAgo + " Weeks Ago";
    }
    
    /**
     * Get short week label (Week 1, Week 2, etc.)
     */
    public static String getShortWeekLabel(int weekIndex) {
        return "Week " + (weekIndex + 1);
    }
    
    // ========== FORMATTING ==========
    
    /**
     * Format date as "January 2024"
     */
    public static String formatMonthYear(Date date) {
        return MONTH_YEAR_FORMAT.format(date);
    }
    
    /**
     * Format date as "Jan"
     */
    public static String formatShortMonth(Date date) {
        return SHORT_MONTH_FORMAT.format(date);
    }
    
    /**
     * Format date as "15" (day number)
     */
    public static String formatDay(Date date) {
        return DAY_FORMAT.format(date);
    }
    
    /**
     * Format date as "Jan 15, 2024"
     */
    public static String formatFullDate(Date date) {
        return FULL_DATE_FORMAT.format(date);
    }
    
    /**
     * Format timestamp to "January 2024"
     */
    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Get current month name and year (e.g., "January 2024")
     */
    public static String getCurrentMonthYear() {
        return MONTH_YEAR_FORMAT.format(new Date());
    }
    
    /**
     * Get month name from month index (0-based)
     */
    public static String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month];
    }
    
    /**
     * Get short month name from month index (0-based)
     */
    public static String getShortMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get current year
     */
    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
    
    /**
     * Get current month (0-based)
     */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }
    
    /**
     * Get the current week of year
     */
    public static int getCurrentWeekOfYear() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }
    
    /**
     * Calculate days remaining in current month
     */
    public static int getDaysRemainingInMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return totalDays - currentDay;
    }
    
    /**
     * Calculate the daily budget based on monthly budget
     * @param monthlyBudget Total monthly budget
     * @return Suggested daily spending limit
     */
    public static double calculateDailyBudget(double monthlyBudget) {
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return monthlyBudget / daysInMonth;
    }
    
    /**
     * Calculate the safe zone daily limit (budget / days remaining)
     * @param remainingBudget Remaining budget for the month
     * @return Suggested daily spending limit based on remaining days
     */
    public static double calculateSafeZoneLimit(double remainingBudget) {
        int daysRemaining = getDaysRemainingInMonth();
        if (daysRemaining <= 0) return remainingBudget;
        return remainingBudget / daysRemaining;
    }
    
    /**
     * Check if a date is in the current month
     */
    public static boolean isInCurrentMonth(Date date) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        
        Calendar now = Calendar.getInstance();
        return dateCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
               dateCal.get(Calendar.MONTH) == now.get(Calendar.MONTH);
    }
    
    /**
     * Check if a timestamp is in the current month
     */
    public static boolean isInCurrentMonth(long timestamp) {
        return isInCurrentMonth(new Date(timestamp));
    }
    
    /**
     * Get progress through the current month (0.0 to 1.0)
     */
    public static float getMonthProgress() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return (float) currentDay / totalDays;
    }
    
    /**
     * Get the date for a specific day in the current month
     */
    public static Date getDateForDayInCurrentMonth(int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * Parse day number from date string "YYYY-MM-DD"
     */
    public static int parseDayFromDateString(String dateStr) {
        try {
            return Integer.parseInt(dateStr.substring(8, 10));
        } catch (Exception e) {
            return 1;
        }
    }
}
