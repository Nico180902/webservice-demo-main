package xyz.prohinig.webservice.model;

public class Greeting {

    private final long id;
    private final String content;
    private final String content2 = "content2";

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getContent2() {
        return content2;
    }
}
