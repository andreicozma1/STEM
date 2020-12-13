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

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;

class State {
	private String name;
	private Text label;
	private double x;
	private double y;
	private boolean start;
	private boolean accept;
	private boolean debug;
	private Color baseColor;
	private Color currColor;
	private Circle circle;
	private Circle acceptCircle;
	private ArrayList<Transition> transition;

	public State(){
		transition = new ArrayList<>();
		this.baseColor = Color.LIGHTGOLDENRODYELLOW;
	}

	public State(String name, double x, double y){
		this.name = name;
		this.x = x;
		this.y = y;
		transition = new ArrayList<>();
	}

	public State(String name, double x, double y, Text label, Circle circle){
		this.name = name;
		this.x = x;
		this.y = y;
		this.label = label;
		this.circle = circle;
		transition = new ArrayList<>();
		this.baseColor = Color.LIGHTGOLDENRODYELLOW;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Text getLabel() {
		return label;
	}
	
	public void setLabel(Text label) {
		this.label = label;
	}
	
	public void setLabelId(String name){
		this.label.setId(name);
		this.label.setText(name);
	}

	public double getX(){
		return x;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public boolean isStart() {
		return start;
	}
	
	public void setStart(boolean start) {
		this.start = start;
	}
	
	public boolean isAccept() {
		return accept;
	}
	
	public void setAccept(boolean accept) {
		this.accept = accept;
	}
	
	public Circle getCircle(){
		return circle;
	}
	
	public void setCircle(Circle circle){
		this.circle = circle;
	}

	public void setCircleId(String name){
		this.circle.setId(name);
	}

	public Circle getAcceptCircle() {
		return acceptCircle;
	}

	public void setAcceptCircle(Circle acceptCircle) {
		this.acceptCircle = acceptCircle;
	}

	public ArrayList<Transition> getTransition() {
		return transition;
	}

	public void addNewTransition(Transition newTransition){
		this.transition.add(newTransition);
	}
	
	public void setTransition(ArrayList<Transition> transition) {
		this.transition = transition;
	}

	public void setDebug(boolean debug) { this.debug = debug; }

	public boolean isDebug() {return debug;}

	public void setColor(Color c){
		this.baseColor = c;
	}

	public Color getBaseColor(){
		return baseColor;
	}

	public Color getCurrColor(){
		return currColor;
	}

	public State cloneState(){
		State clone = new State();

		clone.name = this.name;
		clone.x = this.x;
		clone.y = this.y;
		clone.transition = new ArrayList<>();
		clone.baseColor = this.baseColor;

		return clone;
	}
}
