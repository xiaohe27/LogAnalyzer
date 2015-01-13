package log;

import reg.RegHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor {

    /**
     * Use methods in nio.
     */

    private static String EventName;
    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, Integer[]> TableCol;
    //    private List<String> lines;
//    private Scanner scan;
    private BufferedReader bufferedReader;

    private RegHelper regHelper;

//    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
//        this.TableCol = tableCol;
//        InputStreamReader isReader = new InputStreamReader(System.in);
//        scan = new Scanner(new BufferedReader(isReader));
//    }

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, Path logFile) throws IOException {
        this.TableCol = tableCol;
//        FileInputStream fis = new FileInputStream(logFile.getPath());
        this.bufferedReader = Files.newBufferedReader(logFile, Charset.forName("ISO-8859-1"));
        this.regHelper = new RegHelper(tableCol);
//        this.lines= Files.readAllLines(logFile, Charset.forName("ISO-8859-1"));
    }

    /**
     * Read file line by line.
     */
    public void startLineByLine() throws IOException {
        long numOfLogEntries = 0;
        int lineNum = 0;
//        scan.skip("\\s*");
        //Read the first line
        String line = null;
        if (this.bufferedReader.ready()) {
            line = this.bufferedReader.readLine();
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

        EventName = tsAndFirstEvent[1];
//        for (int i = 2; i < tsAndFirstEvent.length; i++) {
//            eventList.add(this.getEvent(tsAndFirstEvent[i]));
//        }
        eventList.add(this.getOneEvent(line));
//        eventList.addAll(this.getEventList(line));

        //read the remaining lines
        String[] eventLine = null;
        try {
            while (true) {
//                try {
//                    while((line = this.bufferedReader.readLine()).matches("\\s*"))
//                    {}
//                } catch (Exception excep2){
//                    throw new NoSuchElementException();
//                }
                line = this.bufferedReader.readLine();
                if (line.charAt(0) == '@') {
                    numOfLogEntries++;
                    //encounter new log entry, so we can construct the old one now
                    LogEntry logEntry = new LogEntry(tsAndFirstEvent[0], eventList);

//                    System.out.println("No." + (numOfLogEntries - 2) + " log entry is ");
//                    System.out.println(logEntry.toString());

                    //process the new log entry
                    eventList = new ArrayList<>();
                    tsAndFirstEvent = line.split("\\s+");

                    EventName = tsAndFirstEvent[1];

//                    for (int i = 2; i < tsAndFirstEvent.length; i++) {
//                        eventList.add(this.getEvent(tsAndFirstEvent[i]));
//                    }

                } else {
                    eventLine = line.split("\\s+");
                    EventName = eventLine[0];

//                    for (int i = 1; i < eventLine.length; i++) {
//                        eventList.add(this.getEvent(eventLine[i]));
//                    }
                }
                eventList.add(this.getOneEvent(line));
//                eventList.addAll(this.getEventList(line));
            }
        } catch (Exception e) {
//            System.out.println("End of file");
            System.out.println("There are " + numOfLogEntries + " log entries in the log file!~!");

            LogEntry logEntry = new LogEntry(tsAndFirstEvent[0], eventList);

//            System.out.println("No." + (numOfLogEntries - 1) + " event is ");
//            System.out.println(logEntry.toString());

        }
    }

    private List<LogEntry.Event> getEventList(String tuples) {
        List<LogEntry.Event> eventList = new ArrayList<>();
//        System.out.println("Event is <"+EventName+">");
//        this.regHelper.showEventTupleRegEx();

        Matcher matcher = this.regHelper.eventTupleRegEx.get(EventName).matcher(tuples);

//        System.out.println("The event " + EventName + "'s tuple's pattern is " +
//                this.regHelper.eventTupleRegEx.get(EventName));

        Integer[] argTypes = TableCol.get(EventName);
        Object[] argsInTuple = new Object[argTypes.length];

        while (matcher.find()) {     //continuously find the tuples of the table
//            System.out.println("We found the tuple " + matcher.group(0));

            for (int i = 0; i < argsInTuple.length; i++) {
                String dataI = matcher.group(i + 1);

                switch (argTypes[i]) {
                    case RegHelper.INT_TYPE:
                        argsInTuple[i] = Integer.parseInt(dataI);
                        break;


                    case RegHelper.FLOAT_TYPE:
                        argsInTuple[i] = Float.parseFloat(dataI);
                        break;


                    case RegHelper.STRING_TYPE:
                        argsInTuple[i] = dataI;
                        break;
                }
            }

            eventList.add(new LogEntry.Event
                    (EventName, argsInTuple));
        }
        return eventList;
    }

    private LogEntry.Event getOneEvent(String tuple) {

        Matcher matcher = this.regHelper.eventTupleRegEx.get(EventName).matcher(tuple);

        Integer[] argTypes = TableCol.get(EventName);
        Object[] argsInTuple = new Object[argTypes.length];

        if (matcher.find()) {     //continuously find the tuples of the table
//            System.out.println("We found the tuple " + matcher.group(0));

            for (int i = 0; i < argsInTuple.length; i++) {
                String dataI = matcher.group(i + 1);

                switch (argTypes[i]) {
                    case RegHelper.INT_TYPE:
                        argsInTuple[i] = Integer.parseInt(dataI);
                        break;


                    case RegHelper.FLOAT_TYPE:
                        argsInTuple[i] = Float.parseFloat(dataI);
                        break;


                    case RegHelper.STRING_TYPE:
                        argsInTuple[i] = dataI;
                        break;
                }
            }
        }
        return new LogEntry.Event(EventName, argsInTuple);
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

                case RegHelper.FLOAT_TYPE:
                    argsInTuple[i] = Float.parseFloat(dataI);
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
//    private LogEntry getLogEntry() {
//
//        List<LogEntry.Event> eventList = new ArrayList<>();
//
//        String time = scan.next();
//
////        System.out.println("Time is " + time);
//        do {
//            String eventName = scan
//                    .next();
//
////            System.out.println("event is " + eventName);
//            //if we found the event is not of our interest, then skip
//            if (TableCol.get(eventName) == null) {
////                System.out.println("No record for " + eventName);
//                while (scan.hasNext(RegHelper.TupleRegEx)) {
//                    scan.skip(RegHelper.SkipEvent);
//                }
//
//                continue;
//            } else {
////                System.out.println("Has record for "+eventName);
//            }
//
//
//            //list of tuples in the table with name 'eventName'
//            do {
//                String curTuple = scan.next();
//                Object[] argsInTuple = new Object[TableCol.get(eventName).length];
//                String[] fieldsData = curTuple.substring(1, curTuple.length() - 1).split(",");
//                for (int i = 0; i < TableCol.get(eventName).length; i++) {
//                    String dataI = fieldsData[i];
//
////                           System.out.println("No."+i+" field type of table "+
////                     eventName+" is "+RegHelper.getTypeName(TableCol.get(eventName)[i]));
//
//                    switch (TableCol.get(eventName)[i]) {
//                        case RegHelper.INT_TYPE:
//                            argsInTuple[i] = Integer.parseInt(dataI);
//                            break;
//
//                        case RegHelper.LONG_TYPE:
//                            argsInTuple[i] = Long.parseLong(dataI);
//                            break;
//
//                        case RegHelper.FLOAT_TYPE:
//                            argsInTuple[i] = Float.parseFloat(dataI);
//                            break;
//
//                        case RegHelper.DOUBLE_TYPE:
//                            argsInTuple[i] = Double.parseDouble(dataI);
//                            break;
//
//                        case RegHelper.STRING_TYPE:
//                            argsInTuple[i] = dataI;
//                            break;
//                    }
//                }
//                eventList.add(new LogEntry.Event
//                        (eventName, argsInTuple));
//
//            } while (scan.hasNext(RegHelper.TupleRegEx));
//
////        System.out.println("event is "+eventName+" and time is "+time);
////        System.out.println(eventArgs.size()+" is the num of tuples");
//        } while (scan.hasNext(RegHelper.EventName));
//
////        System.out.println("Log entry generated, " + eventList.size() + " events inside");
//        return new LogEntry(time, eventList);
//    }


    /**
     * Just test whether the efficiency can be improved.
     */
//    public void start_regex() {
//        long numOfLogEntries = 0;
//
//        while (this.scan.hasNext()) {
//            LogEntry logEntry = this.getLogEntry();
//
//            numOfLogEntries++;
//        }
//        System.out.println("There are " + numOfLogEntries + " log entries!");
//    }
}
