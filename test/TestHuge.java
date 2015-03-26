import log.LogEntryExtractor_ByteBuffer_AllocateDirect;
import org.junit.Test;
import sig.SigExtractor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by xiaohe on 1/21/15.
 */
public class TestHuge {
    String path2CompleteLog = "/home/xiaohe/workspace/DATA/ldccComplete_MonpolyStyle";
    String path2CompleteLogWithSingleViolation = "/home/xiaohe/workspace/DATA/MeasureBaseTime/ldccComplete_MonpolyStyle_addMore";

    @Test
    public void parseHuge() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Common.testLog_multiTimes(this.path2CompleteLogWithSingleViolation, 1);
    }

    //    @Test
    public void parseHugeByDirectByteBuffer() throws IOException {
        Path logPath = Paths.get(path2CompleteLogWithSingleViolation);
        LogEntryExtractor_ByteBuffer_AllocateDirect lee = new LogEntryExtractor_ByteBuffer_AllocateDirect(SigExtractor.TableCol, logPath);

        long startT = System.currentTimeMillis();
        lee.startReadingEventsByteByByte();
        long timeDiff = System.currentTimeMillis() - startT;

        System.out.println("It takes my log analyzer " + timeDiff +
                " ms to count all the events in the log file after running ");

    }
}
