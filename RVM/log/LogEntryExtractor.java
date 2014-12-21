package log;

import reg.RegHelper;

import java.io.*;
import java.util.*;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor implements Iterator<LogEntry> {

    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, Integer[]> TableCol;
    private Scanner scan;

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, File logFile) throws FileNotFoundException {
        this.TableCol = tableCol;
        FileInputStream fis=new FileInputStream(logFile.getPath());
        scan=new Scanner(new BufferedInputStream(fis), "ISO-8859-1");
    }

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
        this.TableCol = tableCol;
        InputStreamReader isReader = new InputStreamReader(System.in);
        scan=new Scanner(new BufferedReader(isReader));
    }

    /**
     * A log entry is a time-stamped database, where a database is a collection of tables.
     * Each table has a name and a number of tuples.
     *
     * @return
     */
    private LogEntry getLogEntry() {
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

            //if we found the event is not of our interest, then skip
            if (TableCol.get(eventName) == null){
//                System.out.println("No record for "+eventName);

                if (scan.useDelimiter(RegHelper.Delim4FindingTupleList).
                        hasNext(RegHelper.TupleListRegEx)){

                    scan.next(RegHelper.TupleListRegEx);
                    continue;

                } else{
                    System.err.println("Not well formed tuple list.");
                }
            } else{
//                System.out.println("Has record for "+eventName);
            }


            //list of tuples in the table with name 'eventName'
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

//                           System.out.println("No."+i+" field type of table "+
//                     eventName+" is "+RegHelper.getTypeName(TableCol.get(eventName)[i]));

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
                    System.err.println("Should have at least one tuple in the table " + eventName);
                    System.exit(0);
                }
            } while (scan.hasNext(RegHelper.TupleRegEx));

            //it is possible that multiple tables have the same name, then they can be combined to a single table
            //by merge their tuple sets.
            List<LogEntry.EventArg> prevRecords=tableMap.get(eventName);
            if (prevRecords == null) {
                tableMap.put(eventName, eventArgs);
            } else{
                prevRecords.addAll(eventArgs);
            }

//        System.out.println("event is "+eventName+" and time is "+time);
//        System.out.println(eventArgs.size()+" is the num of tuples");
        } while (scan.useDelimiter(RegHelper.Delim4FindingEvent).hasNext(RegHelper.EventName));

        return new LogEntry(time, tableMap);
    }

    @Override
    public boolean hasNext() {
        return scan.useDelimiter(RegHelper.Delim4FindingTimeStamp).hasNext();
    }

    public boolean hasNextLogEntry(){return this.hasNext();}

    @Override
    public LogEntry next() {
        return this.getLogEntry();
    }

    public LogEntry nextLogEntry() {return this.next();}
}
