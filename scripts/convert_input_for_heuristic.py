import sys


filename = sys.argv[1] 
vafThreshold = float(sys.argv[2])


with open(filename) as f:
	content = f.readlines()
	f.close()

m = []

# getting sample names
sample_names = []
tokens = content[0].split("\t")
for i in range(4,len(tokens)):
	if tokens[i].strip() != "":
		sample_names.append(tokens[i].strip())

SNV_names = []
# getting SNV names and matrix content
read_first_line = False
for line in content:
	# ignoring hearder line
	if not read_first_line:
		read_first_line = True
		continue

	tokens = line.split("\t")
	SNV_names.append(tokens[0].strip() + ":" + tokens[1].strip())

	row = []
	for i in range(4,len(tokens)):
		if tokens[i].strip() != "":
			entry = float(tokens[i].strip()) 
			if entry >= vafThreshold:
				row.append("1")
			else:
				row.append("0")
	m.append(row)

fout = open(filename[:filename.rfind(".")] + "_converted.csv", "w")

# first row, SNV names
for name in SNV_names:
	fout.write(";" + name)
fout.write("\n")

for i in range(0,len(sample_names)):
	fout.write(sample_names[i])
	for j in range(0,len(SNV_names)):
		fout.write(";" + m[j][i])
	fout.write("\n")

fout.close()