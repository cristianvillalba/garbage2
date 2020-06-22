/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.bullet.joints.motors.RotationalLimitMotor;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


/**
 *
 * @author Cristian.Villalba
 */
public class MJNeuron implements Savable {
    private ArrayList<MJNeuron> inputs;
    private ArrayList<Float> weights;
    private float bias;
    public static float mutationrate = 0.25f;
    private int type;
    private int subtype;
    private int axis;
    private Vector3f torquedirection;
    private int function;
    private MJNode part;
    private float fixedtime = 0f;
    
    private int effectortype = 0;
    private float torqueforceratio = 200f;
    private float centralforceratio = 3.5f;
      
    public MJNeuron()
    {
        
    }
    
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    
    public String GetInfo()
    {
        String data = "";
        
        data += "torque:" + torqueforceratio;
        data += " central:" + centralforceratio;
        data += " efftype:" + effectortype;
        //data += " fixedtime:" + fixedtime;
        data += " function:" + function;
        data += " torquedirection:" + torquedirection.toString();
        //data += " axis:" + axis;
        //data += " subtype:" + subtype;
        //data += " type:" + type;
        //data += " mutationrate:" + mutationrate;
        //data += " bias:" + bias;
        data += " " + weights.size();
        data += " " + inputs.size();
        
        return data;
    }
    
    public MJNeuron (float t, int ty, int ax) {
        type = ty;
        inputs = new ArrayList();
        weights = new ArrayList();
        axis = ax;
        
        //always put a torquedirection
        torquedirection = new Vector3f(FastMath.nextRandomFloat()-0.5f,FastMath.nextRandomFloat()-0.5f, FastMath.nextRandomFloat()-0.5f).normalizeLocal();
        switch(axis)
        {
            case 0:
            {
                torquedirection = Vector3f.UNIT_X.clone();
                break;
            }
            case 1:
            {
                torquedirection = Vector3f.UNIT_X.negate().clone();
                break;
            }
            case 2:
            {
                torquedirection = Vector3f.UNIT_Y.clone();
                break;
            }
            case 3:
            {
                torquedirection = Vector3f.UNIT_Y.negate().clone();
                break;
            }
            case 4:
            {
                torquedirection = Vector3f.UNIT_Z.clone();
                break;
            }
            case 5:
            {
                torquedirection = Vector3f.UNIT_Z.negate().clone();
                break;
            }
        }
        
        //effectortype =  FastMath.nextRandomInt(0, 1);
        effectortype =  0;//toque instead of central force
        
        if (type == 0)
        {
            if (axis == -1){
                subtype = 0;
            }
            else if (axis == -2)
            {
                subtype = 1;
            }
            else
            {
                subtype = 2;
            }
        }
        
        //function = FastMath.nextRandomInt(0, 8);
        function = 0;
        
        bias = 1.0f;
       
        
        //torqueforceratio = 250f;
        //centralforceratio = 3.5f;

    }
    
    
    
    public MJNeuron Mirror(boolean mutate)
    {
        if (!mutate){
            MJNeuron newmirrored = new MJNeuron(0.0f, type, axis);
      
            newmirrored.SetSubtype(subtype);
            newmirrored.SetFunction(function);

            Vector3f torqueclone = torquedirection.clone();
           
            newmirrored.SetTorqueDir(torqueclone);
            
            ArrayList<Float> nweig = (ArrayList<Float>)weights.clone();
            newmirrored.SetWeights(nweig);
         
            newmirrored.SetEffectorType(effectortype);

            newmirrored.SetCentralForceRatio(centralforceratio);
            newmirrored.SetTorqueForceRatio(torqueforceratio);
            
            newmirrored.SetBias(bias);
            
            return newmirrored;
        }
        else
        {
            MJNeuron newmirrored = new MJNeuron(0.0f, type, axis);
             
            newmirrored.SetSubtype(subtype);
            
            //newmirrored.SetFunction( FastMath.nextRandomInt(0, 8));
            newmirrored.SetFunction(0);
            
            newmirrored.SetEffectorType(effectortype);
            
            ArrayList<Float> nweig = (ArrayList<Float>)weights.clone();
            
            Random rnd = new Random();
            
            for (int j = 0 ; j < weights.size(); j++)
            {
                if (FastMath.nextRandomFloat() < mutationrate){
                    //float start = -1/(FastMath.sqrt(weights.size()));
                    //float end = 1/(FastMath.sqrt(weights.size()));
                    //float r = FastMath.interpolateLinear(FastMath.nextRandomFloat(), start, end);
                    //float r = this.Gaussian(0f, end, start, end);
                    
                    float r = FastMath.nextRandomFloat()*0.3f - 0.15f;
                    //float r = ((float)rnd.nextGaussian())*0.3f;
                    nweig.set(j, r);   
                }
            }
            
            newmirrored.SetWeights(nweig);
            
            Vector3f torqueclone = torquedirection.clone();
            newmirrored.SetTorqueDir(torqueclone);
            newmirrored.SetCentralForceRatio(centralforceratio);
            
            //if (FastMath.nextRandomFloat() < mutationrate){
            //    float newtorqueforceratio = torqueforceratio + torqueforceratio  * (FastMath.nextRandomFloat()*0.4f - 0.2f);
            //    newmirrored.SetTorqueForceRatio(newtorqueforceratio);
            //}
            
            //Vector3f torqueclone = torquedirection.clone();
            
            /*if (FastMath.nextRandomInt(0, 100) < 30){
                torqueclone.set(FastMath.nextRandomFloat()-0.5f,FastMath.nextRandomFloat()-0.5f, FastMath.nextRandomFloat()-0.5f).normalizeLocal();
            }*/
            //if (FastMath.nextRandomFloat() < mutationrate){
            //    torqueclone.set(this.Gaussian(0f, 5f),this.Gaussian(0f, 5f), this.Gaussian(0f, 5f)).normalizeLocal();
            //}
            
            //newmirrored.SetTorqueDir(torqueclone);
            
            //float randforce = 1.0f + FastMath.nextRandomFloat()*0.5f - 0.25f;
            //float randforce = 1.0f + this.Gaussian(0f, 0.25f);
            
            //newmirrored.SetCentralForceRatio(centralforceratio);
            //newmirrored.SetCentralForceRatio(randforce*centralforceratio);
            
            //randforce = 1.0f + FastMath.nextRandomFloat()*0.5f - 0.25f;
            //randforce = 1.0f + this.Gaussian(0f, 0.25f);
             
            //newmirrored.SetTorqueForceRatio(torqueforceratio);
            //newmirrored.SetTorqueForceRatio(randforce*torqueforceratio);
                     
            if (FastMath.nextRandomFloat() < mutationrate){
                float newbias = FastMath.nextRandomFloat()*0.3f - 0.15f;
                //float newbias = 1.0f;
                //float newbias = ((float)rnd.nextGaussian())*0.3f;
                newmirrored.SetBias(newbias);
            }
            
            return newmirrored;
        }
        
    }
    
    public int GetNeuronType(){
        return type;
    }
    
    public void SetWeights(ArrayList<Float> val){
        weights = val;
        //System.out.println("WEIIIIIIIIIIIIIIIIIIIGHTS!");
        //System.out.println(weights);
    }
    
    public void SetFunction(int ty)
    {
        function = ty;
    }
    
    public void SetBias(float b)
    {
        bias = b;
    }
    
    public void SetEffectorType(int h)
    {
        effectortype = h;
    }
    
    
    public void SetTorqueDir(Vector3f g)
    {
        torquedirection = g;
    }
     
    public void SetSubtype(int t)
    {
        subtype = t;
    }
    
    public void SetPart(MJNode p)
    {
        part = p;
    }
    
    public MJNode GetPart()
    {
        return part;
    }

    public void connect (MJNeuron ... ns) {
        for (MJNeuron n : ns) {
            inputs.add(n);
            //weights.add(FastMath.nextRandomFloat()*2.0f - 1.0f);
        }
    }
    
    public ArrayList<MJNeuron> GetConnections()
    {
        return inputs;
    }
    
    public void GenerateWeights()
    {
        weights.clear();
        Random r = new Random();
        
        for (int i = 0 ; i < inputs.size(); i++)
        {
            //float start = -1/(FastMath.sqrt(inputs.size()));
            //float end = 1/(FastMath.sqrt(inputs.size()));
            //float r = FastMath.interpolateLinear(FastMath.nextRandomFloat(), start, end);
            //float r = this.Gaussian(0f, end, start, end);
            //weights.add(r);
            
            //weights.add(FastMath.nextRandomFloat()*2.0f - 1.0f);
            weights.add(FastMath.nextRandomFloat()*0.3f - 0.15f);
        }
        
        //add a bias weight at the end
        //float start = -1/(FastMath.sqrt(inputs.size()));
        //float end = 1/(FastMath.sqrt(inputs.size()));
        //float r = FastMath.interpolateLinear(FastMath.nextRandomFloat(), start, end);
        //weights.add(r);
        
        //weights.add(FastMath.nextRandomFloat()*2.0f - 1.0f);//bias weight
        weights.add(FastMath.nextRandomFloat()*0.3f - 0.15f);//bias weight
        
        //System.out.println(weights);
    }
    
    public float sigmoid(float x)
    {
        return 1f/(1f + FastMath.exp(-x));
    }
    
 
    public float fire (float tpf, int maxticklevel) 
    {
        
        fixedtime = fixedtime + tpf;
        
        /*if (maxticklevel < 0)
        {
            return 0.0f;
        }*/
        
        switch(type)
        {
            case 0:
            {
                switch(subtype)
                {
                    case 0:
                    {
                        if (part.GetJoint() != null && part.getParent() != null){
                            
                            Vector3f vectora = part.localToWorld(Vector3f.UNIT_Z, null).subtract(part.getWorldTranslation()).normalize();
                            Vector3f vectorb = part.getParent().localToWorld(Vector3f.UNIT_Z, null).subtract(part.getParent().getWorldTranslation()).normalize();
                            
                            float angle = vectora.angleBetween(vectorb);

                            angle = FastMath.interpolateLinear(angle/FastMath.TWO_PI, -1.0f, 1.0f);
                            angle = MJNeuron.round(angle, 2);
                            //System.out.println("angle:" + angle);
                            return angle;
                        }
                        else
                        {
                            //System.out.println("angle: 0 :(");
                            return 0.0f;
                        }
                        //break;
                    }
                    case 1:
                    {
                        if (part.GetGhostControl().getOverlappingCount() > 1){
                            //System.out.println("touch");
                            return 0.1f;
                        }
                        else if (part.getWorldTranslation().y < -17.0f)
                        {
                            //System.out.println("touch ground");
                            return 0.15f;
                        }
                        else{
                            //System.out.println("debug 4:");
                            return 0.0f;
                        }
                    }
                    case 2:
                    {
                        if (MJNode.target != null && !Float.isNaN(MJNode.target.getWorldRotation().getX()) && !Float.isNaN(part.getWorldTranslation().x) && !Float.isNaN(part.getWorldRotation().getX())){
                            switch(axis){
                                case 0:
                                {
                                    float distanceaxis = part.worldToLocal(MJNode.target.getWorldTranslation(), null).normalize().x;
                                    distanceaxis = MJNeuron.round(distanceaxis,2);
                                    //System.out.println("distance x:" + distanceaxis);
                                    return distanceaxis;
                                    //break;
                                }
                                case 1:
                                {
                                    float distanceaxis = part.worldToLocal(MJNode.target.getWorldTranslation(), null).normalize().y;
                                    distanceaxis = MJNeuron.round(distanceaxis,2);
                                    //System.out.println("distance y:" + distanceaxis);
                                    return distanceaxis;
                                    //break;
                                }
                                case 2:
                                {
                                    float distanceaxis = part.worldToLocal(MJNode.target.getWorldTranslation(), null).normalize().z;
                                    distanceaxis = MJNeuron.round(distanceaxis,2);
                                    //System.out.println("distance z:" + distanceaxis);
                                    return distanceaxis;
                                    //break;
                                }
                            }
                            
                        }
                        else
                        {
                            //System.out.println("Nonsense :(");
                            return 0.0f;
                        }
                        //break;
                    }
                }
                return 0.0f;
            }
            case 1:
            {
                if (inputs.size() > 0) {
                    float totalWeight = 0f;
                    int nindex = 0;
                    for (MJNeuron n : inputs) {
                        
                        if (n == null)
                        {
                            System.out.println("Nonsense 1:(");
                            return 0.0f;
                        }
                        
                        switch(function){
                            case 0:
                            {
                                totalWeight += n.fire(tpf, maxticklevel - 1) * weights.get(nindex);
                                break;
                            }
                            case 1:
                            {
                                totalWeight += FastMath.sin(n.fire(tpf, maxticklevel - 1)) * weights.get(nindex);
                                break;
                            }
                            case 2:
                            {
                                totalWeight += FastMath.cos(n.fire(tpf, maxticklevel - 1)) * weights.get(nindex);
                                break;
                            }
                            case 3:
                            {
                                totalWeight += FastMath.atan(n.fire(tpf, maxticklevel - 1)) * weights.get(nindex);
                                break;
                            }
                            case 4:
                            {
                                totalWeight += FastMath.exp(n.fire(tpf, maxticklevel - 1)) * weights.get(nindex);
                                break;
                            }
                            case 5:
                            {
                                totalWeight += FastMath.abs(n.fire(tpf, maxticklevel - 1)) * weights.get(nindex);
                                break;
                            }
                            case 6:
                            {
                                totalWeight += FastMath.sin(fixedtime) * weights.get(nindex);
                                break;
                            }
                            case 7:
                            {
                                float intvalue = n.fire(tpf, maxticklevel - 1); 
                                
                                if (intvalue > 0.34f){
                                    totalWeight += intvalue * weights.get(nindex);
                                }
                                else{
                                    totalWeight += 0.34f * weights.get(nindex);
                                }
                                
                                break;
                            }
                            case 8:
                            {
                                float intvalue = n.fire(tpf, maxticklevel - 1); 
                                
                                if (intvalue < 0.34f){
                                    totalWeight += intvalue * weights.get(nindex);
                                }
                                else{
                                    totalWeight += 0.34f * weights.get(nindex);
                                }
                                
                                break;
                            }
                        }
                        
                        
                        /*--- general perceptron function
                        totalWeight += n.fire(tpf) * weights.get(nindex);
                        -----*/
                        nindex++;
                    }

                    //totalWeight = (float)Math.tanh(totalWeight + bias * weights.get(weights.size() - 1));
                    totalWeight = this.sigmoid( totalWeight + bias * weights.get(weights.size() - 1));
                    totalWeight = MJNeuron.round(totalWeight,2);
                    //System.out.println("sensor2:" + totalWeight);
                    //
                    
                    return totalWeight;
                }
                else {
                    //System.out.println("Nonsense 3:(");
                    return 0.0f;
                }
                
            }
            case 2:
            {
                if (inputs.size() > 0) {
                    float totalWeight = 0.0f;
                    int nindex = 0;
                    for (MJNeuron n : inputs) {
                        
                        totalWeight += n.fire(tpf, maxticklevel - 1)*weights.get(nindex);
                        
                        nindex++;
                        //System.out.println("n:" + nindex);
                    }
                    
                    //totalWeight = (float)Math.tanh(totalWeight + bias * weights.get(weights.size() - 1));
                    totalWeight = this.sigmoid( totalWeight + bias * weights.get(weights.size() - 1));
                    totalWeight = MJNeuron.round(totalWeight,2)*-1;
                    //System.out.println("sensorX:" + totalWeight);
                    
                    
                    Vector3f finaldirection;
                                
                    //if (totalWeight > 0){
                        finaldirection = part.localToWorld(torquedirection, null).subtract(part.getWorldTranslation()).normalize();
                    //}
                    //else
                    //{
                        //finaldirection = part.localToWorld(torquedirection.mult(-1.0f), null).subtract(part.getWorldTranslation()).normalize();
                        
                    //}
                                
                    switch(effectortype)
                    {
                        case 0:
                        {            
                                //float finalvalue = totalWeight*torqueforceratio*0.00225f*100; //for 3 and 4 recursive limit
                                //finalvalue = MJNeuron.round(finalvalue, 2);
                                //finalvalue = FastMath.clamp(finalvalue, 0f, 15f);
                                //System.out.println(finalvalue);
                                //part.GetBodyControl().applyTorque(finaldirection.mult(finalvalue));
                                //System.out.println("sensor3:" + totalWeight + " x:" + finaldirection.x + " y:" + finaldirection.y +  " z:" + finaldirection.z);
                                
                                
                                if (part != null && part.GetJoint() != null){
                                     RotationMotor motor;
                                    
                                    switch(axis)
                                    {
                                        case 0:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_Z);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 0 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                        case 1:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_Z);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*-5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 1 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                        case 2:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_Y);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 2 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                        case 3:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_Y);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*-5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 3 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                        case 4:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_X);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 4 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                        case 5:
                                        {
                                            motor = part.GetJoint().getRotationMotor(PhysicsSpace.AXIS_X);
                                            motor.set(MotorParam.TargetVelocity, totalWeight*-5);
                                            
                                            //MJEnvironmentMain.log.info("Axis 5 " + totalWeight + " tpf:" + tpf + " " + part.getWorldTranslation());
                                            break;
                                        }
                                    }
                                }
                            //}

                            break;
                        }
                        case 1:
                        {
                             //float finalvalue = totalWeight*centralforceratio*tpf*300; //for 3 and 4 recursive limit
                            float finalvalue = totalWeight*centralforceratio*0.025f*300; //for 3 and 4 recursive limit
                            finalvalue = MJNeuron.round(finalvalue, 2);
                            part.GetBodyControl().applyCentralForce(finaldirection.mult(finalvalue));
                             
                            //System.out.println("2:" + finalvalue);

                            break;
                        }
                        
                    }
                    
                }
                return 0.0f;
            }
        }
        
        return 0.0f;
    }

    /*public boolean isFired () {
        return fired;
    }*/
    
    public void SetTorqueForceRatio(float f)
    {
        torqueforceratio = f;
    }
    
    public void SetCentralForceRatio(float f)
    {
        centralforceratio = f;
    }
    
    public void Kill(){
        weights.clear();
        inputs.clear();
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        
        capsule.writeSavableArrayList(inputs, "inputs", null);
        
        float[] we = new float[weights.size()];
        
        for (int i = 0 ; i < weights.size(); i++)
        {
            we[i] = weights.get(i);
        }
        
        capsule.write(we, "weights", null);
        capsule.write(type, "type", 0);
        capsule.write(subtype, "subtypetype", 0);
        capsule.write(axis, "axis", 0);
        capsule.write(torquedirection, "torquedirection", null);
        capsule.write(function, "function", 0);
        capsule.write(part, "part", null);
        capsule.write(fixedtime, "time", 0);
        capsule.write(effectortype, "effectortype", 0);
        capsule.write(centralforceratio, "centralforceratio", 0);
        capsule.write(torqueforceratio, "torqueforceratio", 0);
        capsule.write(bias,"bias", 0);
        capsule.write(mutationrate, "mutationrate", 0);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        //someIntValue   = capsule.readInt(    "someIntValue",   1);
        //someFloatValue = capsule.readFloat(  "someFloatValue", 0f);
        //someJmeObject  = capsule.readSavable("someJmeObject",  new Material());
        
        inputs = capsule.readSavableArrayList("inputs", null);
        
        float[] we = capsule.readFloatArray("weights", null);
        weights = new ArrayList<Float>();
        
        for (int i = 0 ; i < we.length; i++)
        {
            weights.add(we[i]);
        }
        
        type = capsule.readInt("type", 0);
        subtype = capsule.readInt("subtypetype", 0);
        axis = capsule.readInt("axis", 0);
        torquedirection = (Vector3f)capsule.readSavable("torquedirection", null);
        function = capsule.readInt("function", 0);
        part = (MJNode)capsule.readSavable("part", null);
        fixedtime = capsule.readFloat("time", 0);
        effectortype = capsule.readInt("effectortype", 0);
        centralforceratio = capsule.readFloat("centralforceratio", 0);
        torqueforceratio = capsule.readFloat("torqueforceratio", 0);
        bias = capsule.readFloat("bias", 0);
        mutationrate = capsule.readFloat("mutationrate", 0);
    }
}
