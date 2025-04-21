package io.opentelemetry.android.internal.resource

import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.resources.Resource

/**
 * Hacky workaround for updating the [Resource] associated with the [SdkLoggerProvider]. Call only
 * before the actual exporters are instantiated
 */
internal fun SdkLoggerProvider.setResourceUnsafe(resource: Resource) {
    // The Resource is part of the immutable LoggerSharedState, which is shared across all instances
    // of SdkLogger created by this SdkLoggerProvider. Changing this is definitely not intended by
    // the Java SDK, but it works here, as this function is called before any actual exporter would
    // have the chance to read the Resource value.
    val sharedState = getFieldValue("sharedState")
    sharedState.setFieldValue("resource", resource)
}

private fun Any.getFieldValue(name: String): Any =
    javaClass.getDeclaredField(name).let { field ->
        field.isAccessible = true
        field.get(this@getFieldValue) as Any
    }

private fun <V> Any.setFieldValue(name: String, value: V) =
    javaClass.getDeclaredField(name).apply {
        isAccessible = true
        // Doesn't work unfortunately
        // java.lang.IllegalArgumentException: Can not set final io.opentelemetry.sdk.resources.Resource field io.opentelemetry.sdk.logs.LoggerSharedState.resource to java.lang.reflect.Field
        // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Field.html#set(java.lang.Object,java.lang.Object)
        set(this, value)
    }