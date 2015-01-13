package analysis;

import log.LogEntryExtractor;
import reg.RegHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by xiaohe on 11/24/14.
 */
public class LogMonitor {
    private HashMap<String, Integer[]> TableCol;

    private HashMap<String, Class[]> MethodArgListMap = new HashMap<>();

    private Class monitorClass;

    public LogMonitor(HashMap<String, Integer[]> tableCol, String libName) throws ClassNotFoundException {
        this.TableCol = tableCol;
        init(); //instantiate the MethodArgListMap
        this.monitorClass = Class.forName(libName);
    }

    private void init() {

        for (String tableName : TableCol.keySet()) {
            Integer[] types4CurTable = TableCol.get(tableName);

            Class[] argTyList4CurMeth = new Class[types4CurTable.length + 1];
            for (int i = 0; i < types4CurTable.length; i++) {
                switch (types4CurTable[i]) {
                    case RegHelper.INT_TYPE:
                        argTyList4CurMeth[i] = Integer.class;
                        break;

                    case RegHelper.FLOAT_TYPE:
                        argTyList4CurMeth[i] = Float.class;
                        break;

                    case RegHelper.STRING_TYPE:
                        argTyList4CurMeth[i] = String.class;
                        break;

                    default: {
                        System.err.println("Unknown type: only support int, float, double, long and string at " +
                                "the moment!");
                        System.exit(0);
                    }
                }
            }

            //append the long type at the end which is the type for the timestamp.
            argTyList4CurMeth[argTyList4CurMeth.length - 1] = long.class;

            MethodArgListMap.put(tableName, argTyList4CurMeth);
        }
    }


    /**
     * A method only for testing purpose.
     *
     * @param path
     */

    public void monitor3(Path path) throws IOException {
        int num = 0;
        Scanner scan = new Scanner(path);
        while (scan.hasNextLine()) {
            if (scan.nextLine().contains("@")) {
//                System.out.println("log entry found");
                num++;
            }
        }

        System.out.println("There are totally " + num + " log entries in the log file!");
    }

    /**
     * A method only for testing purpose.
     *
     * @param path2LogFile
     */
    public void monitor(Path path2LogFile) throws IOException {
        LogEntryExtractor lee = null;

        if (path2LogFile != null) {
            //the path to the log file should be obtained from outside as an argument of 'main'
            File logFile = path2LogFile.toFile();

            lee = new LogEntryExtractor(this.TableCol, path2LogFile);

        } else { //path to log file is null: indicating the scanner will read log entries from System.in
//            lee = new LogEntryExtractor(this.TableCol);
        }

        long startT = System.currentTimeMillis();
        lee.startLineByLine();

        long totalT = System.currentTimeMillis() - startT;

        System.out.println("It took my log analyzer " + totalT + " ms to " +
                "count all the log entries in the log file.");
    }


    /**
     * @param path2LogFile
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
//    public void monitor_real(Path path2LogFile) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//
//        try {
//            LogEntryExtractor lee = null;
//
//            if (path2LogFile != null) {
//                //the path to the log file should be obtained from outside as an argument of 'main'
//                File logFile = path2LogFile.toFile();
//
//                lee = new LogEntryExtractor(this.TableCol, logFile);
//
//            } else { //path to log file is null: indicating the scanner will read log entries from System.in
//                lee = new LogEntryExtractor(this.TableCol);
//            }
//
//            while (lee.hasNext()) {
//                //by comparing the list of args of list of types,
//                //we will know which arg has what type. Types of each field for
//                //every event can be obtained from the sig file (gen a map of
//                // string(event name) to list(type list of the tuple)).
//                LogEntry logEntry = lee.nextLogEntry();
//
//                String ts = logEntry.getTime();
//                Iterator<String> tableNameIter = logEntry.getTableMap().keySet().iterator();
//                while (tableNameIter.hasNext()) {
//
//                    //the order of eval the events matters!!!
//                    //multiple events may happen at the same timepoint, if publish event is sent to monitor first
//                    //and then the approve event, false alarm will be triggered!
//
//
//                    String eventName = tableNameIter.next();
//
//                    if (!eventName.equals("insert"))
//                        continue;
//
//
//                    List<LogEntry.EventArg> tuples = logEntry.getTableMap().get(eventName);
//                    for (int i = 0; i < tuples.size(); i++) {
//                        LogEntry.EventArg curTuple = tuples.get(i);
//
////                        if(curTuple.getFields()[1].equals("db2") && !(curTuple.getFields()[0].equals("script1"))) {
//////                            curTuple.print();
////                        }
//
//
//                        Object[] fields = curTuple.getFields();
//
//                        String methName = eventName + "Event";
//                        Class[] paramTypes = MethodArgListMap.get(eventName);
//                        Object[] args4MonitorMethod = new Object[fields.length + 1];
//                        System.arraycopy(fields, 0, args4MonitorMethod, 0, fields.length);
//                        //the last arg is the timestamp.
//                        args4MonitorMethod[args4MonitorMethod.length - 1] = logEntry.getTime();
//
//                        Method monitorMethod = this.monitorClass.getDeclaredMethod(methName, paramTypes);
//                        monitorMethod.invoke(null, args4MonitorMethod);
//
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
}
