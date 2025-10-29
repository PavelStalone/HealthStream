package ru.health.stream.core.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import javax.inject.Inject

abstract class StarterActivity : ComponentActivity() {

    @Inject
    lateinit var starters: Set<@JvmSuppressWildcards ActivityStarter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_START)
        }
    }

    override fun onResume() {
        super.onResume()

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onPause() {
        super.onPause()

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_PAUSE)
        }
    }

    override fun onStop() {
        super.onStop()

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_STOP)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        starters.forEach { starter ->
            starter.onStateChanged(source = this, event = Lifecycle.Event.ON_DESTROY)
        }
    }
}
