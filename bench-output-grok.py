
import sys
import re

if len(sys.argv) == 0:
  print "No input file."
  sys.exit(1)

filename = sys.argv[1]
data = filename == '-' and sys.stdin or open(filename, 'r')

grokMode = False
grokker = re.compile(r'\s*(\d+)\s*(\d+)\s*:\s*(\d+) .*')
for line in data.readlines():
  if line.startswith('--------------------------------'):
    grokMode = False
  if grokMode:
    m = grokker.match(line)
    if m:
      print "%s,%s,%s" % (m.group(1), m.group(2), m.group(3))
  if line.startswith('------Warmup\'s over-------------'):
    grokMode = True
    print "threads,connections,iterations"



