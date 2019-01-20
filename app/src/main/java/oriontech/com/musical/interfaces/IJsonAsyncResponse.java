package oriontech.com.musical.interfaces;

import java.util.ArrayList;

/**
 * Interface for receive the JSON response asynchronously
 */

public interface IJsonAsyncResponse
{
    void processFinish(ArrayList<String> result);
}