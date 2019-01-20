package oriontech.com.musical.youtube;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;

import java.util.Arrays;

import oriontech.com.musical.R;
import oriontech.com.musical.utils.LogHelper;

import static oriontech.com.musical.utils.Auth.SCOPES;

public class YouTubeSingleton
{
    private static String TAG = LogHelper.makeLogTag(YouTubeSingleton.class);

    private static YouTube youTube;
    private static YouTube youTubeWithCredentials;
    private static GoogleAccountCredential credential;


    // Create the instance
    private static YouTubeSingleton instance = null;
    private static final Object mutex = new Object();

    public static YouTubeSingleton getInstance(Context context)
    {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null)
                    instance = new YouTubeSingleton(context);
            }
        }
        // Return the instance
        return instance;
    }

    private YouTubeSingleton(Context context)
    {
        String appName = context.getString(R.string.app_name);
        credential = GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        youTube = new YouTube.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                httpRequest -> {}
        ).setApplicationName(appName).build();

        youTubeWithCredentials = new YouTube.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                credential
        ).setApplicationName(appName).build();
    }

    public static YouTube getYouTube()
    {
        return youTube;
    }

    public static YouTube getYouTubeWithCredentials()
    {
        return youTubeWithCredentials;
    }

    public static GoogleAccountCredential getCredential()
    {
        return credential;
    }
}
