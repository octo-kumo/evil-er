package main.er;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ERKeyManager implements KeyListener, KeyEventDispatcher {

    private final ERDiagram diagram;
    public boolean CTRL, SHIFT, ALT;

    public ERKeyManager(ERDiagram diagram) {
        this.diagram = diagram;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                CTRL = true;
                break;
            case KeyEvent.VK_SHIFT:
                SHIFT = true;
                break;
            case KeyEvent.VK_ALT:
                diagram.locked.set(ALT = true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                CTRL = false;
                break;
            case KeyEvent.VK_SHIFT:
                SHIFT = false;
                break;
            case KeyEvent.VK_ALT:
                diagram.locked.set(ALT = false);
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (!diagram.acceptingKeys) return false;
        switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
                keyPressed(e);
                break;
            case KeyEvent.KEY_RELEASED:
                keyReleased(e);
                break;
            case KeyEvent.KEY_TYPED:
                keyTyped(e);
                break;
        }
        diagram.repaint();
        return false;
    }
}
