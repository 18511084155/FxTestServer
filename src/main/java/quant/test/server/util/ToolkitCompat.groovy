package quant.test.server.util

import com.sun.javafx.tk.Toolkit

import java.lang.reflect.Field

/**
 * Created by cz on 2017/2/27.
 */
class ToolkitCompat {

    /**
     * 获得用户线程
     * @return
     */
    static Thread getUserThread(){
        Field field=Toolkit.class.getDeclaredField("fxUserThread")
        field.setAccessible(true)
        field.get(null)
    }
}
