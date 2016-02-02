package android.support.v7.widget;

/**
 * @author alafighting 2016-01
 */
public class SupportRecycledViewPool extends RecyclerView.RecycledViewPool {

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter, boolean compatibleWithPrevious) {
        super.onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
    }

}
