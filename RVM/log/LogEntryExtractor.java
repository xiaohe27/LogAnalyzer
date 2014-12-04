package log;

import reg.RegHelper;

import java.io.File;
import java.io.FileNotFoundException;
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
        scan=new Scanner(logFile);
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

                           System.out.println("Event name is "+TableCol.get(eventName)[i]);

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

        System.out.println("event is "+eventName+" and time is "+time);
        System.out.println(eventArgs.size()+" is the num of args");
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
