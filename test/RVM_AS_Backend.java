import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 1/25/15.
 */
public class RVM_AS_Backend {
    String path2SmallLogFile = "./test/pub-approve/Pub.log";
    String path2CompleteLogWithSingleViolation = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldccComplete_MonpolyStyle_addMore";


    @Test
    public void testPub0() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Common.testLog_multiTimes(path2SmallLogFile, 1);
    }

    //    @Test
    public void testPub() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Common.testLog_multiTimes(path2CompleteLogWithSingleViolation, 1);
    }
}
