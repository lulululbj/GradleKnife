package luyao.gradle.knife;


import luyao.plugin.knife.MethodTimeCounter;

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 16:01
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        MethodTimeCounter.start("tag");
        Thread.sleep(1000);
        MethodTimeCounter.end("tag");
    }
}
