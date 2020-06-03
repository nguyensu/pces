#!/bin/bash
#PBS -P l12
#PBS -q normal
#PBS -l walltime=01:00:00
#PBS -l mem=4GB
#PBS -l jobfs=2GB
#PBS -l ncpus=1
## For licensed software, you have to specify it to get the job running. For unlicensed software, you should also specify it to help us analyse the software usage on our system.
#PBS -l software=pces
## The job will be executed from current working directory instead of home.
#PBS -l wd
module load java/jdk1.8.0_60
echo ${name}NS${ns}NPC${npc}BC${bc}TW${tw}PI${pi}NR${nr}NTG${ntg}SIP${sip}MT${mt}PS${ps}SEED${seed}.allout
java -jar pces.jar -n $name  -nsize $ns -npc $npc -bc $bc -tw $tw -pi $pi -nr $nr -ntg $ntg -isize $sip -maxtime $mt -popsize $ps  -showgng 0 -s $seed -b 1 &> ${name}NS${ns}NPC${npc}BC${bc}TW${tw}PI${pi}NR${nr}NTG${ntg}SIP${sip}MT${mt}PS${ps}SEED${seed}.allout
# echo test > output.out
