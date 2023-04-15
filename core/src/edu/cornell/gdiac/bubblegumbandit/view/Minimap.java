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
    private final int CONDENSED_TILES_LONG = 50;

    /**Height of the map */
    private final int CONDENSED_TILES_TALL = 25;

    /**Width of the map */
    private int expandedTilesLong;

    /**Height of the map */
    private int expandedTilesTall;

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

    /** Image drawn as a Bandit tile during the previous pass. */
    private Image prevBanditTile;

    /** How far we are moving between minimap states. */
    private float lerpProgress = 0f;

    /** true if we're toggling and moving between states. */
    private boolean toggling = false;


    /** Initializes the minimap for a given level.
     *
     * @param directory The asset directory.
     * @param levelFormat the current level.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     */
    public void initialize(AssetDirectory directory, JsonValue levelFormat, int physicsWidth, int physicsHeight) {
        //Make the Minimap's background and map tiles.
        setScales();
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
            }
        }
    }

    /**Sets the scales and sizes that the Minimap will use to draw its
     * tiles. */
    private void setScales(){
        condensedScale = new Vector2(.22f, .25f);
        expandedScale = new Vector2(condensedScale.x * 3.5f, condensedScale.y * 3.5f);

    }

    /** Updates the minimap with a time value to help it with transitions
     * between the condensed and expanded state. */
    public void updateMinimap(float dt) {
        if (expanded)expand();
        else condense();
    }

    /**
     * Draws the Minimap.
     *
     * @param banditPosition Tile position of the Bandit.
     * */
    public void draw(Vector2 banditPosition){

        //The number of tiles we need to draw varies based on our status.
        int tilesLong = expanded ? expandedTilesLong : CONDENSED_TILES_LONG;
        int tilesTall = expanded ? expandedTilesTall : CONDENSED_TILES_TALL;

        //The bounds of our map depends on our minimap status.
        int topLeftX = expanded ? (width - tilesLong) / 2 :
                Math.round(banditPosition.x) - tilesLong / 2;
        int topLeftY = expanded ? (height - tilesTall) / 2 :
                Math.round(banditPosition.y) - tilesTall / 2;
        int bottomRightX = topLeftX + tilesLong;
        int bottomRightY = topLeftY + tilesTall;

        //Our condensed minimap needs tighter bounds.
        if (!expanded) {
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
        }

        // Gather Tiles to draw. This loop doesn't really matter much when
        // the minimap is expanded.
        if (prevBanditTile != null) prevBanditTile.setColor(0, 0, 0, 0);
        Set<Vector2> validFloors = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x >= topLeftX && x < bottomRightX && y >= topLeftY && y < bottomRightY) {
                    Vector2 floorPos = new Vector2(x, y);
                    if (floorPositions.contains(floorPos)) {
                        validFloors.add(floorPos);
                    }
                }
                Image tile = minimapTiles[x][y];
                if (tile != null && !validFloors.contains(new Vector2(x, y))) {
                    tile.setSize(0, 0);
                    tile.setColor(0, 0, 0, 0);
                }
            }
        }

        for (Vector2 floorPos : validFloors) {
            int x = (int) floorPos.x;
            int y = (int) floorPos.y;
            int minimapX = x - topLeftX;
            int minimapY = y - topLeftY;
            Image tile = minimapTiles[x][y];
            setMinimapTilePosition(tile, minimapX, minimapY);
            drawAsPlatformTile(tile);
        }

        // Draw the Bandit's Tile.
        int banditX = (int) banditPosition.x + 1;
        int banditY = (int) banditPosition.y;
        int minimapBanditX = banditX - topLeftX;
        int minimapBanditY = banditY - topLeftY;
        Image banditTile = minimapTiles[banditX][banditY];
        setMinimapTilePosition(banditTile, minimapBanditX, minimapBanditY);
        drawAsBanditTile(banditTile);
        prevBanditTile = banditTile;

        // Draw the background.
        minimapStage.draw();
    }



    /**Sets the Minimap's size and position to meet expanded standards. */
    private void expand() {
        setMinimapSizeAndPosition(expandedScale);
    }

    /**Sets the Minimap's size and position to meet condensed standards. */
    private void condense(){
        setMinimapSizeAndPosition(condensedScale);
    }


    /**
     * Sets minimap's background and size to be compliant with
     * some (X, Y) scale.
     *
     * @param scale The scale to apply to the minimap.
     */
    private void setMinimapSizeAndPosition(Vector2 scale) {
        // Set the background's size.
        float mapLength = scale.x * minimapStage.getWidth();
        float mapHeight = scale.y * minimapStage.getHeight();
        minimapBackground.setSize(mapLength, mapHeight);

        // Set the background's position.
        float xOffset;
        float yOffset;
        if (scale.equals(expandedScale)) {
            xOffset = (minimapStage.getWidth() - mapLength) / 2;
            yOffset = (minimapStage.getHeight() - mapHeight) / 2;
        } else {
            xOffset = minimapStage.getWidth() - mapLength - 30;
            yOffset = minimapStage.getHeight() - mapHeight - 30;
        }
        minimapBackground.setPosition(xOffset, yOffset);
    }


    /**Expands the minimap if it is condensed. Condenses the minimap
     * if it is expanded.*/
    public void toggleMinimap() {
        expanded = !expanded;
        toggling = true;
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
     * Draws a Minimap Tile such that it represents the Bandit.
     *
     * @param banditImage The Image of the Tile to modify.
     * */
    private void drawAsBanditTile(Image banditImage){
        banditImage.setColor(Color.PINK);
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

        float condensedTileSize = 5;
        float expandedTileSize = 10;
        float tileSize = expanded ? expandedTileSize : condensedTileSize;
        int tilesLong = expanded ? expandedTilesLong : CONDENSED_TILES_LONG;
        int tilesTall = expanded ? expandedTilesTall : CONDENSED_TILES_TALL;

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
