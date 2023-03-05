#!/bin/sh

# Case 1

proc=2; prop=100000; vs=10; ds=20; t=60; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 2

proc=2; prop=100000; vs=10; ds=20; t=300; c=0;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 3

proc=15; prop=10000; vs=5; ds=10; t=30; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 4

proc=15; prop=10000; vs=30; ds=200; t=60; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 5

proc=2; prop=10000; vs=500; ds=1024; t=180; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 6

proc=30; prop=100000; vs=10; ds=50; t=300; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 7

proc=30; prop=100; vs=50; ds=500; t=120; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 8

proc=70; prop=10000; vs=5; ds=10; t=30; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 9

proc=70; prop=100; vs=300; ds=350; t=90; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 10

proc=100; prop=10000; vs=3; ds=5; t=60; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 11

proc=100; prop=100; vs=50; ds=200; t=60; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 12

proc=100; prop=50; vs=100; ds=500; t=120; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 13

proc=128; prop=1000; vs=5; ds=10; t=90; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 14

proc=128; prop=50; vs=150; ds=500; t=120; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;

# Case 15

proc=128; prop=50; vs=500; ds=1024; t=180; c=1;
echo $"Processes: " $proc;
echo $"VS=" $vs $"DS=" $ds;
echo $"Time: " $t $"s";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;
sleep 10;
