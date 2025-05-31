public class Simbolo {
    String token;
    Object bytes;

    public Simbolo(String token, Object bytes) {
        this.token = token;
        this.bytes = bytes;
    }

    public Object getBytes() {
        return bytes;
    }
}
