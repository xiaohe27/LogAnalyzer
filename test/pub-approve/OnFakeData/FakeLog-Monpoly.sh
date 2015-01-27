Out=timeForFakeLog-Monpoly-Siebel.txt
Start=$(date +"%s")
echo "Start time : $Start" > $Out

/home/xiaohe/SW/offline-log-analysis/existingApp/monpoly-1.1.2/monpoly -sig Pub.sig -formula Pub.mfotl -negate -log /home/xiaohe/workspace/DATA/FakeData4TestingPerf/Pub_fake.log >> violationsInFakeLog

End=$(date +"%s")
echo "Finish time : $End" >> $Out
Diff=$(( $End - $Start ))
echo "\nIt took Monpoly (opt) $Diff seconds to find all violations of Pub Property in the 82.7 MB fake pub log file!" >> $Out
