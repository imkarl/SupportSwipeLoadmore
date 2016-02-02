package com.alafighting.loadmore;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 下拉刷新\上拉加载更多的辅助类
 * @author alafighting 2016-01
 */
public class RecyclerSwipeHelper {

    private static final int WHAT_ON_REFRESH = 101;
    private static final int WHAT_ON_LOADMORE = 102;
    private static final int WHAT_DISPATCH_NOTIFY_REFRESH = 103;
    private static final int WHAT_DISPATCH_NOTIFY_LOADMORE = 104;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case WHAT_DISPATCH_NOTIFY_REFRESH:
                    Object[] argsRefresh = (Object[]) msg.obj;
                    RecyclerFooterAdapterWrapper wrapperRefresh = (RecyclerFooterAdapterWrapper) argsRefresh[0];
                    List<WeakReference<SwipeRefreshLayout.OnRefreshListener>> listenersRefresh = (List<WeakReference<SwipeRefreshLayout.OnRefreshListener>>) argsRefresh[1];

                    wrapperRefresh.setLoadmoreEnabled(false);
                    for (WeakReference<SwipeRefreshLayout.OnRefreshListener> weak : listenersRefresh) {
                        SwipeRefreshLayout.OnRefreshListener listener = weak.get();
                        if (listener != null) {
                            onNotify(listener);
                        }
                    }
                    break;

                case WHAT_DISPATCH_NOTIFY_LOADMORE:
                    Object[] argsLoadmore = (Object[]) msg.obj;
                    RecyclerFooterAdapterWrapper wrapperLoadmore = (RecyclerFooterAdapterWrapper) argsLoadmore[0];
                    List<WeakReference<OnLoadmoreListener>> listenersLoadmore = (List<WeakReference<OnLoadmoreListener>>) argsLoadmore[1];
                    if (!wrapperLoadmore.isLoadingmoreEnabled() || wrapperLoadmore.isLoadingmoreing()) {
                        return;
                    }

                    // TODO 此处需要禁用下拉功能

                    wrapperLoadmore.setLoadmoreing(true);
                    for (WeakReference<OnLoadmoreListener> weak : listenersLoadmore) {
                        OnLoadmoreListener listener = weak.get();
                        if (listener != null) {
                            onNotify(listener);
                        }
                    }
                    break;

                case WHAT_ON_REFRESH:
                case WHAT_ON_LOADMORE:
                    Object listener = msg.obj;
                    if (listener != null) {
                        if (listener instanceof SwipeRefreshLayout.OnRefreshListener) {
                            ((SwipeRefreshLayout.OnRefreshListener) listener).onRefresh();
                        } else if (listener instanceof OnLoadmoreListener) {
                            ((OnLoadmoreListener) listener).onLoadmore();
                        }
                    }
                    break;
            }
        }
    };

    /**
     * 通知刷新
     * @param listener
     */
    private static void onNotify(SwipeRefreshLayout.OnRefreshListener listener) {
        Message message = new Message();
        message.what = WHAT_ON_REFRESH;
        message.obj = listener;
        mHandler.sendMessage(message);
    }
    /**
     * 通知加载更多
     * @param listener
     */
    private static void onNotify(OnLoadmoreListener listener) {
        Message message = new Message();
        message.what = WHAT_ON_LOADMORE;
        message.obj = listener;
        mHandler.sendMessage(message);
    }

    /**
     * 分发刷新通知
     */
    private void dispatchNotifyRefresh() {
        Message message = new Message();
        message.what = WHAT_DISPATCH_NOTIFY_REFRESH;
        message.obj = new Object[]{mAdapterWrapper, mRefreshListeners};
        mHandler.sendMessage(message);
    }
    /**
     * 分发加载更多通知
     */
    private void dispatchNotifyLoadmore() {
        Message message = new Message();
        message.what = WHAT_DISPATCH_NOTIFY_LOADMORE;
        message.obj = new Object[]{mAdapterWrapper, mLoadmoreListeners};
        mHandler.sendMessage(message);
    }

    private final List<WeakReference<SwipeRefreshLayout.OnRefreshListener>> mRefreshListeners = new ArrayList<>();
    private final List<WeakReference<OnLoadmoreListener>> mLoadmoreListeners = new ArrayList<>();
    private final RecyclerFooterAdapterWrapper mAdapterWrapper;
    private boolean mIsLoadmoreEnabled = true;

    private WeakReference<SwipeRefreshLayout> mSwipe;
    public RecyclerSwipeHelper(SwipeRefreshLayout swipe, RecyclerView recycler) {
        this(swipe, recycler, new OnCreateFooterViewListener() {
            @Override
            public View onCreateFooterView(ViewGroup parent) {
                CircleProgressBar progressBar = new CircleProgressBar(parent.getContext());
                progressBar.setCircleBackgroundEnabled(false);
                progressBar.setShowArrow(false);
                progressBar.setColorSchemeResources(android.R.color.holo_blue_light,android.R.color.holo_orange_light,android.R.color.holo_red_light);
                LinearLayout linearLayout = new LinearLayout(parent.getContext());
                linearLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(progressBar);
                linearLayout.setGravity(Gravity.CENTER);
                return linearLayout;
            }
        });
    }
    public RecyclerSwipeHelper(SwipeRefreshLayout swipe, RecyclerView recycler, final OnCreateFooterViewListener createFooterView) {
        this.mSwipe = new WeakReference<>(swipe);

        // 刷新监听
        setOnRefreshListener(swipe, new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isLoadmoreing()) {
                    SwipeRefreshLayout swipe = mSwipe.get();
                    if (swipe != null) {
                        swipe.setRefreshing(false);
                    }
                    return;
                }

                dispatchNotifyRefresh();
            }
        });

        // 加载更多监听
        setOnLoadmoreListener(recycler, new OnLoadmoreListener() {
            @Override
            public void onLoadmore() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SwipeRefreshLayout swipe = mSwipe.get();
                        if (swipe != null) {
                            if (swipe.isRefreshing()) {
                                setLoadmoreing(false);
                                return;
                            }
                        }

                        dispatchNotifyLoadmore();
                    }
                });
            }
        });

        // 包装数据源
        mAdapterWrapper = new RecyclerFooterAdapterWrapper(recycler) {
            @Override
            protected View onCreateFooterView(ViewGroup parent) {
                return createFooterView.onCreateFooterView(parent);
            }
        };
    }

    /**
     * 添加刷新监听
     * @param listener
     */
    public void addOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mRefreshListeners.add(new WeakReference<>(listener));
    }

    /**
     * 添加加载更多监听
     * @param listener
     */
    public void addOnLoadmoreListener(OnLoadmoreListener listener) {
        mLoadmoreListeners.add(new WeakReference<>(listener));
    }

    /**
     * 设置是否启用加载更多功能
     * @param enabled
     */
    public void setLoadmoreEnabled(boolean enabled) {
        mIsLoadmoreEnabled = enabled;
        mAdapterWrapper.setLoadmoreEnabled(enabled);
        dispatchNotifyLoadmore();
    }

    /**
     * 设置当前是否刷新状态
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        SwipeRefreshLayout swipe = mSwipe.get();
        if (swipe != null) {
            swipe.setRefreshing(refreshing);
        }
        mAdapterWrapper.setLoadmoreEnabled(mIsLoadmoreEnabled);
        mAdapterWrapper.setLoadmoreing(false);
    }
    /**
     * 返回当前是否刷新状态
     */
    public boolean isRefreshing() {
        SwipeRefreshLayout swipe = mSwipe.get();
        if (swipe != null) {
            return swipe.isRefreshing();
        }
        return false;
    }


    /**
     * 设置当前是否加载更多状态
     * @param loadmoreing
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
     * 设置刷新监听
     * @param listener
     */
    private static void setOnRefreshListener(SwipeRefreshLayout swipe, SwipeRefreshLayout.OnRefreshListener listener) {
        swipe.setOnRefreshListener(listener);
    }
    /**
     * 设置加载更多监听
     * @param listener
     */
    private static void setOnLoadmoreListener(final RecyclerView recycler, final OnLoadmoreListener listener) {
        RecyclerView.LayoutManager layoutManager = recycler.getLayoutManager();
        if (layoutManager == null) {
            throw new UnsupportedOperationException("layoutManager不能为空");
        }
        if (!(layoutManager instanceof LinearLayoutManager || layoutManager instanceof StaggeredGridLayoutManager)) {
            throw new UnsupportedOperationException("只支持LinearLayoutManager|StaggeredGridLayoutManager");
        }

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (recycler.getAdapter() == null || recycler.getAdapter().getItemCount() == 0) {
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkLoadmore(recycler, listener);
                }
            }
        });
    }

    /**
     * 检查是否需要执行Loadmore
     * @param recycler
     * @param listener
     */
    private static void checkLoadmore(RecyclerView recycler, OnLoadmoreListener listener) {
        RecyclerView.LayoutManager layoutManager = recycler.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

            int totalItemCount = recycler.getAdapter().getItemCount();
            if (lastVisibleItem >= totalItemCount - 1) {
                listener.onLoadmore();
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int last[] = new int[gridLayoutManager.getSpanCount()];
            gridLayoutManager.findLastVisibleItemPositions(last);

            int totalItemCount = recycler.getAdapter().getItemCount();
            for (int i = 0; i < last.length; i++) {
                int lastVisibleItem = last[i];
                if (lastVisibleItem >= totalItemCount - 1) {
                    listener.onLoadmore();
                    break;
                }
            }
        }

    }

}
