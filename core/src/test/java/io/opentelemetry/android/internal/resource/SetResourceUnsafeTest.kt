package io.opentelemetry.android.internal.resource

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SetResourceUnsafeTest {
    private val initialResource = Resource.create(Attributes.of(AttributeKey.booleanKey("updated"), false))
    private val updatedResource = Resource.create(Attributes.of(AttributeKey.booleanKey("updated"), true))

    private val logsExporter = InMemoryLogRecordExporter.create()
    private val loggerProvider = SdkLoggerProvider.builder().setResource(initialResource).addLogRecordProcessor(SimpleLogRecordProcessor.create(logsExporter)).build()


    @Test
    fun `should update resource of SdkLoggerProvider`() {
        emitLog()
        loggerProvider.setResourceUnsafe(updatedResource)
        emitLog()

        assertThat(logsExporter.finishedLogRecordItems)
            .hasSize(2)
            .allSatisfy { record  ->
                assertThat(record)
                .extracting(LogRecordData::getResource)
                .isSameAs(updatedResource)
            }
    }

    private fun emitLog() {
        val logger = loggerProvider.loggerBuilder("instrumentation").build()
        logger.logRecordBuilder().emit()
    }
}