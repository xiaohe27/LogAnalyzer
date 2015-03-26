Start=$(date +%s%N | cut -b1-13)

grep "insert[ \n\t]*(.*)" test.log

End=$(date +%s%N | cut -b1-13)
Diff=$(( $End - $Start ))
echo "\nIt took $Diff milliseconds to read all the insert events in the log" 
