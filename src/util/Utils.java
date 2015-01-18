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
    private BufferedWriter writerTruncate = init(StandardOpenOption.TRUNCATE_EXISTING);
    private BufferedWriter writerAppend = init(StandardOpenOption.APPEND);

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

    private BufferedWriter init(StandardOpenOption option) {
        Path output = Paths.get(Main.outputPath);
        File outFile = output.toFile();
        try {
            if (outFile.exists()) {
                outFile.delete();
                outFile.createNewFile();
            }

            else {
                if (!output.getParent().toFile().exists())
                    output.getParent().toFile().mkdirs();

                output.toFile().createNewFile();
            }

            return newBufferedWriter(output, charset, option, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to create a buffered writerTruncate, exit");
            System.exit(0);
        }
        return null;
    }

    public void write_truncate(String string) throws IOException {
        writerTruncate.write(string);
    }

    public void write_append(String string) throws IOException {
        writerAppend.write(string);
    }

    public void flush() throws IOException {
        this.writerAppend.flush();
    }
}