#!/bin/bash

Run() {

	DATASET_DIR=$1
	DATASET=$2

	echo "***********************************************************************"
	echo "***********************************************************************"
	echo "**************************${DATASET}************************************"
	echo "***********************************************************************"

	maxVAFAbsent=$3 #lichee
	minVAFPresent=$4 # lichee
	VAF=$5 # MIPUP
	minSupport=$6 # MIPUP
	minClusterSize=$7 #lichee
	minPrivateClusterSize=$8 #lichee
	maxClusterDist=$9 #lichee

	if [ "$minClusterSize" == "nil" ]; then 
		minClusterSize_string=""
	else
		minClusterSize_string="-minClusterSize $minClusterSize"
	fi

	if [ "$minPrivateClusterSize" == "nil" ]; then 
		minPrivateClusterSize_string=""
	else
		minPrivateClusterSize_string="-minPrivateClusterSize $minPrivateClusterSize"
	fi

	if [ "$maxClusterDist" == "nil" ]; then 
		maxClusterDist_string=""
	else
		maxClusterDist_string="-maxClusterDist $maxClusterDist"
	fi

	mkdir -p $DATASET_DIR/lichee
	mkdir -p $DATASET_DIR/heuristic
	mkdir -p $DATASET_DIR/ip
	mkdir -p $DATASET_DIR/ipd

	DATASET_NOEXT="${DATASET%.*}"
	DATASET_EXT="${DATASET##*.}"
	DATASET_FILTERED=${DATASET_NOEXT}_filtered.${DATASET_EXT}
	DATASET_FILTERED_NOEXT="${DATASET_FILTERED%.*}"
	DATASET_CONVERTED=${DATASET_FILTERED_NOEXT}_converted.csv
	DATASET_CONVERTED_NOEXT="${DATASET_CONVERTED%.*}"

	# lichee
	echo "*** Running: java -jar $BIN_DIR/lichee.jar -build -n 0 -i $DATASET_DIR/$DATASET -showTree 0 -color -dot -maxVAFAbsent $maxVAFAbsent -minVAFPresent $minVAFPresent $minClusterSize_string $minPrivateClusterSize_string $maxClusterDist_string"
	java -jar $BIN_DIR/lichee.jar -build -n 0 -i $DATASET_DIR/$DATASET -showTree 0 -color -dot -maxVAFAbsent $maxVAFAbsent -minVAFPresent $minVAFPresent $minClusterSize_string $minPrivateClusterSize_string $maxClusterDist_string
	dot -Tpdf $DATASET_DIR/$DATASET.dot -O
	rm -r $DATASET_DIR/lichee_dot_img_temp
	rm $DATASET_DIR/$DATASET.dot
	rm $DATASET_DIR/$DATASET.trees.txt
	mv $DATASET_DIR/$DATASET.dot.pdf $DATASET_DIR/lichee/

	# filtering the matrix
	python $BIN_DIR/remove_weak_SNVs.py $DATASET_DIR/$DATASET $VAF $minSupport

	# preparing the input for the heuristic algorithm
	python $BIN_DIR/convert_input_for_heuristic.py $DATASET_DIR/$DATASET_FILTERED $VAF
	# heuristic algorithm, we don't need the minSupport option, the filtering script handles that
	echo "*** Running ./$BIN_DIR/mixedphylogeny -i $DATASET_DIR/$DATASET_CONVERTED -o $DATASET_DIR/$DATASET_CONVERTED.out.csv --heuristic --verbose"
	./$BIN_DIR/mixedphylogeny -i $DATASET_DIR/$DATASET_CONVERTED -o $DATASET_DIR/$DATASET_CONVERTED.out.csv --heuristic --verbose
	mv $DATASET_DIR/$DATASET_CONVERTED.out.csv $DATASET_DIR/heuristic
	mv $DATASET_DIR/$DATASET_CONVERTED.out.csv.dot $DATASET_DIR/heuristic
	dot -Tpdf $DATASET_DIR/heuristic/$DATASET_CONVERTED.out.csv.dot -O

	# ip
	ALGORITHM=ip
	echo "*** Running java -jar -Djava.library.path=$CPLEX_DIR $BIN_DIR/mipup.jar $DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF"
	java -jar -Djava.library.path=$CPLEX_DIR $BIN_DIR/mipup.jar $DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_columns.csv $DATASET_DIR/$ALGORITHM
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_RS.csv $DATASET_DIR/$ALGORITHM
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot $DATASET_DIR/$ALGORITHM
	rm -r $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/
	dot -Tpdf $DATASET_DIR/$ALGORITHM/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot -O

	# ipd
	ALGORITHM=ipd
	echo "*** Running java -jar -Djava.library.path=$CPLEX_DIR $BIN_DIR/mipup.jar $DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF"
	java -jar -Djava.library.path=$CPLEX_DIR $BIN_DIR/mipup.jar $DATASET_DIR/${DATASET_FILTERED} $ALGORITHM VAF1 $VAF
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_columns.csv $DATASET_DIR/$ALGORITHM
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_RS.csv $DATASET_DIR/$ALGORITHM
	mv $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot $DATASET_DIR/$ALGORITHM
	rm -r $DATASET_DIR/${DATASET_FILTERED_NOEXT}_RS/
	dot -Tpdf $DATASET_DIR/$ALGORITHM/${DATASET_FILTERED_NOEXT}_${ALGORITHM}_tree.dot -O

}

#############################
CPLEX_DIR=/Users/tomescu/Applications/IBM/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_osx/
BIN_DIR=bin
#############################

DATASET_DIR=data/myomas
DATASET=MY21.txt
maxVAFAbsent=0.05 #lichee
minVAFPresent=0.06 # lichee
VAF=0.05 # MIPUP
minSupport=2 # MIPUP
minClusterSize="nil" #lichee
minPrivateClusterSize="nil" #lichee
maxClusterDist="nil" #lichee

Run $DATASET_DIR $DATASET $maxVAFAbsent $minVAFPresent $VAF $minSupport $minClusterSize $minPrivateClusterSize $maxClusterDist

