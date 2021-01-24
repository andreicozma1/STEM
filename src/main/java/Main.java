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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage window;
	private Scene menu;
	private HelpMenu help;
	private Editor editor;

	/* Launch the app */
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		window = primaryStage;

		initMenu();
		window.setScene(menu);
		window.setTitle("STEM");
		window.show();
	}
	
	private void initMenu(){

		BorderPane menuLayout = new BorderPane(); 				//outer Borderpane to hold menubar
		menu = new Scene(menuLayout);

		Image background = new Image("turingback.jpg");
		BackgroundSize bSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
		menuLayout.setBackground(new Background(new BackgroundImage(background, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bSize)));

		window.setWidth(background.getWidth());
		window.setHeight(background.getHeight());

		window.setResizable(false);

		/* Contents of page. */
		VBox buttonLayout = new VBox(10); 				//inner VBox to hold buttons
		buttonLayout.setAlignment(Pos.CENTER_LEFT);
		buttonLayout.setPadding(new Insets(20, 50, 20, 50));
		buttonLayout.prefWidthProperty().bind(menuLayout.widthProperty());

		Button newMachineButton = Styles.makeBtn("New Machine");
		newMachineButton.requestFocus();

		Button loadMachineButton = Styles.makeBtn("Load Machine");
		Button optionsButton = Styles.makeBtn("Options");
		Button helpButton = Styles.makeBtn("Help");
		Button quitButton = Styles.makeBtn("Quit");

		/* Set layout. */
		buttonLayout.getChildren().addAll(newMachineButton, loadMachineButton, optionsButton, helpButton, quitButton); //, closebutton.getCloseButton());
		menuLayout.setCenter(buttonLayout);

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

		optionsButton.setOnAction(e-> {
			OptionsMenu options = new OptionsMenu();
		});

		quitButton.setOnAction(e-> {
			window.close();
		});
	}


}
