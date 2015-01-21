package fsl.uiuc;

import analysis.LogMonitor;
import gen.MonitorGenerator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static String outputPath = "./test-out/violation.txt";
    private static boolean eagerEval;

    /**
     * These are the event names.
     * Can gen them via analyzing all the events in sig file.
     */


    /**
     * Given the path to signature file, formula file and log file, checks whether the properties stated in
     * the formula file are violated by the log file.
     *
     * @param args Three arguments need to be provided in the order of: sig file, formula file, log file.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {

        if (args.length > 3 || args.length < 2) {
            System.err.println("Three args should be provided in this order: <path to signature file>" +
                    " <path to formula file> <path to log file> \nOr omit the path to log file,"
                    + " in which case the contents of log file will be read from the System.in");
        }

        Path path2SigFile = Paths.get(args[0]);

        Path path2FormulaFile = Paths.get(args[1]);

        //if there is no log file's path is given, then the log will be read from stdin
        Path path2Log = null;
        if (args.length == 3) {
            path2Log = Paths.get(args[2]);
        } else {
        }


        MonitorGenerator mg = new MonitorGenerator(path2SigFile, path2FormulaFile);

        LogMonitor lm = new LogMonitor(mg.getMethoArgsMappingFromSigFile(), mg.getMonitorClassPath());

//        lm.monitor(path2Log); //default mapped byte buffer
//        lm.monitor_bytebuffer_allocateDirect(path2Log);
        eagerEval = true;

        if (path2Log.toString().endsWith(".tar.gz")) {
//            System.out.println("Going to read a .tar.gz log file: " + path2Log.toString());
            lm.monitor(path2Log, true);
        } else {
//            System.out.println("Going to read a normal log file");
            lm.monitor(path2Log, false, eagerEval); //default mapped byte buffer
        }
    }
}
