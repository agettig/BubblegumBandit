package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class BackgroundTileModel {

    /** The texture of this tile */
    private TextureRegion tileTexture;
    float x;
    float y;
    Vector2 drawScale;



    /**
     * Create a new TileModel with degenerate settings
     */
    public BackgroundTileModel() {
      tileTexture = null;
    }


    /**
     * Initializes the platform via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the platform subtree
     *
     * @param texture the texture of the tile
     * @param x		the x position of the tile
     * @param y the y position of the tile
     */
    public void initialize(TextureRegion texture, float x, float y, Vector2 drawScale) {
      this.tileTexture = texture;
      this.x = x;
      this.y = y;
      this.drawScale = drawScale;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
      if (tileTexture != null) {
        canvas.draw(tileTexture, Color.WHITE,
            tileTexture.getRegionWidth()/2,
            tileTexture.getRegionHeight()/2,
            x*drawScale.x, y*drawScale.y, 0, 1, 1);
      }
    }

    public boolean hasTile(){
        return tileTexture != null;
    }
  }

