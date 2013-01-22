package communication;

enum DummyMessageType {
    DUMMY
};

public class DummyMessage implements Message<DummyMessageType> {
    @Override
    public long length() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DummyMessageType type() {
        return DummyMessageType.DUMMY;
    }

}
