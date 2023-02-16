package app.peter.seurat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CustomScope : CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default
}