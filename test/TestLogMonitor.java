import analysis.LogMonitor;
import org.junit.Test;

/**
 * Created by xiaohe on 14-11-24.
 */
public class TestLogMonitor {
    @Test
    public void testLogEntryMain(){
        String[] args=new String[]{};
        LogMonitor.main(args);

        //after refactoring, assert whether the log file has been fully read
        //by testing whether hasNext() returns false;
        
    }
}