package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private boolean paused;

    private boolean stop;
    
    private AtomicInteger pauseCounter;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, AtomicInteger pauseCounter) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.paused = false;
        this.stop = false;
        this.pauseCounter = pauseCounter;
    }

    public void run() {

        while (this.health > 0 && !immortalsPopulation.isEmpty() && !stop) {
            try {
                this.pause();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
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
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }
        if (this.health == 0) immortalsPopulation.remove(this);
    }

    public void fight(Immortal i2) {
        int comparison = this.name.compareTo(i2.name);
        System.out.println(this.name + " > " + i2.name + " : " + (comparison < 0 ? i2.name : this.name) + "-" + (i2.getHealth() - 10));
        synchronized (comparison < 0 ? i2 : this) {
            synchronized (comparison > 0 ? this : i2) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pause() throws InterruptedException {
        synchronized (immortalsPopulation) {
            while (paused) {
                pauseCounter.incrementAndGet();
                immortalsPopulation.wait();
            }
        }
    }

    public void changePausedStatus() {
        paused = !paused;
    }

    public void stopThread() {
        this.stop = true;
    }
}