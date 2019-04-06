# Define addresses and ports for multicast channels
MC_ADDR="localhost:8001"
MCB_ADDR="localhost:8002"
MCR_ADDR="localhost:8003"

# Define other parameters
PROTOCOL_VERSION="1.0"
PEER_ID=1 # todo, allow incremental peer launching
PEER_AP="peer_1"

# Launch peer
ARG_LIST="$MC_ADDR $MCB_ADDR $MCR_ADDR $PROTOCOL_VERSION $PEER_ID $PEER_AP"
java -cp "bin/" Peer $ARG_LIST 
