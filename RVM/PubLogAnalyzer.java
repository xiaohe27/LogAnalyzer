import reg.RegHelper;
import rvm.LogEntry;
import rvm.PubRuntimeMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class PubLogAnalyzer {
    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";
    /**
     * Given a table name, return the list of types that represent the types for every column.
     */
    private static HashMap<String, Integer[]> TableCol = new HashMap<>();
    private static Scanner scan;

    private static void init() {
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE};
        TableCol.put(PUBLISH, argTy4Publish);
        TableCol.put(APPROVE, argTy4Approve);
    }

    public static void approve(int report, int time) {
        PubRuntimeMonitor.approveEvent(report, time);
    }


    public static void publish(int report, int time) {

        PubRuntimeMonitor.publishEvent(report, time);
    }

    public static void main(String[] args) {
        init();
        File logFile = new File("Pub.log");
        try {
            scan = new Scanner(logFile);
            while (scan.useDelimiter(RegHelper.Delim4FindingTimeStamp).hasNext()) {
                //by comparing the list of args of list of types,
                //we will know which arg has what type. Types of each field for
                //every event can be obtained from the sig file (gen a map of
                // string(event name) to list(type list of the tuple)).
                LogEntry logEntry = getLogEntry();
                long ts = logEntry.getTime();
                Iterator<String> tableNameIter = logEntry.getTableMap().keySet().iterator();
                while (tableNameIter.hasNext()) {
                    String eventName = tableNameIter.next();
                    List<LogEntry.EventArg> tuples = logEntry.getTableMap().get(eventName);
                    for (int i = 0; i < tuples.size(); i++) {
                        LogEntry.EventArg curTuple = tuples.get(i);
                        Object[] fields = curTuple.getFields();

                        switch (eventName) {
                            case APPROVE:
                                for (int j = 0; j < fields.length; j++) {
                                    PubRuntimeMonitor.approveEvent(
                                            (int) (fields[j]),
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

    /**
     * A log entry is a time-stamped database, where a database is a collection of tables.
     * Each table has a name and a number of tuples.
     *
     * @return
     */
    private static LogEntry getLogEntry() {
        long time = 0;
        HashMap<String, List<LogEntry.EventArg>> tableMap = new HashMap<>();

        if (scan.useDelimiter(RegHelper.Delim4FindingTimeStamp).hasNext(RegHelper.TimeStamp)) {
            time = Long.parseLong(scan.next(RegHelper.TimeStamp).replaceAll("\\s","").replace("@", ""));
        }
        else {
            System.err.println("Should have a time stamp for the event!");
            System.exit(0);
        }

        if (!scan.useDelimiter(RegHelper.Delim4FindingEvent).hasNext(RegHelper.EventName)) {
            System.err.println("Should have at least one event name after time stamp.");
            System.exit(0);
        }
        do {
            String eventName = scan.next(RegHelper.EventName).replaceAll("\\s","");
            List<LogEntry.EventArg> eventArgs = new ArrayList<LogEntry.EventArg>();

            do {
                if (scan.useDelimiter(RegHelper.Delim4FindingTupleList).
                        hasNext(RegHelper.TupleListRegEx)) {
                    String curTupleList = scan.next(RegHelper.TupleListRegEx).replaceAll("\\s","");
                    String[] tuples= curTupleList.split("\\)\\(");
                    //after splitting, only the first and last tuple need to be further processed,
                    //all the tuples in the middle have already been in the form of fields.
                    if(tuples.length>0){
                        tuples[0]=tuples[0].replace("(","");
                        tuples[tuples.length-1]=tuples[tuples.length-1].replace(")","");
                    }

                   for (int k=0; k< tuples.length; k++){
                       String fields = tuples[k];
                       Object[] argsInTuple = new Object[TableCol.get(eventName).length];
                       String[] fieldsData = fields.split(",");
                       for (int i = 0; i < TableCol.get(eventName).length; i++) {
                           String dataI= fieldsData[i].replaceAll("\\s","");
                           switch (TableCol.get(eventName)[i]) {
                               case RegHelper.INT_TYPE:
                                   argsInTuple[i] = Integer.parseInt(dataI);
                                   break;

                               case RegHelper.LONG_TYPE:
                                   argsInTuple[i] = Long.parseLong(dataI);
                                   break;

                               case RegHelper.FLOAT_TYPE:
                                   argsInTuple[i] = Float.parseFloat(dataI);
                                   break;

                               case RegHelper.DOUBLE_TYPE:
                                   argsInTuple[i] = Double.parseDouble(dataI);
                                   break;

                               case RegHelper.STRING_TYPE:
                                   argsInTuple[i] = dataI;
                                   break;
                           }
                       }
                       eventArgs.add(new LogEntry.EventArg
                               (argsInTuple));
                   }

                } else {
                    System.err.println("Should have at least one tuple in the table.");
                    System.exit(0);
                }
            } while (scan.hasNext(RegHelper.TupleRegEx));

            tableMap.put(eventName, eventArgs);

//        System.out.println("event is "+eventName+" and time is "+time);
//        System.out.println(eventArgs.size()+" is the num of args");
        } while (scan.useDelimiter(RegHelper.Delim4FindingEvent).hasNext(RegHelper.EventName));

        return new LogEntry(time, tableMap);
    }

}
