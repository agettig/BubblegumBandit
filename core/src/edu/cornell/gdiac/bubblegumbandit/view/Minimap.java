package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.models.level.TileModel;
import jdk.javadoc.internal.doclets.toolkit.taglets.UserTaglet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_TERRAIN;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_TERRAIN;


public class Minimap {

    /**Width of the level */
    private int width;

    /**Height of the level */
    private int height;

    /**Width of the map */
    private int condensedTilesLong = 50;

    /**Height of the map */
    private int condensedTilesTall = 25;


    /**Width of the map */
    private int expandedTilesLong;

    /**Height of the map */
    private int expandedTilesTall;

    /**Size of a Tile in the Minimap */
    private final float CONDENSED_TILE_SIZE = 5f;

    /**Size of a Tile in the Minimap */
    private final float EXPANDED_TILE_SIZE = 10f;

    /**Minimap's background image */
    private Image minimapBackground;

    /**Minimap's Tiles, indexed by physics coordinate */
    private Image[][] minimapTiles;

    /**Minimap's Stage for drawing */
    private Stage minimapStage;

    /**Minimap's Table for drawing */
    private Table minimapTable;

    /**(X, Y) scale of the Minimap when it is in its smaller form. */
    private Vector2 condensedScale;

    /**(X, Y) scale of the Minimap when it is in its larger form. */
    private Vector2 expandedScale;

    /** The positions of the tiles */
    private ArrayList<Vector2> floorPositions;

    /** true if the Minimap is expanded.*/
    private boolean expanded;

    private Image prevBanditTile;

    int numTilesWide;

    int numTilesTall;



    /** Initializes the minimap for a given level.
     *
     * @param directory The asset directory.
     * @param levelFormat the current level.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     */
    public void initialize(AssetDirectory directory, JsonValue levelFormat, int physicsWidth, int physicsHeight) {
        //Safety checks.
        assert directory != null;
        //Make the Minimap's background and map tiles.
        setScalesAndSizes();
        minimapTable = new Table();
        minimapTable.align(Align.bottomRight);
        minimapTable.setFillParent(true);
        minimapStage = new Stage();
        minimapStage.addActor(minimapTable);
        minimapBackground = new Image(
                directory.getEntry("minimap_background", Texture.class));
        minimapTable.add(minimapBackground);
        minimapBackground.setSize(0, 0);
        minimapBackground.setColor(new Color(0,0,0,.3f));

        assert levelFormat != null;
        assert physicsWidth >= 0;
        assert physicsHeight >= 0;

        //Set fields.
        width = physicsWidth;
        height = physicsHeight;
        makeMinimapTiles(directory);

        //Find all positions of floors/platforms.
        floorPositions = new ArrayList<>();
        JsonValue layer = levelFormat.get("layers").child();
        JsonValue tileLayer = null;
        // TODO: Don't repeat work in LevelModel
        while (layer != null) {
            String layerName = layer.getString("name");
            if ("Terrain".equals(layerName)) {
                tileLayer = layer;
            }
            layer = layer.next();
        }

        int[] worldData = tileLayer.get("data").asIntArray();
        // Iterate over each tile in the world and create if it exists



        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (tileVal != 0) {
                float x = (i % width) + 1f;
                expandedTilesLong = Math.max(expandedTilesLong, (int)x);
                float y = height - (i / width) - 1f;
                expandedTilesTall = Math.max(expandedTilesTall, (int)y);
                floorPositions.add(new Vector2(x, y));
                System.out.println("Adding floor at " + new Vector2(x, y));
            }
        }




    }

    /**Sets the scales and sizes that the Minimap will use to draw its
     * tiles. */
    private void setScalesAndSizes(){
        condensedScale = new Vector2(.22f, .25f);
        expandedScale = new Vector2(condensedScale.x * 3.5f, condensedScale.y * 3.5f);

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
        if(expanded) expand();
        else condense();

        int tilesLong = expanded ? expandedTilesLong : condensedTilesLong;
        int tilesTall = expanded ? expandedTilesTall : condensedTilesTall;


        //Calculate Minimap bounds.
        int topLeftX = Math.round(banditPosition.x) - tilesLong / 2;
        int topLeftY = Math.round(banditPosition.y) - tilesTall / 2;

        int bottomRightX = Math.round(banditPosition.x) + tilesLong / 2;
        int bottomRightY = Math.round(banditPosition.y) + tilesTall / 2;

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

        if(prevBanditTile != null)prevBanditTile.setColor(0, 0, 0, 0);

        Set<Vector2> validFloors = new HashSet<Vector2>();


        int banditX = (int) banditPosition.x + 1;
        int banditY = (int) banditPosition.y;
        int minimapBanditX = banditX - topLeftX;
        int minimapBanditY = banditY - topLeftY;


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Image tile = minimapTiles[x][y];
                if (tile != null) {
                    if (x >= topLeftX && x < bottomRightX && y >= topLeftY && y < bottomRightY) {
                        validFloors.add(new Vector2(x, y));
                    }
                    else if(x != banditX && y != banditY){
                        tile.setSize(0, 0);
                        tile.setColor(0, 0, 0, 0);
                    }
                }
            }
        }



        for(Vector2 floorPos : floorPositions){
            int x = (int) floorPos.x;
            int y = (int) floorPos.y;
            if(validFloors.contains(floorPos)){
                int minimapX = x - topLeftX;
                int minimapY = y - topLeftY;
                Image tile = minimapTiles[x][y];
                setMinimapTilePosition(tile, minimapX, minimapY);
                drawAsPlatformTile(tile);
            }
        }


        Image banditTile = minimapTiles[banditX][banditY];
        setMinimapTilePosition(banditTile, minimapBanditX, minimapBanditY);
        drawAsBanditTile(banditTile);
        prevBanditTile = banditTile;

        //Draw the Minimap.
        minimapStage.draw();
    }

    private void expand(){
        float mapLength = expandedScale.x * minimapStage.getWidth();
        float mapHeight = expandedScale.y * minimapStage.getHeight();
        minimapBackground.setSize(mapLength, mapHeight);

        float xOffset = (minimapStage.getWidth() - mapLength)/2;
        float yOffset = (minimapStage.getHeight() - mapHeight)/2;
        minimapBackground.setPosition(xOffset,yOffset);

    }

    private void condense(){
        float mapLength = condensedScale.x * minimapStage.getWidth();
        float mapHeight = condensedScale.y * minimapStage.getHeight();
        minimapBackground.setSize(mapLength, mapHeight);
        minimapBackground.setPosition(
                minimapStage.getWidth() - mapLength - 30,
                minimapStage.getHeight() - mapHeight - 30);
    }

    public void toggleMinimap(){
        expanded = !expanded;
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
                minimapTiles[x][y].setSize(0,0);
                minimapTiles[x][y].setColor(new Color(0, 0, 0, 0));
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
        platformImage.setColor(Color.WHITE);
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

        float tileSize = expanded ? EXPANDED_TILE_SIZE : CONDENSED_TILE_SIZE;
        int tilesLong = expanded ? expandedTilesLong : condensedTilesLong;
        int tilesTall = expanded ? expandedTilesTall : condensedTilesTall;

        //Calculate offsets and positions for centering.
        float totalTileW = tileSize * tilesLong;
        float totalTileH = tileSize * tilesTall;
        float offsetX = (backgroundW - totalTileW) / 2;
        float offsetY = (backgroundH - totalTileH) / 2;
        float tileX = backgroundX + offsetX + (xPos * tileSize);
        float tileY = backgroundY + offsetY + (yPos * tileSize);

        //Set the Tile's position and size.
        tileImage.setPosition(tileX, tileY);
        tileImage.setSize(tileSize + 1, tileSize + 1);
    }
}
