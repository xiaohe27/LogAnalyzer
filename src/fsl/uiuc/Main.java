package fsl.uiuc;

import analysis.LogMonitor;
import gen.MonitorGenerator;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */




    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Path path2Log= Paths.get("./test/Pub.log");

        Path path2SigFile= Paths.get("./test/pub.sig");

        Path path2FormulaFile= Paths.get("./test/pub.fl");

        MonitorGenerator mg=new MonitorGenerator(path2SigFile,path2FormulaFile);

        LogMonitor lm=new LogMonitor(mg.getMethoArgsMappingFromSigFile(), mg.getMonitorClassPath());
        lm.monitor(path2Log);
    }
}
