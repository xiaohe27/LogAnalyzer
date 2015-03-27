package fsl.uiuc;

import log.LogEntryExtractor_CSV;
import log.LogExtractor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    private static HashSet<String> monitoredEventSet = initMonitoredEventsSet();
    public static Path outputPath = Paths.get("./test-out/violation.txt");
    public static int maxNumOfParams = 1;
    private static HashMap<String, Integer> methodInfo = initMethodInfo();

    private static HashSet<String> initMonitoredEventsSet() {
        HashSet<String> setOfEvents = new HashSet<String>();
        setOfEvents.add("approve");
        setOfEvents.add("publish");
        return setOfEvents;
    }

    public static boolean isMonitoredEvent(String event) {
        return monitoredEventSet.contains(event);
    }

    private static HashMap<String, Integer> initMethodInfo() {
        HashMap<String, Integer> methodInfoTable = new HashMap<>();
        methodInfoTable.put("approve", 1);
        methodInfoTable.put("publish", 1);
        return methodInfoTable;
    }


    private static void initOutputFile() throws IOException {
        File file = outputPath.toFile();
        if (file.exists()) {
            new PrintWriter(file).close();
        } else {
            if (outputPath.getParent().toFile().exists()) {
                file.createNewFile();
            } else {
                outputPath.getParent().toFile().mkdirs();
                file.createNewFile();
            }
        }
    }

    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */


    /**
     * Given the path to signature file, formula file and log file, checks whether the properties stated in
     * the formula file are violated by the log file.
     *
     * @param args Three arguments need to be provided in the order of: sig file, formula file, log file.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */

    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        assert args.length == 1 && args[0].endsWith(".log") :
                "The only argument needed is the log file (with .log suffix).";

        initOutputFile();

        Path path2Log = path2Log = Paths.get(args[0]);
        LogExtractor lee = new LogEntryExtractor_CSV(methodInfo, path2Log, 6);
        lee.startReadingEventsByteByByte();
    }

}
