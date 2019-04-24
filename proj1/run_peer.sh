# Define addresses and ports for multicast channels
MC_ADDR="225.0.0.1:1234"
MCB_ADDR="225.0.0.1:2345"
MCR_ADDR="225.0.0.1:3456"

# Define other parameters
PROTOCOL_VERSION="1.0"

if [ -z "$1" ]
then
    PEER_AP="peer_1"
	PEER_ID=1
else
    PEER_AP="peer_$1"
	PEER_ID=$1
fi

# Launch peer
ARG_LIST="$PROTOCOL_VERSION $PEER_ID $PEER_AP $MC_ADDR $MCB_ADDR $MCR_ADDR"
java -cp "bin/" Shared.Peer $ARG_LIST
