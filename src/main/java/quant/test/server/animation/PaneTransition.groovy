package quant.test.server.animation

import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.layout.Pane
import javafx.util.Duration

/**
 * Created by cz on 2017/2/17.
 */
class PaneTransition {
    final Timeline timeline = new Timeline();

    PaneTransition(Pane node1, Pane node2,double left) {
        if(0>left){
            //right->left
            node1.setTranslateX(0)
            KeyValue keyValue1 = new KeyValue(node1.translateXProperty(), -left, Interpolator.LINEAR)
            KeyFrame keyFrame1 = new KeyFrame(Duration.millis(300), keyValue1)

            node2.setTranslateX(left)
            KeyValue keyValue2 = new KeyValue(node1.translateXProperty(), 0, Interpolator.LINEAR)
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(300), keyValue2)
            timeline.getKeyFrames().addAll(keyFrame1,keyFrame2)
        } else {
            //left->right
            KeyValue keyValue1 = new KeyValue(node1.translateXProperty(), left, Interpolator.LINEAR)
            KeyFrame keyFrame1 = new KeyFrame(Duration.millis(300), keyValue1)

            node2.setTranslateX(-left)
            KeyValue keyValue2 = new KeyValue(node1.translateXProperty(), 0, Interpolator.LINEAR)
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(300), keyValue2)
            timeline.getKeyFrames().addAll(keyFrame1, keyFrame2)
        }
        node1.setVisible(true)
        node2.setVisible(true)
        timeline.setOnFinished({
            node1.setVisible(true)
            node2.setVisible(false)
        })
    }

    def start(){
        timeline.play()
    }

}
