package reg;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaohe on 14-11-22.
 */
public class RegHelper {
    public static final int INT_TYPE=0;
    public static final int FLOAT_TYPE=1;
    public static final int DOUBLE_TYPE=2;
    public static final int LONG_TYPE=3;
    public static final int STRING_TYPE=4;

    public static final String Delim4FindingTimeStamp= "[a-zA-Z]";
    public static final String Delim4FindingTupleList = "[a-zA-Z]|@";
    public static final String Delim4FindingEvent= "\\(";

    public static final String FieldRegEx=  addToGroup(addSpace("\\p{Alnum}+"));
    public static final String FieldsRegEx = FieldRegEx + addToGroup(","+FieldRegEx)+"*";

    public static final String TupleRegEx = RegHelper.addRealParen(FieldsRegEx);
    public static final String TupleListRegEx= TupleRegEx+"+";

    public static final String EventName = addToGroup(addSpace("\\p{Alpha}"+"\\p{Alnum}*"));

    public static final String TimeStamp = addToGroup(addSpace("@"+("\\d+")));

    public static final String TableRegEx = addToGroup(EventName + addToGroup(TupleRegEx +"+"));

    public static final String addRealParen(String str) {
        return addToGroup(addSpace("\\(") + str + addSpace("\\)"));
    }

    public static final String addToGroup(String str) {return "("+str+")";}

    public static final String addSpace(String str) {return "\\s*" + str + "\\s*";}

public static void main(String[] args) {
//           String input="publish(7)";
//    System.out.println(input.matches(TableRegEx));
//
//    input="@10publish(7)(22)approve(2)good(3)";
//    System.out.println(input.matches(TimeStamp + addToGroup(TableRegEx + "+")));
//
//    Scanner scan=new Scanner(input);
//    System.out.println("Has next timestamp: " + scan.useDelimiter(EventName).hasNext(TimeStamp));
//    System.out.println(scan.next(TimeStamp)+" is the ts");
//    System.out.println("Has next timestamp: "+scan.useDelimiter(EventName).hasNext(TimeStamp));
//    System.out.println("Has next eventName: "+scan.useDelimiter(TupleRegEx).hasNext(EventName));
//    System.out.println("Next event is "+scan.next(EventName));
//
//    Pattern pat= Pattern.compile(TableRegEx);
//    Matcher matcher=pat.matcher(input);
//    int count=0;
//    while (matcher.find()){
//        count++;
//        int gNum=matcher.groupCount();
//        System.out.println("There are "+gNum+" groups!");
//
//        for (int i=0; i< gNum; i++){
//            System.out.println("Found val "+matcher.group(i));
//        }
//
//    }
    String input=" (a) (b) ";
    System.out.println(input.matches(TupleRegEx+"+"));

    input="(a)";
    System.out.println(input.matches(TupleRegEx));
}
}
