package com.example.dataencryptionproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUserEmail: String? = null //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        currentUserEmail = auth.currentUser?.email

        val plainTextInput = findViewById<EditText>(R.id.plaintextEditText)
        val encryptedTextView = findViewById<TextView>(R.id.encryptedTextView)
        val encryptedInput = findViewById<EditText>(R.id.encryptedInputEditText)
        val decryptButton = findViewById<Button>(R.id.decryptButton)
        val decryptKeyInput = findViewById<EditText>(R.id.decryptKeyEditText)
        val decryptedTextView = findViewById<TextView>(R.id.decryptedTextView)
        val encryptButton = findViewById<Button>(R.id.encryptButton)



        encryptButton.setOnClickListener(){
            val plainText = plainTextInput.text.toString()

            if (plainText.isNotEmpty()){
                // Encrypt the plain text and generate a secret key
                val encryptedData = encrypt(this, plainText)
                val encodedEncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
                // Display encrypted text in the TextView
                encryptedTextView.text = encodedEncryptedData
                encryptedTextView.visibility = TextView.VISIBLE

                // Send secret key via email
                sendEmailWithEncryptedText(encodedEncryptedData)
            } else{
                Toast.makeText(this, "Please enter text to encrypt",Toast.LENGTH_LONG).show()
            }
        }

        decryptButton.setOnClickListener() {
            val encrypedStr = encryptedInput.text.toString()
            val key = decryptKeyInput.text.toString()

            if (encrypedStr.isNotEmpty() && key.isNotEmpty()){
                // decode the base 64-encoded encrypted string into byte Array
                val encryptedData = Base64.decode(encrypedStr, Base64.DEFAULT)
                val decryptedData = decrypt(this, encryptedData)

                decryptedTextView.text = String(decryptedData)
                decryptedTextView.visibility = TextView.VISIBLE
            }else {
                Toast.makeText(this, "Please enter both encrypted text and secret key", Toast.LENGTH_LONG).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun encrypt(context: Context, strToEncrypt: String): ByteArray {
        val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key = keygen.generateKey()
        saveSecretKey(context, key)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        saveInitializationVector(context, cipher.iv)
        return cipherText
    }

    private fun decrypt(context: Context, dataToDecrypt: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val ivSpec = IvParameterSpec(getSavedInitilizationVector(context))
        cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context), ivSpec)
        return  cipher.doFinal(dataToDecrypt)
    }

    fun saveSecretKey(context: Context, secretKey: SecretKey){
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(secretKey)
        val strToSave = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor  = sharedPref.edit()
        editor.putString("Secret_key", strToSave)
        editor.apply()
    }


    fun getSavedSecretKey(context: Context): SecretKey{
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val strSecretKey = sharedPref.getString("Secret_key", "")
        val bytes = Base64.decode(strSecretKey, Base64.DEFAULT)
        val ois =  ObjectInputStream(ByteArrayInputStream(bytes))
        return ois.readObject() as SecretKey
    }

    fun saveInitializationVector(context: Context, initializationVector: ByteArray){
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(initializationVector)
        val strToSave = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString("Initialization_vector", strToSave)
        editor.apply()
    }

    fun getSavedInitilizationVector(context: Context): ByteArray {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val strInitializationVector = sharedPref.getString("initialization_vector", "")
        val bytes = Base64.decode(strInitializationVector, Base64.DEFAULT)
        val ois = ObjectInputStream(ByteArrayInputStream(bytes))
        return  ois.readObject() as ByteArray
    }

    private fun sendEmailWithEncryptedText(encryptedText: String) {
        val subject = "Encrypted Text & Secret Key"
        val body = "Here is your encrypted text: $encryptedText"

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(currentUserEmail)) // User's email
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
