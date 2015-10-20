/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.parse.MovieDbData;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String MOVIES_LIST_SHARE_HASHTAG = " #PopularMovies";

        private MovieDbData mMoviesData;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);

            MenuItem menuItem = menu.findItem(R.id.action_share);

            ShareActionProvider shareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragement_detail, container, false);
            if (intent == null || !intent.hasExtra(MovieDbData.class.getSimpleName())) {
                return rootView;
            }
            mMoviesData = (MovieDbData) intent.getSerializableExtra(MovieDbData.class.getSimpleName());

            ImageView thumbnail = (ImageView) rootView.findViewById(R.id.poster_thumbnail);
            if (thumbnail != null) {
                Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + mMoviesData.posterPath).into(thumbnail);
            }
            TextView originalTitleText = (TextView) rootView.findViewById(R.id.original_title);
            if (originalTitleText != null) {
                originalTitleText.setText(mMoviesData.originalTitle);
            }

            TextView releaseDateText = (TextView) rootView.findViewById(R.id.release_date);
            if (releaseDateText != null) {
                if (null == mMoviesData.releaseDate)
                    releaseDateText.setText(R.string.release_date_missing);
                else
                    releaseDateText.setText(new SimpleDateFormat("yyyy-MM-dd").
                            format(mMoviesData.releaseDate));
            }

            TextView userRatingText = (TextView) rootView.findViewById(R.id.user_rating);
            if (userRatingText != null) {
                String userRating = String.format("%.1f", mMoviesData.userRating);
                userRatingText.setText(userRating);
            }

            TextView synopsisText = (TextView) rootView.findViewById(R.id.synopsis);
            if (synopsisText != null) {
                if (null == mMoviesData.plotSynopsis || "null".equals(mMoviesData.plotSynopsis))
                    synopsisText.setText(R.string.synopsis_missing);
                else
                    synopsisText.setText(mMoviesData.plotSynopsis);
            }
            return rootView;
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mMoviesData + MOVIES_LIST_SHARE_HASHTAG);
            return shareIntent;
        }
    }

}

