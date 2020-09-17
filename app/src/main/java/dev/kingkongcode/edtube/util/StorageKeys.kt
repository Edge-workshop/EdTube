package dev.kingkongcode.edtube.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StorageKeys<T>(private val key: String, private val defaultValue: T, private val getter: SharedPreferences.(String, T) -> T,
                     private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor) : ReadWriteProperty<Context, T>{


    override fun getValue(thisRef: Context, property: KProperty<*>): T {
        TODO("Not yet implemented")
    }

    override fun setValue(thisRef: Context, property: KProperty<*>, value: T) {
        TODO("Not yet implemented")
    }
}