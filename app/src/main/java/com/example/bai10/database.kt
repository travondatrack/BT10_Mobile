package com.example.bai10

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TodoListApp.db"
        private const val DATABASE_VERSION = 2 // bumped because schema changed (added salt)

        // Table User
        const val TABLE_USER = "User"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_SALT = "salt"

        // Table Task
        const val TABLE_TASK = "Task"
        const val COL_TASK_ID = "id"
        const val COL_TASK_USER_ID = "user_id"
        const val COL_CONTENT = "content"
        const val COL_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = "CREATE TABLE $TABLE_USER ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USERNAME TEXT UNIQUE, $COL_PASSWORD TEXT, $COL_SALT TEXT)"
        val createTaskTable = "CREATE TABLE $TABLE_TASK ($COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TASK_USER_ID INTEGER, $COL_CONTENT TEXT, $COL_STATUS INTEGER)"

        db?.execSQL(createUserTable)
        db?.execSQL(createTaskTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")
        onCreate(db)
    }

    // --- Auth Logic ---
    fun registerUser(user: User): Boolean {
        val db = this.writableDatabase

        // generate salt and hash the password before storing
        val salt = generateSalt()
        val hashed = hashPassword(user.password, salt)

        val values = ContentValues().apply {
            put(COL_USERNAME, user.username)
            put(COL_PASSWORD, hashed)
            put(COL_SALT, salt)
        }
        val result = db.insert(TABLE_USER, null, values)
        return result != -1L
    }

    fun checkLogin(username: String, password: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COL_USER_ID, $COL_PASSWORD, $COL_SALT FROM $TABLE_USER WHERE $COL_USERNAME=?", arrayOf(username))
        return if (cursor.moveToFirst()) {
            // columns: 0 = id, 1 = password, 2 = salt
            val id = cursor.getInt(0)
            val storedHash = cursor.getString(1)
            val storedSalt = cursor.getString(2)
            cursor.close()

            val providedHash = hashPassword(password, storedSalt)
            if (storedHash == providedHash) id else -1
        } else {
            cursor.close()
            -1
        }
    }

    // --- Task Logic ---
    fun addTask(task: Task): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TASK_USER_ID, task.userId)
            put(COL_CONTENT, task.content)
            put(COL_STATUS, if (task.isCompleted) 1 else 0)
        }
        return db.insert(TABLE_TASK, null, values) != -1L
    }

    fun getAllTasks(userId: Int): ArrayList<Task> {
        val list = ArrayList<Task>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TASK WHERE $COL_TASK_USER_ID=?", arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                list.add(Task(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    content = cursor.getString(2),
                    isCompleted = cursor.getInt(3) == 1
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateTaskStatus(id: Int, isCompleted: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_STATUS, if (isCompleted) 1 else 0)
        }
        db.update(TABLE_TASK, values, "$COL_TASK_ID=?", arrayOf(id.toString()))
    }

    fun updateTaskContent(id: Int, newContent: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTENT, newContent)
        }
        db.update(TABLE_TASK, values, "$COL_TASK_ID=?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_TASK, "$COL_TASK_ID=?", arrayOf(id.toString()))
    }

    // ---------- Security helpers ----------
    private fun generateSalt(): String {
        val sr = SecureRandom()
        val salt = ByteArray(16)
        sr.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, saltBase64: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        digest.update(salt)
        val hashed = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashed, Base64.NO_WRAP)
    }
}