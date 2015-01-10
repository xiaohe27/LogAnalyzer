Start=$(date +"%s")
echo "Start time : $Start" > time.txt

java -cp /home/xiaohe/Projects/LogAnalyzer/out/production/LogAnalyzer:$CLASSPATH fsl.uiuc.Main insert.sig insert.fl /home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly 

End=$(date +"%s")
echo "Finish time : $End" >> time.txt
Diff=$(( $End - $Start ))
echo "\nIt took $Diff seconds to read all the events in the 9M log ldcc4Monpoly" >> time.txt
