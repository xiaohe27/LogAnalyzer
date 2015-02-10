package sig;

import com.runtimeverification.rvmonitor.core.ast.Event;
import com.runtimeverification.rvmonitor.core.ast.MonitorFile;
import com.runtimeverification.rvmonitor.core.ast.Property;
import com.runtimeverification.rvmonitor.core.ast.Specification;
import com.runtimeverification.rvmonitor.core.parser.RVParser;
import reg.RegHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * The mappings between spec name and spec level params.
     */
    private HashMap<String, String> specLangParamsMap;

    /**
     * Map from the event's name to the action performed when encountering that event.
     */
    private HashMap<String, String> eventActionsMap;

    /**
     * The table schema which contains the info about event name and event args.
     */
    private HashMap<String, int[]> TableCol;


    private SignatureFormulaExtractor() {
        this.specEventsMap=new HashMap<>();
        this.specPropertiesMap = new HashMap<>();
        this.specMonitoredEventsMap = new HashMap<>();
        this.specLangParamsMap = new HashMap<>();

        this.eventActionsMap = new HashMap<>();

        this.TableCol = new HashMap<>();
    }

    public static final SignatureFormulaExtractor SigExtractor =  InitSigExtractor();

    private static SignatureFormulaExtractor InitSigExtractor() {
        return new SignatureFormulaExtractor();
    }

    public HashMap<String, int[]> extractMethoArgsMappingFromSigFile(Path file) throws IOException {

        String fileContent = new String(Files.readAllBytes(file));
        final Reader source = new StringReader(fileContent);
        final MonitorFile monitorFile = RVParser.parse(source);

        List<Specification> specifications = monitorFile.getSpecifications();

        for (int i = 0; i < specifications.size(); i++) {
            Specification spec = specifications.get(i);

            System.out.println("Spec's name is "+spec.getName());
            System.out.println("Param of the spec is " + spec.getLanguageParameters());

            String specName = spec.getName();


            assert (!this.specEventsMap.containsKey(specName)) : "The specification should not be duplicated!";

            List<Event> eventsInCurSpec = spec.getEvents();
            List<Property> propsInCurSpec = spec.getProperties();


            this.specEventsMap.put(specName, eventsInCurSpec);
            this.specPropertiesMap.put(specName, propsInCurSpec);


            for (int j = 0; j < eventsInCurSpec.size(); j++) {
                Event event = eventsInCurSpec.get(j);
                System.out.println("Event "+j+" is "+event.getName());
            }

            for (int j = 0; j < propsInCurSpec.size(); j++) {
                Property prop = propsInCurSpec.get(j);

                System.out.println("Prop "+j+"'s name is "+prop.getName());
                System.out.println("Prop "+j+"'s syntax is "+prop.getSyntax());
            }
        }



        return null;
    }

    public static void main(String[] args) throws IOException {
        Path logPath = Paths.get("/home/xiaohe/Projects/LogAnalyzer/test/pub-approve/rvm/Pub.rvm");
        HashMap<String, int[]> tableCol = SigExtractor.extractMethoArgsMappingFromSigFile(logPath);

        printMethodSig(tableCol);
    }

    private static void printMethodSig(HashMap<String, int[]> tableCol) {
        if (tableCol == null)
            return;

        for (String s : tableCol.keySet()) {
            System.out.print("Event " + s + "'s sig: (");
            int[] typesOfArgs = tableCol.get(s);
            for (int i = 0; i < typesOfArgs.length - 1; i++) {
                switch (typesOfArgs[i]){
                    case RegHelper.INT_TYPE :
                        System.out.print("int, ");
                        break;

                    case RegHelper.FLOAT_TYPE :
                        System.out.print("double, ");
                        break;

                    case RegHelper.STRING_TYPE :
                        System.out.print("string, ");
                        break;
                }
            }

            if (typesOfArgs.length > 0) {
                switch (typesOfArgs[typesOfArgs[typesOfArgs.length - 1]]){
                    case RegHelper.INT_TYPE :
                        System.out.print("int)");
                        break;

                    case RegHelper.FLOAT_TYPE :
                        System.out.print("double)");
                        break;

                    case RegHelper.STRING_TYPE :
                        System.out.print("string)");
                        break;
                }
            }

        }
    }
}
