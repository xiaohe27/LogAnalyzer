package reg;

/**
 * Created by xiaohe on 14-11-22.
 */
public class RegHelper {
    public static String FieldsRegEx="("+ RegHelper.addSpace("\\p{Alnum}+)")
            +"("+RegHelper.addSpace(",")+
            RegHelper.addSpace("\\p{Alnum}+)*");

    public static String TupleRegEx=RegHelper.addParen(FieldsRegEx);

    public static String TableRegEx=addSpace("\\p{Alpha}(\\p{Alnum}+)*)")+
                        "("+TupleRegEx+"+)";

    public static String addSpace(String str){
        return "\\p{Space}*"+str+"\\p{Space}*";
    }

    public static String addParen(String str){
        return addSpace("\\(")+str+addSpace("\\)");
    }
}
