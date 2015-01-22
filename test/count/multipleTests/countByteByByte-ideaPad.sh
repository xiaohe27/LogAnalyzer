PC=IdeaPad

Out="count-$PC-MyLogAnalyzer_$1.txt"

Start=$(date +"%s")
echo "Start time : $Start" > $Out

java -cp /home/xiaohe/UIUC-WorkSpace/LogAnalyzer/out/production/LogAnalyzer:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl /DATA/ldcc4Monpoly_BaseExecTime >> $Out

End=$(date +"%s")
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer $Diff seconds to find the single violation among the events in the 9M log ldcc4Monpoly buggy at $PC using reading byte by byte approach" >> $Out
