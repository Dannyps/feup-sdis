# FEUP SDIS 18/19 - Project 1

## Compiling

The project can be compiled using `ant`. The configuration `build.xml` file is on the root of this directory, therefore `ant` should run on the said directory.

Project documentation, javadoc, can be generated using `ant javadoc`.

Alternatively, the `compile.sh` script can be used.

```
./compile.sh
```

## Running

### Rmiregistry

Run `rmiregistry` before launching the server our the client. You must **run this tool in the `bin` directory.**

### Launching peers

For launching peers, a shell script is provided: `run_peer.sh`. For making the testing purpose easier, all multicast channels and ports are defined as variables in this script (`MC_ADDR`, `MCB_ADDR`, `MCR_ADDR`). Due the lack of enhancements, the protocol version is also defined as variable on this script.

Access point names are also defined automatically based on the peer indentifier. For example, if the peer identifier is `1`, then the access point is `peer_1`. The general format is `peer_<peer id>`.

The only variable parameter is the peer identifier which is specified as an argument.

```
./run_peer.sh 1
```

The example launches a peer whose identifier is `1`.

### Launching test application

Similiarly to peers, in order to launch the test application another shell script is provided: `run_test_app.sh`.

The general syntax for this script is ilustrated below:

```
./run_test_app.sh <peer id> <operation> <oper1> <oper2>
```