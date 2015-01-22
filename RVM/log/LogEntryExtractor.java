package log;

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
    static final int OneReadSize = 0xFFFFFFF + 1; //256MB one time

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
    private final Charset asciiCharSet = Charset.forName("ISO-8859-1");
    private long TimeStamp; //we can add the @ symbol when it is ready to be printed
    private String EventName;
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
    private int EventNameLen;
    private int[] paramStartPosArr = new int[SigExtractor.maxNumOfParams];
    //    private long[] paramEndPosArr = new long[SigExtractor.maxNumOfParams];
    private int[] paramLenArr = new int[SigExtractor.maxNumOfParams];
    private int curParamIndex; // the current index of param in the event
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

        if (b > 96 && b < 123)
            return true;

        if (b == underscore || b == rightBracket || b == exclamation)
            return true;
        else return false;
    }

    private void getEventNameLen() throws IOException {
        this.EventNameLen = 1;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    this.EventNameLen++;
                } else {
                    if (b == lpa) {
                        this.posInArr--;
                    }
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
                            continue;
                        } else {
                            throw new IOException("Unexpected end of file while parsing an event name");
                        }
                    }
                }
            }
        }

    }

    private void getStringLenFromBuf(byte delim) throws IOException {
        int len = 0;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (isStringChar(b)) {
                    len++;
                } else {
                    this.paramLenArr[this.curParamIndex] = len;
                    if (b == delim)
                        return;

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return;
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
                            continue;
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
                if (isWhiteSpace(b))
                    continue;
                else {
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
                            continue;
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
                    String out = "";

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
                            continue;
                        } else {
                            throw new IOException("Unexpected end of file while parsing a time stamp");
                        }
                    }
                }
            }
        }
    }

    //b > 47 && b < 58 then b is a digit char
    private void getFloatingNumLenFromBuf(byte delim) throws IOException {
        int len = 0;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58 || b == dot) {
                    len++;
                } else {
                    this.paramLenArr[this.curParamIndex] = len;
                    if (b == delim)
                        return;

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return;
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
                            continue;
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
    private void getIntLenFromBuf(byte delim) throws IOException {
        int len = 0;
        while (true) {
            while (this.posInArr < this.BufSize) {
                byte b = byteArr[this.posInArr++];
                if (b > 47 && b < 58) {
                    len++;
                } else {
                    this.paramLenArr[this.curParamIndex] = len;
                    if (b == delim)
                        return;

                    this.rmWhiteSpace();
                    if (this.byteArr[this.posInArr++] == delim) {
                        return;
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
                            continue;
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
                    if (b == lpa) {
                        this.rmWhiteSpace();
                        this.readEvent();
                    } else if (isStringChar(b)) {
                        this.EventNameStartIndex = this.posInArr - 1;
                        this.getEventNameLen();

                        String output = "";

                        if (this.posInArr > this.EventNameStartIndex) {
                            output = new String(this.byteArr, this.EventNameStartIndex,
                                    this.EventNameLen, this.asciiCharSet);
                        } else if (this.posInArr == this.EventNameStartIndex) {
                            throw new IOException("Empty String!");
                        } else {//start index is a pos in the old byte array.
                            int remainingSizInOldBuf = this.oldByteArr.length - this.EventNameStartIndex;
                            //            System.out.println("Remaining siz of old buf is "+remainingSizInOldBuf+";\nstart: "
                            //            +start+"\nlen is "+len);
                            if (remainingSizInOldBuf >= this.EventNameLen)
                                output = new String(this.oldByteArr, this.EventNameStartIndex, this.EventNameLen, this.asciiCharSet);
                            else
                                output = new String(this.oldByteArr, this.EventNameStartIndex,
                                        remainingSizInOldBuf, this.asciiCharSet) +
                                        new String(this.byteArr, 0, this.EventNameLen - remainingSizInOldBuf, this.asciiCharSet);
                        }

                        this.EventName = output;
                    } else if (b == at) {
                        this.rmWhiteSpace();
                        this.getTSFromBuf();
                        numOfLogEntries++;
                    } else if (b == 0) {
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

//        System.out.println("There are " +
//                numOfLogEntries + " log entries in the log file!!!");

    }


    /**
     * We may decide whether to invoke an event method or not;
     * Only parse the event arg if we know we will invoke the method;
     * Even if the args are not parsed, the data types are checked against the event arg types specified in sig.
     *
     * @throws IOException
     */
    private void readEvent() throws IOException {
        Integer[] typesInTuple = TableCol.get(EventName);
        if (typesInTuple == null)
        System.out.println("Null table col, Event name is "+EventName
        + ". And number of reads is "+this.numOfReads+", pos in file is "+this.posInFile);
        for (this.curParamIndex = 0; this.curParamIndex < typesInTuple.length - 1; this.curParamIndex++) {
            this.paramStartPosArr[this.curParamIndex] = this.posInArr;

            switch (typesInTuple[this.curParamIndex]) {
                case RegHelper.INT_TYPE:
                    this.getIntLenFromBuf(comma);
                    break;

                case RegHelper.FLOAT_TYPE:
                    this.getFloatingNumLenFromBuf(comma);
                    break;

                case RegHelper.STRING_TYPE:
                    this.getStringLenFromBuf(comma);
                    break;
            }
            this.rmWhiteSpace();
        }

        if (typesInTuple.length > 0) {
            //the last field should be followed by a right parenthesis
//            this.curParamIndex = typesInTuple.length - 1;
            this.paramStartPosArr[this.curParamIndex] = this.posInArr;

            switch (typesInTuple[this.curParamIndex]) {
                case RegHelper.INT_TYPE:
                    this.getIntLenFromBuf(rpa);
                    break;

                case RegHelper.FLOAT_TYPE:
                    this.getFloatingNumLenFromBuf(rpa);
                    break;

                case RegHelper.STRING_TYPE:
                    this.getStringLenFromBuf(rpa);
                    break;
            }
        }


//        if (EventName.equals(SigExtractor.INSERT)) {
//            Object[] argsInTuple = this.parseEventArgs();
//
//            if (argsInTuple[1].equals("MYDB") && !argsInTuple[0].equals("notARealUserInTheDB"))
//                this.printEvent(argsInTuple);
//        }

        if (EventName.equals(SigExtractor.SCRIPT_MD5)) {
            //script_md5 (MY_Script,myMD5)
            Object[] argsInTuple = this.parseEventArgs();

            if (argsInTuple[0].equals("MY_Script") && !argsInTuple[1].equals("ItsMD5"))
                this.printEvent(argsInTuple);
        }
    }

    /**
     * Parse the args of the event.
     *
     * @throws IOException
     */
    private Object[] parseEventArgs() throws IOException {
        Integer[] typesInTuple = TableCol.get(EventName);
        Object[] data = new Object[typesInTuple.length];

        String dataI = null;


        for (this.curParamIndex = 0; this.curParamIndex < typesInTuple.length - 1; this.curParamIndex++) {
            int startIndex = this.paramStartPosArr[this.curParamIndex];
            int len = this.paramLenArr[this.curParamIndex];

            if (this.posInArr > startIndex) {
                dataI = new String(this.byteArr, startIndex,
                        len, this.asciiCharSet);
            } else if (this.posInArr == this.EventNameStartIndex) {
                throw new IOException("Empty String!");
            } else {//start index is a pos in the old byte array.
                int remainingSizInOldBuf = this.oldByteArr.length - startIndex;
                //            System.out.println("Remaining siz of old buf is "+remainingSizInOldBuf+";\nstart: "
                //            +start+"\nlen is "+len);
                if (remainingSizInOldBuf >= len)
                    dataI = new String(this.oldByteArr, startIndex, len, this.asciiCharSet);
                else
                    dataI = new String(this.oldByteArr, startIndex,
                            remainingSizInOldBuf, this.asciiCharSet) +
                            new String(this.byteArr, 0, len - remainingSizInOldBuf, this.asciiCharSet);
            }


            switch (typesInTuple[this.curParamIndex]) {
                case RegHelper.INT_TYPE:
                    data[this.curParamIndex] = Integer.parseInt(dataI);
                    break;

                case RegHelper.FLOAT_TYPE:
                    data[this.curParamIndex] = Double.parseDouble(dataI);
                    break;

                case RegHelper.STRING_TYPE:
                    data[this.curParamIndex] = dataI;
                    break;
            }
        }

        if (typesInTuple.length > 0) {
            //the last field should be followed by a right parenthesis
            this.curParamIndex = typesInTuple.length - 1;

            int startIndex = this.paramStartPosArr[this.curParamIndex];
            int len = this.paramLenArr[this.curParamIndex];

            if (this.posInArr > startIndex) {
                dataI = new String(this.byteArr, startIndex,
                        len, this.asciiCharSet);
            } else if (this.posInArr == this.EventNameStartIndex) {
                throw new IOException("Empty String!");
            } else {//start index is a pos in the old byte array.
                int remainingSizInOldBuf = this.oldByteArr.length - startIndex;
                //            System.out.println("Remaining siz of old buf is "+remainingSizInOldBuf+";\nstart: "
                //            +start+"\nlen is "+len);
                if (remainingSizInOldBuf >= len)
                    dataI = new String(this.oldByteArr, startIndex, len, this.asciiCharSet);
                else
                    dataI = new String(this.oldByteArr, startIndex,
                            remainingSizInOldBuf, this.asciiCharSet) +
                            new String(this.byteArr, 0, len - remainingSizInOldBuf, this.asciiCharSet);
            }

//            String dataI = output;

            switch (typesInTuple[this.curParamIndex]) {
                case RegHelper.INT_TYPE:
                    data[this.curParamIndex] = Integer.parseInt(dataI);
                    break;

                case RegHelper.FLOAT_TYPE:
                    data[this.curParamIndex] = Double.parseDouble(dataI);
                    break;

                case RegHelper.STRING_TYPE:
                    data[this.curParamIndex] = dataI;
                    break;
            }
        }

        return data;

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
        String output = "";

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
