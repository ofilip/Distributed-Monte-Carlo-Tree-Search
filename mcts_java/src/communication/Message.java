package communication;

public interface Message<M extends Enum<M>> {
    public Enum<M> type();
    public long length();
}
