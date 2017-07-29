package com.example.wind.mycomic.custom;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by wind on 2016/12/31.
 */

public class CustomActionPresenter extends PresenterSelector {
    private Presenter comboActionPresenter = new CustomActionPresenter.ComboActionPresenter();
    private Presenter[] mPresenters = new Presenter[] { comboActionPresenter };
    private Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public Presenter getPresenter(Object item) {
        return comboActionPresenter;
    }

    @Override
    public Presenter[] getPresenters() {
        return mPresenters;
    }

    class ComboActionPresenter extends Presenter {
        private class ActionViewHolder extends Presenter.ViewHolder {
            CustomAction mAction;
            Button mButton;
            int mLayoutDirection;

            public ActionViewHolder(View view, int layoutDirection) {
                super(view);
                mButton = (Button) view.findViewById(android.support.v17.leanback.R.id.lb_action_button);
                mLayoutDirection = layoutDirection;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.support.v17.leanback.R.layout.lb_action_1_line, parent, false);
            return new CustomActionPresenter.ComboActionPresenter.ActionViewHolder(v, parent.getLayoutDirection());
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            final CustomAction action = (CustomAction) item;
            final CustomActionPresenter.ComboActionPresenter.ActionViewHolder vh = (CustomActionPresenter.ComboActionPresenter.ActionViewHolder) viewHolder;
            vh.mAction = action;
            vh.mButton.setText(action.getTitle());
        }

        @Override
        public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
            ((CustomActionPresenter.ComboActionPresenter.ActionViewHolder) viewHolder).mAction = null;
        }
    }
}