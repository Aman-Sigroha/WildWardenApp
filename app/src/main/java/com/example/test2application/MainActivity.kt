package com.example.test2application

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.test2application.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.any { !it.value }) {
                Toast.makeText(this, "Bluetooth and location permissions are required", Toast.LENGTH_LONG).show()
            }
        }

        // Request all necessary permissions
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            Test2ApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(rescueGradient)
                ) {
                    // Add animated tech background
                    AnimatedTechBackground(
                        modifier = Modifier.blur(4.dp)
                    )
                    
                    // Main content
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        RescueTechApp()
                    }
                }
            }
        }
    }

    @Composable
    fun RescueTechApp() {
        var isConnected by remember { mutableStateOf(false) }
        var sensorData by remember { mutableStateOf<SensorData?>(null) }
        var showConnectionPane by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with parallax effect
            RescueTechHeader(isConnected)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Main content with animations
            AnimatedVisibility(
                visible = showConnectionPane && !isConnected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ConnectionPane(
                    onConnectClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            connectToESP32()
                            isConnected = bluetoothSocket?.isConnected == true
                            if (isConnected) {
                                showConnectionPane = false
                                // Start listening for data after connection
                                startDataListener(coroutineScope) { data ->
                                    sensorData = data
                                }
                            }
                        }
                    }
                )
            }
            
            AnimatedVisibility(
                visible = !showConnectionPane || isConnected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SensorDataDisplay(
                    sensorData = sensorData,
                    onSendDataClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            sendDataSignal()
                        }
                    },
                    onDisconnectClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            disconnectFromESP32()
                            isConnected = false
                            showConnectionPane = true
                            sensorData = null
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun RescueTechHeader(isConnected: Boolean) {
        val scrollEffect = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            scrollEffect.animateTo(
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .graphicsLayer {
                    translationY = scrollEffect.value
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.horizontalGradient(
                        colors = listOf(
                            DeepBlue.copy(alpha = 0.8f),
                            CyberPurple.copy(alpha = 0.9f),
                            TechDark.copy(alpha = 0.8f)
                        )
                    ))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RescueTech",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Connection status indicator with pulse animation
                    val pulseAnim = rememberInfiniteTransition()
                    val scale by pulseAnim.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isConnected) 1.2f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    // Add glow effect to the status indicator
                    val glowRadius by pulseAnim.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isConnected) 15f else 5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(if (isConnected) SafetyGreen else RescueRed)
                            .border(1.dp, Color.White, CircleShape)
                            .shadow(glowRadius.dp, CircleShape, spotColor = if (isConnected) SafetyGreen else RescueRed)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Status text with color animation
                    val statusColor by animateColorAsState(
                        targetValue = if (isConnected) SafetyGreen else RescueRed,
                        animationSpec = tween(500)
                    )
                    
                    Text(
                        text = if (isConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun ConnectionPane(onConnectClick: () -> Unit) {
        FloatingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title with typing animation
                var visibleChars by remember { mutableStateOf(0) }
                val titleText = "Connect to Rescue Device"
                
                LaunchedEffect(Unit) {
                    repeat(titleText.length) {
                        delay(50)
                        visibleChars = it + 1
                    }
                }
                
                Text(
                    text = titleText.take(visibleChars),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "This will connect to your ESP32 device and start receiving vital sensor data for emergency response.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Shiny connect button with animations
                val connectButtonAnim = remember { Animatable(1f) }
                val shine = rememberInfiniteTransition()
                val shineOffset by shine.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                // Wiggle animation to draw attention
                val wiggle by shine.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(300, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                LaunchedEffect(Unit) {
                    connectButtonAnim.animateTo(
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
                
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier
                        .scale(connectButtonAnim.value)
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                        .graphicsLayer {
                            rotationZ = wiggle * 0.5f
                            translationX = shineOffset * 0.5f
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RescueBlue
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connect",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun SensorDataDisplay(
        sensorData: SensorData?,
        onSendDataClick: () -> Unit,
        onDisconnectClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // If no data yet, show cool loading animation
            if (sensorData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val rotation = rememberInfiniteTransition()
                    val rotationAngle by rotation.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing)
                        )
                    )
                    
                    val pulseSize by rotation.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    // Background glow
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseSize * 1.2f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        RescueBlue.copy(alpha = 0.5f),
                                        RescueBlue.copy(alpha = 0.1f),
                                        RescueBlue.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                    
                    // Main spinner
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            },
                        color = RescueBlue,
                        strokeWidth = 6.dp
                    )
                    
                    // Inner spinner
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                rotationZ = -rotationAngle
                            },
                        color = ElectricBlue,
                        strokeWidth = 4.dp
                    )
                    
                    // Text with typing animation
                    var loadingText by remember { mutableStateOf("") }
                    val fullText = "Scanning for vital signs..."
                    var textIndex by remember { mutableStateOf(0) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(100)
                            textIndex = (textIndex + 1) % (fullText.length + 1)
                            loadingText = fullText.take(textIndex) + if (textIndex < fullText.length) "_" else ""
                        }
                    }
                    
                    Text(
                        text = loadingText,
                        modifier = Modifier.padding(top = 120.dp),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Display sensor data in animated cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Heart rate with pulsing animation
                    SensorCardWithAnimation(
                        title = "Heart Rate",
                        value = "${sensorData.heartRate} BPM",
                        icon = Icons.Default.Favorite,
                        color = RescueRed,
                        modifier = Modifier.weight(1f),
                        animationType = AnimationType.PULSE
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Temperature with floating animation
                    SensorCardWithAnimation(
                        title = "Temperature",
                        value = "${sensorData.temperature} °C",
                        icon = Icons.Default.Warning,
                        color = AlertOrange,
                        modifier = Modifier.weight(1f),
                        animationType = AnimationType.FLOAT
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location card with tilt effect
                SensorCardWithAnimation(
                    title = "Location",
                    value = "Lat: ${sensorData.location.latitude.format(4)}\nLon: ${sensorData.location.longitude.format(4)}",
                    icon = Icons.Default.LocationOn,
                    color = TechLight,
                    modifier = Modifier.fillMaxWidth(),
                    animationType = AnimationType.TILT
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Accelerometer with float animation
                SensorCardWithAnimation(
                    title = "Accelerometer",
                    value = "X: ${sensorData.acceleration.x.format(2)}\nY: ${sensorData.acceleration.y.format(2)}\nZ: ${sensorData.acceleration.z.format(2)}",
                    icon = Icons.Default.Star,
                    color = SafetyGreen,
                    modifier = Modifier.fillMaxWidth(),
                    animationType = AnimationType.FLOAT
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Device ID with pulse animation
                SensorCardWithAnimation(
                    title = "Device ID",
                    value = sensorData.deviceId,
                    icon = Icons.Default.Info,
                    color = TechDark,
                    modifier = Modifier.fillMaxWidth(),
                    animationType = AnimationType.PULSE
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Emergency button with dramatic effects
            val emergencyButtonAnim = remember { Animatable(1f) }
            val emergencyGlow = rememberInfiniteTransition()
            
            val glowAlpha by emergencyGlow.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            val buttonScale by emergencyGlow.animateFloat(
                initialValue = 1f, 
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Button(
                onClick = onSendDataClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(buttonScale)
                    .shadow(
                        elevation = 16.dp, 
                        spotColor = RescueRed.copy(alpha = glowAlpha),
                        ambientColor = RescueRed.copy(alpha = glowAlpha * 0.5f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                // Pulsating send icon
                val iconScale by emergencyGlow.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(iconScale)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SEND EMERGENCY DATA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Disconnect button with subtle animation
            OutlinedButton(
                onClick = onDisconnectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Disconnect",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    enum class AnimationType { PULSE, FLOAT, TILT }
    
    @Composable
    fun SensorCardWithAnimation(
        title: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color,
        modifier: Modifier = Modifier,
        animationType: AnimationType
    ) {
        when (animationType) {
            AnimationType.PULSE -> {
                PulseCard(
                    modifier = modifier,
                    pulseColor = color,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    SensorCardContent(title, value, icon, color)
                }
            }
            AnimationType.FLOAT -> {
                FloatingCard(
                    modifier = modifier,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    SensorCardContent(title, value, icon, color)
                }
            }
            AnimationType.TILT -> {
                TiltCard(
                    modifier = modifier,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    SensorCardContent(title, value, icon, color)
                }
            }
        }
    }
    
    @Composable
    fun SensorCardContent(
        title: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon
            val iconAnim = rememberInfiniteTransition()
            val iconAlpha by iconAnim.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // Background glow
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color.copy(alpha = 0.1f),
                                    color.copy(alpha = 0f)
                                )
                            )
                        )
                )
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = iconAlpha),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Animated value with typewriter effect
                var displayedValue by remember { mutableStateOf("") }
                LaunchedEffect(value) {
                    displayedValue = ""
                    value.forEachIndexed { index, char ->
                        delay(20)
                        displayedValue = value.take(index + 1)
                    }
                }
                
                Text(
                    text = displayedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    private fun connectToESP32() {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            runOnUiThread {
                Toast.makeText(this, "BLUETOOTH_CONNECT permission denied", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull {
            it.name.contains("ESP32", ignoreCase = true)
        }

        if (device == null) {
            runOnUiThread {
                Toast.makeText(this, "ESP32 not found in paired devices", Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket?.connect()

            outputStream = bluetoothSocket?.outputStream
            inputStream = bluetoothSocket?.inputStream

            runOnUiThread {
                Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            runOnUiThread {
                Toast.makeText(this, "Permission error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun disconnectFromESP32() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            
            runOnUiThread {
                Toast.makeText(this, "Disconnected from ESP32", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error disconnecting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendDataSignal() {
        try {
            val command = "SEND_EMERGENCY_DATA"
            outputStream?.write(command.toByteArray())
            runOnUiThread {
                Toast.makeText(this, "Emergency data signal sent!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Send failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDataListener(scope: kotlinx.coroutines.CoroutineScope, onDataReceived: (SensorData) -> Unit) {
        scope.launch(Dispatchers.IO) {
            while (bluetoothSocket?.isConnected == true) {
                try {
                    val data = receiveMessage()
                    if (data.isNotEmpty()) {
                        val sensorData = parseSensorData(data)
                        withContext(Dispatchers.Main) {
                            onDataReceived(sensorData)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(500) // Poll every 500ms
            }
        }
    }

    private fun receiveMessage(): String {
        return try {
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer) ?: return ""
            String(buffer, 0, bytes)
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseSensorData(rawData: String): SensorData {
        // For demo purposes, we're simulating data parsing
        // In a real app, we would parse JSON or another format from the ESP32
        return try {
            // Placeholder parsing logic - in real app, this would parse the actual data
            // For now, let's just create some mock data
            SensorData(
                deviceId = "ESP32-" + Random.nextInt(1000, 9999),
                heartRate = Random.nextInt(60, 100),
                temperature = 36.5f + Random.nextFloat() * 2,
                location = GpsLocation(
                    latitude = 37.7749 + Random.nextDouble(-0.01, 0.01),
                    longitude = -122.4194 + Random.nextDouble(-0.01, 0.01)
                ),
                acceleration = AccelerometerData(
                    x = Random.nextFloat() * 2 - 1,
                    y = Random.nextFloat() * 2 - 1,
                    z = Random.nextFloat() * 2 - 1
                )
            )
        } catch (e: Exception) {
            // Fallback to default values if parsing fails
            SensorData(
                deviceId = "ESP32-UNKNOWN",
                heartRate = 75,
                temperature = 36.5f,
                location = GpsLocation(0.0, 0.0),
                acceleration = AccelerometerData(0f, 0f, 0f)
            )
        }
    }
}

// Extension functions for formatting
private fun Double.format(digits: Int) = "%.${digits}f".format(this)
private fun Float.format(digits: Int) = "%.${digits}f".format(this)
