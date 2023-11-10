package com.hal1cinogen.systembarsmodernizer.tool

import java.lang.reflect.Method

object ReflectionUtils {
    @JvmStatic
    @Throws(NoSuchMethodException::class)
    fun findMethod(instance: Any, name: String, vararg parameterTypes: Class<*>?): Method {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val method = clazz.getDeclaredMethod(name, *parameterTypes)
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {
                // ignore and search next
            }
            clazz = clazz.superclass
        }
        throw NoSuchMethodException(
            "Method " + name + " with parameters " +
                    listOf(*parameterTypes) + " not found in " + instance.javaClass
        )
    }
}