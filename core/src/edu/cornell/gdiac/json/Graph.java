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

/**
 * Class represents a 2D grid of tiles.
 *
 * Most of the work is done by the internal Tile class.  The outer class is
 * really just a container.
 */
public class Graph {

	private int width;

	private int height;



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
	}

	// Constants

	private static final Color BASIC_COLOR = new Color(0.25f, 0.25f, 0.25f, 0.5f);
	/** Highlight color for power tiles */
	private static final Color POWER_COLOR = new Color( 0.0f,  1.0f,  1.0f, 0.5f);

	// Instance attributes
	/** The board width (in number of tiles) */
	private int width;
	/** The board height (in number of tiles) */
	private int height;

	/**
	 * Creates a new board of the given size
	 *
	 * @param width Board width in tiles
	 * @param height Board height in tiles
	 */
	public Graph(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new TileState[width * height];
		for (int ii = 0; ii < tiles.length; ii++) {
			tiles[ii] = new TileState();
		}
		resetTiles();
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
		return (int)(f / (getTileSize() + getTileSpacing()));
	}

	/**
	 * Returns the screen position coordinate for a board cell index.
	 *
	 * While all positions are 2-dimensional, the dimensions to
 	 * the board are symmetric. This allows us to use the same
	 * method to convert an x coordinate or a y coordinate to
	 * a cell index.
	 *
	 * @param n Tile cell index
	 *
	 * @return the screen position coordinate for a board cell index.
	 */
	public float boardToScreen(int n) {
		return (float) (n + 0.5f) * (getTileSize() + getTileSpacing());
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
	public int getMoveAlongPathToGoalTile() {
		//#region PUT YOUR CODE HERE

		int x = board.screenToBoard(ship.getX());
		int y = board.screenToBoard(ship.getY());

		x = x == board.getWidth() ? x - 1 : x;
		y = y == board.getHeight() ? y - 1 : y;

		List<Integer> directions = new ArrayList<>();
		directions.add(CONTROL_MOVE_LEFT);
		directions.add(CONTROL_MOVE_UP);
		directions.add(CONTROL_MOVE_RIGHT);
		directions.add(CONTROL_MOVE_DOWN);

		int capacity = 16;
		Queue<Vector3> queue = new Queue<>();
		queue.addLast(new Vector3(x, y, CONTROL_NO_ACTION));
		board.setVisited(x, y);
		targetReachable = true;

		Vector3 tile;
		int xCoor, yCoor, direction;

		while (queue.notEmpty()) {


			tile = queue.removeFirst();
			x = (int) tile.x;
			y = (int) tile.y;

			if (board.isGoal(x, y)) {
				return (int) tile.z;
			}
			Collections.shuffle(directions);
			for (int a : directions) {

				xCoor = getNeighborX(a, x);
				yCoor = getNeighborY(a, y);

				if ((int) tile.z == CONTROL_NO_ACTION) {
					direction = a;
				} else {
					direction = (int) tile.z;
				}

				if (board.inBounds(xCoor, yCoor) && !board.isDestroyedAt(xCoor, yCoor) && !board.isVisited(xCoor, yCoor)) {
					// double queue capacity if queue is full
					if (queue.size > capacity) queue.ensureCapacity(capacity);
					capacity *= 2;
					queue.addLast(new Vector3(xCoor, yCoor, direction));
					board.setVisited(xCoor, yCoor);
				}
			}

		}

		targetReachable = false;
		return CONTROL_NO_ACTION;
		//#endregion
		return 0;
	}

	public boolean canMoveToTile(int x, int y){
		return true;
	}

}