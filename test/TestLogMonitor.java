import fsl.uiuc.Main;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {
    //    @Test
    public void testMain0() throws Exception {
        String[] args = new String[]{"./test/pub/pub.sig", "./test/pub/pub.fl", "./test/pub/Pub.log"};
        Main.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;

    }

    @Test
    public void testMain1() throws Exception {
        String[] args = new String[]{"./test/insert-smallLog/insert.sig", "./test/insert-smallLog/insert.fl",
                "./test/insert-smallLog/insert.log"};
        Main.main(args);
    }

    //        @Test
    public void test9MLog_HP() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly"};
        Main.main(args);
    }

//        @Test
    public void test9MLog_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly"};
        Main.main(args);
    }

//        @Test
    public void test9MLog_Siebel() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly"};
        Main.main(args);
    }

    //    @Test
    public void testMain() throws Exception {
        String[] args = new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
