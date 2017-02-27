package quant.test.server.widget;

import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;

import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * Created by cz on 2017/2/27.
 */
public class MyDatePicker extends DatePicker {

    public MyDatePicker() {
    }

    public MyDatePicker(LocalDate localDate) {
        super(localDate);
    }

    @Override
    public void hide() {
        //这里不让其隐藏了.因为外层需要控制点击逻辑
//        super.hide();
        try {
            Method method = ComboBoxBase.class.getDeclaredMethod("showingPropertyImpl");
            method.setAccessible(true);
            Object obj = method.invoke(this);
            if(null!=obj){
                Method setMethod = BooleanPropertyBase.class.getMethod("set", boolean.class);
                setMethod.invoke(obj,false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
