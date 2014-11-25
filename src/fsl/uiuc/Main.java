package fsl.uiuc;

import analysis.LogMonitor;
import reg.RegHelper;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {

    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */
    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";

    public static HashMap<String, Integer[]> TableCol = initTableCol();

    private static HashMap<String, Integer[]> initTableCol() {
        HashMap<String, Integer[]> tmp=new HashMap<>();
        //the arg types can be inferred from the signature file
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE};
        tmp.put(PUBLISH, argTy4Publish);
        tmp.put(APPROVE, argTy4Approve);
        return tmp;
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Path path2Log= Paths.get("Pub.log");
        String monitorClassName="rvm.PubRuntimeMonitor";

        LogMonitor lm=new LogMonitor(TableCol, monitorClassName);
        lm.monitor(path2Log);
    }
}
