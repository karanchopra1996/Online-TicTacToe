#!/bin/sh
# $1: a remote ipname
# $2: ip port
# $3: [auto] // for auto play
java -cp jsch-0.1.54.jar:. OnlineTicTacToe $1 $2 $3
