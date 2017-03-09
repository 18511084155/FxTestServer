package quant.test.server.model
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleStringProperty

import java.text.SimpleDateFormat
/**
 * Created by cz on 2017/3/9.
 */
class ExceptionItem extends RecursiveTreeObject<ExceptionItem> {
    SimpleStringProperty fileName
    SimpleStringProperty filePath
    SimpleStringProperty lastModified

    static ExceptionItem from(File file){
        def item=new ExceptionItem()
        item.fileName=new SimpleStringProperty(file.name)
        item.filePath=new SimpleStringProperty(file.absolutePath)
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        item.lastModified=new SimpleStringProperty(formatter.format(new Date(file.lastModified())))
        item
    }
}
