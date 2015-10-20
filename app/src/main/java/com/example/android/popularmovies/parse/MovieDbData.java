package com.example.android.popularmovies.parse;

import java.io.Serializable;
import java.util.Date;

/**
 * Contains data that match data returned from themoviedb.org
 */
public class MovieDbData implements Serializable {
    public String originalTitle;
    public String posterPath;
    public String plotSynopsis;
    public double userRating;
    public Date releaseDate;

    @Override
    public String toString() {
        return String.format("title=%s\nposterPath=%s\nsynopsis=%s\nuserRating=%s\nreleaseDate=%s",
                originalTitle, posterPath, plotSynopsis, userRating, releaseDate);
    }
}
