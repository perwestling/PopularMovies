package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.popularmovies.parse.MovieData;
import com.example.android.popularmovies.parse.MovieListParser;
import com.example.android.popularmovies.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment doing a forecast in the UI.
 */
public class MovieListFragment extends Fragment {

    private List<String> moviesData = new ArrayList<>();
    private ArrayAdapter<String> adapter;

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

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        adapter = createAdaptor();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent showDetailsIntent = new Intent(getActivity(), DetailActivity.class);
                showDetailsIntent.setAction(Intent.ACTION_SEND);
                showDetailsIntent.putExtra(Intent.EXTRA_TEXT, moviesData.get(position));
                showDetailsIntent.setType("text/plain");

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movieslistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String value = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        FetchMoviesListTask task = new FetchMoviesListTask();
        task.execute();
    }

    private ArrayAdapter<String> createAdaptor() {
        return new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, moviesData);
    }

    class FetchMoviesListTask extends AsyncTask<String, Void, List<MovieData>> {

        private final String LOG_TAG = FetchMoviesListTask.class.getSimpleName();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        @Override
        protected List<MovieData> doInBackground(String... strings) {
            try {
                URL url = buildUrl();

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

        private URL buildUrl() throws MalformedURLException {
            String sortOrder = "popularity.desc";

            // Construct the URL for the Movie Database query
            // Possible parameters are available at the API page, version 3, at
            // https://www.themoviedb.org/documentation/api
            // Example: https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc?api_key=APIKEY
            final String MOVIES_DB_BASE_URL = "https://api.themoviedb.org/3";
            final String OPERATION  = "/discover/movie";
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
        protected void onPostExecute(List<MovieData> results) {
            if (results == null)
                return;
            Log.d(LOG_TAG, "Got result: " + results);
            moviesData.clear();
            for(int i = 0; i < results.size(); ++i) {
                moviesData.add(results.get(i).title);
            }
            adapter.notifyDataSetChanged();
        }

        /**
         * Take the String representing the complete response in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<MovieData> getMovelistFromJson(String jsonString)
                throws JSONException {

            try {
                List<MovieData> movies = MovieListParser.parse(jsonString);
                return movies;
            } catch (ParseException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }
}
