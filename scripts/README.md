## remove_weak_SSNVs.py

This Python script removes those SSNVs whose binary presence/absence pattern in the binaary matrix appears stricly less than *k* times. That is, there are strictly less than *k-1* other SSNVs having the same binary presence/absence pattern in the binary matrix. Run as:

	python remove_weak_SSNVs.py filename vafThreshold k
  
where **filename** is the input file, **vafThreshold** is the threshold for converting VAF values into binary ones (e.g., 0.05) and **k** is as above.

## experiments.sh
Contains all commands and parameters used for the experiments presented in the paper. Refer to the [data](https://github.com/zhero9/MIPUP/tree/master/data) directory for the data. Before running, update the DIR variables with the paths where your data and programs are located. The MY21 dataset is not public, but you can see the running parameters from the **experiments-MY21.sh** script.

## convert_input_for_heuristic.py
You probably don't need to bother with this, unless wanting to reproduce the results presented in the paper. This is a Python script converting the input matrices into transposed binary ones, accepted by the heuristic algorithm for the MCRS problem from [here](https://github.com/alexandrutomescu/MixedPerfectPhylogeny). Run as: 

	python convert_input_for_heuristic.py filename vafThreshold
  
where **filename** is the file you are converting and **vafThreshold** is the threshold for converting VAF values into binary ones (e.g., 0.05).



