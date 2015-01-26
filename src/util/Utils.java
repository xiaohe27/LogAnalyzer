package util;

import fsl.uiuc.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.Files.newBufferedWriter;

/**
 * Created by xiaohe on 9/17/14.
 */
public class Utils {
    public static final Utils MY_Utils = new Utils();

    private Charset charset = Charset.forName("US-ASCII");
    private BufferedWriter bufferedWriter = init();
//    public static final String lineSeparator = System.getProperty("line.separator");

    public static void writeToOutputFile(String contents) throws IOException {
        byte[] bytes = contents.getBytes();

        Files.write(Main.outputPath, bytes, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }


    public static void writeToFile(String contents, String fileName) {
        Path p = Paths.get(fileName);
        byte[] bytes = contents.getBytes();
        try {
            Files.write(p, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            try {
                p.getParent().toFile().mkdirs();

                Files.write(p, bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void writeToDefaultOutputFile(String contents) throws IOException {
        this.bufferedWriter.write(contents);
    }

    public void flushOutput() throws IOException {
        this.bufferedWriter.flush();
    }

    private BufferedWriter init() {

        File outFile = Main.outputPath.toFile();
        boolean outFileExist = outFile.exists();
        try {
            if (!outFileExist) {
                if (!Main.outputPath.getParent().toFile().exists())
                    Main.outputPath.getParent().toFile().mkdirs();

                Main.outputPath.toFile().createNewFile();
            }

            StandardOpenOption option = outFileExist ? StandardOpenOption.TRUNCATE_EXISTING :
                    StandardOpenOption.APPEND;

            return newBufferedWriter(Main.outputPath, charset, option, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to create a buffered writerTruncate, exit");
            System.exit(0);
        }
        return null;
    }
}