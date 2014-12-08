import fsl.uiuc.Main;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {
//    @Test
    public void testMain0() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args=new String[]{"./test/pub/pub.sig","./test/pub/pub.fl","./test/pub/Pub.log"};
        Main.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;

    }

//    @Test
    public void testMain1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args=new String[]{"./test/insert-hugeLog/insert.sig","./test/insert-hugeLog/insert.fl",
                                     "./test/insert-hugeLog/insert.log"};
        Main.main(args);
    }


    @Test
    public void testMain() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args=new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
