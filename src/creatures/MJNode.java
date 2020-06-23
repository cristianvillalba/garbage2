/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
//import com.jme3.bullet.joints.ConeJoint;
//import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Cristian.Villalba
 */
public class MJNode extends Node implements Savable{
    private Box dimensionbox;
    private Sphere dimensionsphere;
    private int objecttype = 0;
    private float localsize = 2.0f;
    private float weight = 0.5f;
    
    private float linearlimit = 1.6f;
    private float anglimit = 2.0f;
    
    private Geometry body;
    private RigidBodyControl body_phy;
    private GhostControl ghost_control;
    private int recursivelimit;
    
    private LinkedList<Integer> joints;
    private HashMap<Integer, MJNode> usedjoints;
    private MJNode copyfrom;
 
    private int id;
    private PhysicsSpace bulletstate;
    private Node rootparent;
    
    /** dimensions used for bricks and wall */
    private static  float brickLength = 0.24f;
    private static  float brickWidth  = 0.48f;
    private static  float brickHeight = 0.12f;
    
    private static  float distance = 7.0f;
    
    public static Geometry target;
    
    private float locallength;
    private float localheight;
    private float localwidth;
    
    //private ArrayList<ConeJoint> jointnexus = new ArrayList<ConeJoint>();
    //private ArrayList<SixDofJoint> jointnexus = new ArrayList<SixDofJoint>();
    private ArrayList<New6Dof> jointnexus = new ArrayList<New6Dof>();
    private Vector3f pivotA;
    private Vector3f pivotB;
    private Vector3f originaltranslation = new Vector3f();

    public enum jtype {
            RIGID,
            REVOLUTE,
            TWIST,
            UNIVERSAL,
            BENDTWIST,
            TWISTBEND,
            SPHERICAL
    }
    
    public MJNode()
    {
        
    }
    
    /*-----copy brain from simetric center--------*/
    public MJNode(MJNode mirrored, MJNode father, Material mats, HashMap<MJNeuron, MJNeuron> sbrain)
    {
        locallength = mirrored.GetLocalLength();
        localheight = mirrored.GetLocalHeight();
        localwidth = mirrored.GetLocalWidth();
        
        bulletstate = mirrored.GetBulletAppState();
        
        
        id = FastMath.nextRandomInt();
        joints = new LinkedList<Integer>();
        usedjoints = new HashMap<Integer, MJNode>();
            
        localsize = mirrored.GetLocalSize();
        objecttype = mirrored.GetObjectType();
                  
        /** Initialize the brick geometry */
        if (objecttype == 0){
            dimensionbox = new Box(locallength, localheight, localwidth);
            dimensionbox.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionbox);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        else{
            dimensionsphere = new Sphere(16, 16, locallength);
            dimensionsphere.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionsphere);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        
        body.setMaterial(mats);
        
        this.attachChild(body);
        
        Vector3f position = mirrored.getLocalTranslation();
        position.x *= -1.0f;
        
        this.setLocalTranslation(position);
        originaltranslation.set(position);
        
        joints.add(0);
        joints.add(1);
        joints.add(2);
        joints.add(3);
        joints.add(4);
        joints.add(5);
        
        for (int i = 0 ; i < mirrored.GetUsedJoints().keySet().toArray().length; i++)
        {
            Integer index = (Integer)mirrored.GetUsedJoints().keySet().toArray()[i];
            
            MJNode tocopy = mirrored.GetUsedJoints().get(index);
            
            int finalindex;
            
            if (index == 2)
            {
                finalindex = 3;
                
            }
            else if (index == 3)
            {
                finalindex = 2;
            }
            else
            {
                finalindex = index;
            }
            
            if (tocopy != mirrored.parent){
                MJNode mirrorednode = new MJNode(tocopy, this, mats, sbrain);
                
                usedjoints.put(finalindex, mirrorednode);
                
            }
            else
            {
                usedjoints.put(finalindex, father);
            }  
            
            joints.removeFirstOccurrence(finalindex);
        }
        
        rootparent = mirrored.GetRootParent();
        rootparent.attachChild(this);
            
        body_phy = new RigidBodyControl(weight);
        
        ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
        this.addControl(body_phy);
        this.addControl(ghost_control);
                
        body_phy.setSleepingThresholds(linearlimit,anglimit);
    }
    
    public MJNode(MJNode orgfather, MJNode orgmother, MJNode father, Material mats, HashMap<MJNode, MJNode> sbrain,int swaplimit, PhysicsSpace state, int swapped, Node root)
    {
        MJNode mirrored = null;
        Vector3f offset = new Vector3f();
        
        if (swapped == 0)
        {
            if (swaplimit > 0)
            {
                mirrored = orgfather;
            }
            else
            {
                mirrored = orgmother;
                swapped = 1;
                swaplimit = orgmother.GetRecursiveLimit()/ 2;
                
                
                offset.set(0f,-1.2f,0f);
            }
        }
        else
        {
            mirrored = orgfather;
        }    
            
        
        
        locallength = mirrored.GetLocalLength();
        localheight = mirrored.GetLocalHeight();
        localwidth = mirrored.GetLocalWidth();
        
        bulletstate = state;
        
        id = FastMath.nextRandomInt();
        
        joints = new LinkedList<Integer>();
        usedjoints = new HashMap<Integer, MJNode>();
            
        localsize = mirrored.GetLocalSize();
        objecttype = mirrored.GetObjectType();
                  
        /** Initialize the brick geometry */
        if (objecttype == 0){
            dimensionbox = new Box(locallength, localheight, localwidth);
            dimensionbox.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionbox);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        else{
            dimensionsphere = new Sphere(16, 16, locallength);
            dimensionsphere.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionsphere);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        
        body.setMaterial(mats);
        
        this.attachChild(body);
        
        Vector3f position = mirrored.getWorldTranslation().clone();
       
        position.addLocal(offset);
        
        
        this.setLocalTranslation(position);
        
        joints.add(0);
        joints.add(1);
        joints.add(2);
        joints.add(3);
        joints.add(4);
        joints.add(5);
        
        sbrain.put(mirrored, this);
        
        
        for (int i = 0 ; i < mirrored.GetUsedJoints().keySet().toArray().length; i++)
        {
            Integer index = (Integer)mirrored.GetUsedJoints().keySet().toArray()[i];
            
            MJNode tocopy = mirrored.GetUsedJoints().get(index);
            
            if (tocopy != mirrored.parent){
                
                
                if (!(swapped == 1 && swaplimit == 1)){
                    MJNode mirrorednode = new MJNode(tocopy, orgmother, this, mats, sbrain, swaplimit - 1, state, swapped, root);
                
                    usedjoints.put(index, mirrorednode);
                    joints.removeFirstOccurrence(index);
                }
                
            }
            else
            {
                usedjoints.put(index, father);
                joints.removeFirstOccurrence(index);
            }  
        }
        
        rootparent = root;
        rootparent.attachChild(this);
            
        
        body_phy = new RigidBodyControl(weight);
       
        ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
        this.addControl(body_phy);
        this.addControl(ghost_control);
        bulletstate.getPhysicsSpace().add(body_phy);
        bulletstate.getPhysicsSpace().add(ghost_control);
        
        body_phy.setSleepingThresholds(linearlimit,anglimit);
        
    }
    
    /*-----------copy complete brain----------*/
    public MJNode(MJNode orgfather, MJNode father, Material mats, HashMap<MJNode, MJNode> sdata, PhysicsSpace state, boolean mutate, Node root)
    {
        MJNode mirrored = orgfather;
            
        locallength = mirrored.GetLocalLength();
        localheight = mirrored.GetLocalHeight();
        localwidth = mirrored.GetLocalWidth();
        
        bulletstate = state;
        
        id = FastMath.nextRandomInt();
        
        joints = new LinkedList<Integer>();
        usedjoints = new HashMap<Integer, MJNode>();
            
        localsize = mirrored.GetLocalSize();
        objecttype = mirrored.GetObjectType();
                  
        /** Initialize the brick geometry */
        if (objecttype == 0){
            dimensionbox = new Box(locallength, localheight, localwidth);
            dimensionbox.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionbox);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        else{
            dimensionsphere = new Sphere(16, 16, locallength);
            dimensionsphere.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionsphere);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        
        body.setMaterial(mats);
        
        this.attachChild(body);
        
        Vector3f position = mirrored.GetOriginalTranslation().clone();
       
        this.setLocalTranslation(position);
        
        originaltranslation.set(position);
        
        joints.add(0);
        joints.add(1);
        joints.add(2);
        joints.add(3);
        joints.add(4);
        joints.add(5);
        
        sdata.put(mirrored, this);
        
        for (int i = 0 ; i < mirrored.GetUsedJoints().keySet().toArray().length; i++)
        {
            Integer index = (Integer)mirrored.GetUsedJoints().keySet().toArray()[i];
            
            MJNode tocopy = mirrored.GetUsedJoints().get(index);
            
            //GANK
            if (tocopy != mirrored.parent){
                MJNode mirrorednode = new MJNode(tocopy, this, mats, sdata, state, mutate, root);
                
                usedjoints.put(index, mirrorednode);
                joints.removeFirstOccurrence(index);
            }
            else
            {
                usedjoints.put(index, father);
                joints.removeFirstOccurrence(index);
            }  
        }
        
        rootparent = root;
        rootparent.attachChild(this);
        
        if (mutate && FastMath.rand.nextFloat() < MJNeuron.mutationrate)
        {
            weight = mirrored.GetWeight() + (FastMath.rand.nextFloat() * 0.1f - 0.05f);
        }
        else
        {
            weight = mirrored.GetWeight();
        }
            
        body_phy = new RigidBodyControl(weight);
        ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
        this.addControl(body_phy);
        this.addControl(ghost_control);
        
        body_phy.setSleepingThresholds(linearlimit,anglimit);
        
    }
    
    public float GetWeight()
    {
        return weight;
    }
    
    public Vector3f GetOriginalTranslation()
    {
        return originaltranslation;
    }
    
    public MJNode(Material mat, Node rootpt, MJNode parent, PhysicsSpace bulletAppState, boolean generate, int i, int joint)
    {        
        locallength = brickLength;
        localheight = brickHeight;
        localwidth = brickWidth;
        
        bulletstate = bulletAppState;
        
        if (generate){
            id = FastMath.nextRandomInt();
            joints = new LinkedList<Integer>();
            usedjoints = new HashMap<Integer, MJNode>();
            
            if (objecttype == 0){
                /*float dim = FastMath.nextRandomFloat() * 2 + 1.0f;
                localheight *= dim;

                dim = FastMath.nextRandomFloat() * 2 + 1.0f;
                locallength *= dim;

                dim = FastMath.nextRandomFloat() * 2 + 1.0f;
                localwidth *= dim;
                */
                
                /*---debug---*/
                localheight = 1.0f;
                localwidth = 1.0f;
                locallength = 1.0f;
                /*---debug---*/
                
            }
            else
            {
                float dim = FastMath.nextRandomFloat() * 2 + 1.0f;
                localheight *= dim;

                locallength = localheight;
                localwidth = localheight;               
            }
            
            joints.add(0);
            joints.add(1);
            joints.add(2);
            joints.add(3);
            joints.add(4);
            joints.add(5);
            
            /*localsize = 1.0f * recursivelimit / recursivelimitmax;
            
            if (FastMath.nextRandomInt(0, 100) > 20)
            {
                localsize = FastMath.nextRandomFloat()*0.2f + 0.8f;
            }*/
            
            localheight *= localsize;
            localwidth *= localsize;
            locallength *= localsize;
                   
        }
        else
        {
            id = i;
        }    
                  
        /** Initialize the brick geometry */
        if (objecttype == 0){
            dimensionbox = new Box(locallength, localheight, localwidth);
            dimensionbox.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionbox);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        else{
            //increase sphere size
            locallength *= 2.0f;
            dimensionsphere = new Sphere(16, 16, locallength);
            dimensionsphere.scaleTextureCoordinates(new Vector2f(1f, .5f));
            body = new Geometry("part", dimensionsphere);
            body.setShadowMode(RenderQueue.ShadowMode.Cast);
        }
        
        body.setMaterial(mat);
        
        if (parent != null)
        {
            ChangePosition(parent, joint);
        }
        else
        {
            /*---For first node-------*/
            //body_phy = new RigidBodyControl(weight * localsize);
            body_phy = new RigidBodyControl(weight);
            ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
            body.addControl(body_phy);
            body.addControl(ghost_control);
         
            body_phy.setSleepingThresholds(linearlimit,anglimit);
        }
        
        
        rootparent = rootpt;
        this.attachChild(body);
        
        if (parent == null){
            rootparent.attachChild(this);
        }
        
        originaltranslation.set(this.getWorldTranslation().clone());
    }
    
    
    public Node GetRootParent()
    {
        return rootparent;
    }
    
    public HashMap<Integer, MJNode> GetUsedJoints()
    {
        return usedjoints;
    }
    
    public PhysicsSpace GetBulletAppState()
    {
        return bulletstate;
    }
    
    public int GetObjectType()
    {
        return objecttype;
    }
    
    
    public int GetId()
    {
        return id;
    }
    
    public int GetRecursiveLimit()
    {
        return recursivelimit;
    }
    
    public void SetRecursiveLimit(int i)
    {
        recursivelimit = i;
    }
    
    public void RandRecursiveLimit()
    {
        //recursivelimit = FastMath.nextRandomInt(1,2); 
        
        //recursivelimit = FastMath.nextRandomInt(4,6); //best option
        //recursivelimit = 2; //recursive other than 1 make incorrect loading of the state
        recursivelimit = 3;//no complex at all fast
        //recursivelimit = 4;//not so complex but good
        //recursivelimit = 5;//slow, complex
        
        //recursivelimit = 5; //worm like
    }
  
    public New6Dof GetJoint()
    {
        if (jointnexus.size() == 0)
        {
            return null;
        }
        else
        {
            return jointnexus.get(0);
        }
    }
    
    public boolean IsMoving()
    {
        boolean finalstate = true;
        
        finalstate = this.body_phy.isActive();
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                
                    if (usedjoints.get(index) != this.parent){
                        finalstate &= usedjoints.get(index).IsMoving();
                    }
                
                
            }
        
        return finalstate;
    }
    
    
    public void SavePreviousPos()
    {
        this.setUserData("previouspos", this.getWorldTranslation().clone());

            for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];
             
                    if (usedjoints.get(index) != this.parent){
                        usedjoints.get(index).SavePreviousPos();
                    }  
            }
        
        
    }
    
    public void PrintPosition()
    {
        MJEnvironmentMain.log.info("pos: " + this.getWorldTranslation());

            for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];
             
                    if (usedjoints.get(index) != this.parent){
                        usedjoints.get(index).PrintPosition();
                    }  
            }
        
        
    }
      
    public boolean CheckCollide(ArrayList<MJNode> allpositions)
    {
        CollisionResults coll = new CollisionResults();
        
            for (int i = 0; i < allpositions.size() ; i++)
            {
                if (allpositions.get(i) != this)
                {
                    if (allpositions.get(i).getWorldTranslation().distance(this.getWorldTranslation()) < 0.01f)
                    {
                        body.removeFromParent();
                        this.removeFromParent();
                        return true;
                    }
                }
                
            }
        
            rootparent.attachChild(this);
            
            body_phy = new RigidBodyControl(weight);
            
            ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
            this.addControl(body_phy);
            this.addControl(ghost_control);
            
            body_phy.setSleepingThresholds(linearlimit,anglimit);
            return false;

    }
    
    public void setMaterial(Material mat)
    {
        body.setMaterial(mat);
        
        for (int i = 0; i < this.children.size();i++)
        {
            this.children.get(i).setMaterial(mat);
        }
    }
    
    
    private void ChangePosition(MJNode parent, int join)
    {
        switch(join)
        {
            case 0:
            {
                Vector3f pos = new Vector3f(parent.getWorldTranslation().x, parent.GetLocalHeight() + localheight + parent.getWorldTranslation().y + distance, parent.getWorldTranslation().z);
                this.setLocalTranslation(pos);
                
                joints.removeFirstOccurrence(1);
                break;
            }
            case 1:
            {
                Vector3f pos = new Vector3f(parent.getWorldTranslation().x, -(parent.GetLocalHeight() + localheight + parent.getWorldTranslation().y + distance),parent.getWorldTranslation().z);
                this.setLocalTranslation(pos);

                joints.removeFirstOccurrence(0);
                break;
            }
            case 2:
            {
                Vector3f pos = new Vector3f(parent.GetLocalLength()+ locallength + parent.getWorldTranslation().x + distance, parent.getWorldTranslation().y,parent.getWorldTranslation().z);
                this.setLocalTranslation(pos);

                joints.removeFirstOccurrence(3);
                break;
            }
            case 3:
            {
                Vector3f pos = new Vector3f(-(parent.GetLocalLength()+ locallength + parent.getWorldTranslation().x + distance), parent.getWorldTranslation().y,parent.getWorldTranslation().z);
                this.setLocalTranslation(pos);

                joints.removeFirstOccurrence(2);
                break;
            }
            case 4:
            {
                Vector3f pos = new Vector3f(parent.getWorldTranslation().x , parent.getWorldTranslation().y,parent.GetLocalWidth()+ localwidth + parent.getWorldTranslation().z + distance);
                this.setLocalTranslation(pos);

                joints.removeFirstOccurrence(5);
                break;
            }
            case 5:
            {
                Vector3f pos = new Vector3f(parent.getWorldTranslation().x , parent.getWorldTranslation().y, -(parent.GetLocalWidth()+ localwidth + parent.getWorldTranslation().z + distance));
                this.setLocalTranslation(pos);

                joints.removeFirstOccurrence(4);
                break;
            }
        }
        
    }
    
    public int GetRandJoint()
    {
        if (joints.size() == 1)
        {
            int value = joints.get(0);
            joints.removeFirst();
            return value;
        }
        else
        {
            //int index = FastMath.nextRandomInt(0, joints.size() - 1); //20-11-2018
            //int value = joints.get(index);
            //joints.remove(index);
            
            int value = joints.get(0); //worm like
            joints.removeFirst(); //worm like
            
            
            return value;
        }
    }
    
    public void ConnectJoints(MJNode node)
    {
        Vector3f jointa = new Vector3f();
        Vector3f jointb = new Vector3f();
           
        jointa.set(this.getWorldTranslation());
        jointb.set(node.getWorldTranslation().subtract(this.getWorldTranslation()).mult(0.5f));
        jointb.addLocal(jointa);
       
        pivotA = this.worldToLocal(jointb, new Vector3f());
        pivotB = node.worldToLocal(jointb, new Vector3f());
        //pivotA = jointa.clone();
        //pivotB = jointa.clone();
        
        //ConeJoint newcone = new ConeJoint(body_phy, node.GetBodyControl(), pivotA, pivotB);
        New6Dof newcone = new New6Dof(body_phy, node.GetBodyControl(), pivotA, pivotB, new Matrix3f(), new Matrix3f(), RotationOrder.XYZ);
        RotationMotor xMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_X);
        //xMotor.set(MotorParam.UpperLimit, 0f);
        //xMotor.set(MotorParam.LowerLimit, 0f);
        xMotor.set(MotorParam.MaxMotorForce, 40000f);
        xMotor.setMotorEnabled(true);
        RotationMotor yMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_Y);
        //yMotor.set(MotorParam.UpperLimit, 0f);
        //yMotor.set(MotorParam.LowerLimit, 0f);
        yMotor.set(MotorParam.MaxMotorForce, 40000f);
        yMotor.setMotorEnabled(true);
        
        RotationMotor motor = newcone.getRotationMotor(PhysicsSpace.AXIS_Z);
        motor.set(MotorParam.MaxMotorForce, 40000f);
        motor.setMotorEnabled(true);
        //SixDofJoint newcone = new SixDofJoint(body_phy, node.GetBodyControl(), pivotA, pivotB, true);
        //newcone.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, FastMath.PI); 
        //newcone.getRotationalLimitMotor(PhysicsSpace.AXIS_Z).setEnableMotor(true);
        
        jointnexus.add(newcone);
          
        //     
    }
    
    public MJNode RandPart()
    {
        int randindex = FastMath.nextRandomInt(0,  usedjoints.keySet().toArray().length - 1);
        
        Integer index = (Integer) usedjoints.keySet().toArray()[randindex];
        
        while (usedjoints.get(index) == this.parent){
            randindex = FastMath.nextRandomInt(0,  usedjoints.keySet().toArray().length - 1);
            index = (Integer) usedjoints.keySet().toArray()[randindex];
        }
        
        if (FastMath.rand.nextBoolean())
        {
            return this;
        }
        else
        {
            return usedjoints.get(index).RandPart();
        }
    }
    
    public void EnterPhysics()
    {
        bulletstate.getPhysicsSpace().add(body_phy);
        bulletstate.getPhysicsSpace().add(ghost_control);
        
        if (jointnexus != null){
            for (int n = 0 ; n < jointnexus.size(); n++){
                bulletstate.getPhysicsSpace().add(jointnexus.get(n));
            }
        }
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
        {
            Integer index = (Integer) usedjoints.keySet().toArray()[i];
            if (usedjoints.get(index) != this.parent){
                usedjoints.get(index).EnterPhysics();
            }
        }
    }
    
    public void ConnectJointsRecursive(MJNode node)
    {
        if (node != null){
            //this.ChangeParent(node);

            Vector3f jointa = new Vector3f();
            Vector3f jointb = new Vector3f();

            jointa.set(this.getWorldTranslation());
            jointb.set(node.getWorldTranslation().subtract(this.getWorldTranslation()).mult(0.5f));
            jointb.addLocal(jointa);
            
            pivotA = this.worldToLocal(jointb, new Vector3f());
            pivotB = node.worldToLocal(jointb, new Vector3f());
            //pivotA = jointa.clone();
            //pivotB = jointa.clone();

            //ConeJoint newcone = new ConeJoint(body_phy, node.GetBodyControl(), pivotA, pivotB);
            //SixDofJoint newcone = new SixDofJoint(body_phy, node.GetBodyControl(), pivotA, pivotB, true);
            //newcone.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, FastMath.PI);
            //newcone.getRotationalLimitMotor(0).setMaxLimitForce(25f);
            New6Dof newcone = new New6Dof(body_phy, node.GetBodyControl(), pivotA, pivotB, new Matrix3f(), new Matrix3f(), RotationOrder.XYZ);
            RotationMotor xMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_X);
            //xMotor.set(MotorParam.UpperLimit, 0f);
            //xMotor.set(MotorParam.LowerLimit, 0f);
            xMotor.set(MotorParam.MaxMotorForce, 40000f);
            xMotor.setMotorEnabled(true);
            RotationMotor yMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_Y);
            //yMotor.set(MotorParam.UpperLimit, 0f);
            //yMotor.set(MotorParam.LowerLimit, 0f);
            yMotor.set(MotorParam.MaxMotorForce, 40000f);
            yMotor.setMotorEnabled(true);

            RotationMotor motor = newcone.getRotationMotor(PhysicsSpace.AXIS_Z);
            motor.set(MotorParam.MaxMotorForce, 40000f);
            motor.setMotorEnabled(true);
            
            
            jointnexus.add(newcone);

        }
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
        {
            Integer index = (Integer) usedjoints.keySet().toArray()[i];
            
            if (usedjoints.get(index) != this.parent){
                usedjoints.get(index).ConnectJointsRecursive(this);
            }
        }
    }
    
    public float GetLocalHeight()
    {
        return localheight;
    }
    
    public float GetLocalLength()
    {
        return locallength;
    }
    
    public float GetLocalWidth()
    {
        return localwidth;
    }
    
    public float GetLocalSize()
    {
        return localsize;
    }
    
    public Material GetMaterial()
    {
        return body.getMaterial();
    }
    
    
    public RigidBodyControl GetBodyControl()
    {
        return body_phy;
    }
    
    public GhostControl GetGhostControl()
    {
        return ghost_control;
    }
    
    
    public void DetachPhysics()
    {
        if (bulletstate != null && ghost_control != null){
            if (bulletstate.getPhysicsSpace() != null){
                bulletstate.getPhysicsSpace().remove(ghost_control);
            }
        }
        
        if (usedjoints != null && usedjoints.size() > 0){
            for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                
                    if (usedjoints.get(index) != this.parent){
                        usedjoints.get(index).DetachPhysics();
                    }
                
                
            }
        }
        
    }
    
    public void GivemeNodes(ArrayList<MJNode> allnodes)
    {
        if (!allnodes.contains(this)){
            allnodes.add(this);
        }
        
        if (usedjoints != null && usedjoints.size() > 0){
            for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                    if (usedjoints.get(index) != this.parent){
                        usedjoints.get(index).GivemeNodes(allnodes);
                    }
            }
        }
        
    }
    
    public float EvaluateHeight()
    {
        float localyheight = getChild(0).getWorldTranslation().y;
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                if (usedjoints.get(index) != this.parent){
                    float childheight = usedjoints.get(index).EvaluateHeight();
                    
                    if (childheight > localyheight)
                    {
                        localyheight = childheight;
                    }
                }
            }
        
        return localyheight;
    } 
    
    public Spatial GetNearest(Vector3f targetpos, Spatial mindistance)
    {
        Spatial newnearest =  null;
        
        if (mindistance == null)
        {
            newnearest = this.getChild(0);
        }
        else
        {
            if (this.getChild(0).getWorldTranslation().distance(targetpos) < mindistance.getWorldTranslation().distance(targetpos))
            {
                newnearest = this.getChild(0);
            }
            else
            {
                newnearest = mindistance;
            }
        }
        
        
        
        Spatial nearestreturn = newnearest;
           
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                if (usedjoints.get(index) != this.parent){
                    Spatial nearest2 = usedjoints.get(index).GetNearest(targetpos, newnearest);
                    
                    if (nearest2.getWorldTranslation().distance(targetpos) < nearestreturn.getWorldTranslation().distance(targetpos))
                    {
                        nearestreturn = nearest2;
                    }
                }
            }
        
        return nearestreturn;
    }
    
    public Vector3f GetAverageVel()
    {
        Vector3f previousvel = (Vector3f)this.getUserData("previouspos");
        
        if (previousvel == null)
        {
            previousvel = new Vector3f();
        }
        
        
        Vector3f vel = this.getWorldTranslation().subtract(previousvel);
        
        /*
        Dont take into consideration childs - only root node
        
        Vector3f childvel = new Vector3f();
        int j = 0;
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                if (usedjoints.get(index) != this.parent){
                    childvel.addLocal(usedjoints.get(index).GetAverageVel());
                    j++;
                }
            }
        
        if (j != 0)
        {
            childvel.divideLocal(j);
            
            vel.addLocal(childvel);
            vel.divideLocal(2);
        }
        */
        return vel;
    }
    
    
    public void KillAll()
    {
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
        {
            Integer index = (Integer) usedjoints.keySet().toArray()[i];

            
                if (usedjoints.get(index) != this.parent){
                    usedjoints.get(index).KillAll();
                }
            
        }
        
        usedjoints.clear();
        joints.clear();
        body.removeFromParent();
        
        this.removeFromParent();
        this.removeControl(body_phy);
        this.removeControl(ghost_control);
             
        body = null;
        body_phy = null;
        ghost_control = null;
        copyfrom = null;
    }
    
    public void RemoveAllBodies()
    {
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
        {
            Integer index = (Integer) usedjoints.keySet().toArray()[i];

            
                if (usedjoints.get(index) != this.parent){
                    usedjoints.get(index).RemoveAllBodies();
                }
            
        }
        
        body.removeFromParent();
        
        this.removeFromParent();
        this.removeControl(body_phy);
        this.removeControl(ghost_control);
    }
    
    
    
    public void SpawnOnEnv(Material mat, Material matsymetric, Node parent)
    {
        bulletstate.getPhysicsSpace().add(body_phy);
        bulletstate.getPhysicsSpace().add(ghost_control);
        
        if (jointnexus != null){
            bulletstate.getPhysicsSpace().add(jointnexus);      
        }
        
        if (parent == null){
            rootparent.attachChild(this);
        }
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                if (usedjoints.get(index) != this.parent){
                    usedjoints.get(index).SpawnOnEnv(mat, matsymetric, this);
                }
            }
    }
    
    public void SpawnOnSpace(Node parent, PhysicsSpace b)
    {
        bulletstate = b;
        bulletstate.getPhysicsSpace().add(body_phy);
        bulletstate.getPhysicsSpace().add(ghost_control);
        
        if (jointnexus != null){
            bulletstate.getPhysicsSpace().add(jointnexus);      
        }
        
        if (parent == null){
            rootparent.attachChild(this);
        }
        
        for (int i = 0; i < usedjoints.keySet().toArray().length; i++)
            {
                Integer index = (Integer) usedjoints.keySet().toArray()[i];

                if (usedjoints.get(index) != this.parent){
                    usedjoints.get(index).SpawnOnSpace(parent, b);
                }
            }
    }
    
    public void RandomizeChild(Material mat,  PhysicsSpace bulletAppState, Node rootparent, int recursivelimit, boolean first, ArrayList<MJNode> allpositions) {
        
        //if (recursivelimit > 0 && joints.size() > 0){
        if (joints.size() > 0){
            int randchilds = 0;
        
            /*if (first)
            {
                randchilds = FastMath.nextRandomInt(1, joints.size() -  1);
            }
            else
            {
                randchilds = FastMath.nextRandomInt(0, joints.size() -  1);
            }*/
            
            //randchilds = 1;
            if (joints.size() == 1){
                randchilds = 1;
            }
            else
            {
                //randchilds = FastMath.nextRandomInt(1, joints.size()); //remove for wormlike 20-11-2018
                randchilds = 1;
            }
            
            for (int i = 0; i < randchilds ; i++)
            {

                int join = GetRandJoint();
                
                
                MJNode node = new MJNode(mat, rootparent, this,  bulletAppState, true, 0, join);
 
                if (!node.CheckCollide(allpositions)){
                    allpositions.add(node);
                    ConnectJoints(node);

                    usedjoints.put(join, node);
                    
                    if (recursivelimit > 0){
                        node.RandomizeChild(mat, bulletAppState, rootparent ,recursivelimit - 1, false, allpositions);
                    }
                    
                    
                }
                else
                {
                    joints.add(join);
                    allpositions.remove(node);
                }
                
                
            }
        }
        
    }
    
    public void SymetricCenter(int index, MJNode father, Material mats, HashMap<MJNeuron, MJNeuron> sbrain)
    {
            if (usedjoints.containsKey(2) && !usedjoints.containsKey(3))
            {
                MJNode symetricnode = new MJNode((MJNode)usedjoints.get(2), this, mats, sbrain);
                
                symetricnode.ConnectJointsRecursive(this);
                
                joints.removeFirstOccurrence(3);
                usedjoints.put(3, symetricnode); 
            }
            
            if (!usedjoints.containsKey(2) && usedjoints.containsKey(3))
            {
                MJNode symetricnode = new MJNode((MJNode)usedjoints.get(3), this, mats, sbrain);
                
                symetricnode.ConnectJointsRecursive(this);
                
                joints.removeFirstOccurrence(2);
                usedjoints.put(2, symetricnode);
            }
            
            if (usedjoints.containsKey(0) && usedjoints.get(0) != father)
            {
                MJNode center = usedjoints.get(0);
                center.SymetricCenter(0,this, mats, sbrain);
            }
            
            if (usedjoints.containsKey(1) && usedjoints.get(1) != father)
            {
                MJNode center = usedjoints.get(1);
                center.SymetricCenter(1,this, mats, sbrain);
            }
            
            if (usedjoints.containsKey(4) && usedjoints.get(4) != father)
            {
                MJNode center = usedjoints.get(4);
                center.SymetricCenter(4,this, mats, sbrain);
            }
            
            if (usedjoints.containsKey(5) && usedjoints.get(5) != father)
            {
                MJNode center = usedjoints.get(5);
                center.SymetricCenter(5,this, mats, sbrain);
            }
    }
    
    
    public void SymetricRand(Material mats, boolean mutate)
    {
        int rand = FastMath.nextRandomInt(0, 10);
        
        HashMap<MJNeuron, MJNeuron> symetricbrain = new HashMap<MJNeuron, MJNeuron>();
       
        
        if (rand < 11)
        {
            if (usedjoints.containsKey(2) && !usedjoints.containsKey(3))
            {
                MJNode symetricnode = new MJNode((MJNode)usedjoints.get(2), this, mats, symetricbrain);
                
                symetricnode.ConnectJointsRecursive(this);
                
                
                joints.removeFirstOccurrence(3);
                usedjoints.put(3, symetricnode); 
            }
            
            if (!usedjoints.containsKey(2) && usedjoints.containsKey(3))
            {
                MJNode symetricnode = new MJNode((MJNode)usedjoints.get(3), this, mats, symetricbrain);
                
                symetricnode.ConnectJointsRecursive(this);
                
                
                joints.removeFirstOccurrence(2);
                usedjoints.put(2, symetricnode);
            }
            
            if (usedjoints.containsKey(0))
            {
                MJNode center = usedjoints.get(0);
                center.SymetricCenter(0,this, mats, symetricbrain);
                
            }
            
            if (usedjoints.containsKey(1))
            {
                MJNode center = usedjoints.get(1);
                center.SymetricCenter(1,this, mats, symetricbrain);
                
            }
            
            if (usedjoints.containsKey(4))
            {
                MJNode center = usedjoints.get(4);
                center.SymetricCenter(4,this, mats, symetricbrain);
                
            }
            
            if (usedjoints.containsKey(5))
            {
                MJNode center = usedjoints.get(5);
                center.SymetricCenter(5,this, mats, symetricbrain);
                
            }
        }
    }
    
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        
        capsule.write(dimensionbox,   "dimensionbox",   null);
        capsule.write(dimensionsphere,   "dimensionsphere",   null);
        capsule.write(objecttype,   "objecttype",   0);
        capsule.write(localsize,   "localsize",   0);
        capsule.write(weight,   "weight",   0);
        capsule.write(body,   "body",   null);
        capsule.write(body_phy,   "body_phy",   null);
        capsule.write(ghost_control,   "ghost_control",   null);
        capsule.write(recursivelimit,   "recursivelimit",   0);
        capsule.write(copyfrom,   "copyfrom",   null);
        capsule.write(id,   "id",   0);
        //capsule.write(bulletstate,   "bulletstate",   bulletstate);
        capsule.write(rootparent,   "rootparent",   null);
        capsule.write(brickLength,   "brickLength",   0);
        capsule.write(brickWidth,   "brickWidth",   0);
        capsule.write(brickHeight,   "brickHeight",   0);
        capsule.write(distance,   "distance",   0);
        capsule.write(target,   "target",   null);
        capsule.write(locallength,   "locallength",   0);
        capsule.write(localheight,   "localheight",   0);
        capsule.write(localwidth,   "localwidth",   0);
        //capsule.write(jointnexus,   "jointnexus",   null);
        capsule.writeSavableArrayList(jointnexus,   "jointnexus",   null);
        
        capsule.write(pivotA,   "pivotA",   null);
        capsule.write(pivotB,   "pivotB",   null);
        capsule.write(originaltranslation,   "originaltranslation",   null);
        capsule.write(this.parent,"parentmjnode", null);
        
        int[] intarray = new int[joints.size()];
        for (int i = 0; i < joints.size(); i++)
        {
            intarray[i] = joints.get(i);
        }
        
        capsule.write(intarray, "joints",   null);
        
        ArrayList<MJNode> usedjoinstr = new ArrayList<MJNode>();
        
        for (int i = 0; i < 6; i++)
        {
            if (usedjoints.containsKey(i))
            {
                usedjoinstr.add(usedjoints.get(i));
            }
            else
            {
                usedjoinstr.add(null);
            }
        }
        
        capsule.writeSavableArrayList(usedjoinstr, "usedjoints", null);
      
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);

        dimensionbox = (Box)capsule.readSavable("dimensionbox",   null);
        dimensionsphere = (Sphere)capsule.readSavable("dimensionsphere",   null);
        objecttype = capsule.readInt("objecttype",   0);
        localsize = capsule.readFloat("localsize",   0);
        weight = capsule.readFloat("weight",   0);
        body = (Geometry)capsule.readSavable("body",   null);
        
        //try{
        //    body_phy = (RigidBodyControl)capsule.readSavable("body_phy",   null);
        //    ghost_control = (GhostControl)capsule.readSavable("ghost_control",   null);
        //}
        //catch(Exception n)
        //{
            body_phy = new RigidBodyControl(weight);
            ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));
            this.addControl(body_phy);
            this.addControl(ghost_control);
        //}
        
        
        recursivelimit = capsule.readInt( "recursivelimit",   0);
        copyfrom = (MJNode)capsule.readSavable("copyfrom",   null);
        id = capsule.readInt("id",   0);
        //capsule.write(bulletstate,   "bulletstate",   bulletstate);
        rootparent = (Node)capsule.readSavable("rootparent",   null);
        brickLength = capsule.readFloat("brickLength",   0);
        brickWidth = capsule.readFloat("brickWidth",   0);
        brickHeight = capsule.readFloat("brickHeight",   0);
        distance = capsule.readFloat("distance",   0);
        target = (Geometry)capsule.readSavable("target",   null);
        locallength = capsule.readFloat("locallength",   0);
        localheight = capsule.readFloat("localheight",   0);
        localwidth = capsule.readFloat("localwidth",   0);
        //jointnexus = (ConeJoint)capsule.readSavable("jointnexus",   null);
        jointnexus = capsule.readSavableArrayList("jointnexus",   null);
        pivotA = (Vector3f)capsule.readSavable("pivotA",   null);
        pivotB = (Vector3f)capsule.readSavable("pivotB",   null);
        originaltranslation = (Vector3f)capsule.readSavable("originaltranslation",   null);
        
        this.parent = (Node)capsule.readSavable("parentmjnode",   null);
        
        int[] intarray = capsule.readIntArray("joints", null);
        joints = new LinkedList<Integer>();
        
        for (int i = 0; i < intarray.length; i++)
        {
            joints.add(intarray[i]);
        }
                
        ArrayList<MJNode> usedjoinstr = capsule.readSavableArrayList("usedjoints", null);
        usedjoints = new HashMap<Integer, MJNode>();
        
        for (int i = 0; i < 6; i++)
        {
            if (usedjoinstr.get(i) != null)
            {
                usedjoints.put(i, usedjoinstr.get(i));
            }
        }
        
    }
}
