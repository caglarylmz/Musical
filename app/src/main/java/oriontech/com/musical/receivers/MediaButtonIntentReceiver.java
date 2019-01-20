package oriontech.com.musical.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import oriontech.com.musical.utils.LogHelper;

public class MediaButtonIntentReceiver extends BroadcastReceiver
{
    private static final String TAG = LogHelper.makeLogTag(MediaButtonIntentReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        LogHelper.e(TAG, "onReceive MediaButtonIntentReceiver");
    }
}