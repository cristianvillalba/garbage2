/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

//import com.bulletphysics.dynamics.constraintsolver.SolverMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.MultiBodyAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import jme3utilities.minie.FilterAll;

/**
 *
 * @author Cristian.Villalba
 */
public class MJEnvironmentMain extends SimpleApplication implements AnalogListener, ActionListener{
    
    /** Prepare Materials */
    Material wall_mat;
    Material stone_mat;
    Material floor_mat;
    Material redmat;
    Material greenmat;
    Material bluemat;
  
    private static final Box    box;
    private RigidBodyControl    floor_phy;
    private RigidBodyControl    wall01_phy;
    private RigidBodyControl    wall02_phy;
    private RigidBodyControl    wall03_phy;
    private RigidBodyControl    wall04_phy;
    private static final Box    floor;
    private static final Box    wall01;
    private static final Box    wall02;
    private static final Box    wall03;
    private static final Box    wall04;
    
    /** dimensions used for bricks and wall */
    private static final float brickLength = 0.48f;
    private static final float brickWidth  = 0.24f;
    private static final float brickHeight = 0.12f;
    
    public static final boolean FIXEDSTEP = false;
    public static final float FIXEDSTEPSIZE = 0.003f;
    
    private Vector3f upforce = new Vector3f(0, 400, 0);
    
    private Vector3f creatupforce = new Vector3f(0, 0, -100);
    private Vector3f creatdownforce = new Vector3f(0, 0,100);
    private Vector3f creatleftforce = new Vector3f(-100, 0, 0);
    private Vector3f creatrightforce = new Vector3f(100, 0, 0);
    
    private boolean applyForce = false;
    
    private boolean applyForceUp = false;
    private boolean applyForceDown = false;
    private boolean applyForceLeft = false;
    private boolean applyForceRight = false;
    
    private Creature creature;
    
    private float fixedtime = 0f;
    
    private boolean speedup = true;
    //private float maxtimer = 240f;
    private float maxtimer = 60f;
    private int checkspertimer = 5;
    private float checkactualtimer = 0f;
    private float checkmaxtimer = maxtimer / checkspertimer;
    
    private int checkvelpertimer = 10;//check how many velocities per target change
    private float checkvelactualtimer = 0f;
    private float checkvelmaxtimer = checkmaxtimer / checkvelpertimer;
    
    
    //private float maxpopulation = 200;
    //private float maxsurvivals = maxpopulation*0.2f;
    private float maxpopulation = 3;
    private float maxsurvivals = 1;
    private int maxparallelchecks = 5;
    private int creaturestospawn;
    private int state = 0;
    
    private ArrayList<Creature> populationobserved;
    private ArrayList<Creature> populationselection;
    private LinkedList<Creature> population;
    private ArrayList<Creature> populationgarbage;
    private Creature backupcreature;
    
    private BitmapText hudText;
    private int generation = 0;
    
    private int typeoffitness = 2; //this will try to train the NN to follow a target
    public static Geometry target;
    
    private boolean accuracy = true;
    private boolean canmove = false;
    private boolean cancheck = false;
    private boolean finallyplay = false;
    
    private ArrayList<Vector3f> randpositions = new ArrayList<Vector3f>();
    private int indexrandpositions = 0;
    private int alivecounter = 0;
      
    final int SHADOWMAP_SIZE=1024;
      
    static {
        box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, .5f));
        /** Initialize the floor geometry */
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
  }
    
    public static void main(String args[]) {
        MJEnvironmentMain app = new MJEnvironmentMain();
        app.setPauseOnLostFocus(false);
        AppSettings newSettings = new AppSettings(true);

        newSettings.setFrameRate(30);

        app.setSettings(newSettings);
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(30f);
        
        populationobserved = new ArrayList<Creature>();
        populationselection = new ArrayList<Creature>();
        population = new LinkedList<Creature>();
        populationgarbage = new ArrayList<Creature>();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        
        LoadLight();
        LoadPhysics();
        LoadHUD();
        
        initRandPositions(false);
        
        LoadTarget();
        
        
        initMaterials();
        initEnv();
        initCam();
        
        
        
        registerInput();
       
    }

    public void onAnalog(String name, float value, float tpf) {

        if (name.equals("targetup") && (state == 2 || state == 6)){
             target.move(0f, 0f, -0.3f);
        }
        if (name.equals("targetdown") && (state == 2 || state == 6)) {
             target.move(0f, 0f, 0.3f);
        }
        if (name.equals("targetleft") && (state == 2 || state == 6)) {
             target.move(-0.3f, 0f, 0f);
        }
        if (name.equals("targetright") && (state == 2 || state == 6)) {
             target.move(0.3f, 0f, 0f);
        }   
    }
    
    private void LoadHUD()
    {
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudText.setColor(ColorRGBA.Blue);                             // font color
        hudText.setText("Init population");             // the text
        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0); // position
        guiNode.attachChild(hudText);
    }
    
    private void LoadTarget()
    {
        target = new Geometry("Target", box);
        MJNode.target = MJEnvironmentMain.target;
        
        Material redmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redmat.setColor("Color", ColorRGBA.Red);
        target.setMaterial(redmat);
        
        target.setLocalTranslation(randpositions.get(indexrandpositions));
        target.setLocalScale(5);
        
        rootNode.attachChild(target);
    }
    
    private void initRandPositions(boolean clear)
    {
        if (clear)
        {
            randpositions.clear();
        }
        
        for (int i = 0; i < checkspertimer; i++)
        {
            Vector3f randpos = new Vector3f(FastMath.nextRandomFloat()*300 - 150f,FastMath.nextRandomFloat()*10 -20.0f, FastMath.nextRandomFloat()*300 - 150f);
            randpositions.add(randpos);
        }
    }
    
    
            
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if ("ragdollup".equals(name)) {
            if (isPressed) {
                applyForce = true;
            } else {
                applyForce = false;
            }
        }
        
        if ("creatureup".equals(name)) {
            if (isPressed) {
                applyForceUp = true;
            } else {
                applyForceUp = false;
            }
        }
        
        if ("creaturedown".equals(name)) {
            if (isPressed) {
                applyForceDown = true;
            } else {
                applyForceDown = false;
            }
        }
        
        if ("creatureleft".equals(name)) {
            if (isPressed) {
                applyForceLeft = true;
            } else {
                applyForceLeft = false;
            }
        }
        
        if ("creatureright".equals(name)) {
            if (isPressed) {
                applyForceRight = true;
            } else {
                applyForceRight = false;
            }
        }
        
        if ("speedup".equals(name)) {
            if (isPressed) {

                if (speedup)
                {
                    
                    maxtimer = 60f;
                    checkmaxtimer = maxtimer / checkspertimer;
                    checkvelmaxtimer = checkmaxtimer / checkvelpertimer;
                    
                    System.out.println("Time loop 60 s");
                    speedup = false;
                }
                else
                {
                    maxtimer = 240f;
                    checkmaxtimer = maxtimer / checkspertimer;
                    checkvelmaxtimer = checkmaxtimer / checkvelpertimer;
                    
                    System.out.println("Time loop 240 s");
                    speedup = true;
                }
            }
        }
        
        if ("spawncreature".equals(name)) {
            if (isPressed) {
                Creature creat = populationobserved.get(0);
                
                creat.SpawnOnEnv(wall_mat,stone_mat, null, stateManager);
            }
        }
        
        if (name.equals("savestate") && isPressed)
        {
            SaveState();
        }
        
        if (name.equals("loadstate") && isPressed)
        {
            LoadState();
        }
        
        if (name.equals("playstate") && isPressed && state != 2)
        {
            PlayState();
        }
        
        if (name.equals("changetarget") && isPressed)
        {
            initRandPositions(true);
        }
        
        if (name.equals("enterphy") && isPressed)
        {
            enterPhysics();
        }
        
        if (name.equals("stopevolutionstate") && isPressed && state != 2)
        {
            StopEvolState();
        }
    }
    
    /** Initialize the materials used in this scene. */
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
      tex3.setWrap(WrapMode.Repeat);
      floor_mat.setTexture("ColorMap", tex3);
      
      redmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      redmat.setColor("Color", ColorRGBA.Red);
      
      greenmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      greenmat.setColor("Color", ColorRGBA.Green);
      
      bluemat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      bluemat.setColor("Color", ColorRGBA.Blue);
    }
    
    public void registerInput() {
        inputManager.addMapping("speedup",new KeyTrigger(keyInput.KEY_1));
        inputManager.addMapping("loadstate",new KeyTrigger(keyInput.KEY_8));
        inputManager.addMapping("savestate",new KeyTrigger(keyInput.KEY_9));
        inputManager.addMapping("playstate",new KeyTrigger(keyInput.KEY_5));
        inputManager.addMapping("stopevolutionstate",new KeyTrigger(keyInput.KEY_2));
        
        inputManager.addMapping("targetup",new KeyTrigger(keyInput.KEY_U));
        inputManager.addMapping("targetdown",new KeyTrigger(keyInput.KEY_J));
        inputManager.addMapping("targetleft",new KeyTrigger(keyInput.KEY_H));
        inputManager.addMapping("targetright",new KeyTrigger(keyInput.KEY_K));
        
        inputManager.addMapping("creatureup",new KeyTrigger(keyInput.KEY_F));
        inputManager.addMapping("creaturedown",new KeyTrigger(keyInput.KEY_V));
        inputManager.addMapping("creatureleft",new KeyTrigger(keyInput.KEY_C));
        inputManager.addMapping("creatureright",new KeyTrigger(keyInput.KEY_B));
        
        inputManager.addListener(this, "speedup");
        inputManager.addListener(this, "loadstate");
        inputManager.addListener(this, "savestate");
        inputManager.addListener(this, "playstate");
        inputManager.addListener(this, "targetup");
        inputManager.addListener(this, "targetdown");
        inputManager.addListener(this, "targetleft");
        inputManager.addListener(this, "targetright");
        inputManager.addListener(this, "stopevolutionstate");
        
        inputManager.addListener(this, "creatureup");
        inputManager.addListener(this, "creaturedown");
        inputManager.addListener(this, "creatureleft");
        inputManager.addListener(this, "creatureright");
    }
    
    //private BulletAppState LoadPhysics()
    private MultiBodyAppState LoadPhysics()
    {
        MultiBodyAppState bulletAppState = new MultiBodyAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        //bulletAppState.setThreadingType(BulletAppState.ThreadingType.SEQUENTIAL);
        bulletAppState.setDebugEnabled(true);
        BulletDebugAppState.DebugAppStateFilter selectAll = new FilterAll(true);
        bulletAppState.setDebugVelocityVectorFilter(selectAll);
        
        
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setAccuracy(1f/120f);
        //bulletAppState.getPhysicsSpace().setMaxSubSteps(1);
        
            
        return bulletAppState;
    }
    
    public void wakeCreature()
    {
        alivecounter++;
        
        if (alivecounter == populationobserved.size())
        {
            for (int i = 0; i < populationobserved.size(); i++)
            {
                populationobserved.get(i).GetRootNode().GetBodyControl().activate();
            }
            moveTarget();
        }
    }
    
    public boolean canAllMove()
    {
        if (alivecounter ==  populationobserved.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private void LoadLight()
    {
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(2500f);
        lamp_light.setPosition(new Vector3f(0.0f,250.0f,200.0f));
        
        PointLight lamp_light2 = new PointLight();
        lamp_light2.setColor(ColorRGBA.White);
        lamp_light2.setRadius(2500f);
        lamp_light2.setPosition(new Vector3f(0.0f,-250.0f,200.0f));
        
        PointLight lamp_light3 = new PointLight();
        lamp_light3.setColor(ColorRGBA.White);
        lamp_light3.setRadius(2500f);
        lamp_light3.setPosition(new Vector3f(0.0f,250.0f,-200.0f));
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));

        viewPort.setBackgroundColor(new ColorRGBA(0.8f, 0.8f, 1.0f, 1.0f));

        rootNode.addLight(lamp_light);
        rootNode.addLight(lamp_light2);
        rootNode.addLight(lamp_light3);
        rootNode.addLight(al);
    }
    
    public void initFloor(BulletAppState state) {
        
        if (floor_phy == null){
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
        else{
            Geometry floor_geo = new Geometry("Floor", floor);
            floor_geo.setMaterial(floor_mat);
            floor_geo.setLocalTranslation(0, -20.0f, 0);
            floor_phy = new RigidBodyControl(0.0f);
            
            floor_geo.addControl(floor_phy);
            state.getPhysicsSpace().add(floor_phy);
            
            
            Geometry wall01_geo = new Geometry("wall01", wall01);
            wall01_geo.setMaterial(floor_mat);
            wall01_geo.setLocalTranslation(0, -20.0f, -240f);
            wall01_phy = new RigidBodyControl(0.0f);
            
            wall01_geo.addControl(wall01_phy);
            state.getPhysicsSpace().add(wall01_phy);
            
            
            Geometry wall02_geo = new Geometry("wall02", wall02);
            wall02_geo.setMaterial(floor_mat);
            wall02_geo.setLocalTranslation(0, -20.0f, 240f);
            wall02_phy = new RigidBodyControl(0.0f);
            
            wall02_geo.addControl(wall02_phy);
            state.getPhysicsSpace().add(wall02_phy);
            
            
            Geometry wall03_geo = new Geometry("wall03", wall03);
            wall03_geo.setMaterial(floor_mat);
            wall03_geo.setLocalTranslation(-240, -20.0f, 0);
            wall03_phy = new RigidBodyControl(0.0f);
            
            wall03_geo.addControl(wall03_phy);
            state.getPhysicsSpace().add(wall03_phy);
            
            
            Geometry wall04_geo = new Geometry("wall04", wall04);
            wall04_geo.setMaterial(floor_mat);
            wall04_geo.setLocalTranslation(240f, -20.0f, 0f);
            wall04_phy = new RigidBodyControl(0.0f);
            
            wall04_geo.addControl(wall04_phy);
            state.getPhysicsSpace().add(wall04_phy);
            
        }
      }
    
    public void initEnv()
    {
        
        for (int i = 0 ; i < maxparallelchecks; i++)
        {
            Creature creat = SpawnCreature();
            
            populationobserved.add(creat);
            
        }
    }
    
    private Creature SpawnCreature()
    {
        MultiBodyAppState state = this.LoadPhysics();
        
        this.initFloor(state);
        
        Creature creat = new Creature();
        creat.BuildRand(wall_mat,stone_mat, rootNode, state);
        
        if (!MJEnvironmentMain.FIXEDSTEP){
            
            PhysicControl control = new PhysicControl(this);
            control.SetCreature(creat);

            state.getPhysicsSpace().addTickListener(control);
        }
        
        return creat;
    }
    
    private void initCam()
    {
        cam.setLocation(new Vector3f(0f,100f,200f));
        cam.lookAt(Vector3f.ZERO.clone(), Vector3f.UNIT_Y.clone());
    }
    
    
    private void LoadBestCreature()
    {
        float proportionrate = 0.0f;

        Collections.sort(populationselection);
        
        System.out.println("Best fitness:" + populationselection.get(0).GetFitness());

        Creature creat = new Creature(populationselection.get(0), false);
        population.push(creat);
               
        if (typeoffitness != 2){ // if fitness type is follow then maintain the older population selection
            populationselection.clear(); // do not clear populationselection, some child could be waiting for this to born
        }
               
        System.gc(); //force garbage collection
         
        populationgarbage.clear();
        
        
        creaturestospawn = 1;
        indexrandpositions = 0;
        canmove = false;
        alivecounter = 0;
        target.setLocalTranslation(randpositions.get(indexrandpositions));
        
        hudText.setText("Play with creature! generation:" + generation);
    }
    
    private void ReevaluateSelection(){
        
        for (int n = 0; n < populationselection.size(); n++ )
        {
            Creature newbornchild = new Creature(populationselection.get(n), false);
            population.push(newbornchild);  
            
            populationselection.get(n).UpdateMarkToKill(generation + 3);
        }
        
        creaturestospawn = maxparallelchecks;
        indexrandpositions = 0;
        canmove = false;
        alivecounter = 0;
       
        if (randpositions.size() > 0 ){
            target.setLocalTranslation(randpositions.get(indexrandpositions));
        }
        
        hudText.setText("Evaluating creatures: " + generation);
    }
    
    private void enterPhysics()
    {
        for (int i = 0 ; i< populationobserved.size(); i++)
        {
            populationobserved.get(i).enterPhysics();
        }
    }
    
    private void Reproduce(boolean detachphysics)
    {
        double proportionrate = 0.0f;
        
        /*------Get proportional childs--------*/
        Collections.sort(populationselection);

        /*----Mark all selection to be killed on next 2 generations----------*/
        populationgarbage.clear();
        for (int j = 0 ; j < populationselection.size(); j++)
        {
            if (!populationselection.get(j).IsMarkedToKill()){
                populationselection.get(j).MarkToKill(generation + 3);
            }
            populationgarbage.add(populationselection.get(j)); 
        }
                
        int previouscreated = 0;
        int maxcountsurvivals = 0;
        
        /*-----count creatures that can be used to mutate------------*/
        for (int i = 0; i < populationselection.size(); i++)
        {
            if (populationselection.get(i).GetFitness() != Float.MAX_VALUE && populationselection.get(i).GetFitness() != Float.POSITIVE_INFINITY && maxcountsurvivals < maxsurvivals)
            {
                maxcountsurvivals++;
            }
        }
        
        for(int i =0 ; i < maxcountsurvivals; i++)
        { 
            double rate = 0.33f;
            int howmanychilds = (int)((maxpopulation - maxcountsurvivals  - previouscreated)* rate);
            previouscreated += howmanychilds;
            
            //---Rise childs--------v
            for (int j = 0 ; j < howmanychilds; j++){
                
                //int childfromwhere = FastMath.nextRandomInt(0, 1);
                int childfromwhere = 1;
                
                //----mutate father or populate having sex-------
                if (childfromwhere == 0)
                {
                    //---select random mother-------
                    int childindex = FastMath.nextRandomInt(0, populationselection.size() - 1);

                    if (populationselection.size() > 1)
                    {
                        while(childindex == i)
                        {
                            childindex = FastMath.nextRandomInt(0, populationselection.size() - 1);
                        }
                    }
                    else
                    {
                        childindex = i;
                    }

                    Creature newbornchild = new Creature(populationselection.get(i), populationselection.get(childindex));

                    population.push(newbornchild);
                }
                else
                {
                    //------get copies of creatures------
                    //only mutate weights in NN
                    //Creature newbornchild = new Creature(populationselection.get(i), true);            
                    Creature newbornchild = new Creature(populationselection.get(i), false);            
                    population.push(newbornchild);
                }
                
            }

            Creature newbornchild = new Creature(populationselection.get(i), false);
            population.push(newbornchild);  
            
        }
        
        /*----Fill the entire population ---- to prevent round issues - the order here is 1 --------*/
        while(population.size() < maxpopulation)
        {
            System.out.println("<------------------------");
            Creature creat;
            if (populationselection.size() == 0)
            {
                System.out.println("New creature------------------------");
                //creat = new Creature(backupcreature, true);
                creat = new Creature(backupcreature, false);
            }
            else {
                System.out.println("New creature from population selection (?)<------------------------");
                //creat = new Creature(populationselection.get(0), true);
                creat = new Creature(populationselection.get(0), false);
            }
            
            population.push(creat);
        }
        
        /*-------Dont select this creature anymore, use new creatures instead - the order here is 2 ------------*/
        /*-------DO NOT COMMENT THIS, YOU WILL BE RUN OUT OF MEMORY-------------*/
        for(int i =0 ; i < populationselection.size(); i++)
        {
            //------------Dont select this Creature anymore-------------------
            populationselection.get(i).SetFitness(Float.POSITIVE_INFINITY);
        }
        
     
           
        generation++;
        
        System.gc(); //force garbage collection
         
        ArrayList<Creature> todelete = new ArrayList<Creature>();
        
        
        for (int i = 0; i < populationgarbage.size(); i++)
        {
            if (populationgarbage.get(i).IsMarkedToKill())
            {
                if (generation >= populationgarbage.get(i).GetUntilGeneration())
                {
                    System.out.println("Remove from garbage:" + populationgarbage.get(i).GetCreatureID());
                    populationgarbage.get(i).KillAll();
                    todelete.add(populationgarbage.get(i));
                    
                }
            }
        }
        populationselection.removeAll(todelete);
        populationgarbage.clear();
        
        if (typeoffitness != 2){ // if fitness type is follow then maintain the older population selection
            populationselection.clear(); // do not clear populationselection, some child could be waiting for this to born
        }
        
      
        
        creaturestospawn = maxparallelchecks;
        indexrandpositions = 0;
        canmove = false;
        cancheck = false;
        alivecounter = 0;
        target.setLocalTranslation(randpositions.get(indexrandpositions));
        
        hudText.setText("Generation: " + generation);
    }
    
    
    public void moveTarget()
    {
        if (!canmove)
        {
            canmove = true;
            cancheck = true;
            checkactualtimer = 0f;
            fixedtime = 0f;
        }
    }
    
    
    private void SaveState()
    {
        String userHome = System.getProperty("user.home");
        BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File(userHome + "/Env/"+"State.j3o");
        try {
            
            if (file.exists())
            {
                String timestamp =  new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                File dest = new File( userHome + "/Env/"+"State-" + timestamp + ".j3o");
                Files.copy(file.toPath(),dest.toPath());
            }
            
            
            if (populationselection.size() > 0){
                MJEnvironmentSave savestate = new MJEnvironmentSave();
                savestate.setPopulationselection(populationselection);
                savestate.setGeneration(generation);
                savestate.setRandpositions(randpositions);
                exporter.save(savestate, file);
                
                hudText.setText("Save Success!");
            }
            
        } catch (Exception ex) {
          //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error: Failed to save game!", ex);
            System.out.println("Error while saving: " + ex.getMessage());
        }
    }
    
    private void PlayState()
    {
        String userHome = System.getProperty("user.home");
        assetManager.registerLocator(userHome, FileLocator.class);
        MJEnvironmentSave loadedstate = (MJEnvironmentSave)assetManager.loadAsset( "/Env/"+"State.j3o");
        
        for(int i = 0 ; i < population.size() ;i++)
        {
            population.get(i).DetachPhysics();
            population.get(i).KillAll();
            stateManager.detach(population.get(i).GetPhysicState());
        }
        
        population.clear();
        
        for(int i = 0 ; i < populationobserved.size() ;i++)
        {
            populationobserved.get(i).DetachPhysics();
            populationobserved.get(i).KillAll();
            stateManager.detach(populationobserved.get(i).GetPhysicState());
        }
        
        populationobserved.clear();
        
        for(int i = 0 ; i < populationselection.size() ;i++)
        {
            populationselection.get(i).DetachPhysics();
            populationselection.get(i).KillAll();
            stateManager.detach(populationselection.get(i).GetPhysicState());
        }
        
        populationselection.clear();
        
        populationselection = loadedstate.getPopulationselection();
      
        randpositions.clear();
        
        randpositions = loadedstate.getRandpositions();
        
        generation = loadedstate.getGeneration();
        
        ReevaluateSelection();
        
        finallyplay = true;
        
        state = 1;
   
    }
    
    private void StopEvolState()
    {
        state = 6;
        canmove = false;
        cancheck = false;
    }
    
    private void LoadState()
    {
        String userHome = System.getProperty("user.home");
        assetManager.registerLocator(userHome, FileLocator.class);
        MJEnvironmentSave loadedstate = (MJEnvironmentSave)assetManager.loadAsset( "/Env/"+"State.j3o");
        
        for(int i = 0 ; i < population.size() ;i++)
        {
            population.get(i).DetachPhysics();
            population.get(i).KillAll();
            stateManager.detach(population.get(i).GetPhysicState());
        }
        
        population.clear();
        
        for(int i = 0 ; i < populationobserved.size() ;i++)
        {
            populationobserved.get(i).DetachPhysics();
            populationobserved.get(i).KillAll();
            stateManager.detach(populationobserved.get(i).GetPhysicState());
        }
        
        populationobserved.clear();
        
        for(int i = 0 ; i < populationselection.size() ;i++)
        {
            populationselection.get(i).DetachPhysics();
            populationselection.get(i).KillAll();
            stateManager.detach(populationselection.get(i).GetPhysicState());
        }
        
        populationselection.clear();
        
        populationselection = loadedstate.getPopulationselection();
         
        randpositions.clear();
        
        randpositions = loadedstate.getRandpositions();
        
        generation = loadedstate.getGeneration();
        
        ReevaluateSelection();
        
        state = 1;
    }
    
    
    
      
    @Override
    public void simpleUpdate(float tpf) {
        
        fixedtime = fixedtime + tpf;
        //timer += tpf;
        
        //hudText.setText("Time: " + fixedtime);
        //System.out.println("Creature 1 average: " + populationobserved.get(0).GetAverageVel());
        
        if (applyForce) {
            populationobserved.get(0).GetRootNode().GetBodyControl().applyForce(upforce, Vector3f.ZERO.clone());  
        }
        
        if (applyForceUp) {
            populationobserved.get(0).GetRootNode().GetBodyControl().applyForce(creatupforce , Vector3f.ZERO.clone());  
        }
        if (applyForceDown) {
            populationobserved.get(0).GetRootNode().GetBodyControl().applyForce(creatdownforce, Vector3f.ZERO.clone());  
        }
        if (applyForceLeft) {
            populationobserved.get(0).GetRootNode().GetBodyControl().applyForce(creatleftforce, Vector3f.ZERO.clone());
        }
        if (applyForceRight) {
            populationobserved.get(0).GetRootNode().GetBodyControl().applyForce(creatrightforce, Vector3f.ZERO.clone());  
        }
        
        if (target != null)
        {
            if (canmove){
                checkactualtimer = checkactualtimer + tpf;
                checkvelactualtimer = checkvelactualtimer + tpf;
                
                if (checkvelactualtimer > checkvelmaxtimer && state != 2)
                {
                    for (int i = 0 ; i < populationobserved.size();i++)
                    {
                        populationobserved.get(i).EvaluateApproachVel(target.getWorldTranslation(), false);
                    }
                    
                    checkvelactualtimer = 0f;
                }
            
                if (checkactualtimer > checkmaxtimer && state != 2)
                {
                    System.out.println("Actual time:" + checkactualtimer + " Max:" + checkmaxtimer);
                    
                    
                    for (int i = 0 ; i < populationobserved.size();i++)
                    {
                        populationobserved.get(i).EvaluateApproachVel(target.getWorldTranslation(), true);
                        
                        if (i == 0){
                            System.out.println("Red creature: " + populationobserved.get(i).GetAverageVel());
                        } else if ( i == 1)
                        {
                            System.out.println("Green creature: " + populationobserved.get(i).GetAverageVel());
                        }
                        else
                        {
                            System.out.println("Blue creature: " + populationobserved.get(i).GetAverageVel());
                        }
                        
                    }


                    if (indexrandpositions < (randpositions.size() - 1))
                    {
                        indexrandpositions++;
                        System.out.println("index:" + indexrandpositions);
                        target.setLocalTranslation(randpositions.get(indexrandpositions));
                    }
                    else
                    {
                        canmove = false;
                    }   

                    checkactualtimer = 0f;
                }
            }
            
        }
        
        switch (state)
        {
            case 0: //---initializing first population
            {
                /*----init population----------*/
                if (cancheck && fixedtime>maxtimer)
                {
                    for (int i = 0 ; i< populationobserved.size(); i++)
                    {
                        MultiBodyAppState bstate = populationobserved.get(i).DetachPhysics();
                        
                        switch(typeoffitness){//only follow a target is the objective
                            case 2:
                            {
                                populationobserved.get(i).EvaluateApproachVel(target.getWorldTranslation(), true);
                                
                                populationobserved.get(i).SetFitness(populationobserved.get(i).GetAverageVel());
                                float dist = populationobserved.get(i).GetFitness();
                                
                               
                                
                                if (populationselection.size() < maxsurvivals && populationobserved.get(i).isStartticking())
                                {
                                    hudText.setText("Init pupulation: " + populationselection.size() + " of " + maxsurvivals);
                                    populationselection.add(populationobserved.get(i));
                                }
                                
                                populationobserved.get(i).RemoveAllBodies();
                                stateManager.detach(bstate);
                                break;
                            }
                        }
                        

                        
                    }

                    populationobserved.clear();

                    if (populationselection.size() >= maxsurvivals)
                    {
                        Reproduce(true);
                        creaturestospawn = maxparallelchecks;
                        canmove = false;
                        cancheck = false;
                        indexrandpositions = 0;
                        target.setLocalTranslation(randpositions.get(indexrandpositions));
                        state = 1;
                    }
                    else{
                        cancheck = false;
                        alivecounter = 0;
                        initEnv();
                    }

                    fixedtime = 0f;
                }
                break;
            }
                
            case 1:
            {
                if (population.size() > 0 && creaturestospawn > 0)
                {
                    for (int i = 0; i < creaturestospawn && population.size() > 0; i++)
                    {
                        Creature creat = population.pop();

                        try{
                            
                            if (creat.IsFromScratch())
                            {
                                creat = SpawnCreature();
                                populationobserved.add(creat);
                            }
                            else
                            {
                                //BulletAppState newstate = this.LoadPhysics();
                                MultiBodyAppState newstate = this.LoadPhysics();
                                
                                if (!MJEnvironmentMain.FIXEDSTEP){
                                    
                                    PhysicControl creaturephysics = new PhysicControl(this);
                                    creaturephysics.SetCreature(creat);
                                    newstate.getPhysicsSpace().addTickListener(creaturephysics);
                                    
                                }

                                this.initFloor(newstate);
                                
                                if (i == 0){
                                    creat.NewBornChild(redmat, stone_mat, rootNode, newstate, this);
                                }
                                else if (i == 1){
                                    creat.NewBornChild(greenmat, stone_mat, rootNode, newstate, this);
                                }
                                else{
                                    creat.NewBornChild(bluemat, stone_mat, rootNode, newstate, this);
                                }
                                //}
                                
                                //moveTarget();
                                populationobserved.add(creat);
                            }
  
                        }
                        catch(Exception n)
                        {
                            System.out.println(":(");
                        }
                        
                        
                        System.out.println("population spawn number: " + i + " population.size:" + population.size() + " selectedpopulation: " + populationselection.size());
                    }
                    
                    creaturestospawn = 0;
                }
                
                
                if (cancheck && fixedtime>maxtimer)
                {
                    for (int i = 0 ; i< populationobserved.size(); i++)
                    {
                        
                        switch(typeoffitness)//only follow a target
                        {
                            case 2:
                            {
                                populationobserved.get(i).EvaluateApproachVel(target.getWorldTranslation(), true);
                                
                                populationobserved.get(i).SetFitness(populationobserved.get(i).GetAverageVel());
                                float dist = populationobserved.get(i).GetFitness();
                                

                                System.out.println("New distanceeeeeeee!!!: " + dist);
                                populationselection.add(populationobserved.get(i));
  
                                        
                                populationobserved.get(i).RemoveAllBodies();
                                
                                
                                MultiBodyAppState bstate = populationobserved.get(i).DetachPhysics();
                                stateManager.detach(bstate);
                                
                                break;
                            }
                        }
                        

                        
                    }

                    populationobserved.clear();
                    
                    if (population.size() == 0)
                    {
                        //advance generation
                        if (!finallyplay)
                        {
                            Reproduce(true);
                        }
                        else{
                            LoadBestCreature();
                            state = 2;
                        }
                    }
                    else
                    {
                        creaturestospawn = maxparallelchecks;
                        canmove = false;
                        cancheck = false;
                        alivecounter = 0;
                        indexrandpositions = 0;
                        target.setLocalTranslation(randpositions.get(indexrandpositions));
                    }

                    fixedtime = 0;
                }
                
                
                break;
            }
            case 2:
            {
                /*---------Play with creature--------------*/
                if (population.size() > 0 && creaturestospawn > 0)
                {
                    for (int i = 0; i < creaturestospawn && population.size() > 0; i++)
                    {
                        Creature creat = population.pop();

                        try{
                            MultiBodyAppState newstate = this.LoadPhysics();
                            
                            if (!MJEnvironmentMain.FIXEDSTEP){
                                    
                                PhysicControl creaturephysics = new PhysicControl(this);
                                creaturephysics.SetCreature(creat);
                                newstate.getPhysicsSpace().addTickListener(creaturephysics);
                                
                            }

                            this.initFloor(newstate);
                            creat.NewBornChild(wall_mat, stone_mat, rootNode, newstate, this);
                            
                            System.out.println("Best creature id:" + creat.GetCreatureID());

                            populationobserved.add(creat);
                        }
                        catch(Exception n)
                        {
                            System.out.println(":(");
                        }
                        
                        
                        System.out.println("population spawn number: " + i + " population.size:" + population.size() + " selectedpopulation: " + populationselection.size());
                    }
                    
                    creaturestospawn = 0;
                }
                break;
            }   
            case 6:
            {
                break;
            }
        } 
    }
}
