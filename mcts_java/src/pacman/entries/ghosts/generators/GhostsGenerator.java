package pacman.entries.ghosts.generators;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class GhostsGenerator implements GhostControllerGenerator {
    private Class controller_class;
    private Constructor controller_constructor;
    private final static Object[] NO_PARAMS = {};
    private final static Class[] NO_SIGNATURE_PARAMS = {};
    
    @SuppressWarnings("unchecked")
    public GhostsGenerator(Class controller_class) throws NoSuchMethodException {
        this.controller_class = controller_class;
        this.controller_constructor = controller_class.getConstructor(NO_SIGNATURE_PARAMS);
        Controller.class.isAssignableFrom(controller_class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        try {
            return (Controller<EnumMap<GHOST, MOVE>>)controller_constructor.newInstance(NO_PARAMS);
        } catch (Exception ex) { ex.printStackTrace(); assert(false); return null; }
    }

    @Override
    public String ghostName() {
        return controller_class.getSimpleName();
    }
}
