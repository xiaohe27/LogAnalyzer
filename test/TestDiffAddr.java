import fsl.uiuc.Main;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * For different fields, even if they have identical val, the generated obj (at different places) will have diff addr.
 * This will cause problem in rvm.
 * Two possible sol:
 * 1. use hashmap to record val-obj mappings, and each time uses the obj to invoke monitor methods.
 * Drawback: can use a lot of mem, and it slows system down.
 * 2. use hashcode? Need to do more experiments to make sure it works.
 * Created by hx312 on 27/01/2015.
 */
public class TestDiffAddr {
//    @Test
    public void testBigPub() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "A:\\DATA\\Gen\\Pub_fake.log"};
        Main.main(args);
    }

    @Test
    public void testSmallPub() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                "./test/pub-approve/Pub.log"};
        Main.main(args);
    }
}
