package http.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }
        out.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        String s = in.nextString();
        return Duration.parse(s);
    }
}