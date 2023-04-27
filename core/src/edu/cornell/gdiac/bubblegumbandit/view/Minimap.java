package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    /** Position of the Minimap background when fully condensed.*/
    private Vector2 condensedPosition;

    /** Position of the Minimap background when fully expanded.*/
    private Vector2 expandedPosition;

    /** Most recent position of the Minimap.*/
    private Vector2 currentPosition;

    /** Most recent size of the Minimap.*/
    private Vector2 currentSize;

    /** Size of the Minimap background when fully condensed.*/
    private Vector2 condensedSize;

    /** Size of the Minimap background when fully expanded.*/
    private Vector2 expandedSize;

    /**(X, Y) scale of the Minimap when it is in its larger form. */
    private Vector2 expandedScale;

    /**Current (X, Y) scale of the Minimap, somewhere in-between the
     * condensed and expanded scales. */
    private Vector2 currentScale;

    /** The positions of the tiles */
    private HashSet<Vector2> floorPositions;

    /** true if the Minimap is expanded.*/
    private boolean expanded;

    /** Image drawn as a Bandit tile during the previous pass. */
    private Image prevBanditTile;

    /** Bandit's position during the previous draw pass. */
    private Vector2 prevBanditPosition;

    /** Floors to draw when the Minimap is expanded.*/
    private Set<Vector2> expandedFloors;

    /** Tiles that were visible in the previous draw pass.*/
    private Set<Vector2> prevVisibleTiles;

    /** How quickly the Minimap changes states.*/
    private final float TRANSITION_SPEED = 15f;

    /** true if the Minimap has been initialized.*/
    private boolean initialized;

    /** How many draw passes we've done since the last reset. */
    private  int draws;


    /** Initializes the minimap for a given level.
     *
     * @param directory The asset directory.
     * @param levelFormat the current level.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     */
    public void initialize(AssetDirectory directory, JsonValue levelFormat, int physicsWidth, int physicsHeight) {
        //Make the Minimap's background and map tiles.
        initialized = false;
        draws = 0;
        minimapTable = new Table();
        minimapTable.align(Align.bottomRight);
        minimapTable.setFillParent(true);
        minimapStage = new Stage();
        minimapStage.addActor(minimapTable);
        minimapBackground = new Image(
                directory.getEntry("minimapBackground", Texture.class));
        minimapTable.add(minimapBackground);
        minimapBackground.setSize(0, 0);
        minimapBackground.setColor(new Color(0,0,0,.3f));

        // These calls are necessary based on how Math.max() is called, if the previous level had
        // a bigger minimap the values will not update and the game will crash
        expandedTilesTall = 0;
        expandedTilesLong = 0;

        assert levelFormat != null;
        assert physicsWidth >= 0;
        assert physicsHeight >= 0;

        //Set fields.
        setScales();
        width = physicsWidth;
        height = physicsHeight;
        makeMinimapTiles(directory);

        //Find all positions of floors/platforms.
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

        floorPositions = new HashSet<>();
        prevBanditPosition = new Vector2();
        expandedFloors = new HashSet<>();
        prevVisibleTiles = new HashSet<>();
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (tileVal != 0) {
                float x = i % width;
                float y = height - (i / width) - 1f;
                Vector2 floorPos = new Vector2(x, y);
                floorPositions.add(floorPos);
                expandedFloors.add(floorPos);
            }
        }
        expandedTilesLong = physicsWidth;
        expandedTilesTall = physicsHeight;
        initialized = true;
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
                minimapTiles[x][y] = new Image(directory.getEntry("minimapTile", Texture.class));
                minimapTiles[x][y].setVisible(false);
                minimapTable.add(minimapTiles[x][y]);

            }
        }
    }

    /**Sets the scales and sizes that the Minimap will use to draw its
     * tiles. */
    private void setScales(){
        condensedScale = new Vector2(.22f, .25f);
        expandedScale = new Vector2(condensedScale.x * 3.5f, condensedScale.y * 3.5f);
        currentScale = new Vector2();

        float condensedMapLength = condensedScale.x * minimapStage.getWidth();
        float condensedMapHeight = condensedScale.y * minimapStage.getHeight();
        condensedSize = new Vector2(condensedMapLength, condensedMapHeight);

        float expandedMapLength = expandedScale.x * minimapStage.getWidth();
        float expandedMapHeight = expandedScale.y * minimapStage.getHeight();
        expandedSize = new Vector2(expandedMapLength, expandedMapHeight);

        float condensedXOffset = minimapStage.getWidth() - condensedMapLength - 30;
        float condensedYOffset = minimapStage.getHeight() - condensedMapHeight - 30;
        condensedPosition = new Vector2(condensedXOffset, condensedYOffset);

        float expandedXOffset = (minimapStage.getWidth() - expandedMapLength) / 2;
        float expandedYOffset = (minimapStage.getHeight() - expandedMapHeight) / 2;
        expandedPosition = new Vector2(expandedXOffset, expandedYOffset);

        currentPosition = new Vector2(minimapBackground.getX(), minimapBackground.getY());
        currentSize = new Vector2(minimapBackground.getWidth(), minimapBackground.getHeight());
    }


    /**
     * Draws the Minimap.
     *
     * @param banditPosition Tile position of the Bandit.
     * */
    public void draw(Vector2 banditPosition){

        if(!initialized) return;
        draws++;

        //Make our collection.
        Set<Vector2> validFloors;
        if(expanded) validFloors = expandedFloors;
        else validFloors = new HashSet<>();

        for(Vector2 v : prevVisibleTiles){
            Image tile = minimapTiles[(int)v.x][(int)v.y];
            tile.setVisible(false);
        }


        //Math calculations.
        int tilesLong = expanded ? expandedTilesLong : CONDENSED_TILES_LONG;
        int tilesTall = expanded ? expandedTilesTall : CONDENSED_TILES_TALL;
        int topLeftX = expanded ? (width - tilesLong) / 2 :
                (int) Math.floor(banditPosition.x) - tilesLong / 2;
        int middleY = height / 2;
        int topLeftY = expanded ? middleY - tilesTall / 2 :
                (int) Math.floor(banditPosition.y) - tilesTall / 2;
        int bottomRightX = topLeftX + tilesLong;
        int bottomRightY = topLeftY + tilesTall;
        if (!expanded) {
            topLeftX = Math.max(topLeftX, 0);
            topLeftY = Math.max(topLeftY, 0);
            bottomRightX = Math.min(bottomRightX, width);
            bottomRightY = Math.min(bottomRightY, height);
        }

        //Bandit positional optimization: only if the Bandit moved.
        if (!expanded && !banditPosition.equals(prevBanditPosition)) {
            validFloors = condensedFloors(topLeftX, topLeftY, bottomRightX, bottomRightY);
            prevBanditPosition.set(banditPosition);
        }



        //Draw the floor Tiles
        for (int x = topLeftX; x < bottomRightX; x++) {
            for (int y = topLeftY; y < bottomRightY; y++) {
                Vector2 floorPos = new Vector2(x, y);
                    Image tile = minimapTiles[x][y];
                    tile.setVisible(false);
                    if (floorPositions.contains(floorPos)) validFloors.add(floorPos);
                    if (validFloors.contains(floorPos)) {
                        int minimapX = x - topLeftX;
                        int minimapY = y - topLeftY;
                        setMinimapTilePosition(tile, minimapX, minimapY);
                        drawAsPlatformTile(tile);
                        prevVisibleTiles.add(floorPos);
                    }
            }
        }

        //Draw the Bandit
        int banditX = (int) banditPosition.x;
        int banditY = (int) banditPosition.y;
        int minimapBanditX = banditX - topLeftX;
        int minimapBanditY = banditY - topLeftY;
        Image banditTile = minimapTiles[banditX][banditY];
        setMinimapTilePosition(banditTile, minimapBanditX, minimapBanditY);
        drawAsBanditTile(banditTile);
        prevBanditTile = banditTile;

        //Draw the background
        minimapStage.draw();
    }

    /** Returns a Set of Vector2 positions that represent Tiles to draw when
     * the Minimap is condensed.
     *
     * @return  Set of Vector2 positions that represent Tiles to draw when
     *          the Minimap is condensed.*/
    private Set<Vector2> condensedFloors(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        Set<Vector2> validFloors = new HashSet<>();
        for (int x = topLeftX; x < bottomRightX; x++) {
            for (int y = topLeftY; y < bottomRightY; y++) {
                Vector2 floorPos = new Vector2(x, y);
                if (floorPositions.contains(floorPos)) {
                    validFloors.add(floorPos);
                }
            }
        }
        return validFloors;
    }


    /**
     * Sets minimap's position and size to be compliant with its
     * current state.
     */
    private void setMinimapSizeAndPosition(boolean smoothTransition) {
        if(smoothTransition){
            minimapBackground.setPosition(currentPosition.x, currentPosition.y);
            minimapBackground.setSize(currentSize.x, currentSize.y);
        }
        else{
            if(expanded) minimapBackground.setSize(expandedSize.x, expandedSize.y);
            else minimapBackground.setSize(condensedSize.x, condensedSize.y);

            // Set the background's position.
            if(expanded) minimapBackground.setPosition(expandedPosition.x, expandedPosition.y);
            else minimapBackground.setPosition(condensedPosition.x, condensedPosition.y);
        }
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
        int tilesLong = expanded ? expandedTilesLong : CONDENSED_TILES_LONG;
        int tilesTall = expanded ? expandedTilesTall : CONDENSED_TILES_TALL;

        // Calculate tile size to fit within the background when expanded
        float expandedTileSizeW = backgroundW / tilesLong;
        float expandedTileSizeH = backgroundH / tilesTall;
        float expandedTileSize = Math.min(expandedTileSizeW, expandedTileSizeH);

        float tileSize = expanded ? expandedTileSize : condensedTileSize;


        //Calculate offsets and positions for centering.
        float totalTileW = tileSize * tilesLong;
        float totalTileH = tileSize * tilesTall;
        float offsetX = (backgroundW - totalTileW) / 2;
        float offsetY = (backgroundH - totalTileH) / 2;
        float tileX = backgroundX + offsetX + (xPos * tileSize);
        float tileY = backgroundY + offsetY + (yPos * tileSize);

        //System.out.println("long: " + expandedTilesLong);
        //System.out.println("tall: " + expandedTilesLong);



        //Set the Tile's position and size.
        tileImage.setPosition(tileX, tileY);
        tileImage.setSize(tileSize, tileSize);
    }


    /**
     * Draws a Minimap Tile such that it represents a platform or
     * wall.
     *
     * @param platformImage The Image of the Tile to modify.
     * */
    private void drawAsPlatformTile(Image platformImage){
        platformImage.setColor(Color.WHITE);
        if(draws > 1) platformImage.setVisible(true);
        else platformImage.setVisible(false);
    }


    /**
     * Draws a Minimap Tile such that it represents the Bandit.
     *
     * @param banditImage The Image of the Tile to modify.
     * */
    private void drawAsBanditTile(Image banditImage){
        banditImage.setColor(Color.PINK);
        if(draws > 1) banditImage.setVisible(true);
        else banditImage.setVisible(false);

    }

    /**Sets the Minimap's size and position to meet expanded standards. */
    private void expand() {
        if(!initialized) return;
        expanded = true;
        currentScale.set(expandedScale);
    }

    /**Sets the Minimap's size and position to meet condensed standards. */
    private void condense(){
        if(!initialized) return;
        expanded = false;
        currentScale.set(condensedScale);
    }


    /** Updates the minimap with a time value to help it with transitions
     * between the condensed and expanded state.
     *
     * @param dt The Delta Time value
     * @param holdingMapKey true if the player is holding down the key
     *                      to expand the Minimap
     * */
    public void updateMinimap(float dt, boolean holdingMapKey) {
        //Start of game
        if(!initialized) return;

        if(!expanded && !holdingMapKey) condense();

        //Swapping
        if(holdingMapKey && !expanded){
            expand();
        }
        if(!holdingMapKey && expanded){
            condense();
        }

        //Linear Interpolation Logic
        if (expanded) {
            currentSize.x = MathUtils.lerp(
                    currentSize.x,
                    expandedSize.x,
                    TRANSITION_SPEED * dt);
            currentSize.y = MathUtils.lerp(currentSize.y,
                    expandedSize.y,
                    TRANSITION_SPEED * dt);
            currentPosition.x = MathUtils.lerp(
                    currentPosition.x,
                    expandedPosition.x,
                    TRANSITION_SPEED * dt);
            currentPosition.y = MathUtils.lerp(
                    currentPosition.y,
                    expandedPosition.y,
                    TRANSITION_SPEED * dt);
        } else {
            currentSize.x = MathUtils.lerp(
                    currentSize.x,
                    condensedSize.x,
                    TRANSITION_SPEED * dt);
            currentSize.y = MathUtils.lerp(
                    currentSize.y,
                    condensedSize.y,
                    TRANSITION_SPEED * dt);
            currentPosition.x = MathUtils.lerp(
                    currentPosition.x,
                    condensedPosition.x,
                    TRANSITION_SPEED * dt);
            currentPosition.y = MathUtils.lerp(
                    currentPosition.y,
                    condensedPosition.y,
                    TRANSITION_SPEED * dt);
        }
        setMinimapSizeAndPosition(true);
    }
}
