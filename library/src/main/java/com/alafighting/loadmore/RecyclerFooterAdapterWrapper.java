package com.alafighting.loadmore;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SupportRecycledViewPool;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 支持底部显示加载更多的Adapter
 * @author alafighting 2016-01
 */
public abstract class RecyclerFooterAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_FOOTER = -1;

    private RecyclerView mRecycler;
    private RecyclerView.Adapter mRealAdapter;
    private boolean mIsLoadmoreEnabled = true;
    private boolean mIsLoadmoreing = false;

    private static class FooterHolder extends RecyclerView.ViewHolder {
        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public RecyclerFooterAdapterWrapper(RecyclerView recycler) {
        this.mRecycler = recycler;
        this.mRealAdapter = recycler.getAdapter();

        recycler.setAdapter(this);
        recycler.setRecycledViewPool(new SupportRecycledViewPool() {
            @Override
            public void onAdapterChanged(RecyclerView.Adapter oldAdapter,
                                         RecyclerView.Adapter newAdapter,
                                         boolean compatibleWithPrevious) {
                super.onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);

                if (newAdapter != RecyclerFooterAdapterWrapper.this) {
                    RecyclerFooterAdapterWrapper.this.setAdapter(newAdapter);
                    mRecycler.setAdapter(RecyclerFooterAdapterWrapper.this);
                }
            }
        });
    }

    public RecyclerView.Adapter getRealAdapter() {
        return this.mRealAdapter;
    }
    public void setAdapter(RecyclerView.Adapter adapter) {
        this.mRealAdapter = adapter;
    }
    public void setLoadmoreEnabled(boolean enabled) {
        if (mIsLoadmoreEnabled == enabled) {
            return;
        }

        mIsLoadmoreEnabled = enabled;
        if (!mIsLoadmoreEnabled) {
            mIsLoadmoreing = false;
        }
        notifyDataSetChangedAll();
    }
    public boolean isLoadingmoreEnabled() {
        return mIsLoadmoreEnabled;
    }
    public void setLoadmoreing(boolean loadmoreing) {
        if (mIsLoadmoreing == loadmoreing) {
            return;
        }

        mIsLoadmoreing = loadmoreing;
        notifyDataSetChangedAll();

        if (mIsLoadmoreing) {
            mRecycler.smoothScrollToPosition(getItemCount());
        }
    }
    public boolean isLoadingmoreing() {
        return mIsLoadmoreing;
    }

    /**
     * 刷新列表
     */
    private void notifyDataSetChangedAll() {
        super.notifyDataSetChanged();
        if (mRealAdapter != null) {
            mRealAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType){
            case TYPE_FOOTER:
                View footerView = onCreateFooterView(parent);
                holder = new FooterHolder(footerView);
                break;
            default:
                holder = mRealAdapter.onCreateViewHolder(parent, viewType);
                break;
        }
        return holder;
    }

    /**
     * 创建底部显示加载更多的View
     * @param parent
     * @return
     */
    protected abstract View onCreateFooterView(ViewGroup parent);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterHolder) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (params != null) {
                if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                    ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                }
            }
            return;
        }
        mRealAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (holder instanceof FooterHolder) {
            this.onBindViewHolder(holder, position);
            return;
        }
        mRealAdapter.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsLoadmoreEnabled && mIsLoadmoreing) {
            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }
        }
        return mRealAdapter.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (mIsLoadmoreEnabled && mIsLoadmoreing) {
            return mRealAdapter.getItemCount() + 1;
        }
        return mRealAdapter.getItemCount();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
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
        if (holder instanceof FooterHolder) {
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
        super.registerAdapterDataObserver(observer);
        mRealAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
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
