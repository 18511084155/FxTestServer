package quant.test.server.widget.drag;

import com.jfoenix.controls.JFXTextField;

/**
 * 可拖动的text-field对象
 */
public class DragTextField extends JFXTextField{

    public DragTextField() {
        setOnDragOver(new DragOverEvent(this));
        setOnDragDropped(new DragDroppedEvent(this));
    }

    public DragTextField(String text) {
        super(text);
        setOnDragOver(new DragOverEvent(this));
        setOnDragDropped(new DragDroppedEvent(this));
    }

}