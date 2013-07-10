package com.afollestad.silk.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

/**
 * The base of all other fragments in the library; contains methods that help maintain consistency among all fragments,
 * also provides various convenience methods.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class SilkFragment extends Fragment {

    private boolean mAttached;

    /**
     * Gets the fragment layout to be inflated.
     */
    public abstract int getLayout();

    /**
     * Gets the title set to the activity when the Fragment is attached.
     */
    public abstract String getTitle();

    /**
     * Gets the menu resource for the action bar, return 0 if none.
     */
    public abstract int getMenu();

    /**
     * Called when the Fragment becomes visible or invisible to the user.
     * <p/>
     * This works even when in a ViewPager when Fragments that are not actually visible are initialized off to the side.
     * <p/>
     * Sometimes {#getUserVisibleHint}, {#isVisible}, {#setUserVisibleHint}, etc. can be deceiving. This is the answer.
     */
    public abstract void onVisibilityChange(boolean visible);

    private void notifyVisibility(boolean visible) {
        if (visible == mAttached) {
            // Don't allow multiple notifications
            return;
        }
        mAttached = visible;
        onVisibilityChange(visible);
    }

    /**
     * Gets whether or not the fragment is attached and visible to the user.
     */
    public final boolean isAttached() {
        return mAttached;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getMenu() > 0)
            setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint())
            notifyVisibility(true);
        if (getTitle() != null)
            getActivity().setTitle(getTitle());
    }

    @Override
    public final void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) notifyVisibility(true);
        else if (isVisible() && !isVisibleToUser) notifyVisibility(false);
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public final void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // This is overridden so sub-classes can't override it, use onVisibilityChange() instead.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(getMenu(), menu);
    }

    public final void runOnUiThread(Runnable runnable) {
        if (getActivity() == null)
            return;
        getActivity().runOnUiThread(runnable);
    }
}
