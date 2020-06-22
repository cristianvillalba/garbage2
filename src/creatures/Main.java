package creatures;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.MultiBodyAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jme3utilities.minie.FilterAll;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener{
    
    private RigidBodyControl    floor_phy;
    private RigidBodyControl    wall01_phy;
    private RigidBodyControl    wall02_phy;
    private RigidBodyControl    wall03_phy;
    private RigidBodyControl    wall04_phy;
    private Box    floor;
    private Box    wall01;
    private Box    wall02;
    private Box    wall03;
    private Box    wall04;
    Material wall_mat;
    Material stone_mat;
    Material floor_mat;
    Material redmat;
    Material greenmat;
    Material bluemat;
    MultiBodyAppState bulletstate;
    boolean canlog = false;
    public static Logger log;
    ArrayList<RigidBodyControl> wormbody;
    ArrayList<Node> worm; 
    
    
    public static void main(String[] args) {
        log = Logger.getLogger("BulletLog");  
        FileHandler fh;
        try {
            fh = new FileHandler("bulletlog.log");  
            log.addHandler(fh);
            CustomLogFormatter formatter = new CustomLogFormatter();  
            fh.setFormatter(formatter);  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(50f);
        wormbody = new ArrayList<RigidBodyControl>();
        worm = new ArrayList<Node>(); 
        
        initMaterials();
        
        bulletstate = LoadPhysics();
        initFloor(bulletstate);  
        
        registerInput();
    }
    
    public void initMaterials() {
      wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      TextureKey key = new TextureKey("Textures/alien_15.jpg");
      key.setGenerateMips(true);
      Texture tex = assetManager.loadTexture(key);
      wall_mat.setTexture("ColorMap", tex);

      stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      TextureKey key2 = new TextureKey("Textures/alien_15.jpg");
      key2.setGenerateMips(true);
      Texture tex2 = assetManager.loadTexture(key2);
      stone_mat.setTexture("ColorMap", tex2);

      floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      TextureKey key3 = new TextureKey("Textures/hardwood.jpg");
      key3.setGenerateMips(true);
      Texture tex3 = assetManager.loadTexture(key3);
      tex3.setWrap(Texture.WrapMode.Repeat);
      floor_mat.setTexture("ColorMap", tex3);
      
      redmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      redmat.setColor("Color", ColorRGBA.Red);
      
      greenmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      greenmat.setColor("Color", ColorRGBA.Green);
      
      bluemat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      bluemat.setColor("Color", ColorRGBA.Blue);
    }
    
    public void initFloor(BulletAppState state) {
        floor = new Box(240f, 0.1f, 240f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
        
        wall01 = new Box(240f, 120.0f, 20.0f);
        wall01.scaleTextureCoordinates(new Vector2f(3, 6));
        
        wall02 = new Box(240f, 120.0f, 20.0f);
        wall02.scaleTextureCoordinates(new Vector2f(3, 6));
        
        wall03 = new Box(20.0f, 120.0f, 240f);
        wall03.scaleTextureCoordinates(new Vector2f(3, 6));
        
        wall04= new Box(20.0f, 120.0f, 240f);
        wall04.scaleTextureCoordinates(new Vector2f(3, 6));
        
        
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -20.0f, 0);
        this.rootNode.attachChild(floor_geo);

        floor_phy = new RigidBodyControl(0.0f);

        floor_geo.addControl(floor_phy);
        floor_geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        state.getPhysicsSpace().add(floor_phy);


        Geometry wall01_geo = new Geometry("wall01", wall01);
        wall01_geo.setMaterial(floor_mat);
        wall01_geo.setLocalTranslation(0f, -20.0f, -240f);
        this.rootNode.attachChild(wall01_geo);

        wall01_phy = new RigidBodyControl(0.0f);

        wall01_geo.addControl(wall01_phy);
        wall01_geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        state.getPhysicsSpace().add(wall01_phy);


        Geometry wall02_geo = new Geometry("wall02", wall02);
        wall02_geo.setMaterial(floor_mat);
        wall02_geo.setLocalTranslation(0f, -20.0f, 240f);
        this.rootNode.attachChild(wall02_geo);

        wall02_phy = new RigidBodyControl(0.0f);

        wall02_geo.addControl(wall02_phy);
        wall02_geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        state.getPhysicsSpace().add(wall02_phy);


        Geometry wall03_geo = new Geometry("wall03", wall03);
        wall03_geo.setMaterial(floor_mat);
        wall03_geo.setLocalTranslation(-240f, -20.0f, 0);
        this.rootNode.attachChild(wall03_geo);

        wall03_phy = new RigidBodyControl(0.0f);

        wall03_geo.addControl(wall03_phy);
        wall03_geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        state.getPhysicsSpace().add(wall03_phy);


        Geometry wall04_geo = new Geometry("wall04", wall04);
        wall04_geo.setMaterial(floor_mat);
        wall04_geo.setLocalTranslation(240f, -20.0f, 0);
        this.rootNode.attachChild(wall04_geo);

        wall04_phy = new RigidBodyControl(0.0f);

        wall04_geo.addControl(wall04_phy);
        wall04_geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        state.getPhysicsSpace().add(wall04_phy);
       
      }
    
    public void registerInput() {
        inputManager.addMapping("loadcreature",new KeyTrigger(keyInput.KEY_1));
        
        inputManager.addListener(this, "loadcreature");
    }
    
    
    private MultiBodyAppState LoadPhysics()
    {
        MultiBodyAppState bulletAppState = new MultiBodyAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        bulletAppState.setDebugEnabled(true);
        BulletDebugAppState.DebugAppStateFilter selectAll = new FilterAll(true);
        bulletAppState.setDebugVelocityVectorFilter(selectAll);
        
        
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setAccuracy(1/120f);
        
            
        return bulletAppState;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!canlog && wormbody.size() > 0)
        {
            boolean issteady = false;
            for (int i = 0 ; i < wormbody.size(); i++)
            {
                issteady |= wormbody.get(i).isActive();
            }
            
            if (!issteady)
            {
                for (int i = 0 ; i < worm.size(); i++)
                {
                    log.info("body n" + i + " : " + worm.get(i).getChild(0).getWorldTranslation());
                }
                canlog = true;
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }
    
    private void loadCreature()
    {
        float ypos = 0;  
        ArrayList<GhostControl> wormghost = new ArrayList<GhostControl>();
        ArrayList<New6Dof> wormjoint = new ArrayList<New6Dof>();
        
        for (int i = 0 ; i < 4 ; i++, ypos -= 5.0f)
        {
            Box dimensionbox = new Box(1.0f, 1.0f, 1.0f);
            Geometry body = new Geometry("part", dimensionbox);
            body.setMaterial(redmat);

            RigidBodyControl body_phy = new RigidBodyControl(0.5f);

            GhostControl ghost_control = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,0.5f,0.5f)));

            Node childnode = new Node();
            childnode.setLocalTranslation(0, ypos, 0);
            childnode.attachChild(body);
            body.addControl(body_phy);
            body.addControl(ghost_control);

            rootNode.attachChild(childnode);
            
                                  
            worm.add(childnode);
            wormbody.add(body_phy);
            wormghost.add(ghost_control);
        }
        
        for (int i = 0 ; i < 3; i++)
        {
            Vector3f jointa = new Vector3f();
            Vector3f jointb = new Vector3f();

            jointa.set(worm.get(i).getWorldTranslation());
            jointb.set(worm.get(i+1).getWorldTranslation().subtract(worm.get(i).getWorldTranslation()).mult(0.5f));
            jointb.addLocal(jointa);
            
            Vector3f pivotA = worm.get(i).worldToLocal(jointb, new Vector3f());
            Vector3f pivotB = worm.get(i+1).worldToLocal(jointb, new Vector3f());
            
            New6Dof newcone = new New6Dof(wormbody.get(i), wormbody.get(i+1), pivotA, pivotB, new Matrix3f(), new Matrix3f(), RotationOrder.XYZ);
            RotationMotor xMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_X);

            xMotor.set(MotorParam.MaxMotorForce, 40000f);
            xMotor.setMotorEnabled(true);
            RotationMotor yMotor = newcone.getRotationMotor(PhysicsSpace.AXIS_Y);

            yMotor.set(MotorParam.MaxMotorForce, 40000f);
            yMotor.setMotorEnabled(true);

            RotationMotor motor = newcone.getRotationMotor(PhysicsSpace.AXIS_Z);
            motor.set(MotorParam.MaxMotorForce, 40000f);
            motor.setMotorEnabled(true);
            
            wormjoint.add(newcone);
        }
        
        for (int i = 0; i < 4 ; i++)
        {
            bulletstate.getPhysicsSpace().add(wormbody.get(i));
            bulletstate.getPhysicsSpace().add(wormghost.get(i));
        }
        
        for (int i = 0; i < 3 ; i++)
        {
            bulletstate.getPhysicsSpace().add(wormjoint.get(i));
        }
    }

    @Override
    public void onAction(String string, boolean isPressed, float tpf) {
        if (string.equals("loadcreature") && isPressed)
        {
            loadCreature();
        }
    }
}
