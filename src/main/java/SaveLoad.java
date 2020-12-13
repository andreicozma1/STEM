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

import javafx.scene.control.Alert;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class SaveLoad {
    private int stateNextVal;

    //This array is needed for Reset Tape
    public ArrayList<Character> globalTape = new ArrayList<>();

    public int getStateNextVal() {
        return stateNextVal;
    }

    public boolean saveMachine(Window window, Machine m){
        /* Get the file to save to from user. */
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(window);


        if(file != null) {
            /* Save the file */
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                /* Calls the machine's toString(). */
                bw.write(m.toString());
                bw.close();
                return true;
            }
            catch (IOException e) {
                /* Error occured during saving. */
                Alert saveError = new Alert(Alert.AlertType.ERROR);
                saveError.setResizable(true);
                saveError.initOwner(window);
                saveError.initModality(Modality.APPLICATION_MODAL);
                saveError.setTitle("Error Saving");
                saveError.setHeaderText("There was an error trying to save this machine.");
                saveError.showAndWait();
                return false;
            }
        }
        else {
            /* No file chosen */
        }
        return false;
    }

    public Machine loadMachine(Window window){

        /* Get a file to load from the user. */
        Machine loadMachine = new Machine();
        FileChooser chooser = new FileChooser();
        //FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Text Files", "*");
        //chooser.getExtensionFilters().add(filter);

        chooser.setTitle("Pick a Turing Machine file");
        File file = chooser.showOpenDialog(window);
        if(file != null) {
            /* Reads in the file */

            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(file));
                String curLine = br.readLine(); // Header (Compare to find out if imported from old or current)

                String JFLAPISHHeader = "// Save File for JFLAP-ISH";
                String STEMHeader = "// Save File for STEM";
                String xTuringHeader = "xTuringMachine File Format 1.0";

                if (curLine.equals(STEMHeader) || curLine.equals(JFLAPISHHeader)) {
                    // Load a file designed for this program.
                    loadMachine = loadSTEM(br, curLine);
                } else if (curLine.equals(xTuringHeader)) {
                    // Load a file from xTuringMachine
                    loadMachine = loadxTuring(br, curLine);
                } else {
                    // Not a valid header format, display a message and return false
                    Alert invalidFileType = new Alert(Alert.AlertType.INFORMATION);
                    invalidFileType.setResizable(true);
                    invalidFileType.setTitle("Invalid File");
                    invalidFileType.setHeaderText("Incorrect File header.");
                    invalidFileType.initOwner(window);
                    invalidFileType.initModality(Modality.APPLICATION_MODAL);
                    invalidFileType.showAndWait();

                }

            }
            catch (Exception e) {
                /* Error occured. */
                Alert fileError = new Alert(Alert.AlertType.ERROR);
                fileError.setResizable(true);
                fileError.setTitle("File Error");
                fileError.setHeaderText("Ran into a problem loading that file!");
                fileError.setContentText(e.getMessage());
                fileError.initOwner(window);
                fileError.initModality(Modality.APPLICATION_MODAL);
                fileError.showAndWait();

            }
        }
        return loadMachine;
    }
    public Machine loadxTuring(BufferedReader br, String curLine) throws IOException{
        Machine loadMachine = new Machine();
        br.readLine(); // TransitionCharacters MaxStates (Not needed for our machine)


        curLine = br.readLine(); // TapeMinIndex TapeMaxIndex TapeHeadIndex; (Grab the head index from the end)

        Pattern tapeInfo = Pattern.compile("(-?\\d+) (-?\\d+) (-?\\d+)\\p{Punct}");
        Matcher tapeMatch = tapeInfo.matcher(curLine);
        int tapeHead;
        if (tapeMatch.matches()) tapeHead = Integer.parseInt(tapeMatch.group(3)) - Integer.parseInt(tapeMatch.group(1));
        else throw new IOException("Bad TapeHead");

        curLine = br.readLine(); // Tape (Grab this and copy it)
        ArrayList<Character> tapeChars = new ArrayList<>();
        for (char c : curLine.toCharArray()){
            if(c == '#')
                tapeChars.add(' ');
            else
                tapeChars.add(c);
        }

        //Have to set the globalTape variable here
        globalTape = tapeChars;

        // Tape is loaded here.
        loadMachine.getTape().initTape(tapeChars);
        loadMachine.getTape().setTapeHead(tapeHead);

        System.out.printf("--- TAPE HEAD = %d ----\n", tapeHead);

        curLine = br.readLine(); // NumTransitions (This tells how many more lines to read for transitions)
        Pattern transitionNum = Pattern.compile("(\\d+)\\p{Punct}");
        Matcher transitionNumMatch = transitionNum.matcher(curLine);
        int numTransitions;
        if (transitionNumMatch.matches()) numTransitions = Integer.parseInt(curLine.substring(0, curLine.length() - 1));
        else throw new IOException("Bad NumTransitions");
        System.out.println(numTransitions);
        Hashtable<String, State> statesNeeded = new Hashtable<>();
        ArrayList<Transition> totalTransitions = new ArrayList<>();

        // Pattern match to read a transition line.
        Pattern transitionLinePattern = Pattern.compile("(\\d+) (\\p{ASCII}) (\\p{ASCII}) (\\p{ASCII}) (-?\\d+)\\p{Punct}"); //
        Matcher transitionMatch;

        for(int i = 0; i < numTransitions;) {
            // Next transition, and if reached the end of file before reading all transitions, throw exception
            // curState read write move nextState;
            if ((curLine = br.readLine()) == null) throw new EOFException();
            //System.out.println(curLine);
            transitionMatch = transitionLinePattern.matcher(curLine);

            // If the line is a valid transition
            if (transitionMatch.matches()) {
                //System.out.println("Found a matching transition line with number of elements: " + transitionMatch.groupCount());
                //System.out.println(transitionMatch.group(1) + " " + transitionMatch.group(2) + " " + transitionMatch.group(3) + " " + transitionMatch.group(4) + " " + transitionMatch.group(5));
                // We have a state to put the transition in
                State startState;
                Transition newTransition = new Transition();
                if(statesNeeded.containsKey(transitionMatch.group(1))) {
                    startState = statesNeeded.get(transitionMatch.group(1));
                }
                // Otherwise make a state
                else {
                    startState = new State();
                    statesNeeded.put(transitionMatch.group(1), startState);
                }


                // Setup the transition
                startState.getTransition().add(newTransition);
                newTransition.setFromState(startState);
                if (transitionMatch.group(2).charAt(0) == '#') newTransition.setReadChar(' '); // Change the blanks to spaces
                else if (transitionMatch.group(2).charAt(0) == '*') newTransition.setReadChar('~'); // Change the wildcard to tilde
                else newTransition.setReadChar(transitionMatch.group(2).charAt(0));

                if (transitionMatch.group(3).charAt(0) == '#') newTransition.setWriteChar(' '); // Change the blanks to spaces
                else if (transitionMatch.group(3).charAt(0) == '*') newTransition.setWriteChar('~'); // Change the wildcard to tilde
                else newTransition.setWriteChar(transitionMatch.group(3).charAt(0));

                if (transitionMatch.group(4).charAt(0) == 'L') newTransition.setMoveDirection(Transition.Direction.LEFT);
                else if (transitionMatch.group(4).charAt(0) == 'R') newTransition.setMoveDirection(Transition.Direction.RIGHT);
                else throw new IOException("Bad Direction"); // Old machine only accepted left and right directions.

                if(statesNeeded.containsKey(transitionMatch.group(5))) {
                    newTransition.setToState(statesNeeded.get(transitionMatch.group(5)));
                }
                else {
                    State toState = new State();
                    statesNeeded.put(transitionMatch.group(5), toState);

                    if(transitionMatch.group(5).equals("-1")) {
                        toState.setAccept(true); // Old machine only had one accept state.
                    }
                    newTransition.setToState(toState);
                }
                totalTransitions.add(newTransition);
                startState.getTransition().add(newTransition);
                i++;

            }
            // Skip the line if it isn't valid
        }
        Integer counter = 0;
        int highestState = Integer.MIN_VALUE;
        for(Map.Entry<String, State> s : statesNeeded.entrySet()) {
            State curState = s.getValue();
            curState.setName(s.getKey());

            curState.setX(100 + 150 * (counter % 5) + 30 * (counter / 5));
            curState.setY(120 + 150 * (counter / 5) + 30 * (counter % 2));
            curState.setLabel(new Text(s.getKey()));

            loadMachine.addState(curState);
            counter++;
            int curVal = Integer.parseInt(curState.getName());
            if (curVal > highestState) highestState = curVal;
        }
        stateNextVal = highestState + 1;
        loadMachine.setTransitions(totalTransitions);


        return loadMachine;
        // Create our own state locations
    }


    public Machine loadSTEM(BufferedReader br, String curLine) throws IOException {
        Machine loadMachine = new Machine();
        Hashtable<String, State> totalStates = new Hashtable<>();
        // TODO: This needs to be changed to add ability to read more information on a line.
        // See https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html for information about Patterns.
        // () are used to create groups that can be read later.
        // ? Is used for optional information
        // + means read more of the same until finished
        Pattern statePattern = Pattern.compile("\t(-?\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\w+) (\\w+)( (\\d*.\\d*) (\\d*.\\d*) (\\d*.\\d*) (\\d*.\\d*))?");
        Pattern transitionPattern = Pattern.compile("\t(-?\\d+) (-?\\d+) (\\p{ASCII}) (\\p{ASCII}) (\\w+)");

        // Read until beginning of states
        while(!curLine.equals("STATES:")) {
            curLine = br.readLine();
        }
        // Process all states
        while(!curLine.startsWith("//")) {
            curLine = br.readLine();
            Matcher stateMatcher = statePattern.matcher(curLine);
            // If there was a matching pattern for a state
            if (stateMatcher.matches()) {
                // Create a state and set everything to values from file
                // TODO: Set information matched with pattern here.
                State newState = new State();
                newState.setLabel(new Text(stateMatcher.group(1)));
                newState.setName(stateMatcher.group(1));
                newState.setX(Double.parseDouble(stateMatcher.group(2)));
                newState.setY(Double.parseDouble(stateMatcher.group(3)));
                if (Boolean.parseBoolean(stateMatcher.group(4))) {
                    loadMachine.setStartState(newState);
                    newState.setStart(true);
                }
                newState.setAccept(Boolean.parseBoolean(stateMatcher.group(5)));
                String k = stateMatcher.group(6);
                if(k != null){
                    Scanner colGrabber = new Scanner(k);
                    Color nucolor = new Color(colGrabber.nextDouble(), colGrabber.nextDouble(), colGrabber.nextDouble(), colGrabber.nextDouble());
                    newState.setColor(nucolor);
                }

                // Add state to machine
                loadMachine.addState(newState);
                // Setup state references by ID for transitions
                totalStates.put(stateMatcher.group(1), newState);

            }
        }
        // Read until beginning of transitions
        while(!curLine.equals("TRANSITION:")) {
            curLine = br.readLine();
        }
        // Process all transitions
        while(!curLine.startsWith("//")) {
            curLine = br.readLine();
            Matcher transitionMatcher = transitionPattern.matcher(curLine);
            // If there was a matching pattern for a transition
            if (transitionMatcher.matches()) {
                // Create a transition and set everything to values from file
                Transition newTransition = new Transition();
                newTransition.setFromState(totalStates.get(transitionMatcher.group(1)));
                totalStates.get((transitionMatcher.group(1))).getTransition().add(newTransition);
                newTransition.setToState(totalStates.get(transitionMatcher.group(2)));
                newTransition.setReadChar(transitionMatcher.group(3).charAt(0));
                newTransition.setWriteChar(transitionMatcher.group(4).charAt(0));
                if(transitionMatcher.group(5).equals("RIGHT")) newTransition.setMoveDirection(Transition.Direction.RIGHT);
                else if(transitionMatcher.group(5).equals("LEFT")) newTransition.setMoveDirection(Transition.Direction.LEFT);
                else if(transitionMatcher.group(5).equals("STAY")) newTransition.setMoveDirection(Transition.Direction.STAY);
                else throw new IOException("Bad Transition");
                // Add transition to machine
                loadMachine.getTransitions().add(newTransition);
            }
        }
        // Read until beginning of tape
        while(!curLine.equals("TAPE:")) {
            curLine = br.readLine();
        }
        // Read tape
        curLine = br.readLine();
        curLine = curLine.substring(1, curLine.length());
        int tapeHead = Integer.parseInt(curLine);
        curLine = br.readLine();
        curLine = curLine.substring(1, curLine.length()); // Remove the tab
        ArrayList<Character> newTape = new ArrayList<>();
        for(char c : curLine.toCharArray()) newTape.add(c);

        //Have to set the globalTape variable here
        globalTape = newTape;

        loadMachine.getTape().initTape(newTape);
        loadMachine.getTape().setTapeHead(tapeHead);

        // Get the highest state ID + 1 to start new states at.
        int highestState = Integer.MIN_VALUE;
        for (Map.Entry<String, State> pair : totalStates.entrySet()) {

            int cur = Integer.parseInt(pair.getKey());
            if (cur > highestState) highestState = cur;
        }
        if(highestState >= 0) {
            stateNextVal = highestState + 1;
        }
        else{
            stateNextVal = 0;
        }

        curLine = br.readLine();
        
        // load in the start triangle rotation
        if(curLine != null){
            String[] cur_rot = curLine.split(":");
            loadMachine.setStartTriRotation(Integer.parseInt(cur_rot[1]));
        }

        return loadMachine;
    }
}
