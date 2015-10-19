package com.example.android.popularmovies.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse a JSON string returned by themoviedb.org.
 */
public class MovieListParser {

    public static List<MovieData> parse(String input) throws ParseException {
        List<MovieData> result = new ArrayList<>();
        try {
            JSONObject jsonString = new JSONObject(input);
            JSONArray jsonResults = jsonString.getJSONArray("results");
            for (int i = 0; i < jsonResults.length(); ++i) {
                result.add(parseResult(jsonResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new ParseException("Failed to parse: " + input, e);
        }
        return result;
    }

    private static MovieData parseResult(JSONObject jsonResult) throws JSONException, ParseException {
        MovieData data = new MovieData();
        try {
            data.title = jsonResult.getString("title");
        } catch (JSONException e) {
            throw new ParseException("Failed to parse title from: " + jsonResult.toString(2), e);
        }
        return data;
    }
}
