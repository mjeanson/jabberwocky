package test

import java.nio.file.Paths
import java.util.StringJoiner

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.trace.event.ITraceEvent
import com.efficios.jabberwocky.trace.event.FieldValue

private val TRACE_PATH = "/home/alexandre/src/tc/tracecompass-test-traces/ctf/src/main/resources/kernel"

fun main(args: Array<String>) {
    val tracePath = Paths.get(TRACE_PATH)
    val trace = GenericCtfTrace(tracePath)
    trace.iterator().use({ iter ->
        var prevTimestamp: Long = 0
        var i = 0
        while (iter.hasNext()) {
            val event = iter.next()
            val offset = event.timestamp - prevTimestamp
            printEvent(trace, event, offset)
            prevTimestamp = event.timestamp
            i++
        }
    })
}

private fun printEvent(trace: CtfTrace<CtfTraceEvent>, event: ITraceEvent, offset: Long) {
    val ts = event.timestamp
    val ts2 = trace.innerTrace.timestampCyclesToNanos(ts)
//        val timestampStr = TS_FORMAT.format(ts2)
    val timestampStr = ts2.toString()

    val sj = StringJoiner(", ")
            .add(timestampStr)
            .add(offset.toString())
            .add(event.eventName)
            .add(event.cpu.toString())

    val sj2 = StringJoiner(", ", "{", "}")
    for (fieldName in event.fieldNames) {
        val fieldValue = event.getField(fieldName, FieldValue::class.java)
        sj2.add(fieldName + '=' + fieldValue.toString())
    }
    sj.add(sj2.toString())

    System.out.println(sj.toString())
}

