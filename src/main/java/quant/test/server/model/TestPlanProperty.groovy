package quant.test.server.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty

/**
 * Created by cz on 2017/3/2.
 */
class TestPlanProperty extends RecursiveTreeObject<TestPlanProperty> {
    SimpleIntegerProperty caseId
    SimpleStringProperty name
    SimpleStringProperty testCase
    SimpleStringProperty startDate
    SimpleStringProperty endDate
    SimpleBooleanProperty cycle
    SimpleBooleanProperty invalid
    SimpleIntegerProperty uid
    SimpleLongProperty st
    SimpleLongProperty et

    TestPlanProperty(TestPlanItem item) {
        caseId=new SimpleIntegerProperty(item.caseId)
        name=new SimpleStringProperty(item.name)
        testCase=new SimpleStringProperty(item.testCase)
        startDate=new SimpleStringProperty(item.startDate)
        endDate=new SimpleStringProperty(item.endDate)
        cycle=new SimpleBooleanProperty(item.cycle)
        invalid=new SimpleBooleanProperty(item.invalid)
        uid=new SimpleIntegerProperty(item.uid)
        st=new SimpleLongProperty(item.st)
        et=new SimpleLongProperty(item.et)
    }
}
