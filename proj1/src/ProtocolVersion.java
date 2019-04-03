class ProtocolVersion {
    private String v;

    public ProtocolVersion(String v) throws Exception {
        if(v.length() != 3 || v.charAt(1)!='.')
            throw new Exception("Invalid protocol version passed. It must be of format 'n.m'");

        this.v = v;
    }

    /**
     * @return the v
     */
    public String getV() {
        return v;
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}