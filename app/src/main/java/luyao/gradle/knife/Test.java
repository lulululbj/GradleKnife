package luyao.gradle.knife;

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 16:01
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        TimeCounter.start("tag");
        Thread.sleep(1000);
        TimeCounter.end("tag");
    }
}
