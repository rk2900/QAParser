# This tool is to transmit DOT file into PNG data of parse tree

import os
import sys

fileList = os.listdir("./");
print fileList

for f in fileList:
	a,b = os.path.splitext(f)
	if(b == ".dot"):
		cmd = "dot -Tpng ./"+f+" > ./"+a+".png"
		os.popen(cmd)