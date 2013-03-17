package communication.exceptions;

import mcts.exceptions.*;

public class UnknownChannelException extends Exception {
    public UnknownChannelException() { super(); }
    public UnknownChannelException(String message) { super(message); }
    public UnknownChannelException(Throwable cause) { super(cause); }
    public UnknownChannelException(String message, Throwable cause) { super(message, cause); }
    
    
}
