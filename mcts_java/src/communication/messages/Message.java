package communication.messages;

public abstract class Message {    
    protected String name;
    
    protected Message(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return Message length in bytes.
     */
    public abstract long length();
    
    /**
     * 
     * @return Message name.
     */
    public String name() {
        return name;
    }
}
