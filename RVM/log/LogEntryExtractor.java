package log;

import reg.RegHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor {

    /**
     * Use methods in nio.
     */

    private String TimeStamp;

    private String EventName;

    private HashMap<String, Object[]> TableData;
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

    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, Path logFile)
    throws IOException {
        this.TableCol = tableCol;
        this.bufferedReader = Files.newBufferedReader(logFile, Charset.forName("ISO-8859-1"));
        this.regHelper = new RegHelper(tableCol);
        init();
    }

    private void init() {
        this.TableData = new HashMap<>();
        for (String eventName : this.TableCol.keySet()) {
            Object[] fields = new Object[this.TableCol.get(eventName).length];
            this.TableData.put(eventName, fields);
        }
    }

    /**
     * Read file line by line.
     */
    public void startLineByLine() throws IOException {
        long numOfLogEntries = 0;

        String line = null;

        try {
            while (true) {
                line = this.bufferedReader.readLine();
                if (line.charAt(0) == '@') {
                    numOfLogEntries++;

                    String[] tsAndFirstEvent = line.split("\\s+");
                    TimeStamp = tsAndFirstEvent[0];
                    EventName = tsAndFirstEvent[1];

                    for (int i = 2; i < tsAndFirstEvent.length; i++) {
                        this.triggerEvent(tsAndFirstEvent[i]);
                    }

                } else {
                    String eventLine[] = line.split("\\s+");
                    EventName = eventLine[0];

                    for (int i = 1; i < eventLine.length; i++) {
                        this.triggerEvent(eventLine[i]);
                    }
                }

                //use matcher to find args in the tuple and trigger event...
//                this.getOneEventViaRegEx(line);
            }
        } catch (Exception e) {
//            System.out.println("End of file");
            System.out.println("There are " +
                    numOfLogEntries + " log entries in the log file!~!");

        }
    }


    private void getOneEventViaRegEx(String tuple) {

        Matcher matcher = this.regHelper.eventTupleRegEx.get(EventName).matcher(tuple);

        Integer[] argTypes = TableCol.get(EventName);
        Object[] argsInTuple = this.TableData.get(this.EventName);

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

    }


    private void triggerEvent(String tuple) {
        Object[] argsInTuple = this.TableData.get(this.EventName);
        String[] fieldsData = tuple.substring(1, tuple.length() - 1).split(",");
        for (int i = 0; i < TableCol.get(EventName).length; i++) {
            String dataI = fieldsData[i];

//         System.out.println("No."+i+" field type of table "+
//  eventName+" is "+RegHelper.getTypeName(TableCol.get(eventName)[i]));

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

        //trigger event using the info gathered so far
//        this.printEvent();

    }

    private void printEvent(){
        System.out.print("\n" + this.TimeStamp + " " + this.EventName + "(");
        Object[] data=this.TableData.get(this.EventName);
        for (int i = 0; i < data.length - 1; i++) {
            System.out.print(data[i]+",");
        }

        if (data.length>0){
            System.out.print(data[data.length-1]);
        }

        System.out.print(")\n");
    }

}
