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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HelpMenu {

	public HelpMenu(){
		Scene menu;
		Stage window = new Stage();
		FXMLLoader loader = new FXMLLoader();
		window.setTitle("Help");

		// load the fxml file containing the help menu content
		try {
			URL xml_url = new URL(this.getClass().getResource("test.fxml").toExternalForm());
			loader.setLocation(xml_url);
		}catch(MalformedURLException e){
			System.out.println("Could not find fxml file");
			return;
		}

		// load the fxml content into the Scene
		try {
			ScrollPane vbox = loader.<ScrollPane>load();
			menu = new Scene(vbox);
		}catch(IOException e){
			System.out.println("Could not load help menu content");
			return;
		}

		window.setScene(menu);
		window.show();
	}

	public void addWebView(VBox layout, String html_path) {

		WebView webView = new WebView();
		WebEngine webEngine= webView.getEngine();
		webEngine.load(html_path);
		layout.getChildren().add(webView);
	}
}
