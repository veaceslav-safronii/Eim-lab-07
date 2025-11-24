package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var chatArrayAdapter: ArrayAdapter<String>
    private var chatMessages = mutableListOf<String>()

    private var selectedDevice: BluetoothDevice? = null

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var listDevicesButton: Button

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    companion object {
        val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSIONS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO 1: Initialize views
        initViews()

        // TODO 2: Initialize Bluetooth
        initBluetooth()

        // TODO 3: Request permissions
        checkPermissions()

        // TODO 6: Start server socket for incoming connections
        startServer()

        // TODO 7: Send message using ConnectedThread
        sendMessage()
    }

    // TODO 1: Implement the method that initializes the views
    private fun initViews() {
        // TODO 1: Implement the method that initializes the views
        // Conectăm componentele din XML la codul Kotlin
        val chatListView: ListView = findViewById(R.id.chatListView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        listDevicesButton = findViewById(R.id.listDevicesButton)

        // Setăm un eveniment pentru butonul de listare a dispozitivelor
        listDevicesButton.setOnClickListener { listPairedDevices() }

        // Pregătim o listă pentru mesaje
        chatMessages = ArrayList()

        // Creăm un adaptor care transformă lista noastră de mesaje în elemente vizuale
        chatArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatMessages)

        // Schimbarile din adaptor se vor vedea imediat in interfata grafica. In acest exemplue, chatListView de tip ListView va fi actualizata cand facem schimbari la chatArrayAdapter
        chatListView.adapter = chatArrayAdapter
    }


    // TODO 2: Implement the method that initializes Bluetooth
    private fun initBluetooth() {
        // Vom lua o referinta la adaptorul de bluetooth
        // de pe telefon. Putem vedea acest adaptor ca o interfata
        // cu driver-ul de bluetooth.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Verificăm dacă Bluetooth este disponibil
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available.", Toast.LENGTH_LONG).show()
            finish() // Închidem aplicația dacă Bluetooth nu este disponibil
        }
    }


    // TODO 3: Implement the method that checks permissions
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        // Pentru Android 12 sau mai nou, folosim permisiuni specifice Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // Pentru versiuni mai vechi, accesul la locație este necesar
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Cerem permisiunile utilizatorului
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }

    // TODO 5: Implement the method that displays a dialog for selecting a paired device
    private fun listPairedDevices() {
        // TODO 5: Implement the method that displays a dialog for selecting a paired device
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Obținem dispozitivele împerecheate
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        val deviceList = mutableListOf<String>()
        val devices = mutableListOf<BluetoothDevice>()

        // Le adaugam in lista
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                deviceList.add(device.name + "\n" + device.address)
                devices.add(device)
            }
        }

        // Afișăm dispozitivele într-un dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Device")

        val deviceArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)

        // Tratam situatia in care un dispozitiv este apasat in cadrul dialogului
        builder.setAdapter(deviceArrayAdapter) { dialog, which ->
            selectedDevice = devices[which]

            // deschidem un thread de comunicare
            connectThread = ConnectThread(this, bluetoothAdapter, selectedDevice, MY_UUID)
            connectThread?.start()
        }

        builder.show()
    }


    private fun startServer() {
        // TODO 6: Implement server socket to listen for incoming connections
        // Inițializăm AcceptThread, care va gestiona conexiunile primite
        acceptThread = AcceptThread(this, bluetoothAdapter, MY_UUID)
        acceptThread?.start()
    }

    private fun sendMessage() {
        // TODO 7: Implement the method that sends a message to the connected device
        sendButton.setOnClickListener { v ->
            // Preluăm mesajul introdus de utilizator
            val message = messageEditText.text.toString()

            // Verificăm dacă mesajul nu este gol și conexiunea este activă
            if (message.isNotEmpty() && connectedThread != null) {

                // Trimitem mesajul ca un array de bytes
                connectedThread?.write(message.toByteArray())

                // Golește câmpul de text
                messageEditText.setText("")

                // Adaugă mesajul trimis în interfață
                addChatMessage("Me: $message")
            }
        }
    }

    // Update the UI with a new message
    fun addChatMessage(message: String) {
        chatMessages.add(message)
        chatArrayAdapter.notifyDataSetChanged()
    }

    // TODO 4: Handle permission results and activate Bluetooth programmatically
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // TODO 4: handle permission results
        if (requestCode == REQUEST_PERMISSIONS) {
            var permissionGranted = true

            // Verificăm dacă toate permisiunile au fost acordate
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false
                    break
                }
            }

            if (!permissionGranted) {
                Toast.makeText(this, "Permissions required for Bluetooth operation.", Toast.LENGTH_LONG).show()
                finish()
            }

            // Activăm Bluetooth dacă nu este deja activat
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    fun manageConnectedSocket(socket: BluetoothSocket) {
        connectedThread?.cancel()
        connectedThread = ConnectedThread(this, socket).apply { start() }
    }

    // TODO 4: Handle Bluetooth enable result
    // Handle Bluetooth enable result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
            Toast.makeText(this, "Bluetooth must be enabled to continue.", Toast.LENGTH_LONG).show()
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO 8: cleanup threads
        // Închidem AcceptThread dacă rulează
        acceptThread?.cancel()

        // Închidem ConnectThread dacă rulează
        connectThread?.cancel()

        // Închidem ConnectedThread dacă rulează
        connectedThread?.cancel()
    }
}
