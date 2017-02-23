package quant.test.server.widget.drag;

import com.jfoenix.controls.JFXTextField;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

import java.io.File;

public class DragDroppedEvent implements EventHandler<DragEvent> {
    private final JFXTextField textField;

    public DragDroppedEvent(JFXTextField textField){
        this.textField = textField;
    }

    public void handle(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()){
            try {
                File file = dragboard.getFiles().get(0);
                if (file != null) {
                    textField.setText(file.getAbsolutePath());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}