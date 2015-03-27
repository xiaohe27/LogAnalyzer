package rvm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class LogReader {
    //a strict log reader will reject a log if it contains an event with unknown name (not occur
    // in the specification file)
    public static boolean strictLogReader;
    private static HashSet<String> monitoredEventSet = LogReader.initMonitoredEventsSet();
    public static Path outputPath = Paths.get("./test-out/violation.txt");
    public static int maxNumOfParams = 1;
    private static HashMap<String, int[]> methodInfo = LogReader.initMethodInfo();

    private static HashSet<String> initMonitoredEventsSet() {
        HashSet<String> setOfEvents = new HashSet<String>();
        setOfEvents.add("approve");
        setOfEvents.add("publish");
        return setOfEvents;
    }

    public static boolean isMonitoredEvent(String event) {
        return LogReader.monitoredEventSet.contains(event);
    }

    private static HashMap<String, int[]> initMethodInfo() {
        HashMap<String, int[]> methodInfoTable = new HashMap<String, int[]>();
        methodInfoTable.put("approve", new int[] { 0 });
        methodInfoTable.put("publish", new int[] { 0 });
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


    public static void main(String[] args) throws IOException {
        assert args.length == 1 && args[0].endsWith(".log") :
                "The only argument needed is the log file (with .log suffix).";

        initOutputFile();

        Path path2Log = path2Log = Paths.get(args[0]);
//        LogEntryExtractor lee = new LogEntryExtractor(methodInfo, path2Log, 6);
//        lee.startReadingEventsByteByByte();
    }

}