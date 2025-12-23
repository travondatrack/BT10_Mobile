package com.example.bai10

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bai10.R

class LoginActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // fixed: use the actual layout file "login.xml"

        db = DatabaseHelper(this)

        val edtUser = findViewById<EditText>(R.id.edtUsername)
        val edtPass = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val user = edtUser.text.toString()
            val pass = edtPass.text.toString()
            if(user.isNotEmpty() && pass.isNotEmpty()) {
                val userId = db.checkLogin(user, pass)
                if (userId != -1) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRegister.setOnClickListener {
            val user = edtUser.text.toString()
            val pass = edtPass.text.toString()
            if(user.isNotEmpty() && pass.isNotEmpty()) {
                val newUser = User(username = user, password = pass)
                if (db.registerUser(newUser)) {
                    Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Tên tài khoản đã tồn tại", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }
    }
}