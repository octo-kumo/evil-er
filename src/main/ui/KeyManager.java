package main.ui;

import main.renderer.Diagram;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyManager implements KeyListener, KeyEventDispatcher {

    public boolean CTRL, SHIFT, ALT;
    private final Diagram diagram;

    public KeyManager(Diagram diagram) {
        this.diagram = diagram;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL -> CTRL = true;
            case KeyEvent.VK_SHIFT -> SHIFT = true;
            case KeyEvent.VK_ALT -> ALT = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL -> CTRL = false;
            case KeyEvent.VK_SHIFT -> SHIFT = false;
            case KeyEvent.VK_ALT -> ALT = false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (!diagram.acceptingKeys) return false;
        switch (e.getID()) {
            case KeyEvent.KEY_PRESSED -> keyPressed(e);
            case KeyEvent.KEY_RELEASED -> keyReleased(e);
            case KeyEvent.KEY_TYPED -> keyTyped(e);
        }
        return false;
    }
}
