PC=Siebel

Out="count-$PC-MyLogAnalyzer-ExtractThenAnalyze.txt"

Start=$(($(date +%s%N)/1000000))

tar xfz /home/xiaohe/SW/offline-log-analysis/LOG-DATA/ldcc4Monpoly.tar.gz

echo "Start time : $Start" > $Out

java -cp /home/xiaohe/Projects/LogAnalyzer/production:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl ldcc4Monpoly >> $Out

End=$(($(date +%s%N)/1000000))
echo "\nFinish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took my log analyzer totally $Diff seconds to first extract the 9M log file from .tar.gz and then find the single violation among the events at $PC using reading byte by byte approach" >> $Out
