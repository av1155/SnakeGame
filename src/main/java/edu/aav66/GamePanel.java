package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.util.Random;
import javax.swing.*;
import javazoom.jl.player.Player;

/**
 * The {@code GamePanel} class encapsulates the main gameplay area of the Snake game.
 * It is responsible for handling game logic, rendering the game state, and processing
 * player input. This class extends {@link JPanel} and implements {@link ActionListener}
 * to respond to game events.
 */
public class GamePanel extends JPanel implements ActionListener
{
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;
    static final int DELAY = 75;
    final int x[] = new int[GAME_UNITS]; // x coordinates of the snake
    final int y[] = new int[GAME_UNITS]; // y coordinates of the snake
    Color redColor = new Color( 204, 0, 0, 220 );
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;
    private JButton replayButton;
    String resourcesPath = "/Users/andreaventi/Developer/Snake/src/main/resources/";

    /**
     * Constructs a new GamePanel and initializes the game components including
     * setting up the UI and starting background music. This constructor also
     * sets the panel properties required for the game such as size, background color,
     * and key listeners for controlling the snake.
     */
    GamePanel()
    {
        random = new Random();
        this.setPreferredSize( new Dimension( SCREEN_WIDTH, SCREEN_HEIGHT ) );
        this.setBackground( Color.black );
        this.setFocusable( true );
        this.addKeyListener( new MyKeyAdapter() );

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
                try ( FileInputStream fileInputStream = new FileInputStream( filePath ) )
                {
                    Player player = new Player( fileInputStream );
                    player.play();
                }
                catch ( Exception e )
                {
                    System.err.println( "Problem playing file " + filePath );
                    e.printStackTrace();
                }
            }
        } ).start();
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
            // // Set the color of the grid lines
            // g.setColor( Color.gray );
            //
            // // Draw the grid
            // for ( int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++ )
            // {
            //     g.drawLine( i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT ); // Vertical lines
            //     g.drawLine( 0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE );  // Horizontal lines
            // }

            // Draw the apple
            g.setColor( redColor );
            g.fillOval( appleX, appleY, UNIT_SIZE, UNIT_SIZE );

            // Draw the snake
            for ( int i = 0; i < bodyParts; i++ )
            {
                // Draw the head of the snake
                if ( i == 0 )
                {
                    // Forest green color for the snake head
                    g.setColor( new Color( 34, 139, 34 ) );
                    g.fillRect( x[i], y[i], UNIT_SIZE, UNIT_SIZE );
                }

                // Draw the body of the snake
                else
                {
                    g.setColor( new Color( 45, 180, 0 ) );
                    // Random color for the snake body
                    g.setColor( new Color( random.nextInt( 255 ), random.nextInt( 255 ), random.nextInt( 255 ), 220 ) );
                    g.fillRect( x[i], y[i], UNIT_SIZE, UNIT_SIZE );
                }
            }

            // Draw the current score
            g.setColor( redColor );
            g.setFont( new Font( "Futura", Font.BOLD, 40 ) );
            FontMetrics metrics = getFontMetrics( g.getFont() );

            // Center the text on the top of the screen
            g.drawString( "Score: " + applesEaten,
                          ( SCREEN_WIDTH - metrics.stringWidth( "Score: " + applesEaten ) ) / 2,
                          g.getFont().getSize() );
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
        for ( int i = bodyParts; i > 0; i-- )
        {
            if ( ( x[0] == x[i] ) && ( y[0] == y[i] ) )
            {
                running = false;
            }
        }

        // Check if the head of the snake collides with left border
        if ( x[0] < 0 )
            running = false;

        // Check if the head of the snake collides with right border
        if ( x[0] > SCREEN_WIDTH )
            running = false;

        // Check if the head of the snake collides with top border
        if ( y[0] < 0 )
            running = false;

        // Check if the head of the snake collides with bottom border
        if ( y[0] > SCREEN_HEIGHT )
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
        // Draw the game over text
        g.setColor( redColor );
        g.setFont( new Font( "Futura", Font.BOLD, 75 ) );
        FontMetrics metrics1 = getFontMetrics( g.getFont() );

        // Center the text on the screen
        g.drawString( "Game Over", ( SCREEN_WIDTH - metrics1.stringWidth( "Game Over" ) ) / 2, SCREEN_HEIGHT / 2 );

        // Draw the current score
        g.setColor( redColor );
        g.setFont( new Font( "Futura", Font.BOLD, 40 ) );
        FontMetrics metrics2 = getFontMetrics( g.getFont() );

        // Center the text on the top of the screen
        g.drawString( "Score: " + applesEaten, ( SCREEN_WIDTH - metrics2.stringWidth( "Score: " + applesEaten ) ) / 2,
                      g.getFont().getSize() );

        replayButton.setBounds( SCREEN_WIDTH / 2 - 75, SCREEN_HEIGHT * 2 / 3, 150, 50 );
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
     * Responds to timer events by updating the game state. This method
     * moves the snake, checks for apples, and checks for collisions.
     *
     * @param e The action event triggered by the timer.
     */
    @Override public void actionPerformed( ActionEvent e )
    {
        if ( running )
        {
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
            switch ( e.getKeyCode() )
            {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if ( direction != 'R' && direction != 'L' )
                {
                    direction = 'L';
                }
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if ( direction != 'L' && direction != 'R' )
                {
                    direction = 'R';
                }
                break;

            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if ( direction != 'D' && direction != 'U' )
                {
                    direction = 'U';
                }
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if ( direction != 'U' && direction != 'D' )
                {
                    direction = 'D';
                }
                break;
            }
        }
    }
}
