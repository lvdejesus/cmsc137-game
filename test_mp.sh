#!/bin/bash
make run > host.log 2>&1 &
HOST_PID=$!
sleep 2
make run > client.log 2>&1 &
CLIENT_PID=$!
sleep 5
kill $HOST_PID
kill $CLIENT_PID
cat host.log
echo "---"
cat client.log
