package log;

import formula.FormulaExtractor;
import reg.RegHelper;
import sig.SigExtractor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Serves as lexer and parser for log file.
 */
public class LogEntryExtractor implements LogExtractor {
    /**
     * Use a byte to denote different tokens in the log.
     * -1: init state, or NULL.
     * 0: time stamp;
     * 1: event name;
     * 2: event args;
     * We need to ensure a seq of tokens is accepted only if it is a word in the lang (derivable from the FSM).
     */
    public static final byte NULL_TOKEN = -1;
    public static final byte TS_TOKEN = 0;
    public static final byte EventName_TOKEN = 1;
    public static final byte EventArgs_TOKEN = 2;
    static final int Times = 2;
    static final int OneReadSize = (0xFFFFFFF + 1) * Times; //256MB as the unit size
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
    static final byte minus = (byte) '-';
    private final Charset asciiCharSet = Charset.forName("ISO-8859-1");
    private long TimeStamp; //we can add the @ symbol when it is ready to be printed
    private String EventName;
    private byte prevToken = NULL_TOKEN;


    /**
     * Given a table name, return the list of types that represent the types for each column (table schema).
     */
    private HashMap<String, Integer[]> TableCol;
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
    private byte[] oldByteArr;
    //if the ending index is less than the starting one, then the starting index comes from the old byte array
//    private int TimeStampStartIndex;  //starting: [
//    private int TimeStampLen;    //ending )
    private int EventNameStartIndex;

    private int paramStartIndex;

    private Integer[] typesInTuple;
    private boolean isAMonitoredEvent;

    //    public LogEntryExtractor(HashMap<String, Integer[]> tableCol) {
//        this.TableCol = tableCol;
//        InputStreamReader isReader = new InputStreamReader(System.in);
//        scan = new Scanner(new BufferedReader(isReader));
//    }
    private long numOfReads;
    private long lastReadSize;
    private long curNumOfReads;
    private FileChannel inChannel;

    /**
     * Create an obj for log entry extractor.
     *
     * @param tableCol
     * @param logFile
     * @param powOf2TimesKB Multiple of 1024.
     * @throws IOException
     */
    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, Path logFile, int powOf2TimesKB)
            throws IOException {
        this.TableCol = tableCol;
        this.logFilePath = logFile.toString();
        this.BufSize = (int) (Math.pow(2, powOf2TimesKB)) * 1024;
        this.byteArr = new byte[this.BufSize];
        this.oldByteArr = new byte[this.BufSize];
    }


    public LogEntryExtractor(HashMap<String, Integer[]> tableCol, Path logFile) throws IOException {
        this(tableCol, logFile, 5);
    }

    private boolean isWhiteSpace(byte b) {
        return b == newLine || b == space || b == tab || b == nl2 || b == space0B || b == spaceF;
    }

    private boolean isStringChar(byte b) {
        if (b > 44 && b < 59)
            return true;
        if (b > 64 && b < 92)
            return true;

        return b > 96 && b < 123 || b == underscore || b == rightBracket || b == exclamation;

    }

    private String getEventName() throws IOException {
        int len = 1;

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    len++;
                } else {
                    if (b == lpa) {
                        this.posInArr--;
                    }

                    return this.getStringFromBytes(this.EventNameStartIndex, len);
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing an event name");
                        }
                    }
                }
            }
        }

    }

    private String getStringFromBuf(byte delim) throws IOException {
        int len = 0;
        String output;
        while (true) {
            if (this.posInArr < this.BufSize && len == 0 && byteArr[this.posInArr] == doubleQuote) {
                this.posInArr++;
                return this.getQuotedStringFromBuf(delim);
            }

            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    len++;
                } else {
                    output = this.getStringFromBytes(this.paramStartIndex, len);
                    if (b == delim)
                        return output;

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return output;
                    } else {
                        throw new IOException("Unexpected delimiter " + (char) this.byteArr[this.posInArr - 1]);
                    }
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr); //refill the byte array if mbb if not exhausted
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);   //mbb's remaining bytes does not fill the whole byte array
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) { //mbb is exhausted but more contents available in the file.
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing a string");
                        }
                    }
                }
            }
        }
    }

    /**
     * Only invoked by getStringFromBuf, and used to extract tuple's field of type String.
     *
     * @param delim
     * @return
     * @throws IOException
     */
    private String getQuotedStringFromBuf(byte delim) throws IOException {
        int len = 0;
        String output;

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b != doubleQuote) {
                    len++;
                } else {
                    output = this.getStringFromBytes(this.paramStartIndex, len);

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return output;
                    } else {
                        throw new IOException("Unexpected delimiter " + (char) this.byteArr[this.posInArr - 1]);
                    }

                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing a string");
                        }
                    }
                }
            }
        }
    }

    private String getQuotedStringFromBuf(int startIndex) throws IOException {
        int len = 0;

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b != doubleQuote) {
                    len++;
                } else {
                    return this.getStringFromBytes(startIndex, len);
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing a string");
                        }
                    }
                }
            }
        }
    }

    private void rmWhiteSpace() throws IOException {
        out:
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (!isWhiteSpace(b)) {
                    this.posInArr--;
                    break out;
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();


                        } else {
                            throw new IOException("Unexpected end of file while removing the white spaces");
                        }
                    }
                }
            }
        }
    }

    private void skipComment() throws IOException {
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b == newLine || b == nl2) {
                    return;
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();


                        } else {
                            throw new IOException("Unexpected end of file while removing the white spaces");
                        }
                    }
                }
            }
        }
    }

    private void getTSFromBuf() throws IOException {
        int TimeStampStartIndex = this.posInArr;

        int TimeStampLen = 0;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58 || b == dot) {
                    TimeStampLen++;
                } else {
                    String out;

                    if (this.posInArr > TimeStampStartIndex) {
                        out = new String(this.byteArr, TimeStampStartIndex,
                                TimeStampLen, this.asciiCharSet);
                    } else if (this.posInArr == this.EventNameStartIndex) {
                        throw new IOException("Empty Time Stamp!");
                    } else {
                        int sizInOldBuf = this.BufSize - TimeStampStartIndex;
                        out = new String(this.oldByteArr, TimeStampStartIndex,
                                sizInOldBuf, this.asciiCharSet) +
                                new String(this.byteArr, 0, TimeStampLen - sizInOldBuf, this.asciiCharSet);
                    }

                    TimeStamp = Math.round(Double.parseDouble(out));

                    return;
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing a time stamp");
                        }
                    }
                }
            }
        }
    }

    //b > 47 && b < 58 then b is a digit char
    private double getFloatingNumFromBuf(byte delim) throws IOException {
        int len = 0;
        String output;
        if (this.byteArr[this.posInArr] == minus) {
            this.posInArr++;
            len++;
        }

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58 || b == dot) {
                    len++;
                } else {
                    output = this.getStringFromBytes(this.paramStartIndex, len);
                    if (b == delim)
                        return Double.parseDouble(output);

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return Double.parseDouble(output);
                    } else {
                        throw new IOException("Unexpected delimiter " + (char) this.byteArr[this.posInArr - 1]);
                    }
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();


                        } else {
                            throw new IOException("Unexpected end of file while parsing a floating number");
                        }
                    }
                }
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
        int len = 0;
        String output;
        if (this.byteArr[this.posInArr] == minus) {
            this.posInArr++;
            len++;
        }

        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58) {
                    len++;
                } else {
                    output = this.getStringFromBytes(this.paramStartIndex, len);

                    if (b == delim)
                        return Integer.parseInt(output);

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return Integer.parseInt(output);
                    } else {
                        throw new IOException("Unexpected delimiter " + (char) this.byteArr[this.posInArr - 1]);
                    }
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) {
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing an integer");
                        }
                    }
                }
            }
        }


    }

    private void checkAround(int start, int len) {
        System.out.println(new String(this.byteArr, start, len, this.asciiCharSet));
    }

    private void looseCheckingEventArgs() throws IOException {
        boolean isCurFieldEmpty = true;
        boolean canSeeComma = false;
        boolean canAppend = true;


        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    canSeeComma = false;
                    if (isCurFieldEmpty) {
                        isCurFieldEmpty = false;
                        canAppend = true;
                    } else {
                        if (canAppend) {

                        } else {
                            throw new IOException("Syntax error found in the event " + this.EventName + "'s args");
                        }
                    }

                } else if (b == comma) {
                    if (isCurFieldEmpty) {
                        throw new IOException("Should have something before comma in the event " + this.EventName + "'s args");
                    }

                    isCurFieldEmpty = true;
                    canSeeComma = true;
                    canAppend = true;
                } else if (isWhiteSpace(b)) {
                    canAppend = false;
                } else if (b == rpa) {

                    if (canSeeComma)
                        throw new IOException("Empty between last comma and right bracket in event " + EventName + "'s args");

                    else
                        return;
                } else if (b == doubleQuote) {
                    if (isCurFieldEmpty) {
                        this.getQuotedStringFromBuf(this.posInArr);
                        isCurFieldEmpty = false;
                        canAppend = false;
                    } else {
                        throw new IOException("Syntax error found in the event " + this.EventName + "'s args");
                    }


                } else {
                    throw new IOException("Unknown char " + (char) b);
                }
            }

            byte[] tmp = this.oldByteArr;
            this.oldByteArr = this.byteArr;
            this.byteArr = tmp;

            this.posInFile += this.BufSize;
            this.posInArr = 0;

            while (true) { //refill the mbb and byte array if necessary
                try {
                    this.mbb.get(this.byteArr);
                    break;
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                        break;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);

                            System.gc();

                        } else {
                            throw new IOException("Unexpected end of file while parsing an integer");
                        }
                    }
                }
            }
        }
    }

    public void startReadingEventsByteByByte() throws IOException {
        long numOfLogEntries = 0;
        this.posInFile = 0;   //pos in file is the absolute pos in file where the current byte array starts

        RandomAccessFile aFile = new RandomAccessFile
                (this.logFilePath, "r");

        this.inChannel = aFile.getChannel();
        this.fileSize = inChannel.size();

//        System.out.println("There are totally "+this.fileSize+" bytes in the file");
        this.numOfReads = this.fileSize / OneReadSize;
        this.lastReadSize = this.fileSize % OneReadSize;

        while (this.curNumOfReads <= this.numOfReads) {
            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
            this.curNumOfReads++;
//        System.out.println("Log is loaded? "+this.mbb.isLoaded());
//        System.out.println("MBB is direct? "+this.mbb.isDirect());
            this.mbb.load();
//        System.out.println("Again, Log is loaded? "+this.mbb.isLoaded());

            this.mbb.position(0);
            while (this.posInFile + this.posInArr < this.fileSize) {
                try {
                    this.mbb.get(this.byteArr);
                } catch (BufferUnderflowException e) {
                    int remaining = this.mbb.remaining();
                    if (remaining > 0) {
                        this.mbb.get(this.byteArr, 0, remaining);
                        this.BufSize = remaining;
                    } else {
                        if (this.curNumOfReads <= this.numOfReads) {
                            this.mbb = inChannel.map(FileChannel.MapMode.READ_ONLY,
                                    this.curNumOfReads * OneReadSize, this.curNumOfReads == numOfReads ? lastReadSize : OneReadSize);
                            this.curNumOfReads++;
                            this.mbb.position(0);
                            continue;
                        } else {
                            throw new IOException("Unexpected end of file while parsing, cur pos in file is " +
                                    this.posInFile + ", and the file size is " + this.fileSize);
                        }
                    }
                }


                this.posInArr = 0;
                while (this.posInArr < this.BufSize) {
                    byte b = this.byteArr[this.posInArr++];
                    if (isWhiteSpace(b))
                        continue;

                    //change the order of different branches, cmp whether we can gain perf benefits by
                    //considering the probabilities.
                    if (b == lpa) { // read the event args
                        //ensure it is valid to have an event args followed by prev thing
                        if (this.prevToken != EventName_TOKEN &&
                                this.prevToken != EventArgs_TOKEN) {
                            throw new IOException("Event args should follow an event name or event args");
                        }
                        this.prevToken = EventArgs_TOKEN;


                        if (this.isAMonitoredEvent) {//do the most rigorous type checking to the event args
                            this.rmWhiteSpace();

                            this.readEvent();
                        } else {//event is not monitored, just ensure no syntax error
                            this.looseCheckingEventArgs();
                        }
                    } else if (isStringChar(b) || b == doubleQuote) { //read an event
                        if (this.prevToken == NULL_TOKEN) {
                            throw new IOException("Event name should follow a time stamp or event args or event name");
                        }

                        this.prevToken = EventName_TOKEN;

                        if (b == doubleQuote) {
                            this.EventNameStartIndex = this.posInArr;
                            this.EventName = this.getQuotedStringFromBuf(this.EventNameStartIndex);

                        } else {
                            this.EventNameStartIndex = this.posInArr - 1;
                            this.EventName = this.getEventName();

                        }

                        this.typesInTuple = TableCol.get(EventName);
                        if (this.typesInTuple == null) {
                            throw new IOException("Unknown event " + EventName);
                        }

                        if (FormulaExtractor.monitoredEventList.contains(EventName)) {
                            this.isAMonitoredEvent = true;
                        } else {
                            this.isAMonitoredEvent = false;
                        }

                    } else if (b == at) {
                        if (this.prevToken != EventArgs_TOKEN && this.prevToken != NULL_TOKEN) {
                            throw new IOException("Time stamp should follow event args or null (if it is the first token in the file)");
                        }
                        this.prevToken = TS_TOKEN;

                        this.rmWhiteSpace();
                        this.getTSFromBuf();
                        numOfLogEntries++;
                    } else if (b == hash) {
                         this.skipComment();
                    }
                    else if (b == 0) {
                        break;
                    } else {
                        throw new IOException("Unexpected char'" + (char) b + "'");
                    }
                }

                byte[] tmp = this.oldByteArr;
                this.oldByteArr = this.byteArr;
                this.byteArr = tmp;

                this.posInFile += this.BufSize;
            }
        }


        inChannel.close();

        aFile.close();

        System.out.println("There are " +
                numOfLogEntries + " log entries in the log file!!!");

    }


    /**
     * We may decide whether to invoke an event method or not;
     * Only parse the event arg if we know we will invoke the method;
     * Even if the args are not parsed, the data types are checked against the event arg types specified in sig.
     *
     * @throws IOException
     */
    private void readEvent() throws IOException {
        Object[] tupleData = new Object[typesInTuple.length];
        int i = 0;
        for (; i < typesInTuple.length - 1; i++) {
            this.paramStartIndex = this.posInArr;

            switch (typesInTuple[i]) {
                case RegHelper.INT_TYPE:
                    tupleData[i] = this.getIntFromBuf(comma);
                    break;

                case RegHelper.FLOAT_TYPE:
                    tupleData[i] = this.getFloatingNumFromBuf(comma);
                    break;

                case RegHelper.STRING_TYPE:
                    tupleData[i] = this.getStringFromBuf(comma);
                    break;
            }
            this.rmWhiteSpace();
        }

        if (typesInTuple.length > 0) {
            //the last field should be followed by a right parenthesis
//            this.curParamIndex = typesInTuple.length - 1;
            this.paramStartIndex = this.posInArr;

            switch (typesInTuple[i]) {
                case RegHelper.INT_TYPE:
                    tupleData[i] = this.getIntFromBuf(rpa);
                    break;

                case RegHelper.FLOAT_TYPE:
                    tupleData[i] = this.getFloatingNumFromBuf(rpa);
                    break;

                case RegHelper.STRING_TYPE:
                    tupleData[i] = this.getStringFromBuf(rpa);
                    break;
            }
        }

//        this.printEvent(tupleData);

        if (EventName.equals(SigExtractor.INSERT)) {
            if (tupleData[1].equals("MYDB") && !tupleData[0].equals("notARealUserInTheDB"))
                this.printEvent(tupleData);
        }

//        if (EventName.equals(SigExtractor.SCRIPT_MD5)) {
//            //script_md5 (MY_Script,myMD5)
//            if (tupleData[0].equals("MY_Script") && !tupleData[1].equals("ItsMD5"))
//                this.printEvent(tupleData);
//        }
    }

    private void printEvent(Object[] data) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("\n@" + TimeStamp + " " + this.EventName + "(");

        for (int i = 0; i < data.length - 1; i++) {
            sb.append(data[i] + ",");
        }

        if (data.length > 0) {
            sb.append(data[data.length - 1]);
        }

        sb.append(")\n");

        System.out.println(sb.toString());
    }


    private String getStringFromBytes(int start, int len) throws IOException {
        String output;

        if (this.posInArr > start) {
            output = new String(this.byteArr, start,
                    len, this.asciiCharSet);
        } else if (this.posInArr == this.EventNameStartIndex) {
            throw new IOException("Empty String!");
        } else {//start index is a pos in the old byte array.
            int remainingSizInOldBuf = this.oldByteArr.length - start;
//            System.out.println("Remaining siz of old buf is "+remainingSizInOldBuf+";\nstart: "
//            +start+"\nlen is "+len);
            if (remainingSizInOldBuf >= len)
                output = new String(this.oldByteArr, start, len, this.asciiCharSet);
            else
                output = new String(this.oldByteArr, start,
                        remainingSizInOldBuf, this.asciiCharSet) +
                        new String(this.byteArr, 0, len - remainingSizInOldBuf, this.asciiCharSet);
        }

        return output;
    }

}
