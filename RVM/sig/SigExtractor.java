package sig;

import reg.RegHelper;

import java.io.File;
import java.util.HashMap;

/**
 * Created by xiaohe on 12/3/14.
 */
public class SigExtractor {

    public static final String PUBLISH = "publish";
    public static final String APPROVE = "approve";

    public static HashMap<String, Integer[]> TableCol = initTableCol();

    private static HashMap<String, Integer[]> initTableCol() {
        HashMap<String, Integer[]> tmp=new HashMap<>();
        //the arg types can be inferred from the signature file
        Integer[] argTy4Publish = new Integer[]{RegHelper.INT_TYPE, RegHelper.STRING_TYPE};
        Integer[] argTy4Approve = new Integer[]{RegHelper.INT_TYPE, RegHelper.STRING_TYPE};
        tmp.put(PUBLISH, argTy4Publish);
        tmp.put(APPROVE, argTy4Approve);
        return tmp;
    }


    public static HashMap<String, Integer[]> extractMethoArgsMappingFromSigFile(File f){
        //fake method at the moment, needs to be implemented.
        return TableCol;
    }
}
