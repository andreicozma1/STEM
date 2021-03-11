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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

public class HelpMenu {

	public HelpMenu(){
		Stage window = new Stage();
		window.setTitle("Help");
		VBox layout = new VBox();
		Scene menu = new Scene(layout, 300, 300);

		String html_path = this.getClass().getResource("help.html").toString();

		addWebView(layout, html_path);

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
