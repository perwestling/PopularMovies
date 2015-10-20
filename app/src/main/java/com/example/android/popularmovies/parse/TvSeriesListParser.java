package com.example.android.popularmovies.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse a JSON string containing information about TV series returned by themoviedb.org.
 */
public class TvSeriesListParser {

    public static List<MovieDbData> parse(String input) throws ParseException {
        List<MovieDbData> result = new ArrayList<>();
        try {
            JSONObject jsonString = new JSONObject(input);
            if (containsJson(jsonString, "results")) {
                JSONArray jsonResults = jsonString.getJSONArray("results");
                for (int i = 0; i < jsonResults.length(); ++i) {
                    result.add(parseResult(jsonResults.getJSONObject(i)));
                }
            } else if (containsJson(jsonString, "status_code")) {
                throw new ParseException("Error message returned: " + input);
            } else {
                throw new ParseException("Unknown return: " + input);
            }
        } catch (JSONException e) {
            throw new ParseException("Failed to parse: " + input, e);
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
            data.title = jsonResult.getString("name");
        } catch (JSONException e) {
            throw new ParseException("Failed to parse name from: " + jsonResult.toString(2), e);
        }
        return data;
    }
}
