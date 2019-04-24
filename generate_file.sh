if [[ $# -ne 2 ]]; then
	echo "Usage: $0 <size in KB> <filename>"
	echo "Files are placed in a 'test' folder"
	exit 1
fi

# create folder for containing demo files
mkdir -p test

# generate random file
head -c "$1KB" </dev/urandom >"test/$2"
