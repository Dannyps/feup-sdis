PEER_AP="peer_$1"

# args: peernumber service arg1 arg2

# Launch test 

if [ -z "$3" ] # arg3 is not set
then
    java -cp "bin/" TestApp $PEER_AP $2
    exit
fi

if [ -z "$4" ] # arg4 is not set
then
    java -cp "bin/" TestApp $PEER_AP $2 $3
    exit
fi


# all args are set
java -cp "bin/" TestApp $PEER_AP $2 $3 $4
