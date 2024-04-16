package edu.aav66;

import javax.swing.JFrame;

/**
 * The {@code GameFrame} class extends {@link JFrame} and is responsible for creating
 * the main window of the Snake game. It initializes and sets up the game panel where
 * the game is played.
 *
 * This class configures the frame, adding the necessary components and setting
 * various properties like size, close operation, and visibility to make the game
 * ready to play immediately upon instantiation.
 */
public class GameFrame extends JFrame
{

    /**
     * Constructs a new {@code GameFrame} object, setting up the game environment.
     * It initializes a new {@code GamePanel} to be the content of this frame and
     * configures various properties of the frame to ensure the game is displayed
     * correctly.
     */
    public GameFrame()
    {
        // Add an instance of GamePanel to this frame
        this.add( new GamePanel() );

        // Set the title of the frame to "Snake"
        this.setTitle( "Snake" );

        // Ensure the application exits when the frame is closed
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // Disable resizing of the frame to maintain consistent gameplay
        this.setResizable( false );

        // Pack the components within the frame
        this.pack();

        // Make the frame visible to the user
        this.setVisible( true );

        // Position the frame in the center of the screen
        this.setLocationRelativeTo( null );
    }
}
