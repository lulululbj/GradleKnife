package luyao.plugin.test

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/23 16:27
 */
class TestPlugin : Plugin<Project>{


    override fun apply(target: Project) {
        println("TestPlugin apply")
    }

}