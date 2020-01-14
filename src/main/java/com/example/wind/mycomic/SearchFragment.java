package com.example.wind.mycomic;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.wind.mycomic.object.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private static final int REQUEST_SPEECH = 0x00000010;
    private static final long SEARCH_DELAY_MS = 1000L;

    private ArrayObjectAdapter mRowsAdapter;
    private HashMap<String, ArrayList<Movie>> mItems = ShareDataClass.getInstance().movieList;
    private String mQuery;

    private final Handler mHandler = new Handler();
    private final Runnable mDelayedLoad = new Runnable() {
        @Override
        public void run() {
            loadRows();
        }
    };

    /**
     * Starts {@link #loadRows()} method after delay.
     * @param query the word to be searched
     * @param delay the time to wait until loadRows will be executed (milliseconds).
     */
    private void loadQueryWithDelay(String query, long delay) {
        mHandler.removeCallbacks(mDelayedLoad);
        if (!query.equals("") && !query.equals("nil")) {
            mQuery = query;
            mHandler.postDelayed(mDelayedLoad, delay);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "start - onPause");
        //super.onPause() 會有問題，但目前仍然不知道正確的 pause 方式，先用 onDestroy 代替
        super.onDestroy();
        Log.d(TAG, "end - onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: test");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        setSearchResultProvider(this);
        setupEventListeners();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            try
            {
                Log.d(TAG, "instanceof Movie");
                if (item instanceof Movie) {
                    Movie movie = (Movie) item;
                    Log.d(TAG, "Item: " + item.toString());
                    Intent intent = new Intent(getActivity(), SeasonActivity.class);

                    intent.putExtra(DetailsActivity.MOVIE_CATEGORY, movie.getCategory());
                    intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getUUID());

                    getActivity().startActivity(intent);
                    /*
                    Movie movie = (Movie) item;

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);

                    intent.putExtra(DetailsActivity.MOVIE_CATEGORY, movie.getCategory());
                    intent.putExtra(DetailsActivity.MOVIE_UUID, movie.getUUID());

                    startActivity(intent);
                    */
                }
            }
            catch(Exception e)
            {
                String longString = e.getMessage();
                int maxLogSize = 1000;
                for(int i = 0; i <= longString.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > longString.length() ? longString.length() : end;
                    Log.v(TAG, longString.substring(start, end));
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            // Do something while the card was seelected.
        }
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0;
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        Log.d(TAG, "getResultsAdapter");
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery){
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        loadQueryWithDelay(newQuery, SEARCH_DELAY_MS);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        loadQueryWithDelay(query, 0);
        return true;
    }

    private void loadRows() {
        // offload processing from the UI thread
        new AsyncTask<String, Void, ListRow>() {
            private final String query = mQuery;

            @Override
            protected void onPreExecute() {
                Log.d(TAG, "start - onPreExecute");
                mRowsAdapter.clear();
                Log.d(TAG, "end - onPreExecute");
            }

            @Override
            protected ListRow doInBackground(String... params) {
                Log.d(TAG, "start - doInBackground");
                final List<Movie> result = new ArrayList<>();

                Iterator iter = mItems.entrySet().iterator();
                while (iter.hasNext()) {
                    HashMap.Entry entry = (HashMap.Entry) iter.next();
                    String movie_category = entry.getKey().toString();
                    ArrayList<Movie> movie_list = (ArrayList<Movie>) entry.getValue();
                    for(int i = 0; i < movie_list.size(); i++) {
                        Movie movie = movie_list.get(i);
                        String uuid = movie.getUUID();
                        if (movie.getTitle().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
                            result.add(movie);
                        }
                    }
                }

                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                listRowAdapter.addAll(0, result);
                HeaderItem header = new HeaderItem("Search Results");
                Log.d(TAG, "end - doInBackground");
                return new ListRow(header, listRowAdapter);
            }

            @Override
            protected void onPostExecute(ListRow listRow) {
                Log.d(TAG, "start - onPostExecute");
                mRowsAdapter.add(listRow);
                Log.d(TAG, "end - onPostExecute");
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}