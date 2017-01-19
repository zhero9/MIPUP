# MInimum Perfect Unmixed Phylogeny v0.1 
**For questions or problems with this code, contact [edinehusic@gmail.com](mailto:edinehusic@gmail.com),
or open an ISSUE on github.**

This repository contains implementations of the two Integer Linear Programs from a paper to **appear** 
and implementations of one greedy and one local search heuristics which are not mentioned in the paper and have no constant
approximation guaranties.

## 1. Background and previous results
The problems addresed are **Minimum Conflict-Free Row Split problems**. They were originaly proposed
by *I. Hajirasouliha, B. Raphael, Reconstructing Mutational 
History in Multiply Sampled Tumors Using Perfect Phylogeny Mixtures. 
WABI 2014: 354-367 doi:[10.1007/978-3-662-44753-6_27](http://dx.doi.org/10.1007/978-3-662-44753-6_27)*.
Later, NP-hardness of the problems was confirmed by
Hujdurovic, A., Kacar, U., Milanic, M., Ries, B., and Tomescu, A. I. (2016).
Complexity and algorithms for finding a perfect phylogeny from mixed tumor
samples. IEEE/ACM Transactions on Computational Biology and Bioinformatics.
Available online, DOI: [10.1109/TCBB.2016.2606620](http://ieeexplore.ieee.org/document/7589999/).

A shorter version of the last paper appeared in the Proceedings of WABI 2015:

*Ademir Hujdurović, Urša Kačar, Martin Milanič, Bernard Ries, and Alexandru I. Tomescu, Finding a Perfect
Phylogeny from Mixed Tumor Samples. WABI 2015, LNCS 9289, pp. 80-92, extended version available at 
[http://arxiv.org/abs/1506.07675](http://arxiv.org/abs/1506.07675).*

The above papers also proposed heuristics and gave an exact algorithm for a particular class of inputs. 
The implementation is available at https://github.com/alexandrutomescu/MixedPerfectPhylogeny.
Slides and a visual description of the these results are available 
[**here**](https://www.cs.helsinki.fi/u/tomescu/perfect-phylogeny-tumors.pdf).

## 2. Our approach
We consider two problems:
- split each row of a given binary matrix into a bitwise *OR* of a set of rows so that the resulting matrix corresponds to a perfect phylogeny and has the minimum number of rows among all matrices with this property. **(MCRS)**
- split each row as above, but the task is to minimize the number of distinct rows of the resulting matrix. **(MCDRS)**

We gave new formulations of the two problems, showing that the problems are equivalent to 
two optimization problems on branchings in a derived directed acyclic graph. 
Building on these formulations, we can model the two problems with simple polynomially-sized integer
linear programs. This git repository contains implementation of the results. 
As an input we take a matrix, construct corresponding digraph and consequently ILP formulations. 
ILP are solved using [IBM CPLEX](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/).

## 3. Input formats
We give two different ways of input. The first type is the classical binary matrix as described in problem and the other one is a transpose of a matrix containing the measured value of mutations and a threshold.
### 3.1 {0,1} matrices
The input for the problem is a **m**x**n** binary matrix in .csv format separeted by ";". 
The first column must contain the row (i.e., sample) names,
and the first row must contain the column (i.e., mutation location) names. 
For example, the following table *test1.csv*

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

and saved as name_of_input.csv 
 
### 3.2 Transpose matrix of single nucleotide variants (SNVs) and a threshold
The input for the problem is a **n**x**m** real valued matrix in .txt format, **tab** separeted. Since this is the transpose of matrix we are really intereseted, the first row in fact will contain rows of matrix we want to split, and the first several columns contain the names and description of  mutations/columns (after transpose). 
For example, the following table


|chrom	|pos		|DESC	|normal	|a		|b		|c		|d    |
|-------|---------------|-------|-------|---------------|---------------|---------------|-----|
|chr1	|13806323	|PDPN	|0	|0.00274	|0.000209	|0.000238	|0.213|
|chr1	|33336351	|ADC	|0	|0.0000209	|0.0000778	|0.00014	|0.172|
|chr1	|112100092	|DDX20	|0	|0.36		|0.417		|0.263		|0.714|
|chr1	|152585526	|ATP8B2	|0	|0.412		|0.324		|0.475		|0.328|
|chr1	|165363067	|DUSP27	|0	|0.262		|0.342		|0.359		|0.256|
|chr1	|220962389	|C1orf58|0	|0.242		|0.164		|0.308		|0.104|
|chr10	|1043055	|GTPBP4	|0	|0.251		|0.263		|0.246		|0.277|

is encoded as 


	#chrom	pos	DESC	normal	a	b	c	d
	chr1	13806323	PDPN	0	0.00274	0.000209	0.000238	0.213
	chr1	33336351	ADC	0	0.0000209	0.0000778	0.00014	0.172
	chr1	112100092	DDX20	0	0.36	0.417	0.263	0.714
	chr1	152585526	ATP8B2	0	0.412	0.324	0.475	0.328
	chr1	165363067	DUSP27	0	0.262	0.342	0.359	0.256
	chr1	220962389	C1orf58	0	0.242	0.164	0.308	0.104
	chr10	1043055	GTPBP4	0	0.251	0.263	0.246	0.277

Observe that row containing **normal** is necessary and is used as indicatior of how many first rows correspond to the names and description of mutations. The columns after **normal** contain just measurements. Thogether with above matrix you need to provide a *threshold* which will be used to construct binary matrix with associating value one if a measurment is bigger than threshold and zero otherwise. For a threshold equal to ..., after transposing we obtain a matrix


|	|chr1_13806323  |chr1_33336351  |chr1_112100092|chr1_152585526|chr1_165363067|chr1_220962389|chr10_1043055|
|-------|---------------|---------------|--------------|--------------|--------------|--------------|-------------|
|a	|		|		|	       |	      |	             |		    |		  |
|b	|		|		|	       |	      |	             |		    |		  |
|c	|		|		|	       |	      |	             |		    |		  |
|d	|		|		|	       |	      |	             |		    |		  |

and this is the matrix for which we want to preform row split. This is done inside the program.


## 4 Output
The output of the program for an instance matrix.csv is contained in folder *matrix_rs*.
For each of the two diferent version of problem, the folder contains three files.
- *matrix_algorithm_RS.csv* (Contains optimal conflict-free row split. This matrix has the same .csv format. If a row labeled r is split into k rows in the output matrix, the labels of the resulting rows will be r_1, r_2, ..., r_k.)
- *matrix_algorithm_tree.dot* (Contains perfect phylogenetic tree of the above matrix that can be vizualized with 
	[Graphviz](http://www.graphviz.org/) for example.)
- *matrix_algorithm_columns.csv* (Contains the equalities among mutations and their representation, i.e. sets of mutations 	that appear at same nodes in phylogenetic tree.)
	
The term *algorithm* in the name of the files correspond to either:
- **ip** stands for an optimal solution of MCRS,
- **ipd** stands for an optimail solution of MCDRS.

For the above {0,1} matrix an optimal solution for MCRS produces the following:
#### test1_ip_RS.csv

#### test1_ip_tree.dot visualised by Grapzhiv

#### test1_ip_columns.csv 

For the above real valued matrix with threshold **!!!** an optimal solution for MCRS produces the following:
#### test2_ip_RS.csv

#### test2_ip_tree.dot visualised by Grapzhiv

#### test2_ip_columns.csv

## 5 Running

### 5.1 {0,1} matrices
Navigae to your *rowsplit.jar* executable and call the following code:

	java -jar -Djava.library.path=/path to cplex.jar file (found under the <CPLEX>/lib directory)/ rowsplit.jar path_to_data_file.txt ip

Write **ip** for an optimal solution of MCRS problem or **ipd** for an optimal solution of MCDRS problem.

### 5.2 Transpose matrix of single nucleotide variants (SNVs) and a threshold
Navigae to your *rowsplit.jar* executable and call the following code:

	java -jar -Djava.library.path=/path to cplex.jar file (found under the <CPLEX>/lib directory)/ rowsplit.jar path_to_data_file.txt ip VAF1 t

Write **ip** for an optimal solution of MCRS problem or **ipd** for an optimal solution of MCDRS problem. Indicate **t** as value of threshold.

## 6 Instalation
The program requires full version of IBM CPLEX Optimizer. The free version is bounded for linear programs containing up to 1000 variables and constraints, but it is possible to obtain academic version. The process of obtaning academic version can take more than a week. 

## 7 Results
To appear.
