This folder contains the scripts used for running the experiments.

## experiments.sh
Contains all commands and parameters. Before running, update the DIR variables with the paths where your data and programs are located. The MY21 dataset is not public, but one can see the running parameters from the **experiments-MY21.sh** script.

## remove_weak_SSNVs.py

This Python script removes those SSNVs whose binary presence/absence pattern in the binaary matrix appears stricly less than *k* times. That is, there are strictly less than *k-1* other SSNVs having the same binary presence/absence pattern in the binary matrix. Run as:

	python remove_weak_SSNVs.py filename vafThreshold minSupport
  
where **filename** is the input file, **vafThreshold** is the threshold for converting VAF values into binary ones (e.g., 0.05) and **minSupport** (variable *k*) is the number of times the binary pattern of the SSNV must appear in the matrix to be kept.

## convert_input_for_heuristic.py
This is a Python script converint the input matrices into transposed binary ones, accepted by the heuristic algorithm from [here](https://github.com/alexandrutomescu/MixedPerfectPhylogeny). You probably don't need to bother with this, unless wanting to reproduce the results presented in the paper. Run as: 

	python convert_input_for_heuristic.py filename vafThreshold
  
where **filename** is the file you are converting and **vafThreshold** is the threshold for converting VAF values into binary ones (e.g., 0.05).



