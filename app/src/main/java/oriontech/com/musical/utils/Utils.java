package oriontech.com.musical.utils;

import android.util.Log;
import android.util.SparseArray;

import com.google.api.services.youtube.model.SearchResult;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import oriontech.com.musical.model.YouTubeVideo;
import oriontech.com.musical.youtubeExtractor.YtFile;

import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_140;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_141;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_17;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_171;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_18;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_22;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_249;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_250;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_251;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_36;
import static oriontech.com.musical.utils.Config.YOUTUBE_ITAG_43;

public class Utils
{
    private static final String TAG = "Utils";

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "Q");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "S");
    }

    public static String formatViewCount(String viewCounts)
    {
        String[] split = viewCounts.split(" ");
        String[] segments = split[0].split(",");

//        return formatSting(segments) + " " + split[1];
//        조회수 173,893회

        return formatLong(segmentToLong(segments)) + " " + split[1];
    }

    private static long segmentToLong(String[] segments)
    {
        long number = 0;
        int count = segments.length - 1;
        for (String segment : segments) {
//            Log.e(TAG, "segment: " + segment);
            try {
                number += Integer.parseInt(segment) * Math.pow(10, 3 * count--);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        return number;
    }

    public static String formatSting(String[] segments)
    {
        int count = segments.length;
        String suffix = count > 2 ? " M" : count > 1 ? " K" : "";
        count = count > 2 ? count - 2 : count > 1 ? count - 1 : count;
        String number = "";
        for (String segment : segments) {
            number += segment;
            if (count-- == 1) break;
            number += ",";
        }

        return number + suffix;
    }

    public static String formatLong(long value)
    {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatLong(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatLong(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    /**
     * Converting ISO8601 formatted duration to normal readable time
     */
    public static String convertISO8601DurationToNormalTime(String isoTime)
    {
        String formattedTime = new String();

        if (isoTime.contains("H") && isoTime.contains("M") && isoTime.contains("S")) {
            String hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"));
            String minutes = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("M"));
            String seconds = isoTime.substring(isoTime.indexOf("M") + 1, isoTime.indexOf("S"));
            formattedTime = hours + ":" + formatTo2Digits(minutes) + ":" + formatTo2Digits(seconds);
        } else if (!isoTime.contains("H") && isoTime.contains("M") && isoTime.contains("S")) {
            String minutes = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("M"));
            String seconds = isoTime.substring(isoTime.indexOf("M") + 1, isoTime.indexOf("S"));
            formattedTime = minutes + ":" + formatTo2Digits(seconds);
        } else if (isoTime.contains("H") && !isoTime.contains("M") && isoTime.contains("S")) {
            String hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"));
            String seconds = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("S"));
            formattedTime = hours + ":00:" + formatTo2Digits(seconds);
        } else if (isoTime.contains("H") && isoTime.contains("M") && !isoTime.contains("S")) {
            String hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"));
            String minutes = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("M"));
            formattedTime = hours + ":" + formatTo2Digits(minutes) + ":00";
        } else if (!isoTime.contains("H") && !isoTime.contains("M") && isoTime.contains("S")) {
            String seconds = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("S"));
            formattedTime = "0:" + formatTo2Digits(seconds);
        } else if (!isoTime.contains("H") && isoTime.contains("M") && !isoTime.contains("S")) {
            String minutes = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("M"));
            formattedTime = minutes + ":00";
        } else if (isoTime.contains("H") && !isoTime.contains("M") && !isoTime.contains("S")) {
            String hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"));
            formattedTime = hours + ":00:00";
        }

        return formattedTime;
    }

    /**
     * Makes values consist of 2 letters "01"
     */
    private static String formatTo2Digits(String str)
    {
        if (str.length() < 2) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * Prints videos nicely formatted
     *
     * @param videos
     */
    public static void prettyPrintVideos(List<YouTubeVideo> videos)
    {
        Log.d(TAG, "=============================================================");
        Log.d(TAG, "\t\tTotal Videos: " + videos.size());
        Log.d(TAG, "=============================================================\n");

        Iterator<YouTubeVideo> playlistEntries = videos.iterator();

        while (playlistEntries.hasNext()) {
            YouTubeVideo playlistItem = playlistEntries.next();
            Log.d(TAG, " video name  = " + playlistItem.getTitle());
            Log.d(TAG, " video id    = " + playlistItem.getId());
            Log.d(TAG, " duration    = " + playlistItem.getDuration());
            Log.d(TAG, " thumbnail   = " + playlistItem.getThumbnailURL());
            Log.d(TAG, "\n-------------------------------------------------------------\n");
        }
    }

    /**
     * Prints video nicely formatted
     *
     * @param playlistEntry
     */
    public static void prettyPrintVideoItem(YouTubeVideo playlistEntry)
    {
        Log.d(TAG, "*************************************************************");
        Log.d(TAG, "\t\tItem:");
        Log.d(TAG, "*************************************************************");

        Log.d(TAG, " video name  = " + playlistEntry.getTitle());
        Log.d(TAG, " video id    = " + playlistEntry.getId());
        Log.d(TAG, " duration    = " + playlistEntry.getDuration());
        Log.d(TAG, " thumbnail   = " + playlistEntry.getThumbnailURL());
        Log.d(TAG, "\n*************************************************************\n");
    }


    public static boolean validateUrl(String url)
    {
        // https://r8---sn-3u-bh2ee.googlevideo.com/videoplayback
        return url.contains(".googlevideo.com/videoplayback");
    }

    /**
     * Get the best available audio stream
     *
     * @param ytFiles Array of available streams
     * @return Audio stream with highest bitrate
     */
    public static YtFile getBestStream(SparseArray<YtFile> ytFiles)
    {
//        Log.e(TAG, "ytFiles: " + ytFiles);
        if (ytFiles.get(YOUTUBE_ITAG_141) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_141");
            return ytFiles.get(YOUTUBE_ITAG_141);
        } else if (ytFiles.get(YOUTUBE_ITAG_140) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_140");
            return ytFiles.get(YOUTUBE_ITAG_140);
        } else if (ytFiles.get(YOUTUBE_ITAG_251) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_251");
            return ytFiles.get(YOUTUBE_ITAG_251);
        } else if (ytFiles.get(YOUTUBE_ITAG_250) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_250");
            return ytFiles.get(YOUTUBE_ITAG_250);
        } else if (ytFiles.get(YOUTUBE_ITAG_249) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_249");
            return ytFiles.get(YOUTUBE_ITAG_249);
        } else if (ytFiles.get(YOUTUBE_ITAG_171) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_171");
            return ytFiles.get(YOUTUBE_ITAG_171);
        } else if (ytFiles.get(YOUTUBE_ITAG_18) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_18");
            return ytFiles.get(YOUTUBE_ITAG_18);
        } else if (ytFiles.get(YOUTUBE_ITAG_22) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_22");
            return ytFiles.get(YOUTUBE_ITAG_22);
        } else if (ytFiles.get(YOUTUBE_ITAG_43) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_43");
            return ytFiles.get(YOUTUBE_ITAG_43);
        } else if (ytFiles.get(YOUTUBE_ITAG_36) != null) {
            LogHelper.e(TAG, " gets YOUTUBE_ITAG_36");
            return ytFiles.get(YOUTUBE_ITAG_36);
        }

        LogHelper.e(TAG, " gets YOUTUBE_ITAG_17");
        return ytFiles.get(YOUTUBE_ITAG_17);
    }

    /**
     * Concatenates provided ids in order to search for all of them at once and not in many iterations (slower)
     *
     * @param searchResults results acquired from search query
     * @return concatenated ids
     */
    public static String concatenateIDs(List<SearchResult> searchResults)
    {
        StringBuilder contentDetails = new StringBuilder();
        for (SearchResult result : searchResults) {
            if (result.getId() == null) {
                continue;
            }

            String id = result.getId().getVideoId();
            if (id != null) {
                contentDetails.append(id);
                contentDetails.append(",");
            }
        }

        if (contentDetails.length() == 0) {
            return null;
        }

        if (contentDetails.toString().endsWith(",")) {
            contentDetails.setLength(contentDetails.length() - 1); // remove last
        }

        return contentDetails.toString();
    }

    public static boolean isEmpty(final String s)
    {
        // Null-safe, short-circuit evaluation.
        return s == null || s.trim().isEmpty();
    }
}