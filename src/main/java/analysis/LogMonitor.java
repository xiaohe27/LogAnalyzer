//package analysis;
//
//import formula.FormulaExtractor;
//import log.LogEntryExtractor;
//import log.LogEntryExtractor_FromArchive;
//import log.LogExtractor;
//import reg.RegHelper;
//
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Scanner;
//
///**
// * Created by xiaohe on 11/24/14.
// */
//public class LogMonitor {
//    public Class monitorClass;
//    private HashMap<String, int[]> TableCol;
//    private HashMap<String, Method> EventNameMethodMap = new HashMap<>();
//
//    public LogMonitor(HashMap<String, int[]> tableCol, String libName) throws ClassNotFoundException, NoSuchMethodException {
//        this.TableCol = tableCol;
//        this.monitorClass = Class.forName(libName);
//        init(); //instantiate the EventNameMethodMap
//    }
//
//    public HashMap<String, Method> getEventNameMethodMap() {
//        return this.EventNameMethodMap;
//    }
//
//    private void init() throws NoSuchMethodException {
//
//        for (String eventName : TableCol.keySet()) {
//            int[] types4CurTable = TableCol.get(eventName);
//
//            Class[] argTyList4CurMeth = new Class[types4CurTable.length];
//            for (int i = 0; i < types4CurTable.length; i++) {
//                switch (types4CurTable[i]) {
//                    case RegHelper.INT_TYPE:
//                        argTyList4CurMeth[i] = Integer.class;
//                        break;
//
//                    case RegHelper.FLOAT_TYPE:
//                        argTyList4CurMeth[i] = Double.class;
//                        break;
//
//                    case RegHelper.STRING_TYPE:
//                        argTyList4CurMeth[i] = String.class;
//                        break;
//
//                    default: {
//                        System.err.println("Unknown type: only support int, double and string at " +
//                                "the moment!");
//                        System.exit(0);
//                    }
//                }
//            }
//
//            if (FormulaExtractor.monitoredEventList.contains(eventName)) {
//                String methName = eventName + "Event";
//                Method monitorMethod = this.monitorClass.getDeclaredMethod(methName, argTyList4CurMeth);
//                this.EventNameMethodMap.put(eventName, monitorMethod);
//            }
//        }
//    }
//
//

//
//
//    /**
//     * A method only for testing purpose.
//     *
//     * @param path2LogFile
//     */
//    public void monitorWithProfiling(Path path2LogFile, boolean isTarGz) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        LogExtractor lee;
//        if (path2LogFile != null) {
//            //the path to the log file should be obtained from outside as an argument of 'main'
//            if (isTarGz) {
//                lee = new LogEntryExtractor_FromArchive(this.TableCol, path2LogFile);
//            } else {
//                lee = new LogEntryExtractor(this.TableCol, path2LogFile, this.EventNameMethodMap);
//            }
//        } else { //path to log file is null: indicating the scanner will read log entries from System.in
//            lee = null;
//        }
//
//        System.out.println("Please get ready to profile the app, input a line with `enter` please.");
//        Scanner scan = new Scanner(System.in);
//        String res = scan.nextLine();
//        System.out.println("response is " + res);
//
//        long startT = System.currentTimeMillis();
//
//        lee.startReadingEventsByteByByte();
//
//        long totalT = System.currentTimeMillis() - startT;
//
//        System.out.println("It took my log analyzer " + totalT + " ms to " +
//                "count all the log entries in the log file in the format of " + (isTarGz ? "tar.gz" : "plain txt"));
//    }
//
//
//    public void monitor(Path path2LogFile, boolean isTarGz) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        this.monitor(path2LogFile, isTarGz, false);
//    }
//
//    public void monitor(Path path2LogFile) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        this.monitor(path2LogFile, false, false);
//    }
//
//    /**
//     * A method only for testing purpose.
//     *
//     * @param path2LogFile
//     */
//    public void monitor(Path path2LogFile, boolean isTarGz, boolean eagerEval) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        LogExtractor lee = null;
//        if (path2LogFile != null) {
//            //the path to the log file should be obtained from outside as an argument of 'main'
//            if (isTarGz) {
//                lee = new LogEntryExtractor_FromArchive(this.TableCol, path2LogFile, 8);
//            } else {
//                lee = new LogEntryExtractor(this.TableCol, path2LogFile, 6, this.EventNameMethodMap); //use lazy eval strategy.
//            }
//
//        } else { //path to log file is null: indicating the scanner will read log entries from System.in
////            lee = new LogEntryExtractor(this.TableCol);
//        }
//
//        long startT = System.currentTimeMillis();
//
//        lee.startReadingEventsByteByByte();
//
//        long totalT = System.currentTimeMillis() - startT;
//
//        System.out.println("It took my log analyzer " + totalT + " ms to " +
//                "count all the log entries in the log file in the format of " + (isTarGz ? "tar.gz" : "plain txt"));
//    }
//}
