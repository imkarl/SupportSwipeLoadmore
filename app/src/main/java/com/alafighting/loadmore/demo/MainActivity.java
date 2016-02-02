package com.alafighting.loadmore.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.alafighting.loadmore.OnLoadmoreListener;
import com.alafighting.loadmore.RecyclerSwipeHelper;

import java.util.Arrays;

/**
 * @author alafighting 2016-02
 */
public class MainActivity extends AppCompatActivity {

    private static Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        final DemoAdapter adapter = new DemoAdapter();
        recyclerView.setAdapter(adapter);


        final RecyclerSwipeHelper helper = new RecyclerSwipeHelper(swipeRefreshLayout, recyclerView);

        helper.addOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("[MainActivity]", "onRefresh");
                        adapter.clear();
                        adapter.addAll(Arrays.asList("test", "test", "test", "test", "test"));
                        helper.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        helper.addOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("[MainActivity]", "onLoadmore");
                        adapter.addAll(Arrays.asList("test", "test", "test", "test", "test"));
                        helper.setLoadmoreing(false);
                    }
                }, 1000);
            }
        });
    }

}
