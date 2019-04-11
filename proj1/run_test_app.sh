PEER_AP="peer_$1"

# Launch test 
java -cp "bin/" TestApp $PEER_AP "BACKUP" $2 $3
