package quant.test.server.widget

import javafx.scene.control.ListCell
import quant.test.server.controller.TestPlanItemController
import quant.test.server.model.TestPlanItem
/**
 * Created by czz on 2017/2/15.
 */
class TestPlanCell extends ListCell<TestPlanItem> {
    private final TestPlanItemController controller = new TestPlanItemController();
    private final def view = controller.getView()

    @Override
    protected void updateItem(TestPlanItem item, boolean empty) {
        super.updateItem(item, empty)
        if (empty) {
            setGraphic(null)
        } else {
            controller.bindItem(item)
            setGraphic(view)
        }
    }

}
