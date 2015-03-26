import fsl.uiuc.Main;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by hx312 on 24/01/2015.
 */
public class ShouldPassTest {
    @Test
    public void test1() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "./test/shouldPass/good1.log"};
        Main.main(args);
    }

    @Test
    public void test2() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "./test/shouldPass/good2.log"};
        Main.main(args);
    }

    @Test
    public void test3() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "./test/shouldPass/good3.log"};
        Main.main(args);
    }

    @Test
    public void test4() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "./test/shouldPass/good4.log"};
        Main.main(args);
    }
}
