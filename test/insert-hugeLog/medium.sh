Start=$(date +"%s")
echo "Start time : $Start" > time.txt

sh logAnalyzer-3args.sh /home/xiaohe/SW/offline-log-analysis/ldcc4Monpoly > insertTo9M_LinesLog.txt

End=$(date +"%s")
echo "Finish time : $End" >> time.txt
Diff=$(( $End - $Start ))
echo "\nIt took $Diff seconds to analyze the insert property of the 9M lines log ldcc4Monpoly" >> time.txt
