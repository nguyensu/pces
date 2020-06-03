Example of executing the jar file:
java -jar pces.jar -n pces_rcjs -nsize 4 -npc 2 -tw 10 -nr 10 -ntg 5 -isize 5 -maxtime 14000 -popsize 100 -updatef 5 -maxdepth 7 -showgng 0 -s 1 -b 1 -fitw 0.99 -guided 1

Descriptions of parameters:
-n		: name of the experiment, to be used as the prefix for output files (not need to change for now)
-nsize		: neighbourhood size (not need in this case)
-npc 		: number of components for PCA (default = 2)
-tw		: time window (default = 10)
-nr 		: number of rules (default = 10)
-ntg		: number of training instances per generation (default = 5)
-isize		: size of intermediate population (multiple of the popsize, default = 5)
-maxtime	: maximum running time (in second, default = 14000)
-popsize	: population size (default = 100)
-updatef	: number of generations before validation is run (default = 5)
-maxdepth	: maximum depth of GP trees (default = 7)
-showgng	: for visualisation (default = 0)
-s		: random seed
-b		: number of random seed per run (will be seed, seed+1..., seed+b)
-fitw		: fitness weight (between generational fitness and gng approximation, default = 0.99)
-guided		: guided genetic operations (default = 1)

The \src folder (including the training instances) need to be in the same folder with the jar file.

Experiments to run (with default values above if not stated otherwise):

- 30 seeds (1 to 30) for -guided = 0
- 30 seeds (1 to 30) for -fitw = 0.5, 0.7
- 30 seeds (1 to 30) for -popsize = 100, 200 and -maxtime = 10 hours
- 30 seeds (1 to 30) for -nr = 5, 10, 20 and -maxtime = 10 hours
