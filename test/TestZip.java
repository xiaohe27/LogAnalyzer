import fsl.uiuc.Main;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by hx312 on 19/01/2015.
 */
public class TestZip {
    String HP_9M_Zip_Path = "A:\\DATA\\ldcc4Monpoly.tar.gz";
    String HP_SmallZip_Path = "A:\\Projects\\LogAnalyzer\\test\\insert-smallLog\\insert.tar.gz";

    @Test
    public void test9M_Zip_HP() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                HP_9M_Zip_Path};
        Main.main(args);
    }

//    @Test
    public void testSmallZip_HP() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException, IllegalAccessException {
        String[] args = new String[]{"./test/count/insert.sig", "./test/count/insert.fl",
                HP_SmallZip_Path};
        Main.main(args);
    }
}
