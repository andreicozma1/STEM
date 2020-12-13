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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelpMenu {
	private Scene menu;
	private CloseButton closebutton;
	
	public HelpMenu(Stage window, Scene prev){
		closebutton = new CloseButton();
		closebutton.setCloseButton(window);
		
		Label label = new Label("Hello world");
//		label.setStyle("-fx-text-fill: #839496");
		
		Button backButton = new Button("Back");
		backButton.setOnAction(e->window.setScene(prev));
		
		VBox layout = new VBox(20);
		layout.getChildren().addAll(label, backButton, closebutton.getCloseButton());
//		layout.setStyle("-fx-background-color: #002b36");
		menu = new Scene(layout, 300, 300);
	}
	
	public void setMenu(Stage window){
		window.setTitle("Help");
		window.setScene(menu);
	}
}
