package mcts.distributed.agents;

import communication.Priority;
import communication.messages.MoveMessage;
import java.util.EnumMap;
import java.util.Map;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class JointActionExchangingAgent extends FullMCTSGhostAgent {
    private final Map<GHOST, MoveMessage> receivedMoves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private long totalSimulations = 0;
    private EnumMap<GHOST, MOVE> lastBestMove = Utils.NEUTRAL_GHOSTS_MOVES; /* best move during last message sending */


    public JointActionExchangingAgent(DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);
        hookMoveMessageHandler(receivedMoves);
    }

    private void sendMessages() {
        EnumMap<GHOST,MOVE> currentBestMove = mctree.bestDecisionMove();
        if (!Utils.ghostMovesEqual(currentBestMove,Utils.NEUTRAL_GHOSTS_MOVES)&&!Utils.ghostMovesEqual(lastBestMove, currentBestMove)) {
            lastBestMove = currentBestMove;
            broadcastMoveMessage(Priority.HIGH);
        }
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            totalSimulations++;
        }
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        return getMoveFromMessages(receivedMoves);
    }

    @Override
    public long totalSimulations() {
        return totalSimulations;
    }
}
