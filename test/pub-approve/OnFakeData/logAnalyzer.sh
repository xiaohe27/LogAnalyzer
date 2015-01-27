PC=Siebel

Out="count-$PC-MyLogAnalyzer.txt"

Start=$(date +"%s")
echo "Start time : $Start" > $Out

java -cp /home/xiaohe/Projects/LogAnalyzer/production:$CLASSPATH fsl.uiuc.Main Pub.sig Pub.mfotl /home/xiaohe/workspace/DATA/FakeData4TestingPerf/Pub_fake.log >> myAnalyzer

End=$(date +"%s")
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer $Diff seconds to find the violations among the events in the fake log at $PC using reading byte by byte approach" >> $Out
