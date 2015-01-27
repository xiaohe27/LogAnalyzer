package formula;

import sig.SigExtractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hx312 on 03/12/2014.
 */
public class FormulaExtractor {

    public static List<String> monitoredEventList = init();
    public static HashMap<String, boolean[]> skippedFieldsMap = init2();
    private static String monitorName;

    public FormulaExtractor(Path formulaPath) {
        //analyze the formula file and set the fields accordingly.

    }
    ;

    private static HashMap<String, boolean[]> init2() {
        HashMap<String, boolean[]> tmp = new HashMap<>();
        boolean[] skipList = new boolean[SigExtractor.maxNumOfParams];
        skipList[1] = true;
        tmp.put(SigExtractor.INSERT, skipList);
        return tmp;
    }

    private static List<String> init() {
        List<String> tmp = new ArrayList<>();
        //needs real impl. it is fake method here
        monitorName = "rvm.InsertRuntimeMonitor";
//        monitorName = "rvm.PubRuntimeMonitor";

        tmp = new ArrayList<>();

        tmp.add(SigExtractor.INSERT);

//        tmp.add(SigExtractor.SCRIPT_MD5);
//        tmp.add(SigExtractor.APPROVE);
//        tmp.add(SigExtractor.PUBLISH);
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
