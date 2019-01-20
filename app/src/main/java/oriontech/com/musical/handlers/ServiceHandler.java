package oriontech.com.musical.handlers;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import oriontech.com.musical.services.BackgroundExoAudioService;

public class ServiceHandler extends Handler
{
    private final WeakReference<BackgroundExoAudioService> weakReference;

    public ServiceHandler(BackgroundExoAudioService service)
    {
        weakReference = new WeakReference<>(service);
    }

    // Define how to handle any incoming messages here
    @Override
    public void handleMessage(Message message)
    {
//            LogHelper.e(TAG, "ServiceHandler | handleMessage");
        BackgroundExoAudioService service = weakReference.get();
        if (service != null && service.getPlayback() != null) {
            if (service.getPlayback().isPlaying()) {
                service.sendSessionTokenToActivity();
            }
        }
    }
}
