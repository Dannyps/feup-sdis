public enum MessageType {
    PUTCHUNK,
    STORED,
    GETCHUNK,
    CHUNK,
    DELETE,
    REMOVED;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}