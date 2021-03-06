/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.app;

import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_HEADERS_STATE;
import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_LOAD_DATA_DELAY;
import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_NUM_ROWS;
import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_REPEAT_PER_ROW;
import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_SET_ADAPTER_AFTER_DATA_LOAD;
import static android.support.v17.leanback.app.BrowseFragmentTestActivity.EXTRA_TEST_ENTRANCE_TRANSITION;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.util.Log;
import android.view.View;

public class BrowseTestFragment extends BrowseFragment {
    private static final String TAG = "BrowseTestFragment";

    final static int DEFAULT_NUM_ROWS = 100;
    final static int DEFAULT_REPEAT_PER_ROW = 20;
    final static long DEFAULT_LOAD_DATA_DELAY = 2000;
    final static boolean DEFAULT_TEST_ENTRANCE_TRANSITION = true;
    final static boolean DEFAULT_SET_ADAPTER_AFTER_DATA_LOAD = false;

    private ArrayObjectAdapter mRowsAdapter;

    // For good performance, it's important to use a single instance of
    // a card presenter for all rows using that presenter.
    final static StringPresenter sCardPresenter = new StringPresenter();

    int NUM_ROWS;
    int REPEAT_PER_ROW;
    boolean mEntranceTransitionStarted;
    boolean mEntranceTransitionEnded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        NUM_ROWS = arguments.getInt(EXTRA_NUM_ROWS, BrowseTestFragment.DEFAULT_NUM_ROWS);
        REPEAT_PER_ROW = arguments.getInt(EXTRA_REPEAT_PER_ROW,
                DEFAULT_REPEAT_PER_ROW);
        long LOAD_DATA_DELAY = arguments.getLong(EXTRA_LOAD_DATA_DELAY,
                DEFAULT_LOAD_DATA_DELAY);
        boolean TEST_ENTRANCE_TRANSITION = arguments.getBoolean(
                EXTRA_TEST_ENTRANCE_TRANSITION,
                DEFAULT_TEST_ENTRANCE_TRANSITION);
        final boolean SET_ADAPTER_AFTER_DATA_LOAD = arguments.getBoolean(
                EXTRA_SET_ADAPTER_AFTER_DATA_LOAD,
                DEFAULT_SET_ADAPTER_AFTER_DATA_LOAD);

        if (!SET_ADAPTER_AFTER_DATA_LOAD) {
            setupRows();
        }

        setTitle("BrowseTestFragment");
        setHeadersState(arguments.getInt(EXTRA_HEADERS_STATE, HEADERS_ENABLED));

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onSearchClicked");
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                    RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row.getHeaderItem().getName()
                        + " " + rowViewHolder
                        + " " + ((ListRowPresenter.ViewHolder) rowViewHolder).getGridView());
            }
        });
        if (TEST_ENTRANCE_TRANSITION) {
            // don't run entrance transition if fragment is restored.
            if (savedInstanceState == null) {
                prepareEntranceTransition();
            }
        }
        // simulates in a real world use case  data being loaded two seconds later
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isDestroyed()) {
                    return;
                }
                if (SET_ADAPTER_AFTER_DATA_LOAD) {
                    setupRows();
                }
                loadData();
                startEntranceTransition();
            }
        }, LOAD_DATA_DELAY);
    }

    private void setupRows() {
        ListRowPresenter lrp = new ListRowPresenter();

        mRowsAdapter = new ArrayObjectAdapter(lrp);

        setAdapter(mRowsAdapter);
    }

    @Override
    protected void onEntranceTransitionStart() {
        super.onEntranceTransitionStart();
        mEntranceTransitionStarted = true;
    }

    @Override
    protected void onEntranceTransitionEnd() {
        super.onEntranceTransitionEnd();
        mEntranceTransitionEnded = true;
    }

    private void loadData() {
        for (int i = 0; i < NUM_ROWS; ++i) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(sCardPresenter);
            int index = 0;
            for (int j = 0; j < REPEAT_PER_ROW; ++j) {
                listRowAdapter.add("Hello world-" + (index++));
                listRowAdapter.add("This is a test-" + (index++));
                listRowAdapter.add("Android TV-" + (index++));
                listRowAdapter.add("Leanback-" + (index++));
                listRowAdapter.add("Hello world-" + (index++));
                listRowAdapter.add("Android TV-" + (index++));
                listRowAdapter.add("Leanback-" + (index++));
                listRowAdapter.add("GuidedStepFragment-" + (index++));
            }
            HeaderItem header = new HeaderItem(i, "Row " + i);
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            Log.i(TAG, "onItemClicked: " + item + " row " + row);
        }
    }

    public VerticalGridView getGridView() {
        return getRowsFragment().getVerticalGridView();
    }
}
