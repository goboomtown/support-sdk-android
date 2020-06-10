package com.goboomtown.supportsdk.view;

import android.view.KeyEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author http://innodroid.com/blog/post/tracking-selected-item-in-recyclerview; https://stackoverflow.com/questions/27194044/how-to-properly-highlight-selected-item-on-recyclerview
 * @author fbeachler
 */
public abstract class TrackSelectionAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {
    // Start with first item selected
    private int focusedItem = 0;

    protected abstract boolean trySelectFocusedItem(int position);

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyEvent.KEYCODE_ENTER) {
                        return trySelectFocusedItem(focusedItem);
                    }
                }
                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;
        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }
        return false;
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int i) {
        // Set selected state; use a state list drawable to style the view
        viewHolder.itemView.setSelected(focusedItem == i);
    }

    /**
     * @param focusedItem the focusedItem index to set
     */
    public void setFocusedItem(int focusedItem) {
        this.focusedItem = focusedItem;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            // Handle item click and set the selection
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redraw the old selection and the new
                    notifyItemChanged(focusedItem);
                    focusedItem = getLayoutPosition();
                    notifyItemChanged(focusedItem);
                }
            });
        }
    }
}