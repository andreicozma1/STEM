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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HelpMenu {

	public HelpMenu(){
		Scene menu;
		Stage window = new Stage();
		window.setTitle("Help");

		// load the fxml content into the Scene
		try {
			URL xml_url = new URL(this.getClass().getResource("fxml/helpmenu.fxml").toExternalForm());
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(xml_url);
			menu = loader.load();
		}catch(MalformedURLException e){
			System.out.println("Could not find fxml file");
			return;
		} catch(IOException e) {
			System.out.println("Could not load help menu content");
			return;
		}
		window.setResizable(false);
		window.setScene(menu);
		window.show();
	}
}
