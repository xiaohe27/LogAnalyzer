package analysis;

import log.LogEntry;
import log.LogEntryExtractor;
import reg.RegHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        this.monitorClass=Class.forName(libName);
    }
    private void init() {

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

    public void monitor(Path path2LogFile) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        try {
            LogEntryExtractor lee=null;

            if (path2LogFile != null) {
                //the path to the log file should be obtained from outside as an argument of 'main'
                File logFile = path2LogFile.toFile();

                lee = new LogEntryExtractor(this.TableCol, logFile);

            } else{ //path to log file is null: indicating the scanner will read log entries from System.in
                lee = new LogEntryExtractor(this.TableCol);
            }

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

                    if (!eventName.equals("insert"))
                        continue;


                    List<LogEntry.EventArg> tuples = logEntry.getTableMap().get(eventName);
                    for (int i = 0; i < tuples.size(); i++) {
                        LogEntry.EventArg curTuple = tuples.get(i);

                        String userName= (String) (curTuple.getFields()[0]);
                        if (!userName.equals("notARealUserInTheDB"))
                            curTuple.print();

//                        Object[] fields = curTuple.getFields();

//                        String methName= eventName+"Event";
//                        Class[] paramTypes= MethodArgListMap.get(eventName);
//                        Object[] args4MonitorMethod=new Object[fields.length+1];
//                        System.arraycopy(fields, 0, args4MonitorMethod, 0, fields.length);
//                        //the last arg is the timestamp.
//                        args4MonitorMethod[args4MonitorMethod.length-1]=logEntry.getTime();

//                        Method monitorMethod= this.monitorClass.getDeclaredMethod(methName, paramTypes);
//                        monitorMethod.invoke(null, args4MonitorMethod);

                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
