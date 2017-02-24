package quant.test.server.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;

import java.io.File;

/**
 * Created by cz on 2017/2/24.
 */
@DefaultProperty(value="icon")
public class ApkFileValidator extends ValidatorBase {

    @Override
    protected void eval() {
        Node node=srcControl.get();
        if(null!=node && node instanceof TextInputControl){
            TextInputControl textField = (TextInputControl)node;
            File file=new File(textField.getText());
            hasErrors.set(!(file.exists()&&file.getAbsolutePath().endsWith(".apk")));
        }
    }
}
