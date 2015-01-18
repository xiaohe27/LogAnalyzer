import fsl.uiuc.Main;
import log.LogEntryExtractor;
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

    //        @Test
    public void testMain1() throws Exception {
        String[] args = new String[]{"./test/insert-smallLog/insert.sig", "./test/insert-smallLog/insert.fl",
                "./test/insert-smallLog/insert.log"};
        Main.main(args);
    }

    //    @Test
    public void test9M_Single_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLog_multiTimes("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly", 10);
    }

//    @Test
    public void test9M_Single_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLog_multiTimes("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly", 1);
    }

    //    @Test
    public void test9M_Single_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLog_multiTimes("/home/hexiao2/DATA/ldcc4Monpoly", 10);
    }


    public void test9MLog_multiTimes(String path, int num) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        Path logFile = Paths.get(path);

        long[] timeArr = new long[num];
        for (int i = 0; i < num; i++) {
            LogEntryExtractor lee = new LogEntryExtractor(SigExtractor.TableCol, logFile);

            long startT = System.currentTimeMillis();

            lee.startReadingEventsByteByByte();

            long totalT = System.currentTimeMillis() - startT;

            timeArr[i] = totalT;
        }

        long avgTime = computeAvg(timeArr);
        System.out.println("It takes my log analyzer " + avgTime +
                " ms to count all the events in the log file after running " +
                num + " tests");
    }


//                @Test
    public void test9MLog_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly"};
        Main.main(args);
    }


    //                @Test
    public void test9MLog_singleViolation_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "/DATA/ldcc4Monpoly_BaseExecTime"};
        Main.main(args);
    }

    @Test
    public void test9MLog_Siebel_SingleRun() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
//        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
//                "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldcc4Monpoly_buggy"};

        String logPath_base = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldcc4Monpoly_buggy";
        String logPath = "/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly";
        this.test9MLog_multiTimes(logPath_base, 1);

    }

    //        @Test
    public void test9MLogBuffSize_IdeaPad() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLogBuffSize("/home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly");
    }

    //            @Test
    public void test9MLogBuffSize_UIUC() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLogBuffSize("/home/hexiao2/DATA/ldcc4Monpoly");
    }

//        @Test
    public void test9MLogBuffSize_Siebel() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLogBuffSize("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly");
    }


    //                @Test
    public void test9MLogBuffSize_HP() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        this.test9MLogBuffSize("A:\\DATA\\ldcc4Monpoly.tar\\ldcc4Monpoly");
    }

    public void test9MLogBuffSize(String logFilePath) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        int numOfLines = 2;
        int numOfCols = 3;

        int offset = 6;

        long[][] timeArr = new long[numOfLines][numOfCols];
        long[] avgTimeArr = new long[numOfLines];

        Path logFile = Paths.get(logFilePath);

        //each i represents a test with a specific buffer size
        //run multiple times to get the avg time
        for (int i = 0; i < timeArr.length; i++) {

            int multiple = (int) Math.pow(2, i + offset);


//            LogEntryExtractor lee = new LogEntryExtractor(SigExtractor.TableCol, logFile, multiple);
            LogEntryExtractor_ByteBuffer_AllocateDirect lee = new LogEntryExtractor_ByteBuffer_AllocateDirect(SigExtractor.TableCol, logFile, multiple);

            for (int j = 0; j < timeArr[0].length; j++) {
                long startT = System.currentTimeMillis();

                lee.startReadingEventsByteByByte();

                long totalT = System.currentTimeMillis() - startT;

                timeArr[i][j] = totalT;

                System.out.println("Using buffer of size "+multiple+" KB, it took my log analyzer " + totalT + " ms to " +
                        "count all the log entries in the log file via reading events byte by byte!~!");
            }

            avgTimeArr[i] = computeAvg(timeArr[i]);
            System.out.println("In the test with buf size " + multiple + " KB, the avg time used is " + avgTimeArr[i]);
        }
    }

    private long computeAvg(long[] longs) {
        long sum = 0;
        for (int i = 0; i < longs.length; i++) {
            sum += longs[i];
        }

        return sum / longs.length;
    }

    //    @Test
    public void testMain() throws Exception {
        String[] args = new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
