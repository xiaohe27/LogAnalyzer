package gen;

import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import formula.FormulaExtractor;
import reg.RegHelper;
import sig.SignatureFormulaExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaohe on 2/2/15.
 */
public class InvokerGenerator {
    private JCodeModel CodeModel;
    private String MonitorName;
    private List<String> RawMonitorNames;

    private String outputDir;
    public InvokerGenerator(String outputDir) {
        this.outputDir = outputDir;
    }

    public void generateCustomizedInvoker(String monitorClassPath, List<String> specNames, HashMap<String, int[]> tableSchema) {
        CodeModel = new JCodeModel();
        this.MonitorName = monitorClassPath;

        assert MonitorName != null;

//        this.RawMonitorName = specName + "RawMonitor";
        this.RawMonitorNames = new ArrayList<>();
        String packageName = "rvm.";
        for (int i = 0; i < specNames.size(); i++) {
            this.RawMonitorNames.add(packageName + specNames.get(i) + "RawMonitor");
        }

        try {
            JDefinedClass logReaderClass = CodeModel._class("LogReader");
            initLogReaderClass(logReaderClass);

            JDefinedClass definedClass = logReaderClass._class(JMod.NONE | JMod.STATIC, "MonitorMethodsInvoker");
            SingleStreamCodeWriter sscw = new SingleStreamCodeWriter(System.out);

            buildInvocationMethod(definedClass, tableSchema);
            File outputDir = new File(this.outputDir);
            if (!outputDir.exists())
                outputDir.mkdirs();


//            CodeModel.build(sscw);
            CodeModel.build(outputDir);

        } catch (JClassAlreadyExistsException e) {
            // ...
            System.err.println("JClassAlreadyExisting-Exception: " + e.getMessage());
        } catch (IOException e) {
            // ...
            System.err.println("IO-Exception: " + e.getMessage());
        }
    }

    private void initLogReaderClass(JDefinedClass definedClass) {
        definedClass.direct("\n    private static String outputPathStr = \"./test-out/violation.txt\";\n" +
                "    public static Path outputPath = Paths.get(outputPathStr);\n" +
                "\n" +
                "    private static void initOutputFile() throws IOException {\n" +
                "        File file = outputPath.toFile();\n" +
                "        if (file.exists()) {\n" +
                "            new PrintWriter(file).close();\n" +
                "        } else {\n" +
                "            if (outputPath.getParent().toFile().exists()) {\n" +
                "                file.createNewFile();\n" +
                "            } else {\n" +
                "                outputPath.getParent().toFile().mkdirs();\n" +
                "                file.createNewFile();\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) throws IOException {\n" +
                "        if (args.length > 2 || args.length < 1) {\n" +
                "            System.err.println(\"Two args should be provided in this order: <path to rvm spec file>\" +\n" +
                "                    \" <path to log file> \\nOr omit the path to log file,\"\n" +
                "                    + \" in which case the contents of log file will be read from the System.in\");\n" +
                "        }\n" +
                "\n" +
                "        initOutputFile();\n" +
                "\n" +
                "        Path path2SigFile = Paths.get(args[0]);\n" +
                "\n" +
                "        //if there is no log file's path is given, then the log will be read from stdin\n" +
                "        Path path2Log = null;\n" +
                "        if (args.length == 2) {\n" +
                "            path2Log = Paths.get(args[1]);\n" +
                "        } else {\n" +
                "            throw new IOException(\"Does not support reading form std input yet.\");\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        LogEntryExtractor lee = new LogEntryExtractor(EventSigExtractor.extractEventsInfoFromSigFile(path2SigFile.toFile()), path2Log, 6);\n" +
                "\n" +
                "        lee.startReadingEventsByteByByte();\n" +
                "    }\n" +
                "\n" +
                "    public static interface LogExtractor {\n" +
                "        public void startReadingEventsByteByByte() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;\n" +
                "    }");
    }


    private void buildInvocationMethod(JDefinedClass definedClass, HashMap<String, int[]> tableSchema) {
        JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC, Void.TYPE, "invoke");
        String eventNameStr = "eventName";
        String methodArgsStr = "data";
        String objArrListStr = "violationsInCurLogEntry";

        JVar eventNameParam = method.param(String.class, eventNameStr);
        JVar tupleData = method.param(Object[].class, methodArgsStr);

        JType objArrListTy = CodeModel.ref(List.class).narrow(Object[].class);
        JVar violationsInCurLogEntry = method.param(objArrListTy, "violationsInCurLogEntry");

        //gen the body of the method
        JBlock body = method.body();

        JFieldRef[] hasViolation = new JFieldRef[this.RawMonitorNames.size()];

        for (int i = 0; i < this.RawMonitorNames.size(); i++) {
            String RawMonitorNameI = this.RawMonitorNames.get(i);
            hasViolation[i] = CodeModel.ref(RawMonitorNameI).staticRef("hasViolation");
            body.assign(hasViolation[i], JExpr.lit(false));
        }

        JSwitch jSwitch = body._switch(eventNameParam);

        JClass monitorClass = CodeModel.ref(MonitorName);

        for (String eventName : FormulaExtractor.monitoredEventList) {
            JCase jCase = jSwitch._case(JExpr.lit(eventName));
            JInvocation eventMethodInvok = monitorClass.staticInvoke(eventName + "Event");
            jCase.body().add(eventMethodInvok);

            int[] cols = tableSchema.get(eventName);

            for (int i = 0; i < cols.length; i++) {
                JExpression index = JExpr.lit(i);
                switch (cols[i]) {
                    case RegHelper.INT_TYPE:
                        JType intTy = CodeModel.directClass("Integer");
                        JExpression intArg = JExpr.cast(intTy, tupleData.component(index));
                        eventMethodInvok.arg(intArg);
                        break;

                    case RegHelper.FLOAT_TYPE:
                        JType floatTy = CodeModel.directClass("Double");
                        JExpression floatArg = JExpr.cast(floatTy, tupleData.component(index));
                        eventMethodInvok.arg(floatArg);
                        break;

                    case RegHelper.STRING_TYPE:
                        JType stringTy = CodeModel.directClass("String");
                        JExpression stringArg = JExpr.cast(stringTy, tupleData.component(index));
                        eventMethodInvok.arg(stringArg);
                        break;
                }
            }

            jCase.body()._break();
        }

        for (int i = 0; i < this.RawMonitorNames.size(); i++) {
            JConditional ifBlock = body._if(hasViolation[i]);
            JInvocation addViolationStmt = violationsInCurLogEntry.invoke("add");
            addViolationStmt.arg(tupleData);
            ifBlock._then().add(addViolationStmt);
        }
    }


    public static void main(String[] args) throws IOException {
        String monitorName;
        monitorName = "rvm.InsertRuntimeMonitor";
//        monitorName = "rvm.PubRuntimeMonitor";
        InvokerGenerator ig = new InvokerGenerator("./target/generated-sources/CodeModel");
        Path path2SigFile = Paths.get("./test/pub-approve/rvm/Pub.rvm");
        SignatureFormulaExtractor.EventsInfo eventsInfo =  SignatureFormulaExtractor.SigExtractor.
                extractEventsInfoFromSigFile(path2SigFile);

        ArrayList<String> specList = new ArrayList<>();
        specList.add("Insert");
        ig.generateCustomizedInvoker(monitorName, specList, eventsInfo.getTableCol());
    }
}
