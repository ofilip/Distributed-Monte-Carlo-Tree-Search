package mcts;

public enum DecisionNeededFrom {
    NOBODY,
    PACMAN_ONLY,
    GHOSTS_ONLY,
    BOTH;
    
    public static DecisionNeededFrom get(boolean pacman_decision, boolean ghosts_decision) {
        if (pacman_decision) {
            if (ghosts_decision) {
                return BOTH;
            } else {
                return PACMAN_ONLY;
            }
        } else {
            if (ghosts_decision) {
                return GHOSTS_ONLY;
            } else {
                return NOBODY;
            }
        }
    }
}
