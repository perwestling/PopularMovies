package com.example.android.popularmovies.parse;

import org.json.JSONException;

public class ParseException extends Exception {
    public ParseException(String message, Exception cause) {
        super(message, cause);
    }
}
