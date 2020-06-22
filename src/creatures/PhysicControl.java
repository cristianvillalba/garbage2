/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package creatures;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;

/**
 *
 * @author Cristian.Villalba
 */
public class PhysicControl implements PhysicsTickListener{
    Creature maincreature;
    private float maxticktime = 0.1f;
    private MJEnvironmentMain mainapp;
    private boolean alive = false;
     
     public PhysicControl(MJEnvironmentMain m)
     {
         mainapp = m;
     }
    
    public void SetCreature(Creature d)
    {
        maincreature = d;
    }
    
    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        if (!maincreature.isStartticking())
        {
            if (!maincreature.IsMoving())
            {
                maincreature.setStartticking(true);
                System.out.print("Start Brain!");
                mainapp.wakeCreature();
            }
        }
        else
        {
            if (mainapp.canAllMove()){
                maincreature.Tick(tpf);
            }
        }
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        
    }
    
    
}
