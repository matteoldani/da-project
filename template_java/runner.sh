#!/bin/bash

	for i in {1..126}; do
	./run.sh --id $i --hosts ../example/hosts_stress --output ../example/output/$i.output ../example/configs/broadcast.config & 
	done
