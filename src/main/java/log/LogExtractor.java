package log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by hx312 on 19/01/2015.
 */
public interface LogExtractor {
    public void startReadingEventsByteByByte() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;
}
