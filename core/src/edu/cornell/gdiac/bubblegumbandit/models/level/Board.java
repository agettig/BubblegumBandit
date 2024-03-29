package edu.cornell.gdiac.bubblegumbandit.models.level;

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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

import java.util.ArrayList;
import java.util.List;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;


/**
 * Class represents a 2D grid of tiles.
 * <p>
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

        /**
         * Is this a goal tiles
         */
        public boolean goal = false;
        /**
         * Has this tile been visited (used for pathfinding)?
         */
        public boolean visited = false;

        public int value;

    }

    // Constants

    private static final int GRAVITY_DOWN_TILE = 1;

    private static final int GRAVITY_UP_TILE = 3;

    private static final int BOTH_GRAVITY_TILE = 2;

    private Queue<Vector3> queue = new Queue<>();


    private TileState[][] tiles;

    // Instance attributes
    /**
     * The board height (in number of tiles)
     */
    private int height;
    /**
     * The board width (in number of tiles)
     */
    private int width;

    //needed for draw method at least
    private Vector2 scale;

    /**
     * Creates a new board of the given size
     *
     * @param boardJson json representation of board
     * @param boardIdOffset the id offset of the first tile in the board
     * @param scale the scale of the level
     */
    public Board(JsonValue boardJson, int boardIdOffset, Vector2 scale) {
        this.scale = scale;
        this.height = boardJson.getInt("height");
        this.width = boardJson.getInt("width");
        int[] jsonTiles = boardJson.get("data").asIntArray();
        tiles = new TileState[width][height];
        int x = 0;
        int y = height - 1;
        for (int i = 0; i < jsonTiles.length; i++) {
            TileState tile = new TileState();
            if (jsonTiles[i] == 0) {
                tile.value = 0;
            } else {
                tile.value = jsonTiles[i] - boardIdOffset + 1;
            }
            tiles[x][y] = tile;
            x++;
            if (x == width) {
                y--;
                x = 0;
            }
        }
    }


    /**
     * Mark all desirable tiles to move to.
     * <p>
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     * <p>
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    public int getMoveAlongPathToGoalTile(Vector2 startPos, boolean gravityFlipped) {
        //#region PUT YOUR CODE HERE

        queue.clear();
        int x = (int) startPos.x;
        int y = (int) startPos.y;

        if (!isValidMove(x, y, gravityFlipped)) return CONTROL_NO_ACTION;
        queue.addLast(new Vector3(x, y, CONTROL_NO_ACTION));
        setVisited(x, y);

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
                if ((gravityFlipped && tile.z == CONTROL_MOVE_UP) || (!gravityFlipped && tile.z == CONTROL_MOVE_DOWN)) {
                    return CONTROL_NO_ACTION;
                }
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

                if (!isVisited(xCoor, yCoor) && isValidMove(xCoor, yCoor, gravityFlipped)) {
                    queue.addLast(new Vector3(xCoor, yCoor, direction));
                    setVisited(xCoor, yCoor);
                }
            }

        }

        return CONTROL_NO_ACTION;
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
            return y + 1;
        } else if (direction == CONTROL_MOVE_DOWN) {
            return y - 1;
        }
        return y;
    }

    /**
     * Resets the values of all the tiles on screen.
     */
    public void resetTiles() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TileState tile = tiles[x][y];
                tile.goal = false;
                tile.visited = false;
            }
        }
    }


  /**
   * Returns the number of tiles vertically across the board.
   *
   * @return the number of tiles vertically across the board.
   */
    public int getHeight() {
        return height;
    }


  /**
   * Returns the number of tiles horizontally across the board.
   *
   * @return the number of tiles horizontally across the board.
   */
    public int getWidth() {
        return width;
    }


    // PATHFINDING METHODS (REQUIRED)
    // Use these methods to implement pathfinding on the board.

    /**
     * Returns true if the given position is a valid tile
     * <p>
     * It does not check whether the tile is live or not.  Dead tiles are still valid.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Returns true if the tile has been visited.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
     * <p>
     * A marked tile will return true for isVisited(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setVisited(int x, int y) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        tiles[x][y].visited = true;
    }

    /**
     * Returns true if the tile is a goal.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
     * <p>
     * A marked tile will return true for isGoal(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        tiles[x][y].goal = true;
    }

    public boolean isValidMove(int x, int y, boolean isGravityFipped) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return false;
        }
        int val = tiles[x][y].value;
        if (val == BOTH_GRAVITY_TILE) return true;
        if (isGravityFipped) {
            if (val == GRAVITY_UP_TILE) return true;
        } else if (val == GRAVITY_DOWN_TILE) return true;
        return false;
    }

    public void drawBoard(GameCanvas canvas) {
        PolygonShape s = new PolygonShape();
        s.setAsBox(this.scale.x*3/8, this.scale.y*3/8); //smaller than grid squares
        float margin = this.scale.x*1/2;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = tiles[i][j].value;
                if (val != 0) {
                    Color color;
                    if (val == GRAVITY_UP_TILE) color = Color.BLUE;
                    else if (val == GRAVITY_DOWN_TILE) {
                        color = Color.RED;
                    } else {
                        color = Color.GREEN;
                    }
                    canvas.drawPhysics(s, color, i * scale.x + margin, j * scale.y + margin);
                }
            }
        }
    }

    public float centerOffset(float f) {
        int tile = (int) f;
        float nearestCenter = tile + .5f;
        return f - nearestCenter;
    }

}