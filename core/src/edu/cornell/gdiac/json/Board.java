package edu.cornell.gdiac.json;

/*
 * Board.java
 *
 * This class keeps track of all the tiles in the game. If a photon hits
 * a ship on a Tile, then that Tile falls away.
 *
 * Because of this gameplay, there clearly has to be a lot of interaction
 * between the Board, Ships, and Photons.  However, this way leads to
 * cyclical references.  As we will discover later in the class, cyclic
 * references are bad, because they lead to components that are too
 * tightly coupled.
 *
 * To address this problem, this project uses a philosophy of "passive"
 * models.  Models do not access the methods or fields of any other
 * Model class.  If we need for two Model objects to interact with
 * one another, this is handled in a controller class. This can get
 * cumbersome at times (particularly in the coordinate transformation
 * methods in this class), but it makes it easier to modify our
 * code in the future.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;

import java.util.ArrayList;
import java.util.List;

import static edu.cornell.gdiac.json.controllers.InputController.*;

/**
 * Class represents a 2D grid of tiles.
 *
 * Most of the work is done by the internal Tile class.  The outer class is
 * really just a container.
 */
public class Board {


    /**
     * Each tile on the board has a set of attributes associated with it.
     * However, no class other than board needs to access them directly.
     * Therefore, we make this an inner class.
     */
    private static class TileState {

        /** Is this a goal tiles */
        public boolean goal = false;
        /** Has this tile been visited (used for pathfinding)? */
        public boolean visited = false;

        public int value;

    }

    // Constants

    private static final Color BASIC_COLOR = new Color(0.25f, 0.25f, 0.25f, 0.5f);
    /** Highlight color for power tiles */
    private static final Color POWER_COLOR = new Color( 0.0f,  1.0f,  1.0f, 0.5f);

    private TileState[][] tiles;

    // Instance attributes
    /** The board width (in number of tiles) */
    private int width;
    /** The board height (in number of tiles) */
    private int height;

    private float tileSize =25f;

    /**
     * Creates a new board of the given size
     *
     * @param width Board width in tiles
     * @param height Board height in tiles
     */
    public Board(int width, int height, JsonValue board) {
        this.width = width;
        this.height = height;
        JsonValue row = board.child;
        tiles = new TileState[height][width];
        for (int i= 0; i < height; i++) {
            int[] rowVals = row.asIntArray();
            for (int j = 0; j < width; j++){
                TileState tile = new TileState();
                tile.value = rowVals[j];
                tiles[i][j] = tile;
            }
            row = row.next;
        }
        resetTiles();
    }


    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     *
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    public int getMoveAlongPathToGoalTile(Vector2 startPos, Vector2 targetPos, boolean gravityFlipped) {
        //#region PUT YOUR CODE HERE

        int x = (int) startPos.x;
        int y = (int) startPos.y;
        int capacity = 16;
        Queue<Vector3> queue = new Queue<>();
        queue.addLast(new Vector3(startPos.x, startPos.y, CONTROL_NO_ACTION));
        setVisited((int)startPos.x, (int)startPos.y);

        Vector3 tile;
        int xCoor, yCoor, direction;

        List<Integer> directions = new ArrayList<>();
        directions.add(CONTROL_MOVE_LEFT);
        directions.add(CONTROL_MOVE_UP);
        directions.add(CONTROL_MOVE_RIGHT);
        directions.add(CONTROL_MOVE_DOWN);

        while (queue.notEmpty()) {

            tile = queue.removeFirst();
            x = (int) tile.x;
            y = (int) tile.y;

            if (isGoal(x, y)) {
                return (int) tile.z;
            }

            for (int a : directions) {

                xCoor = getNeighborX(a, x);
                yCoor = getNeighborY(a, y);

                if ((int) tile.z == CONTROL_NO_ACTION) {
                    direction = a;
                } else {
                    direction = (int) tile.z;
                }

                if (inBounds(xCoor, yCoor) && !isVisited(xCoor, yCoor)) {
                    // double queue capacity if queue is full
                    if (queue.size > capacity) queue.ensureCapacity(capacity);
                    capacity *= 2;
                    queue.addLast(new Vector3(xCoor, yCoor, direction));
                    setVisited(xCoor, yCoor);
                }
            }

        }

        return CONTROL_MOVE_LEFT;
    }

    /**
     * getNeighborX returns the x coordinate of the controllers neighbor in the a direction
     *
     * @param direction direction of neighbor
     * @param x         x-coordinate of ship
     */
    private int getNeighborX(int direction, int x) {
        if (direction == CONTROL_MOVE_LEFT) {
            return x - 1;
        } else if (direction == CONTROL_MOVE_RIGHT) {
            return x + 1;
        }
        return x;
    }


    /**
     * getNeighborY returns the y coordinate of the controllers neighbor in the a direction
     *
     * @param direction direction of neighbor
     * @param y         y-coordinate of ship
     */
    private int getNeighborY(int direction, int y) {
        if (direction == CONTROL_MOVE_UP) {
            return y - 1;
        } else if (direction == CONTROL_MOVE_DOWN) {
            return y + 1;
        }
        return y;
    }

    /**
     * Resets the values of all the tiles on screen.
     */
    public void resetTiles() {
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                TileState tile = tiles[x][y];
                tile.goal = false;
                tile.visited = false;
            }
        }
    }

    /**
     * Returns the number of tiles horizontally across the board.
     *
     * @return the number of tiles horizontally across the board.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the number of tiles vertically across the board.
     *
     * @return the number of tiles vertically across the board.
     */
    public int getHeight() {
        return height;
    }

    // METHODS FOR LAB 2

    // CONVERSION METHODS (OPTIONAL)
    // Use these methods to convert between tile coordinates (int) and
    // world coordinates (float).

    /**
     * Returns the board cell index for a screen position.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param f Screen position coordinate
     *
     * @return the board cell index for a screen position.
     */
    public int screenToBoard(float f) {
        return (int)(f / tileSize);
    }

//    /**
//     * Returns the screen position coordinate for a board cell index.
//     *
//     * While all positions are 2-dimensional, the dimensions to
//     * the board are symmetric. This allows us to use the same
//     * method to convert an x coordinate or a y coordinate to
//     * a cell index.
//     *
//     * @param n Tile cell index
//     *
//     * @return the screen position coordinate for a board cell index.
//     */
//    public float boardToScreen(int n) {
//        return (float) (n + 0.5f) * (getTileSize());
//    }


    // PATHFINDING METHODS (REQUIRED)
    // Use these methods to implement pathfinding on the board.

    /**
     * Returns true if the given position is a valid tile
     *
     * It does not check whether the tile is live or not.  Dead tiles are still valid.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < height && y < width;
    }

    /**
     * Returns true if the tile has been visited.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile has been visited.
     */
    public boolean isVisited(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return tiles[x][y].visited;
    }

    /**
     * Marks a tile as visited.
     *
     * A marked tile will return true for isVisited(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setVisited(int x, int y) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        tiles[x][y].visited = true;
    }

    /**
     * Returns true if the tile is a goal.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is a goal.
     */
    public boolean isGoal(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return tiles[x][y].goal;
    }

    /**
     * Marks a tile as a goal.
     *
     * A marked tile will return true for isGoal(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        tiles[x][y].goal = true;
    }

    /**
     * Clears all marks on the board.
     *
     * This method should be done at the beginning of any pathfinding round.
     */
    public void clearMarks() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TileState state = tiles[x][y];
                state.visited = false;
                state.goal = false;
            }
        }
    }
}