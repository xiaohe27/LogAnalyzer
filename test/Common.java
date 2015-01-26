import analysis.LogMonitor;
import gen.MonitorGenerator;
import log.LogEntryExtractor;
import log.LogEntryExtractor_Eager;
import log.LogExtractor;
import sig.SigExtractor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by xiaohe on 15-1-18.
 */
public class Common {
    public static void testLog_multiTimes(String path, int num) throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        testLog_multiTimes(path, num, false); //by default, use lazy eval
    }

    public static void testLog_multiTimes(String path, int num, boolean eager) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        Path logFile = Paths.get(path);
        MonitorGenerator mg = new MonitorGenerator(logFile, null);

        LogMonitor lm = new LogMonitor(mg.getMethoArgsMappingFromSigFile(), mg.getMonitorClassPath());


        long[] timeArr = new long[num];
        for (int i = 0; i < num; i++) {
            LogExtractor lee = (eager ? new LogEntryExtractor_Eager(SigExtractor.TableCol, logFile) :
                    new LogEntryExtractor(SigExtractor.TableCol, logFile, lm.getEventNameMethodMap()));

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

    public static void testLogBuffSize(String logFile) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        Path logFilePath = Paths.get(logFile);

        MonitorGenerator mg = new MonitorGenerator(logFilePath, null);

        LogMonitor lm = new LogMonitor(mg.getMethoArgsMappingFromSigFile(), mg.getMonitorClassPath());

        int numOfLines = 2;
        int numOfCols = 3;

        int offset = 6;

        long[][] timeArr = new long[numOfLines][numOfCols];
        long[] avgTimeArr = new long[numOfLines];

        //each i represents a test with a specific buffer size
        //run multiple times to get the avg time
        for (int i = 0; i < timeArr.length; i++) {

            int multiple = (int) Math.pow(2, i + offset);


            LogEntryExtractor lee = new LogEntryExtractor(SigExtractor.TableCol, logFilePath, multiple, lm.getEventNameMethodMap());
//            LogEntryExtractor_ByteBuffer_AllocateDirect lee = new LogEntryExtractor_ByteBuffer_AllocateDirect(SigExtractor.TableCol, logFile, multiple);

            for (int j = 0; j < timeArr[0].length; j++) {
                long startT = System.currentTimeMillis();

                lee.startReadingEventsByteByByte();

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
