/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Cristian.Villalba
 */
public class MJEnvironmentSave implements Savable{
    private ArrayList<Creature> populationselection;
    private int generation;
    private ArrayList<Vector3f> randpositions = new ArrayList<Vector3f>();

    public ArrayList<Creature> getPopulationselection() {
        return populationselection;
    }

    public void setPopulationselection(ArrayList<Creature> populationselection) {
        this.populationselection = populationselection;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }
     
    public ArrayList<Vector3f> getRandpositions() {
        return randpositions;
    }

    public void setRandpositions(ArrayList<Vector3f> randpositions) {
        this.randpositions = randpositions;
    }
    
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.writeSavableArrayList(populationselection, "populationselection", null);
        capsule.write(generation, "generation", 0);
        capsule.writeSavableArrayList(randpositions, "randpositions", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        
        populationselection = capsule.readSavableArrayList("populationselection", null);
        generation = capsule.readInt("generation", 0);
        randpositions = capsule.readSavableArrayList("randpositions", null);
    }
}
