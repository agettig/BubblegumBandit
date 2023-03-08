package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;


/**
 * Represents a tuple of a Bubblegum object and the joint it
 * created when it collided with a body. The joint is either
 * a Joint or JointDef object.
 * */
public class GumJointPair {

    /**The Bubblegum object */
    private Bubblegum gum;

    /**The WeldJoint attached to the Bubblegum in this GumJointPair */
    private WeldJoint joint;

    /**The WeldJointDef attached to the Bubblegum in this GumJointPair */
    private WeldJointDef jointDef;


    /**Instantiates a GumJointPair with a Bubblegum object and a WeldJoint
     * object.
     *
     * @param gum The Bubblegum object in the pair
     * @param joint the WeldJoint attached to the Bubblegum object passed into
     *              this constructor
     *
     * */
    public GumJointPair(Bubblegum gum, WeldJoint joint){
        assert gum != null;
        assert joint != null;

        this.gum = gum;
        this.joint = joint;
    }

    /**Instantiates a GumJointPair with a Bubblegum object and a WeldJointDef
     * object.
     *
     * @param gum The Bubblegum object in the pair
     * @param jointDef the WeldJointDef attached to the Bubblegum object passed into
     *              this constructor
     *
     * */
    public GumJointPair(Bubblegum gum, WeldJointDef jointDef){
        assert gum != null;
        assert jointDef != null;

        this.gum = gum;
        this.jointDef = jointDef;
    }

    /**
     * Returns the Bubblegum object in this GumJointPair.
     *
     * @returns the Bubblegum object in this GumJointPair.
     * */
    public Bubblegum getGum() {
        return gum;
    }

    /**
     * Returns the WeldJoint object in this GumJointPair.
     *
     * @returns the WeldJoint object in this GumJointPair.
     * */
    public WeldJoint getJoint(){
        return joint;
    }

    /**
     * Returns the WeldJointDef object in this GumJointPair.
     *
     * @returns the WeldJointDef object in this GumJointPair.
     * */
    public WeldJointDef getJointDef(){
        return jointDef;
    }
}
