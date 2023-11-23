#!/bin/bash

# after enter the tmux windos, using Ctrl+b then d to detach the session
tmux new-session -d -s mySession -n myWindow
tmux send-keys -t mySession:myWindow "bash ./start.sh" Enter
tmux attach -t mySession:myWindow
