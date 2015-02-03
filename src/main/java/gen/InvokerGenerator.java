package gen;

import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import formula.FormulaExtractor;
import reg.RegHelper;
import sig.SigExtractor;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by xiaohe on 2/2/15.
 */
public class InvokerGenerator {

    public static void generateCustomizedInvoker(String monitorClassPath, HashMap<String, int[]> tableSchema) {
        JCodeModel codeModel = new JCodeModel();
        try {
            JDefinedClass definedClass = codeModel._class("log.invoker.MonitorMethodsInvoker");

            SingleStreamCodeWriter sscw = new SingleStreamCodeWriter(System.out);

            buildMethod(definedClass, tableSchema);

            codeModel.build(sscw);

        } catch (JClassAlreadyExistsException e) {
            // ...
        } catch (IOException e) {
            // ...
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

        JExpression monitorClass = JExpr.lit(FormulaExtractor.monitorName);

        for (String eventName : FormulaExtractor.monitoredEventList) {
            JCase jCase = jSwitch._case(JExpr.lit(eventName));

            jCase.body().invoke(monitorClass, "hi");

            int[] cols = tableSchema.get(eventName);

            for (int i = 0; i < cols.length; i++) {
                switch (cols[i]){
                    case RegHelper.INT_TYPE :
                }
            }
        }
    }


    public static void main(String[] args) {
        String monitorName;
        //        monitorName = "rvm.InsertRuntimeMonitor";
        monitorName = "rvm.PubRuntimeMonitor";

        generateCustomizedInvoker(monitorName, SigExtractor.TableCol);
    }
}
