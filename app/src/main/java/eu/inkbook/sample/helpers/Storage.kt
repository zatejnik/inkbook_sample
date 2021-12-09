package eu.inkbook.sample.helpers

import android.content.Context
import android.content.SharedPreferences

/** This is a Shared Preferences helper class, provides the easy and convenient access to prefs props */
class Storage(context: Context) {
    private val storage: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    companion object {
        val TAG = Storage::class.java.simpleName
    }

    fun contains(key: String): Boolean {
        return storage.contains(key)
    }

    fun getPrefs(): SharedPreferences {
        return storage
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return storage.getBoolean(key, default)
    }

    fun getFloat(key: String, default: Float): Float {
        return storage.getFloat(key, default)
    }

    fun getInt(key: String, default: Int): Int {
        return storage.getInt(key, default)
    }

    fun getLong(key: String, default: Long): Long {
        return storage.getLong(key, default)
    }

    fun getDouble(key: String, default: Double): Double {
        val lDefault = java.lang.Double.doubleToRawLongBits(default)
        return java.lang.Double.longBitsToDouble(storage.getLong(key, lDefault))
    }

    fun getString(key: String, default: String): String {
        return storage.getString(key, default) ?: ""
    }

    fun getStringSet(key: String, default: Set<String>): MutableSet<String>? {
        return storage.getStringSet(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        val storageEditor = storage.edit()
        storageEditor.putBoolean(key, value)
        storageEditor.commit()
    }

    fun putDouble(key: String, value: Double) {
        val storageEditor = storage.edit()
        storageEditor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        storageEditor.commit()
    }

    fun putFloat(key: String, value: Float) {
        val storageEditor = storage.edit()
        storageEditor.putFloat(key, value)
        storageEditor.commit()
    }

    fun putInt(key: String, value: Int) {
        val storageEditor = storage.edit()
        storageEditor.putInt(key, value)
        storageEditor.commit()
    }

    fun putLong(key: String, value: Long) {
        val storageEditor = storage.edit()
        storageEditor.putLong(key, value)
        storageEditor.commit()
    }

    fun putString(key: String, value: String) {
        val storageEditor = storage.edit()
        storageEditor.putString(key, value)
        storageEditor.commit()
    }

    fun putStringSet(key: String, value: Set<String>) {
        val storageEditor = storage.edit()
        storageEditor.putStringSet(key, value)
        storageEditor.commit()
    }

    fun remove(key: String) {
        val storageEditor = storage.edit()
        storageEditor.remove(key)
        storageEditor.commit()
    }

    fun clear() {
        val storageEditor = storage.edit()
        storageEditor.clear()
        storageEditor.commit()
    }
}