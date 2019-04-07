package utils;

public final class Hash {
    /**
     * Builds an hexadecimal string representation of the file hash
     * @return File identifier in hexadecimal format
     */
    public static String getHexHash(byte[] b) {
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(0xff & b[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private Hash() {};
}