package formula;

import sig.SigExtractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hx312 on 03/12/2014.
 */
public class FormulaExtractor {

    public static List<String> monitoredEventList = init();
    private static String monitorName;
    ;

    public FormulaExtractor(Path formulaPath) {
        //analyze the formula file and set the fields accordingly.

    }

    private static List<String> init() {
        List<String> tmp = new ArrayList<>();
        //needs real impl. it is fake method here
        monitorName = "rvm.insertRuntimeMonitor";

        tmp = new ArrayList<>();

        tmp.add(SigExtractor.INSERT);

//        tmp.add(SigExtractor.SCRIPT_MD5);
        return tmp;

    }


    public String getMonitorName() {
        return monitorName;
    }

    public List<String> getMonitoredEventList() {
        return monitoredEventList;
    }

    public boolean isMonitoredEvent(String eventName) {
        return this.monitoredEventList.contains(eventName);
    }
}
