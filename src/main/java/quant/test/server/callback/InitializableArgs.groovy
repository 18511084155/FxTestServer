package quant.test.server.callback

import javafx.fxml.Initializable

/**
 * Created by cz on 2017/2/27.
 */
interface InitializableArgs<M> extends Initializable{
    void setArgs(M map)
}
