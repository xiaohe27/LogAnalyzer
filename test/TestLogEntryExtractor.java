import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogEntryExtractor {
    @Test
    public void testLogEntryMain(){
        String[] args=new String[]{};
        LogEntryExtractor.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;
        
    }
}
