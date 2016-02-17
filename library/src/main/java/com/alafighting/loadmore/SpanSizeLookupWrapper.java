package com.alafighting.loadmore;

import android.support.v7.widget.GridLayoutManager;

/**
 * 包裹拓展SpanSizeLookup
 * @author alafighting 2016-02
 */
public class SpanSizeLookupWrapper extends GridLayoutManager.SpanSizeLookup {
    private GridLayoutManager.SpanSizeLookup wrapper;
    private int spanSize;
    private RecyclerFooterAdapterWrapper adapter;

    public SpanSizeLookupWrapper(GridLayoutManager layoutManager,
                                 RecyclerFooterAdapterWrapper adapter) {
        this.wrapper = layoutManager.getSpanSizeLookup();
        this.spanSize = layoutManager.getSpanCount();
        this.adapter = adapter;
    }

    @Override
    public int getSpanSize(int position) {
        if (adapter.isLoadingRow(position)) {
            return spanSize;
        } else {
            return wrapper.getSpanSize(position);
        }
    }

    public GridLayoutManager.SpanSizeLookup getWrapper() {
        return wrapper;
    }

}
