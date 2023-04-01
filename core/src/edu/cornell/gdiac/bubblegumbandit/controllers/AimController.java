package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

public class AimController {

    /** The colors used in the aim render */
    private final Color[] COLORS = new Color[]{new Color(1, .619f, .62f, 1),
            new Color(1, .73f, .73f, .9f),
            new Color(1, .81f, .81f, .8f),
            new Color(1, .86f, .86f, .7f),
            new Color(1, .905f, .905f, .6f),
            new Color(1, 1, 1, .5f)};

    /** The max number of dots in the trajectory */
    private final int MAX_DOTS = 6;

    /**
     * The gap between each dot in the trajectory diagram (for raytraced trajectory.)
     */
    private final float trajectoryGap = 0.5f;

    /**
     * The scale of each dot in the trajectory diagram (for raytraced trajectory.)
     */
    private final float trajectoryScale = 0.5f;

    private LevelModel level;

    private TextureRegion trajectoryTexture;

    private JsonValue constants;

    /** The current number of dots */
    private int range;

    /** Array of dot positions */
    private float[] dotPos;

    public AimController() {
        dotPos = new float[MAX_DOTS * 2];
    }

    /** Initializes the AimController. */
    public void initialize(LevelModel level, AssetDirectory directory, JsonValue constants) {
        this.level = level;
        trajectoryTexture = new TextureRegion(directory.getEntry("trajectoryProjectile", Texture.class));
        this.constants = constants;
    }

    /**
     * Returns the origin of the gum when fired by the player.
     *
     * @param gumJV the JSON Value representing the gum projectile.
     * @return The origin of the projectile of the gum when fired.
     */
    public Vector2 getProjOrigin(JsonValue gumJV, GameCanvas canvas) {
        Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
        cross.scl(1 / level.getScale().x, 1 / level.getScale().y);

        Rectangle bounds = level.getBounds();
        cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
        cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));

        Vector2 target = cross;
        BanditModel bandit = level.getBandit();

        float offsetX = gumJV.getFloat("offsetX", 0);
        float offsetY = gumJV.getFloat("offsetY", 0);
        offsetY *= bandit.getYScale();

        Vector2 origin = new Vector2(bandit.getX(), bandit.getY() + offsetY);
        Vector2 dir = new Vector2((target.x - origin.x), (target.y - origin.y));
        dir.nor();
        dir.scl(offsetX);

        // Adjust origin of shot based on target pos
        // Rotate around top half of player for gravity pulling down, bottom half for gravity pulling up
        if (dir.y * level.getWorld().getGravity().y < 0) {
            origin.x += dir.x;
        } else {
            origin.x += (target.x > bandit.getX() ? offsetX : -offsetX);
        }
        origin.y += dir.y;
        return origin;
    }

    public Vector2 getProjTarget(GameCanvas canvas) {
        Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
        cross.scl(1 / level.getScale().x, 1 / level.getScale().y);

        Rectangle bounds = level.getBounds();
        cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
        cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));
        return cross;
    }

    /** Update the trajectory */
    public void update(GameCanvas canvas, float dt) {
        Vector2 target = PlayerController.getInstance().getCrossHair();
        JsonValue gumJV = constants.get("gumProjectile");
        Vector2 origin = getProjOrigin(gumJV, canvas);
        Vector2 dir = new Vector2((target.x - origin.x), (target.y - origin.y));
        dir.nor();
        dir.scl(level.getBounds().width * 2); // Make sure ray will cover the whole screen
        Vector2 end = new Vector2(origin.x + dir.x, origin.y + dir.y); // Find end point of the ray cast

        final Vector2 intersect = new Vector2();

        RayCastCallback ray = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                if (!ob.getName().equals("gumProjectile")) {
                    intersect.set(point);
                    return fraction;
                }
                return -1;
            }
        };
        level.getWorld().rayCast(ray, origin, end);

        dir = new Vector2(intersect.x - origin.x, intersect.y - origin.y);
        int numSegments = (int) (dir.len() / trajectoryGap); // Truncate to find number before colliding
        dir.nor();
        range = numSegments + 1;
        if (range > MAX_DOTS) range = MAX_DOTS;
        for (int i = 0; i < range; i++) {
            dotPos[2*i] = origin.x + (dir.x * i * trajectoryGap);
            dotPos[2*i+1] = origin.y + (dir.y * i * trajectoryGap);
        }
    }

    /**
     * Draws the path of the projectile using the result of a raycast. Only works for shooting in a straight line (gravity scale of 0).
     *
     * @param canvas      The GameCanvas to draw the trajectory on.
     */
    public void drawProjectileRay(GameCanvas canvas) {
        for (int i = 0; i < range; i++) {
            canvas.draw(trajectoryTexture, COLORS[i], trajectoryTexture.getRegionWidth() / 2f, trajectoryTexture.getRegionHeight() / 2f, dotPos[2*i] * level.getScale().x,
                    dotPos[2*i+1] * level.getScale().y, trajectoryTexture.getRegionWidth() * trajectoryScale, trajectoryTexture.getRegionHeight() * trajectoryScale);
        }
    }
}
