
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

import Editor.Editor;
import Editor.HelpMenu;
import Editor.Styles;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage window;
	private Scene menu;
	private HelpMenu help;

	/* Launch the app */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;

		initMenu();
		window.setScene(menu);
		window.getIcons().add(new Image("tmicon.png"));
		window.setTitle("STEM");
		window.show();
	}

	private void initMenu() {

		BorderPane menuLayout = new BorderPane(); //outer Borderpane to hold menubar
		menu = new Scene(menuLayout);

		Image background = new Image("background.png");
		BackgroundSize bSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
		menuLayout.setBackground(new Background(new BackgroundImage(background, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bSize)));

		window.setWidth(800);
		window.setHeight(600);

		window.setResizable(false);

		/* Contents of page. */
		VBox buttonLayout = new VBox(9); //inner VBox to hold buttons
		buttonLayout.setAlignment(Pos.CENTER_LEFT);
		buttonLayout.setPadding(new Insets(20, 50, 20, 50));
		buttonLayout.prefWidthProperty().bind(menuLayout.widthProperty());

		Label title = new Label("Simple Turing\nMachine Simulator");
		title.setFont(Font.font(null, FontWeight.BOLD, 30));
		title.setStyle(Styles.IDLE_BUTTON_STYLE);
		title.setPadding(new Insets(8, 20, 40, 20));

		Button newMachineButton = makeBtn("New Machine");
		newMachineButton.requestFocus();

		Button loadMachineButton = makeBtn("Load Machine");
		Button helpButton = makeBtn("Help");
		Button quitButton = makeBtn("Quit");

		Region spacer = new Region();
		spacer.setPrefHeight(90);
		/* Set layout. */
		buttonLayout.getChildren().addAll(title, newMachineButton, loadMachineButton, helpButton, quitButton, spacer); //, closebutton.getCloseButton());
		menuLayout.setCenter(buttonLayout);

		/* After menu is set up, create other scenes. */
		helpButton.setOnAction(e -> {
			new HelpMenu();
		});

		newMachineButton.setOnAction(e -> {
			new Editor(false);
		});

		loadMachineButton.setOnAction(e -> {
			new Editor(true);
		});

		quitButton.setOnAction(e -> {
			window.close();
		});
	}

	static Button makeBtn(String title) {
		Button btn = new Button(title);
		btn.setPadding(new Insets(8, 20, 8, 20));
		btn.setFont(Font.font(null, FontWeight.BOLD, 20));
		btn.setStyle(Styles.IDLE_BUTTON_STYLE);
		btn.setOnMouseEntered(e -> btn.setStyle(Styles.HOVERED_BUTTON_STYLE));
		btn.setOnMouseExited(e -> btn.setStyle(Styles.IDLE_BUTTON_STYLE));
		return btn;
	}

}
