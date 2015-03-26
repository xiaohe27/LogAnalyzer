PC=Idea-Pad

Out="count-$PC-MyLogAnalyzer.txt"

Start=$(date +"%s")
echo "Start time : $Start" > $Out

java -cp /home/xiaohe/UIUC-WorkSpace/LogAnalyzer/out/production/LogAnalyzer:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl /home/xiaohe/UIUC-WorkSpace/DATA/ldcc4Monpoly >> $Out

End=$(date +"%s")
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer $Diff seconds to count all the events in the 9M log ldcc4Monpoly at $PC using reading byte by byte approach" >> $Out
