package com.alafighting.loadmore.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.alafighting.loadmore.OnLoadmoreListener;
import com.alafighting.loadmore.RecyclerSwipeHelper;

import java.util.Arrays;

/**
 * 演示如何通过[下拉刷新]+[上拉加载更多]实现分页功能
 * @author alafighting 2016-02
 */
public class DemoActivity extends AppCompatActivity {

    private static Handler mHandler = new Handler();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DemoAdapter adapter;
    private RecyclerSwipeHelper helper;
    private int page = 1;

    /**
     * 模拟数据全部加载
     */
    private static final int PAGE_STOP = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new DemoAdapter();
        recyclerView.setAdapter(adapter);

        // 初始化辅助类
        helper = new RecyclerSwipeHelper(swipeRefreshLayout, recyclerView);

        // 监听下拉刷新
        helper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        // 监听上拉加载更多
        helper.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore() {
                loadmore();
            }
        });

        // 自动刷新
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                helper.setRefreshing(true);
                refresh();
            }
        }, 100);
    }

    private void refresh() {
        // 重新启用加载更多
        helper.setEnabledLoadmore(true);

        page = 1;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("[DemoActivity]", "onRefresh  "+page);
                adapter.clear();
                adapter.addAll(Arrays.asList("test", "test", "test", "test", "test"));
                helper.setRefreshing(false);
            }
        }, 1000);
    }

    private void loadmore() {
        page = page + 1;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("[DemoActivity]", "onLoadmore  "+page);
                adapter.addAll(Arrays.asList("test", "test", "test", "test", "test"));
                helper.setLoadmoreing(false);

                if (page == PAGE_STOP) {
                    // 全部加载完成，停止使用加载更多
                    helper.setEnabledLoadmore(false);
                }
            }
        }, 1000);
    }

}
