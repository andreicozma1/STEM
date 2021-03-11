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

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

public class HelpMenu {
	private Stage window;
	private Scene menu;
	private VBox layout;
	private CloseButton closebutton;
	
	public HelpMenu(Scene prev){
		window = new Stage();
		window.setTitle("Help");
		layout = new VBox(20);

		WebView =

		closebutton = new CloseButton();
		closebutton.setCloseButton(window);

		ScrollPane scrollPane = new ScrollPane();
		Label label = new Label("Hello world");
		for (int i =0 ; i <30; i++) {
			Label l = new Label("Hello world");
			layout.getChildren().addAll(l);
		}

//		Button backButton = new Button("Back");
//		backButton.setOnAction(e->window.setScene(prev));
		
		layout.getChildren().addAll(label, closebutton.getCloseButton());
		scrollPane.setContent(layout);
		menu = new Scene(scrollPane, 300, 300);
		window.setScene(menu);
		window.show();
	}
}
