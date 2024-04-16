package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javazoom.jl.player.Player;

/**
 * The {@code GamePanel} class encapsulates the main gameplay area of the Snake game.
 * It is responsible for handling game logic, rendering the game state, and processing
 * player input. This class extends {@link JPanel} and implements {@link ActionListener}
 * to respond to game events.
 */
public class GamePanel extends JPanel implements ActionListener
{
    // Deque to store the directions of the snake
    private Deque<Character> directionQueue = new ArrayDeque<>();

    // Dimensions of the game panel
    public static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;
    static final int DELAY = 75;

    // Colors used in the game
    static final Color BACKGROUND_COLOR = Color.black;
    static final Color HEAD_COLOR = new Color( 34, 139, 34, 220 );
    static final Color BODY_COLOR = new Color( 45, 180, 0, 220 );
    static final Color APPLE_COLOR = new Color( 204, 0, 0, 220 );
    static final Color SCORE_COLOR = new Color( 204, 0, 0, 220 );

    // Constants for font sizes
    private static final Font LARGE_FONT = new Font( "Futura", Font.BOLD, 75 );
    private static final Font MEDIUM_FONT = new Font( "Futura", Font.BOLD, 40 );

    // Snake variables
    public final int x[] = new int[GAME_UNITS]; // x coordinates of the snake
    public final int y[] = new int[GAME_UNITS]; // y coordinates of the snake

    // Game variables
    private BufferedImage appleSprite;
    public int bodyParts = 6;
    public int applesEaten;
    int highScore;
    public int appleX;
    public int appleY;
    public char direction = 'R';
    public boolean running = false;
    Timer timer;
    Random random;

    private JButton replayButton;
    String resourcesPath = "/Users/andreaventi/Developer/Snake/src/main/resources/";
    private String devPath = resourcesPath + "highscore.txt";
    private String prodPath = "highscore.txt";

    // Additional getter methods needed for testing
    public int getApplesEaten() { return applesEaten; }

    public int getBodyParts() { return bodyParts; }

    public boolean isRunning() { return running; }

    public int[] getXCoordinates() { return x; }

    public int[] getYCoordinates() { return y; }

    public BufferedImage getAppleSprite() { return appleSprite; }

    /**
     * Constructs a new GamePanel and initializes the game components including
     * setting up the UI and starting background music. This constructor also
     * sets the panel properties required for the game such as size, background color,
     * and key listeners for controlling the snake.
     */
    public GamePanel()
    {
        random = new Random();
        this.setPreferredSize( new Dimension( SCREEN_WIDTH, SCREEN_HEIGHT ) );
        this.setBackground( BACKGROUND_COLOR );
        this.setDoubleBuffered( true );            // Enable double buffering for smoother rendering
        this.setFocusable( true );                 // Allow the panel to receive keyboard input
        this.addKeyListener( new MyKeyAdapter() ); // Add key listener for controlling the snake

        loadAppleSprite();

        replayButton = new JButton( "Replay" );
        replayButton.setFont( new Font( "Futura", Font.BOLD, 20 ) );
        replayButton.addActionListener( new ActionListener() {
            @Override public void actionPerformed( ActionEvent e ) { restartGame(); }
        } );
        replayButton.setBounds( SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT - 60, 100, 40 );
        replayButton.setEnabled( false );
        this.add( replayButton );

        startGame();
        playMusic( resourcesPath + "DRIVE(chosic.com).mp3" );
    }

    /**
     * Initializes and starts the game by placing the first apple and starting
     * the game timer which triggers periodic updates to the game state.
     */
    public void startGame()
    {
        replayButton.setEnabled( false ); // Disable the button until next game over.
        replayButton.setVisible( false ); // Hide the button until next game over.

        newApple();
        running = true;
        timer = new Timer( DELAY, this );
        timer.start();
    }

    /**
     * Overrides the {@link JPanel#paintComponent(Graphics)} method to
     * custom render the game's graphical elements such as the snake and apples.
     * This method is called by the Swing framework whenever the panel needs redrawing.
     *
     * @param g The {@link Graphics} context used for drawing.
     */
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        draw( g );
    }

    /**
     * Draws all game elements on this panel. If the game is running, it draws
     * the snake and the apple; if the game is over, it calls the gameOver method.
     *
     * @param g The Graphics context used for drawing game elements.
     */
    public void draw( Graphics g )
    {
        if ( running )
        {
            if ( appleSprite != null )
                g.drawImage( appleSprite, appleX, appleY, UNIT_SIZE, UNIT_SIZE, this );
            else
            { // Draw the apple as a colored oval
                g.setColor( APPLE_COLOR );
                g.fillOval( appleX, appleY, UNIT_SIZE, UNIT_SIZE );
            }

            // Draw the snake
            for ( int i = 0; i < bodyParts; i++ )
            {
                Color randomBodyColor =
                    new Color( random.nextInt( 255 ), random.nextInt( 255 ), random.nextInt( 255 ), 220 );

                // If the current segment is the head, draw it with the head color
                // Otherwise, draw the body with a random body color
                g.setColor( i == 0 ? HEAD_COLOR : randomBodyColor );
                g.fillRect( x[i], y[i], UNIT_SIZE, UNIT_SIZE );
            }

            // // Draw the current score
            drawCenteredText( g, "Score: " + applesEaten, MEDIUM_FONT, MEDIUM_FONT.getSize() );
        }

        else
            gameOver( g );
    }

    /**
     * Generates a new location for the apple randomly on the game panel.
     * The method calculates random x and y coordinates within the game boundaries
     * that align with the grid defined by UNIT_SIZE.
     */
    public void newApple()
    {
        appleX = random.nextInt( (int)( SCREEN_WIDTH / UNIT_SIZE ) ) * UNIT_SIZE;
        appleY = random.nextInt( (int)( SCREEN_HEIGHT / UNIT_SIZE ) ) * UNIT_SIZE;
    }

    /**
     * Updates the position of the snake in the direction it is currently moving.
     * This method shifts each segment of the snake forward in the array to simulate
     * movement, with the head moving forward into the next grid position based on
     * the current direction.
     */
    public void move()
    {
        // Move the body parts of the snake
        for ( int i = bodyParts; i > 0; i-- )
        {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch ( direction )
        {
        case 'U':
            y[0] = y[0] - UNIT_SIZE;
            break;

        case 'D':
            y[0] = y[0] + UNIT_SIZE;
            break;

        case 'L':
            x[0] = x[0] - UNIT_SIZE;
            break;

        case 'R':
            x[0] = x[0] + UNIT_SIZE;
            break;
        }
    }

    /**
     * Checks if the snake's head has collided with an apple.
     * If so, increases the length of the snake by one segment and increments the score.
     * A new apple is then generated at a random location.
     */
    public void checkApple()
    {
        if ( ( x[0] == appleX ) && ( y[0] == appleY ) )
        {
            bodyParts++;
            applesEaten++;
            highScore = Math.max( highScore, applesEaten );
            writeHighScore();
            newApple();
        }
    }

    /**
     * Checks for collisions between the snake's head and its body or the borders of the game panel.
     * If a collision is detected, it sets the running flag to false, effectively ending the game.
     */
    public void checkCollisions()
    {
        // Check if the head of the snake collides with the body
        for ( int i = bodyParts - 1; i > 0; i-- )
        {
            if ( ( x[0] == x[i] ) && ( y[0] == y[i] ) )
            {
                running = false;
                break; // No need to check further if collision is found
            }
        }

        // Check if the head of the snake collides with left border
        if ( x[0] < 0 )
            running = false;

        // Check if the head of the snake collides with right border
        else if ( x[0] >= SCREEN_WIDTH )
            running = false;

        // Check if the head of the snake collides with top border
        else if ( y[0] < 0 )
            running = false;

        // Check if the head of the snake collides with bottom border
        else if ( y[0] >= SCREEN_HEIGHT )
            running = false;

        if ( !running )
            timer.stop();
    }

    /**
     * Renders the game over screen.
     * Displays the 'Game Over' text and the final score in the center of the panel.
     * Also re-enables the replay button allowing the game to be restarted.
     *
     * @param g The Graphics context used for drawing the text and button.
     */
    public void gameOver( Graphics g )
    {
        drawCenteredText( g, "Game Over", LARGE_FONT, SCREEN_HEIGHT / 3 );
        drawCenteredText( g, "High Score: " + readHighScore(), MEDIUM_FONT, SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() );
        drawCenteredText( g, "Score: " + applesEaten, MEDIUM_FONT,
                          SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() + MEDIUM_FONT.getSize() + 20 );

        setupReplayButton();
    }

    /**
     * Responds to timer events by updating the game state. This method
     * moves the snake, checks for apples, and checks for collisions.
     *
     * @param e The action event triggered by the timer.
     */
    @Override public void actionPerformed( ActionEvent e )
    {
        if ( running )
        {
            synchronized ( directionQueue )
            {
                if ( !directionQueue.isEmpty() )
                {
                    direction = directionQueue.poll();
                }
            }
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    /**
     * Inner class to handle keyboard events for controlling the snake.
     */
    public class MyKeyAdapter extends KeyAdapter
    {
        /**
         * Invoked when a key is pressed and updates the direction of the snake accordingly.
         * @param e The event to be processed.
         */
        @Override public void keyPressed( KeyEvent e )
        {
            synchronized ( directionQueue )
            {
                char newDirection = direction;
                switch ( e.getKeyCode() )
                {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    newDirection = ( direction != 'R' ) ? 'L' : direction;
                    break;

                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    newDirection = ( direction != 'L' ) ? 'R' : direction;
                    break;

                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    newDirection = ( direction != 'D' ) ? 'U' : direction;
                    break;

                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    newDirection = ( direction != 'U' ) ? 'D' : direction;
                    break;

                case KeyEvent.VK_SPACE:
                    if ( !running && replayButton.isEnabled() )
                    {
                        restartGame();
                    }
                    return; // Skip direction queueing
                }

                if ( newDirection != direction )
                {
                    if ( directionQueue.isEmpty() || directionQueue.getLast() != newDirection )
                    {
                        directionQueue.add( newDirection );
                    }
                }
            }
        }
    }

    /**
     * Loads the apple sprite image from the resources directory.
     * This method attempts to load the apple image file specified by {@code appleSprite.png}
     * and assigns it to the {@code appleSprite} BufferedImage. If the image cannot be loaded,
     * the error is printed to the standard error stream and the stack trace is printed.
     */
    private void loadAppleSprite()
    {
        try
        {
            appleSprite = ImageIO.read( new File( resourcesPath + "appleSprite.png" ) );
        }
        catch ( IOException e )
        {
            System.err.println( "Unable to load apple sprite: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    /**
     * Draws text centered on the screen.
     * @param g The Graphics context used for drawing.
     * @param text The text to be drawn.
     * @param font The font to use for the text.
     * @param yPos The y-position for the text.
     */
    private void drawCenteredText( Graphics g, String text, Font font, int yPos )
    {
        g.setFont( font );
        g.setColor( SCORE_COLOR );
        FontMetrics metrics = getFontMetrics( font );
        int x = ( SCREEN_WIDTH - metrics.stringWidth( text ) ) / 2;
        g.drawString( text, x, yPos );
    }

    /**
     * Configures and displays the replay button.
     */
    private void setupReplayButton()
    {
        int buttonWidth = 150;
        int buttonHeight = 50;
        int buttonX = ( SCREEN_WIDTH - buttonWidth ) / 2;
        int buttonY = SCREEN_HEIGHT - 120;

        replayButton.setBounds( buttonX, buttonY, buttonWidth, buttonHeight );
        replayButton.setEnabled( true );
        replayButton.setVisible( true );
    }

    /**
     * Restarts the game by resetting the snake's body, score, and game state.
     * This method reinitializes the game components to their start conditions, hides the replay button,
     * stops the current timer, and starts a new timer for fresh game updates.
     */
    public void restartGame()
    {
        // Reset the snake body parts
        bodyParts = 6;
        for ( int i = 0; i < bodyParts; i++ )
        {
            x[i] = 0; // Reset x position
            y[i] = 0; // Reset y position
        }

        // Reset game state variables
        applesEaten = 0;
        directionQueue.clear();
        direction = 'R';
        running = true;
        replayButton.setEnabled( false ); // Disable the button until next game over.
        replayButton.setVisible( false ); // Hide the button until next game over.

        newApple();
        timer.stop();                     // Stop the current timer
        timer = new Timer( DELAY, this ); // Reinitialize the timer
        timer.start();                    // Restart the timer

        repaint(); // Repaint the game panel to refresh the screen
    }

    /**
     * Plays background music from a specified file path. This method runs
     * the music in a separate thread to ensure it does not block the GUI thread.
     *
     * @param filePath The path to the music file to be played.
     */
    public void playMusic( String filePath )
    {
        new Thread( new Runnable() {
            public void run()
            {
                try
                {
                    while ( true )
                    { // Loop to allow the music to replay indefinitely
                        FileInputStream fileInputStream = new FileInputStream( filePath );
                        Player player = new Player( fileInputStream );
                        player.play();
                        player.close();
                    }
                }
                catch ( Exception e )
                {
                    System.err.println( "Problem playing file " + filePath );
                    e.printStackTrace();
                }
            }
        } ).start();
    }

    private String getHighScorePath()
    {
        File devFile = new File( devPath );
        if ( devFile.exists() )
        {
            return devPath;
        }
        else
        {
            return prodPath;
        }
    }

    /**
     * Writes the current high score to a file named "highscore.txt".
     * This method uses a BufferedWriter to write the high score as a string to a file.
     * If an IOException occurs during file writing, it logs the exception message
     * and prints the stack trace to the standard error stream.
     */
    public void writeHighScore()
    {
        File file = new File( getHighScorePath() );
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( file ) ) )
        {
            writer.write( Integer.toString( highScore ) );
        }
        catch ( IOException e )
        {
            System.err.println( "Problem writing high score file " + getHighScorePath() );
            e.printStackTrace();
        }
    }

    /**
     * Reads the high score from a file named "highscore.txt".
     * This method attempts to open and read the high score from the file.
     * If the file does not exist, it returns 0. If the file exists but contains invalid data
     * (data that is not an integer), it logs an error message. If the file is not found
     * during the read operation, it logs a file not found error and prints the stack trace.
     *
     * @return the high score read from the file, or 0 if an error occurs or if the file does not exist
     */
    public int readHighScore()
    {
        File file = new File( getHighScorePath() );
        if ( !file.exists() )
            return 0;

        try ( Scanner scanner = new Scanner( file ) )
        {
            if ( scanner.hasNextInt() )
                highScore = scanner.nextInt();

            else
                System.err.println( "Invalid data in high score file" );
        }
        catch ( FileNotFoundException e )
        {
            System.err.println( "High score file not found: " + getHighScorePath() );
            e.printStackTrace();
        }

        return highScore;
    }
}
