package com.example.playtracker.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.playtracker.R
import com.example.playtracker.data.remote.dto.auth.RegisterRequestDto
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.ui.theme.AzulElectrico
import com.example.playtracker.ui.theme.TextoClaro
import com.example.playtracker.ui.theme.VioletaAcento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        if (email.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
            error = "Rellena todos los campos"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = "Email no válido"
            return false
        }
        if (password.length < 6) {
            error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        if (password != confirm) {
            error = "Las contraseñas no coinciden"
            return false
        }
        error = null
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de PlayTracker",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Repite la contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!validate()) return@Button
                    scope.launch(Dispatchers.IO) {
                        try {
                            isLoading = true
                            val response = RetrofitInstance.api.register(
                                RegisterRequestDto(
                                    email = email.trim(),
                                    username = username.trim(),
                                    password = password
                                )
                            )
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Cuenta creada. Inicia sesión para continuar.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack() // volver al login
                                } else {
                                    // Intenta extraer mensaje del backend; si no, mensaje generico
                                    error = when (response.code()) {
                                        400 -> "Email o usuario ya en uso"
                                        else -> "No se pudo registrar (${response.code()})"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                error = "Error de red: ${e.message}"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulElectrico,
                    contentColor = TextoClaro
                )
            ) {
                Text(
                    if (isLoading) "Creando cuenta..." else "Crear cuenta",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 8.dp),
                enabled = !isLoading
            ) {
                Text(
                    "¿Ya tienes cuenta? Inicia sesión",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
