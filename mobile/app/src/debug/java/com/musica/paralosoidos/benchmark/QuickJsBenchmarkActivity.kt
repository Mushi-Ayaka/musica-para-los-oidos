package com.musica.paralosoidos.benchmark

import android.app.Activity
import android.os.Bundle
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext

class QuickJsBenchmarkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread {
            val totalStart = SystemClock.elapsedRealtimeNanos()
            var context: QuickJSContext? = null
            try {
                val pssBeforeLoadKb = Debug.getPss()
                val loaderStart = SystemClock.elapsedRealtimeNanos()
                QuickJSLoader.init()
                val loaderMs = elapsedMillis(loaderStart)
                val pssAfterLoadKb = Debug.getPss()

                val contextStart = SystemClock.elapsedRealtimeNanos()
                context = QuickJSContext.create()
                val contextMs = elapsedMillis(contextStart)
                val pssAfterContextKb = Debug.getPss()

                val evaluateStart = SystemClock.elapsedRealtimeNanos()
                context.evaluate(
                    "(() => { const value = 'benchmark123'.split('').reverse().join(''); " +
                        "if (value !== '321kramhcneb') throw new Error('JS check failed'); })()"
                )
                val evaluateMs = elapsedMillis(evaluateStart)
                val pssAfterEvaluateKb = Debug.getPss()
                val quickJsMemoryBytes = context.getMemoryUsedSize()
                context.close()
                context = null

                Log.i(
                    TAG,
                    "RESULT loaderMs=$loaderMs contextMs=$contextMs " +
                        "evaluateMs=$evaluateMs totalMs=${elapsedMillis(totalStart)} " +
                        "quickJsMemoryBytes=$quickJsMemoryBytes " +
                        "pssBeforeLoadKb=$pssBeforeLoadKb pssAfterLoadKb=$pssAfterLoadKb " +
                        "pssAfterContextKb=$pssAfterContextKb pssAfterEvaluateKb=$pssAfterEvaluateKb"
                )
            } catch (error: Throwable) {
                Log.e(TAG, "FAILED", error)
            } finally {
                runOnUiThread { finish() }
            }
        }.start()
    }

    private fun elapsedMillis(startNanos: Long): Long =
        (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000

    private companion object {
        const val TAG = "QuickJsBenchmark"
    }
}
