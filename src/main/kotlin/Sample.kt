import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun getWithDelay(): String {
    println("With delay on thread = ${Thread.currentThread().name}")
    return java.net.URL("https://hub.dummyapis.com/delay?seconds=2")
        .readText()
}

fun getIPAddress(): String {
    println("getIpAddress on thread = ${Thread.currentThread().name}")
    return java.net.URL("https://api.ipify.org").readText()
}

/**
 * Use the default Coroutine Dispatcher to spin up threads.
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html
 * Will have at least 2 threads, but maximum is the number of CPU cores.
 * My macbook M1 Pro has 10 cores, so the below will execute in slight more than 2 seconds if the loop count is 5 or less
 * However, any additional loops reuses threads and therefor blocks.
 */
fun main(args: Array<String>) {

    val elapsed: Long = measureTimeMillis { // Measure how long the launch block takes to execute
        runBlocking<Unit> {

            val job = GlobalScope.launch {
                val defferedList = emptyList<Deferred<String>>()
                repeat(6) {// Load up some coroutines with the async function. (2 per loop)
                    try {
                        // Get the IP address
                        val ip = async { getIPAddress() }
                        defferedList.plus(ip)
                        
                        // get from a server with a delay
                        val response = async { getWithDelay() }
                        defferedList.plus(response)

                    } catch (ex: Exception) {
                        println("Error getting response: $ex")
                    }
                }

                val resultList = defferedList.awaitAll() // Why does this not wait?
                for (result in resultList) {
                    println("Results loop")
                    println("result ${result}")
                }
            }

            println("Started...")
            job.join()
        }

    }
    println("Done in $elapsed ms") // Show elapsed time
}
