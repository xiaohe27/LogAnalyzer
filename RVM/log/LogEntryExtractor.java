package log;

import reg.RegHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

    private String TimeStamp; //we can add the @ symbol when it is ready to be printed

    private String EventName;

    private HashMap<String, Object[]> TableData;
    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, Integer[]> TableCol;

    private BufferedReader bufferedReader;

    private RegHelper regHelper;

    private String logFilePath;

// indirect optimal 8kb
//    private static final int DirectBufSizeOptimal4MyHP = 64 * 1024;
    private static final int BufSize = 64 * 1024;

    //some tokens
    static final byte newLine = (byte) '\n';
    static final byte space = (byte) ' ';
    static final byte tab = (byte) '\t';
    static final byte nl2 = (byte) '\r';
    static final byte at = (byte) '@';
    static final byte lpa = (byte) '(';
    static final byte rpa = (byte) ')';
    static final byte comma = (byte) ',';
    static final byte hash = (byte) '#';
    static final byte doubleQuote = (byte) '\"';
    static final byte underscore = (byte) '_';
    static final byte rightBracket = (byte) ']';
    static final byte exclamation = (byte) '!';
    static final byte dot = (byte) '.';


    static final int ts_mode = 0;
    static final int eventName_mode = 1;
    static final int tuple_mode = 2;
    static byte nxtExpected; // set this variable accordingly before reading a token


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
        this.logFilePath = logFile.toString();
        init();
    }

    private void init() throws IOException {
        this.TableData = new HashMap<>();
        for (String eventName : this.TableCol.keySet()) {
            Object[] fields = new Object[this.TableCol.get(eventName).length];
            this.TableData.put(eventName, fields);
        }
    }


    private boolean isWhiteSpace(byte b) {
        return b == newLine || b == space || b == tab || b == nl2;
    }
    private boolean isStringChar(byte b) {
       if (b > 44 && b < 59)
           return true;
        if (b > 64 && b < 92 )
            return true;

        if (b > 96 && b < 123)
            return true;

        if (b == underscore || b == rightBracket || b== exclamation)
            return true;
        else return false;
    }


    private ByteBuffer buffer;
    private FileChannel inChannel;

    /**
     * Get a string from byte buffer (not check thoroughly, assume white spaces).
     * @return
     * @throws IOException
     */
    private String getStringFromBuf_uncheck() throws IOException {
        StringBuffer sb=new StringBuffer();
        while (true) {
            while (buffer.hasRemaining()){
                byte b=buffer.get();
                if (isStringChar(b))
                    sb.append((char)b);

                else {
                     return sb.toString();
                }
            }

            this.buffer.clear();
            if (this.inChannel.read(this.buffer) == -1){
                throw new IOException("Unexpected end of file, unfinished string.");
            } else {
                this.buffer.flip();
            }
        }

        
    }
    /**
     * Get a float number from byte buffer (not check thoroughly, assume white spaces).
     * @return
     * @throws IOException
     */
    private double getFloatingNumFromBuf_uncheck() throws IOException {
        StringBuffer sb=new StringBuffer();
        while (true) {
            while (buffer.hasRemaining()){
                byte b=buffer.get();
                if (Character.isDigit(b) || b == dot)
                    sb.append((char)b);

                else {
                    return Double.parseDouble(sb.toString());
                }
            }

            this.buffer.clear();
            if (this.inChannel.read(this.buffer) == -1){
                throw new IOException("Unexpected end of file, unfinished string.");
            } else {
                this.buffer.flip();
            }
        }


    }

    /**
     * Get an int number from byte buffer (not check thoroughly, assume white spaces).
     * @return
     * @throws IOException
     */
    private int getIntFromBuf_uncheck() throws IOException {
        StringBuffer sb=new StringBuffer();
        while (true) {
            while (buffer.hasRemaining()){
                byte b=buffer.get();
//                System.out.println("byte is "+b);
//                System.out.println("digit char is "+ (char)b);
                if (Character.isDigit(b))
                    sb.append((char)b);

                else {
                    return Integer.parseInt(sb.toString());
                }
            }

            this.buffer.clear();
            if (this.inChannel.read(this.buffer) == -1){
                throw new IOException("Unexpected end of file, unfinished string.");
            } else {
                this.buffer.flip();
            }
        }


    }

    public void startReadingEventsByteByByte() throws IOException {
        long numOfLogEntries = 0;

        RandomAccessFile aFile = new RandomAccessFile
                (this.logFilePath, "r");
        inChannel = aFile.getChannel();

//        this.buffer = ByteBuffer.allocateDirect(BufSize); //direct or indirect?
        this.buffer = ByteBuffer.allocate(BufSize);
        while (inChannel.read(this.buffer) > 0) {
            this.buffer.flip();

            while (this.buffer.hasRemaining()) {
                byte b = this.buffer.get();
                if (isWhiteSpace(b))
                    continue;

                //change the order of different branches, cmp whether we can gain perf benefits by
                //considering the probabilities.
                if (b == at) {
                    TimeStamp = String.valueOf(this.getIntFromBuf_uncheck());
                    numOfLogEntries++;
                }

                else if (isStringChar(b)) {
                    EventName = (char)b + this.getStringFromBuf_uncheck();
                }

                else if (b == lpa) {
                     this.triggerEvent_byteVer();
                }

            }
            this.buffer.clear(); // do something with the data and clear/compact it.
        }
        inChannel.close();
        aFile.close();

        System.out.println("There are " +
                numOfLogEntries + " log entries in the log file!!!");
//        System.out.println("There are " + numOfLines + " lines");
//        System.out.println("There are " + numOfBytes + " lines");
    }

    private void triggerEvent_byteVer() throws IOException {
        Integer[] typesInTuple = TableCol.get(EventName);
        Object[] argsInTuple = TableData.get(EventName);
        for (int i = 0; i < typesInTuple.length; i++) {
            String dataI = this.getStringFromBuf_uncheck();

//         System.out.println("No."+i+" field type of table "+
//  eventName+" is "+RegHelper.getTypeName(TableCol.get(eventName)[i]));

            switch (typesInTuple[i]) {
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
//        this.printEvent();
    }


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

//                    for (int i = 2; i < tsAndFirstEvent.length; i++) {
//                        this.triggerEvent(tsAndFirstEvent[i]);
//                    }

                } else {
                    String eventLine[] = line.split("\\s+");
                    EventName = eventLine[0];

//                    for (int i = 1; i < eventLine.length; i++) {
//                        this.triggerEvent(eventLine[i]);
//                    }
                }

                //use matcher to find args in the tuple and trigger event...
                this.triggerEventsViaRegEx(line);
            }
        } catch (Exception e) {
//            System.out.println("End of file");
            System.out.println("There are " +
                    numOfLogEntries + " log entries in the log file!~!");

        }
    }


    private void triggerEventsViaRegEx(String line) {

        Matcher matcher = this.regHelper.eventTupleRegEx.get(EventName).matcher(line);

        Integer[] argTypes = TableCol.get(EventName);
        Object[] argsInTuple = this.TableData.get(this.EventName);

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
            //trigger event using the info gathered so far
//            this.printEvent();
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

    private void printEvent() {
        System.out.print("\n" + this.TimeStamp + " " + this.EventName + "(");
        Object[] data = this.TableData.get(this.EventName);
        for (int i = 0; i < data.length - 1; i++) {
            System.out.print(data[i] + ",");
        }

        if (data.length > 0) {
            System.out.print(data[data.length - 1]);
        }

        System.out.print(")\n");
    }

}
