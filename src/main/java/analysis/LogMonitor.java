package analysis;

import formula.FormulaExtractor;
import log.*;
import reg.RegHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by xiaohe on 11/24/14.
 */
public class LogMonitor {
    public Class monitorClass;
    private HashMap<String, int[]> TableCol;
    private HashMap<String, Method> EventNameMethodMap = new HashMap<>();

    public LogMonitor(HashMap<String, int[]> tableCol, String libName) throws ClassNotFoundException, NoSuchMethodException {
        this.TableCol = tableCol;
        this.monitorClass = Class.forName(libName);
        init(); //instantiate the EventNameMethodMap
    }

    public HashMap<String, Method> getEventNameMethodMap() {
        return this.EventNameMethodMap;
    }

    private void init() throws NoSuchMethodException {

        for (String eventName : TableCol.keySet()) {
            int[] types4CurTable = TableCol.get(eventName);

            Class[] argTyList4CurMeth = new Class[types4CurTable.length];
            for (int i = 0; i < types4CurTable.length; i++) {
                switch (types4CurTable[i]) {
                    case RegHelper.INT_TYPE:
                        argTyList4CurMeth[i] = Integer.class;
                        break;

                    case RegHelper.FLOAT_TYPE:
                        argTyList4CurMeth[i] = Double.class;
                        break;

                    case RegHelper.STRING_TYPE:
                        argTyList4CurMeth[i] = String.class;
                        break;

                    default: {
                        System.err.println("Unknown type: only support int, double and string at " +
                                "the moment!");
                        System.exit(0);
                    }
                }
            }

            if (FormulaExtractor.monitoredEventList.contains(eventName)) {
                String methName = eventName + "Event";
                Method monitorMethod = this.monitorClass.getDeclaredMethod(methName, argTyList4CurMeth);
                this.EventNameMethodMap.put(eventName, monitorMethod);
            }
        }
    }


//    public void triggerEvent(String EventName, Object[] tupleData) throws InvocationTargetException, IllegalAccessException, IOException, NoSuchMethodException {
//
//    }


    /**
     * A method only for testing purpose.
     *
     * @param path2LogFile
     */
    public void monitorWithProfiling(Path path2LogFile, boolean isTarGz) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LogExtractor lee;
        if (path2LogFile != null) {
            //the path to the log file should be obtained from outside as an argument of 'main'
            if (isTarGz) {
                lee = new LogEntryExtractor_FromArchive(this.TableCol, path2LogFile);
            } else {
                lee = new LogEntryExtractor(this.TableCol, path2LogFile, this.EventNameMethodMap);
            }
        } else { //path to log file is null: indicating the scanner will read log entries from System.in
            lee = null;
        }

        System.out.println("Please get ready to profile the app, input a line with `enter` please.");
        Scanner scan = new Scanner(System.in);
        String res = scan.nextLine();
        System.out.println("response is " + res);

        long startT = System.currentTimeMillis();

        lee.startReadingEventsByteByByte();

        long totalT = System.currentTimeMillis() - startT;

        System.out.println("It took my log analyzer " + totalT + " ms to " +
                "count all the log entries in the log file in the format of " + (isTarGz ? "tar.gz" : "plain txt"));
    }


    public void monitor(Path path2LogFile, boolean isTarGz) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.monitor(path2LogFile, isTarGz, false);
    }

    public void monitor(Path path2LogFile) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.monitor(path2LogFile, false, false);
    }

    /**
     * A method only for testing purpose.
     *
     * @param path2LogFile
     */
    public void monitor(Path path2LogFile, boolean isTarGz, boolean eagerEval) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LogExtractor lee = null;

        if (path2LogFile != null) {
            //the path to the log file should be obtained from outside as an argument of 'main'
            if (isTarGz) {
                lee = new LogEntryExtractor_FromArchive(this.TableCol, path2LogFile, 8);
            } else {
                if (eagerEval) {
                    lee = new LogEntryExtractor_Eager(this.TableCol, path2LogFile);
                } else {
                    lee = new LogEntryExtractor(this.TableCol, path2LogFile, 6, this.EventNameMethodMap); //use lazy eval strategy.
                }
            }

        } else { //path to log file is null: indicating the scanner will read log entries from System.in
//            lee = new LogEntryExtractor(this.TableCol);
        }

        long startT = System.currentTimeMillis();

        lee.startReadingEventsByteByByte();

        long totalT = System.currentTimeMillis() - startT;

        System.out.println("It took my log analyzer " + totalT + " ms to " +
                "count all the log entries in the log file in the format of " + (isTarGz ? "tar.gz" : "plain txt"));
    }

    public void monitor_bytebuffer_allocateDirect(Path path2LogFile) throws IOException {
        LogEntryExtractor_ByteBuffer_AllocateDirect lee = null;

        if (path2LogFile != null) {
            //the path to the log file should be obtained from outside as an argument of 'main'
            File logFile = path2LogFile.toFile();

            lee = new LogEntryExtractor_ByteBuffer_AllocateDirect(this.TableCol, path2LogFile);

        } else { //path to log file is null: indicating the scanner will read log entries from System.in
//            lee = new LogEntryExtractor(this.TableCol);
        }


        long startT = System.currentTimeMillis();

        lee.startReadingEventsByteByByte();

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
//                        Class[] paramTypes = EventNameMethodMap.get(eventName);
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
