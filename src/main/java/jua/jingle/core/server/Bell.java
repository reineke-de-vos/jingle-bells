package jua.jingle.core.server;

public class Bell {

    public static final long INFINITE_TTL = -1;

    private BellAction action;
    private String name;
    private String value;
    private long ttl;

    public Bell(BellAction action, String name, String value, long ttl) {
        this.action = action;
        this.name = name;
        this.value = value;
        this.ttl = ttl;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public BellAction getAction() {
        return action;
    }

    public long getTtl() {
        return ttl;
    }

}
