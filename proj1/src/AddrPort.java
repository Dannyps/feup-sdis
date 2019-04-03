import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

class AddrPort {
    String address = "";
    int port = 0;

    public AddrPort(String a, int p) {
        address = a;
        port = p;
    }

    public AddrPort(String pair) throws Exception {
        String[] n = pair.split(":");

        if (n.length == 2) {
            address = n[0];
            port = Integer.parseInt(n[1]);
        } else
            throw new Exception("String passed was not of the form addr:port");
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    InetAddress getAddressINA() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return address.toString() + ":" + Integer.toString(port);
    }

    public InetSocketAddress getInetSocketAddress(){
        return new InetSocketAddress(address, port);
    }
}