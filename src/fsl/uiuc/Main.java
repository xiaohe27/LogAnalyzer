package fsl.uiuc;

import reg.RegHelper;
import rvm.PubRuntimeMonitor;

import java.util.HashMap;

public class Main {

    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */
    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";

    public static HashMap<String, Integer[]> TableCol = new HashMap<>();

    private static HashMap<String, Class[]> MethodArgListMap = new HashMap<>();

    private static Class MonitorClass;

    private static void init() {
        //the arg types can be inferred from the signature file
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE};
        TableCol.put(PUBLISH, argTy4Publish);
        TableCol.put(APPROVE, argTy4Approve);

        MonitorClass = PubRuntimeMonitor.class;
    }

    public static void main(String[] args) {
        // write your code here
    }
}
