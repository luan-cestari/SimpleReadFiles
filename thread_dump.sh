#!/bin/bash
for i in {1..10} 
do
	NFILE="$(basename $0).$$.$i.tmp"
	jps | fgrep -v Jps | awk '{ system("jstack -l " $1) }' > $NFILE
done
