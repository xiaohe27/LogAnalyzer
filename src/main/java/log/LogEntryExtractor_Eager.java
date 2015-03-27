package log;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor_Eager implements LogExtractor {

    //some tokens
    static final byte newLine = (byte) '\n';
    static final byte space = (byte) ' ';
    static final byte tab = (byte) '\t';
    static final byte nl2 = (byte) '\r';
    static final byte space0B = 0x0B;
    static final byte spaceF = (byte) '\f';
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
    private String TimeStamp; //we can add the @ symbol when it is ready to be printed
    private String EventName;
    private HashMap<String, Object[]> TableData;
    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, int[]> TableCol;

    private String logFilePath;
    //    indirect optimal 8kb
//    private static final int DirectBufSizeOptimal4MyHP = 64 * 1024;
    private int BufSize;
    //    private ByteBuffer byteArr;
//    private FileChannel inChannel;
    private byte[] byteArr;
    private MappedByteBuffer mbb;
    private long fileSize;
    private long posInFile; //pos in the file
    private int posInArr;


//    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
//        this.TableCol = tableCol;
//        InputStreamReader isReader = new InputStreamReader(System.in);
//        scan = new Scanner(new BufferedReader(isReader));
//    }

    /**
     * Create an obj for log entry extractor.
     *
     * @param tableCol
     * @param logFile
     * @param multipleOf1K Multiple of 1024.
     * @throws IOException
     */
    public LogEntryExtractor_Eager(HashMap<String, int[]> tableCol, Path logFile, int multipleOf1K)
            throws IOException {
        this.TableCol = tableCol;
        this.logFilePath = logFile.toString();

        this.BufSize = multipleOf1K * 1024;
        this.byteArr = new byte[this.BufSize];
        init();
    }

    public LogEntryExtractor_Eager(HashMap<String, int[]> tableCol, Path logFile) throws IOException {
        this(tableCol, logFile, 4);
    }

    private void init() throws IOException {
        this.TableData = new HashMap<>();
        for (String eventName : this.TableCol.keySet()) {
            Object[] fields = new Object[this.TableCol.get(eventName).length];
            this.TableData.put(eventName, fields);
        }
    }


    private boolean isWhiteSpace(byte b) {
        return b == newLine || b == space || b == tab || b == nl2 || b == space0B || b == spaceF;
    }

    private boolean isStringChar(byte b) {
        if (b > 44 && b < 59)
            return true;
        if (b > 64 && b < 92)
            return true;

        if (b > 96 && b < 123)
            return true;

        if (b == underscore || b == rightBracket || b == exclamation)
            return true;
        else return false;
    }


    private String getEventName() throws IOException {
        StringBuffer sb = new StringBuffer();
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b))
                    sb.append((char) b);

                else {
                    if (b == lpa) {
                        this.posInArr--;
                    }
                    return sb.toString();
                }
            }

            this.posInArr = 0;
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing an event name");
            }
        }

    }


    private String getStringFromBuf(byte delim) throws IOException {
        StringBuffer sb = new StringBuffer();
        String returnedStr = null;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    try {
                        sb.append((char) b);
                    } catch (Exception e) {
                        throw new IOException("A string field should not contain white spaces inside");
                    }
                } else {
                    if (!isWhiteSpace(b) && b != delim) {
                        throw new IOException("UnExpected char\'" + (char) b + "\'");
                    }

                    if (sb != null) {
                        returnedStr = sb.toString();
                        sb = null;
                    }
                    if (b != delim) {
                        continue;
                    }
                    return returnedStr;
                }
            }

            this.posInArr = 0;
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing a string");
            }
        }
    }

    private void rmWhiteSpace() {
        while (this.posInArr < this.BufSize) {
            byte b = byteArr[this.posInArr++];
            if (isWhiteSpace(b))
                continue;
            else {
                this.posInArr--;
                break;
            }
        }
    }

    private long getTSFromBuf() throws IOException {
        StringBuffer sb = new StringBuffer();

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58 || b == dot) {

                    sb.append((char) b);

                } else {
                    return Math.round(Double.parseDouble(sb.toString()));
                }
            }

            this.posInArr = 0;
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing a time stamp");
            }
        }
    }

    private double getFloatingNumFromBuf(byte delim) throws IOException {
        StringBuffer sb = new StringBuffer();
        String returnedStr = null;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58 || b == dot) {
                    try {
                        sb.append((char) b);
                    } catch (Exception e) {
                        throw new IOException("A floating number field should not contain white spaces inside");
                    }
                } else {
                    if (!isWhiteSpace(b) && b != delim) {
                        throw new IOException("UnExpected char\'" + (char) b + "\'");
                    }

                    if (sb != null) {
                        returnedStr = sb.toString();
                        sb = null;
                    }
                    if (b != delim) {
                        continue;
                    }
                    return Double.parseDouble(returnedStr);
                }
            }

            this.posInArr = 0;
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing a floating number");
            }
        }
    }

    /**
     * Get an int number from byte byteArr (not check thoroughly, assume white spaces).
     *
     * @return
     * @throws IOException
     */
    private int getIntFromBuf(byte delim) throws IOException {
        StringBuffer sb = new StringBuffer();
        String returnedStr = null;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58) {
                    try {
                        sb.append((char) b);
                    } catch (Exception e) {
                        throw new IOException("An int number field should not contain white spaces inside");
                    }
                } else {
                    if (!isWhiteSpace(b) && b != delim) {
                        throw new IOException("UnExpected char\'" + (char) b + "\'");
                    }

                    if (sb != null) {
                        returnedStr = sb.toString();
                        sb = null;
                    }
                    if (b != delim) {
                        continue;
                    }
                    return Integer.parseInt(returnedStr);
                }
            }

            this.posInArr = 0;
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing an integer");
            }
        }


    }

    public void startReadingEventsByteByByte() throws IOException {
        long numOfLogEntries = 0;

        RandomAccessFile aFile = new RandomAccessFile
                (this.logFilePath, "r");

        FileChannel inChannel = aFile.getChannel();
        this.fileSize = inChannel.size();

//        System.out.println("There are totally "+this.fileSize+" bytes in the file");

        this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, this.fileSize);

//        System.out.println("Log is loaded? "+this.mbb.isLoaded());
//        System.out.println("MBB is direct? "+this.mbb.isDirect());
        this.mbb.load();
//        System.out.println("Again, Log is loaded? "+this.mbb.isLoaded());

        this.byteArr = new byte[this.BufSize];
        this.posInFile = 0;
        this.mbb.position(0);
        while (this.posInFile < this.fileSize) {
            try {
                this.mbb.get(this.byteArr);
                this.posInFile += this.BufSize;
            } catch (BufferUnderflowException e) {
                int remaining = this.mbb.remaining();
                if (remaining > 0) {
                    this.mbb.get(this.byteArr, 0, remaining);
                    this.posInFile = this.fileSize;
                    this.BufSize = remaining;
                } else
                    throw new IOException("Unexpected end of file while parsing, cur pos in file is " +
                            this.posInFile + ", and the file size is " + this.fileSize);
            }

            this.posInArr = 0;
            while (this.posInArr < this.BufSize) {
                byte b = this.byteArr[this.posInArr++];
                if (isWhiteSpace(b))
                    continue;

                //change the order of different branches, cmp whether we can gain perf benefits by
                //considering the probabilities.
                if (b == lpa) {
                    this.rmWhiteSpace();
                    this.triggerEvent();
                } else if (isStringChar(b)) {
                    EventName = (char) b + this.getEventName();
                } else if (b == at) {
                    this.rmWhiteSpace();
                    TimeStamp = String.valueOf(this.getTSFromBuf());
                    numOfLogEntries++;
                } else if (b == 0) {
                    break;
                } else {
                    throw new IOException("Unexpected char'" + (char) b + "'");
                }
            }
        }
        inChannel.close();

        aFile.close();

//        System.out.println("There are " +
//                numOfLogEntries + " log entries in the log file!!!");

    }


    private void triggerEvent() throws IOException {
        int[] typesInTuple = TableCol.get(EventName);
        Object[] argsInTuple = TableData.get(EventName);
        for (int i = 0; i < typesInTuple.length - 1; i++) {

            switch (typesInTuple[i]) {
                case RegHelper.INT_TYPE:
                    argsInTuple[i] = this.getIntFromBuf(comma);
                    break;

                case RegHelper.FLOAT_TYPE:
                    argsInTuple[i] = this.getFloatingNumFromBuf(comma);
                    break;

                case RegHelper.STRING_TYPE:
                    argsInTuple[i] = this.getStringFromBuf(comma);
                    break;
            }
            this.rmWhiteSpace();
        }

        if (typesInTuple.length > 0) {
            //the last field should be followed by a right parenthesis
            int lastIndex = typesInTuple.length - 1;
            switch (typesInTuple[lastIndex]) {
                case RegHelper.INT_TYPE:
                    argsInTuple[lastIndex] = this.getIntFromBuf(rpa);
                    break;

                case RegHelper.FLOAT_TYPE:
                    argsInTuple[lastIndex] = this.getFloatingNumFromBuf(rpa);
                    break;

                case RegHelper.STRING_TYPE:
                    argsInTuple[lastIndex] = this.getStringFromBuf(rpa);
                    break;
            }
        }
    }

    private void printEvent() {
        System.out.print("\n@" + this.TimeStamp + " " + this.EventName + "(");
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