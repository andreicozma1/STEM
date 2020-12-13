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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.Group;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.util.ArrayList;


public class TransitionEditor {

    public Transition createdTransition;
    private ArrayList<Transition> deletedTransitions;

    public TransitionEditor(Stage window, State from, State to){
        Stage transitionEditor = new Stage();
        transitionEditor.initModality(Modality.APPLICATION_MODAL);
        transitionEditor.initOwner(window);

        BorderPane borderPane = new BorderPane();
        HBox hBox = new HBox();
        VBox col1 = new VBox();
        VBox col2 = new VBox();
        VBox col3 = new VBox();


        //    ____      _    _
        //   / ___|___ | |  / |
        //  | |   / _ \| |  | |
        //  | |___ (_) | |  | |
        //   \____\___/|_|  |_|
        //
        Text col1Text = new Text("Read");

        TextField col1TextArea = new TextField();
        col1TextArea.setPrefColumnCount(1);
        col1TextArea.setTextFormatter(new TextFormatter<>((TextFormatter.Change change) -> {
            String newText = change.getControlNewText();
            if(newText.length() > 1 || containsIllegalCharacters(newText))
                return null;
            else
                return change;
        }));
        col1TextArea.setAlignment(Pos.CENTER);
        col1TextArea.setStyle("-fx-padding: 5px; -fx-border-insets: 5px; -fx-background-insets:5px;");
        col1TextArea.resize(100, 100);

        col1.getChildren().addAll(col1Text,col1TextArea);

        //    ____      _    ____
        //   / ___|___ | |  |___ \
        //  | |   / _ \| |    __) |
        //  | |___ (_) | |   / __/
        //   \____\___/|_|  |_____|
        //
        Text col2Text= new Text("Write");

        TextField col2TextArea = new TextField();
        col2TextArea.setPrefColumnCount(1);
        col2TextArea.setTextFormatter(new TextFormatter<>((TextFormatter.Change change) -> {
            String newText = change.getControlNewText();
            if(newText.length() > 1 || containsIllegalCharacters(newText))
                return null;
            else
                return change;
        }));
        col2TextArea.setAlignment(Pos.CENTER);
        col2TextArea.setStyle("-fx-padding: 5px; -fx-border-insets: 5px; -fx-background-insets: 5px;");
        col2TextArea.resize(100, 100);

        col2.getChildren().addAll(col2Text, col2TextArea);

        //    ____      _    _____
        //   / ___|___ | |  |___ /
        //  | |   / _ \| |    |_ \
        //  | |___ (_) | |   ___) |
        //   \____\___/|_|  |____/
        //
        Text col3Text = new Text("Move");

        VBox col3VBox = new VBox();

        ToggleGroup col3Toggle = new ToggleGroup();
        ToggleButton left = new ToggleButton("<-");
        ToggleButton right = new ToggleButton("->");
        ToggleButton none = new ToggleButton("S");

        left.setUserData(Transition.Direction.LEFT);
        left.setToggleGroup(col3Toggle);
        right.setUserData(Transition.Direction.RIGHT);
        right.setToggleGroup(col3Toggle);
        none.setUserData(Transition.Direction.STAY);
        none.setToggleGroup(col3Toggle);

        col3VBox.getChildren().addAll(right, left, none);

        col3.getChildren().addAll(col3Text, col3VBox);

        //   ____        _               _ _
        //  / ___| _   _| |__  _ __ ___ (_) |_
        //  \___ \| | | | '_ \| '_ ` _ \| | __|
        //   ___) | |_| | |_) | | | | | | | |_
        //  |____/ \__,_|_.__/|_| |_| |_|_|\__|
        //

        Button submitButton = new Button("Confirm");
        submitButton.setOnAction(event -> {
            createdTransition = new Transition(to, from, col1TextArea.getCharacters().charAt(0),
                    col2TextArea.getCharacters().charAt(0),
                    (Transition.Direction) col3Toggle.getSelectedToggle().getUserData());

            transitionEditor.close();
        });


        borderPane.setAlignment(submitButton, Pos.CENTER);

        //   ____  _           _ _
        //  | __ )(_)_ __   __| (_)_ __   __ _ ___
        //  |  _ \| | '_ \ / _` | | '_ \ / _` / __|
        //  | |_) | | | | | (_| | | | | | (_| \__ \
        //  |____/|_|_| |_|\__,_|_|_| |_|\__, |___/
        //                               |___/
        col1.setAlignment(Pos.TOP_CENTER);
        col2.setAlignment(Pos.TOP_CENTER);
        col3.setAlignment(Pos.TOP_CENTER);

        hBox.getChildren().addAll(col1,col2,col3);

        ObjectExpression<Font> col1TextAreaFontTrack = Bindings.createObjectBinding(
                () -> Font.font(col1TextArea.getWidth() / 2), col1TextArea.widthProperty());
        ObjectExpression<Font> col2TextAreaFontTrack = Bindings.createObjectBinding(
                () -> Font.font(col2TextArea.getWidth() / 2), col2TextArea.widthProperty());
        ObjectExpression<Font> toggleButtonFontTrack = Bindings.createObjectBinding(
                () -> Font.font(left.getWidth() / 8), left.widthProperty());
        ObjectExpression<Font> submitButtonFontTrack = Bindings.createObjectBinding(
                () -> Font.font(Math.min(borderPane.getWidth(), borderPane.getHeight())/ 8), borderPane.widthProperty(), borderPane.heightProperty());

        BooleanBinding submitButtonDisableBinding = new BooleanBinding() {
            {
                super.bind(col1TextArea.textProperty(),
                        col2TextArea.textProperty(),
                        col3Toggle.selectedToggleProperty());
            }

            @Override
            protected boolean computeValue() {
                return (col1TextArea.getText().isEmpty()
                        || col2TextArea.getText().isEmpty()
                        || col3Toggle.getSelectedToggle() == null);
            }
        };

        col1TextArea.prefHeightProperty().bind(col1TextArea.widthProperty());
        col1TextArea.fontProperty().bind(col1TextAreaFontTrack);

        col2TextArea.prefHeightProperty().bind(col2TextArea.widthProperty());
        col2TextArea.fontProperty().bind(col2TextAreaFontTrack);

        left.prefWidthProperty().bind(col3.widthProperty());
        left.prefHeightProperty().bind(col2TextArea.widthProperty().divide(3.2));
        left.fontProperty().bind(toggleButtonFontTrack);
        right.prefWidthProperty().bind(col3.widthProperty());
        right.prefHeightProperty().bind(col2TextArea.widthProperty().divide(3.2));
        right.fontProperty().bind(toggleButtonFontTrack);
        none.prefWidthProperty().bind(col3.widthProperty());
        none.prefHeightProperty().bind(col2TextArea.widthProperty().divide(3.2));
        none.fontProperty().bind(toggleButtonFontTrack);

        submitButton.fontProperty().bind(submitButtonFontTrack);
        submitButton.disableProperty().bind(submitButtonDisableBinding);

        col1.prefWidthProperty().bind(hBox.widthProperty().divide(3));
        col2.prefWidthProperty().bind(hBox.widthProperty().divide(3));
        col3.prefWidthProperty().bind(hBox.widthProperty().divide(3));

        borderPane.setCenter(hBox);
        borderPane.setBottom(submitButton);
        Scene popUp = new Scene(borderPane, 300,200);
        transitionEditor.setScene(popUp);

        transitionEditor.setMinWidth(300);
        transitionEditor.minHeightProperty().bind(borderPane.widthProperty().divide(1.5));

        transitionEditor.showAndWait();
    }
    /* Constructor for editing an already existing transition
    */
    public TransitionEditor(Stage window, Path path){
        Stage transitionEditor = new Stage();
        transitionEditor.setTitle("Transition Editor");
        transitionEditor.setWidth(640);
        transitionEditor.setHeight(500);
        transitionEditor.setResizable(true);
        transitionEditor.initModality(Modality.APPLICATION_MODAL);
        transitionEditor.initOwner(window);
        deletedTransitions = new ArrayList<Transition>();
        ObservableList<Transition> list = FXCollections.observableArrayList();

        TableView table = new TableView();
        Label label = new Label("To make any changes, double click on the Read/Write/Direction cells, and press Enter when finished. Any edits are final. Once the window is closed, the edits will take effect.");
        label.setMaxWidth(transitionEditor.getWidth());
        label.setMaxHeight(transitionEditor.getHeight());
        label.setPrefWidth(transitionEditor.getWidth()/4);
        label.setPrefHeight(transitionEditor.getHeight()/4);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        //label.setFont(new Font("Arial", 20));
        table.setEditable(true);
 
        TableColumn FromS = new TableColumn("From State");
        FromS.setMaxWidth(100);
        FromS.setPrefWidth(100);
        FromS.setCellValueFactory(new PropertyValueFactory<Transition, String>("FromStateName"));
        TableColumn ToS = new TableColumn("To State");
        ToS.setMaxWidth(100);
        ToS.setPrefWidth(100);
        ToS.setCellValueFactory(new PropertyValueFactory<Transition, String>("ToStateName"));
        TableColumn read = new TableColumn("Read");
        read.setMaxWidth(100);
        read.setPrefWidth(100);
        read.setCellValueFactory(new PropertyValueFactory<Transition, String>("ReadString"));
        read.setCellFactory(TextFieldTableCell.forTableColumn());
        read.setOnEditCommit(
            new EventHandler<CellEditEvent<Transition, String>>(){
                @Override
                public void handle(CellEditEvent<Transition, String> t) {
                    if(t.getNewValue().length() == 1){
                        ((Transition) t.getTableView().getItems().get(t.getTablePosition().getRow())).setReadChar(t.getNewValue().charAt(0));
                        t.getTableView().refresh();
                    }
                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setResizable(true);
						alert.initOwner(window);
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("Incorrect Transition");
						alert.setHeaderText("Read can only be 1 character in length.");
                        alert.showAndWait();
                        t.getTableView().refresh();
                    }
                }
            }
        );
        TableColumn write = new TableColumn("Write");
        write.setMaxWidth(100);
        write.setPrefWidth(100);
        write.setCellValueFactory(new PropertyValueFactory<Transition, String>("WriteString"));
        write.setCellFactory(TextFieldTableCell.forTableColumn());
        write.setOnEditCommit(
            new EventHandler<CellEditEvent<Transition, String>>(){
                @Override
                public void handle(CellEditEvent<Transition, String> t) {
                    if(t.getNewValue().length() == 1){
                        ((Transition) t.getTableView().getItems().get(t.getTablePosition().getRow())).setWriteChar(t.getNewValue().charAt(0));
                        t.getTableView().refresh();
                    }
                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setResizable(true);
						alert.initOwner(window);
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("Incorrect Transition");
						alert.setHeaderText("Write can only be 1 character in length.");
                        alert.showAndWait();
                        t.getTableView().refresh();
                    }
                }
            }
        );
        TableColumn direction = new TableColumn("Direction");
        direction.setMaxWidth(100);
        direction.setPrefWidth(100);
        direction.setCellValueFactory(new PropertyValueFactory<Transition, String>("DirectionChar"));
        direction.setCellFactory(TextFieldTableCell.forTableColumn());
        direction.setOnEditCommit(
            new EventHandler<CellEditEvent<Transition, String>>(){
                @Override
                public void handle(CellEditEvent<Transition, String> t) {
                    if(Character.toUpperCase(t.getNewValue().charAt(0)) == 'L' || 
                        Character.toUpperCase(t.getNewValue().charAt(0)) == 'R' || Character.toUpperCase(t.getNewValue().charAt(0)) == 'S'){
                        ((Transition) t.getTableView().getItems().get(t.getTablePosition().getRow())).setDirectionChar(Character.toUpperCase(t.getNewValue().charAt(0)));
                        t.getTableView().refresh();
                    }
                    else{
                        System.out.println("This is our value: " + t.getNewValue());
                        System.out.println("This is our length: " + t.getNewValue().length());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setResizable(true);
						alert.initOwner(window);
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("Incorrect Transition");
						alert.setHeaderText("Direction must be 'L', 'R', or 'S'\nL: Left\nR: Right\nS: Stay");
                        alert.showAndWait();
                        t.getTableView().refresh();
                    }
                }
            }
        );

        TableColumn deleteCol = new TableColumn("Delete Transition");
        deleteCol.setMaxWidth(130);
        deleteCol.setPrefWidth(130);

        /* Adding the button to the column
        */
        Callback<TableColumn<Transition, String>, TableCell<Transition, String>> cellFactory
                = //
                new Callback<TableColumn<Transition, String>, TableCell<Transition, String>>() {
            @Override
            public TableCell call(final TableColumn<Transition, String> param) {
                
                final TableCell<Transition, String> cell = new TableCell<Transition, String>() {

                    final Button del_btn = new Button("Delete transition!");
                    
                    @Override
                    public void updateItem(String item, boolean empty) {
                        
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            // event listener for when the button is pressed
                            del_btn.setOnAction(event -> {
                                Alert alert = new Alert(Alert.AlertType.NONE);
                                alert.setResizable(true);
                                alert.setTitle("Delete?");
                                alert.setContentText("Are you sure you want to delete this transition? The transition will not be recoverable after being deleted.");
                                ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                                alert.getButtonTypes().setAll(yesButton, noButton);

                                /* this portion is when the user clicks to delete a transition. It asks for a confirmation
                                * If confirmed: the respective transition is stored in a list. Then, once the user presses the X for closing the window, all changes will be recorded
                                */
                                alert.showAndWait().ifPresent(type -> {
                                        if (type.getText() == "Yes") {
                                            // get the respective transition, add it to the deleted list, and remove it from the list in the table
                                            Transition trns = getTableView().getItems().get(getIndex());
                                            deletedTransitions.add(trns);
                                            list.remove(trns);

                                        } else if (type.getText() == "No") {
                                            System.out.println("USER CLICKED NO");
                                        }
                                    });
                            });
                            setGraphic(del_btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        
        deleteCol.setCellFactory(cellFactory);

        

        table.setItems(list);
        
        // if it is not a self loop
        if(path.getStateOne() != path.getStateTwo()){
            for(State s : path.getStates()){
                if(s.getTransition().size() > 0){
                    for(Transition t : s.getTransition()){
                        if((t.getToState() == path.getStateTwo() && t.getFromState() == path.getStateOne()) || 
                            (t.getToState() == path.getStateOne() && t.getFromState() == path.getStateTwo())){
                            list.add(t);
                        }
                    }
                }
            }
        }
        else{
            if(path.getStateOne().getTransition().size() > 0){
                for(Transition t : path.getStateOne().getTransition()){
                    if(t.getFromState() == t.getToState()){
                        list.add(t);
                    }
                }
            }
        }
        
        table.getColumns().addAll(FromS, ToS, read, write, direction, deleteCol);
        VBox vbox = new VBox(label);
        vbox.getChildren().addAll(table);
        Scene popUp = new Scene(vbox);
        transitionEditor.setScene(popUp);
        transitionEditor.showAndWait();
    }

    public ArrayList<Transition> getDeletedTransition(){
        return deletedTransitions;
    }


    public TransitionEditor(Stage window, State from, State to, char read, char write){
        createdTransition = new Transition(to, from, read, write);
    }

    private boolean containsIllegalCharacters(String s){
        char s_arr[] = s.trim().toCharArray();

        for(char c : s_arr){
            if(c < 32 || c >= 127)
                return true;
        }

        return false;
    }
}
