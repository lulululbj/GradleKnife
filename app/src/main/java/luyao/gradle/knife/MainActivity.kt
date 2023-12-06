package luyao.gradle.knife

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import luyao.gradle.knife.databinding.ActivityMainBinding
import luyao.plugin.knife.MethodTrace

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        System.loadLibrary("zbar")

    }

    private fun initListener() {
        binding.run {
            methodTrace.setOnClickListener { testMethodTrace("Hello", Snackbar.LENGTH_LONG) }
            methodTrace2.setOnClickListener {
                testMethodTrace2(
                    "methodTrace",
                    listOf("1", "2", "3", "4", "5")
                )
            }
        }
    }

    @MethodTrace(traceParamsAndReturnValue = true)
    private fun testMethodTrace(text: String, showTime: Int): Pair<Int, String> {
        Snackbar.make(binding.root, text, showTime)
            .setAction("Action", null).show()
        return showTime to text
    }

    @MethodTrace(traceParamsAndReturnValue = true)
    private fun testMethodTrace2(tag: String, list: List<String>): List<String> {
        list.forEach {
            Log.e(tag, it)
        }
        return list.map { it.repeat(2) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}