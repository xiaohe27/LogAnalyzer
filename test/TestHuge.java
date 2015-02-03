import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 1/21/15.
 */
public class TestHuge {
    String path2CompleteLog = "/home/xiaohe/workspace/DATA/ldccComplete_MonpolyStyle";
    String path2CompleteLogWithSingleViolation = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldccComplete_MonpolyStyle_addMore";

    @Test
    public void parseHuge() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Common.testLog_multiTimes(this.path2CompleteLogWithSingleViolation, 1);
    }


}
