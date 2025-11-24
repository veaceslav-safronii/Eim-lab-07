package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> chatArrayAdapter;
    private List<String> chatMessages;

    private BluetoothDevice selectedDevice;

    private EditText messageEditText;
    private Button sendButton;
    private Button listDevicesButton;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_PERMISSIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO 1: Initialize views
        initViews();

        // TODO 2: Initialize Bluetooth
        initBluetooth();

        // TODO 3: Request permissions
        checkPermissions();

        // TODO 6: Start server socket for incoming connections
        startServer();

        // TODO 7: Send message using ConnectedThread
        sendMessage();
    }

    // TODO 1: Implement the method that initializes the views
    private void initViews() {
    }

    // TODO 2: Implement the method that initializes Bluetooth
    private void initBluetooth() {

    }

    // TODO 3: Implement the method that checks permissions
    private void checkPermissions() {

    }

    // TODO 5: Implement the method that displays a dialog for selecting a paired device
    private void listPairedDevices() {

    }

    // TODO 6: Implement server socket to listen for incoming connections
    private void startServer() {
    }

    // TODO 7: Implement the method that sends a message to the connected device
    private void sendMessage() {

    }

    // Update the UI with a new message
    public void addChatMessage(String message) {
        chatMessages.add(message);
        chatArrayAdapter.notifyDataSetChanged();
    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        connectedThread = new ConnectedThread(this, socket);
        connectedThread.start();
    }

    // TODO 4: Handle permission results and activate Bluetooth programmatically
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    // TODO 4: Handle Bluetooth enable result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // TODO 8: Clean up threads on activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
