package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health = new AtomicInteger(0);
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private Object lock = new Object();
    private boolean waiting = false;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health.set(health);
        System.out.println(health);
        this.defaultDamageValue=defaultDamageValue;
        this.lock=lock;
    }

    public void pause() { this.waiting = true; }
    public void restart() { this.waiting = false; }

    public void run() {

        while (true) {

            if(waiting) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {

        synchronized (this) {
            synchronized (lock) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health.addAndGet(defaultDamageValue);
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else{
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }

    public void changeHealth(int v) {
        //System.out.println("change: "+v);
        health.set(v);
    }

    public int getHealth() {
        //System.out.println(health.get());
        return health.get();
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
