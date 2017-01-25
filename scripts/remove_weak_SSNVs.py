import sys


def binary_string_from_line(line):
	tokens = line.split("\t")
	column_string = ""

	for i in range(4,len(tokens)):
		
		current_token = tokens[i].strip()
		if len(current_token) == 0:
			continue

		new_character = ""
		if float(current_token) < vafThreshold:
			new_character = "0"
		else:
			new_character = "1"
		column_string += new_character

	return column_string




filename = sys.argv[1] 
vafThreshold = float(sys.argv[2])
minSupport = float(sys.argv[3])

with open(filename) as f:
	content = f.readlines()
	f.close()

support_of_column = dict()

# going through the rows of he matrix (i.e. SSNVs), and 
# seeing how many times each binary pattern has been observed)

read_first_line = False
for line in content:
	# ignoring the header
	if not read_first_line:
		read_first_line = True
		continue

	column_string = binary_string_from_line(line)

	if column_string in support_of_column:
		support_of_column[column_string] = support_of_column[column_string] + 1
	else:
		support_of_column[column_string] = 0

fout = open(filename[:filename.rfind(".")] + "_filtered.txt", "w")

# printing only those 

read_first_line = False
for line in content:
	# ignoring the header
	if not read_first_line:
		read_first_line = True
		fout.write(line)
		continue
	
	column_string = binary_string_from_line(line)

	if support_of_column[column_string] >= minSupport:
		fout.write(line)

fout.close()