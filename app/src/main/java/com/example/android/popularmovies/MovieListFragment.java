package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.popularmovies.parse.MovieDbData;
import com.example.android.popularmovies.parse.MovieListParser;
import com.example.android.popularmovies.parse.ParseException;
import com.example.android.popularmovies.parse.TvSeriesListParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment doing a forecast in the UI.
 */
public class MovieListFragment extends Fragment {

    private static final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private List<MovieDbData> moviesData = new ArrayList<>();
    private ArrayAdapter<MovieDbData> adapter;

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_moviedb);
        adapter = createAdaptor();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Click on " + moviesData.get(position));
                Intent showDetailsIntent = new Intent(getActivity(), DetailActivity.class);
                showDetailsIntent.setAction(Intent.ACTION_SEND);
                showDetailsIntent.putExtra(MovieDbData.class.getSimpleName(), moviesData.get(position));

                startActivity(showDetailsIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder =
                prefs.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        String mediaType =
                prefs.getString(getString(R.string.pref_media_type_key), getString(R.string.pref_media_type_default));
        FetchMoviesListTask task = new FetchMoviesListTask(sortOrder, mediaType);
        task.execute();
    }

    private static class ImageArrayAdaptor extends ArrayAdapter<MovieDbData> {

        private final Context context;
        private final int layoutResourceId;
        private final List<MovieDbData> moviesData;

        public ImageArrayAdaptor(Context context, int layoutResourceId, List<MovieDbData> database) {
            super(context, R.layout.list_item, database);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.moviesData = database;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ImageView imgView = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                imgView = (ImageView) row.findViewById(R.id.list_item_view);
                row.setTag(imgView);
            } else {
                imgView = (ImageView) row.getTag();
            }
            
            MovieDbData data = moviesData.get(position);
            Picasso.with(context).load("http://image.tmdb.org/t/p/w185" + data.posterPath).into(imgView);
            return row;
        }
    }
    private ArrayAdapter<MovieDbData> createAdaptor() {
        return new ImageArrayAdaptor(getActivity(), R.layout.list_item, moviesData);
    }

    class FetchMoviesListTask extends AsyncTask<String, Void, List<MovieDbData>> {

        private final String LOG_TAG = FetchMoviesListTask.class.getSimpleName();
        private final String sortOrder;
        private final String mediaType;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        public FetchMoviesListTask(String sortOrder, String mediaType) {
            this.sortOrder = sortOrder;
            this.mediaType = mediaType;
        }

        @Override
        protected List<MovieDbData> doInBackground(String... strings) {
            try {
                URL url = buildUrl(sortOrder, mediaType);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                return getMovelistFromJson(buffer.toString());
            } catch (IOException | JSONException e) {
                Log.e(LOG_TAG, "Error: " + e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        private URL buildUrl(String sortOrder, String mediaType) throws MalformedURLException {

            // Construct the URL for the Movie Database query
            // Possible parameters are available at the API page, version 3, at
            // https://www.themoviedb.org/documentation/api
            // Example: https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc?api_key=APIKEY
            final String MOVIES_DB_BASE_URL = "https://api.themoviedb.org/3";
            final String OPERATION  = "/discover/" + mediaType;
            final String SORT_BY = "sort_by";
            final String APIKEY_PARAM = "api_key";
            final String APIKEY_VALUE = getResources().getString(R.string.api_key);

            Uri builtUri = Uri.parse(MOVIES_DB_BASE_URL + OPERATION).buildUpon()
                    .appendQueryParameter(APIKEY_PARAM, APIKEY_VALUE)
                    .appendQueryParameter(SORT_BY, sortOrder)
                    .build();
            String urlString = builtUri.toString();
            Log.d(LOG_TAG, "Lookup movies using: " + urlString);
            return new URL(urlString);
        }

        @Override
        protected void onPostExecute(List<MovieDbData> results) {
            if (results == null) {
                Toast.makeText(getActivity(), "Failed to get any data", Toast.LENGTH_LONG).show();
            } else {
                Log.d(LOG_TAG, "Got result: " + results);
                moviesData.clear();
                moviesData.addAll(results);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * Take the String representing the complete response in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<MovieDbData> getMovelistFromJson(String jsonString)
                throws JSONException {

            try {
                if (mediaType.equals("movie")) {
                    return MovieListParser.parse(jsonString);
                } else {
                    return TvSeriesListParser.parse(jsonString);
                }
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parse failure: " + e.getMessage() + ". Input: " + jsonString, e);
                return null;
            }
        }
    }
}
