package com.example.android.popularmovies.parse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.fail;

public class TvSeriesListParserTest {
    private final static String TV_SERIES_1 = "{\"backdrop_path\":\"/pmBSUaEw0Tz0UwvAbCBWbhkfNpR.jpg\",\"first_air_date\":\"1993-09-11\",\"genre_ids\":[16,10759],\"id\":590," +
            "\"original_language\":\"en\",\"original_name\":\"SWAT Kats: The Radical Squadron\"," +
            "\"overview\":\"SWAT Kats: The Radical Squadron is animated television series created by Christian Tremblay and Yvon Tremblay and produced by Hanna-Barbera and Turner Program Services. The series takes place in the fictional metropolis of Megakat City, which is populated entirely by anthropomorphic felines who are just like people, known as 'kats'. The titular SWAT Kats are two vigilante pilots who possess a state-of-the-art fighter jet with an array of weaponry. Throughout the series, they face various villains as well as Megakat City's militarized police force, the Enforcers.\\n\\nThe show originally premiered and ran on TBS's syndication block The Funtastic World of Hanna-Barbera from 1993 to 1995. Every episode of the series was directed by Robert Alvarez. The bulk of the series was written by either Glenn Leopold or Lance Falk. Jim Stenstrum contributed two episodes, while David Ehrman, Von Williams, Eric Clark, Mark Saraceni and Jim Katz all contributed one episode each. There were a total of twenty-five finished episodes and a special episode, that features a report on the SWAT Kats and of all their missions and gadgets as well as three unfinished episodes and two episodes still in the concept stage. The show re-aired on Cartoon Network and Boomerang.\"," +
            "\"origin_country\":[\"US\"],\"poster_path\":\"/vk60r1R9kD3ZastlVb7AY7hhAv7.jpg\",\"popularity\":0.694516,\"name\":\"SWAT Kats: The Radical Squadron\"," +
            "\"vote_average\":10.0,\"vote_count\":1}";
    private final static String TV_SERIES_2 = "{\"backdrop_path\":\"/3lK4WrsgmWpBR0RmkioN2ldmoVB.jpg\",\"first_air_date\":\"1994-09-10\",\"genre_ids\":[16,10759],\"id\":2121," +
            "\"original_language\":\"en\",\"original_name\":\"ReBoot\"," +
            "\"overview\":\"ReBoot is a Canadian CGI-animated action-adventure cartoon series that originally aired from 1994 to 2001. It was produced by Vancouver-based production company Mainframe Entertainment, Alliance Communications, BLT Productions and created by Gavin Blair, Ian Pearson, Phil Mitchell and John Grace, with the visuals designed by Brendan McCarthy after an initial attempt by Ian Gibson.\\n\\nIt was the first half-hour, completely computer-animated TV series.\"," +
            "\"origin_country\":[\"CA\"],\"poster_path\":\"/z1hmvYR13gzjsLkfwBA4kWGVsyn.jpg\",\"popularity\":0.062873,\"name\":\"ReBoot\",\"vote_average\":10.0,\"vote_count\":1}";
    private final static String TV_SERIES_PATTERN = "{\"page\":1,\"results\":[%s]},\"total_pages\":3121,\"total_results\":62402}";

    @Before
    public void setUp() {

    }

    @Test(expected = ParseException.class)
    public void shouldThrowIfNonJsonInput() throws Exception {
        TvSeriesListParser.parse("");
    }

    @Test
    public void shouldReturnEmptyListIfInputIsEmpty() throws Exception {
        assertThat(TvSeriesListParser.parse(makeTvSeriesListJson()), hasSize(0));
    }

    @Test
    public void shouldThrowIfErrorMessage() throws Exception {
        try {
            TvSeriesListParser.parse("{\"status_code\":7,\"status_message\":\"Invalid API key: You must be granted a valid key.\"}");
            fail("Should have thrown");
        } catch (ParseException e) {
            assertThat(e.getMessage(), containsString("status_code"));
            assertThat(e.getMessage(), containsString("status_message"));
            assertThat(e.getMessage(), containsString("Invalid API key"));
        }
    }

    @Test
    public void shouldThrowIfMissingName() throws Exception {
        try {
            TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1.replace("\"original_name\":\"SWAT Kats: The Radical Squadron\",", "")));
            fail("Should have thrown");
        } catch (ParseException e) {
            assertThat(e.getMessage(), containsString("Failed to parse tv result"));
        }
    }

    @Test
    public void shouldParseNameOfOneTvSeries() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1));
        assertThat(result, hasSize(1));
        MovieDbData tv = result.get(0);
        assertThat(tv.originalTitle, is("SWAT Kats: The Radical Squadron"));
    }

    @Test
    public void shouldParseNameOfMultipleTvSeries() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1, TV_SERIES_2));
        assertThat(result, hasSize(2));
        MovieDbData tv = result.get(1);
        assertThat(tv.originalTitle, is("ReBoot"));
    }

    @Test
    public void shouldParsePosterPath() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1, TV_SERIES_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.posterPath, is("/z1hmvYR13gzjsLkfwBA4kWGVsyn.jpg"));
    }

    @Test
    public void shouldParsePlotSynopsis() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1, TV_SERIES_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.plotSynopsis, startsWith("ReBoot is a Canadian CGI-animated action-adventure cartoon series "));
    }

    @Test
    public void shouldParseUserRating() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1, TV_SERIES_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.userRating, is(10.0));
    }

    @Test
    public void shouldParseReleaseDateOfTvSeries() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_1, TV_SERIES_2));
        assertThat(result, hasSize(2));
        MovieDbData movie = result.get(1);
        assertThat(movie.releaseDate.toString(), is(createDate(1994, 9, 10)));
    }

    @Test
    public void shouldHandleEntryWithoutReleaseDate() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_2.replace(
                "\"first_air_date\":\"1994-09-10\",",
                "\"first_air_date\":\"null\",")));
        assertThat(result.get(0).releaseDate, is(nullValue()));
    }

    @Test
    public void shouldIngoreEntriesWithoutSnapshot() throws Exception {
        List<MovieDbData> result = TvSeriesListParser.parse(makeTvSeriesListJson(TV_SERIES_2.replace(
                "\"poster_path\":\"/z1hmvYR13gzjsLkfwBA4kWGVsyn.jpg\"",
                "\"poster_path\":\"null\"")));
        assertThat(result.size(), is(0));
    }

    private String makeTvSeriesListJson(String... movie) {
        return String.format(TV_SERIES_PATTERN, StringUtils.join(movie, ","));
    }

    private String createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        return cal.getTime().toString();
    }
}

