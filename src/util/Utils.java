package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by xiaohe on 9/17/14.
 */
public class Utils {

    public static void writeToFile(String contents, String fileName) {
        Path p = Paths.get(fileName);
        byte[] bytes = contents.getBytes();
        try {
            Files.write(p, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            try {
                Files.write(p, bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
