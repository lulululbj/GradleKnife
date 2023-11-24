package luyao.gradle.knife;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 16:08
 */
public class TimeCounter {

    private static final Map<String, Long> countMap = new HashMap<>();

    public static void start(String tag) {
        countMap.put(tag, System.currentTimeMillis());
        Log.e("TimeCounter", tag + " start");
    }

    public static void end(String tag) {
        Long start = countMap.get(tag);
        if (start == null) {
            System.out.println("tag not found");
            return;
        }
        Log.e("TimeCounter", tag + " cost time : " + (System.currentTimeMillis() - start) + "ms");
        countMap.remove(tag);
    }
}
