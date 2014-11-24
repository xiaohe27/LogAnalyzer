package analysis;

import log.LogEntry;
import log.LogEntryExtractor;
import reg.RegHelper;
import rvm.PubRuntimeMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private static HashMap<String, Class[]> MethodArgListMap = new HashMap<>();

    private static Class MonitorClass;

    private static void init() {
        //the arg types can be inferred from the signature file
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE};
        TableCol.put(PUBLISH, argTy4Publish);
        TableCol.put(APPROVE, argTy4Approve);

        MonitorClass=PubRuntimeMonitor.class;

        for (String tableName: TableCol.keySet()){
            Integer[] types4CurTable= TableCol.get(tableName);

            Class[] argTyList4CurMeth= new Class[types4CurTable.length + 1];
            for (int i = 0; i < types4CurTable.length; i++) {
                switch (types4CurTable[i]){
                    case RegHelper.INT_TYPE:
                        argTyList4CurMeth[i]=Integer.class;
                        break;

                    case RegHelper.LONG_TYPE:
                        argTyList4CurMeth[i]=Long.class;
                        break;

                    case RegHelper.FLOAT_TYPE:
                        argTyList4CurMeth[i]=Float.class;
                        break;

                    case RegHelper.DOUBLE_TYPE:
                        argTyList4CurMeth[i]=Double.class;
                        break;

                    case RegHelper.STRING_TYPE:
                        argTyList4CurMeth[i]=String.class;
                        break;

                    default:
                    {
                        System.err.println("Unknown type: only support int, float, double, long and string at " +
                                "the moment!");
                        System.exit(0);
                    }
                }
            }

            //append the long type at the end which is the type for the timestamp.
            argTyList4CurMeth[argTyList4CurMeth.length-1]=long.class;

            MethodArgListMap.put(tableName, argTyList4CurMeth);
        }
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

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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


                        String methName= eventName+"Event";
                        Class[] paramTypes= MethodArgListMap.get(eventName);
                        Object[] args4MonitorMethod=new Object[fields.length+1];
                        System.arraycopy(fields, 0, args4MonitorMethod, 0, fields.length);
                        //the last arg is the timestamp.
                        args4MonitorMethod[args4MonitorMethod.length-1]=logEntry.getTime();

                        Method monitorMethod= MonitorClass.getDeclaredMethod(methName, paramTypes);
                        monitorMethod.invoke(null, args4MonitorMethod);

                    }
                }


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
