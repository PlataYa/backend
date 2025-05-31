package plataya.app.service

class PlataYaCVUCounter {
    private val CVU_PREFIX = 100000000000 // CVU prefix for PlataYa
    private var cvuCounter = 0

    fun getNextCVU(): Long {
        cvuCounter++
        return CVU_PREFIX + cvuCounter
    }
}
