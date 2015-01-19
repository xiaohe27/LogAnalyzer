PC=Siebel

Out="count-$PC-MyLogAnalyzer-fromZip.txt"

Start=$(($(date +%s%N)/1000000))
echo "Start time : $Start" > $Out

java -cp /home/xiaohe/Projects/LogAnalyzer/production:/home/xiaohe/.m2/repository/org/apache/commons/commons-compress/1.3/commons-compress-1.3.jar:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl /home/xiaohe/SW/offline-log-analysis/LOG-DATA/ldcc4Monpoly.tar.gz >> $Out

End=$(($(date +%s%N)/1000000))
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer $Diff seconds to find the single violation among the events in 9M log ldcc4Monpoly in the format of .tar.gz at $PC" >> $Out
