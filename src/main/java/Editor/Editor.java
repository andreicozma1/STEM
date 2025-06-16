package Editor;

/*
 *     Simple Turing machine EMulator (STEM)
 *     Copyright (C) 2018  Sam MacLean,  Joel Kovalcson, Dakota Sanders, Matt Matto, Andrei Cozma, Hunter Price
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
import javafx.beans.binding.ObjectExpression;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import Types.*;
import Types.Changes.*;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class Editor {
	private final Stage window;
	private Scene editor;
	private ToolBar menuBar;
	private Pane editorSpace;
	private BorderPane parentPane;
	private ZoomableScrollPane scrollPane;
	private ToggleGroup toggleGroup;
	private Machine currentMachine;
	private EventHandler<MouseEvent> currentHandler;
	private ArrayList<Integer> deletedValues = new ArrayList<>();
	private State transitionFromState;
	private State trackerState;
	private int stateNextVal = 0;
	private int circleRadius;
	private Polygon startTriangle;
	private ContextMenu contextMenu;
	private String machineFile;
	private double prevStateX;
	private double prevStateY;
	private double oldStateX;
	private double oldStateY;
	private Editor thisEditor;
	private final LinkedList<Change> undoStack = new LinkedList<Change>();
	private final LinkedList<Change> redoStack = new LinkedList<Change>();
	private boolean draggingMouse = false;
	private ArrayList<State> selectedStates = new ArrayList<State>();
	private final Rectangle selectedArea = new Rectangle(0, 0, 0, 0);
	private final ContextMenu selectedAreaMenu = new ContextMenu();
	private Shape choosingBox;
	private double firstX; // for letting the user select
	private double firstY; // an area from any corner
	// used for rotating the start triangle
	private static final int START_LEFT = 0;
	private static final int START_BOTTOM = 1;
	private static final int START_RIGHT = 2;
	private static final int START_TOP = 3;
	//
	private static final int UNDO_MEMORY_LENGTH = 1000;

	// used for drawing comment boxes
	private EventHandler<MouseEvent> pressHandler = event -> {
		System.out.println("Dummy Event");
	};
	private EventHandler<MouseEvent> dragHandler = event -> {
		System.out.println("Dummy Event");
	};
	private EventHandler<MouseEvent> releaseHandler = event -> {
		System.out.println("Dummy Event");
	};

	public Editor(Boolean shouldLoad) {
		window = new Stage();
		parentPane = new BorderPane();

		editorSpace = new Pane();
		// editorSpace.setPrefSize(13000, 10000);

		scrollPane = new ZoomableScrollPane(editorSpace);
		parentPane.setCenter(scrollPane);
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // make the editor space take up the entire screen when zoomed out all the way
		int uiHeight = 175;
		editorSpace.setPrefSize((screenBounds.getWidth() - 2) / scrollPane.getMaxScaleValue(),
				(screenBounds.getHeight() - uiHeight) / scrollPane.getMaxScaleValue());
    

		editor = new Scene(parentPane, screenBounds.getWidth() / 2, screenBounds.getHeight() / 2);
		System.out.println(screenBounds.getWidth() / 2);
		System.out.println(screenBounds.getHeight() / 2);
		editor.getStylesheets().add("transparenttextarea.css");

		initMenuBar();
		initContextMenu();
		initSelectedRectangle();

		window.setMinWidth(screenBounds.getWidth() / 3);
		window.setMinHeight(screenBounds.getHeight() / 3);

		circleRadius = 20;
		startTriangle = new Polygon();

		if (shouldLoad) {
			if (!loadMachine())
				return;
		} else
			newMachine();

		window.setScene(editor);
		window.show();
		thisEditor = this;
	}

	// Call when exiting the Editor
	private boolean deleteEditor() {
		System.out.println("If you see this you should be saving your machine");
		if (machineFile.compareTo(currentMachine.toString()) != 0) {
			ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
			ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
			ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

			Alert saveAlert = new Alert(Alert.AlertType.WARNING, "You have not saved your machine, would you like to?");
			saveAlert.setResizable(true);
			saveAlert.initOwner(window);
			saveAlert.initModality(Modality.APPLICATION_MODAL);
			saveAlert.setTitle("Warning!");
			saveAlert.getButtonTypes().setAll(yes, no, cancel);

			Optional<ButtonType> buttonData = saveAlert.showAndWait();

			if (buttonData.isPresent() && buttonData.get().getButtonData() == ButtonBar.ButtonData.YES) {
				if (!saveMachine(window, currentMachine))
					return false;
			} else if (buttonData.isPresent()
					&& buttonData.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
				return false;
			} else if (!buttonData.isPresent())
				return false;
		}

		window.setOnCloseRequest(null);

		deletedValues.clear();

		window.setMinWidth(300);
		window.setMinHeight(400);
		window.setHeight(400);
		window.setWidth(300);

		/* garbage collection to the rescue */
		editor = null;
		menuBar = null;
		editorSpace = null;
		scrollPane = null;
		currentMachine = null;
		parentPane = null;
		if (currentHandler != null) {
			window.removeEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
		}
		currentHandler = null;
		return true;
	}

	//   ___ _   _ ___ _____ ____
	//  |_ _| \ | |_ _|_   _/ ___|
	//   | ||  \| || |  | | \___ \
	//   | || |\  || |  | |  ___) |
	//  |___|_| \_|___| |_| |____/
	//
	private void initSelectedRectangle() {
		selectedArea.setFill(new Color(0.1, 0.1, 0.1, 0.1));
		selectedArea.setStroke(Color.BLACK);
		selectedArea.toFront();

		selectedArea.setOnMouseEntered(event -> {
			if (!this.draggingMouse) {
				selectedArea.setFill(new Color(0.1, 0.1, 0.1, 0.15));
			}
		});

		selectedArea.setOnMouseExited(event -> {
			if (!this.draggingMouse) {
				selectedArea.setFill(new Color(0.1, 0.1, 0.1, 0.1));
			}
		});

		MenuItem createUnit = new MenuItem("Create Unit");
        createUnit.setOnAction(event -> {
            System.out.println("Creating Unit");
        });

		MenuItem deleteArea = new MenuItem("Delete");
		deleteArea.setOnAction(event -> {
			selectedArea.setWidth(-1);
			addChange(new StateManyDelete(selectedStates, currentMachine, this));
		});
		selectedAreaMenu.getItems().addAll(createUnit, deleteArea);
		selectedArea.setOnContextMenuRequested(
				event -> selectedAreaMenu.show(selectedArea, event.getScreenX(), event.getScreenY()));

		selectedArea.setOnMousePressed(event -> {
			scrollPane.setPannable(false);
			draggingMouse = true;

			oldStateX = selectedArea.getX();
			oldStateY = selectedArea.getY();

			prevStateX = event.getSceneX();
			prevStateY = event.getSceneY();
		});

		selectedArea.setOnMouseDragged(event -> {
			// get the offsets
			double offsetX = (event.getSceneX() - prevStateX) / scrollPane.scaleValue;
			double offsetY = (event.getSceneY() - prevStateY) / scrollPane.scaleValue;

			// if the resulting offset would push the selectedRectangle off screen, do not move along that axis
			if (selectedArea.getX() + offsetX < 0
					|| selectedArea.getX() + selectedArea.getWidth() + offsetX > editorSpace.getPrefWidth()) {
				offsetX = 0;
			} else {
				prevStateX = event.getSceneX();
			}
			if (selectedArea.getY() + offsetY < 0
					|| selectedArea.getY() + selectedArea.getHeight() + offsetY > editorSpace
							.getPrefHeight()) {
				offsetY = 0;
			} else {
				prevStateY = event.getSceneY();
			}
			// move the rectangle
			selectedArea.setX(selectedArea.getX() + offsetX);
			selectedArea.setY(selectedArea.getY() + offsetY);
			// move each state by the offsets
			ArrayList<Transition> transitions = new ArrayList<Transition>();
			for (State state : selectedStates) {
				double newX = state.getX() + offsetX;
				double newY = state.getY() + offsetY;
				state.getCircle().setCenterX(newX);
				state.getCircle().setCenterY(newY);

				state.getSelctedCircle().setCenterX(newX);
				state.getSelctedCircle().setCenterY(newY);

				state.setX(newX);
				state.setY(newY);

				state.getLabel().setX(newX - (state.getLabel().getLayoutBounds().getWidth() / 2));
				state.getLabel().setY(newY + (state.getLabel().getLayoutBounds().getHeight() / 4));

				// update the accept circle if needed
				if (state.isAccept()) {
					state.getAcceptCircle().setCenterX(newX);
					state.getAcceptCircle().setCenterY(newY);
				}

				// if the state is a start state, redraw it
				if (state.isStart()) {
					drawStartTriangle(state);
				}

				transitions.addAll(state.getTransition());
				for (Transition t : currentMachine.getTransitions()) {
					if (t.getToState() == state && t.getToState() != t.getFromState()) {
						transitions.add(t);
					}
				}
			}
			redrawPaths(transitions);
		});

		selectedArea.setOnMouseReleased(event -> {
			// create the StateManyMove change
			if (selectedArea.getX() != oldStateX || selectedArea.getY() != oldStateY) {
				addChange(new StateManyMove(selectedArea, currentMachine, selectedStates,
						selectedArea.getX() - oldStateX,
						selectedArea.getY() - oldStateY, this));
			}
			scrollPane.setPannable(true);
		});

		this.editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (!draggingMouse) {
				if (!(event.getX() > selectedArea.getX()
						&& event.getX() < selectedArea.getX() + selectedArea.getWidth()
						&& event.getY() > selectedArea.getY()
						&& event.getY() < selectedArea.getY() + selectedArea.getHeight())) {
					unselectAllStates();
				}
			}
			draggingMouse = false;
		});

		this.editorSpace.getChildren().add(selectedArea);
	}

	public void unselectAllStates() {
		selectedArea.setWidth(-1);
		for (State state : selectedStates) {
			state.setSelected(false);
			this.editorSpace.getChildren().remove(state.getSelctedCircle());
			state.setSelctedCircle(null);
		}
		selectedStates.clear();
	}

	private void initTapeDisplay() {
		// StackPane to overlay elements
		// GridPane to display the boxes and the characters BorderPane for buttons
		BorderPane tapeBar = new BorderPane();
		parentPane.setBottom(tapeBar);

		GridPane tapeDisplay = new GridPane();
		tapeDisplay.setAlignment(Pos.CENTER);

		GridPane headDisplay = new GridPane();
		headDisplay.setAlignment(Pos.CENTER);

		// Move tape view right button
		Button shiftRight = new Button(">>");
		shiftRight.setPrefWidth(50);
		shiftRight.setPrefHeight(30);

		tapeBar.setPrefHeight(0);
		// Move tape view left button
		Button shiftLeft = new Button("<<");
		shiftLeft.setPrefWidth(50);
		shiftLeft.setPrefHeight(30);

		tapeBar.setTop(headDisplay);
		tapeBar.setCenter(tapeDisplay);
		tapeBar.setLeft(shiftLeft);
		tapeBar.setRight(shiftRight);
		//tapeArea.setPrefHeight(headDisplay.getHeight() + tapeDisplay.getHeight());

		shiftLeft.setOnMouseClicked((button) -> {
			currentMachine.getTape().decrementDisplayOffset();
			currentMachine.getTape().refreshTapeDisplay_noCenter();
		});

		shiftRight.setOnMouseClicked((button) -> {
			currentMachine.getTape().incrementDisplayOffset();
			currentMachine.getTape().refreshTapeDisplay_noCenter();
		});

		currentMachine.getTape().setDisplay(tapeDisplay, headDisplay, tapeBar);
	}

	/* This sets up the menu bar, but does NOT set the button actions */
	private void initMenuBar() {
		menuBar = new ToolBar();
		parentPane.setTop(menuBar);

		toggleGroup = new ToggleGroup();

		ToggleButton addState = new ToggleButton("Add State (1)");
		addState.setUserData("Add State");
		addState.setToggleGroup(toggleGroup);

		ToggleButton deleteState = new ToggleButton("Delete (3)");
		deleteState.setUserData("Delete Value");
		deleteState.setToggleGroup(toggleGroup);

		ToggleButton addTransition = new ToggleButton("Add Transition (2)");
		addTransition.setUserData("Add Transition");
		addTransition.setToggleGroup(toggleGroup);

		ToggleButton editTransition = new ToggleButton("Edit Transition (4)");
		editTransition.setUserData("Edit Transition");
		editTransition.setToggleGroup(toggleGroup);

		ToggleButton addCommentBox = new ToggleButton("Add Comment Box (5)");
		addCommentBox.setUserData("Add Comment Box");
		addCommentBox.setToggleGroup(toggleGroup);

		// END TOGGLE BUTTONS

		Separator separator = new Separator();
		separator.setOrientation(Orientation.VERTICAL);

		Separator separator1 = new Separator();
		separator1.setOrientation(Orientation.VERTICAL);

		// Begin NON-Toggle buttons

		//quickTapeEdit takes the text input and updates the tape with the given value
		TextField quickTapeEdit = new TextField();
		quickTapeEdit.setOnAction(e -> editTape(window, currentMachine, quickTapeEdit.getText()));

		Button tapeButton = new Button("Edit Tape (E)");
		tapeButton.setOnAction(e -> editTape(window, currentMachine, quickTapeEdit.getText()));

		//New Reset Button
		Button resetButton = new Button("Reset Tape (R)");
		resetButton.setOnAction(e -> resetTape(currentMachine));

		// Run Machine with options for speed
		MenuItem manualControl = new MenuItem("Manual");
		MenuItem slow = new MenuItem("Slow");
		slow.setOnAction(e -> {
			currentMachine.setSpeed(500);
		});
		MenuItem normal = new MenuItem("Normal");
		normal.setOnAction(e -> {
			currentMachine.setSpeed(250);
		});
		MenuItem fast = new MenuItem("Fast");
		fast.setOnAction(e -> {
			currentMachine.setSpeed(75);
		});
		MenuItem noDelay = new MenuItem("No Delay");
		noDelay.setOnAction(e -> {
			currentMachine.setSpeed(0);
		});

		SplitMenuButton runMachine = new SplitMenuButton(manualControl, slow, normal, fast, noDelay);
		runMachine.setText("Run Machine (X)");
		runMachine.setOnAction(e -> {
			if (currentMachine.getStartState() == null) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setResizable(true);
				alert.initOwner(window);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle("The machine has finished");
				alert.setHeaderText("No start state set.");
				alert.showAndWait();
			} else {
				runMachine(runMachine, addState, deleteState, addTransition, editTransition, tapeButton, resetButton);
			}
		});

		manualControl.setOnAction(e -> {
			int oldSpeed = currentMachine.getSpeed();
			currentMachine.setSpeed(-1);
			if (currentMachine.getStartState() == null) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setResizable(true);
				alert.initOwner(window);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle("The machine has finished");
				alert.setHeaderText("No start state set.");
				alert.showAndWait();
			} else {
				runMachine(runMachine, addState, deleteState, addTransition, editTransition, tapeButton, resetButton);
			}
			currentMachine.setSpeed(oldSpeed);
		});

		/*	Button for rotating the start triangle. Just mod-increments the start rotation index, and calls the drawStartTriangle function
		*/
		Button rotateStartTri_button = new Button("Rotate Start Triangle");
		rotateStartTri_button.setOnAction(event -> {
			if (startTriangle != null && currentMachine.getStartState() != null) {
				currentMachine.setStartTriRotation((currentMachine.getStartTriRotation() + 1) % 4);
				drawStartTriangle(currentMachine.getStartState());
			} else {
				Alert a = new Alert(Alert.AlertType.INFORMATION);
				a.setResizable(true);
				a.setTitle("Error!");
				a.setHeaderText("No start state!");
				a.setContentText("Please select a start state and try again.");
				a.showAndWait();
			}
		});

		Button screenshot_button = new Button("Screenshot Machine");
		screenshot_button.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();

			//Set extension filter and set the initial file name to include the png extension
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));
			fileChooser.setInitialFileName("screenshot.png");

			//Prompt user to select a file
			File file = fileChooser.showSaveDialog(null);

			if (file != null) {

				try {
					//Get the bounds of the current viewport and translate it as needed
					SnapshotParameters params = new SnapshotParameters();
					params.setTransform(new Translate(
							(int) scrollPane.getHvalue(),
							(int) scrollPane.getVvalue()));
					WritableImage writableImage = new WritableImage(
							(int) scrollPane.getViewportBounds().getWidth(),
							(int) scrollPane.getViewportBounds().getHeight());

					scrollPane.snapshot(params, writableImage);

					RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);

					//Write the snapshot to the chosen file
					ImageIO.write(renderedImage, "png", file);

				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		Button saveButton = new Button("Save");
		saveButton.setOnAction(event -> saveMachine(window, currentMachine));

		Button undoButton = new Button("Undo");
		undoButton.setOnAction(e -> {
			if (undoStack.size() > 0) {
				redoStack.push(undoStack.pop().undo());
			}
		});

		Button redoButton = new Button("Redo");
		redoButton.setOnAction(e -> {
			if (redoStack.size() > 0)
				undoStack.push(redoStack.pop().apply());
		});

		Button helpButton = new Button("Help");
		helpButton.setOnAction(event -> new HelpMenu());

		// Add toggle buttons
		menuBar.getItems().addAll(addState, addTransition, deleteState, editTransition, addCommentBox);

		// Add separator
		menuBar.getItems().add(separator);

		// Add tape editor UI
		menuBar.getItems().addAll(quickTapeEdit, tapeButton);

		// Add another seperator
		menuBar.getItems().add(separator1);

		// Add non-toggle buttons + Resetting Tape
		menuBar.getItems().addAll(resetButton, runMachine, saveButton, rotateStartTri_button, screenshot_button,
				undoButton, redoButton, helpButton);

		// Cursor when over the bar will always be default cursor
		menuBar.addEventFilter(MouseEvent.MOUSE_MOVED, event -> editor.setCursor(Cursor.DEFAULT));

		// add keyboard shortcuts
		editor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			KeyCode code = event.getCode();
			if (code == KeyCode.DIGIT1) {
				toggleGroup.selectToggle(addState);
			} else if (code == KeyCode.DIGIT2) {
				toggleGroup.selectToggle(addTransition);
			} else if (code == KeyCode.DIGIT3) {
				toggleGroup.selectToggle(deleteState);
			} else if (code == KeyCode.DIGIT4) {
				toggleGroup.selectToggle(editTransition);
			} else if (code == KeyCode.DIGIT5) {
				toggleGroup.selectToggle(addCommentBox);
			} else if (code == KeyCode.ESCAPE) {
				if (toggleGroup.getSelectedToggle() == null) {
					unselectAllStates();
					editorSpace.getChildren().remove(choosingBox);
				}
				toggleGroup.selectToggle(null);
			} else if (code == KeyCode.E) {
				editTape(window, currentMachine, quickTapeEdit.getText());
			} else if (code == KeyCode.R) {
				resetTape(currentMachine);
			} else if (code == KeyCode.X) {
				if (currentMachine.getStartState() == null) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setResizable(true);
					alert.initOwner(window);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.setTitle("The machine has finished");
					alert.setHeaderText("No start state set.");
					alert.showAndWait();
				} else {
					runMachine(runMachine, addState, deleteState, addTransition, editTransition, tapeButton,
							resetButton);
				}
			} else if (code == KeyCode.S && event.isControlDown()) {
				saveMachine(window, currentMachine);
			}
			// add the two common shortcuts for redo: CTRL+SHIFT+Z and CTRL+Y
			else if ((code == KeyCode.Z && event.isControlDown() && event.isShiftDown())
					|| (code == KeyCode.Y && event.isControlDown())) {
				if (redoStack.size() > 0) {
					undoStack.push(redoStack.pop().apply());
					if (!(undoStack.peek() instanceof StateMove)) {
						unselectAllStates();
					}
				}
			} else if (code == KeyCode.Z && event.isControlDown()) {
				if (undoStack.size() > 0) {
					redoStack.push(undoStack.pop().undo());
					if (!(redoStack.peek() instanceof StateMove)) {
						unselectAllStates();
					}
				}
			}
		});
	}

	public void addChange(Change change) {
		undoStack.push(change.apply());
		// don't reset the redo list if the change is just moving a transition
		if (!(change instanceof StateMove)) {
			redoStack.clear();
		}
		if (undoStack.size() > UNDO_MEMORY_LENGTH) {
			undoStack.removeLast();
		}
		// print out change list
		System.out.println("");
		for (Change c : undoStack) {
			System.out.println(c);
		}
	}

	private void initContextMenu() {
		contextMenu = new ContextMenu();

		MenuItem setStart = new MenuItem("Set Start");
		setStart.setOnAction(event -> {
			State s = (State) contextMenu.getOwnerNode().getUserData();
			drawStartTriangle(s);

			addChange(new StartChange(currentMachine, currentMachine.getStartState(), s, this));
			System.out.printf("State %s is now start\n", currentMachine.getStartState().getName());
		});

		MenuItem toggleAccept = new MenuItem("Toggle Accept");
		toggleAccept.setOnAction(event -> {
			State s = (State) contextMenu.getOwnerNode().getUserData();

			if (s.getAcceptCircle() == null) {
				addChange(new AcceptAddition(s, this));

				System.out.printf("State %s is accept = %s\n", s.getName(), s.isAccept());
			} else {
				addChange(new AcceptDelete(s, this));
				System.out.printf("State %s is accept = %s\n", s.getName(), s.isAccept());
			}
		});

		MenuItem moveState = new MenuItem("Move State");
		moveState.setOnAction(event -> {
			State s = (State) contextMenu.getOwnerNode().getUserData();
			Double initialX = s.getX();
			Double initialY = s.getY();
			ArrayList<Transition> tl = new ArrayList<>();

			tl.addAll(s.getTransition());
			for (Transition t : currentMachine.getTransitions()) {
				if (t.getToState() == s && t.getToState() != t.getFromState()) {
					System.out.printf("Adding Transiton %s -> %s, %c ; %c ; %c\n", t.getFromState().getName(),
							t.getToState().getName(),
							t.getReadChar(), t.getWriteChar(), t.getMoveDirection().toString().charAt(0));
					tl.add(t);
				}
			}

			toggleGroup.selectToggle(null);
			editorSpace.setCursor(Cursor.DEFAULT);

			for (Node n : menuBar.getItems()) {
				if (n instanceof ToggleButton || n instanceof Button || n instanceof SplitMenuButton)
					n.setDisable(true);
			}

			EventHandler<MouseEvent> move = event1 -> {

				if ((Math.abs(event1.getX() - s.getX()) > 5 || Math.abs(event1.getY() - s.getY()) > 5)
						&& event1.getY() > circleRadius) {

					s.setX(event1.getX());
					s.setY(event1.getY());
					redrawState(s);
					redrawPaths(tl);
				}
			};

			EventHandler<MouseEvent> click = new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					editorSpace.removeEventHandler(MouseEvent.MOUSE_MOVED, move);

					if (event.getButton() == MouseButton.PRIMARY) {
						double min = Double.MAX_VALUE;
						for (State state : currentMachine.getStates()) {
							if (state == s)
								continue;
							double dist = distForm(s.getX(), state.getX(),
									s.getY(), state.getY());
							if (min > dist)
								min = dist;
						}

						if (min / 3 < circleRadius) {
							s.setX(initialX);
							s.setY(initialY);
							redrawState(s);
							redrawPaths(tl);
						}
					}
					if (event.getButton() == MouseButton.SECONDARY) {
						s.setX(initialX);
						s.setY(initialY);
						redrawState(s);
						redrawPaths(tl);
					}

					if (s.getX() != initialX && s.getY() != initialY) {
						addChange(new StateMove(currentMachine, s, initialX, initialY, thisEditor));
					}

					for (Node n : menuBar.getItems()) {
						if (n instanceof ToggleButton || n instanceof Button || n instanceof SplitMenuButton)
							n.setDisable(false);
					}
					editorSpace.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
				}
			};

			editorSpace.addEventHandler(MouseEvent.MOUSE_MOVED, move);
			editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, click);
		});

		MenuItem setColor = new MenuItem("Set Color");
		setColor.setOnAction(event -> {
			State s = (State) contextMenu.getOwnerNode().getUserData();
			ColorPicker stateChanger = new ColorPicker(s.getBaseColor());
			Dialog<Color> pickerWindow = new Dialog<>();
			pickerWindow.setTitle("Color Picker");
			pickerWindow.setHeaderText("Select a state color");
			pickerWindow.getDialogPane().getButtonTypes().add(ButtonType.OK);
			pickerWindow.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
			GridPane grid = new GridPane();
			grid.add(stateChanger, 1, 0);
			pickerWindow.getDialogPane().setContent(grid);
			pickerWindow.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.OK) {
					return stateChanger.getValue();
				}
				return null;
			});
			Optional<Color> newColor = pickerWindow.showAndWait();
			if (newColor.isPresent()) {
				addChange(new ColorChange(s, newColor.get()));
			}
		});

		MenuItem setBreak = new MenuItem("Set Breakpoint");
		setBreak.setOnAction(event -> {
			State s = (State) contextMenu.getOwnerNode().getUserData();
			if (!s.isDebug()) {
				addChange(new BreakpointAddition(s));

				System.out.printf("State %s is breakpoint = %s\n", s.getName(), s.isDebug());
			} else {
				addChange(new BreakpointDelete(s));

				System.out.printf("State %s is breakpoint = %s\n", s.getName(), s.isDebug());
			}
		});

		MenuItem copyState = new MenuItem("Copy State");
		copyState.setOnAction(event -> {
			State originState = (State) contextMenu.getOwnerNode().getUserData();
			State state = originState.cloneState();
			Double initialX = state.getX();
			Double initialY = state.getY();
			String name;
			ArrayList<Transition> transitions = new ArrayList<Transition>();
			ArrayList<Path> paths = new ArrayList<Path>();

			// Figure out what the name of the state should be;
			if (deletedValues.isEmpty()) {
				name = Integer.toString(stateNextVal);
				stateNextVal++;
			} else {
				int minIndex = deletedValues.indexOf(Collections.min(deletedValues));
				int savedVal = deletedValues.get(minIndex);
				deletedValues.remove(minIndex);
				name = Integer.toString(savedVal);
			}

			Circle circle = new Circle(state.getX(), state.getY(), circleRadius, Color.LIGHTGOLDENRODYELLOW);
			circle.setId(name);
			circle.setStrokeWidth(2);
			circle.setStroke(Color.BLACK);

			Text text = new Text(name);
			text.setId(name);
			text.setX(circle.getCenterX() - (text.getLayoutBounds().getWidth() / 2));
			text.setY(circle.getCenterY() + (text.getLayoutBounds().getHeight() / 4));

			state.setName(name);
			state.setCircle(circle);
			state.setLabel(text);
			circle.setUserData(state);
			text.setUserData(state);

			if (originState.isAccept()) {
				state.setAccept(true);

				Circle c = new Circle(state.getCircle().getCenterX(), state.getCircle().getCenterY(),
						circleRadius * 1.25, null);
				c.setStrokeWidth(2);
				c.setStroke(Color.BLACK);

				state.setAcceptCircle(c);
			}

			circle.setOnContextMenuRequested(event1 -> {
				contextMenu.show(circle, event1.getScreenX(), event1.getScreenY());
			});
			text.setOnContextMenuRequested(event2 -> {
				contextMenu.show(text, event2.getScreenX(), event2.getScreenY());
			});

			Transition clonedTransition = null;
			// loop through all outgoing transitions associated with the state
			for (Transition tr : originState.getTransition()) {
				// self loop
				if (tr.getFromState() == tr.getToState()) {
					clonedTransition = cloneTransition(state, state, tr.getReadChar(), tr.getWriteChar());
					clonedTransition.setMoveDirection(tr.getMoveDirection());
				}
				// outgoing transition
				else {
					clonedTransition = cloneTransition(state, tr.getToState(), tr.getReadChar(), tr.getWriteChar());
					clonedTransition.setMoveDirection(tr.getMoveDirection());
				}

				transitions.add(clonedTransition);
				Path path = new Path(clonedTransition.getFromState(), clonedTransition.getToState()); // setup a new path between the new from state and old destination
				paths.add(path);
				clonedTransition.setPath(path); // set the transitions new path
			}

			// check for incoming transitions
			for (Transition tr : currentMachine.getTransitions()) { // loop through all transitions in the machine
				if (tr.getToState() == originState && tr.getToState() != tr.getFromState()) {
					clonedTransition = cloneTransition(tr.getFromState(), state, tr.getReadChar(), tr.getWriteChar());
					clonedTransition.setMoveDirection(tr.getMoveDirection());
					transitions.add(clonedTransition);
					Path path = new Path(tr.getFromState(), tr.getToState());
					paths.add(path);
					clonedTransition.setPath(path);
				}
			}

			addChange(new StateAddition(state, transitions, currentMachine, this));

			for (Node n : menuBar.getItems()) {
				if (n instanceof ToggleButton || n instanceof Button || n instanceof SplitMenuButton)
					n.setDisable(true);
			}

			EventHandler<MouseEvent> move = event1 -> {

				if ((Math.abs(event1.getX() - state.getX()) > 5 || Math.abs(event1.getY() - state.getY()) > 5)
						&& event1.getY() > circleRadius) {

					state.setX(event1.getX());
					state.setY(event1.getY());
					redrawState(state);
					redrawPaths(transitions);
				}
			};

			EventHandler<MouseEvent> click = new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					editorSpace.removeEventHandler(MouseEvent.MOUSE_MOVED, move);

					if (event.getButton() == MouseButton.PRIMARY) {
						double min = Double.MAX_VALUE;
						for (State s : currentMachine.getStates()) {
							if (s == state)
								continue;
							double dist = distForm(state.getX(), s.getX(),
									state.getY(), s.getY());
							if (min > dist)
								min = dist;
						}

						if (min / 3 < circleRadius) {
							state.setX(initialX);
							state.setY(initialY);
							redrawState(state);
							redrawPaths(transitions);
						}
					}
					if (event.getButton() == MouseButton.SECONDARY) {
						state.setX(initialX);
						state.setY(initialY);
						redrawState(state);
						redrawPaths(transitions);
					}

					for (Node n : menuBar.getItems()) {
						if (n instanceof ToggleButton || n instanceof Button || n instanceof SplitMenuButton)
							n.setDisable(false);
					}
					editorSpace.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
				}

			};

			editorSpace.addEventHandler(MouseEvent.MOUSE_MOVED, move);
			editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, click);
		});

		contextMenu.getItems().addAll(setStart, toggleAccept, moveState, copyState, setBreak, setColor);
	}

	//   __  __            _     _              ___       _ _
	//  |  \/  | __ _  ___| |__ (_)_ __   ___  |_ _|_ __ (_) |_ ___
	//  | |\/| |/ _` |/ __| '_ \| | '_ \ / _ \  | || '_ \| | __/ __|
	//  | |  | | (_| | (__| | | | | | | |  __/  | || | | | | |_\__ \
	//  |_|  |_|\__,_|\___|_| |_|_|_| |_|\___| |___|_| |_|_|\__|___/
	//
	public void newMachine() {
		currentMachine = new Machine();
		window.setTitle("*new machine*");
		startMachine();
	}

	public boolean saveMachine(Stage window, Machine m) {
		SaveLoad saveLoad = new SaveLoad();
		if (saveLoad.saveMachine(window, m)) {
			machineFile = m.toString();
			System.out.println(machineFile);
			return true;
		}
		return false;
	}

	//Where I store global tape given to us from the SaveLoad class's current tape
	ArrayList<Character> originalTape = new ArrayList<>();

	public boolean loadMachine() {
		SaveLoad saveLoad = new SaveLoad();
		currentMachine = saveLoad.loadMachine(window);

		if (currentMachine == null)
			return false;

		stateNextVal = saveLoad.getStateNextVal();

		//When the machine is loaded, we set originalTape
		originalTape = saveLoad.globalTape;

		//currentMachine = currentMachine;
		redrawAllStates();
		redrawAllPaths();
		redrawAllComments();

		//currentMachine.getTape().refreshTapeDisplay();
		startMachine();
		return true;
	}

	/* Called whenever a new machine is setup */
	private void startMachine() {
		initTapeDisplay();
		machineFile = currentMachine.toString();

		window.setOnCloseRequest(we -> {
			if (!deleteEditor())
				we.consume();
		});

		ObjectExpression<Font> textTrack = Bindings.createObjectBinding(
				() -> Font.font(Math.min(editorSpace.getWidth() / 55, 20)), editorSpace.widthProperty());

		Circle circle = new Circle(circleRadius, null);
		circle.setStroke(Color.BLACK);

		SnapshotParameters sp = new SnapshotParameters();
		sp.setFill(Color.TRANSPARENT);

		Image img = circle.snapshot(sp, null);
		ImageCursor cursor = new ImageCursor(img, img.getHeight() / 2, img.getWidth() / 2);

		EventHandler<MouseEvent> MoveEvent = event -> {
			if (event.getY() > circleRadius)
				editorSpace.setCursor(cursor);
			else
				editorSpace.setCursor(Cursor.DEFAULT);
		};

		toggleGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {

			//   ____       _
			//  / ___|  ___| |_ _   _ _ __
			//  \___ \ / _ \ __| | | | '_ \
			//   ___) |  __/ |_| |_| | |_) |
			//  |____/ \___|\__|\__,_| .__/
			//                       |_|

			editorSpace.removeEventFilter(MouseEvent.MOUSE_MOVED, MoveEvent);
			editorSpace.setCursor(Cursor.DEFAULT);
			editorSpace.getChildren().remove(choosingBox);
			if (currentHandler != null)
				editorSpace.removeEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
			if (pressHandler != null)
				editorSpace.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
			if (dragHandler != null)
				editorSpace.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
			if (releaseHandler != null)
				editorSpace.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			if (transitionFromState != null) {
				transitionFromState.getCircle().setFill(transitionFromState.getBaseColor());
				transitionFromState = null;
			}
			for (Path p : currentMachine.getPaths())
				p.setTextFillColor(Color.BLACK);

			for (TextArea ta : currentMachine.getComments())
				ta.setEditable(false);

			unselectAllStates();

			redrawAllPaths();

			//   _   _
			//  | \ | | ___  _ __   ___
			//  |  \| |/ _ \| '_ \ / _ \
			//  | |\  | (_) | | | |  __/
			//  |_| \_|\___/|_| |_|\___|
			//
			if (new_toggle == null) {
				System.out.println("No toggle selected");

				// allow for holding shift and dragging to select multiple states at once for moving
				// this will later be used for condensing a set of states into a unit
				// drew much 'inspiration' from how comment boxes are done
				// does not select a state unless the middle of the state is within the selected area
				pressHandler = event -> {
					if (event.isShiftDown()) {
						scrollPane.setPannable(false);
						this.draggingMouse = true;
						selectedArea.setX(event.getX());
						selectedArea.setY(event.getY());
						selectedArea.setWidth(0);
						selectedArea.setHeight(0);
						selectedArea.toFront();
						this.firstX = event.getX();
						this.firstY = event.getY();
					}
				};
				dragHandler = event -> {
					if (event.isShiftDown()) {
						if (event.getX() < this.firstX) {
							selectedArea.setX(Math.max(0, event.getX()));
							selectedArea.setWidth(this.firstX - Math.max(0, event.getX()));
						} else {
							selectedArea.setWidth(
									event.getX() - this.firstX);
						}
						if (event.getY() < this.firstY) {
							selectedArea.setY(Math.max(0, event.getY()));
							selectedArea.setHeight(this.firstY - Math.max(0, event.getY()));
						} else {
							selectedArea.setHeight(event.getY() - this.firstY);
						}

						Circle selectedCircle = null;

						// get which states are now selected
						for (State state : currentMachine.getStates()) {
							if (this.selectedStates.contains(state)) {
								// check if this state was now unselected
								if (!(state.getX() > selectedArea.getX()
										&& state.getX() < selectedArea.getX()
												+ selectedArea.getWidth()
										&& state.getY() > selectedArea.getY()
										&& state.getY() < selectedArea.getY()
												+ selectedArea.getHeight())) {
									selectedStates.remove(state);
									state.setSelected(false);
									this.editorSpace.getChildren().remove(state.getSelctedCircle());
									state.setSelctedCircle(null);
								}
								continue;
							}

							// check if center of state is within the rectangle
							if (state.getX() > selectedArea.getX()
									&& state.getX() < selectedArea.getX()
											+ selectedArea.getWidth()
									&& state.getY() > selectedArea.getY()
									&& state.getY() < selectedArea.getY()
											+ selectedArea.getHeight()) {
								selectedStates.add(state);
								state.setSelected(true);
								selectedCircle = new Circle(state.getX(), state.getY(), circleRadius * 1.5,
										Color.TRANSPARENT);
								selectedCircle.setStrokeWidth(2);
								selectedCircle.setStroke(Color.BLUE);
								state.setSelctedCircle(selectedCircle);
								this.editorSpace.getChildren().add(selectedCircle);
							}
						}
					}
				};
				releaseHandler = event -> {
					scrollPane.setPannable(true);
					selectedArea.toFront();

					// enable/disable the delete option
					if (selectedStates.size() > 0) {
						selectedAreaMenu.getItems().get(1).setDisable(false);
					} else {
						selectedAreaMenu.getItems().get(1).setDisable(true);
					}

					// enable/disable the createUnit option
					if (selectedStates.size() < 2) {
						selectedAreaMenu.getItems().get(0).setDisable(true);
					} else {
						selectedAreaMenu.getItems().get(0).setDisable(false);
					}
				};

				editorSpace.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				editorSpace.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
				editorSpace.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}

			//      _       _     _   ____  _        _
			//     / \   __| | __| | / ___|| |_ __ _| |_ ___
			//    / _ \ / _` |/ _` | \___ \| __/ _` | __/ _ \
			//   / ___ \ (_| | (_| |  ___) | |_ (_| | |_  __/
			//  /_/   \_\__,_|\__,_| |____/ \__\__,_|\__\___|
			//
			else if (new_toggle.getUserData() == "Add State") {
				System.out.println(new_toggle.getUserData());

				editorSpace.addEventFilter(MouseEvent.MOUSE_MOVED, MoveEvent);

				// Define our new click handler
				currentHandler = event -> {

					// If click is left click and not too close to another circle
					double minDist = calcDist(event, currentMachine);
					if (event.getButton() == MouseButton.PRIMARY
							&& !(event.getTarget() instanceof Circle)
							&& !(event.getTarget() instanceof Text)
							&& (minDist / 3) >= circleRadius
							&& event.getY() > circleRadius) {

						// Figure out what the name of the state should be;
						String name;
						if (deletedValues.isEmpty()) {
							name = Integer.toString(stateNextVal);
							System.out.println(stateNextVal);
							stateNextVal++;
						} else {
							int minIndex = deletedValues.indexOf(Collections.min(deletedValues));
							int savedVal = deletedValues.get(minIndex);
							deletedValues.remove(minIndex);
							System.out.println(savedVal);
							name = Integer.toString(savedVal);
						}

						Circle c = new Circle(event.getX(), event.getY(), circleRadius, Color.LIGHTGOLDENRODYELLOW);
						c.setId(name);
						c.setStrokeWidth(2);
						c.setStroke(Color.BLACK);

						Text t = new Text(name);
						t.setId(name);
						t.setX(c.getCenterX() - (t.getLayoutBounds().getWidth() / 2));
						t.setY(c.getCenterY() + (t.getLayoutBounds().getHeight() / 4));

						// Set Create State and add it to the Node's user data
						// so it is easy to find if clicked on
						State s = new State(name, event.getX(), event.getY(), t, c);
						c.setUserData(s);
						t.setUserData(s);

						c.setOnContextMenuRequested(event1 -> {
							contextMenu.show(c, event1.getScreenX(), event1.getScreenY());
						});
						t.setOnContextMenuRequested(event2 -> {
							contextMenu.show(t, event2.getScreenX(), event2.getScreenY());
						});

						// add the event listeners for the new states
						s.getCircle().setOnMousePressed(stateClicked);
						s.getCircle().setOnMouseDragged(stateDragged);
						s.getCircle().setOnMouseReleased(stateReleased);

						s.getLabel().setOnMousePressed(stateClicked);
						s.getLabel().setOnMouseDragged(stateDragged);
						s.getLabel().setOnMouseReleased(stateReleased);
						addChange(new StateAddition(s, null, currentMachine, this));
					}
				};

				// Add the new event handler to the editorSpace
				editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
			}

			//   ____       _      _        __     __    _
			//  |  _ \  ___| | ___| |_ ___  \ \   / /_ _| |_   _  ___
			//  | | | |/ _ \ |/ _ \ __/ _ \  \ \ / / _` | | | | |/ _ \
			//  | |_| |  __/ |  __/ |_  __/   \ V / (_| | | |_| |  __/
			//  |____/ \___|_|\___|\__\___|    \_/ \__,_|_|\__,_|\___|
			//
			else if (new_toggle.getUserData() == "Delete Value") {
				System.out.println(new_toggle.getUserData());

				for (Path p : currentMachine.getPaths())
					p.setTextFillColor(Color.DARKRED);

				for (int i = 0; i < currentMachine.getComments().size(); i++) {
					TextArea ta = currentMachine.getComments().get(i);
					if (ta.getLength() == 0) {
						addChange(new CommentDelete(ta, currentMachine, this));
						i--;
					}
				}

				currentHandler = event -> {
					if (event.getButton() == MouseButton.PRIMARY
							&& (event.getTarget() instanceof Circle
									|| event.getTarget() instanceof Rectangle
									|| event.getTarget() instanceof Text)) {

						Object Target = ((Node) event.getTarget()).getUserData();

						if (Target instanceof State) {
							addChange(new StateDelete((State) Target, currentMachine, this));
						} else if (Target instanceof Transition) {
							addChange(new TransitionDelete(currentMachine, (Transition) Target, this));
						} else if (event.getTarget() instanceof Rectangle) {
							addChange(new CommentBoxDelete((Rectangle) event.getTarget(), currentMachine, this));
						}

						for (Transition t : currentMachine.getTransitions())
							System.out.printf("%c ; %c ; %c\n", t.getReadChar(), t.getWriteChar(),
									t.getMoveDirection().toString().charAt(0));

						for (Path p : currentMachine.getPaths())
							System.out.println(p.toString());

					}
				};
				editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
			}

			//      _       _     _   _____                    _ _   _
			//     / \   __| | __| | |_   _| __ __ _ _ __  ___(_) |_(_) ___  _ __
			//    / _ \ / _` |/ _` |   | || '__/ _` | '_ \/ __| | __| |/ _ \| '_ \
			//   / ___ \ (_| | (_| |   | || | | (_| | | | \__ \ | |_| | (_) | | | |
			//  /_/   \_\__,_|\__,_|   |_||_|  \__,_|_| |_|___/_|\__|_|\___/|_| |_|
			//
			else if (new_toggle.getUserData() == "Add Transition") {
				System.out.println(new_toggle.getUserData());

				currentHandler = event -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						if (event.getTarget() instanceof Circle || event.getTarget() instanceof Text) {
							Node Target = (Node) event.getTarget();

							if (Target.getUserData() instanceof State) {
								State s = (State) Target.getUserData();
								System.out.printf("State: %s\n", s.getName());

								if (transitionFromState == null) {
									transitionFromState = s;
									transitionFromState.getCircle().setFill(Color.AQUA);
								} else {
									System.out.printf("Create Transition from %s to %s\n",
											transitionFromState.getName(), s.getName());

									s.getCircle().setFill(Color.AQUA);
									Transition t = addTransition(transitionFromState, s);

									if (t == null) {
										transitionFromState.getCircle().setFill(transitionFromState.getBaseColor());
										s.getCircle().setFill(s.getBaseColor());
										transitionFromState = null;

										return;
									}

									// if one is already found, alert user, and return
									for (Transition temp : currentMachine.getTransitions()) {
										if (temp.compareTo(t)) {
											// reset the colors and selections
											s.getCircle().setFill(s.getBaseColor());
											transitionFromState.getCircle().setFill(transitionFromState.getBaseColor());
											transitionFromState = null;

											// alert the user
											Alert alert = new Alert(Alert.AlertType.ERROR);
											alert.setResizable(true);
											alert.setTitle("Transition Exists");
											alert.setContentText("That transition already exists! Please try again.");
											alert.showAndWait();

											return;
										}
									}

									addChange(new TransitionAddition(t, currentMachine, this));

									s.getCircle().setFill(s.getBaseColor());
									transitionFromState.getCircle().setFill(transitionFromState.getBaseColor());
									transitionFromState = null;
								}
							}
						} else {
							if (transitionFromState != null)
								transitionFromState.getCircle().setFill(transitionFromState.getBaseColor());
							transitionFromState = null;
						}

					}

				};
				editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
			} else if (new_toggle.getUserData() == "Edit Transition") {
				System.out.println(new_toggle.getUserData());

				for (Path p : currentMachine.getPaths())
					p.setTextFillColor(Color.DARKGREEN);

				currentHandler = event -> {
					if (event.getButton() == MouseButton.PRIMARY && event.getTarget() instanceof Text) {

						Object Target = ((Node) event.getTarget()).getUserData();

						if (Target instanceof Transition) {
							Transition targetTransition;

							targetTransition = (Transition) Target;

							editTransition(targetTransition);
						}
					}
				};
				editorSpace.addEventHandler(MouseEvent.MOUSE_CLICKED, currentHandler);
			}

			//                _     _    _____                                     _
			//       /\      | |   | |  / ____|                                   | |
			//      /  \   __| | __| | | |     ___  _ __ ___  _ __ ___   ___ _ __ | |_ ___
			//     / /\ \ / _` |/ _` | | |    / _ \| '_ ` _ \| '_ ` _ \ / _ \ '_ \| __/ __|
			//    / ____ \ (_| | (_| | | |___| (_) | | | | | | | | | | |  __/ | | | |_\__ \
			//   /_/    \_\__,_|\__,_|  \_____\___/|_| |_| |_|_| |_| |_|\___|_| |_|\__|___/
			//

			else if (new_toggle.getUserData() == "Add Comment Box") {
				System.out.println(new_toggle.getUserData());

				class RectCoords {
					double x1 = 0;
					double y1 = 0;
					double x2 = 0;
					double y2 = 0;

					ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
					Color randColor;
				}

				final RectCoords c = new RectCoords();

				for (TextArea ta : currentMachine.getComments())
					ta.setEditable(true);

				// Define our new click handler
				pressHandler = event -> {

					if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
						c.rects.add(new Rectangle());
						c.randColor = Color.color(Math.random(), Math.random(), Math.random());
						c.x1 = event.getX();
						c.y1 = event.getY();
					} else {
						TextArea ta = new TextArea();
						ta.setFont(Font.font("Verdana", 20));
						ta.setStyle("-fx-background-color: rgba(255,255,255,0.4)");
						ta.setLayoutX(event.getX());
						ta.setLayoutY(event.getY());
						ta.setPrefColumnCount(15);
						ta.setPrefRowCount(5);
						ta.setPromptText("Enter comment");
						addChange(new CommentAddition(ta, currentMachine, this));
						System.out.printf("Creating textbox at %f,%f\n", event.getX(), event.getY());
					}
				};

				dragHandler = event -> {

					if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
						draggingMouse = true;
						//System.out.println("Checking for drag");
						c.x2 = event.getX();
						c.y2 = event.getY();
						//System.out.printf("x2 = %f, y2 = %f\n", c.x2, c.y2);

						Rectangle r = c.rects.get(c.rects.size() - 1);
						r.setX(c.x1);
						r.setY(c.y1);
						r.setWidth(c.x2 - c.x1);
						r.setHeight(c.y2 - c.y1);
						r.setFill(c.randColor);
						r.setOpacity(.50);
						//r.setUserData("I'm a rectangle.");
						if (!editorSpace.getChildren().contains(r)) {
							editorSpace.getChildren().add(r);
							r.toBack();
						}

						if (!currentMachine.getcBoxes().contains(r)) {
							currentMachine.getcBoxes().add(r);
						}
					}
				};

				releaseHandler = event -> {
					if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
						if (draggingMouse) {
							addChange(new CommentBoxAddition(c.rects.get(c.rects.size() - 1), currentMachine, this));
						}
						draggingMouse = false;
					}
				};
				editorSpace.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				editorSpace.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
				editorSpace.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}
		});

		toggleGroup.selectToggle(toggleGroup.getToggles().get(0));
		toggleGroup.selectToggle(null);
	}

	/* drawStartTriangle: draws the triangle indicating the start state
	 * Parameters:
	 *		s: the start state for the machine
	 * Post-condition: the start triangle is cleared, and the re-drawm
	*/
	public void drawStartTriangle(State s) {
		// remove the original points
		editorSpace.getChildren().remove(startTriangle);
		startTriangle.getPoints().clear();
		if (s == null || s.getCircle() == null) {
			return;
		}

		// add the new points depending on there the state is, and what rotation we're at
		switch (currentMachine.getStartTriRotation()) {
			case START_LEFT:
				startTriangle.getPoints().addAll(
						s.getCircle().getCenterX() - circleRadius - 1, s.getCircle().getCenterY(),
						s.getCircle().getCenterX() - 2 * circleRadius, s.getCircle().getCenterY() - circleRadius,
						s.getCircle().getCenterX() - 2 * circleRadius, s.getCircle().getCenterY() + circleRadius);

				editorSpace.getChildren().addAll(startTriangle);
				break;
			case START_BOTTOM:
				startTriangle.getPoints().addAll(
						s.getCircle().getCenterX(), s.getCircle().getCenterY() + circleRadius - 1,
						s.getCircle().getCenterX() - circleRadius, s.getCircle().getCenterY() + 2 * circleRadius,
						s.getCircle().getCenterX() + circleRadius, s.getCircle().getCenterY() + 2 * circleRadius);

				editorSpace.getChildren().addAll(startTriangle);
				break;
			case START_RIGHT:
				startTriangle.getPoints().addAll(
						s.getCircle().getCenterX() + circleRadius - 1, s.getCircle().getCenterY(),
						s.getCircle().getCenterX() + 2 * circleRadius, s.getCircle().getCenterY() + circleRadius,
						s.getCircle().getCenterX() + 2 * circleRadius, s.getCircle().getCenterY() - circleRadius);

				editorSpace.getChildren().addAll(startTriangle);
				break;
			case START_TOP:
				startTriangle.getPoints().addAll(
						s.getCircle().getCenterX(), s.getCircle().getCenterY() - circleRadius - 1,
						s.getCircle().getCenterX() + circleRadius, s.getCircle().getCenterY() - 2 * circleRadius,
						s.getCircle().getCenterX() - circleRadius, s.getCircle().getCenterY() - 2 * circleRadius);

				editorSpace.getChildren().addAll(startTriangle);
				break;
		}

		// set the colors
		startTriangle.setFill(null);
		startTriangle.setStroke(Color.BLACK);
	}

	//   __  __            _     _                __  __                               _       _   _
	//  |  \/  | __ _  ___| |__ (_)_ __   ___    |  \/  | __ _ _ __  _   _ _ __  _   _| | __ _| |_(_) ___  _ __
	//  | |\/| |/ _` |/ __| '_ \| | '_ \ / _ \   | |\/| |/ _` | '_ \| | | | '_ \| | | | |/ _` | __| |/ _ \| '_ \
	//  | |  | | (_| | (__| | | | | | | |  __/   | |  | | (_| | | | | |_| | |_) | |_| | | (_| | |_| | (_) | | | |
	//  |_|  |_|\__,_|\___|_| |_|_|_| |_|\___|   |_|  |_|\__,_|_| |_|\__,_| .__/ \__,_|_|\__,_|\__|_|\___/|_| |_|
	//

	private Transition addTransition(State from, State to) {
		// This window suspends until Transition editor is done.
		TransitionEditor t = new TransitionEditor(window, from, to);

		// Check if transition is valid is done.
		if (t.createdTransition == null)
			System.out.println("null");
		else
			System.out.printf("Transition: %s -> %s %c %c %s\n", t.createdTransition.getFromState().getName(),
					t.createdTransition.getToState().getName(),
					t.createdTransition.getReadChar(), t.createdTransition.getWriteChar(),
					t.createdTransition.getMoveDirection().toString());

		return t.createdTransition;
	}

	/* deleteTransitionsFromEditor: deletes the transitions that the user deleted from the transition editor
	*/
	private void deleteTransitionsFromEditor(ArrayList<Transition> deleteTransitions) {
		if (deleteTransitions != null) {
			for (Transition t : deleteTransitions) {
				addChange(new TransitionDelete(currentMachine, t, this));
			}
		}
	}

	/*	editTransition: this function opens the second edit transition window.
	*   	It'll open the window and wait until the user is done with whatever changes they are making
	*	Post-condition: once the user closes the window, the machine will be updated with the changes the user made.
	*/
	private void editTransition(Transition transition) {
		// This window suspends until Transition editor is done.
		TransitionEditor t = new TransitionEditor(window, transition.getPath(), this);
		ArrayList<Transition> deletedTrns = t.getDeletedTransition();

		// if the user chose to delete any transitions, go through and delete each one
		if (deletedTrns != null) {
			deleteTransitionsFromEditor(deletedTrns);
		}
		redrawAllPaths();
		for (Path p : currentMachine.getPaths())
			p.setTextFillColor(Color.DARKGREEN);
	}

	private Transition cloneTransition(State from, State to, char read, char write) {
		TransitionEditor t = new TransitionEditor(window, from, to, read, write);
		return t.createdTransition;
	}

	//Need to store the editedTape's value in case they change it
	//using the editTape function
	public ArrayList<Character> editedTape = new ArrayList<>();

	//Current code for resetting the tape
	private void resetTape(Machine currentMachine) {

		//If the tape was preloaded in, we want to reset tape to that value
		//until they use the edit tape button to actually change the tape
		if (editedTape.isEmpty()) {

			currentMachine.getTape().initTape(originalTape);
			currentMachine.getTape().refreshTapeDisplay();
		}

		//If they use the edit tape button, we simply put the
		//original tape onto the display
		else {

			currentMachine.getTape().initTape(editedTape);
			currentMachine.getTape().refreshTapeDisplay();
		}

	}

	//editTape changed to no longer open a seperate window to edit the tape; it just changes the value outright now
	private void editTape(Stage window, Machine currentMachine, String r) {

		ArrayList<Character> characters = new ArrayList<>();
		for (Character c : r.toCharArray()) {
			if (c >= 32 && c < 126) {
				characters.add(c);
			} else {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setResizable(true);
				alert.setTitle("Invalid character(s)");
				alert.setContentText("You input invalid character(s) in your tape.");
				alert.initOwner(window);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.showAndWait();
				r = "";
				editTape(window, currentMachine, r);
				return;
			}
		}

		//Need to store this character string for my
		//Reset tape function
		editedTape = characters;
		currentMachine.getTape().initTape(characters);
		currentMachine.getTape().refreshTapeDisplay();

	}

	private void runMachine(SplitMenuButton thisButton, Node... args) {
		currentMachine.getTape().centerTapeDisplay();
		toggleGroup.selectToggle(null);
		for (Node b : args)
			b.setDisable(true);

		Tester tester = new Tester();

		if (currentMachine.getTape().getSize() < 0) {
			currentMachine.getTape().initTape(new ArrayList<>(' '));
		}

		if (currentMachine.getSpeed() == -1) {
			ObjectExpression<Font> textTrack = Bindings.createObjectBinding(
					() -> Font.font(Math.min(editorSpace.getWidth() / 55, 20)), editorSpace.widthProperty());

			Text t = new Text("<Right Arrow> Advance one state  <Left Arrow> Back one state  <Esc> Stop Machine");
			t.xProperty().bind(editorSpace.widthProperty().divide(10));
			t.yProperty().bind(editorSpace.heightProperty());
			t.fontProperty().bind(textTrack);
			editorSpace.getChildren().add(t);

			ArrayList<MachineStep> machineSteps = new ArrayList<>();

			//currentMachine.getTape().centerTapeDisplay();
			currentMachine.getTape().refreshTapeDisplay();

			EventHandler<KeyEvent> keyPress = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent keyEvent) {
					System.out.println(keyEvent.getCode());
					if (keyEvent.getCode() == KeyCode.ESCAPE) {
						thisButton.fire();
						System.out.println("ESC");

						keyEvent.consume();
					} else if (keyEvent.getCode() == KeyCode.RIGHT) {
						State currentState;
						System.out.println("ESC");
						if (machineSteps.size() == 0) {
							currentState = currentMachine.getStartState();
						} else {
							System.out.println(machineSteps.get(machineSteps.size() - 1).getTransition().toString());
							currentState = machineSteps.get(machineSteps.size() - 1).getTransition().getToState();
						}

						System.out.printf("Current State = %s\n", currentState.getName());
						Transition next = tester.nextTransition(currentState, currentMachine.getTape());

						if (next == null) {
							Alert alert = new Alert(Alert.AlertType.ERROR);
							alert.setResizable(true);
							alert.initOwner(window);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.setTitle("The machine has finished");

							if (currentState.isAccept()) {
								alert.setGraphic(
										new ImageView(this.getClass().getResource("/checkmark.png").toString()));
								alert.setHeaderText(String.format(
										"The machine has finished successfully at State %s.\n%d transition(s) completed.",
										tester.getFinalState().getName(), tester.getTransitionCounter()));
								thisButton.fire();
							} else {
								alert.setHeaderText(String.format(
										"The machine has finished unsuccessfully at State %s.\n%d transition(s) completed.",
										tester.getFinalState().getName(), tester.getTransitionCounter()));
								alert.setContentText(tester.getFailReason());
								thisButton.fire();
							}

							//currentMachine.getTape().centerTapeDisplay();
							currentMachine.getTape().refreshTapeDisplay();

							alert.showAndWait();
							keyEvent.consume();
							return;
						}

						System.out.printf("Next = %c %c %s\n", next.getReadChar(), next.getWriteChar(),
								next.getMoveDirection().toString());
						machineSteps.add(new MachineStep(next, currentMachine.getTape().currentTapeVal()));

						next.getFromState().getCircle().setFill(next.getFromState().getBaseColor());
						next.getToState().getCircle().setFill(Color.GREENYELLOW);

						if (next.getWriteChar() != '~') {
							try {
								currentMachine.getTape().setTape(next.getWriteChar());
							} catch (Exception e) {
								showException(e);
							}
						}

						switch (machineSteps.get(machineSteps.size() - 1).getTransition().getMoveDirection()) {
							case LEFT:
								currentMachine.getTape().left();
								break;
							case RIGHT:
								currentMachine.getTape().right();
								break;
							case STAY:
								break;
						}

						//currentMachine.getTape().centerTapeDisplay();
						currentMachine.getTape().refreshTapeDisplay();

						keyEvent.consume();
					} else if (keyEvent.getCode() == KeyCode.LEFT) {
						System.out.println("Left");
						if (machineSteps.size() == 0) {
							keyEvent.consume();
							return;
						}

						MachineStep lastStep = machineSteps.get(machineSteps.size() - 1);
						System.out.printf("Next = %c %c %s\n", lastStep.getTransition().getReadChar(),
								lastStep.getTransition().getWriteChar(),
								lastStep.getTransition().getMoveDirection().toString());

						lastStep.getTransition().getToState().getCircle()
								.setFill(lastStep.getTransition().getToState().getBaseColor());

						switch (lastStep.getTransition().getMoveDirection()) {
							case LEFT:
								currentMachine.getTape().right();
								break;
							case RIGHT:
								currentMachine.getTape().left();
								break;
							case STAY:
								break;
						}

						try {
							currentMachine.getTape().setTape(lastStep.getChar());
						} catch (Exception e) {
							showException(e);
						}

						lastStep.getTransition().getFromState().getCircle().setFill(Color.GREENYELLOW);

						//currentMachine.getTape().centerTapeDisplay();
						currentMachine.getTape().refreshTapeDisplay();

						machineSteps.remove(machineSteps.size() - 1);

						keyEvent.consume();
					}
					t.requestFocus();
				}
			};

			thisButton.setText("Stop Machine");
			thisButton.setOnAction(event -> {
				editorSpace.getChildren().remove(t);

				currentMachine.getTape().refreshTapeDisplay();

				for (State s : currentMachine.getStates())
					s.getCircle().setFill(s.getBaseColor());

				for (Node b : args)
					b.setDisable(false);

				machineSteps.clear();
				editorSpace.removeEventHandler(KeyEvent.KEY_PRESSED, keyPress);

				System.out.println(machineSteps.size());
				thisButton.setText("Run Machine (X)");
				thisButton.setOnAction(event1 -> runMachine(thisButton, args));

			});

			editorSpace.addEventHandler(KeyEvent.KEY_PRESSED, keyPress);
			currentMachine.getStartState().getCircle().setFill(Color.GREENYELLOW);
			t.requestFocus();
		} else {
			String speedText = currentMachine.getSpeedString();
			ObjectExpression<Font> textTrack = Bindings.createObjectBinding(
					() -> Font.font(Math.min(editorSpace.getWidth() / 55, 20)), editorSpace.widthProperty());
			Text t = new Text(
					"<Up Arrow> Increase Speed  <Down Arrow> Decrease Speed  <Esc> Stop Machine \tCurrent Speed: "
							+ speedText);
			t.xProperty().bind(editorSpace.widthProperty().divide(10));
			t.yProperty().bind(editorSpace.heightProperty());
			t.fontProperty().bind(textTrack);
			editorSpace.getChildren().add(t);

			Task<Void> task = new Task<Void>() {

				@Override
				public Void call() {
					try {
						trackerState = currentMachine.getStartState();
						if (trackerState != null) {

							trackerState = tester.runMachine(currentMachine, trackerState);

							while (trackerState.isDebug()) {
								trackerState.getCircle().setFill(Color.GREENYELLOW);
								final CountDownLatch waitForInput = new CountDownLatch(1);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {

										Alert debugLog = new Alert(Alert.AlertType.ERROR);
										debugLog.setResizable(true);
										debugLog.initOwner(window);
										debugLog.initModality(Modality.APPLICATION_MODAL);
										ButtonType moreDebug = new ButtonType("Continue with breakpoint set");
										ButtonType lessDebug = new ButtonType("Continue and disable breakpoint");
										ButtonType Cancel = new ButtonType("Stop");

										debugLog.setHeaderText("Breakpoint hit");
										debugLog.getButtonTypes().setAll(moreDebug, lessDebug, Cancel);
										Optional<ButtonType> method = debugLog.showAndWait();
										if (method.get() == moreDebug) {
											trackerState.setDebug(true);
											trackerState.getCircle().setStroke(Color.RED);
											tester.setCont(true);
										} else if (method.get() == lessDebug) {
											trackerState.setDebug(false);
											trackerState.getCircle().setStroke(Color.BLACK);
											tester.setCont(true);
										} else {
											trackerState.getCircle().setFill(trackerState.getBaseColor());
											tester.setCont(false);
										}

										waitForInput.countDown();
									}
								});
								waitForInput.await();

								if (!(tester.isCont())) {
									break;
								} else {
									trackerState = tester.runMachine(currentMachine, trackerState);
								}
							}
						} else {
							tester.setFailReason("Machine has no start state!");
						}
					} catch (Exception e) {
						if (!(e instanceof InterruptedException)) {
							showException(e);
						}
					}
					return null;
				}
			};

			EventHandler<KeyEvent> keyPress = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent keyEvent) {
					if (keyEvent.getCode() == KeyCode.ESCAPE) {
						thisButton.fire();
						System.out.println("ESC");
						editorSpace.getChildren().remove(t);
						task.cancel();
						keyEvent.consume();
					} else if (keyEvent.getCode() == KeyCode.UP) {
						int nextSpeed = currentMachine.getSpeed() == 500 ? 250
								: currentMachine.getSpeed() == 250 ? 75
										: currentMachine.getSpeed() == 75 ? 0 : currentMachine.getSpeed();
						currentMachine.setSpeed(nextSpeed);
						tester.setCurSpeed(currentMachine.getSpeed());
						System.out.println("Speeding up");

						String speedText = currentMachine.getSpeedString();
						t.setText(
								"<Up Arrow> Increase Speed  <Down Arrow> Decrease Speed  <Esc> Stop Machine \tCurrent Speed: "
										+ speedText);
					} else if (keyEvent.getCode() == KeyCode.DOWN) {
						int nextSpeed = currentMachine.getSpeed() == 0 ? 75
								: currentMachine.getSpeed() == 75 ? 250
										: currentMachine.getSpeed() == 250 ? 500 : currentMachine.getSpeed();
						currentMachine.setSpeed(nextSpeed);
						tester.setCurSpeed(currentMachine.getSpeed());
						System.out.println("Slowing down");

						String speedText = currentMachine.getSpeedString();
						t.setText(
								"<Up Arrow> Increase Speed  <Down Arrow> Decrease Speed  <Esc> Stop Machine \tCurrent Speed: "
										+ speedText);
					}
					t.requestFocus();
				}
			};

			task.setOnSucceeded(event -> {
				currentMachine.getTape().refreshTapeDisplay();

				Alert alert = new Alert(
						Alert.AlertType.ERROR);
				alert.setResizable(true);
				alert.initOwner(window);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle("The machine has finished");

				if (tester.didSucceed()) {
					alert.setGraphic(new ImageView(this.getClass().getResource("/checkmark.png").toString()));
					alert.setHeaderText(String.format(
							"The machine has finished successfully at State %s.\n%d transition(s) completed.",
							tester.getFinalState().getName(), tester.getTransitionCounter()));
				} else {
					alert.setHeaderText(String.format(
							"The machine has finished unsuccessfully at State %s.\n%d transition(s) completed.",
							tester.getFinalState().getName(), tester.getTransitionCounter()));
					alert.setContentText(tester.getFailReason());
				}

				alert.showAndWait();

				thisButton.setText("Run Machine");
				thisButton.setOnAction(event1 -> runMachine(thisButton, args));

				for (Node b : args)
					b.setDisable(false);

				window.removeEventHandler(KeyEvent.KEY_RELEASED, keyPress);
				task.cancel();
				tester.setCont(false);
				editorSpace.getChildren().remove(t);
			});
			task.setOnCancelled(event -> {
				currentMachine.getTape().refreshTapeDisplay();

				for (State s : currentMachine.getStates())
					s.getCircle().setFill(s.getBaseColor());

				for (Node b : args)
					b.setDisable(false);

				window.removeEventHandler(KeyEvent.KEY_RELEASED, keyPress);
				task.cancel();

				thisButton.setText("Run Machine");
				thisButton.setOnAction(event1 ->

				runMachine(thisButton, args));
				tester.setCont(false);
				editorSpace.getChildren().remove(t);
			});

			thisButton.setText("Stop Machine");
			thisButton.setOnAction(event -> {

				currentMachine.getTape().refreshTapeDisplay();

				for (

				State s : currentMachine.getStates())
					s.getCircle().setFill(s.getBaseColor());

				for (Node b : args)
					b.setDisable(false);

				window.removeEventHandler(KeyEvent.KEY_RELEASED, keyPress);
				task.cancel();
				tester.setCont(false);
				editorSpace.getChildren().remove(t);
			});

			new Thread(task).start();
			window.addEventHandler(KeyEvent.KEY_RELEASED, keyPress);
			t.requestFocus();
		}
	}

	//   ____          _                      __  __      _   _               _
	//  |  _ \ ___  __| |_ __ __ ___      __ |  \/  | ___| |_| |__   ___   __| |___
	//  | |_) / _ \/ _` | '__/ _` \ \ /\ / / | |\/| |/ _ \ __| '_ \ / _ \ / _` / __|
	//  |  _ <  __/ (_| | | | (_| |\ V  V /  | |  | |  __/ |_| | | | (_) | (_| \__ \
	//  |_| \_\___|\__,_|_|  \__,_| \_/\_/   |_|  |_|\___|\__|_| |_|\___/ \__,_|___/
	//
	private void redrawAllStates() {
		for (State s : currentMachine.getStates())
			redrawState(s);
	}

	/* mouseReleased EventHandler: The is the event handler for when a state is released (or the circle/text of the text)
	*		This function will update the the state one more time, and then also move the start triangle if needed.
	* Post-condition: state is entirely updated
	* NOTE: this function expects for only a State's circle or text to be linked to it
	*/
	EventHandler<MouseEvent> stateReleased = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			State state = null;
			Text label = null;
			Circle circle = null;

			// get the source of the click. Will always be a Text or a Circle
			if (e.getSource() instanceof Text) {
				label = (Text) (e.getSource());
			} else {
				circle = (Circle) (e.getSource());
			}

			// find which state was pressed
			for (State i : currentMachine.getStates()) {
				if (circle == i.getCircle() || label == i.getLabel()) {
					state = i;
				}
			}
			circle = state.getCircle();
			label = state.getLabel();

			// get the transitions
			ArrayList<Transition> transitions = new ArrayList<>();
			transitions.addAll(state.getTransition());
			for (Transition transition : currentMachine.getTransitions()) {
				if (transition.getToState() == state && transition.getToState() != transition.getFromState()) {
					//System.out.printf("Adding Transiton %s -> %s, %c ; %c ; %c\n", t.getFromState().getName(), t.getToState().getName(),
					//		t.getReadChar(), t.getWriteChar(), t.getMoveDirection().toString().charAt(0));
					transitions.add(transition);
				}
			}
			redrawPaths(transitions);

			// if the state is a start state, redraw it
			if (state.isStart()) {
				drawStartTriangle(state);
			}

			editorSpace.getChildren().remove(state.getSelctedCircle());
			state.setSelctedCircle(null);
			state.setSelected(false);
			if (state.getX() != oldStateX && state.getY() != oldStateY) {
				addChange(new StateMove(currentMachine, state, oldStateX, oldStateY, thisEditor));
			}
			scrollPane.setPannable(true);
		}
	};

	/* stateClicked EventHandler: This is the event handler for if a state is pressed (or the circle/text of the state.)
	 * 		This function just stores the where the mouse was currently clicked
	 * NOTE: this function expects for only a State's circle or text to be linked to it
	*/
	EventHandler<MouseEvent> stateClicked = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			scrollPane.setPannable(false);

			State state = null;
			Text label = null;
			Circle circle = null;

			// get the source of the click. Will always be a Text or a Circle
			if (e.getSource() instanceof Text) {
				label = (Text) (e.getSource());
			} else {
				circle = (Circle) (e.getSource());
			}

			// find which state it is
			for (State i : currentMachine.getStates()) {
				if (circle == i.getCircle() || label == i.getLabel()) {
					state = i;
				}
			}
			oldStateX = state.getX();
			oldStateY = state.getY();
			prevStateX = e.getSceneX();
			prevStateY = e.getSceneY();

			// add the selected circle around it
			Circle selectedCircle = new Circle(state.getCircle().getCenterX(), state.getCircle().getCenterY(),
					circleRadius * 1.5, null);
			selectedCircle.setStrokeWidth(2);
			selectedCircle.setStroke(Color.BLUE);
			state.setSelected(true);
			state.setSelctedCircle(selectedCircle);
			editorSpace.getChildren().add(selectedCircle);
		}
	};

	/* stateDragged EventHandler: This is the event handler for if a state is dragged (or the circle/text of the state)
	*		This function will calculate the changes, and update the circle, text, transitions, and the accept circle if needed
	*		It also only works if the primary button is clicked on the mouse.
	*		It will also only work if the State is being dragged within the bounds of the window
	* Post-condition: the state's position has been updated
	* NOTE: this function expects for only a State's circle or text to be linked to it
	*/
	EventHandler<MouseEvent> stateDragged = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			// only drag on primary button being pressed, otherwise consume
			if (e.isPrimaryButtonDown()) {
				State s = null;
				Text l = null;
				Circle c = null;
				Circle selectCircle = null;

				// get the source of the click. Will always be a Text or a Circle
				if (e.getSource() instanceof Text) {
					l = (Text) (e.getSource());
				} else {
					c = (Circle) (e.getSource());
				}

				// find which state it is
				for (State i : currentMachine.getStates()) {
					if (c == i.getCircle() || l == i.getLabel()) {
						s = i;
					}
				}

				c = s.getCircle();
				l = s.getLabel();
				selectCircle = s.getSelctedCircle();

				double offsetX = e.getSceneX() - prevStateX;
				double offsetY = e.getSceneY() - prevStateY;

				double newX = c.getCenterX() + offsetX / scrollPane.scaleValue;
				double newY = c.getCenterY() + offsetY / scrollPane.scaleValue;
				if ((newX > circleRadius) && newX < (editorSpace.getPrefWidth() - circleRadius) && (newY > circleRadius)
						&& (newY < editorSpace.getPrefHeight() - (110 + circleRadius))) {

					// set the coordinates for the circle
					c.setCenterX(newX);
					c.setCenterY(newY);

					selectCircle.setCenterX(newX);
					selectCircle.setCenterY(newY);

					// set the state's x/y coordinates
					s.setX(newX);
					s.setY(newY);

					l.setX(newX - (l.getLayoutBounds().getWidth() / 2));
					l.setY(newY + (l.getLayoutBounds().getHeight() / 4));

					prevStateX = e.getSceneX();
					prevStateY = e.getSceneY();

					// update the accept circle if needed
					if (s.isAccept()) {
						s.getAcceptCircle().setCenterX(newX);
						s.getAcceptCircle().setCenterY(newY);
					}

					// if the state is a start state, redraw it
					if (s.isStart()) {
						drawStartTriangle(s);
					}

					// update transitions
					ArrayList<Transition> tl = new ArrayList<>();
					tl.addAll(s.getTransition());
					for (Transition t : currentMachine.getTransitions()) {
						if (t.getToState() == s && t.getToState() != t.getFromState()) {
							//System.out.printf("Adding Transiton %s -> %s, %c ; %c ; %c\n", t.getFromState().getName(), t.getToState().getName(),
							//		t.getReadChar(), t.getWriteChar(), t.getMoveDirection().toString().charAt(0));
							tl.add(t);
						}
					}
					redrawPaths(tl);
				}
			} else {
				e.consume();
			}

		}
	};

	private void redrawState(State s) {
		editorSpace.getChildren().removeAll(s.getCircle(), s.getLabel());
		if (s.getAcceptCircle() != null)
			editorSpace.getChildren().remove(s.getAcceptCircle());

		Circle c = new Circle(s.getX(), s.getY(), circleRadius, s.getBaseColor());
		c.setId(s.getName());
		c.setStrokeWidth(2);
		c.setStroke(Color.BLACK);
		s.setCircle(c);
		Text t = new Text(s.getName());
		t.setId(s.getName());
		t.setX(c.getCenterX() - (t.getLayoutBounds().getWidth() / 2));
		t.setY(c.getCenterY() + (t.getLayoutBounds().getHeight() / 4));
		s.setLabel(t);
		// Set Create State and add it to the Node's user data
		// so it is easy to find if clicked on
		c.setUserData(s);
		t.setUserData(s);

		c.setOnContextMenuRequested(event1 -> contextMenu.show(c, event1.getScreenX(), event1.getScreenY()));
		t.setOnContextMenuRequested(event2 -> contextMenu.show(t, event2.getScreenX(), event2.getScreenY()));

		editorSpace.getChildren().addAll(s.getCircle(), s.getLabel());

		if (s.isAccept()) {
			Circle ca = new Circle(s.getCircle().getCenterX(), s.getCircle().getCenterY(), circleRadius * 1.25, null);
			ca.setStrokeWidth(2);
			ca.setStroke(Color.BLACK);

			s.setAcceptCircle(ca);
			editorSpace.getChildren().add(s.getAcceptCircle());
		}

		if (s.isStart() || currentMachine.getStartState() == s) {
			drawStartTriangle(s);
		}

		s.getCircle().setOnMousePressed(stateClicked);
		s.getCircle().setOnMouseDragged(stateDragged);
		s.getCircle().setOnMouseReleased(stateReleased);

		s.getLabel().setOnMousePressed(stateClicked);
		s.getLabel().setOnMouseDragged(stateDragged);
		s.getLabel().setOnMouseReleased(stateReleased);

	}

	// TODO: Create better redraw method in path class so we don't have to delete it
	public void redrawPath(Transition t) {
		if (t.getPath() != null) {
			currentMachine.getPaths().remove(t.getPath());
			// System.out.println("Delete" + t.getPath());
			editorSpace.getChildren().removeAll(t.getPath().getAllNodes());

			t.getPath().getTransitions().forEach(transition -> transition.setPath(null));
		}
		Path path = null;
		for (Path p : currentMachine.getPaths()) {
			if (p.compareTo(t.getFromState(), t.getToState())) {
				path = p;
				break;
			}
		}
		if (path == null) {
			path = new Path(t.getFromState(), t.getToState());
			currentMachine.getPaths().add(path);
		}

		t.setPath(path);
		ArrayList<Node> nodes = path.addTransition(t);
		editorSpace.getChildren().addAll(nodes);

		for (Node n : nodes)
			if (n instanceof Line || n instanceof CubicCurve)
				n.toBack();
	}

	public void redrawPaths(ArrayList<Transition> tl) {
		for (Transition transition : tl) {
			redrawPath(transition);
		}
	}

	private void redrawAllPaths() {
		for (Transition t : currentMachine.getTransitions()) {
			redrawPath(t);
		}
	}

	private void redrawAllComments() {
		for (TextArea ta : currentMachine.getComments())
			redrawComment(ta);

		for (Rectangle r : currentMachine.getcBoxes())
			redrawcBox(r);
	}

	private void redrawComment(TextArea ta) {
		editorSpace.getChildren().add(ta);
	}

	private void redrawcBox(Rectangle r) {
		editorSpace.getChildren().add(r);
		r.toBack();
	}

	public void showException(Exception e) {
		System.out.println(e);
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setResizable(true);
		alert.initOwner(window);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setTitle("An Exception has Occurred!");
		alert.setHeaderText(e.toString());
		// TODO: Better send us message
		alert.setContentText("Oops...");

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		String exceptionText = stringWriter.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

	//   __  __       _   _       _____                 _   _
	//  |  \/  | __ _| |_| |__   |  ___|   _ _ __   ___| |_(_) ___  _ __  ___
	//  | |\/| |/ _` | __| '_ \  | |_ | | | | '_ \ / __| __| |/ _ \| '_ \/ __|
	//  | |  | | (_| | |_| | | | |  _|| |_| | | | | (__| |_| | (_) | | | \__ \
	//  |_|  |_|\__,_|\__|_| |_| |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|___/
	//
	private double calcDist(MouseEvent event, Machine currentMachine) {
		double min = Double.MAX_VALUE;
		if (!(currentMachine.getStates().isEmpty())) {
			for (State state : currentMachine.getStates()) {
				double dist = distForm(event.getX(), state.getCircle().getCenterX(),
						event.getY(), state.getCircle().getCenterY());
				if (min > dist) {
					min = dist;
				}
			}
		}
		return min;
	}

	private double distForm(double x1, double x2, double y1, double y2) {
		return Math.hypot(x2 - x1, y2 - y1);
	}

	// 	    ____     _   _                
	// 	  / ___| ___| |_| |_ ___ _ __ ___ 
	//   | |  _ / _ \ __| __/ _ \ '__/ __|
	//  | |_| |  __/ |_| ||  __/ |  \__ \
	// 	\____|\___|\__|\__\___|_|  |___/
	//
	public Pane getEditorSpace() {
		return this.editorSpace;
	}

	public int getStateNextVal() {
		return this.stateNextVal;
	}

	public int getCircleRadius() {
		return this.circleRadius;
	}

	public Polygon getStartTriangle() {
		return this.startTriangle;
	}

	public ArrayList<Integer> getDeletedValues() {
		return this.deletedValues;
	}

	public Rectangle getSelectedArea() {
		return this.selectedArea;
	}

	public ArrayList<State> getSelectedStates() {
		return this.selectedStates;
	}

	public Shape getChoosingBox() {
		return this.choosingBox;
	}

	//     ____       _   _                
	//   / ___|  ___| |_| |_ ___ _ __ ___ 
	//  \___ \ / _ \ __| __/ _ \ '__/ __|
	//  ___) |  __/ |_| ||  __/ |  \__ \
	// |____/ \___|\__|\__\___|_|  |___/
	//
	public void setStateNextVal(int value) {
		this.stateNextVal = value;
	}

	void setCircleRadius(int size) {
		circleRadius = size;
	}

	public void setChoosingBox(Shape value) {
		choosingBox = value;
	}

}
