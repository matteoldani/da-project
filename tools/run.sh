#! /bin/sh
# setopt localoptions rmstarsilent

rm -f logs/*
../template_java/cleanup.sh
../template_java/build.sh

echo "Number of process: "$1
echo "Number of proposals: "$2
echo "Max number in proposal: "$3
echo "Max distinct numbers: "$4
echo "Execution time: "$5

python3 lattice-performance.py agreement -r ../template_java/run.sh -l logs/ -p $1 -n $2 -v $3 -d $4 -t $5

sleep 10
cat logs/*.stderr
wc -l logs/*.output
python3 lattice_validater.py logs logs $1 $2



