package analysis;

import log.LogEntry;
import log.LogEntryExtractor;
import reg.RegHelper;
import rvm.PubRuntimeMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaohe on 11/24/14.
 */
public class LogMonitor {
    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */
    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";

    public static HashMap<String, Integer[]> TableCol = new HashMap<>();

    private static void init() {
        //the arg types can be inferred from the signature file
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE};
        TableCol.put(PUBLISH, argTy4Publish);
        TableCol.put(APPROVE, argTy4Approve);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //These two methods can be gen automatically.
    //event name is the name of the method, and arguments include args of the event plus the additional arg 'time'.
    public static void approve(int report, int time) {
        PubRuntimeMonitor.approveEvent(report, time);
    }

    public static void publish(int report, int time) {

        PubRuntimeMonitor.publishEvent(report, time);
    }

    public static void main(String[] args) {
        init();
        //the path to the log file should be obtained from outside as an argument of 'main'
        File logFile = new File("Pub.log");
        try {
            LogEntryExtractor lee=new LogEntryExtractor(TableCol, logFile);

            while (lee.hasNext()) {
                //by comparing the list of args of list of types,
                //we will know which arg has what type. Types of each field for
                //every event can be obtained from the sig file (gen a map of
                // string(event name) to list(type list of the tuple)).
                LogEntry logEntry = lee.nextLogEntry();
                long ts = logEntry.getTime();
                Iterator<String> tableNameIter = logEntry.getTableMap().keySet().iterator();
                while (tableNameIter.hasNext()) {

                    //the order of eval the events matters!!!
                    //multiple events may happen at the same timepoint, if publish event is sent to monitor first
                    //and then the approve event, false alarm will be triggered!


                    String eventName = tableNameIter.next();
                    List<LogEntry.EventArg> tuples = logEntry.getTableMap().get(eventName);
                    for (int i = 0; i < tuples.size(); i++) {
                        LogEntry.EventArg curTuple = tuples.get(i);
                        Object[] fields = curTuple.getFields();

                        switch (eventName) {
                            //traverse all the monitored events
                            case APPROVE:
                                for (int j = 0; j < fields.length; j++) {
                                    PubRuntimeMonitor.approveEvent(
                                            (int) (fields[j]),         //the type comes from sig file.
                                            logEntry.getTime()
                                    );

                                    System.out.println("approve " +
                                            (int) (fields[j]) + " at time " + logEntry.getTime());
                                }

                                break;

                            case PUBLISH:
                                for (int j = 0; j < fields.length; j++) {
                                    PubRuntimeMonitor.publishEvent(
                                            (int) (fields[j]),
                                            logEntry.getTime()
                                    );
                                    System.out.println("publish " +
                                            (int) (fields[j]) + " at time " + logEntry.getTime());
                                }

                                break;
                        }
                    }
                }


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
