import fsl.uiuc.Main;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {

    //        @Test
    public void testMain1() throws Exception {
        String[] args = new String[]{"./test/insert-smallLog/insert.sig", "./test/insert-smallLog/insert.fl",
                "./test/insert-smallLog/insert.log"};
        Main.main(args);
    }

    //    @Test
    public void test9M_Single_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLog_multiTimes("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly", 10);
    }

    //    @Test
    public void test9M_Single_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLog_multiTimes("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly", 1);
    }

    //    @Test
    public void test9M_Single_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLog_multiTimes("/home/hexiao2/DATA/ldcc4Monpoly", 10);
    }


    //                @Test
    public void test9MLog_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly"};
        Main.main(args);
    }


    //                    @Test
    public void test9MLog_singleViolation_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/DATA/ldcc4Monpoly_BaseExecTime"};
        Main.main(args);
    }

    //    @Test
    public void test9MLog_Siebel_SingleRun() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
//        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
//                "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldcc4Monpoly_buggy"};

        String logPath_base = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldcc4Monpoly_buggy";
        String logPath = "/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly";
        Common.test9MLog_multiTimes(logPath_base, 1);

    }

//    @Test
    public void test9MLogBuffSize_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLogBuffSize("/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly");
    }

    //            @Test
    public void test9MLogBuffSize_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLogBuffSize("/home/hexiao2/DATA/ldcc4Monpoly");
    }

    //        @Test
    public void test9MLogBuffSize_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLogBuffSize("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly");
    }


    //                @Test
    public void test9MLogBuffSize_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.test9MLogBuffSize("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly");
    }


    //    @Test
    public void testMain() throws Exception {
        String[] args = new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
