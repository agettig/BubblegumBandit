package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.models.level.CrusherModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

/**
 * RayCastEnv is used to detect environmental features in front of enemies
 * Note: In debug mode, represented by green horizontal ray casts
 * */
public class RayCastEnv {
    /** The color of the FOV graphic. */
    private Color color;

    /** The color of the rays of the FOV during debug mode. */
    private Color DEBUGCOLOR;

    /**
     * Length of ray casts
     * */
    private float length;

    /**
     * Contains the current endpoints of the FOV
     */
    private Array<Vector2> rays = new Array<>();
    /**
     * The bodies currently in the FOV
     */
    private Array<Body> bodies = new Array<>();

    private float height;

    private boolean faceRight;


    /**
     * Creates an FOV
     * @param color the color of the FOV for drawing, will always be drawn translucent
     */
    public RayCastEnv( Color color, float height) {
        this.color = new Color(color.r, color.g, color.b, .5f);
        this.length = 2;
        this.DEBUGCOLOR = color;
        this.height = height;
        for(int i = 0; i < height * 2; i++) this.rays.add(new Vector2());
    }

    /**
     * Updates the FOV to the current Box2d world state
     * @param world the world
     * @param origin the origin of the FOV in Box2d world coordinates
     */
    public void update(World world, Vector2 origin) {
        bodies.clear();
        float y = origin.y - height/2 ;
        for (int i = 0; i < rays.size; i++) {
            final int finalI = i;
            Vector2 begin = new Vector2(origin.x, y);
            float x2 = faceRight ? origin.x + length : origin.x - length;
            Vector2 end = new Vector2(x2, y );
            rays.get(finalI).set(end);

            final float[] minFraction = new float[]{1};
            RayCastCallback rayFirstPass = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point,
                                              Vector2 normal, float fraction) {
                    // TODO: Should enemies obscure enemy vision? Add additional categories here if so.

                    boolean isHazard = ((Obstacle) fixture.getBody().getUserData()).getName().equals("hazard");
                    boolean isBlock = fixture.getBody().getUserData() instanceof CrusherModel;

                    if (fixture.getFilterData().categoryBits == CollisionController.CATEGORY_TERRAIN
                            && !isBlock) {
                        rays.get(finalI).set(point);
                        if (fraction < minFraction[0]) {
                            minFraction[0] = fraction;
                        }
                        return fraction;
                    }
                    return -1f;
                }
            };

            // Add the bodies that collide with the ray before it is obscured by a wall.
            RayCastCallback raySecondPass = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point,
                                              Vector2 normal, float fraction) {

                    boolean isHazard = ((Obstacle) fixture.getBody().getUserData()).getName().equals("hazard");
                    boolean isBlock = fixture.getBody().getUserData() instanceof CrusherModel;
                    if(fraction < minFraction[0] && !bodies.contains(fixture.getBody(), true) && (isBlock)) {
                        bodies.add(fixture.getBody());
                    }
                    return minFraction[0];
                }
            };

            world.rayCast(rayFirstPass, begin, end);
            world.rayCast(raySecondPass, begin, end);
            rays.get(i).sub(origin);
            y += height/rays.size;
        }
    }



    /**
     * Returns whether a given obstacle is in view. Call only after update is called.
     * @param obstacle the obstacle being checked
     * @return whether the obstacle is in view.
     */
    public boolean canSee(Obstacle obstacle) {
        return bodies.contains(obstacle.getBody(), true);
    }

    public Array<Body> getBodies(){
        return bodies;
    }


    /**
     * Modifies the direction of the FOV, maintains range
     * Should be called when the parent obstacle turns around or flips, etc.
     * Could also be used for robots that scan back and forth.
     */
    public void setFaceRight(boolean faceRight) {
        this.faceRight = faceRight;
    }


    /**
     * Draws the FOV, calls a helped method in GameCanvas.
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, float x, float y, float scalex, float scaley) {
        canvas.drawFOV(color, rays, x, y, scalex, scaley);
//        canvas.drawRays(color, rays, x, y, radius, scalex, scaley);
    }

    /**
     * Draws the outline of the physics object.
     */
    public void drawDebug(GameCanvas canvas, float x, float y, float scalex, float scaley) {
        canvas.drawEnvRays(DEBUGCOLOR, rays, x, y, scalex, scaley, faceRight);
    }
}
