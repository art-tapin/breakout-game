/* File: GBall.java
 * ----------------
 * This class defines a GObject class that represents a moving ball in a game 'Breakout'
 * The entity of Ball is also executed in a separate Thread (see run())
 */
package com.shpp.p2p.cs.atiupin.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;

public class GBall extends GOval implements Runnable {

    /* Private instance variables */
    private double r;

    private double enclosureWidth;
    private double enclosureHeight;

    private double startingX;
    private double startingY;

    private double dx;
    private double dy;

    /** Creates a new ball with the use of GOval constructor */
    public GBall(double r) {
        super(r * 2, r * 2);
        this.r = r;
    }

    /**
     * The main logic of the moving ball
     * The behaviour of it is executed in a separate Thread
     */
    @Override
    public void run() {
        setRandomVx();
        while(true) {
            advanceOneTimeStep();
            pause(5);
        }
    }

    /**
     * This method checks balls collision with enclosures and makes the appropriate 'step' depending on that
     * Enclosures are bounds of the canvas
     * Hitting the bottom wall is equals to losing one attempt and restarting from the default point
     */
    private void advanceOneTimeStep() {

        double bx = this.getX();
        double by = this.getY();

        if (bx < 0 || bx > enclosureWidth - 2 * r) {
            changeXOpposite();
        }
        if (by < 0) {
            changeYOpposite();
        }
        move(dx, dy);

        if (by > enclosureHeight + r) {
            setVelocity(0, 0);
            pause(800);
            this.setLocation(startingX, startingY);
        }
    }

    public void changeYOpposite() {
        this.dy = -dy;
    }
    public void changeXOpposite() {
        this.dx = -dx;
    }

    /** Sets the velocity of the ball */
    public void setVelocity(double vx, double vy) {
        this.dx = vx;
        this.dy = vy;
    }

    public void setVY(double vy) {
        this.dy = vy;
    }

    /**
     * Set random initial dx(vx) at start of each game (party)
     */
    public void setRandomVx() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        this.dx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5))
            this.dx = -dx;
    }

    /** Sets the size of the enclosure == canvas' bounds */
    public void setEnclosureSize(double width, double height) {
        enclosureWidth = width;
        enclosureHeight = height;
    }

    public void setStartingCoords(double x, double y) {
        this.startingX = x;
        this.startingY = y;
    }

    public double getSpeedVX() {
        return this.dx;
    }

    public double getSpeedVY() {
        return this.dy;
    }

    /**
     * This is my own settings for the behavior of the ball when it collides with paddle/brick
     * They change the ball's flying angle (--> velocity also) when after colliding
     * This mechanic makes the game slightly random and interesting --> improves gaming experience
     * Every if-statement is a 'lever'
     * The improved end result is reached with the help of these 'levers'
     * They have been established empirically by myself
     */
    public void changeVelocityALittle() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        double positiveCorr = rgen.nextDouble(-0.1, 0.5); // adds more value to the positive side
        double negativeCorr = rgen.nextDouble(-0.5, 0.1); // adds more value to the negative side
        double boostToEscapeEdgeStates = 0.8; // edge states: slow-mo (0 > x < 1) or sonicX: (x > 3)
        double alignMovement = 0.2; // helps to align ball movement by Y-axis

        if (rgen.nextBoolean(0.5)) {
            if (Math.abs(dx) >= 1 && Math.abs(dy) >= 1) {
                if (this.dx > 0 && this.dy > 0) {
                    dx += positiveCorr;
                    dy += negativeCorr;
                }
                if (this.dx < 0 && this.dy < 0) {
                    dx += positiveCorr;
                    dy += negativeCorr;
                }
            }

            if (this.dy > 0 && this.dy < 1) {
                dy += boostToEscapeEdgeStates;
            }
            if (this.dx > 0 && this.dx < 1) {
                dx += boostToEscapeEdgeStates;
            }

            if (Math.abs(this.dx) > 3) {
                if (dx < 0) {
                    dx += boostToEscapeEdgeStates;
                } else {
                    dx += -boostToEscapeEdgeStates;
                }
            }
            if (Math.abs(this.dy) > 3) {
                if (dy < 0) {
                    dy += boostToEscapeEdgeStates;
                } else {
                    dy += -boostToEscapeEdgeStates;
                }
            }

            if (Math.abs(this.dx) > Math.abs(this.dy)) {
                if (dy < 0) {
                    dy += -alignMovement;
                } else {
                    dy += alignMovement;
                }
            }
        }
    }
}