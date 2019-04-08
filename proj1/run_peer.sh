# Define addresses and ports for multicast channels
MC_ADDR="224.0.0.1:8001"
MCB_ADDR="224.0.0.1:8002"
MCR_ADDR="224.0.0.1:8003"

# Define other parameters
PROTOCOL_VERSION="1.0"
PEER_ID=1 # todo, allow incremental peer launching

if [ -z "$1" ]
then
    PEER_AP="peer_1"
else
    PEER_AP="peer_$1"
fi

# Launch peer
ARG_LIST="$PROTOCOL_VERSION $PEER_ID $PEER_AP $MC_ADDR $MCB_ADDR $MCR_ADDR"
java -cp "bin/" Peer $ARG_LIST
