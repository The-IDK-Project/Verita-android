package io.theidkteam.verita.ui.login

import android.app.Activity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.theidkteam.verita.R
import io.theidkteam.verita.data.SettingsManager

@Destination(start = true)
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator? = null,
    onBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? io.theidkteam.verita.VeritaApp)?.settingsManager
    val state by viewModel.loginState.collectAsState()

    LoginScreenContent(
        state = state,
        onLogin = { hs, user, pass -> viewModel.login(hs, user, pass) },
        navigator = navigator,
        onBack = onBack,
        settingsManager = settingsManager
    )
}

@Composable
fun LoginScreenContent(
    state: LoginViewModel.LoginState,
    onLogin: (String, String, String) -> Unit,
    navigator: DestinationsNavigator?,
    onBack: () -> Unit = {},
    settingsManager: SettingsManager? = null
) {
    val context = LocalContext.current
    var homeserver by remember { mutableStateOf("https://matrix.org") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var showTelegramLogin by remember { mutableStateOf(false) }
    var showTelegramWebLogin by remember { mutableStateOf(false) }
    
    val uriHandler = LocalUriHandler.current

    if (showTelegramWebLogin) {
        TelegramWebViewLogin(
            loginUrl = null, // Передайте сюда URL, если у вас есть внешняя страница логина
            onDismiss = { showTelegramWebLogin = false },
            onAuthDataReceived = { data: Map<String, String> ->
                showTelegramWebLogin = false
                if (data.containsKey("access_token")) {
                    // Если перехвачен токен доступа
                    onLogin("https://matrix.org", "@tg_user:matrix.org", "token:${data["access_token"]}")
                } else {
                    // Стандартные данные виджета (id, hash)
                    onLogin("https://matrix.org", "@tg_${data["id"]}:matrix.org", "tg_web_${data["hash"]}")
                }
            }
        )
    } else if (showTelegramLogin) {
        TelegramLoginUI(
            onDismiss = { showTelegramLogin = false },
            onComplete = { identifier: String, code: String, pass: String ->
                // Handle bridge login with phone/email, code and 2FA password
                showTelegramLogin = false
                val sanitizedId = identifier.replace("+", "").replace("@", "_at_").replace(".", "_")
                onLogin("https://matrix.org", "@tg_$sanitizedId:matrix.org", "tg_${code}_${pass.hashCode()}")
            },
            onSwitchToWeb = {
                showTelegramLogin = false
                showTelegramWebLogin = true
            }
        )
    } else {
        val popularHomeservers = listOf(
            "https://matrix.org",
            "https://mozilla.org",
            "https://kde.org"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                var showLanguageMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showLanguageMenu = true }) {
                    Icon(Icons.Default.Language, contentDescription = "Language")
                }
                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.language_english)) },
                        onClick = {
                            settingsManager?.saveLanguage("en")
                            showLanguageMenu = false
                            (context as? Activity)?.recreate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.language_russian)) },
                        onClick = {
                            settingsManager?.saveLanguage("ru")
                            showLanguageMenu = false
                            (context as? Activity)?.recreate()
                        }
                    )
                }
            }

            Text(text = stringResource(R.string.login_welcome), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.login_select_homeserver),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularHomeservers) { server ->
                    FilterChip(
                        selected = homeserver == server,
                        onClick = { homeserver = server },
                        label = { Text(server.replace("https://", "")) }
                    )
                }
            }
            
            OutlinedTextField(
                value = homeserver,
                onValueChange = { homeserver = it },
                label = { Text("Homeserver") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is LoginViewModel.LoginState.Loading
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.login_username)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onLogin(homeserver, username, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is LoginViewModel.LoginState.Loading
            ) {
                if (state is LoginViewModel.LoginState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.login_button))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { 
                    val registerUrl = "$homeserver/_matrix/static/client/register/"
                    uriHandler.openUri(registerUrl)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.login_register))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showTelegramLogin = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0088CC),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LOGIN WITH TELEGRAM", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    homeserver = "https://matrix.org"
                    username = "test_user_verita"
                    password = "test_password_123"
                }
            ) {
                Text(stringResource(R.string.login_fill_test))
            }

            TextButton(
                onClick = {
                    if (navigator != null) {
                        navigator.navigate("room_list_screen")
                    } else {
                        onBack()
                    }
                }
            ) {
                Text(stringResource(R.string.login_skip))
            }
            
            if (state is LoginViewModel.LoginState.Error) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            LaunchedEffect(state) {
                if (state is LoginViewModel.LoginState.Success) {
                    if (navigator != null) {
                        navigator.navigate("room_list_screen")
                    } else {
                        onBack()
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramLoginUI(
    onDismiss: () -> Unit,
    onComplete: (String, String, String) -> Unit,
    onSwitchToWeb: () -> Unit = {}
) {
    var step by remember { mutableIntStateOf(1) } // 1: Phone, 2: Code, 3: Email, 4: EmailCode, 5: Password (2FA)
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = {
                when (step) {
                    1 -> onDismiss()
                    2 -> step = 1
                    3 -> step = 1
                    4 -> step = 3
                    5 -> if (email.isNotEmpty()) step = 4 else step = 2
                    else -> onDismiss()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Telegram Logo placeholder (Standard Circular)
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = Color(0xFF0088CC)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (step == 3 || step == 4) Icons.Default.Email else Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when(step) {
                1 -> "Your Phone"
                2 -> "Enter Code"
                3 -> "Your Email"
                4 -> "Email Code"
                else -> "Password"
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = when(step) {
                1 -> "Please confirm your country code and\nenter your phone number."
                2 -> "We've sent an SMS with an activation code to your phone $phoneNumber."
                3 -> "Please enter your email address to\nreceive an activation code."
                4 -> "We've sent a 6-digit activation code\nto $email."
                else -> "Your account is protected by an\nadditional password."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (step) {
            1 -> {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+1 234 567 89 00") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { if (phoneNumber.length > 5) step = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    enabled = phoneNumber.length >= 7
                ) {
                    Text("NEXT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = { 
                    onSwitchToWeb()
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Use Official Web Widget", color = Color(0xFF0088CC))
                }

                TextButton(onClick = { 
                    code = ""
                    step = 3 
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Login with Email instead", color = Color(0xFF0088CC))
                }
            }
            2 -> {
                OutlinedTextField(
                    value = code,
                    onValueChange = { newValue -> if (newValue.length <= 6) code = newValue },
                    label = { Text("Code") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("000 000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        if (code.length >= 5) {
                            step = 5 // Go to password
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    enabled = code.length >= 5
                ) {
                    Text("NEXT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { /* Handle resend */ }) {
                        Text("Resend SMS", color = Color(0xFF0088CC))
                    }
                    TextButton(onClick = { 
                        code = ""
                        step = 3 
                    }) {
                        Text("Use Email", color = Color(0xFF0088CC))
                    }
                }
            }
            3 -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("example@domain.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { if (email.contains("@")) step = 4 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    enabled = email.contains("@") && email.contains(".")
                ) {
                    Text("SEND CODE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = { 
                    email = ""
                    step = 1 
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Back to Phone", color = Color(0xFF0088CC))
                }
            }
            4 -> {
                OutlinedTextField(
                    value = code,
                    onValueChange = { newValue -> if (newValue.length <= 6) code = newValue },
                    label = { Text("Email Code") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("000 000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        if (code.length >= 5) {
                            step = 5 // Go to password
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    enabled = code.length >= 5
                ) {
                    Text("NEXT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { /* Handle resend */ }) {
                        Text("Resend Code", color = Color(0xFF0088CC))
                    }
                    TextButton(onClick = { 
                        code = ""
                        step = 3 
                    }) {
                        Text("Wrong email?", color = Color(0xFF0088CC))
                    }
                }
            }
            5 -> {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onComplete(if (email.isNotEmpty()) email else phoneNumber, code, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    enabled = password.isNotEmpty()
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            "Verita uses Matrix-Telegram bridge to connect you.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramWebViewLogin(
    loginUrl: String? = null,
    onDismiss: () -> Unit,
    onAuthDataReceived: (Map<String, String>) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Telegram Login") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: ""
                            
                            // 1. Перехват токена (из fragment # или query ?)
                            if (url.contains("access_token=")) {
                                val token = url.substringAfter("access_token=").substringBefore("&")
                                onAuthDataReceived(mapOf("access_token" to token))
                                return true
                            }

                            // 2. Перехват через специальную схему
                            if (url.startsWith("tgauth://")) {
                                val params = request?.url?.queryParameterNames?.associateWith {
                                    request.url.getQueryParameter(it) ?: ""
                                } ?: emptyMap()
                                onAuthDataReceived(params)
                                return true
                            }
                            return false
                        }
                    }
                    
                    if (loginUrl != null) {
                        loadUrl(loginUrl)
                    } else {
                        // Загружаем виджет по умолчанию
                        val html = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    body { display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f0f0f0; }
                                </style>
                            </head>
                            <body>
                                <script async src="https://telegram.org/js/telegram-widget.js?22" 
                                    data-telegram-login="VeritaBot" 
                                    data-size="large" 
                                    data-onauth="onTelegramAuth(user)" 
                                    data-request-access="write"></script>
                                <script type="text/javascript">
                                    function onTelegramAuth(user) {
                                        const params = new URLSearchParams(user).toString();
                                        window.location.href = 'tgauth://success?' + params;
                                    }
                                </script>
                            </body>
                            </html>
                        """.trimIndent()
                        loadDataWithBaseURL("https://telegram.org", html, "text/html", "UTF-8", null)
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenContent(
            state = LoginViewModel.LoginState.Idle,
            onLogin = { _, _, _ -> },
            navigator = null,
            onBack = {}
        )
    }
}
