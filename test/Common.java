import log.LogReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
* Created by xiaohe on 15-1-18.
*/
public class Common {
    public static void testLog_multiTimes(String path, int num) throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        testLog_multiTimes(path, num, false); //by default, use lazy eval
    }

    public static void testLog_multiTimes(String path, int num, boolean eager) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", path};

        long[] timeArr = new long[num];
        for (int i = 0; i < num; i++) {


            long startT = System.currentTimeMillis();

            LogReader.main(args);

            long totalT = System.currentTimeMillis() - startT;

            timeArr[i] = totalT;
        }

        long avgTime = computeAvg(timeArr);
        System.out.println("It takes my log analyzer " + avgTime +
                " ms to count all the events in the log file after running " +
                num + " tests");
    }

    public static void testLogBuffSize(String logFile) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", logFile};

        int numOfLines = 2;
        int numOfCols = 3;

        int offset = 6;

        long[][] timeArr = new long[numOfLines][numOfCols];
        long[] avgTimeArr = new long[numOfLines];

        //each i represents a test with a specific buffer size
        //run multiple times to get the avg time
        for (int i = 0; i < timeArr.length; i++) {

            int multiple = (int) Math.pow(2, i + offset);


            for (int j = 0; j < timeArr[0].length; j++) {
                long startT = System.currentTimeMillis();

                LogReader.main(args);

                long totalT = System.currentTimeMillis() - startT;

                timeArr[i][j] = totalT;

                System.out.println("Using buffer of size " + multiple + " KB, it took my log analyzer " + totalT + " ms to " +
                        "count all the log entries in the log file via reading events byte by byte!~!");
            }

            avgTimeArr[i] = computeAvg(timeArr[i]);
            System.out.println("In the test with buf size " + multiple + " KB, the avg time used is " + avgTimeArr[i]);
        }
    }

    private static long computeAvg(long[] longs) {
        long sum = 0;
        for (int i = 0; i < longs.length; i++) {
            sum += longs[i];
        }

        return sum / longs.length;
    }

}
