import fsl.uiuc.Main;
import log.LogEntryExtractor;
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
    //    @Test
    public void testMain0() throws Exception {
        String[] args = new String[]{"./test/pub/pub.sig", "./test/pub/pub.fl", "./test/pub/Pub.log"};
        Main.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;

    }

    //    @Test
    public void testMain1() throws Exception {
        String[] args = new String[]{"./test/insert-smallLog/insert.sig", "./test/insert-smallLog/insert.fl",
                "./test/insert-smallLog/insert.log"};
        Main.main(args);
    }

    //    @Test
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

    @Test
    public void test9MLog_Siebel_BuffSize() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        int numOfLines=10;
        int numOfCols=10;

        long[][] timeArr = new long[numOfLines][numOfCols];
        long[] avgTimeArr = new long[numOfLines];

        Path logFile=Paths.get("/home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly");

        //each i represents a test with a specific buffer size
        //run multiple times to get the avg time
        for (int i = 0; i < timeArr.length; i++) {

            int multiple= (int) Math.pow(2, i);

            LogEntryExtractor lee = new LogEntryExtractor(SigExtractor.TableCol, logFile, multiple);

            for (int j = 0; j < timeArr[0].length; j++) {
                long startT = System.currentTimeMillis();

                lee.startReadingEventsByteByByte();

                long totalT = System.currentTimeMillis() - startT;

                timeArr[i][j] = totalT;

//                System.out.println("Using buffer of size "+multiple+" KB, it took my log analyzer " + totalT + " ms to " +
//                        "count all the log entries in the log file via reading events byte by byte!~!");
            }

            avgTimeArr[i] = computeAvg(timeArr[i]);
            System.out.println("In the test with buf size "+multiple+" KB, the avg time used is "+avgTimeArr[i]);

        }



    }

    private long computeAvg(long[] longs) {
        long sum=0;
        for (int i = 0; i < longs.length; i++) {
            sum+=longs[i];
        }

        return sum/longs.length;
    }

    //    @Test
    public void testMain() throws Exception {
        String[] args = new String[]{"./test/insert-hugeLog/insert.sig", "./test/insert-hugeLog/insert.fl"
//                ,"/home/xiaohe/SW/offline-log-analysis/ldcc.csv"
        };

        Main.main(args);
    }
}
