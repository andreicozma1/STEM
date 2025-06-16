package Types;

import java.util.ArrayList;

import Editor.Editor;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Unit_Old {

    private boolean created;
    private ArrayList<State> states;
    private ArrayList<Transition> internalTransitions;
    private ArrayList<Transition> externalTransitions;
    private State entrance;
    private State exit;
    private Editor editor;
    private Machine machine;
    private String name;
    private Rectangle rectangle;
    private EventHandler<MouseEvent> chooseEventHandler;
    private EventHandler<KeyEvent> escapeHandler;
    private EventHandler<MouseEvent> cancelHandler;

    public Unit_Old(Machine machine, Editor editor) {
        this.editor = editor;
        this.machine = machine;
        created = false;
        this.states = (ArrayList<State>) editor.getSelectedStates().clone();
        this.internalTransitions = new ArrayList<>();
        this.externalTransitions = new ArrayList<>();
        Rectangle box = new Rectangle(0, 0, editor.getEditorSpace().getWidth(), editor.getEditorSpace().getHeight());
        // I don't know why we have to make a new rectangle with all the same dimensions as selectedArea for this to work properly, but we do
        Rectangle clip = new Rectangle(editor.getSelectedArea().getX(),
                editor.getSelectedArea().getY(),
                editor.getSelectedArea().getWidth(),
                editor.getSelectedArea().getHeight());
        editor.setChoosingBox(Shape.subtract(box, clip));
        editor.getChoosingBox().setFill(new Color(0.1, 0.1, 0.1, 0.5));
        editor.getEditorSpace().getChildren().add(editor.getChoosingBox());
        editor.unselectAllStates();

        selectEntrance();
    }

    private void selectEntrance() {
        chooseEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    Object target = ((Node) event.getTarget()).getUserData();
                    if (target instanceof State) {
                        System.out.println("Selected Entrance");
                        event.consume();
                        entrance = (State) target;

                        // add a selected circle to indicate that it is the entrance
                        Circle selectCircle = new Circle(entrance.getX(), entrance.getY(),
                                editor.getCircleRadius() * 1.5, Color.TRANSPARENT);
                        selectCircle.setStroke(new Color(0, 0.6, 0, 1));
                        selectCircle.setStrokeWidth(2);
                        entrance.setSelctedCircle(selectCircle);
                        editor.getEditorSpace().getChildren().add(selectCircle);

                        editor.getEditorSpace().removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                        selectExit();
                    }
                }
            }
        };

        cancelHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getTarget() == editor.getChoosingBox()) {
                        cleanUp();
                    }
                }
            }
        };

        escapeHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println(event.getCode());
                if (event.getCode() == KeyCode.ESCAPE) {
                    cleanUp();
                }
            }
        };
        editor.getEditorSpace().addEventHandler(MouseEvent.MOUSE_CLICKED, chooseEventHandler);
        editor.getEditorSpace().addEventHandler(MouseEvent.MOUSE_CLICKED, cancelHandler);
        editor.getEditorSpace().addEventHandler(KeyEvent.KEY_PRESSED, escapeHandler);
    }

    private void selectExit() {
        // allow user to select the output state
        chooseEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isConsumed()) {
                    return;
                }
                if (event.getButton() == MouseButton.PRIMARY) {
                    Object target = ((Node) event.getTarget()).getUserData();
                    if (target instanceof State) {
                        System.out.println("Selected Exit");
                        exit = (State) target;
                        createUnit();
                    }
                }
            }
        };
        editor.getEditorSpace().addEventHandler(MouseEvent.MOUSE_CLICKED, chooseEventHandler);

    }

    private void cleanUp() {
        editor.getEditorSpace().getChildren().remove(editor.getChoosingBox());

        /*
         * Might remove this part so that the state is recognizable as the entrance in the unit
         * Will keep for now until I get there
         */
        if (entrance != null) {
            editor.getEditorSpace().getChildren().remove(entrance.getSelctedCircle());
            entrance.setSelctedCircle(null);
        }

        editor.getEditorSpace().removeEventHandler(KeyEvent.KEY_PRESSED, escapeHandler);
        editor.getEditorSpace().removeEventHandler(MouseEvent.MOUSE_CLICKED, chooseEventHandler);
        editor.getEditorSpace().removeEventHandler(MouseEvent.MOUSE_CLICKED, cancelHandler);
    }

    private void createUnit() {
        created = true;
        cleanUp();

        for (State state : states) {
            if(state == exit || state == entrance){
                for (Transition transition : state.getTransition()) {
                    if((transition.getFromState() == entrance || transition.getFromState() == exit) && states.contains(transition.getToState())){
                        internalTransitions.add(transition);
                    }
                    else{
                        externalTransitions.add(transition);
                    }
                }
            }
            else{
                internalTransitions.addAll(state.getTransition());
            }
        }

        // create the rectangle that will represent the unit
        rectangle = new Rectangle(editor.getSelectedArea().getX(), editor.getSelectedArea().getY(), 200, 200);
        rectangle.setUserData(this);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(2);
        rectangle.setFill(Color.LIGHTGOLDENRODYELLOW);

        // disable movement on the entrance and exit nodes
        entrance.getCircle().setOnMousePressed(null);
        entrance.getCircle().setOnMouseDragged(null);
        entrance.getCircle().setOnMouseReleased(null);
        entrance.getLabel().setOnMousePressed(null);
        entrance.getLabel().setOnMouseDragged(null);
        entrance.getLabel().setOnMouseReleased(null);

        exit.getCircle().setOnMousePressed(null);
        exit.getCircle().setOnMouseDragged(null);
        exit.getCircle().setOnMouseReleased(null);
        exit.getLabel().setOnMousePressed(null);
        exit.getLabel().setOnMouseDragged(null);
        exit.getLabel().setOnMouseReleased(null);

        // let the unit be dragged
        rectangle.setOnMouseClicked((event) -> {
            
        });        

        editor.getEditorSpace().getChildren().add(rectangle);

        // remove every state and transition from the parent editor space
        for (Transition t : machine.getTransitions()){
            if((t.getToState() == entrance && !states.contains(t.getFromState())) ||
                (t.getFromState() == exit && !states.contains((t.getToState())))){
                    continue;
            }
            editor.getEditorSpace().getChildren().removeAll(t.getPath().getAllNodes());
        }
        for(State s : states){
            if(s == entrance || s == exit){
                continue;
            }
            editor.getEditorSpace().getChildren().removeAll(s.getCircle(), s.getAcceptCircle(), s.getLabel());
        }

        entrance.getCircle().setCenterX(rectangle.getX());
        entrance.getCircle().setCenterY(rectangle.getY() + rectangle.getHeight() / 2);
        exit.getCircle().setCenterX(rectangle.getX() + rectangle.getWidth());
        exit.getCircle().setCenterY(rectangle.getY() + rectangle.getHeight() / 2);

        entrance.getLabel().setX(entrance.getCircle().getCenterX() - (entrance.getLabel().getLayoutBounds().getWidth() / 2));
        entrance.getLabel().setY(entrance.getCircle().getCenterY() + (entrance.getLabel().getLayoutBounds().getHeight() / 4));
        exit.getLabel().setX(exit.getCircle().getCenterX() - (exit.getLabel().getLayoutBounds().getWidth() / 2));
        exit.getLabel().setY(exit.getCircle().getCenterY() + (exit.getLabel().getLayoutBounds().getHeight() / 4));

        rectangle.toFront();
        entrance.getCircle().toFront();
        exit.getCircle().toFront();
        entrance.getLabel().toFront();
        exit.getLabel().toFront();

        editor.redrawPaths(externalTransitions);
    }

    /*
     * Getters
     */

    public boolean gotCreated() {
        return created;
    }

    public double getX() {
        return rectangle.getX();
    }

    public double getY() {
        return rectangle.getY();
    }

    public double getWidth() {
        return rectangle.getWidth();
    }

    public double getHeight() {
        return rectangle.getHeight();
    }

    /*
     * Setters
     */

    public void setX(double value) {
        rectangle.setX(value);
    }

    public void setY(double value) {
        rectangle.setY(value);
    }

    public void setWidth(double value) {
        rectangle.setWidth(value);
    }

    public void setHeight(double value) {
        rectangle.setHeight(value);
    }

}
