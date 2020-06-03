#!/bin/bash
EXPNAME="ENSrcjs"
UF="20"
for seed in 1 #{1..30}
do
for nsize in 4
do
for npc in 2
do
for bc in 8
do
for tw in 50
do
for pi in RCJS_1_3_50_40.txt
do
for nr in 1
do
for ntg in 1
do
for sip in 5
do
for mt in 36000
do
for ps in 2000
do
FILE=EXP${EXPNAME}NS${nsize}NPC${npc}BC${bc}TW${tw}PI${pi}NR${nr}NTG${ntg}SIP${sip}MT${mt}PS${ps}SEED${seed}.out
if [ ! -f "$FILE" ]; then
    echo Experiment ${EXPNAME} NS${nsize} NPC${npc} BC${bc} TW${tw} PI${pi} NR${nr} NTG${ntg} SIP${sip} MT${mt} PS${ps} SEED${seed}
    qsub -v name=$EXPNAME,ns=$nsize,npc=$npc,bc=$bc,tw=$tw,pi=$pi,nr=$nr,ntg=$ntg,sip=$sip,mt=$mt,ps=$ps,seed=$seed,uf=$UF exp_run_gpfjss.sh
fi
done
done
done
done
done
done
done
done
done
done
done
