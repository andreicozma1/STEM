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

import javafx.scene.control.Button;
import javafx.stage.Stage;

public class CloseButton {
	private Button button;
	
	public CloseButton(){
		button = new Button("Close");
	}
	
	public Button getCloseButton(){
		return button;
	}
	
	public void setCloseButton(Stage window){
		button.setOnAction(e->window.close());
	}
}
