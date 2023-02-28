package edu.cornell.gdiac.json;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import com.badlogic.gdx.utils.Disposable;


/** Fields of vision for enemies */
public class Vision {

    /** The color of the FOV graphic. */
    private Color color;

    /** The direction of the FOV. In LibGdx, 0 is up. */
    private float direction;
    /** The range in radians of the FOV */
    private float range;
    /** The length of the FOV. */
    private float radius;
    /** The number of rays being cast */
    private int numRays = 5;

    private Obstacle origin;


    public Vision(float radius, float direction, float range, Color color, Obstacle origin) {
        this.color = color;
        this.radius = radius;
        this.direction = direction;
        this.range = range;
        this.origin = origin;
    }

    /** Creates a sphere of vision */
    public Vision(float radius, Color color, Obstacle origin) {
        this.color = color;
        this.radius = radius;
        this.direction = direction;
        this.range = range;
        this.origin = origin;
    }


    public void test(){
        Vector2 p1 = new Vector2(), p2 = new Vector2(), collision = new Vector2(), normal = new Vector2();
    }

    /** modifies direction but maintains range */
    public void setDirection(float direction) {
        this.direction = direction;
    }

    public boolean seen(final Obstacle check, World world) {
        if(!near(check)) return false;
        final boolean[] found = {false};
        float startAngle = (direction - range/2) % (float) Math.PI;
        for(int i = 0; i<numRays; i++) {
            float angle = startAngle +  i*(range/numRays) % (float) Math.PI;
            RayCastCallback ray = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                    if(fixture.getBody().equals(check.getBody())) {
                        found[0] = true;
                        return 0f; //continues looking
                    }
                    return -1f;
                }
            };
            world.rayCast(ray, origin.getPosition(), new Vector2((float)Math.cos(angle), (float)Math.sin(angle)));
            if(found[0]) return true;
        }
        return false;
    }

    //for use in seen above, uses radius and dot product check
    private boolean near(Obstacle check) {
        Vector2 pos = origin.getPosition();
        Vector2 checkPos = check.getPosition();
        Vector2 distance = pos.sub(checkPos);
        if(distance.len()>radius) return false;

        Vector2 heading = new Vector2((float) Math.cos(direction), (float) Math.sin(direction));
        float angleDst = distance.dot(heading);
        return angleDst < range;
    }

    public Array<Body> getBodiesInView(World world) {
        float startAngle = (direction - range/2) % (float) Math.PI;
        final Array<Body> bodies = new Array<Body>();
        for(int i = 0; i<numRays; i++) {
            float angle = startAngle + i * (range / numRays) % (float) Math.PI;
            RayCastCallback ray = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                    if(!bodies.contains(fixture.getBody(), true)) bodies.add(fixture.getBody());
                    return -1f;
                }
            };
            world.rayCast(ray, origin.getPosition(), new Vector2((float) Math.cos(angle), (float) Math.sin(angle)));
        }
        return bodies;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.drawFOV(color, 1000000, 100, 1, 1);
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
//    public void drawDebug(GameCanvas canvas) {
//        if (color != null) {
//            for(PolygonShape tri : shapes) {
//                canvas.drawPhysics(tri,color,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
//            }
//        }
//    }






}
