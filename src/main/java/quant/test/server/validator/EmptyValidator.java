package quant.test.server.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;

/**
 * Created by cz on 2017/2/24.
 */
@DefaultProperty(value="icon")
public class EmptyValidator extends ValidatorBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void eval() {
        Node control = srcControl.get();
        if(null!=control&& control instanceof TextInputControl){
            TextInputControl textControl=(TextInputControl)control;
            hasErrors.set(3>textControl.getText().length());
        }
    }
}
