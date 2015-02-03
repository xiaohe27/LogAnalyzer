package fsl.uiuc;

//import analysis.LogMonitor;

import formula.FormulaExtractor;
import gen.InvokerGenerator;
import sig.SigExtractor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static String outputPathStr = "./test-out/violation.txt";

    public static Path outputPath = Paths.get(outputPathStr);


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

        initOutputFile();

        Path path2SigFile = Paths.get(args[0]);

        Path path2FormulaFile = Paths.get(args[1]);

        //if there is no log file's path is given, then the log will be read from stdin
        Path path2Log = null;
        if (args.length == 3) {
            path2Log = Paths.get(args[2]);
        } else {
        }

        InvokerGenerator.generateCustomizedInvoker(FormulaExtractor.monitorName, SigExtractor.extractMethoArgsMappingFromSigFile(path2SigFile.toFile()));
    }

    private static void initOutputFile() throws IOException {
        File file = outputPath.toFile();
        if (file.exists()) {
            new PrintWriter(file).close();
        } else {
            if (outputPath.getParent().toFile().exists()) {
                file.createNewFile();
            } else {
                outputPath.getParent().toFile().mkdirs();
                file.createNewFile();
            }
        }
    }
}
