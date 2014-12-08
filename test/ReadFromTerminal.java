import java.io.BufferedReader;
import java.io.InputStreamReader;

class ReadFromTerminal{
public static void main(String[] args){
InputStreamReader isReader = new InputStreamReader(System.in);
BufferedReader bufReader = new BufferedReader(isReader);
while(true){
    try {
        String inputStr = null;
        if((inputStr=bufReader.readLine()) != null) {
            System.out.println("Input is "+inputStr);
        }
        else {
           break;
        }
    }
    catch (Exception e) {
       
    }
}
}
}
