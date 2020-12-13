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

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage window;
	private Scene menu;
	private CloseButton closebutton;
	private HelpMenu help;
	private Editor editor;
	
	/* Launch the app */
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		window = primaryStage;
		window.setMinWidth(300);
		window.setMinHeight(400);
		initMenu();
		window.setScene(menu);
		window.setTitle("STEM");
		window.show();

	}
	
	private void initMenu(){
		/* File menu option. */
		Menu fileMenu = new Menu("File");
		fileMenu.setStyle("-fx-font-size: 10px;");
		fileMenu.getItems().add(new MenuItem("Close"));
		
		/* Help menu option */
		Menu helpMenu = new Menu("Help");
		helpMenu.setStyle("-fx-font-size: 10px;");
		helpMenu.getItems().add(new MenuItem("About"));
		MenuBar menuBar = new MenuBar();
		
		/* Add menu options */
		menuBar.getMenus().addAll(fileMenu, helpMenu);
		menuBar.setStyle("-fx-background-color: #dae4e3");
		
		/* Contents of page. */
		VBox buttonLayout = new VBox(20); 				//inner VBox to hold buttons
		buttonLayout.setPadding(new Insets(0, 20, 20, 20));

		ObjectExpression<Font> fontTrack = Bindings.createObjectBinding(
				() -> Font.font(buttonLayout.getWidth() / 25), buttonLayout.widthProperty());

		Label label0 = new Label("Welcome to the Simple Turing machine EMulator!");
		Label label1 = new Label("To begin, create a new machine.");

		label0.fontProperty().bind(fontTrack);
		label1.fontProperty().bind(fontTrack);

		Button newMachineButton = new Button("New Machine");
		newMachineButton.prefWidthProperty().bind(buttonLayout.widthProperty());
		newMachineButton.prefHeightProperty().bind(buttonLayout.heightProperty());
		newMachineButton.fontProperty().bind(fontTrack);
		newMachineButton.requestFocus();

		Button loadMachineButton = new Button("Load Machine");
		loadMachineButton.prefWidthProperty().bind(buttonLayout.widthProperty());
		loadMachineButton.prefHeightProperty().bind(buttonLayout.heightProperty());
		loadMachineButton.fontProperty().bind(fontTrack);

		Button helpButton = new Button("Help");
		helpButton.prefWidthProperty().bind(buttonLayout.widthProperty());
		helpButton.prefHeightProperty().bind(buttonLayout.heightProperty());
		helpButton.fontProperty().bind(fontTrack);

		/* Set layout. */
		BorderPane menuLayout = new BorderPane(); 				//outer Borderpane to hold menubar
		menuLayout.setTop(menuBar);

		//Delete this once help menu is better
		helpButton.setDisable(true);

		buttonLayout.getChildren().addAll(label0, label1, newMachineButton, loadMachineButton, helpButton); //, closebutton.getCloseButton());

		menuLayout.setCenter(buttonLayout);
		menu = new Scene(menuLayout, 300, 400);
		
		/* After menu is set up, create other scenes. */
		help = new HelpMenu(window, menu);
		helpButton.setOnAction(e-> help.setMenu(window));
		
		newMachineButton.setOnAction(e-> {
			editor = new Editor(window, menu);
			editor.setMenu(window);
			editor.newMachine(window, menu);
			editor = null;
		});

		loadMachineButton.setOnAction(e-> {
			editor = new Editor(window, menu);
			editor.setMenu(window);
			editor.loadMachine(window, menu);
			editor = null;
		});

	}
}
