package funny.co.core;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyboardHandler implements EventHandler<KeyEvent> {
    private static final Logger log = Logger.getLogger(KeyboardHandler.class.getName());
    private final Caller caller;
    private final GameController controller;

    public KeyboardHandler(Caller caller, GameController controller) {
        this.caller = caller;
        this.controller = controller;
    }

    @Override
    public void handle(KeyEvent e) {
        log.log(Level.FINE, "{0} key pressed", e.getCode());
        switch (e.getCode()) {
            case LEFT:
                controller.back();
                break;
            case RIGHT:
                controller.forward();
                break;
            case F3:
                break;
            case F4:
                break;
            case ESCAPE:
                log.info("exiting application");
                caller.close();
        }
    }
}
