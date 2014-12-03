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


    /**
     * Given the path to signature file, formula file and log file, checks whether the properties stated in
     * the formula file are violated by the log file.
     * @param args Three arguments need to be provided in the order of: sig file, formula file, log file.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if(args.length != 3)
        {
            System.err.println("Three args should be provided in this order: <path to signature file>" +
                    " <path to formula file> <path to log file>");
        }

        Path path2SigFile= Paths.get(args[0]);

        Path path2FormulaFile= Paths.get(args[1]);

        Path path2Log= Paths.get(args[2]);

        MonitorGenerator mg=new MonitorGenerator(path2SigFile,path2FormulaFile);

        LogMonitor lm=new LogMonitor(mg.getMethoArgsMappingFromSigFile(), mg.getMonitorClassPath());
        lm.monitor(path2Log);
    }
}
