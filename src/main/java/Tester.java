/*
 *     Simple Turing machine EMulator (STEM)
 *     Copyright (C) 2018  Sam MacLean,  Joel Kovalcson, Dakota Sanders, Matt Matto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 */

import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Tester {
    private String failReason;
    private boolean succeeded;
    private int loops;
    private int curSpeed;
    private boolean cont;

    public boolean isCont() {
        return cont;
    }

    public void setCont(boolean cont) {
        this.cont = cont;
    }

    public void setFailReason(String reason) { failReason = reason; }

    public String getFailReason() {
        return failReason;
    }

    public boolean didSucceed() {
        return succeeded;
    }

    public void setCurSpeed(int speed) {
        this.curSpeed = speed;
    }

    public int getCurSpeed() {
        return curSpeed;
    }

    public Transition nextTransition(State currentState, Tape tape){
        Character curChar = tape.currentTapeVal();
        Transition curTransition = null;


        // Find the transition where the current tape char is
        // equal to the first transition's readChar
        for(Transition t : currentState.getTransition()){
            if(t.getReadChar() == curChar && t.getFromState() == currentState){
                curTransition = t;
                break;
            }
        }

        // If no Transition is found, search for a transition
        // with no read char. I.E. catchall transition
        if(curTransition == null){
            for(Transition t : currentState.getTransition()){
                if(t.getFromState() == currentState && t.getReadChar() == '~'){
                    curTransition = t;
                    break;
                }
            }
        }

        return curTransition;
    }

    public State runMachine(Machine m, State startState) throws Exception{
        State currentState;
        ArrayList<State> states = m.getStates();
        Tape tape = m.getTape();

        // Fail if there is no start state
        currentState = startState;
       if(currentState == null){
            failReason = "Machine has no start state!";
            succeeded = false;
            return null;
        }

        setCurSpeed(m.getSpeed());

        setCont(true);

        loops = 0;

        // Main body

        Transition next = this.nextTransition(currentState, m.getTape());
        while(next != null && cont != false) {

            // Set the color of the selected State
            if(currentState.getCircle() != null){
                currentState.getCircle().setFill(Color.GREENYELLOW);
            }

            // If the writeChar is the null character do not write anything
            if(next.getWriteChar() != '~'){
                try{
                    tape.setTape(next.getWriteChar());
                } catch (Exception e){
                    failReason = String.format("Cannot set %c to tape location %d", next.getWriteChar(), tape.getTapeHead());
                    throw e;
                }
            }

            System.out.printf("Going from State %s to %s along Transition %c ; %c ; %c\n",
                    currentState.getName(), next.getToState().getName(),
                    next.getReadChar(), next.getWriteChar(), next.getMoveDirection().toString().charAt(0));

            TimeUnit.MILLISECONDS.sleep(curSpeed);
            switch(next.getMoveDirection()){
                case LEFT:
                    tape.left();
                    break;
                case RIGHT:
                    tape.right();
                    break;
                case STAY:
                    break;
            }

            Platform.runLater(() -> {
                //m.getTape().centerTapeDisplay();
                m.getTape().refreshTapeDisplay();
            });

            // Reset Colors
            if(currentState.getCircle() != null) {
                currentState.setColor(currentState.getBaseColor());
                currentState.getCircle().setFill(currentState.getBaseColor());
            }

            currentState = next.getToState();
            next = this.nextTransition(currentState, m.getTape());

            loops++;
            // TODO: prompt user if loop goes over X iterations
            if(loops % 1000 == 0){
            }

            //Detect breakpoints
            if(currentState.isDebug()){
                return currentState;
            }
        }

        if(currentState != null)
            currentState.getCircle().setFill(Color.GREENYELLOW);

        Platform.runLater(() -> {
            //m.getTape().centerTapeDisplay();
            m.getTape().refreshTapeDisplay();
        });

        TimeUnit.MILLISECONDS.sleep(curSpeed);
        if(currentState.getCircle() != null) {
            currentState.setColor(currentState.getBaseColor());
            currentState.getCircle().setFill(currentState.getBaseColor());
        }
        this.succeeded = currentState.isAccept();
        return currentState;
    }


}