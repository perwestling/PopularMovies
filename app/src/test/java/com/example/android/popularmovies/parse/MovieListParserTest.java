package com.example.android.popularmovies.parse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class MovieListParserTest {
    private final static String MOVIE_1 = "{\"adult\":false,\"backdrop_path\":\"/dkMD5qlogeRMiEixC4YNPUvax2T.jpg\",\"genre_ids\":[28,12,878,53],\"id\":135397," +
            "\"original_language\":\"en\",\"original_title\":\"Jurassic World\",\"overview\":\"Twenty-two years after the events of Jurassic Park, Isla Nublar now features a fully functioning dinosaur theme park, Jurassic World, as originally envisioned by John Hammond.\"," +
            "\"release_date\":\"2015-06-12\",\"poster_path\":\"/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg\",\"popularity\":53.196327,\"title\":\"Jurassic World\"," +
            "\"video\":false,\"vote_average\":6.9,\"vote_count\":2646}";
    private final static String MOVIE_2 = "{\"adult\":false,\"backdrop_path\":\"/sEgULSEnywgdSesVHFHpPAbOijl.jpg\",\"genre_ids\":[18,12,878],\"id\":286217," +
            "\"original_language\":\"en\",\"original_title\":\"The Martian\",\"overview\":\"During a manned mission to Mars, Astronaut Mark Watney is presumed dead after a fierce storm and left behind by his crew. But Watney has survived and finds himself stranded and alone on the hostile planet. With only meager supplies, he must draw upon his ingenuity, wit and spirit to subsist and find a way to signal to Earth that he is alive.\"," +
            "\"release_date\":\"2015-10-02\",\"poster_path\":\"/AjbENYG3b8lhYSkdrWwlhVLRPKR.jpg\",\"popularity\":39.896562,\"title\":\"The Martian\"," +
            "\"video\":false,\"vote_average\":7.7,\"vote_count\":495}";
    private final static String MOVIE_LIST_PATTERN = "{\"page\":1,\"results\":[%s]},\"total_pages\":12357,\"total_results\":247129}";

    @Before
    public void setUp() {

    }

    @Test(expected = ParseException.class)
    public void shouldThrowIfNonJsonInput() throws Exception {
        MovieListParser.parse("");
    }

    @Test
    public void shouldReturnEmptyListIfInputIsEmpty() throws Exception {
        assertThat(MovieListParser.parse(makeMovieListJson()), hasSize(0));
    }

    @Test
    public void shouldThrowIfErrorMessage() throws Exception {
        try {
            MovieListParser.parse("{\"status_code\":7,\"status_message\":\"Invalid API key: You must be granted a valid key.\"}");
            fail("Should have thrown");
        } catch (ParseException e) {
            assertThat(e.getMessage(), containsString("status_code"));
            assertThat(e.getMessage(), containsString("status_message"));
            assertThat(e.getMessage(), containsString("Invalid API key"));
        }
    }

    @Test
    public void shouldThrowIfMissingTitle() throws Exception {
        try {
            MovieListParser.parse(makeMovieListJson(MOVIE_1.replace("\"original_title\":\"Jurassic World\",", "")));
            fail("Should have thrown");
        } catch (ParseException e) {
            assertThat(e.getMessage(), containsString("Failed to parse movie result"));
        }
    }

    @Test
    public void shouldParseTitleOfMovie() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1));
        assertThat(result, hasSize(1));
        MovieDbData movie = result.get(0);
        assertThat(movie.originalTitle, is("Jurassic World"));
    }

    @Test
    public void shouldParseTitleOfMovies() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1, MOVIE_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.originalTitle, is("The Martian"));
    }

    @Test
    public void shouldParsePosterPath() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1, MOVIE_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.posterPath, is("/AjbENYG3b8lhYSkdrWwlhVLRPKR.jpg"));
    }

    @Test
    public void shouldParsePlotSynopsisOfMovies() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1, MOVIE_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.plotSynopsis, startsWith("During a manned mission to Mars, Astronaut Mark Watney"));
    }

    @Test
    public void shouldParseUserRatingOfMovies() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1, MOVIE_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.userRating, is(7.7));
    }

    @Test
    public void shouldParseReleaseDateOfMovies() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_1, MOVIE_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.releaseDate.toString(), is(createDate(2015, 10, 2)));
    }

    @Test
    public void shouldHandleEntryWithoutReleaseDate() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_2.replace(
                "\"release_date\":\"2015-10-02\",",
                "\"release_date\":\"null\",")));
        assertThat(result.get(0).releaseDate, is(nullValue()));
    }

    @Test
    public void shouldIngoreEntriesWithoutSnapshot() throws Exception {
        List<MovieDbData> result = MovieListParser.parse(makeMovieListJson(MOVIE_2.replace(
                "\"poster_path\":\"/AjbENYG3b8lhYSkdrWwlhVLRPKR.jpg\"",
                "\"poster_path\":\"null\"")));
        assertThat(result.size(), is(0));
    }

    private String makeMovieListJson(String... movie) {
        return String.format(MOVIE_LIST_PATTERN, StringUtils.join(movie, ","));
    }

    private String createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        return cal.getTime().toString();
    }
}
