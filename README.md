# MIPUP: MInimum Perfect Unmixed Phylogeny v1.0 
**For questions or problems with this code, contact [edinehusic@gmail.com](mailto:edinehusic@gmail.com),
or open an [ISSUE](https://github.com/zhero9/MIPUP/issues) on github.**

This repository contains implementations of the two Integer Linear Programs from 

*A. Hujdurović, E. Husić, M. Mehine, M. Milanič, R. Rizzi and A.I. Tomescu (2017). MIPUP: Minimum perfect unmixed phylogenies via branchings in graphs and ILP, Submitted*

It also contains implementations of one greedy heuristic, and one local search heuristic (without any constant approximation guarantees) that are not mentioned in the paper.

## 1. Background and previous results
The problems addresed are **Minimum Conflict-Free Row Split problems**. They were originaly proposed
by *I. Hajirasouliha, B. Raphael, [Reconstructing Mutational 
History in Multiply Sampled Tumors Using Perfect Phylogeny Mixtures](http://dx.doi.org/10.1007/978-3-662-44753-6_27). 
WABI 2014: 354-367*. Later, NP-hardness of the problems was confirmed by
*A. Hujdurović, U. Kačar, M. Milanič, B. Ries and A.I. Tomescu (2016).
[Complexity and algorithms for finding a perfect phylogeny from mixed tumor
samples](http://ieeexplore.ieee.org/document/7589999/). IEEE/ACM Transactions on Computational Biology and Bioinformatics.* This paper also proposed [a heuristic and an exact algorithm for a particular class of inputs](https://github.com/alexandrutomescu/MixedPerfectPhylogeny). Slides and a visual description of the these results are available [**here**](https://www.cs.helsinki.fi/u/tomescu/perfect-phylogeny-tumors.pdf).

## 2. Our approach
We consider two problems:
- split each row of a given binary matrix into a bitwise *OR* of a set of rows so that the resulting matrix corresponds to a perfect phylogeny and has the minimum number of rows among all matrices with this property. **(MCRS)**
- split each row as above, but the task is to minimize the number of distinct rows of the resulting matrix. **(MCDRS)**

We gave new formulations of the two problems, showing that the problems are equivalent to 
two optimization problems on branchings in a derived directed acyclic graph. 
Building on these formulations, we can model the two problems with simple polynomially-sized integer
linear programs. This git repository contains implementation of the results. 
As an input we take a matrix, construct corresponding digraph, and consequently the ILP formulations. 
ILP are solved using [IBM CPLEX](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/).

## 3. Input formats
We allow for two types of input. The first type is the classical binary matrix as described in the problem formulation. The other one is a transpose of a matrix containing the variant allele frequency of every SSNV in every sample and a threshold for transforming these into binary values. This format is the same used by tool [LICHeE](https://github.com/viq854/lichee).

### 3.1. {0,1} matrices
This input is an **m** x **n** binary matrix in .csv format separeted by ";". 
The first column must contain the row (i.e., sample) names,
and the first row must contain the column (i.e., mutation location) names. 
For example, the following table *matrix.csv*

|   | c1| c2| c3| c4| c5| c3'|
|---|---|---|---|---|---|----|
| r1|  1|  0|  0|  0|  0|   0|
| r2|  1|  1|  1|  1|  0|   1|
| r3|  0|  1|  0|  1|  0|   0|
| r4|  0|  1|  1|  0|  1|   1|
| r5|  0|  1|  0|  0|  0|   0|

is encoded as:

	;c1;c2;c3;c4;c5;c3'
	r1;1;0;0;0;0;0
	r2;1;1;1;1;0;1
	r3;0;1;0;1;0;0
	r4;0;1;1;0;1;1
	r5;0;1;0;0;0;0
 
### 3.2. Transpose matrix of variant allele frequencies (VAFs) of the mutations (SSNVs) and a threshold
This input is an **n** x **m** real-valued matrix in .txt format, **tab** separeted. Since this is the transpose of the matrix we are really interested, the first row will in fact contain the names of the samples, and the first several columns contain the names and description of  mutations. For example, the following table *matrix.txt*


|chrom	|pos		|DESC	|normal	|a		|b		|c		|d    |
|-------|---------------|-------|-------|---------------|---------------|---------------|-----|
|chr1	|13806323	|PDPN	|0	|0.00274	|0.000209	|0.000238	|0.213|
|chr1	|33336351	|ADC	|0	|0.0000209	|0.0000778	|0.00014	|0.172|
|chr1	|152585526	|ATP8B2	|0	|0.412		|0.324		|0.475		|0.328|
|chr1	|165363067	|DUSP27	|0	|0.262		|0.342		|0.359		|0.256|
|chr1	|220962389	|C1orf58|0	|0.242		|0.164		|0.308		|0.104|
|chr10	|1043055	|GTPBP4	|0	|0.251		|0.263		|0.246		|0.277|

is encoded as 


	#chrom	pos	DESC	normal	a	b	c	d
	chr1	13806323	PDPN	0	0.00274	0.000209	0.000238	0.000213
	chr1	33336351	ADC	0	0.0000209	0.0000778	0.00014	0.172
	chr1	152585526	ATP8B2	0	0.412	0.324	0.475	0.000328
	chr1	165363067	DUSP27	0	0.262	0.342	0.000359	0.256
	chr1	220962389	C1orf58	0	0.000242	0.164	0.308	0.104
	chr10	1043055	GTPBP4		0	0.00251	0.000263	0.246	0.277

Observe that the column **normal** filled only with **0** is necessary. The columns after **normal** contain the VAF values in each sample. You also need to provide a *threshold* which will be used to transform this matrix into a binary one, according to the rule:
- if VAF value >= threshold, then we get a **1**, 
- otherwise, we get a **0**. 

For a threshold equal to **0.003**, after transposing, we obtain the following matrix


|	|chr1_13..  |chr1_33..  |chr1_15..|chr1_16..|chr1_22..|chr10_10..|
|-------|---------------|---------------|--------------|--------------|--------------|-------------|
|a	|1		|0		|1	       |1	      |0	     |	1	   |
|b	|0		|0		|1	       |	1     |	1	     |		0  |
|c	|0		|1		|1	       |	0     |		1    |		 1 |
|d	|1		|1		| 0	       |	1     |	 	1    |	1	   |

and this is the matrix for which we want to preform the optimal row split. This is done inside the program.


## 4. Output
The output for an instance **matrix.csv** or **matrix.txt** is contained in the folder **matrix_RS**, and consists of:
- *matrix_algorithm_RS.csv*: Contains the optimal conflict-free row split matrix, in the binary .csv format. If a row labeled r is split into k rows in the output matrix, the labels of the resulting rows will be r_1, r_2, ..., r_k.
- *matrix_algorithm_tree.dot*: Contains the perfect phylogenetic tree of the above conflict-free matrix, in dot format. In can be vizualized with [Graphviz](http://www.graphviz.org/) for example.
- *matrix_algorithm_columns.csv*: Contains the SSNV names having the same binary profile in all the samples.
	
The term *algorithm* in the name of the files corresponds to either:
- **ip**: an optimal solution of the MCRS problem,
- **ipd**: an optimail solution of the MCDRS problem.

The edges of the phylogenetic tree from the .dot file are labeled with:
- for binary inputs: **S|n**, where **S** (a letter **A,B,C,...**) is the name given by MIPUP to the group of mutations having the same binary profile in all the samples, and **n** is the number of mutations in this group. This group of mutations occurred on this edge of the phylogenetic tree. The mutations corresponding to **S** can be found in *matrix_algorithm_columns.csv*. 
- for VAF format: **S|n|mean -+ std**, where **S|n** is as above, and **mean** is the mean of the VAFs in this group and **std** is the standard deviation of the VAFs. 

For the above {0,1} matrix an optimal solution for MCRS produces the following:
#### matrix_ip_RS.csv
	;c1;c2;c3;c4;c5;c3'
	r1;1;0;0;0;0;0
	r2_1;1;0;0;0;0;0
	r2_2;0;1;1;0;0;1
	r2_3;0;1;0;1;0;0
	r3;0;1;0;1;0;0
	r4;0;1;1;0;1;1
	r5;0;1;0;0;0;0

#### matrix_ip_tree.dot visualised by Grapzhiv

<img src="https://github.com/zhero9/MIPUP/blob/master/ExamplesReadMe/matrix_RS/matrix_ip_tree.png" width="400px" height="450px" />

#### matrix_ip_columns.csv 

	Folowing mutations-columns are equal.
	A;c1
	B;c2
	C;c3;c3'
	D;c4
	E;c5

For the above real-valued matrix with threshold **0.003** an optimal solution for MCDRS produces the following:
#### matrixVAF_ipd_RS.csv
	;chr1:13806323;chr1:33336351;chr1:152585526;chr1:165363067;chr1:220962389;chr10:1043055
	a_1;0;0;1;0;0;0
	a_2;0;0;0;1;0;0
	b_1;0;0;1;0;0;0
	b_2;0;0;0;1;0;0
	b_3;0;0;0;0;1;0
	c_1;0;0;1;0;0;0
	c_2;0;0;0;0;1;1
	d_1;0;1;0;1;0;0
	d_2;0;0;0;0;1;1

#### matrixVAF_ipd_tree.dot visualised by Grapzhiv
<img src="https://github.com/zhero9/MIPUP/blob/master/ExamplesReadMe/matrixVAF_RS/matrixVAF_ipd_tree-1.png"  width="450px" height="450px" />
#### matrixVAF_ipd_columns.csv

	Folowing mutations-columns are equal.
	A;chr1:13806323
	B;chr1:33336351
	C;chr1:152585526
	D;chr1:165363067
	E;chr1:220962389
	F;chr10:1043055

## 5. Running

### 5.1. {0,1} matrices
Navigate to your *rowsplit.jar* executable and call the following code from the command line:

	java -jar -Djava.library.path=/path_to_cplex.jar_file_(found under the <CPLEX>/lib directory)/ rowsplit.jar path_to_data_file.csv ip

**path_to_data_file.csv** is the input file. Argument **-Djava.library.path** can be something like **/Users/tomescu/Applications/IBM/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_osx/**. Write **ip** for an optimal solution to the MCRS problem or **ipd** for an optimal solution to the MCDRS problem.

### 5.2. Transpose matrix of variant allele frequencies (VAFs) of the mutations (SSNVs) and a threshold
Navigate to your *rowsplit.jar* executable and call the following code from the command line:

	java -jar -Djava.library.path=/path_to_cplex.jar_file (found under the <CPLEX>/lib directory)/ rowsplit.jar path_to_data_file.txt ip VAF1 t

**path_to_data_file.txt** is the input file and value **t** is the threshold. Write **ip** for an optimal solution to the MCRS problem or **ipd** for an optimal solution to the MCDRS problem. 

## 6. Instalation
MIPUP requires the full version of the IBM CPLEX Optimizer and Java. The free version is bounded for linear programs containing up to 1000 variables and constraints. Note that it is possible to obtain the full academic version (this can take more than a week). 

## 7. Results
To appear.
