/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.MultiBodyAppState;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Cristian.Villalba
 */
public class Creature implements Comparable, Savable{
    private MJNode rootnode;
    private MultiBodyAppState bulletappstate;
    private float fitness = 0.0f;
    private Vector3f originalpos = new Vector3f();
    private Creature father;
    private Creature mother;
    
    private ArrayList<Float> averagedistances = new ArrayList<Float>();
    private boolean marktokill = false;
    private int untilgeneration = 0;
    private boolean mutate = false;
    private int creatureid;
    public boolean startticking = false;
    
    private boolean fromscratch = true;
    
    private ArrayList<MJNeuron[]> neuronmain;
    
    
    public Creature()
    {
        
    }
    
    public Creature(Creature f, Creature m)
    {
        father = f;
        mother = m;
        fromscratch = false;
    }
    
    public Creature(Creature toclone, boolean m)
    {
        father = toclone;
        mutate = m;
        fromscratch = false;
    }
    
    public boolean IsFromScratch()
    {
        return fromscratch;
    }
    
    public MJNode GetRootNode()
    {
        return rootnode;
    }
      
    public void Tick(float tpf)
    {
        int maxtick = 5;
        
        if (neuronmain != null && neuronmain.size() > 0){
            for (int i =0 ; i < neuronmain.get(3).length - 1; i++)
            {
                neuronmain.get(3)[i].fire(tpf, maxtick);
            }
        }
        
    }
    
    public void SavePreviousPos()
    {
        if (rootnode != null){
            rootnode.SavePreviousPos();
        }
    }
    
    public boolean IsMoving()
    {
        if (rootnode != null){
            return rootnode.IsMoving();
        }
        return false;
    }
    
    public void SetFitness(float g)
    {
        fitness = g;
    }
    
    
    public float GetFitness()
    {
        return fitness;
    }
    
    
    public MultiBodyAppState DetachPhysics()
    {
        if (rootnode != null){
            rootnode.DetachPhysics();
            
            if (bulletappstate != null){
                if (bulletappstate.getPhysicsSpace() != null){
                    bulletappstate.getPhysicsSpace().removeAll(rootnode);
                }
            }
            rootnode.removeFromParent();

            return bulletappstate; 
        }
        return null;  
    }
    
    public void KillAll()
    {
        if (rootnode != null){
            rootnode.KillAll();
            
            rootnode.removeFromParent();
            rootnode = null;
            
            
        }
    }
    
    public void PrintPostion()
    {
        if (rootnode != null){
            rootnode.PrintPosition(); 
        }
    }
    
     public void RemoveAllBodies()
    {
        if (rootnode != null){
            rootnode.RemoveAllBodies();
            
            rootnode.removeFromParent();    
        }
    }
    
    
    public void SpawnOnEnv(Material mat, Material matsymetric, Node parent, AppStateManager stateM)
    {
        averagedistances.clear();
        stateM.attach(bulletappstate);
        rootnode.SpawnOnEnv(mat, matsymetric, null);
        SetFitness(0.0f);
    }
        
    public Vector3f GetOriginalPos()
    {
        return originalpos;
    }
    
    public void SetOriginalPos(float x, float y, float z)
    {
        originalpos.set(x, y, z);
    }
    
    public ArrayList<MJNeuron[]> GetBrain()
    {
        return neuronmain;
    }
    
    public void CopyBrain(HashMap<MJNode, MJNode> sbrainnodes, boolean mutate, Creature father)
    {
        HashMap<MJNeuron, MJNeuron> sbrain = new HashMap<MJNeuron, MJNeuron>();
        
        neuronmain = new ArrayList<MJNeuron[]>();
        
        MJNeuron[] sensors = new MJNeuron[father.GetBrain().get(0).length];
        MJNeuron[] neuronsv0 = new MJNeuron[father.GetBrain().get(1).length];
        MJNeuron[] neurons = new MJNeuron[father.GetBrain().get(2).length];
        MJNeuron[] effectors = new MJNeuron[father.GetBrain().get(3).length];
        
        for (int i = 0; i < father.GetBrain().get(0).length; i++)
        {
            MJNeuron mirror = father.GetBrain().get(0)[i].Mirror(mutate);
            
            MJNode part = father.GetBrain().get(0)[i].GetPart();
            mirror.SetPart(sbrainnodes.get(part));
            
            sensors[i] = mirror;
            
            sbrain.put(father.GetBrain().get(0)[i], mirror);
        }
        
        for (int i = 0; i < father.GetBrain().get(1).length; i++)
        {
            MJNeuron mirror = father.GetBrain().get(1)[i].Mirror(mutate);
            neuronsv0[i] = mirror;
            
            sbrain.put(father.GetBrain().get(1)[i], mirror);
        }
        
        for (int i = 0; i < father.GetBrain().get(2).length; i++)
        {
            MJNeuron mirror = father.GetBrain().get(2)[i].Mirror(mutate);
            neurons[i] = mirror;
            
            sbrain.put(father.GetBrain().get(2)[i], mirror);
        }
        
        for (int i = 0; i < father.GetBrain().get(3).length; i++)
        {
            MJNeuron mirror = father.GetBrain().get(3)[i].Mirror(mutate);
            
            MJNode part = father.GetBrain().get(3)[i].GetPart();
            mirror.SetPart(sbrainnodes.get(part));

            effectors[i] = mirror;
            
            sbrain.put(father.GetBrain().get(3)[i], mirror);
        }
        
        
        neuronmain.add(sensors);
        neuronmain.add(neuronsv0);
        neuronmain.add(neurons);
        neuronmain.add(effectors);
        
        /*--------------copy connections--------------*/    
        for (int i = 0; i < father.GetBrain().get(0).length; i++)
        {
            MJNeuron toconnect = sbrain.get(father.GetBrain().get(0)[i]);

            for (int j = 0; j < father.GetBrain().get(0)[i].GetConnections().size(); j++)
            {
                if (sbrain.get(father.GetBrain().get(0)[i].GetConnections().get(j)) != null)
                {
                    toconnect.connect(sbrain.get(father.GetBrain().get(0)[i].GetConnections().get(j)));
                }
                else
                {
                    System.out.println("Cnx warning");
                }

            }
        }

        for (int i = 0; i < father.GetBrain().get(1).length; i++)
        {
            MJNeuron toconnect = sbrain.get(father.GetBrain().get(1)[i]);

            for (int j = 0; j < father.GetBrain().get(1)[i].GetConnections().size(); j++)
            {
                if (sbrain.get(father.GetBrain().get(1)[i].GetConnections().get(j)) != null){

                    toconnect.connect(sbrain.get(father.GetBrain().get(1)[i].GetConnections().get(j)));
                }
                else
                {
                    System.out.println("Cnx warning");
                }
            }

        }
        
        for (int i = 0; i < father.GetBrain().get(2).length; i++)
        {
            MJNeuron toconnect = sbrain.get(father.GetBrain().get(2)[i]);

            for (int j = 0; j < father.GetBrain().get(2)[i].GetConnections().size(); j++)
            {
                if (sbrain.get(father.GetBrain().get(2)[i].GetConnections().get(j)) != null){

                    toconnect.connect(sbrain.get(father.GetBrain().get(2)[i].GetConnections().get(j)));
                }
                else
                {
                    System.out.println("Cnx warning");
                }
            }

        }

        for (int i = 0; i < father.GetBrain().get(3).length; i++)
        {
            MJNeuron toconnect = sbrain.get(father.GetBrain().get(3)[i]);

            for (int j = 0; j < father.GetBrain().get(3)[i].GetConnections().size(); j++)
            {
                if (sbrain.get(father.GetBrain().get(3)[i].GetConnections().get(j)) != null){
                    toconnect.connect(sbrain.get(father.GetBrain().get(3)[i].GetConnections().get(j)));
                }
                else{
                    System.out.println("Cnx warning");
                }   
            }
        }
        
        //PrintBrain(neuronmain);
        //PrintBrain(father.GetBrain());
    }
    
    private void PrintBrain(ArrayList<MJNeuron[]> b)
    {
        System.out.println("Sens:");
        for(int i = 0; i < b.get(0).length; i++)
        {
            System.out.println(b.get(0)[i].GetInfo());
        }
        System.out.println("Neur:");
        for(int i = 0; i < b.get(1).length; i++)
        {
            System.out.println(b.get(1)[i].GetInfo());
        }
        System.out.println("Eff:");
        for(int i = 0; i < b.get(2).length; i++)
        {
            System.out.println(b.get(2)[i].GetInfo());
        }
    }
    
    public void GenerateBrain()
    {
        MJNeuron[] sensors = null;
        
        neuronmain = new ArrayList<MJNeuron[]>();
     
        int randsensor = 8; // 2 eyes and 2 more other type sensors;
        sensors = new MJNeuron[randsensor];
        
        int randneuronsizev0 = FastMath.nextRandomInt(14, 20);
        MJNeuron[] neuronsv0 = new MJNeuron[randneuronsizev0];
        
        int randneuronsize = FastMath.nextRandomInt(14, 20);
        MJNeuron[] neurons = new MJNeuron[randneuronsize];
        
        int randeffector = this.GetPartCount();
        MJNeuron[] effectors = new MJNeuron[randeffector];
        
        neuronmain.add(sensors);
        neuronmain.add(neuronsv0);
        neuronmain.add(neurons);
        neuronmain.add(effectors);
               
        MJNode randpart = this.RandPart();
        for(int i = 0; i <  3; i++)
        {
            int type = 2;
            
            MJNeuron sens2;
            
            sens2 = new MJNeuron(2.0f, 0, i);       
            sens2.SetPart(randpart);
            sensors[i] = sens2;
        }
        
        randpart = this.RandPart();
        for(int i = 0; i <  3; i++)
        {
            int type = 2;
            
            MJNeuron sens2;
            
            sens2 = new MJNeuron(2.0f, 0, i);       
            sens2.SetPart(randpart);
            sensors[i + 3] = sens2;
        }
        
        for(int i = 0; i <  2; i++)
        {
            int type = i;
            
            MJNeuron sens2;
            
            sens2 = new MJNeuron(2.0f, 0, i-2);       
            sens2.SetPart(this.RandPart());
            sensors[i + 6] = sens2;
        }
        
        for(int i = 0; i < neuronsv0.length; i++)
        {
            MJNeuron neur = new MJNeuron(0.05f, 1, 0);
            neuronsv0[i] = neur;
            
            for (int k = 0 ; k < sensors.length; k++)
            {
               neur.connect(sensors[k]);
            }
        }
        
        for(int i = 0; i < neurons.length; i++)
        {
            MJNeuron neur = new MJNeuron(0.05f, 1, 0);
            neurons[i] = neur;
            
            for (int k = 0 ; k < neuronsv0.length; k++)
            {
               neur.connect(neuronsv0[k]);
            }
        }
        
        for(int i = 0; i < effectors.length; i++)
        {
            MJNode part =  this.GetInnerPart(i);
            
            MJNeuron eff = new MJNeuron(0.05f, 2,i);
            eff.SetPart(part);
            effectors[i] = eff;
            
            for (int k = 0 ; k < neurons.length; k++)
            {
                eff.connect(neurons[k]);
            }
        }
        
        for (int i = 0; i < neuronsv0.length; i++)
        {
            neuronsv0[i].GenerateWeights();
        }
        
        for (int i = 0; i < neurons.length; i++)
        {
            neurons[i].GenerateWeights();
        }
        
        for (int i = 0; i < effectors.length; i++)
        {
            effectors[i].GenerateWeights();
        }
    }
    
    
    public void NewBornChild(Material mat, Material matsymetric, Node parent, MultiBodyAppState bullet, MJEnvironmentMain main)
    {
        bulletappstate = bullet;
        HashMap<MJNeuron, MJNeuron> symetricbrain = new HashMap<MJNeuron, MJNeuron>();
        HashMap<MJNode, MJNode> symetricdata = new HashMap<MJNode, MJNode>();
        
        bulletappstate.setEnabled(false);
        
        if (father != null && mother != null)
        {
            rootnode = new MJNode(father.GetRootNode(),mother.GetRootNode(),null, mat, symetricdata, father.GetRootNode().GetRecursiveLimit() /2, bulletappstate, 0, parent);
        
            rootnode.ConnectJointsRecursive(null);
            
            int rec = father.GetRootNode().GetRecursiveLimit() /2 + mother.GetRootNode().GetRecursiveLimit()/2;
            rootnode.SetRecursiveLimit(rec);
            
            creatureid = FastMath.nextRandomInt();
        }
        else
        {
            rootnode = new MJNode(father.GetRootNode(),null, mat, symetricdata,  bulletappstate, mutate, parent);
            
            rootnode.ConnectJointsRecursive(null);    
            CopyBrain(symetricdata, mutate, father);
            
            creatureid = FastMath.nextRandomInt();
                  
            if (!mutate){
                creatureid = father.GetCreatureID();
            }
            else
            {
                //rootnode.RandCnxRecursively(this);
            }

            int rec = father.GetRootNode().GetRecursiveLimit();
            rootnode.SetRecursiveLimit(rec);
            
            rootnode.EnterPhysics(); //do not enter physics directly
            
            
        }
      
        bulletappstate.setEnabled(true);
        father = null;
        mother = null;
        
        SetFitness(0.0f);
    }
    
    public MJNode RandPart()
    {
        ArrayList<MJNode> allnodes = new ArrayList<MJNode>();
        
        allnodes.add(rootnode);
        rootnode.GivemeNodes(allnodes);
        
        int index = FastMath.nextRandomInt(0, allnodes.size() - 1);
        
        return allnodes.get(index);
    }
    
    public int GetPartCount()
    {
        ArrayList<MJNode> allnodes = new ArrayList<MJNode>();
        
        allnodes.add(rootnode);
        rootnode.GivemeNodes(allnodes);
        
        return allnodes.size();
    }
    
    public MJNode GetInnerPart(int i)
    {
        ArrayList<MJNode> allnodes = new ArrayList<MJNode>();
        
        allnodes.add(rootnode);
        rootnode.GivemeNodes(allnodes);
        
        int index = i;
        
        return allnodes.get(index);
    }
    
    public void enterPhysics()
    {
        rootnode.EnterPhysics();
    }
    
   
    public void BuildRand(Material mat, Material matsymetric, Node parent, MultiBodyAppState bullet)
    {
        ArrayList<MJNode> allpositions = new ArrayList<MJNode>();
                
        bulletappstate = bullet;
        rootnode = new MJNode(mat, parent, null, bullet, true, 0, -1);
        rootnode.RandRecursiveLimit();
        rootnode.setName("ALIVE AND RANDOM");
        allpositions.add(rootnode);
        
        
        rootnode.RandomizeChild(mat, bullet, parent, rootnode.GetRecursiveLimit() - 1, true, allpositions);
        rootnode.SymetricRand(matsymetric, false);
        
        GenerateBrain();
        
        rootnode.EnterPhysics();
        
        creatureid = rootnode.GetId();
        
    }
    
    public int GetCreatureID()
    {
        return creatureid;
    }
    
    public void InvertCreatureID()
    {
        creatureid = -1;
    }
    
    public BulletAppState GetPhysicState()
    {
        return bulletappstate;
    }
    
    public float EvaluateHeight()
    {
        return rootnode.EvaluateHeight();
    }
    
    private void SetDistance(float v)
    {
        averagedistances.add(v);
    }
    
    private float GetDistance()
    {
        if (averagedistances.size() > 0)
        {
            return averagedistances.get(averagedistances.size()- 1);
        }
        else
            return -1;
    }
    
    public void EvaluateApproachVel(Vector3f targetpos, boolean searchagain)
    {
        //Spatial nearest = rootnode.GetNearest(targetpos, null);
        Spatial nearest = rootnode;
        Vector3f velocity = rootnode.GetAverageVel();
        Vector3f totargetactual = targetpos.subtract(nearest.getWorldTranslation());
        
        float rate = 0;
     
        if (!searchagain){ //get latest distance observed
            
            if (this.GetDistance() == 0f){
                System.out.println("Goal achieved!");
                this.SetDistance(0);
                originalpos.set(nearest.getWorldTranslation()); //saves position      
                return;
            }
        }
         
        if (!this.IsMoving())
        {
            rate = 100000;
            this.SetDistance(rate);
        }
        else if (velocity.length() <= 5.0f)
        { 
            rate = 100000;
            this.SetDistance(rate);
        }
        else if (totargetactual.length() <= 7.0f)
        {
           this.SetDistance(0);
        }
        else
        {

            Vector3f speedtowards = velocity.project(totargetactual);
           
            rate = 1/speedtowards.length() + totargetactual.length();
           
            this.SetDistance(rate);
        }
        
        originalpos.set(nearest.getWorldTranslation()); //guarda la nueva posicion inicial
        this.SavePreviousPos();
    }
    
    public float GetAverageVel()
    {
        float totals = 0;
        
        //System.out.println(averagedistances);
        
        for (int i = 0 ; i < averagedistances.size(); i++)
        {
            totals += averagedistances.get(i);
        }
        
        return (totals / ((float)averagedistances.size()));
    }
    
    public void MarkToKill(int untilgen)
    {
        if (!marktokill){
            untilgeneration = untilgen;
            marktokill = true;
        }
    }
    
    public void UpdateMarkToKill(int untilgen)
    {
        untilgeneration = untilgen;
        marktokill = true;
    }
    
    
    public boolean IsMarkedToKill()
    {
        return marktokill;
    }
    
    public int GetUntilGeneration()
    {
        return untilgeneration;
    }
    
    public boolean IsAlive()
    {
        if (mother == null && father == null)
            return true;
        else
            return false;
    }

    public int compareTo(Object o) {
        float outfitness = ((Creature)o).GetFitness();
        
        if ( new Float(outfitness).isInfinite() &&  new Float(this.fitness).isInfinite()){
            return 0;
        }
        
        if ( new Float(this.fitness).isInfinite()){
            return 1;
        }
        
        if ( new Float(outfitness).isInfinite()){
            return -1;
        }
        
        return      this.fitness > outfitness ? 1
                :   this.fitness < outfitness ? -1
                :0;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        
        capsule.write(rootnode,   "rootnode",   null);
        //capsule.write(bulletappstate,   "bulletappstate",   null);
        capsule.write(fitness,   "fitness",   0);
        capsule.write(originalpos,   "originalpos",   null);
        capsule.write(father,   "father",   null);
        capsule.write(mother,   "mother",   null);
        capsule.write(creatureid,"creatureid", 0);
        
        float[] floatarray = new float[averagedistances.size()];
        
        for (int i = 0; i < averagedistances.size(); i++)
        {
            floatarray[i] = averagedistances.get(i);
        }
        capsule.write(floatarray, "averagedistances", null);
        
        capsule.write(marktokill,   "marktokill",   false);
        capsule.write(untilgeneration,   "untilgeneration",   0);
        capsule.write(mutate,   "mutate",   false);
        capsule.write(fromscratch, "fromscratch", true);
        
        if (neuronmain != null && neuronmain.size() == 4){
            ArrayList<MJNeuron> sensors = new ArrayList<MJNeuron>();
            ArrayList<MJNeuron> neuronsv0 = new ArrayList<MJNeuron>();
            ArrayList<MJNeuron> neurons = new ArrayList<MJNeuron>();
            ArrayList<MJNeuron> effectors = new ArrayList<MJNeuron>();

            for (int i = 0; i < neuronmain.get(0).length;i++)
            {
                sensors.add(neuronmain.get(0)[i]);
            }
            for (int i = 0; i < neuronmain.get(1).length;i++)
            {
                neuronsv0.add(neuronmain.get(1)[i]);
            }
            for (int i = 0; i < neuronmain.get(2).length;i++)
            {
                neurons.add(neuronmain.get(2)[i]);
            }
            for (int i = 0; i < neuronmain.get(3).length;i++)
            {
                effectors.add(neuronmain.get(3)[i]);
            }

            capsule.writeSavableArrayList(sensors, "sensors", null);
            capsule.writeSavableArrayList(neuronsv0, "neuronsv0", null);
            capsule.writeSavableArrayList(neurons, "neurons", null);
            capsule.writeSavableArrayList(effectors, "effectors", null);
        }
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
 
        rootnode = (MJNode)capsule.readSavable("rootnode",   null);
        //capsule.write(bulletappstate,   "bulletappstate",   bulletappstate);
        fitness = capsule.readFloat("fitness",   0);
        originalpos = (Vector3f)capsule.readSavable("originalpos",   null);
        father = (Creature)capsule.readSavable("father",   null);
        mother = (Creature)capsule.readSavable("mother",   null);
        creatureid = capsule.readInt("creatureid", 0);
        
        float[] floatarray = capsule.readFloatArray("averagedistances", null);
        
        averagedistances = new ArrayList<Float>();
        for (int i = 0; i < floatarray.length; i++)
        {
            averagedistances.add(floatarray[i]);
        }
        marktokill = capsule.readBoolean("marktokill",   false);
        untilgeneration = capsule.readInt("untilgeneration",   0);
        mutate = capsule.readBoolean("mutate",   false);
        fromscratch = capsule.readBoolean("fromscratch", true);
        
        ArrayList<MJNeuron> sensors = capsule.readSavableArrayList("sensors", null);
        ArrayList<MJNeuron> neuronsv0 = capsule.readSavableArrayList("neuronsv0", null);
        ArrayList<MJNeuron> neurons = capsule.readSavableArrayList("neurons", null);
        ArrayList<MJNeuron> effectors = capsule.readSavableArrayList("effectors", null);
        
        neuronmain = new ArrayList<MJNeuron[]>();
        
        if (sensors != null && neuronsv0 != null && neurons != null && effectors != null){
            MJNeuron[] sens = new MJNeuron[sensors.size()];
            MJNeuron[] neuv0 = new MJNeuron[neuronsv0.size()];
            MJNeuron[] neu = new MJNeuron[neurons.size()];
            MJNeuron[] eff = new MJNeuron[effectors.size()];

            for (int i = 0; i < sensors.size();i++)
            {
                sens[i] = sensors.get(i);
            }
            for (int i = 0; i < neuronsv0.size();i++)
            {
                neuv0[i] = neuronsv0.get(i);
            }
            for (int i = 0; i < neurons.size();i++)
            {
                neu[i] = neurons.get(i);
            }
            for (int i = 0; i < effectors.size();i++)
            {
                eff[i] = effectors.get(i);
            }

            neuronmain.add(sens);
            neuronmain.add(neuv0);
            neuronmain.add(neu);
            neuronmain.add(eff);
        }
    }

    /**
     * @return the startticking
     */
    public boolean isStartticking() {
        return startticking;
    }

    /**
     * @param startticking the startticking to set
     */
    public void setStartticking(boolean startticking) {
        this.startticking = startticking;
    }
}
