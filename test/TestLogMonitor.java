import fsl.uiuc.Main;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {
    @Test
    public void testMain() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args=new String[]{"./test/pub.sig","./test/pub.fl","./test/Pub.log"};
        Main.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;
        
    }
}
