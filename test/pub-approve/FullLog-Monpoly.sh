Out=timeForCompleteLog-Mon-Siebel.txt
Start=$(date +"%s")
echo "Start time : $Start" > $Out

/home/xiaohe/SW/offline-log-analysis/existingApp/monpoly-1.1.2/monpoly -sig Pub.sig -formula Pub.mfotl -negate -log /home/xiaohe/workspace/DATA/MeasureBaseTime/ldccComplete_MonpolyStyle_addMore >> $Out

End=$(date +"%s")
echo "Finish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took Monpoly (opt) $Diff seconds to find the single violation of Pub Property in the 7.6 GB log file!" >> $Out
