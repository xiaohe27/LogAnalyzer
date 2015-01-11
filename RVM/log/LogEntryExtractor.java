package log;

import reg.RegHelper;

import java.io.*;
import java.util.*;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor {

    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, Integer[]> TableCol;
    private Scanner scan;

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, File logFile) throws FileNotFoundException {
        this.TableCol = tableCol;
        FileInputStream fis = new FileInputStream(logFile.getPath());
        scan = new Scanner(new BufferedInputStream(fis), "ISO-8859-1");
    }

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
        this.TableCol = tableCol;
        InputStreamReader isReader = new InputStreamReader(System.in);
        scan = new Scanner(new BufferedReader(isReader));
    }

    /**
     * Use methods in nio.
     */

    private static String EventName;

    /**
     * Read file line by line.
     */
    public void startLineByLine() {
        long numOfLogEntries = 0;
//        scan.skip("\\s*");
        //Read the first line
        String line = null;
        if (scan.hasNextLine()) {
            line = scan.nextLine().trim();
            if (line.charAt(0) == '@')
                numOfLogEntries++;
            else {
                System.out.println("Log file not well formed, time stamp expected");
                System.exit(0);
            }
        } else {
            System.out.println("Empty file");
            System.exit(0);
        }
        List<LogEntry.Event> eventList = new ArrayList<>();
        String[] tsAndFirstEvent = line.split("\\s+");
//        if (tsAndFirstEvent[0].charAt(0) != '@'){
//            throw new Exception("Not well formed log entry");
//        }
        if (tsAndFirstEvent.length >= 2)
            EventName = tsAndFirstEvent[1];
        for (int i = 2; i < tsAndFirstEvent.length; i++) {
            eventList.add(this.getEvent(tsAndFirstEvent[i]));
        }

        //read the remaining lines
        String[] eventLine = null;
        try {
            while (true) {
                line = scan.skip("\\s*").nextLine();
                if (line.charAt(0) == '@') {
                    numOfLogEntries++;
                    //encounter new log entry, so we can construct the old one now
                    LogEntry logEntry = new LogEntry(tsAndFirstEvent[0], eventList);

//                    System.out.println("No." + (numOfLogEntries - 2) + " log entry is ");
//                    System.out.println(logEntry.toString());

                    //process the new log entry
                    eventList = new ArrayList<>();
                    tsAndFirstEvent = line.split("\\s+");

                    if (tsAndFirstEvent.length >= 2)
                        EventName = tsAndFirstEvent[1];

                    for (int i = 2; i < tsAndFirstEvent.length; i++) {
                        eventList.add(this.getEvent(tsAndFirstEvent[i]));
                    }
                } else {
                    eventLine = line.split("\\s+");
                    EventName = eventLine[0];

                    for (int i = 1; i < eventLine.length; i++) {
                        eventList.add(this.getEvent(eventLine[i]));
                    }
                }
            }
        } catch (NoSuchElementException e) {
//            System.out.println("End of file");
            System.out.println("There are " + numOfLogEntries + " log entries in the log file!~!");

            LogEntry logEntry = new LogEntry(tsAndFirstEvent[0], eventList);

//            System.out.println("No." + (numOfLogEntries - 1) + " event is ");
//            System.out.println(logEntry.toString());

        }

    }

    private LogEntry.Event getEvent(String tuple) {
        Object[] argsInTuple = new Object[TableCol.get(EventName).length];
        String[] fieldsData = tuple.substring(1, tuple.length() - 1).split(",");
        for (int i = 0; i < TableCol.get(EventName).length; i++) {
            String dataI = fieldsData[i];

//                           System.out.println("No."+i+" field type of table "+
//                     eventName+" is "+RegHelper.getTypeName(TableCol.get(eventName)[i]));

            switch (TableCol.get(EventName)[i]) {
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
        return new LogEntry.Event
                (EventName, argsInTuple);
    }


    /**
     * A log entry is a time-stamped database, where a database is a collection of tables.
     * Each table has a name and a number of tuples.
     *
     * @return
     */
    private LogEntry getLogEntry() {

        List<LogEntry.Event> eventList = new ArrayList<>();

        String time = scan.next();

//        System.out.println("Time is " + time);
        do {
            String eventName = scan
                    .next();

//            System.out.println("event is " + eventName);
            //if we found the event is not of our interest, then skip
            if (TableCol.get(eventName) == null) {
//                System.out.println("No record for " + eventName);
                while (scan.hasNext(RegHelper.TupleRegEx)) {
                    scan.skip(RegHelper.SkipEvent);
                }

                continue;
            } else {
//                System.out.println("Has record for "+eventName);
            }


            //list of tuples in the table with name 'eventName'
            do {
                String curTuple = scan.next();
                Object[] argsInTuple = new Object[TableCol.get(eventName).length];
                String[] fieldsData = curTuple.substring(1, curTuple.length() - 1).split(",");
                for (int i = 0; i < TableCol.get(eventName).length; i++) {
                    String dataI = fieldsData[i];

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
                eventList.add(new LogEntry.Event
                        (eventName, argsInTuple));

            } while (scan.hasNext(RegHelper.TupleRegEx));

//        System.out.println("event is "+eventName+" and time is "+time);
//        System.out.println(eventArgs.size()+" is the num of tuples");
        } while (scan.hasNext(RegHelper.EventName));

//        System.out.println("Log entry generated, " + eventList.size() + " events inside");
        return new LogEntry(time, eventList);
    }


    /**
     * Just test whether the efficiency can be improved.
     */
    public void start_regex() {
        long numOfLogEntries = 0;

        while (this.scan.hasNext()) {
            LogEntry logEntry = this.getLogEntry();

            numOfLogEntries++;
        }
        System.out.println("There are " + numOfLogEntries + " log entries!");
    }
}
