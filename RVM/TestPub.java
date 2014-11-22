import rvm.LogEntry;
import rvm.PubRuntimeMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestPub {
	public TestPub(){
	}

	public static void approve(int report, int time){
		PubRuntimeMonitor.approveEvent(report, time);	
	}


	public static void publish(int report, int time){

        PubRuntimeMonitor.publishEvent(report, time);
	}

	public static void main(String[] args){
		File logFile=new File("Pub.log");
        try {
            Scanner scan=new Scanner(logFile);

            while (scan.hasNextLine()){
                String line=scan.nextLine();
//                System.out.println("The cur line is "+line+"!");
                if(line.trim().equals(""))
                    break;
                //by comparing the list of args of list of types,
                //we will know which arg has what type. Types of each field for
                //every event can be obtained from the sig file (gen a map of
                // string(event name) to list(type list of the tuple)).
                LogEntry logEntry=getLogEntry(line);

                switch (logEntry.getEventName()){
                    case "approve":
                        for (int i=0; i<logEntry.getArgList().size(); i++) {
                            PubRuntimeMonitor.approveEvent(
                                    logEntry.getArgList().get(i).getReportID(),
                                    logEntry.getTime()
                            );

                            System.out.println("approve "+
                                    logEntry.getArgList().get(i).getReportID());
                        }

                        break;

                    case "publish":
                        for (int i=0; i<logEntry.getArgList().size(); i++) {
                            PubRuntimeMonitor.publishEvent(
                                    logEntry.getArgList().get(i).getReportID(),
                                    logEntry.getTime()
                            );
                            System.out.println("publish "+
                                    logEntry.getArgList().get(i).getReportID());
                        }

                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}

    private static LogEntry getLogEntry(String line) {
        Scanner scan=new Scanner(line).useDelimiter(" ");
        long time=0;
        String eventName="";
        List<LogEntry.EventArg> eventArgs=new ArrayList<LogEntry.EventArg>();

            String nxt=scan.next();
            if(nxt.startsWith("@")){
                time=Long.parseLong(nxt.replace("@",""));
            }

        eventName=scan.next();

        while(scan.hasNext("\\(\\p{Alnum}*\\)")) {
            String curTuple = scan.next("\\(\\p{Alnum}*\\)");
             eventArgs.add( new LogEntry.EventArg
                    (Integer.parseInt(curTuple.replace("(", "").replace(")", ""))));

        }

//        System.out.println("event is "+eventName+" and time is "+time);
//        System.out.println(eventArgs.size()+" is the num of args");
        return new LogEntry(time,eventName,eventArgs);
    }

}
