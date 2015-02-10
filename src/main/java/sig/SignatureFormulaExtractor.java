package sig;

import com.runtimeverification.rvmonitor.core.ast.Event;
import com.runtimeverification.rvmonitor.core.ast.MonitorFile;
import com.runtimeverification.rvmonitor.core.ast.Property;
import com.runtimeverification.rvmonitor.core.parser.RVParser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hx312 on 10/02/2015.
 */
public class SignatureFormulaExtractor {
    /**
     *   Maybe there are multiple specs, each of which contains some events and properties.
     *   For the events that do not appear in the corresponding property section, they are valid events, but NOT monitored.
     *   It is NOT allowed to have events with same name but different sig.
     *   Events and Properties are indexed by spec name because we want to distinguish events in different specs.
     *   An event with the same signature can appear in multiple specs, however, it is also possible that the same event
     *   is monitored in one spec while NOT monitored in another.
     */
    private HashMap<String, List<Event>> specEventsMap;
    private HashMap<String, List<Property>> specPropertiesMap;
    /**
     * E.G. boolean array's No. 5 element is true indicates the No.5'event in that spec is monitored.
     */
    private HashMap<String, boolean[]> specMonitoredEventsMap;
    private SignatureFormulaExtractor() {
        this.specEventsMap=new HashMap<>();
        this.specPropertiesMap = new HashMap<>();
        this.specMonitoredEventsMap = new HashMap<>();
    }

    public static final SignatureFormulaExtractor SigExtractor =  InitSigExtractor();

    private static SignatureFormulaExtractor InitSigExtractor() {
        return new SignatureFormulaExtractor();
    }

    public HashMap<String, int[]> extractMethoArgsMappingFromSigFile(Path file) throws IOException {
        String fileContent = new String(Files.readAllBytes(file));
        final Reader source = new StringReader(fileContent);
        final MonitorFile spec = RVParser.parse(source);


        return null;
    }
}
