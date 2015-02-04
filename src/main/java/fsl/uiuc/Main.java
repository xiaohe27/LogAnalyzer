package fsl.uiuc;

//import analysis.LogMonitor;

import formula.FormulaExtractor;
import gen.InvokerGenerator;
import sig.SigExtractor;
import util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static Path genLogReaderPath;

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
    public static void main(String[] args) throws IOException {
        genLogReaderPath = initOutputFile();

        Path path2SigFile = Paths.get(args[0]);

        InvokerGenerator.generateCustomizedInvoker(FormulaExtractor.monitorName, SigExtractor.extractMethoArgsMappingFromSigFile(path2SigFile.toFile()));
        String imports = new String(Files.readAllBytes(Paths.get("./src/main/resources/import.code")));
        String mainBody = new String(Files.readAllBytes(Paths.get("./src/main/resources/main.code")));
        //A:\Projects\LogAnalyzer\target\generated-sources\CodeModel
        String logReader = new String(Files.readAllBytes(Paths.get("./target/generated-sources/CodeModel/LogReader.java")));
        Utils.MyUtils.writeToOutputFileUsingBW(imports);
        Utils.MyUtils.writeToOutputFileUsingBW(logReader);
        Utils.MyUtils.writeToOutputFileUsingBW(mainBody);
        Utils.MyUtils.flushOutput();
    }

    private static Path initOutputFile() {
       Path path = Paths.get("./CustomizedLogReader/log/LogReader.java");
        File file = path.toFile();
        try {
            if (file.exists()) {
                new PrintWriter(file).close();
            } else {
                if (path.getParent().toFile().exists()) {
                    file.createNewFile();
                } else {
                    path.getParent().toFile().mkdirs();
                    file.createNewFile();
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
       return path;
    }
}
