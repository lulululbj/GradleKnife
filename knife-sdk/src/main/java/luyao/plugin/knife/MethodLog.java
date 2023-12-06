package luyao.plugin.knife;

import android.util.Log;

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/27 14:44
 */
public class MethodLog {

    public static void printMethod(String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("method: ").append(methodName).append(" args: ");
        if (args != null) {
            for (Object arg : args) {
                sb.append(arg).append(" ");
            }
        }
        Log.e("GradleKnife", "detect method: " + methodName + " args: " + sb);
    }
}
