
package alarmiko.geoalarm.alarm.alarmiko.list;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.db.BaseItemCursor;
import alarmiko.geoalarm.alarm.alarmiko.db.ObjectWithId;
import alarmiko.geoalarm.alarm.alarmiko.ui.BaseFragment;
import butterknife.BindView;
import butterknife.OnClick;

public abstract class RecyclerViewFragment<
        T extends ObjectWithId,
        VH extends BaseViewHolder<T>,
        C extends BaseItemCursor<T>,
        A extends BaseCursorAdapter<T, VH, C>>
    extends BaseFragment implements
        LoaderManager.LoaderCallbacks<C>,
        OnListItemInteractionListener<T>,
        ScrollHandler {

    public static final String ACTION_SCROLL_TO_STABLE_ID = "list.action.SCROLL_TO_STABLE_ID";
    public static final String EXTRA_SCROLL_TO_STABLE_ID = "list.extra.SCROLL_TO_STABLE_ID";

    private A mAdapter;
    private long mScrollToStableId = RecyclerView.NO_ID;

    @BindView(R.id.list)
    RecyclerView mList;

    @Nullable
    // Subclasses are not required to use the default content layout, so this may not be present.
    @BindView(R.id.empty_view)
    TextView mEmptyView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    public abstract void onFabClick();

    protected abstract void onScrolledToStableId(long id, int position);

    /**
     * @return the adapter to set on the RecyclerView. Called in onCreateView().
     */
    protected abstract A onCreateAdapter();

    @StringRes
    protected int emptyMessage() {
        // The reason this isn't abstract is so we don't require subclasses that
        // don't have an empty view to implement this.
        return 0;
    }

    @DrawableRes
    protected int emptyIcon() {
        // The reason this isn't abstract is so we don't require subclasses that
        // don't have an empty view to implement this.
        return 0;
    }

    protected boolean hasEmptyView() {
        return true;
    }

    protected final A getAdapter() {
        return mAdapter;
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        // Called in onCreateView(), so the host activity is alive already.
        return new LinearLayoutManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mList.setLayoutManager(getLayoutManager());
        mList.setAdapter(mAdapter = onCreateAdapter());
        if (hasEmptyView() && mEmptyView != null) {
            // Configure the empty view, even if there currently are items.
            mEmptyView.setText(emptyMessage());
            mEmptyView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, emptyIcon(), 0, 0);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // http://stackoverflow.com/a/14632434/5055032
        // A Loader's lifecycle is bound to its Activity, not its Fragment.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<C> loader, C data) {
        mAdapter.swapCursor(data);
        if (hasEmptyView() && mEmptyView != null) {
            // TODO: Last I checked after a fresh install, this worked fine.
            // However, previous attempts (without fresh installs) didn't hide the empty view
            // upon an item being added. Verify this is no longer the case.
            mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        // This may have been a requery due to content change. If the change
        // was an insertion, scroll to the last modified alarm.
        performScrollToStableId(mScrollToStableId);
        mScrollToStableId = RecyclerView.NO_ID;
    }

    @OnClick(R.id.fab)
    void onClickedFab() {
        onFabClick();
    }

    @Override
    public void onLoaderReset(Loader<C> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * @return a layout resource that MUST contain a RecyclerView. The default implementation
     * returns a layout that has just a single RecyclerView in its hierarchy.
     */
    @Override
    protected int contentLayout() {
        return R.layout.fragment_recycler_view;
    }

    @Override
    public void setScrollToStableId(long id) {
        mScrollToStableId = id;
    }

    @Override
    public void scrollToPosition(int position) {
        mList.smoothScrollToPosition(position);
    }

    public final void performScrollToStableId(long stableId) {
        if (stableId != RecyclerView.NO_ID) {
            int position = -1;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (mAdapter.getItemId(i) == stableId) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                scrollToPosition(position);
                onScrolledToStableId(stableId, position);
            }
        }
    }
}
