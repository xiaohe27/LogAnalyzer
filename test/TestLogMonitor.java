import fsl.uiuc.Main;
import log.LogEntryExtractor_ByteBuffer_AllocateDirect;
import org.junit.Test;
import sig.SigExtractor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {

    //      @Test
    public void testMain1() throws Exception {
        String[] args = new String[]{"./test/insert-smallLog/insert.sig", "./test/insert-smallLog/insert.fl",
                "./test/insert-smallLog/insert.log"};
        Main.main(args);
    }

    //        @Test
    public void test9M_Single_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLog_multiTimes("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly", 1);
    }

//    @Test
    public void test9M_Single_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLog_multiTimes("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly", 1);
    }

    //    @Test
    public void test9M_Single_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLog_multiTimes("/home/hexiao2/DATA/ldcc4Monpoly", 10);
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

            @Test
    public void test9MLog_singleViolation_HP() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly_BaseExecTime"};
        Main.main(args);
    }

    //    @Test
    public void test9MLog_Siebel_SingleRun() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {

        String logPath_base = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldcc4Monpoly_buggy";
//        Common.testLog_multiTimes(logPath_base, 5, true); //eager eval

        Common.testLog_multiTimes(logPath_base, 5, false); //lazy eval

    }

    //    @Test
    public void test9MLogBuffSize_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLogBuffSize("/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly");
    }

    //            @Test
    public void test9MLogBuffSize_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLogBuffSize("/home/hexiao2/DATA/ldcc4Monpoly");
    }

    //        @Test
    public void test9MLogBuffSize_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLogBuffSize("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly");
    }


    //                    @Test
    public void test9MLogBuffSize_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        Common.testLogBuffSize("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly");
    }

//    @Test
    public void parseLogByDirectByteBuffer() throws IOException {
        String logFile = "A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly";
        Path logPath = Paths.get(logFile);
        LogEntryExtractor_ByteBuffer_AllocateDirect lee = new LogEntryExtractor_ByteBuffer_AllocateDirect(SigExtractor.TableCol, logPath);

        long startT = System.currentTimeMillis();
        lee.startReadingEventsByteByByte();
        long timeDiff = System.currentTimeMillis() - startT;

        System.out.println("It takes my log analyzer " + timeDiff +
                " ms to count all the events in the log file after running ");

    }

    //    @Test
    public void testMain() throws Exception {
        String[] args = new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
