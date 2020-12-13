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
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableIntegerValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Tape{
    private GridPane tapeDisplay;
    private GridPane headDisplay;
    private final Lock l = new ReentrantLock();
    private Integer tapeDisplayOffset = 0;
    private Integer tapeHead = 0;
    private Integer centerWidth;
    private Integer tapeIndex = 1;
    ObservableIntegerValue tapeWidth;

    private LinkedList<Character> tape = new LinkedList<>();

    @Override
    public String toString() {
        String s = new String();
        for(char c : tape) {
            s += c;
        }
        return s;
    }

    public void incrementDisplayOffset() {
        System.out.println("in increment");
        tapeDisplayOffset++;
    }
    public void decrementDisplayOffset() {
        System.out.println("In decrement");
        tapeDisplayOffset--;
    }

    public void centerTapeDisplay() {
        tapeDisplayOffset = tapeHead - tapeWidth.get() / 2;
    }

    /*  refreshTapeDisplay_noCenter - works just like refreshTapeDisplay but will not center the tape
    */
    public void refreshTapeDisplay_noCenter(){
                int index = tapeDisplayOffset;
                Character[] tapeChars = getTapeAsArray();
                int size = tapeChars.length;

                System.out.println("tapeHead: " + tapeHead + "  tapeWidth: " + tapeWidth.get());
                System.out.println("centerWidth: " + centerWidth + "   tapeIndeX: " + tapeIndex);
                System.out.println("tapeDisplayoffset: " + tapeDisplayOffset);
                



                for(Node n : tapeDisplay.getChildren()) {
                    if (n instanceof StackPane) {
                        for(Node b : ((StackPane) n).getChildren()) {
                            
                            if (b instanceof Label) {
                                if(index < size && index >= 0) {
                                    ((Label) b).setText(tapeChars[index].toString());
                                    ((Label) b).setFont(Font.font(20));
                                }
                                else {
                                    ((Label) b).setText(" ");
                                }
                            }
                            
                            if (b instanceof Rectangle) {
                                if (index == tapeHead) ((Rectangle) b).setFill(Paint.valueOf("#CAE1F9"));
                                else ((Rectangle) b).setFill(Color.TRANSPARENT);
                            }
                        }
                        index++;
                    }
                }
                
                
                index = tapeDisplayOffset;
                for(Node n: headDisplay.getChildren()) {
                    if (n instanceof StackPane) {
                        for (Node b : ((StackPane) n).getChildren()) {
                            if (b instanceof Label) {
                                if (index == getTapeHead()) {
                                    ((Label) b).setText("↓");
                                    ((Label) b).setFont(Font.font(20));
                                } else {
                                    ((Label) b).setText(" ");
                                }
                            }
                        }
                    }
                    index++;
                }
                
    }

    public void refreshTapeDisplay() {
                // center the tape if it reaches the edge
                int headIndex;
                headIndex = tapeHead % (tapeWidth.get() / 2);
                if (headIndex >= (tapeWidth.get()/2)-1){
                    centerTapeDisplay();
                }

                int index = tapeDisplayOffset;
                Character[] tapeChars = getTapeAsArray();
                int size = tapeChars.length;

                for(Node n : tapeDisplay.getChildren()) {
                    if (n instanceof StackPane) {
                        for(Node b : ((StackPane) n).getChildren()) {
                            
                            if (b instanceof Label) {
                                if(index < size && index >= 0) {
                                    ((Label) b).setText(tapeChars[index].toString());
                                    ((Label) b).setFont(Font.font(20));
                                }
                                else {
                                    ((Label) b).setText(" ");
                                }
                            }
                            
                            if (b instanceof Rectangle) {
                                if (index == tapeHead) ((Rectangle) b).setFill(Paint.valueOf("#CAE1F9"));
                                else ((Rectangle) b).setFill(Color.TRANSPARENT);
                            }
                        }
                        index++;
                    }
                }
                
                
                index = tapeDisplayOffset;
                for(Node n: headDisplay.getChildren()) {
                    if (n instanceof StackPane) {
                        for (Node b : ((StackPane) n).getChildren()) {
                            if (b instanceof Label) {
                                if (index == getTapeHead()) {
                                    ((Label) b).setText("↓");
                                    ((Label) b).setFont(Font.font(20));
                                } else {
                                    ((Label) b).setText(" ");
                                }
                            }
                        }
                    }
                    index++;
                }
                
    }

    public void setDisplay(GridPane tape, GridPane head, BorderPane tapeArea) {
        tapeDisplayOffset = 0;
        tapeDisplay = tape;
        headDisplay = head;

        Character[] initTapeChars;
        try {
            initTapeChars = getTapeAsArray();
        }
        catch (NullPointerException e) {
            initTapeChars = new Character[] {};
        }
        int initIndex = tapeDisplayOffset;
        int initSize = initTapeChars.length;
        tapeArea.getChildren().remove(tapeDisplay);
        tapeArea.getChildren().remove(headDisplay);
        tapeDisplay = new GridPane();
        tapeDisplay.setAlignment(Pos.CENTER);
        headDisplay = new GridPane();
        headDisplay.setAlignment(Pos.CENTER);

        // FIXME Add a right click listener to choose the head by right clicking the rectangle desired?
        for (Integer i = 0; i < ((int)tapeArea.getWidth() - 130) / 31; i++) {
            StackPane box = new StackPane();
            StackPane headBox = new StackPane();
            Rectangle tapeBox = new Rectangle(30, 30, Color.TRANSPARENT);
            Rectangle headTapeBox = new Rectangle(30, 30, Color.TRANSPARENT);

            Label tapeChar;
            Label headTapeChar;

            if (initIndex == tapeHead) {
                headTapeChar = new Label("↓");
                headTapeChar.setFont(Font.font(20));
                //tapeBox.setFill(Paint.valueOf("#CAE1F9"));
            }
            else {
                headTapeChar = new Label(" ");
            }
            if (initIndex < initSize && initIndex >= 0) {
                tapeChar = new Label(initTapeChars[initIndex].toString());
                tapeChar.setFont(Font.font(20));
            }
            else {
                tapeChar = new Label(" ");
            }
            if (initIndex == tapeHead) tapeBox.setFill(Paint.valueOf("#CAE1F9"));
            else tapeBox.setFill(Color.TRANSPARENT);

            tapeChar.setId(Integer.toString(i + tapeDisplayOffset));

            tapeBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                tapeHead = Integer.parseInt(tapeChar.getId()) + tapeDisplayOffset;
                refreshTapeDisplay();
            });

            tapeChar.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                tapeHead = Integer.parseInt(tapeChar.getId()) + tapeDisplayOffset;

                refreshTapeDisplay();
            });

            headTapeBox.setStroke(Color.TRANSPARENT);
            tapeBox.setStroke(Paint.valueOf("#000000"));
            GridPane.setConstraints(box, i, 0);
            GridPane.setConstraints(headBox, i, 0);
            headBox.getChildren().add(headTapeBox);
            headBox.getChildren().add(headTapeChar);
            headDisplay.getChildren().add(headBox);
            box.getChildren().add(tapeBox);
            box.getChildren().add(tapeChar);
            tapeDisplay.getChildren().add(box);
            initIndex++;
        }
        tapeArea.setTop(headDisplay);
        tapeArea.setCenter(tapeDisplay);



        tapeWidth = Bindings.createIntegerBinding(
                () -> ((int)(tapeArea.getWidth() - 130)) / 31, tapeArea.widthProperty());

        tapeWidth.addListener((obs, oldCount, newCount) -> {
            Character[] tapeChars;
            try {
                tapeChars = getTapeAsArray();
            }
            catch (NullPointerException e) {
                tapeChars = new Character[] {};
            }
            int index = tapeDisplayOffset;
            int size = tapeChars.length;
            tapeArea.getChildren().remove(tapeDisplay);
            tapeArea.getChildren().remove(headDisplay);
            tapeDisplay = new GridPane();
            tapeDisplay.setAlignment(Pos.CENTER);
            headDisplay = new GridPane();
            headDisplay.setAlignment(Pos.CENTER);

            for (Integer i = 0; i < newCount.intValue(); i++) {
                StackPane box = new StackPane();
                StackPane headBox = new StackPane();
                Rectangle tapeBox = new Rectangle(30, 30, Color.TRANSPARENT);
                Rectangle headTapeBox = new Rectangle(30, 30, Color.TRANSPARENT);
                Label tapeChar;
                Label headTapeChar;


                if (index == tapeHead) {
                    headTapeChar = new Label("↓");
                    headTapeChar.setFont(Font.font(20));

                }
                else {
                    headTapeChar = new Label(" ");
                }
                if (index < size && index >= 0) {
                    tapeChar = new Label(tapeChars[index].toString());
                    tapeChar.setFont(Font.font(20));

                }
                else {
                    tapeChar = new Label(" ");

                }
                if (index == tapeHead) tapeBox.setFill(Paint.valueOf("#CAE1F9"));
                else tapeBox.setFill(Color.TRANSPARENT);

                tapeChar.setId(Integer.toString(i));

                tapeBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    tapeHead = Integer.parseInt(tapeChar.getId()) + tapeDisplayOffset;

                    refreshTapeDisplay();
                });

                tapeChar.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    tapeHead = Integer.parseInt(tapeChar.getId()) + tapeDisplayOffset;

                    refreshTapeDisplay();
                });

                headTapeBox.setStroke(Color.TRANSPARENT);
                tapeBox.setStroke(Paint.valueOf("#000000"));
                GridPane.setConstraints(box, i, 0);
                GridPane.setConstraints(headBox, i, 0);
                headBox.getChildren().add(headTapeBox);
                headBox.getChildren().add(headTapeChar);
                headDisplay.getChildren().add(headBox);
                box.getChildren().add(tapeBox);
                box.getChildren().add(tapeChar);
                tapeDisplay.getChildren().add(box);
                index++;
            }
            tapeArea.setTop(headDisplay);
            tapeArea.setCenter(tapeDisplay);
        });
    }


    public boolean setTapeHead(int tapeHead) {
        if(tapeHead > this.tape.size()-1 || tapeHead < 0)
            return false;

        this.tapeHead = tapeHead;
        return true;
    }

    public int getTapeHead(){
        return this.tapeHead;
    }

    // Initialize Tape to t and set Tapehead to the start
    public void initTape(ArrayList<Character> t){
        this.tape.clear();
        this.tape.addAll(t);
        this.tapeHead = 0;
        this.tapeDisplayOffset = 0;
    }

    public int getSize(){ return tape.size(); }

    public void appendTape(Character c){
        tape.addLast(c);
    }

    public void prependTape(Character c){
        tape.addFirst(c);
    }

    public void setTape(Character c) throws Exception{
        tape.set(tapeHead, c);
    }

    public Character currentTapeVal(){
        if(tape.isEmpty())
            appendTape(' ');
        return tape.get(tapeHead);
    }

    public Character left(){
        tapeHead--;

        if(tapeHead == -1){
            prependTape(' ');
            tapeHead = 0;
        }
        return tape.get(tapeHead);
    }

    public Character right(){
        tapeHead++;

        if(tapeHead > tape.size()-1){
            appendTape(' ');
            tapeHead = tape.size()-1;
        }
        return tape.get(tapeHead);
    }

    public Character[] getTapeAsArray(){
        return tape.toArray(new Character[0]);
    }
}
