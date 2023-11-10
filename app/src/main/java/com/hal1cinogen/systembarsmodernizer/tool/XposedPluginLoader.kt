package com.hal1cinogen.systembarsmodernizer.tool

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.hal1cinogen.systembarsmodernizer.BuildConfig
import com.hal1cinogen.systembarsmodernizer.tool.ReflectionUtils.findMethod
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException

object XposedPluginLoader {
    private val sPluginCache: MutableMap<Class<*>, Any> = HashMap()

    @Throws(Exception::class)
    fun load(pluginClz: Class<*>, context: Context, lpparam: LoadPackageParam) {
        var pluginObj: Any? = sPluginCache[pluginClz]
        if (pluginObj == null) {
            synchronized(pluginClz) {
                pluginObj = sPluginCache[pluginClz]
                if (pluginObj == null) {
                    val temp = loadFromLocal(pluginClz)
                    pluginObj = temp
                    sPluginCache[pluginClz] = temp
                }
            }
        }
        pluginObj?.let { callPluginMain(it, context, lpparam) }

    }

    @Throws(Exception::class)
    private fun loadFromDex(context: Context, pluginClz: Class<*>): Any? {
        val apkFile = getModuleApkFile(context)
        val odexDir = context.cacheDir
        val xposedClassLoader = XposedPluginLoader::class.java.classLoader ?: return null
        hijackDexElements(xposedClassLoader, apkFile, odexDir)
        val findClzMethod =
            BaseDexClassLoader::class.java.getDeclaredMethod("findClass", String::class.java)
        findClzMethod.isAccessible = true
        val clz = findClzMethod.invoke(xposedClassLoader, pluginClz.name) as Class<*>
        return clz.newInstance()
    }

    private fun getModuleApkFile(context: Context): File {
        try {
            val info = context.applicationContext.packageManager.getApplicationInfo(
                BuildConfig.APPLICATION_ID,
                0
            )
            return File(info.sourceDir)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return File("/data/local/tmp/" + BuildConfig.APPLICATION_ID + ".apk")
    }

    const val COLUMN_KEY = "key"
    const val COLUMN_TYPE = "type"
    const val COLUMN_VALUE = "value"
    private fun queryAll(baseUri: Uri, context: Context): Map<String, Any> {
        val uri = baseUri.buildUpon().appendPath("").build()
        val columns = arrayOf(COLUMN_KEY, COLUMN_TYPE, COLUMN_VALUE)
        val cursor = query(context, uri, columns)
        return try {
            val map = HashMap<String, Any>()
            if (cursor == null) {
                return map
            }
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                map[name] = getValue(cursor, 1, 2)
            }
            map
        } finally {
            cursor?.close()
        }
    }

    private fun getValue(cursor: Cursor, typeCol: Int, valueCol: Int): Any {
        return when (val expectedType = cursor.getInt(typeCol)) {
            RemoteContract.TYPE_STRING -> cursor.getString(valueCol)
            RemoteContract.TYPE_STRING_SET -> cursor.getString(valueCol)
            RemoteContract.TYPE_INT -> cursor.getInt(valueCol)
            RemoteContract.TYPE_LONG -> cursor.getLong(valueCol)
            RemoteContract.TYPE_FLOAT -> cursor.getFloat(valueCol)
            RemoteContract.TYPE_BOOLEAN -> cursor.getInt(valueCol) != 0
            else -> throw AssertionError("Invalid expected type: $expectedType")
        }
    }

    private fun query(context: Context, uri: Uri, columns: Array<String>): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, columns, null, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cursor
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun hijackDexElements(classLoader: ClassLoader, apkFile: File, odexFile: File) {
        try {
            val pathListField = BaseDexClassLoader::class.java.getDeclaredField("pathList")
            pathListField.isAccessible = true
            val pathList = pathListField[classLoader] ?: return
            val dexPathListClass: Class<*> = pathList.javaClass
            val dexElementsField = dexPathListClass.getDeclaredField("dexElements")
            dexElementsField.isAccessible = true
            val dexElements = dexElementsField[pathList] as Array<*>
            for (dexElement in dexElements) {
                Log.d("Naughty", "dexElement $dexElement")
            }
            val suppressedExceptions = ArrayList<IOException>()
            val apkFileList = ArrayList<File>()
            apkFileList.add(apkFile)
            val apkDexElements =
                makePathElements(pathList, apkFileList, odexFile, suppressedExceptions)
            if (suppressedExceptions.size > 0) {
                for (e in suppressedExceptions) {
                    e.printStackTrace()
                    Log.e("Naughty", "Exception in makePathElements", e)
                }
            }
            dexElementsField[pathList] = apkDexElements
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(
        IllegalAccessException::class,
        InvocationTargetException::class,
        NoSuchMethodException::class
    )
    private fun makePathElements(
        dexPathList: Any, files: ArrayList<File>, optimizedDirectory: File,
        suppressedExceptions: ArrayList<IOException>
    ): Array<Any> {
        val makeDexElements = findMethod(
            dexPathList, "makePathElements", MutableList::class.java, File::class.java,
            MutableList::class.java
        )
        return makeDexElements.invoke(
            dexPathList, files, optimizedDirectory,
            suppressedExceptions
        ) as Array<Any>
    }

    @Throws(
        ClassNotFoundException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class,
        IllegalAccessException::class,
        InstantiationException::class
    )
    private fun loadFromLocal(pluginClz: Class<*>): Any {
        return pluginClz.newInstance()
    }

    @Throws(
        ClassNotFoundException::class,
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InstantiationException::class,
        InvocationTargetException::class
    )
    private fun callPluginMain(pluginObj: Any, context: Context, lpparam: LoadPackageParam) {
        val method = pluginObj.javaClass.getDeclaredMethod(
            "main",
            Context::class.java,
            LoadPackageParam::class.java
        )
        method.invoke(pluginObj, context, lpparam)
    }

    private fun forceClassLoaderReloadClasses(
        classLoader: ClassLoader,
        packageNameStartWith: String,
        apkPath: String
    ): Boolean {
        var temp = packageNameStartWith
        try {
            val findClzMethod =
                BaseDexClassLoader::class.java.getDeclaredMethod("findClass", String::class.java)
            findClzMethod.isAccessible = true
            temp = "$packageNameStartWith."
            val dexFile = DexFile(apkPath)
            val classNames = dexFile.entries()
            while (classNames.hasMoreElements()) {
                val className = classNames.nextElement()
                if (className.startsWith(temp)) {
                    try {
                        findClzMethod.invoke(classLoader, className)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    object RemoteContract {
        const val COLUMN_KEY = "key"
        const val COLUMN_TYPE = "type"
        const val COLUMN_VALUE = "value"
        val COLUMN_ALL = arrayOf(
            COLUMN_KEY,
            COLUMN_TYPE,
            COLUMN_VALUE
        )
        const val TYPE_NULL = 0
        const val TYPE_STRING = 1
        const val TYPE_STRING_SET = 2
        const val TYPE_INT = 3
        const val TYPE_LONG = 4
        const val TYPE_FLOAT = 5
        const val TYPE_BOOLEAN = 6
    }
}