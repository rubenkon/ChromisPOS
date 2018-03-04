//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) (c) 2015-2016
//    http://www.chromis.co.uk
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.
package uk.chromis.pos.util;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.Timer;

public class AutoLogoff implements ActionListener, AWTEventListener {

    private final static long KEY_EVENTS = AWTEvent.KEY_EVENT_MASK;
    private final static long MOUSE_EVENTS = AWTEvent.MOUSE_MOTION_EVENT_MASK
            + AWTEvent.MOUSE_EVENT_MASK;
    private final static long USER_EVENTS = KEY_EVENTS + MOUSE_EVENTS;

    private Action action = null;
    private Integer period = 10000;

    private final long eventMask;
    private Boolean running = false;
    private Timer LogoffTimer = null;
    private JDialog m_object = null;

    private static AutoLogoff INSTANCE = new AutoLogoff();
    public static Boolean timer = false;

    // create a basic timer instance
    private AutoLogoff() {
        this.eventMask = USER_EVENTS;
    }

    public static AutoLogoff getInstance() {
        if (INSTANCE == null) {
            synchronized (AutoLogoff.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AutoLogoff();
                }
            }
        }
        return INSTANCE;
    }

    /*
     * 
     * Routines to control the timer
     * start() manually starts the timer
     * stop() manually stops the timer
     * restart() manually restart the timer
     * isRunning() returns the state of the timer
     * setTimer(Integer period, action ) set the interval rate of the timer and action event
     * 
     * 
     */
    public void start() {
        if (this.timer && LogoffTimer != null ) {
            this.running = true;
            LogoffTimer.setRepeats(false);
            LogoffTimer.start();
            Toolkit.getDefaultToolkit().addAWTEventListener(this, eventMask);
        }
    }

    public void stop() {
        if (this.timer ) {
            this.running = false;
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            if( LogoffTimer != null ) 
                LogoffTimer.stop();
        }
    }

    // Implement ActionListener for the Timer
    @Override
    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
    }

    // Implement AWTEventListener, all events are dispatched via this
    @Override
    public void eventDispatched(AWTEvent e) {
        restartTimer();
    }

    // returns the timer state
    public boolean isTimerRunning() {
        if (this.timer) {
            return (this.running);
        } else {
            return false;
        }
    }

    // set the timer interval in seconds
    public void setTimer(Integer period, Action action) {
        
        stop();
        LogoffTimer = new Timer(period, action);
        this.timer = true;
    }

    public void restartTimer() {
        if (this.timer && LogoffTimer != null) {
            LogoffTimer.restart();
        }
    }

    public void activateTimer() {
        // System.out.println("activate");
        this.timer = true;
        this.running = true;
        this.start();
        restartTimer();
    }
    
    public void deactivateTimer() {
        // System.out.println("deactivate");
        this.stop();
        this.running = false;
        this.timer = false;
    }

    public void setActiveFrame(JDialog frame) {
        m_object = frame;
    }

    public JDialog getActiveFrame() {
        return m_object;
    }
}
