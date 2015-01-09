package sig;

import reg.RegHelper;

import java.io.File;
import java.util.HashMap;

/**
 * Created by xiaohe on 12/3/14.
 */
public class SigExtractor {

    public static final String INSERT = "insert";

    public static HashMap<String, Integer[]> TableCol = initTableCol();

    private static HashMap<String, Integer[]> initTableCol() {
        HashMap<String, Integer[]> tmp = new HashMap<>();
        //the arg types can be inferred from the signature file
        Integer[] argTy4Insert = new Integer[]{RegHelper.STRING_TYPE, RegHelper.STRING_TYPE,
                RegHelper.STRING_TYPE, RegHelper.STRING_TYPE};

        tmp.put(INSERT, argTy4Insert);
        return tmp;
    }


    public static HashMap<String, Integer[]> extractMethoArgsMappingFromSigFile(File f) {
        //fake method at the moment, needs to be implemented.
        return TableCol;
    }
}
