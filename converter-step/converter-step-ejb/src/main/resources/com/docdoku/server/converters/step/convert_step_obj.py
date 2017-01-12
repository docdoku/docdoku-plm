from optparse import OptionParser;
import sys;
import os;

parser = OptionParser();

parser.add_option("-l", "--freeCadLibPath", dest="l", help ="");
parser.add_option("-i", "--inputFile", dest="i", help ="");
parser.add_option("-o", "--outputFile", dest="o", help ="");

(options, args) = parser.parse_args();

freeCadLibPath = options.l;
inputFile = options.i;
outputFile = options.o;

sys.path.append(freeCadLibPath);

import FreeCAD;
import Part, Mesh;

def explodeOBJS():
	if not inputFile or not outputFile:
		sys.exit(2);

	Part.open(inputFile);
	Mesh.export(FreeCAD.ActiveDocument.Objects,outputFile);

if __name__ == "__main__":
	explodeOBJS();
