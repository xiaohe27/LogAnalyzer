package log;

import java.io.IOException;

/**
 * Created by hx312 on 19/01/2015.
 */
public interface LogExtractor {
    public void startReadingEventsByteByByte() throws IOException;
}
