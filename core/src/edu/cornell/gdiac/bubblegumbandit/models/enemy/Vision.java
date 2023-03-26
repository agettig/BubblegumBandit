package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;


/** Fields of vision for enemies */
public class Vision {

    /** The color of the FOV graphic. */
    private Color color;

    /** The color of the rays of the FOV during debug mode. */
    private Color DEBUGCOLOR = Color.RED;

    /** The direction of the FOV. In LibGdx, 0 is up. */
    private float direction;
    /** The range in radians of the FOV */
    private float range;
    /** The length of the FOV. */
    private float radius;
    /** The number of rays being cast per radius unit */
    private int numRays = 10;

    boolean resetRadius = false;

    /**
     * Contains the current endpoints of the FOV
     */
    private Array<Vector2> rays = new Array<>(numRays);
    /**
     * The bodies currently in the FOV
     */
    private Array<Body> bodies = new Array<>();


    /**
     * Creates an FOV
     * @param radius the radius/length of the FOV
     * @param direction the direction the FOV points in radians
     * @param range the range in radians of the FOV
     * @param color the color of the FOV for drawing, will always be drawn translucent
     */
    public Vision(float radius, float direction, float range, Color color) {
        this.color = new Color(color.r, color.g, color.b, .5f);
        this.radius = radius;
        this.direction = direction;
        this.range = range;
        for(int i = 0; i<numRays*radius; i++) this.rays.add(new Vector2());
    }

    /**
     * Creates a circular FOV
     * @param radius the radius of the FOV
     * @param color the color of the FOV for drawing, will always be translucent
     */
    public Vision(float radius, Color color) {
        this.color = new Color(color.r, color.g, color.b, .5f);
        this.radius = radius;
        this.direction = (float) Math.PI/2;
        this.range = (float) Math.PI*2;
        for(int i = 0; i<numRays*radius; i++) this.rays.add(new Vector2());
    }

    /**
     * Updates the FOV to the current Box2d world state
     * @param world the world
     * @param origin the origin of the FOV in Box2d world coordinates
     */
    public void update(World world, Vector2 origin) {
        bodies.clear();
        if(resetRadius) {
            this.rays.clear();
            for(int i = 0; i<numRays*radius; i++) this.rays.add(new Vector2());
            resetRadius = false;
        }
        float startAngle = direction - range / 2;
        float incrementAngle = range/(rays.size-1);
        for (int i = 0; i < rays.size; i++) {
            float angle =  startAngle + i * incrementAngle;
            final int finalI = i;
            Vector2 end = new Vector2(origin.x + radius * (float) Math.cos(angle),
                    origin.y + radius * (float) Math.sin(angle));
            rays.get(finalI).set(end);

            final float[] minFraction = new float[1];
            RayCastCallback rayFirstPass = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point,
                                              Vector2 normal, float fraction) {
                    // TODO: If we add dynamic cover, will need to change this
                    if (fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
                        rays.get(finalI).set(point);
                        minFraction[0] = fraction;
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
                    if(fraction < minFraction[0] && !bodies.contains(fixture.getBody(), true)) {
                        bodies.add(fixture.getBody());
                    }
                    return minFraction[0];
                }
            };

            world.rayCast(rayFirstPass, origin, end);
            world.rayCast(raySecondPass, origin, end);
            rays.get(i).sub(origin);
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


    /**
     * Modifies the direction of the FOV, maintains range
     * Should be called when the parent obstacle turns around or flips, etc.
     * Could also be used for robots that scan back and forth.
     * @param direction the new direction in radians
     */
    public void setDirection(float direction) {
        this.direction = direction;
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
//        canvas.drawFOV(color, rays, x, y, scalex, scaley);
//        canvas.drawFOV(color, rays, x, y, radius, scalex, scaley);
        canvas.drawRays(DEBUGCOLOR, rays, x, y, scalex, scaley);
    }

    public void setRange(float range) {
        this.range = range;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        resetRadius = true;

    }
}
