package gen;

import formula.FormulaExtractor;
import sig.SigExtractor;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaohe on 12/3/14.
 * Generate a monitor lib according to the template below and instantiation parameters.
 *
 package rvm;

 Pub(Integer report) {
 Integer report;
 long time;

 event publish (Integer report, long time) {
 this.report=report;
 this.time=time;
 }

 event approve (Integer report, long time) {
 this.report=report;
 this.time=time;
 }

 ltl: [](publish => (*) approve)

 @violation { System.out.println("should not publish financial report "+this.report+" without pre-approval");}

 }

 */
public class MonitorGenerator {

    private File sigFile;
    private FormulaExtractor formulaExtractor;

    public MonitorGenerator(Path sigFilePath, Path formulaFilePath){
        this.sigFile= sigFilePath.toFile();
        this.formulaExtractor=new FormulaExtractor(formulaFilePath);

        this.genMonitorLib();
    }

    private void genMonitorLib() {
        //to be implemented.
    }


    public String getMonitorClassPath(){
        return this.formulaExtractor.getMonitorName();
    }

    public List<String> getMethodNameList() {
        return this.formulaExtractor.getMethodNameList();
    }

    public HashMap<String, Integer[]> getMethoArgsMappingFromSigFile(){
        return SigExtractor.extractMethoArgsMappingFromSigFile(sigFile);
    }

}
