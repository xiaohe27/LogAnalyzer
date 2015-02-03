package gen;

import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import formula.FormulaExtractor;
import reg.RegHelper;
import sig.SigExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by xiaohe on 2/2/15.
 */
public class InvokerGenerator {
    private static JCodeModel CodeModel;
    private static String MonitorName;

    public static void generateCustomizedInvoker(String monitorClassPath, HashMap<String, int[]> tableSchema) {
        CodeModel = new JCodeModel();
        MonitorName = monitorClassPath;
        try {
            JDefinedClass definedClass = CodeModel._class("log.invoker.MonitorMethodsInvoker");

            SingleStreamCodeWriter sscw = new SingleStreamCodeWriter(System.out);

            buildMethod(definedClass, tableSchema);
            File outputDir = new File("./target/generated-sources/CodeModel");
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

    private static void buildMethod(JDefinedClass definedClass, HashMap<String, int[]> tableSchema) {
        JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC, Void.TYPE, "invoke");
        String eventNameStr = "eventName";
        String methodArgsStr = "data";

        JVar eventNameParam = method.param(String.class, eventNameStr);

        JVar methodArgsParam = method.param(Object[].class, methodArgsStr);

        //gen the body of the method
        JBlock body = method.body();

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
                        JExpression intArg = JExpr.cast(intTy, methodArgsParam.component(index));
                        eventMethodInvok.arg(intArg);
                        break;

                    case RegHelper.FLOAT_TYPE:
                        JType floatTy = CodeModel.directClass("Double");
                        JExpression floatArg = JExpr.cast(floatTy, methodArgsParam.component(index));
                        eventMethodInvok.arg(floatArg);
                        break;

                    case RegHelper.STRING_TYPE:
                        JType stringTy = CodeModel.directClass("String");
                        JExpression stringArg = JExpr.cast(stringTy, methodArgsParam.component(index));
                        eventMethodInvok.arg(stringArg);
                        break;
                }
            }

            jCase.body()._break();
        }
    }


    public static void main(String[] args) {
        String monitorName;
        monitorName = "rvm.InsertRuntimeMonitor";
//        monitorName = "rvm.PubRuntimeMonitor";

        generateCustomizedInvoker(monitorName, SigExtractor.TableCol);
    }
}
