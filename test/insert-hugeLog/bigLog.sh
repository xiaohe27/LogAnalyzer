Start=$(date +"%s")
echo "Start time : $Start" > time.txt

./csv2log 4 /home/xiaohe/SW/offline-log-analysis/ldcc.csv | sh logAnalyzer-2args.sh > insert2HugeLog.txt

End=$(date +"%s")
echo "Finish time : $End" >> time.txt
Diff=$(( $End - $Start ))
echo "\nIt took $Diff seconds to analyze the insert property of the huge log ldcc.csv" >> time.txt
