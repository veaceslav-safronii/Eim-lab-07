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
    private val chatMessages = mutableListOf<String>()

    private var selectedDevice: BluetoothDevice? = null
    private lateinit var messageEditText: EditText

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

        val chatListView: ListView = findViewById(R.id.chatListView)
        messageEditText = findViewById(R.id.messageEditText)
        val sendButton: Button = findViewById(R.id.sendButton)
        val listDevicesButton: Button = findViewById(R.id.listDevicesButton)

        chatArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatMessages)
        chatListView.adapter = chatArrayAdapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        requestPermissions()

        acceptThread = AcceptThread(this, bluetoothAdapter, MY_UUID).apply { start() }

        listDevicesButton.setOnClickListener { listPairedDevices() }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty() && connectedThread != null) {
                connectedThread?.write(message.toByteArray())
                messageEditText.text.clear()
                addChatMessage("Me: $message")
            }
        }
    }

    fun addChatMessage(message: String) {
        chatMessages.add(message)
        chatArrayAdapter.notifyDataSetChanged()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }

    private fun listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val pairedDevices = bluetoothAdapter.bondedDevices
        val deviceList = mutableListOf<String>()
        val devices = mutableListOf<BluetoothDevice>()

        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                deviceList.add("${device.name}\n${device.address}")
                devices.add(device)
            }
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Device")
            val deviceArrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, deviceList)
            setAdapter(deviceArrayAdapter) { _, which ->
                    selectedDevice = devices[which]
                connectThread = ConnectThread(this@MainActivity, bluetoothAdapter, selectedDevice!!, MY_UUID).apply { start() }
            }
            show()
        }
    }

    fun manageConnectedSocket(socket: BluetoothSocket) {
        connectedThread?.cancel()
        connectedThread = ConnectedThread(this, socket).apply { start() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions required for Bluetooth operation.", Toast.LENGTH_LONG).show()
                finish()
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
            Toast.makeText(this, "Bluetooth must be enabled to continue.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        acceptThread?.cancel()
        connectThread?.cancel()
        connectedThread?.cancel()
    }
}
