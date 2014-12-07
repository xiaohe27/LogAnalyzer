package formula;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hx312 on 03/12/2014.
 */
public class FormulaExtractor {

    private String monitorName;
    private List<String> methodNameList;

    public FormulaExtractor(Path formulaPath) {
        //analyze the formula file and set the fields accordingly.
        init();
    }

    private void init() {
        //needs real impl. it is fake method here
        this.monitorName="rvm.insertRuntimeMonitor";

        this.methodNameList =new ArrayList<>();
        String INSERT = "insert";

        this.methodNameList.add(INSERT);

    }


    public String getMonitorName() {
        return monitorName;
    }

    public List<String> getMethodNameList() {
        return methodNameList;
    }
}
