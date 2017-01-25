#!/bin/bash

Run() {
	
	DATASET_DIR=$1
	DATASET=$2

	maxVAFAbsent=$3 #lichee
	minVAFPresent=$4 # lichee
	VAF=$5 # MIPUP
	minSupport=$6 # MIPUP
	minClusterSize=$7 #lichee
	minPrivateClusterSize=$8 #lichee
	maxClusterDist=$9 #lichee

	if [ "$minClusterSize" == "" ]; then 
		minClusterSize_string=""
	else
		minClusterSize_string="-minClusterSize $minClusterSize"
	fi

	if [ "$minPrivateClusterSize" == "" ]; then 
		minPrivateClusterSize_string=""
	else
		minPrivateClusterSize_string="-minPrivateClusterSize $minPrivateClusterSize"
	fi

	if [ "$maxClusterDist" == "" ]; then 
		maxClusterDist_string=""
	else
		maxClusterDist_string="-maxClusterDist $maxClusterDist"
	fi

	mkdir -p data/$DATASET_DIR/lichee
	mkdir -p data/$DATASET_DIR/heuristic
	mkdir -p data/$DATASET_DIR/ip
	mkdir -p data/$DATASET_DIR/ipd

	DATASET_NOEXT="${DATASET%.*}"
	DATASET_EXT="${DATASET##*.}"
	DATASET_FILTERED=${DATASET_NOEXT}_filtered.${DATASET_EXT}
	DATASET_FILTERED_NOEXT="${DATASET_FILTERED%.*}"
	DATASET_CONVERTED=${DATASET_FILTERED_NOEXT}_converted.csv
	DATASET_CONVERTED_NOEXT="${DATASET_CONVERTED%.*}"

	# lichee
	java -jar bin/lichee.jar -build -n 0 -i data/$DATASET_DIR/$DATASET -showTree 0 -color -dot -maxVAFAbsent $maxVAFAbsent -minVAFPresent $minVAFPresent $minClusterSize_string $minPrivateClusterSize_string $maxClusterDist_string
	dot -Tpdf data/$DATASET_DIR/$DATASET.dot -O
	rm -r data/$DATASET_DIR/lichee_dot_img_temp
	rm data/$DATASET_DIR/$DATASET.dot
	rm data/$DATASET_DIR/$DATASET.trees.txt
	mv data/$DATASET_DIR/$DATASET.dot.pdf data/$DATASET_DIR/lichee/

	# filtering the matrix
	python bin/remove_weak_SNVs.py data/$DATASET_DIR/$DATASET $VAF $minSupport

	# preparing the input for the heuristic algorithm
	python bin/convert_input_for_heuristic.py data/$DATASET_DIR/$DATASET_FILTERED $VAF

	# heuristic, we don't need the minSupport option, the filtering script handles that
	./bin/mixedphylogeny -i data/$DATASET_DIR/$DATASET_CONVERTED -o data/$DATASET_DIR/$DATASET_CONVERTED.out.csv --heuristic
	mv data/$DATASET_DIR/$DATASET_CONVERTED.out.csv data/$DATASET_DIR/heuristic
	mv data/$DATASET_DIR/$DATASET_CONVERTED.out.csv.dot data/$DATASET_DIR/heuristic
	dot -Tpdf data/$DATASET_DIR/heuristic/$DATASET_CONVERTED.out.csv.dot -O

	# ip
	ALGORITHM=ip
	java -jar -Djava.library.path=/Users/tomescu/Applications/IBM/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_osx/ bin/mipup.jar data/$DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_columns.csv data/$DATASET_DIR/$ALGORITHM
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_RS.csv data/$DATASET_DIR/$ALGORITHM
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot data/$DATASET_DIR/$ALGORITHM
	rm -r data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/
	dot -Tpdf data/$DATASET_DIR/$ALGORITHM/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot -O

	# ipd
	ALGORITHM=ipd
	java -jar -Djava.library.path=/Users/tomescu/Applications/IBM/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_osx/ bin/mipup.jar data/$DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_columns.csv data/$DATASET_DIR/$ALGORITHM
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_RS.csv data/$DATASET_DIR/$ALGORITHM
	mv data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot data/$DATASET_DIR/$ALGORITHM
	rm -r data/$DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/
	dot -Tpdf data/$DATASET_DIR/$ALGORITHM/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot -O

}

DATASET_DIR=myomas
DATASET=MY21.txt
maxVAFAbsent=0.05 #lichee
minVAFPresent=0.06 # lichee
VAF=0.05 # MIPUP
minSupport=2 # MIPUP
minClusterSize="" #lichee
minPrivateClusterSize="" #lichee
maxClusterDist="" #lichee

Run $DATASET_DIR $DATASET $maxVAFAbsent $minVAFPresent $VAF $minSupport $minClusterSize $minPrivateClusterSize $maxClusterDist

