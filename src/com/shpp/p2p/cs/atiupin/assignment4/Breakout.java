/* File: Breakout.java
 * -------------------
 * This is my version of "Breakout" arcade-game
 *
 * The rules are simple:
 * - If you are ready -> just click the mouse to start the game
 * - Use your mouse to control the paddle - it is your main tool (and luck of course)
 * - The ball will beat off the paddle and fly to the bricks
 * - If ball touches the brick - they disappear!
 * - Try your best and destroy all bricks on the screen!
 * - If you will end with "GAME OVER" :
 *   it is not the end! You have a chance try again - just press ENTER after the defeat!
 *
 * This version of game includes some extended features like:
 * - Music and sound effects
 * - The ability to restart the game after defeat
 * - The ability to exit the game after defeat
 * - My own a little bit complicated levers of ball behaviour (see ball.changeVelocityALittle)
 *
 * Music and audio by Marichka Kvaertie - my lovely girlfriend
 */

package com.shpp.p2p.cs.atiupin.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    public static final int UPPER_BAR_HEIGHT = 33;

    /**
     * Dimensions of game board (usually the same)
     */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT - UPPER_BAR_HEIGHT;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * (Error == "похибка")
     * Is used to give the paddle a bit more left and right spaces for paddle's movement
     */
    int PADDLE_X_ERROR = 2;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;
    private static final int BALL_DIAMETER = BALL_RADIUS * 2;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    private static final int INITIAL_VY = 2;

    /**
     * Paddle and ball colors
     */
    private static final Color PADDLE_COLOR = Color.BLACK;
    private static final Color BALL_COLOR = Color.BLACK;

    /**
     * Starting coordinates of paddle and ball
     */
    int PAD_STARTING_X = WIDTH / 2 - PADDLE_WIDTH / 2;
    int PAD_STARTING_Y = HEIGHT - (PADDLE_Y_OFFSET + PADDLE_HEIGHT);

    int BALL_STARTING_X = WIDTH / 2 - BALL_RADIUS;
    int BALL_STARTING_Y = HEIGHT / 2 - BALL_RADIUS;

    /* Some global variables: */

    int remainingLives = NTURNS; // iteration-variable

    boolean restartGame = false;

    RandomGenerator rgen = RandomGenerator.getInstance();

    /**
     * Label fonts:
     * labelFont1: winning label
     * labelFont2: lives label; restart/escape label; game over label
     */
    Font labelFont1 = new Font ("TimesRoman", Font.BOLD, 40);
    Font labelFont2 = new Font ("TimesRoman", Font.BOLD, 20);
    Font livesFont = new Font ("TimesRoman", Font.PLAIN, 15);

    Color[] bricksColorSet = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN};

    ArrayList<GRect> bricksOnScreen = new ArrayList<>(NBRICK_ROWS * NBRICKS_PER_ROW);

    /**
     * Initialization of:
     * - main game objects with default parameters: background music; paddle; ball;
     * - listeners and theirs behaviour in 'Anonymous style': mouseMotion and keyListener;
     * - game board: building the brick field
     */
    public void init() {
        Music backgroundMusic = new Music();
        backgroundMusic.setFile("assets/music.wav");

        Paddle paddle = new Paddle(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        paddle.setColor(PADDLE_COLOR);

        GBall ball = new GBall(BALL_RADIUS);
        ball.setFilled(true);
        ball.setColor(BALL_COLOR);
        ball.setEnclosureSize(WIDTH, HEIGHT);
        ball.setStartingCoords(BALL_STARTING_X, BALL_STARTING_Y);
        ball.setVY(INITIAL_VY);

        createNewGameBoard(paddle, ball);

        // Initializing listeners and theirs behaviour
        GCanvas canvas = getGCanvas();

        /* MouseMotionListener.mouseMoved is responsible for defining current X coord of the mouse
         * Mouse current X coordinate defines the paddle current X coordinate
         */
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                controlPaddle(e.getX());
            }

            /**
             * A little anonymous subfunction that is responsible for the paddle movement mechanics
             * Left and right bounds are extended a little bit by PADDLE_X_ERROR constant for better gaming experience
             * @param x Current cursor X coordinate
             */
            private void controlPaddle(int x) {
                if (x <= WIDTH - (PADDLE_WIDTH / 2 - PADDLE_X_ERROR) && (x >= PADDLE_WIDTH / 2 - PADDLE_X_ERROR)) {
                    add(paddle, x - (PADDLE_WIDTH / 2.0), PAD_STARTING_Y);
                }
            }
        });


         /* KeyListener.keyTyped is responsible for the restart/exit mechanics after defeat
          * ENTER == restart the game
          * ESCAPE == exit the program
          */
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (remainingLives == 0) {
                    if (e.getKeyChar() == (char) 10) {
                        restartGame = true;
                    }
                    if (e.getKeyChar() == (char) 27) {
                        java.lang.System.exit(0);
                    }
                }
            }
        });

        waitForClick();

        backgroundMusic.playUntilStop();

        new Thread(ball).start(); // ball mechanics execute in a separate Thread

        run(paddle, ball, backgroundMusic);
    }

    /**
     * Creates brick field and places the main objects on the canvas (game board)
     * @param paddle First moving object of the game
     * @param ball Second moving object of the game
     */
    private void createNewGameBoard(Paddle paddle, GBall ball) {
        checkAndClearBoard();
        addMovingObjectsDefault(paddle, ball);
        createBrickField();
    }

    private void createBrickField() {
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            for (int j = 0; j < NBRICK_ROWS; j++) {

                Color currRowColor = Color.DARK_GRAY; // default color if something goes wrong

                switch (j) {
                    case 0, 1: {
                        currRowColor = bricksColorSet[0];
                        break;
                    }
                    case 2, 3: {
                        currRowColor = bricksColorSet[1];
                        break;
                    }
                    case 4, 5: {
                        currRowColor = bricksColorSet[2];
                        break;
                    }
                    case 6, 7: {
                        currRowColor = bricksColorSet[3];
                        break;
                    }
                    case 8, 9: {
                        currRowColor = bricksColorSet[4];
                        break;
                    }
                }
                
                drawBrick(i, j, currRowColor);
            }
        }
    }

    /**
     * Simply adds the main moving game objects on the game board
     * @param paddle First main moving object of the game
     * @param ball Second main moving object of the game
     */
    private void addMovingObjectsDefault(Paddle paddle, GBall ball) {
        add(paddle, PAD_STARTING_X, PAD_STARTING_Y);
        add(ball, BALL_STARTING_X, BALL_STARTING_Y);
    }

    /**
     * If the board is not clear from (previous) bricks entirely --> clear the board
     */
    private void checkAndClearBoard() {
        if (!bricksOnScreen.isEmpty()) {
            for (GRect brick : bricksOnScreen) {
                remove(brick);
            }
        }
    }

    /**
     * Initializes and adds the brick of current row's color
     * @param i X-offset iterator that is used as multiplier
     * @param j Y-offset iterator that is used as multiplier
     * @param currRowColor Current row bricks' color
     */
    private void drawBrick(int i, int j, Color currRowColor) {
        double startingX = BRICK_SEP / 2.0;
        double startingY = BRICK_Y_OFFSET;

        double offset_x = BRICK_WIDTH + BRICK_SEP;
        double offset_y = BRICK_HEIGHT + BRICK_SEP;

        GRect brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);

        brick.setFilled(true);
        brick.setFillColor(currRowColor);

        add(brick, startingX + (i * offset_x), startingY + (j * offset_y));

        bricksOnScreen.add(brick);
    }

    /**
     * Starts program after its initialization
     * This method contains and manages the main game logic
     * @param paddle Paddle object - first moving object of the game
     * @param ball Ball object - second moving object of the game
     * @param backgroundMusic Initialized background music
     */
    public void run(Paddle paddle, GBall ball, Music backgroundMusic) {

        // Sound effects that are involved
        Music gameOver = new Music();
        gameOver.setFile("assets/gameOver.wav");
        Music winning = new Music();
        winning.setFile("assets/winning.wav");
        Music hitPaddle = new Music();
        hitPaddle.setFile("assets/bang.wav");
        Music hitBrick = new Music();
        hitBrick.setFile("assets/clock.wav");

        // These labels display the main information during the game
        GLabel label;
        GLabel label2;
        GLabel lives = new GLabel("LIVES: " + NTURNS);
        lives.setFont(labelFont2);
        lives.setFont(livesFont);
        add(lives,  WIDTH / 2.0 - lives.getWidth() / 2, HEIGHT);
        lives.sendToBack();

        int bricksRemainingCount = NBRICK_ROWS * NBRICKS_PER_ROW;

        GObject collider; // object the ball collided with (paddle/brick)

        while (bricksRemainingCount != 0) {

            collider = getCollidingObject(ball);

            if (collider == paddle) {

                hitPaddle.play();

                // if ball's bottom basement(bound) is under the paddle
                if (ball.getY() + BALL_DIAMETER > HEIGHT - PADDLE_Y_OFFSET) {
                    ball.changeXOpposite();
                } else {

                    // check and change X direction (if necessary)
                    if (checkToChangeXDirection(ball, paddle, 21.0)) {
                            ball.changeXOpposite();
                    }

                    // set Y direction - always
                    ball.movePolar(1, 90); // bounces on one pixel up immediately (to avoid bugs)
                    ball.changeYOpposite();

                    // makes smooth random changes of ball movement to make gaming experience more interesting
                    ball.changeVelocityALittle();
                }

                // if collider object == brick:
            }  else if (collider != ball && collider != lives && collider != null){

                hitBrick.play();

                // if ball is above the brick (else ball is under the brick) --> make a bounce
                if (ball.getY() + BALL_RADIUS > collider.getY() - BRICK_HEIGHT / 2.0) {
                    ball.movePolar(1, 90); // little bounce up to avoid bugs
                } else {
                    ball.movePolar(1, -90); // little bounce down to avoid bugs
                }

                remove(collider);

                bricksRemainingCount--;

                // check and change X direction (if necessary)
                if (checkToChangeXDirection(ball, collider, 12)) {
                    if (rgen.nextBoolean(0.8)) {
                        ball.changeXOpposite();
                    }
                }

                // set Y direction - always
                ball.changeYOpposite();

                // makes smooth random changes of ball movement to make gaming experience more interesting
                ball.changeVelocityALittle();
            }

            /* if ball stops == hit the bottom wall
             --> player looses one attempt(life), ball restarts from default state */
            if (ball.getSpeedVX() == 0 && ball.getSpeedVY() == 0) {

                remainingLives--;

                lives.setLabel("LIVES: " + remainingLives);

                // GAME OVER:
                if (remainingLives == 0) {
                    backgroundMusic.stop();
                    ball.setVisible(false);
                    gameOver.play();
                    doGameOverAnim();

                    // RESTART/EXIT:
                    label = new GLabel("TO START NEW GAME - PRESS ENTER");
                    label.setFont(labelFont2);
                    add(label, WIDTH / 2.0 - label.getWidth() / 2, HEIGHT / 2.0 - label.getHeight() / 2);
                    label2 = new GLabel("OR PRESS 'ESC' TO EXIT");
                    label2.setFont(labelFont2);
                    add(label2, WIDTH / 2.0 - label2.getWidth() / 2, HEIGHT / 1.5 - label2.getHeight() / 2);

                    // restartGame is set by the keyListener
                    while(!restartGame) {
                        pause(100);
                    }

                    /* NEW GAME:
                     * Music, objects, counters, lives, game board are updated to a default states
                     */
                    backgroundMusic.playUntilStop();

                    remainingLives = NTURNS;
                    bricksRemainingCount = NBRICK_ROWS * NBRICKS_PER_ROW;
                    lives.setLabel("LIVES: " + remainingLives);

                    remove(label);
                    remove(label2);

                    createNewGameBoard(paddle, ball);

                    ball.setVisible(true);

                    restartGame = false;
                }

                waitForClick();

                // Initial velocity parameters of every new game (party)
                ball.setRandomVx();
                ball.setVY(INITIAL_VY);
            }
        }

        /*
         * WINNING THE GAME:
         * Removing unnecessary labels, playing the winning animation and music, exit
         */
        backgroundMusic.stop();

        ball.setVelocity(0,0);
        remove(lives);
        pause(500);

        winning.play();

        doWinningAnim(ball);

        removeAll();

        pause(500);

        java.lang.System.exit(0);
    }

    /**
     * Makes simple winning animations with the help of gaming ball
     * @param ball Uses ball object to create different colorful particles
     */
    private void doWinningAnim(GBall ball) {

        double randXWinningAnim;
        double randYWinningAnim;

        GLabel winning = new GLabel("YOU WIN ! ! !");
        winning.setFont(labelFont1);
        add(winning, WIDTH / 2.0 - winning.getWidth() / 2, HEIGHT / 2.0 - winning.getHeight() / 2);

        for (int i = 0; i < 5; i++) {
            randXWinningAnim = rgen.nextDouble(10, WIDTH - BALL_DIAMETER);
            randYWinningAnim = rgen.nextDouble(10, WIDTH - BALL_DIAMETER);
            ball.setColor(rgen.nextColor());
            ball.setLocation(randXWinningAnim, randYWinningAnim);
            pause(500);
        }
    }

    /**
     * This method uses some principles from vector maths.
     * They allow to establish the placement collision of ball and corners of paddle/brick
     * Remark: the corners of the paddle/brick are sensitive to collisions
     * Instances ('# # # ⬤' - direction of the ball) :
     *  (1)  #                               # (3)
     *         #                         #
     *            ⬤                   ⬤
     *              ------------------
     *             |                 |
     *             ------------------
     *            ⬤                  ⬤
     *         #                        #
     * (2)  #                              #  (4)
     * @param ball Ball object
     * @param collider Collided object (paddle/brick)
     * @param coef This is simply a coefficient to measure the width of the sensitive corners of paddle/brick
     * @return true --> change the balls dx (vx) field; false --> let it be as it is (do not change)
     */
    private boolean checkToChangeXDirection(GBall ball, GObject collider, double coef) {
            // if ball touches the left corner of paddle/brick
        if ((ball.getX() + BALL_RADIUS) < (collider.getX() + collider.getWidth() / coef)) {
            // instance (1) || instance (2)
            if (ball.getSpeedVX() > 0 && ball.getSpeedVY() > 0 || ball.getSpeedVX() > 0 && ball.getSpeedVY() < 0) {
                return true;
            } else {
                return false;
            }
            // if ball touches the left corner of paddle/brick
        } else if ((ball.getX() + BALL_RADIUS) > (collider.getX() + collider.getWidth() - collider.getWidth() / coef)) {
            // instance (3) || (4)
            if (ball.getSpeedVX() <  0 && ball.getSpeedVY() > 0 || ball.getSpeedVX() < 0 && ball.getSpeedVY() < 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
}

    /**
     * Shows a simple 'Game Over' animation based on the label movement form the left side to right
     */
    private void doGameOverAnim() {
        GLabel gameOver = new GLabel("GAME OVER!");
        add(gameOver, -gameOver.getWidth(), HEIGHT / 2.0);
        gameOver.setFont(labelFont2);
        while (gameOver.getX()  < WIDTH + gameOver.getWidth()) {
            gameOver.movePolar(50, 0);
            pause(250);
        }

    }

    /**
     * This method checks object the ball collided with (paddle/brick)
     * It uses some specific Check Points that surrounds the ball
     * (use visualisation below for better understanding)
     * @param ball The object that moves and collides with other objects
     * @return The collided object
     */
    private GObject getCollidingObject(GBall ball) {

        /*         VISUALISATION OF THE CHECK POINTS (they marked '@'):
         *
         *                      A3         K1          B3
         *       A   *  ******  @  * * *   @   * * * *  @  ******  *   B
         *           *                                             *
         *           *  A2 @                              B2 @     *
         *           *                                             *
         *       A1  @                                             @  B1
         *           *                                             *
         *           *                                             *
         *           *                                             *
         *       S1  @                                         S2  @
         *           *                                             *
         *           *                                             *
         *           *                                             *
         *       D3  @                                          C3 @
         *           *                                             *
         *           *  D2 @                               C2 @    *
         *           *                                             *
         *       D   *  ******  @  * * *   @   * * * *  @  ******  *   C
         *                     D1          K2          C1
         */

        double startX = ball.getX();
        double startY = ball.getY();
        double boundsOffset = 2;

        GPoint a1 = new GPoint(startX, startY + BALL_RADIUS / 2.0);
        GPoint a3 = new GPoint(startX + BALL_RADIUS / 2.0, startY);
        GPoint a2 = getMiddlePoint(a1, a3, startX, startY);

        GPoint b1 = new GPoint(startX + BALL_DIAMETER, startY + BALL_RADIUS / 2.0 );
        GPoint b3 = new GPoint(startX + BALL_DIAMETER - BALL_RADIUS / 2.0, startY);
        GPoint b2 = getMiddlePoint(b1, b3, b3.getX(), b3.getY());

        GPoint c1 = new GPoint(startX + BALL_DIAMETER - BALL_RADIUS / 2.0, startY + BALL_DIAMETER);
        GPoint c3 = new GPoint(startX + BALL_DIAMETER, startY + BALL_DIAMETER - BALL_RADIUS / 2.0);
        GPoint c2 = getMiddlePoint(c1, c3, c1.getX(), c3.getY());

        GPoint d1 = new GPoint(startX + BALL_RADIUS / 2.0, startY + BALL_DIAMETER);
        GPoint d3 = new GPoint(startX, startY + BALL_DIAMETER - BALL_RADIUS / 2.0);
        GPoint d2 = getMiddlePoint(d1, d3, d3.getX(), d3.getY());

        GPoint s1 = new GPoint(startX - boundsOffset, startY + BALL_RADIUS);
        GPoint s2 = new GPoint(startX + BALL_DIAMETER + boundsOffset, startY + BALL_RADIUS);

        GPoint k1 = new GPoint(startX + BALL_RADIUS, startY - boundsOffset);
        GPoint k2 = new GPoint(startX + BALL_RADIUS, startY + BALL_DIAMETER + boundsOffset);

        GPoint[] collisionDots = new GPoint[]{a1, a2, a3, b1, b2, b3, c1, c2, c3, d1, d2, d3, s1, s2, k1, k2};

        for (GPoint collisionDot : collisionDots) {
            if (this.getElementAt(collisionDot) != null) {
                return this.getElementAt(collisionDot);
            }
        }

        return null;
    }

    /**
     * Math vector function that receives two dots with X and Y coords and finds the middle point of the vector
     * @param A First dot
     * @param B Second dot
     * @param startX The X coordinate of temp origin of coordinates
     * @param startY The Y coordinate of temp origin of coordinates
     * @return The middle point (GPoint object) of the vector AB
     */
    private GPoint getMiddlePoint(GPoint A, GPoint B, double startX, double startY) {
        return new GPoint(startX + Math.abs(B.getX() - A.getX()) / 2, startY + Math.abs(B.getY() - A.getY()) / 2);
    }
}