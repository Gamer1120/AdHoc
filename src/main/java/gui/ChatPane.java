package gui;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Created by reneb_000 on 8-4-2015.
 */
public class ChatPane extends Pane {

    private VBox rightBox;
    private VBox leftBox;
    private Insets padding;

    public ChatPane(){
        rightBox = new VBox();
        leftBox = new VBox();
        rightBox.setMinWidth(300);
        leftBox.setMinWidth(300);
        padding = new Insets(10,10,10,10);
        //rightBox.setPadding(padding);
        //leftBox.setPadding(padding);
        rightBox.setSpacing(10);
        leftBox.setSpacing(10);

        HBox main = new HBox();
        //main.setPadding(padding);
        main.getChildren().addAll(leftBox, rightBox);
        this.getChildren().add(main);
    }


    public VBox getRight(){
        return rightBox;
    }

    public VBox getLeft(){
        return leftBox;
    }
    //true for right false for left
    public void add(Cloud cloud, boolean right){
        if(right){
            rightBox.getChildren().add(cloud);
            leftBox.getChildren().add(cloud.getEmptyCloud());
        }else {
            rightBox.getChildren().add(cloud.getEmptyCloud());
            leftBox.getChildren().add(cloud);
        }
    }


}
