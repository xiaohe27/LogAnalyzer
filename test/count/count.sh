PC=Siebel

Out="count-$PC-MyLogAnalyzer.txt"

Start=$(date +"%s")
echo "Start time : $Start" > $Out

java -cp /home/xiaohe/Projects/LogAnalyzer/production:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl /home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly >> $Out

End=$(date +"%s")
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer $Diff seconds to count all the events in the 9M log ldcc4Monpoly at $PC" >> $Out
