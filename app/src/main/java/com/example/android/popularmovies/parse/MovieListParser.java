package com.example.android.popularmovies.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Parse a JSON string containing information about movies returned by themoviedb.org.
 */
public class MovieListParser {

    public static List<MovieDbData> parse(String input) throws ParseException {
        List<MovieDbData> result = new ArrayList<>();
        try {
            JSONObject jsonString = new JSONObject(input);
            if (containsJson(jsonString, "results")) {
                JSONArray jsonResults = jsonString.getJSONArray("results");
                for (int i = 0; i < jsonResults.length(); ++i) {
                    MovieDbData parsed = parseResult(jsonResults.getJSONObject(i));
                    if (parsed != null) {
                        result.add(parsed);
                    }
                }
            } else if (containsJson(jsonString, "status_code")) {
                throw new ParseException("Error message returned: " + input);
            } else {
                throw new ParseException("Unknown return: " + input);
            }
        } catch (JSONException e) {
            throw new ParseException("Failed to parse movie: " + e.getMessage() + "\nInput:" + input, e);
        }
        return result;
    }

    private static boolean containsJson(JSONObject jsonString, String key) {
        try {
            jsonString.get(key);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static MovieDbData parseResult(JSONObject jsonResult) throws JSONException, ParseException {
        MovieDbData data = new MovieDbData();
        try {
            data.originalTitle = jsonResult.getString("original_title");
            data.plotSynopsis = jsonResult.getString("overview");
            data.releaseDate = makeDate(jsonResult.getString("release_date"));
            data.userRating = jsonResult.getDouble("vote_average");
            data.posterPath = jsonResult.getString("poster_path");
            if (data.posterPath == null || data.posterPath.equals("null")) {
                return null;
            }
        } catch (JSONException e) {
            throw new ParseException("Failed to parse movie result: " + e.getMessage()
                    + "\nResult: " + jsonResult.toString(2), e);
        }
        return data;
    }

    private static Date makeDate(String dateInput) throws JSONException {
        if (dateInput.equals("null")) {
            return null;
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(dateInput);
        } catch (java.text.ParseException e) {
            throw new JSONException("Incorrect date " + dateInput + ": " + e.getMessage());
        }
    }
}
