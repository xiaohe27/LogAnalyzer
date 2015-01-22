import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 1/21/15.
 */
public class TestHuge {
    String path2CompleteLog = "/home/xiaohe/workspace/DATA/ldccComplete_MonpolyStyle";

    @Test
    public void parseHuge() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Common.testLog_multiTimes(this.path2CompleteLog, 1);
    }
}
