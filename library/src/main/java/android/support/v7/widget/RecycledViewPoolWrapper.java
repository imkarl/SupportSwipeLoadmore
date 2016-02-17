package android.support.v7.widget;

/**
 * 包裹拓展RecycledViewPool
 * @author alafighting 2016-02
 */
public class RecycledViewPoolWrapper extends RecyclerView.RecycledViewPool {
    private RecyclerView.RecycledViewPool wrapper;

    public RecycledViewPoolWrapper(RecyclerView.RecycledViewPool wrapper) {
        this.wrapper = wrapper;
    }

    public RecyclerView.RecycledViewPool getWrapper() {
        return wrapper;
    }

    @Override
    void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter, boolean compatibleWithPrevious) {
        if (this.wrapper == null) {
            super.onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
        } else {
            this.wrapper.onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
        }
        onAdapterChanged(oldAdapter, newAdapter);
    }

    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
    }

    @Override
    public void clear() {
        if (this.wrapper == null) {
            super.clear();
            return;
        }
        this.wrapper.clear();
    }

    @Override
    public void setMaxRecycledViews(int viewType, int max) {
        if (this.wrapper == null) {
            super.setMaxRecycledViews(viewType, max);
            return;
        }
        this.wrapper.setMaxRecycledViews(viewType, max);
    }

    @Override
    public RecyclerView.ViewHolder getRecycledView(int viewType) {
        if (this.wrapper == null) {
            return super.getRecycledView(viewType);
        }
        return this.wrapper.getRecycledView(viewType);
    }

    @Override
    int size() {
        if (this.wrapper == null) {
            return super.size();
        }
        return this.wrapper.size();
    }

    @Override
    public void putRecycledView(RecyclerView.ViewHolder scrap) {
        if (this.wrapper == null) {
            super.putRecycledView(scrap);
            return;
        }
        this.wrapper.putRecycledView(scrap);
    }

    @Override
    void attach(RecyclerView.Adapter adapter) {
        if (this.wrapper == null) {
            super.attach(adapter);
            return;
        }
        this.wrapper.attach(adapter);
    }

    @Override
    void detach() {
        if (this.wrapper == null) {
            super.detach();
            return;
        }
        this.wrapper.detach();
    }

}
