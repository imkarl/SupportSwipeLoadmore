package com.alafighting.loadmore;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecycledViewPoolWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 支持底部显示加载更多的Adapter
 * @author alafighting 2016-01
 */
public class RecyclerFooterAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_FOOTER = -1;

    private RecyclerView mRecycler;
    private RecyclerView.Adapter mRealAdapter;
    private FooterViewCreator mFooterViewCreator;
    private boolean mIsLoadmoreing = false;

    private FooterHolder mFooterHolder;

    private static class FooterHolder extends RecyclerView.ViewHolder {
        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public RecyclerFooterAdapterWrapper(RecyclerView recycler) {
        this.mRecycler = recycler;
        this.mRealAdapter = recycler.getAdapter();

        mRecycler.setAdapter(this);
        mRecycler.setRecycledViewPool(new RecycledViewPoolWrapper(recycler.getRecycledViewPool()) {
            @Override
            public void onAdapterChanged(RecyclerView.Adapter oldAdapter,
                                         RecyclerView.Adapter newAdapter) {
                super.onAdapterChanged(oldAdapter, newAdapter);

                RecyclerFooterAdapterWrapper wrapper = RecyclerFooterAdapterWrapper.this;
                if (newAdapter instanceof RecyclerFooterAdapterWrapper) {
                    wrapper = (RecyclerFooterAdapterWrapper) newAdapter;
                } else {
                    wrapper.setAdapter(newAdapter);
                }

                if (wrapper != RecyclerFooterAdapterWrapper.this) {
                    mRecycler.setAdapter(wrapper);
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType){
            case TYPE_FOOTER:
                if (mFooterHolder == null) {
                    mFooterHolder = new FooterHolder(createFooterView(parent));
                }
                holder = mFooterHolder;
                break;
            default:
                holder = mRealAdapter.onCreateViewHolder(parent, viewType);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterHolder) {
            if (isLoadingmoreing()) {
                holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.getLayoutParams().height = 10;
                holder.itemView.setVisibility(View.GONE);
            }
            return;
        }
        mRealAdapter.onBindViewHolder(holder, position);
    }

    public void setFooterViewCreator(FooterViewCreator creator) {
        this.mFooterViewCreator = creator;
    }

    /**
     * 创建底部显示加载更多的ViewmFooterHolder
     * @param parent 父级控件
     * @return 加载更多的View
     */
    View createFooterView(ViewGroup parent) {
        if (mFooterViewCreator != null) {
            return mFooterViewCreator.onCreateView(parent);
        }

        // 创建底部加载更多的View
        ProgressBar progressBar = new ProgressBar(parent.getContext());
        progressBar.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tips = new TextView(parent.getContext());
        tips.setId(android.R.id.text1);
        tips.setTextColor(Color.parseColor("#ff33b5e5"));
        tips.setText("加载中...");

        RelativeLayout layout = new RelativeLayout(parent.getContext());
        layout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(progressBar);
        layout.addView(tips);

        ((RelativeLayout.LayoutParams)tips.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);

        ((RelativeLayout.LayoutParams)progressBar.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
        ((RelativeLayout.LayoutParams)progressBar.getLayoutParams()).addRule(RelativeLayout.LEFT_OF, tips.getId());
        return layout;
    }

    public RecyclerView.Adapter getRealAdapter() {
        return this.mRealAdapter;
    }
    public void setAdapter(RecyclerView.Adapter adapter) {
        this.mRealAdapter = adapter;
    }

    public void setLoadmoreing(boolean loadmoreing) {
        if (mIsLoadmoreing != loadmoreing) {
            mIsLoadmoreing = loadmoreing;
            notifyItemChanged(getItemCount() - 1);
            if (mFooterHolder != null) {
                // 强制刷新
                onBindViewHolder(mFooterHolder, getItemCount() - 1);
            }
        }
    }
    public boolean isLoadingmoreing() {
        return mIsLoadmoreing;
    }

    boolean isLoadingRow(int position) {
        return position == getLoadingRowPosition();
    }

    private int getLoadingRowPosition() {
        return getItemCount() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isLoadingRow(position) ? TYPE_FOOTER : mRealAdapter.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mRealAdapter.getItemCount() + 1;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        if (mRealAdapter == null) {
            return;
        }
        mRealAdapter.setHasStableIds(hasStableIds);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterHolder) {
            // TODO
            return super.onFailedToRecycleView(holder);
        }
        return mRealAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public long getItemId(int position) {
        if (position == getItemCount()-1) {
            return super.getItemId(position);
        }
        return mRealAdapter.getItemId(position);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (mRealAdapter == null) {
            return;
        }
        mRealAdapter.onViewRecycled(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterHolder) {
            return;
        }
        mRealAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterHolder) {
            return;
        }
        mRealAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mRealAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mRealAdapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRealAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRealAdapter.onDetachedFromRecyclerView(recyclerView);
    }

}
