/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
public class Performer {
    private final StringList state;

    public Performer(StringList strings) {
        this.state = strings;
    }

    public JSONObject add(String str) {
        synchronized (state) {
            state.add(str);
            return createResponse("add", state.toString());
        }
    }

    public JSONObject display() {
        synchronized (state) {
            return createResponse("display", state.toString());
        }
    }

    public JSONObject count() {
        synchronized (state) {
            return createResponse("count", Integer.toString(state.size()));
        }
    }

    public JSONObject quit() {
        return createResponse("quit", "Goodbye!");
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }

    private JSONObject createResponse(String type, String data) {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("data", data);
        return json;
    }
}
