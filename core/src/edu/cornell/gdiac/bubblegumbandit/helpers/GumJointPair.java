package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;


/**
 * Represents a tuple of a GumModel object and the joint it
 * created when it collided with a body. The joint is either
 * a Joint or JointDef object.
 * */
public class GumJointPair {

    /**The GumModel object */
    private GumModel gum;

    /**The WeldJoint attached to the GumModel in this GumJointPair */
    private WeldJoint joint;

    /**The WeldJointDef attached to the GumModel in this GumJointPair */
    private WeldJointDef jointDef;


    /**Instantiates a GumJointPair with a GumModel object and a WeldJoint
     * object.
     *
     * @param gum The GumModel object in the pair
     * @param joint the WeldJoint attached to the GumModel object passed into
     *              this constructor
     *
     * */
    public GumJointPair(GumModel gum, WeldJoint joint){
        assert gum != null;
        assert joint != null;

        this.gum = gum;
        this.joint = joint;
    }

    /**Instantiates a GumJointPair with a GumModel object and a WeldJointDef
     * object.
     *
     * @param gum The GumModel object in the pair
     * @param jointDef the WeldJointDef attached to the GumModel object passed into
     *              this constructor
     *
     * */
    public GumJointPair(GumModel gum, WeldJointDef jointDef){
        assert gum != null;
        assert jointDef != null;

        this.gum = gum;
        this.jointDef = jointDef;
    }

    /**
     * Returns the GumModel object in this GumJointPair.
     *
     * @returns the GumModel object in this GumJointPair.
     * */
    public GumModel getGum() {
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
