package com.example.pigeonpal;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * SocketManager - A Singleton class to handle TCP socket connections.
 * It manages a background thread to listen for incoming data and provides
 * methods to send commands back to the server.
 */
public class SocketManager {

    // Singleton instance
    private static SocketManager instance;
    
    // List of active listeners (Activities) to be notified of events
    private final List<SocketListener> listeners = new ArrayList<>();
    
    // Background thread for non-blocking network I/O
    private Thread connectionThread;
    
    // The underlying TCP socket
    private Socket socket;
    
    // Connection state flag
    private boolean isRunning = false;
    
    // Handler to post results back to the Main (UI) Thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Buffered reader for incoming stream
    private BufferedReader in;
    
    // Output stream for sending data to server
    private DataOutputStream out;

    // Private constructor for Singleton pattern
    private SocketManager() {}

    /**
     * Returns the global instance of the SocketManager.
     */
    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * Interface for components to receive socket state updates and data.
     */
    public interface SocketListener {
        void onSocketConnected();
        void onSocketDisconnected();
        // Received parsed message with sensor reading and device states
        void onMessageReceived(float temp, Boolean lamp, Boolean light, Boolean radio);
        void onError(Exception e);
    }

    /**
     * Registers a new listener for socket events.
     */
    public void addListener(SocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a listener (usually called in onPause or onDestroy).
     */
    public void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns true if the socket is currently connected and running.
     */
    public boolean isConnected() {
        return isRunning && socket != null && socket.isConnected();
    }

    /**
     * Sends a raw string command to the server on a new background thread.
     * @param data The command string (e.g., "L1\n")
     */
    public void sendData(String data) {
        if (out != null && isRunning) {
            new Thread(() -> {
                try {
                    out.writeBytes(data);
                    out.flush(); // Ensure data is actually sent immediately
                } catch (IOException e) {
                    notifyError(e);
                }
            }).start();
        }
    }

    /**
     * Establishes a TCP connection and starts the listening loop.
     * @param host Server IP address
     * @param port Server port number
     */
    public void startConnection(String host, int port) {
        if (isRunning) return; // Prevent duplicate connections
        isRunning = true;

        connectionThread = new Thread(() -> {
            try {
                // Resolve host and connect
                InetAddress inet = InetAddress.getByName(host);
                socket = new Socket(inet, port);
                
                // Initialize I/O streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                
                notifyConnected();

                String line;
                // Continuously read lines from the server
                while (isRunning && (line = in.readLine()) != null) {
                    try {
                        // Expected format: "temperature,lampState,lightState,radioState"
                        String[] parts = line.split(",");
                        
                        // Parse temperature and store in DataManager
                        float temp = Float.parseFloat(parts[0].trim());
                        DataManager.getInstance().addReading(temp);
                        
                        // Parse device states as booleans
                        Boolean lamp = Boolean.parseBoolean(parts[1].trim());
                        Boolean light = Boolean.parseBoolean(parts[2].trim());
                        Boolean radio = Boolean.parseBoolean(parts[3].trim());
                        
                        // Push data to all active listeners
                        notifyMessageReceived(temp, lamp, light, radio);
                    } catch (Exception e) {
                        // Log and skip malformed data lines
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    notifyError(e);
                }
            } finally {
                stopConnection(); // Ensure cleanup on thread exit
            }
        });
        
        connectionThread.start();
    }

    /**
     * Closes the connection and releases all resources.
     */
    public void stopConnection() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (connectionThread != null) {
            connectionThread.interrupt();
            connectionThread = null;
        }
        
        notifyDisconnected();
    }

    // Helper methods to execute callbacks on the UI Thread
    private void notifyConnected() {
        mainHandler.post(() -> {
            for (SocketListener l : new ArrayList<>(listeners)) l.onSocketConnected();
        });
    }

    private void notifyDisconnected() {
        mainHandler.post(() -> {
            for (SocketListener l : new ArrayList<>(listeners)) l.onSocketDisconnected();
        });
    }

    private void notifyMessageReceived(float temp, Boolean lamp, Boolean light, Boolean radio) {
        mainHandler.post(() -> {
            for (SocketListener l : new ArrayList<>(listeners)) l.onMessageReceived(temp, lamp, light, radio);
        });
    }

    private void notifyError(Exception e) {
        mainHandler.post(() -> {
            for (SocketListener l : new ArrayList<>(listeners)) l.onError(e);
        });
    }
}
