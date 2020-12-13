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

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;

public class Path {
    private State state1;
    private State state2;
    private Line line;
    private CubicCurve curve;
    private ArrayList<Text> aboveTexts;
    private ArrayList<Text> belowTexts;

    private static double distance = 12;
    private double theta;
    private double textX;
    private double textY;
    private double midPointX;
    private double midPointY;

    public Path (State state1, State state2){
        this.state1 = state1;
        this.state2 = state2;
        aboveTexts = new ArrayList<>();
        belowTexts = new ArrayList<>();
    }

    public ArrayList<Node> addTransition(Transition t){
        ArrayList<Node> ret = new ArrayList<>();

        State fromState = t.getFromState();
        State toState = t.getToState();

        if(toState == fromState){
            if(curve == null){
                curve = new CubicCurve(fromState.getCircle().getCenterX(), fromState.getCircle().getCenterY(),
                        fromState.getCircle().getCenterX()-4*fromState.getCircle().getRadius(),
                        fromState.getCircle().getCenterY()-4*fromState.getCircle().getRadius(),
                        fromState.getCircle().getCenterX()+4*fromState.getCircle().getRadius(),
                        fromState.getCircle().getCenterY()-4*fromState.getCircle().getRadius(),
                        fromState.getCircle().getCenterX(), fromState.getCircle().getCenterY());

                curve.setFill(null);
                curve.setStroke(Color.BLACK);

                ret.add(curve);
            }
            char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
            char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

            Text newText = new Text(String.format("%c ; %c ; %c",
                    readChar, writeChar, t.getMoveDirection().toString().charAt(0)));

            newText.setTextAlignment(TextAlignment.CENTER);

            newText.setX(fromState.getCircle().getCenterX() - 1.6 * distance);
            newText.setY(fromState.getCircle().getCenterY() - 3.25*fromState.getCircle().getRadius() - (aboveTexts.size()  * distance));

            //System.out.printf("X: %f, Y: %f\n", newText.getX(), newText.getY());

            newText.setUserData(t);
            aboveTexts.add(newText);

            ret.add(newText);

        }
        else {
            if (line == null) {
                line = new Line(fromState.getX(), fromState.getY(),
                        toState.getX(), toState.getY());
                line.toBack();


                if (toState.getX() != fromState.getX())
                    theta = computeDegree(fromState.getX(), toState.getX(),
                            fromState.getY(), toState.getY());
                else
                    theta = 90;

                //System.out.println(theta);
                //System.out.println(Math.toRadians(theta));

                //System.out.printf("sin(%f) == %f\n", Math.toRadians(theta), Math.sin(Math.toRadians(theta)));
                //System.out.printf("cos(%f) == %f\n", Math.toRadians(theta), Math.cos(Math.toRadians(theta)));


                textX = distance * Math.sin(Math.toRadians(theta));
                textY = distance * Math.cos(Math.toRadians(theta));

                //System.out.printf("TextX: %f, TextY: %f\n", textX, textY);

                midPointX = (fromState.getX() + toState.getX()) / 2.0;
                midPointY = (fromState.getY() + toState.getY()) / 2.0;

                //System.out.printf("MidPointX: %f, MidpointY: %f\n", midPointX, midPointY);

                //System.out.println("New Line");
                ret.add(line);
            }
            if ((fromState.getX() != toState.getX() && fromState.getX() < toState.getX())
                    || (fromState.getX() == toState.getX() && fromState.getY() < toState.getY())) {

                char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
                char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

                Text newText = new Text(String.format("%c ; %c ; %c -->",
                        readChar, writeChar, t.getMoveDirection().toString().charAt(0)));

                newText.setTextAlignment(TextAlignment.CENTER);

                newText.setX(midPointX + (aboveTexts.size() + 1) * textX - 2 * distance * Math.cos(Math.toRadians(theta)));
                newText.setY(midPointY - (aboveTexts.size() + 1) * textY - 2 * distance * Math.sin(Math.toRadians(theta)));

                newText.getTransforms().add(new Rotate(theta, newText.getX(), newText.getY()));

                //System.out.printf("X: %f, Y: %f\n", newText.getX(), newText.getY());

                newText.setUserData(t);

                aboveTexts.add(newText);

                ret.add(newText);
            } else {
                char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
                char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

                Text newText = new Text(String.format("<-- %c ; %c ; %c",
                        readChar, writeChar, t.getMoveDirection().toString().charAt(0)));

                newText.setTextAlignment(TextAlignment.CENTER);

                newText.setX(midPointX - (belowTexts.size() + 1) * textX - textX * 0.5 - 2 * distance * Math.cos(Math.toRadians(theta)));
                newText.setY(midPointY + (belowTexts.size() + 1) * textY + textY * 0.5 - 2 * distance * Math.sin(Math.toRadians(theta)));

                newText.getTransforms().add(new Rotate(theta, newText.getX(), newText.getY()));

                //System.out.printf("X: %f, Y: %f\n", newText.getX(), newText.getY());

                newText.setUserData(t);

                belowTexts.add(newText);

                ret.add(newText);
            }
        }

        return ret;
    }

    public ArrayList<Node> addTransitions(ArrayList<Transition> tl){
        ArrayList<Node> ret = new ArrayList<>();

        for(Transition t : tl)
            ret.addAll(this.addTransition(t));

        return ret;
    }

    public ArrayList<Node> removeTransition(Transition t){
        ArrayList<Node> ret = new ArrayList<>();
        State fromState = t.getFromState();
        State toState = t.getToState();
        String text;

        if(toState == fromState){
            char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
            char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

            text = String.format("%c ; %c ; %c",
                    t.getReadChar(), t.getWriteChar(), t.getMoveDirection().toString().charAt(0));

            for(Text curText : aboveTexts){
                if (curText.getText().compareTo(text) == 0){
                    ret.add(curText);
                    aboveTexts.remove(curText);
                    break;
                }
            }
        }
        if((fromState.getX() != toState.getX() && fromState.getX() < toState.getX())
                || (fromState.getX() == toState.getX() && fromState.getY() < toState.getY()) ){

            char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
            char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

            text = String.format("%c ; %c ; %c -->",
                    readChar, writeChar, t.getMoveDirection().toString().charAt(0));

            for(Text curText : aboveTexts){
                if (curText.getText().compareTo(text) == 0){
                    ret.add(curText);
                    aboveTexts.remove(curText);
                    break;
                }
            }
        }
        else {
            char readChar = (t.getReadChar() == ' ') ? '☐' : t.getReadChar();
            char writeChar = (t.getWriteChar() == ' ') ? '☐' : t.getWriteChar();

            text = String.format("<-- %c ; %c ; %c",
                    readChar, writeChar, t.getMoveDirection().toString().charAt(0));

            for(Text curText : belowTexts) {
                if (curText.getText().compareTo(text) == 0){
                    ret.add(curText);
                    belowTexts.remove(curText);
                    break;
                }
            }
        }

        if(aboveTexts.isEmpty() && belowTexts.isEmpty()){
            if(line != null)
                ret.add(line);
            if(curve != null)
                ret.add(curve);
            line = null;
            curve = null;
        }

        return ret;
    }

    public ArrayList<Node> removeTransitions(ArrayList<Transition> tl){
        ArrayList<Node> ret = new ArrayList<>();

        for(Transition t : tl)
            ret.addAll(removeTransition(t));

        return ret;
    }

    public ArrayList<Node> getAllNodes(){
        ArrayList<Node> ret = new ArrayList<>();
        if(line != null)
            ret.add(line);
        if(curve != null)
            ret.add(curve);
        ret.addAll(aboveTexts);
        ret.addAll(belowTexts);

        return ret;
    }

    public boolean compareTo(State a, State b){
        if(a == b){
            return a == state1 && a == state2 && b == state1 && b == state2;
        }
        else
            return (a == state1 || a == state2) && (b == state1 || b == state2);
    }

    public State getStateOne(){
        return state1;
    }

    public State getStateTwo(){
        return state2;
    }

    public ArrayList<State> getStates(){
        ArrayList<State> ret = new ArrayList<>();

        ret.add(state1);
        ret.add(state2);

        return ret;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append(String.format("%s and %s\n", state1.getName(), state2.getName()));
        for (Text t : aboveTexts)
            s.append(String.format("%s\n", t.getText()));
        for (Text t : belowTexts)
            s.append(String.format("%s\n", t.getText()));

        return s.toString();
    }

    public void setTextFillColor(Paint paint){
        for (Text t : aboveTexts)
            t.setFill(paint);
        for (Text t : belowTexts)
            t.setFill(paint);
    }

    private static double computeDegree(double x1, double x2, double y1, double y2){
        if (x1 != x2)
            return Math.toDegrees(Math.atan((y2-y1)/(x2-x1)));
        else
            return 0;
    }

}
