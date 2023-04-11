package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import jdk.javadoc.internal.doclets.toolkit.taglets.UserTaglet;

import java.util.ArrayList;


public class Minimap {

    /**Width of the level */
    private int width;

    /**Height of the level */
    private int height;

    /**Width of the map */
    private final int MAP_WIDTH = 26;

    /**Height of the map */
    private final int MAP_HEIGHT = 16;

    /**Size of a Tile in the Minimap */
    private final float MAP_TILE_SIZE = 10f;

    /**Minimap's background image */
    private Image minimapBackground;

    /**Minimap's Tiles, indexed by physics coordinate */
    private Image[][] minimapTiles;

    /**Minimap's Stage for drawing */
    private Stage minimapStage;

    /**Minimap's Table for drawing */
    private Table minimapTable;

    /**Ratio of the Minimap's length to the screen width  */
    private final float HORZ_SCALE = .25f;

    /**Ratio of the Minimap's height to the screen height  */
    private final float VERT_SCALE = .3f;


    /**
     * Makes the Minimap.
     *
     * @param directory The BubblegumBandit AssetDirectory reference.
     * @param levelFormat The JSON level format.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     * */
    public Minimap(AssetDirectory directory, JsonValue levelFormat,
                   int physicsWidth, int physicsHeight){

        //Safety checks.
        assert directory != null;
        assert levelFormat != null;
        assert physicsWidth >= 0;
        assert physicsHeight >= 0;

        //Set fields.
        width = physicsWidth;
        height = physicsHeight;

        //Make the Minimap's background and map tiles.
        minimapTable = new Table();
        minimapTable.align(Align.bottomRight);
        minimapTable.setFillParent(true);
        minimapStage = new Stage();
        minimapStage.addActor(minimapTable);
        minimapBackground = new Image(
                directory.getEntry("minimap_background", Texture.class));
        minimapTable.add(minimapBackground);
        minimapBackground.setSize(0, 0);
        minimapBackground.setColor(Color.BLACK);
        makeMinimapTiles(directory);
    }

    /**
     * Draws the Minimap.
     *
     * @param canvasWidth Width of the canvas.
     * @param canvasHeight Height of the canvas.
     * @param banditPosition Tile position of the Bandit.
     * @param enemyPositions Positions of enemies.
     * @param levelFormat The JSON level format.
     * */
    public void draw(float canvasWidth, float canvasHeight, Vector2 banditPosition,
                     ArrayList<Vector2> enemyPositions, JsonValue levelFormat){

        //Make the Minimap background.
        float mapLength = HORZ_SCALE * canvasWidth;
        float mapHeight = VERT_SCALE * canvasHeight;
        minimapBackground.setSize(mapLength, mapHeight);
        minimapBackground.setPosition(
                minimapStage.getWidth() - mapLength - 30,
                minimapStage.getHeight() - mapHeight - 30);

        //Find all positions of floors/platforms.
        ArrayList<Vector2> floorPositions = new ArrayList<Vector2>();
        JsonValue floor = levelFormat.get("platforms").child();
        while(floor != null){
            float[] pos = floor.get("pos").asFloatArray();
            int xPos = (int) pos[0];
            int yPos = (int) pos[1];

            float[] size = floor.get("size").asFloatArray();
            int platWidth = (int) size[0];
            int platHeight = (int) size[1];

            int xOffset = platWidth / 2;
            int yOffset = platHeight / 2;

            for (int x = xPos - xOffset; x < xPos + platWidth - xOffset; x++) {
                for (int y = yPos - yOffset; y < yPos + platHeight - yOffset; y++) {
                    floorPositions.add(new Vector2(x, y));
                }
            }
            floor = floor.next();
        }

        //Calculate Minimap bounds.
        int topLeftX = Math.round(banditPosition.x) - MAP_WIDTH / 2;
        int topLeftY = Math.round(banditPosition.y) - MAP_HEIGHT / 2;

        int bottomRightX = Math.round(banditPosition.x) + MAP_WIDTH / 2;
        int bottomRightY = Math.round(banditPosition.y) + MAP_HEIGHT / 2;

        if (topLeftX < 0) {
            bottomRightX -= topLeftX;
            topLeftX = 0;
        }
        if (topLeftY < 0) {
            bottomRightY -= topLeftY;
            topLeftY = 0;
        }
        if (bottomRightX > width) {
            topLeftX -= (bottomRightX - width);
            bottomRightX = width;
        }
        if (bottomRightY > height) {
            topLeftY -= (bottomRightY - height);
            bottomRightY = height;
        }

        //Draw the relevant Minimap tiles.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Image tile = minimapTiles[x][y];
                if (tile != null) {
                    if (x >= topLeftX && x < bottomRightX && y >= topLeftY && y < bottomRightY) {
                        int minimapX = x - topLeftX;
                        int minimapY = y - topLeftY;
                        setMinimapTilePosition(tile, minimapX, minimapY);
                        tile.setColor(Color.WHITE);
                        if (x == Math.round(banditPosition.x) && y == Math.round(banditPosition.y)) {
                            drawAsBanditTile(tile);
                        }
                    }
                    else tile.setSize(0, 0);

                    //Draw platforms.
                    for(Vector2 v : floorPositions){
                        if((int)v.x == x && (int)v.y == y){
                            drawAsPlatformTile(tile);
                        }
                    }

                    //Draw enemies.
                    for(Vector2 v : enemyPositions){
                        if((int)v.x == x && (int)v.y == y){
                            drawAsEnemyTile(tile);
                        }
                    }
                }
            }
        }

        //Draw the Minimap.
        minimapStage.draw();
    }

    /**
     * Makes Images to represent all possible tiles in the level.
     * Indexes them by their position in a 2D array of Images.
     *
     * @param directory The AssetDirectory of which to extract textures.
     * */
    private void makeMinimapTiles(AssetDirectory directory){
        minimapTiles = new Image[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                minimapTiles[x][y] =
                        new Image(directory.getEntry("minimap_tile", Texture.class));
                minimapTable.add(minimapTiles[x][y]);
            }
        }
    }


    /**
     * Draws a Minimap Tile such that it represents a platform or
     * wall.
     *
     * @param platformImage The Image of the Tile to modify.
     * */
    private void drawAsPlatformTile(Image platformImage){
        platformImage.setColor(Color.BLUE);
    }


    /**
     * Draws a Minimap Tile such that it represents an out-of
     * bounds area.
     *
     * @param outOfBoundsImage The Image of the Tile to modify.
     * */
    private void drawAsOutOfBoundsTile(Image outOfBoundsImage){
        outOfBoundsImage.setSize(0, 0);
    }

    /**
     * Draws a Minimap Tile such that it represents the Bandit.
     *
     * @param banditImage The Image of the Tile to modify.
     * */
    private void drawAsBanditTile(Image banditImage){
        banditImage.setColor(Color.PINK);
    }

    /**
     * Draws a Minimap Tile such that it represents an Enemy.
     *
     * @param enemyImage The Image of the Tile to modify.
     * */
    private void drawAsEnemyTile(Image enemyImage){
        enemyImage.setColor(Color.OLIVE);
    }

    /**
     * Sets the position of an (x, y) Tile in the minimap such that it is
     * centered on top of the Minimap background and in line with its
     * fellow Tiles.
     *
     * @param tileImage The Tile to modify.
     * @param xPos the X Minimap coordinate of the Tile to modify.
     * @param yPos the Y Minimap coordinate of the Tile to modify.
     * */
    private void setMinimapTilePosition(Image tileImage, int xPos, int yPos){

        //Get Minimap background info.
        float backgroundX = minimapBackground.getX();
        float backgroundY = minimapBackground.getY();
        float backgroundW = minimapBackground.getWidth();
        float backgroundH = minimapBackground.getHeight();

        //Calculate offsets and positions for centering.
        float totalTileW = MAP_TILE_SIZE * MAP_WIDTH;
        float totalTileH = MAP_TILE_SIZE * MAP_HEIGHT;
        float offsetX = (backgroundW - totalTileW) / 2;
        float offsetY = (backgroundH - totalTileH) / 2;
        float tileX = backgroundX + offsetX + (xPos * MAP_TILE_SIZE);
        float tileY = backgroundY + offsetY + (yPos * MAP_TILE_SIZE);

        //Set the Tile's position and size.
        tileImage.setPosition(tileX, tileY);
        tileImage.setSize(MAP_TILE_SIZE, MAP_TILE_SIZE);
    }
}
