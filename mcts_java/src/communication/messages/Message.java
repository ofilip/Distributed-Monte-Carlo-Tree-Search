package communication.messages;

public abstract class Message {    
    protected String name;
    
    protected Message(String name) {
        this.name = name;
    }
    
    public abstract long length();
    public String name() {
        return name;
    }
}
