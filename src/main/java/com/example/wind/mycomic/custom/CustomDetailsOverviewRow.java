package com.example.wind.mycomic.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wind on 2016/12/31.
 */

public class CustomDetailsOverviewRow extends Row {
    /**
     * Listener for changes of CustomDetailsOverviewRow.
     */
    public static class Listener {

        /**
         * Called when CustomDetailsOverviewRow has changed image drawable.
         */
        public void onImageDrawableChanged(CustomDetailsOverviewRow row) {
        }

        /**
         * Called when CustomDetailsOverviewRow has changed main item.
         */
        public void onItemChanged(CustomDetailsOverviewRow row) {
        }

        /**
         * Called when CustomDetailsOverviewRow has changed actions adapter.
         */
        public void onActionsAdapterChanged(CustomDetailsOverviewRow row) {
        }
    }

    private Object mItem;
    private Drawable mImageDrawable;
    private boolean mImageScaleUpAllowed = true;
    private ArrayList<WeakReference<Listener>> mListeners;
    private PresenterSelector mDefaultActionPresenter = new CustomActionPresenter();
    private ObjectAdapter mActionsAdapter = new ArrayObjectAdapter(mDefaultActionPresenter);
    private Context mContext;
    /**
     * Constructor for a CustomDetailsOverviewRow.
     *
     * @param item The main item for the details page.
     */
    public CustomDetailsOverviewRow(Object item, Context context) {
        super(null);
        mItem = item;
        mContext = context;
        ((CustomActionPresenter)mDefaultActionPresenter).setContext(mContext);
        verify();
    }

    /**
     * Adds listener for the details page.
     */
    final void addListener(Listener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<WeakReference<Listener>>();
        } else {
            for (int i = 0; i < mListeners.size();) {
                Listener l = mListeners.get(i).get();
                if (l == null) {
                    mListeners.remove(i);
                } else {
                    if (l == listener) {
                        return;
                    }
                    i++;
                }
            }
        }
        mListeners.add(new WeakReference<Listener>(listener));
    }

    /**
     * Removes listener of the details page.
     */
    final void removeListener(Listener listener) {
        if (mListeners != null) {
            for (int i = 0; i < mListeners.size();) {
                Listener l = mListeners.get(i).get();
                if (l == null) {
                    mListeners.remove(i);
                } else {
                    if (l == listener) {
                        mListeners.remove(i);
                        return;
                    }
                    i++;
                }
            }
        }
    }

    /**
     * Notifies listeners for main item change on UI thread.
     */
    final void notifyItemChanged() {
        if (mListeners != null) {
            for (int i = 0; i < mListeners.size();) {
                Listener l = mListeners.get(i).get();
                if (l == null) {
                    mListeners.remove(i);
                } else {
                    l.onItemChanged(this);
                    i++;
                }
            }
        }
    }

    /**
     * Notifies listeners for image related change on UI thread.
     */
    final void notifyImageDrawableChanged() {
        if (mListeners != null) {
            for (int i = 0; i < mListeners.size();) {
                Listener l = mListeners.get(i).get();
                if (l == null) {
                    mListeners.remove(i);
                } else {
                    l.onImageDrawableChanged(this);
                    i++;
                }
            }
        }
    }

    /**
     * Notifies listeners for actions adapter changed on UI thread.
     */
    final void notifyActionsAdapterChanged() {
        if (mListeners != null) {
            for (int i = 0; i < mListeners.size();) {
                Listener l = mListeners.get(i).get();
                if (l == null) {
                    mListeners.remove(i);
                } else {
                    l.onActionsAdapterChanged(this);
                    i++;
                }
            }
        }
    }

    /**
     * Returns the main item for the details page.
     */
    public final Object getItem() {
        return mItem;
    }

    /**
     * Sets the main item for the details page.  Must be called on UI thread after
     * row is bound to view.
     */
    public final void setItem(Object item) {
        if (item != mItem) {
            mItem = item;
            notifyItemChanged();
        }
    }

    /**
     * Sets a drawable as the image of this details overview.  Must be called on UI thread
     * after row is bound to view.
     *
     * @param drawable The drawable to set.
     */
    public final void setImageDrawable(Drawable drawable) {
        if (mImageDrawable != drawable) {
            mImageDrawable = drawable;
            notifyImageDrawableChanged();
        }
    }

    /**
     * Sets a Bitmap as the image of this details overview.  Must be called on UI thread
     * after row is bound to view.
     *
     * @param context The context to retrieve display metrics from.
     * @param bm The bitmap to set.
     */
    public final void setImageBitmap(Context context, Bitmap bm) {
        mImageDrawable = new BitmapDrawable(context.getResources(), bm);
        notifyImageDrawableChanged();
    }

    /**
     * Returns the image drawable of this details overview.
     *
     * @return The overview's image drawable, or null if no drawable has been
     *         assigned.
     */
    public final Drawable getImageDrawable() {
        return mImageDrawable;
    }

    /**
     * Allows or disallows scaling up of images.
     * Images will always be scaled down if necessary.  Must be called on UI thread
     * after row is bound to view.
     */
    public void setImageScaleUpAllowed(boolean allowed) {
        if (allowed != mImageScaleUpAllowed) {
            mImageScaleUpAllowed = allowed;
            notifyImageDrawableChanged();
        }
    }

    /**
     * Returns true if the image may be scaled up; false otherwise.
     */
    public boolean isImageScaleUpAllowed() {
        return mImageScaleUpAllowed;
    }

    /**
     * Returns the actions adapter.  Throws ClassCastException if the current
     * actions adapter is not an instance of {@link ArrayObjectAdapter}.
     */
    private ArrayObjectAdapter getArrayObjectAdapter() {
        return (ArrayObjectAdapter) mActionsAdapter;
    }

    /**
     * Adds an Action to the overview. It will throw ClassCastException if the current actions
     * adapter is not an instance of {@link ArrayObjectAdapter}. Must be called on the UI thread.
     *
     * @param action The Action to add.
     * @deprecated Use {@link #setActionsAdapter(ObjectAdapter)} and {@link #getActionsAdapter()}
     */
    @Deprecated
    public final void addAction(CustomAction action) {
        getArrayObjectAdapter().add(action);
    }

    /**
     * Adds an Action to the overview at the specified position. It will throw ClassCastException if
     * current actions adapter is not an instance of f{@link ArrayObjectAdapter}. Must be called
     * on the UI thread.
     *
     * @param pos The position to insert the Action.
     * @param action The Action to add.
     * @deprecated Use {@link #setActionsAdapter(ObjectAdapter)} and {@link #getActionsAdapter()}
     */
    @Deprecated
    public final void addAction(int pos, CustomAction action) {
        getArrayObjectAdapter().add(pos, action);
    }

    /**
     * Removes the given Action from the overview. It will throw ClassCastException if current
     * actions adapter is not {@link ArrayObjectAdapter}. Must be called on UI thread.
     *
     * @param action The Action to remove.
     * @return true if the overview contained the specified Action.
     * @deprecated Use {@link #setActionsAdapter(ObjectAdapter)} and {@link #getActionsAdapter()}
     */
    @Deprecated
    public final boolean removeAction(CustomAction action) {
        return getArrayObjectAdapter().remove(action);
    }

    /**
     * Returns a read-only view of the list of Actions of this details overview. It will throw
     * ClassCastException if current actions adapter is not {@link ArrayObjectAdapter}. Must be
     * called on UI thread.
     *
     * @return An unmodifiable view of the list of Actions.
     * @deprecated Use {@link #setActionsAdapter(ObjectAdapter)} and {@link #getActionsAdapter()}
     */
    @Deprecated
    public final List<CustomAction> getActions() {
        return getArrayObjectAdapter().unmodifiableList();
    }

    /**
     * Returns the {@link ObjectAdapter} for actions.
     */
    public final ObjectAdapter getActionsAdapter() {
        return mActionsAdapter;
    }

    /**
     * Sets the {@link ObjectAdapter} for actions.  A default {@link PresenterSelector} will be
     * attached to the adapter if it doesn't have one.
     *
     * @param adapter  Adapter for actions.
     */
    public final void setActionsAdapter(ObjectAdapter adapter) {
        if (adapter != mActionsAdapter) {
            mActionsAdapter = adapter;
            if (mActionsAdapter.getPresenterSelector() == null) {
                mActionsAdapter.setPresenterSelector(mDefaultActionPresenter);
            }
            notifyActionsAdapterChanged();
        }
    }

    private void verify() {
        if (mItem == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
    }
}

