package com.alafighting.loadmore.demo;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alafighting 2016-02
 */
public class DemoAdapter extends RecyclerView.Adapter {

    private List<String> mDatas = new ArrayList<>();

    public DemoAdapter() {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((TextHolder)holder).setText(mDatas.get(position)+" "+position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void addAll(List<String> datas) {
        this.mDatas.addAll(datas);
        this.notifyDataSetChanged();
    }

    public void clear() {
        this.mDatas.clear();
        this.notifyDataSetChanged();
    }


    private static class TextHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public TextHolder(TextView itemView) {
            super(itemView);
            textView = itemView;
        }
        public void setText(CharSequence message) {
            textView.setText(message);
        }
    }

}
