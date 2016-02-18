package com.alafighting.loadmore;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * 下拉刷新\上拉加载更多的辅助类
 * @author alafighting 2016-01
 */
public class RecyclerSwipeHelper {

    private static final int WHAT_ON_REFRESH = 101;
    private static final int WHAT_ON_LOADMORE = 102;

    /**
     * 默认触发加载更多的阀值
     */
    public static final int LOADING_TRIGGER_THRESHOLD = 3;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case WHAT_ON_REFRESH:
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                    break;

                case WHAT_ON_LOADMORE:
                    if (isRefreshing() || isLoadmoreing() || !isEnabledLoadmore()) {
                        return;
                    }
                    if (mRecycler.getAdapter() == null || mRecycler.getAdapter().getItemCount() == 0) {
                        return;
                    }

                    // 此处改变显示内容
                    mAdapterWrapper.setLoadmoreing(true);

                    if (mLoadmoreListener != null) {
                        mLoadmoreListener.onLoadmore();
                    }
                    break;
            }
        }
    };

    private RecyclerView.OnScrollListener WrapperScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            checkLoadmore();
        }
    };
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mAdapterWrapper.notifyDataSetChanged();
            checkLoadmore();
        }
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
            checkLoadmore();
        }
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
            checkLoadmore();
        }
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
            checkLoadmore();
        }
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
            checkLoadmore();
        }
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapterWrapper.notifyItemMoved(fromPosition, toPosition);
            checkLoadmore();
        }
    };

    private SwipeRefreshLayout mSwipe;
    private RecyclerView mRecycler;
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener;
    private OnLoadmoreListener mLoadmoreListener;
    private RecyclerFooterAdapterWrapper mAdapterWrapper;
    private SpanSizeLookupWrapper mSpanSizeLookup;
    private boolean mIsLoadmoreEnabled = true;
    private int mThreshold = LOADING_TRIGGER_THRESHOLD;

    public RecyclerSwipeHelper(SwipeRefreshLayout swipe, RecyclerView recycler) {
        this.mSwipe = swipe;
        this.mRecycler = recycler;

        // 刷新监听
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessage(WHAT_ON_REFRESH);
            }
        });

        // 滑动监听
        mRecycler.addOnScrollListener(WrapperScrollListener);

        // 包装数据源
        mAdapterWrapper = new RecyclerFooterAdapterWrapper(recycler) {
            @Override
            public void setAdapter(RecyclerView.Adapter adapter) {
                if (getRealAdapter() != null) {
                    unregisterAdapterDataObserver(mDataObserver);
                }
                super.setAdapter(adapter);
                if (getRealAdapter() != null) {
                    registerAdapterDataObserver(mDataObserver);
                }
            }
        };
        mAdapterWrapper.registerAdapterDataObserver(mDataObserver);

        // For GridLayoutManager use separate/customisable span lookup for loading row
        if (mRecycler.getLayoutManager() instanceof GridLayoutManager) {
            mSpanSizeLookup = new SpanSizeLookupWrapper((GridLayoutManager) mRecycler.getLayoutManager(), mAdapterWrapper);
            ((GridLayoutManager) mRecycler.getLayoutManager()).setSpanSizeLookup(mSpanSizeLookup);
        }
    }

    /**
     * 解除绑定
     */
    public void unbind() {
        mSwipe.setOnRefreshListener(null);
        mRecycler.removeOnScrollListener(WrapperScrollListener);   // Remove scroll listener
        mAdapterWrapper.unregisterAdapterDataObserver(mDataObserver); // Remove data observer
        mRecycler.setAdapter(mAdapterWrapper.getRealAdapter()); // Swap back original adapter
        if (mRecycler.getLayoutManager() instanceof GridLayoutManager && mSpanSizeLookup != null) {
            // Swap back original SpanSizeLookup
            GridLayoutManager.SpanSizeLookup spanSizeLookup = mSpanSizeLookup.getWrapper();
            ((GridLayoutManager) mRecycler.getLayoutManager()).setSpanSizeLookup(spanSizeLookup);
        }
    }

    /**
     * 设置刷新监听
     * @param listener 监听器
     */
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    /**
     * 设置加载更多监听
     * @param listener 监听器
     */
    public void setOnLoadmoreListener(OnLoadmoreListener listener) {
        mLoadmoreListener = listener;
    }

    /**
     * 设置加载进度条的创建器
     * @param creator 创建器
     */
    public void setFooterViewCreator(FooterViewCreator creator) {
        mAdapterWrapper.setFooterViewCreator(creator);
    }


    /**
     * 设置当前是否刷新状态
     * @param refreshing 是否刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        mSwipe.setRefreshing(refreshing);
    }
    /**
     * 返回当前是否刷新状态
     */
    public boolean isRefreshing() {
        return mSwipe.isRefreshing();
    }


    /**
     * 设置当前是否加载更多状态
     * @param loadmoreing 是否加载更多状态
     */
    public void setLoadmoreing(boolean loadmoreing) {
        mAdapterWrapper.setLoadmoreing(loadmoreing);
    }
    /**
     * 返回当前是否加载更多状态
     */
    public boolean isLoadmoreing() {
        return mAdapterWrapper.isLoadingmoreing();
    }

    /**
     * 设置是否启用加载更多功能
     * @param enabled 是否启用加载更多
     */
    public void setEnabledLoadmore(boolean enabled) {
        if (mIsLoadmoreEnabled != enabled) {
            mIsLoadmoreEnabled = enabled;
            if (!mIsLoadmoreEnabled) {
                setLoadmoreing(false);
            }
        }
    }
    public boolean isEnabledLoadmore() {
        return mIsLoadmoreEnabled;
    }

    /**
     * 设置触发加载更多的阀值
     * @param threshold 表示距底部有几个item时执行
     */
    public void setThreshold(int threshold) {
        this.mThreshold = Math.max(0, threshold);
    }

    public RecyclerView.Adapter getRealAdapter() {
        return mAdapterWrapper.getRealAdapter();
    }


    /**
     * 检查是否需要执行Loadmore
     */
    void checkLoadmore() {
        int visibleItemCount = mRecycler.getChildCount();
        int totalItemCount = mRecycler.getLayoutManager().getItemCount();

        int firstVisibleItemPosition;
        if (mRecycler.getLayoutManager() instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) mRecycler.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (mRecycler.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            // https://code.google.com/p/android/issues/detail?id=181461
            if (mRecycler.getLayoutManager().getChildCount() > 0) {
                firstVisibleItemPosition = ((StaggeredGridLayoutManager) mRecycler.getLayoutManager()).findFirstVisibleItemPositions(null)[0];
            } else {
                firstVisibleItemPosition = 0;
            }
        } else {
            throw new IllegalStateException("LayoutManager needs to subclass LinearLayoutManager or StaggeredGridLayoutManager");
        }

        // Check if end of the list is reached (counting threshold) or if there is no items at all
        if ((totalItemCount - visibleItemCount) <= (firstVisibleItemPosition + mThreshold)
                || totalItemCount == 0) {
            mHandler.sendEmptyMessage(WHAT_ON_LOADMORE);
        }
    }

}
