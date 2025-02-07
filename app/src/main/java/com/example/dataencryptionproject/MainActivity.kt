package com.example.dataencryptionproject

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class MainActivity : AppCompatActivity() {
    private var isKeyVisible = false
    private var isDecryptedVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val plainTextInput = findViewById<EditText>(R.id.plaintext)
        val keyInput = findViewById<EditText>(R.id.key)
        val encryptedText = findViewById<TextView>(R.id.encryptedText)
        val encryptedInput = findViewById<EditText>(R.id.encryptedInput)
        val decryptKeyInput = findViewById<EditText>(R.id.decryptKey)
        val decryptedText = findViewById<TextView>(R.id.decryptKey)
        val encryptButton = findViewById<Button>(R.id.encryptButton)
        val decryptButton = findViewById<Button>(R.id.decryptButton)
        val toggleKeyVisibility = findViewById<ImageView>(R.id.toggleKeyVisibility)
        val toggleDecryptKeyVisibility = findViewById<ImageView>(R.id.toggleDecryptKeyVisibility)

        //Toggle password visibility for the encryption key input
        toggleKeyVisibility.setOnClickListener(){
            isKeyVisible = !isKeyVisible
            togglePasswordVisibility(keyInput, toggleKeyVisibility, isKeyVisible)
        }

        toggleDecryptKeyVisibility.setOnClickListener(){
            isDecryptedVisible = !isDecryptedVisible
            togglePasswordVisibility(decryptKeyInput, toggleDecryptKeyVisibility, isDecryptedVisible)
        }

        encryptButton.setOnClickListener(){
            val plainText = plainTextInput.text.toString()
            val key = keyInput.text.toString()

            if (plainText.isNotEmpty() && key.isNotEmpty()){
                val encryptedData = encrypt(this, plainText)
                val encodedEncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
                encryptedText.text = encodedEncryptedData
            }else{
                Toast.makeText(this, "Please enter both PlainText and key", Toast.LENGTH_LONG).show()
            }
        }

        decryptButton.setOnClickListener() {
            val encryptedStr = encryptedInput.text.toString()
            val Key = decryptKeyInput.text.toString()

            if (encryptedStr.isNotEmpty() && Key.isNotEmpty()) {
                // Decode the Base 64-encoded encrypted sting into a ByteArray
                val encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT)
                // decrypt the data and display the plain text
                val decryptedData = decrypt(this, encryptedData)
                decryptedText.text = String(decryptedData)
            } else {
                Toast.makeText(this, "Please enter both Encrypted Text and key", Toast.LENGTH_LONG).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun togglePasswordVisibility(editText : EditText?, toggleIcon: ImageView?, isVisible: Boolean) {
        if (isVisible){
            // show the password
            if (editText != null) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            if (toggleIcon != null) {
                toggleIcon.setImageResource(R.drawable.ic_visibility_on)
            } // change the icon to "visible"
        } else{
            // hide the password
            if (editText != null) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            if (toggleIcon != null) {
                toggleIcon.setImageResource(R.drawable.ic_visibility_off)
            }
        }

        // Move the cursor to the end of the text
        if (editText != null) {
            editText.setSelection(editText.text.length)
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
}
