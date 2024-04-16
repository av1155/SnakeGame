import static org.junit.jupiter.api.Assertions.*;

import edu.aav66.GamePanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the GamePanel class.
 */
public class GamePanelTest
{

    private GamePanel gamePanel;

    @BeforeEach void setUp()
    {
        gamePanel = new GamePanel();
        gamePanel.startGame(); // Start the game in a controlled state for testing
    }

    @Test void testAppleEating()
    {
        int initialScore = gamePanel.applesEaten;
        int initialBodyParts = gamePanel.bodyParts;

        // Simulate the snake eating an apple
        gamePanel.appleX = gamePanel.x[0]; // Place apple directly in front of the snake
        gamePanel.appleY = gamePanel.y[0];
        gamePanel.checkApple();

        assertEquals( initialScore + 1, gamePanel.applesEaten, "Apple eating should increase score by 1" );
        assertEquals( initialBodyParts + 1, gamePanel.bodyParts, "Eating an apple should increase body parts by 1" );
    }

    @Test void testCollisionWithSelf()
    {
        // Setting up the snake to collide with itself
        gamePanel.x[0] = gamePanel.x[1]; // Direct the head of the snake into its body
        gamePanel.y[0] = gamePanel.y[1];
        gamePanel.checkCollisions();

        assertFalse( gamePanel.running, "Collision with self should stop the game" );
    }

    @Test void testGameRestart()
    {
        // Ensure the apple is placed exactly where the snake's head is
        gamePanel.appleX = gamePanel.getXCoordinates()[0];
        gamePanel.appleY = gamePanel.getYCoordinates()[0];

        // Simulate some game activity
        gamePanel.checkApple(); // Simulate eating an apple
        int scoreAfterEating = gamePanel.getApplesEaten();

        // Assert that the score increased as expected
        assertEquals( 1, scoreAfterEating, "Score should increase by 1 after eating an apple" );

        // Restart the game
        gamePanel.restartGame();

        // Verify that the game state is reset
        assertEquals( 0, gamePanel.getApplesEaten(), "Apples eaten should be reset to 0 after restart" );
        assertEquals( 6, gamePanel.getBodyParts(), "Body parts should be reset to initial length after restart" );
        assertTrue( gamePanel.isRunning(), "Game should be running after restart" );
    }

    @Test void testMovementLogic()
    {
        // Initial position of the snake's head
        int initialX = gamePanel.getXCoordinates()[0];
        int initialY = gamePanel.getYCoordinates()[0];

        // Move the snake down
        gamePanel.direction = 'D'; // Change direction to 'Down'
        gamePanel.move();          // Trigger a move

        // Check new position after moving down
        assertEquals( initialY + GamePanel.UNIT_SIZE, gamePanel.getYCoordinates()[0],
                      "Snake should move down correctly" );

        // Prepare to move right from the new position
        initialX = gamePanel.getXCoordinates()[0]; // Update initialX to current position after moving down

        // Move the snake right
        gamePanel.direction = 'R'; // Change direction to 'Right'
        gamePanel.move();          // Trigger a move

        // Check new position after moving right
        assertEquals( initialX + GamePanel.UNIT_SIZE, gamePanel.getXCoordinates()[0],
                      "Snake should move right correctly" );
    }

    @Test void testGameInitialization()
    {
        // Check initial game state
        assertTrue( gamePanel.isRunning(), "Game should be running immediately after initialization" );
        assertEquals( 6, gamePanel.getBodyParts(), "Initial length of snake should be correct" );
        assertNotNull( gamePanel.getAppleSprite(), "Apple sprite should be loaded" );
    }

    @Test void testBoundaryCollision()
    {
        // Move snake to the right boundary
        gamePanel.getXCoordinates()[0] = GamePanel.SCREEN_WIDTH - GamePanel.UNIT_SIZE;
        gamePanel.direction = 'R'; // Direction right
        gamePanel.move();
        gamePanel.checkCollisions();

        // Check if the game stopped
        assertFalse( gamePanel.isRunning(), "Game should stop when snake hits the right boundary" );
    }
}
