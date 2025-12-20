package com.vishnu.expensetracker.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper callback for swipe-to-delete functionality
 * Provides visual feedback with red background and delete icon when swiping
 */
public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Context context;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Drawable deleteIcon;
    private final int iconMargin;
    private final Paint clearPaint;

    public SwipeToDeleteCallback(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.background = new ColorDrawable();
        this.backgroundColor = Color.parseColor("#F44336"); // Red color
        this.deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
        if (this.deleteIcon != null) {
            this.deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        this.iconMargin = 32;
        this.clearPaint = new Paint();
        this.clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder, 
                         @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support drag & drop
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                           int actionState, boolean isCurrentlyActive) {
        
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();
        
        boolean isCancelled = dX == 0 && !isCurrentlyActive;
        
        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(),
                    (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
            return;
        }
        
        // Draw red background
        background.setColor(backgroundColor);
        
        if (dX < 0) {
            // Swiping to the left
            background.setBounds(
                    itemView.getRight() + (int) dX,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
        } else {
            // Swiping to the right
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + (int) dX,
                    itemView.getBottom()
            );
        }
        background.draw(c);
        
        // Draw delete icon
        if (deleteIcon != null) {
            int iconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            
            if (dX < 0) {
                // Swiping left - icon on right
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else {
                // Swiping right - icon on left
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }
            deleteIcon.draw(c);
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, float left, float top, float right, float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.4f; // 40% of the item width to trigger delete
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 0.5f; // Make swiping easier
    }
}
