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

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmAlert {
	public static void confirmAlert(String title, String message){
		Stage confirmMenu = new Stage();
		
		//handle this window before moving on
		confirmMenu.initModality(Modality.APPLICATION_MODAL);
		confirmMenu.setTitle("Confirmation");
		confirmMenu.setMinWidth(100);
		
		Label label = new Label("Are you sure you want to do this?");
		
		Button yes = new Button("Yes");
		
		Button no = new Button("No");
		
		VBox layout = new VBox(10);
		layout.getChildren().addAll(label, yes, no);
		layout.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(layout);
		confirmMenu.setScene(scene);
		confirmMenu.showAndWait();
	}
}
