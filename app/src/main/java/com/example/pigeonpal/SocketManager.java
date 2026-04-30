package com.example.pigeonpal;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * SocketManager - A Singleton class to handle TCP socket connections.
 * It manages a background thread to listen for incoming data and
 * communicates with the UI thread via a Listener interface.
 */
public class SocketManager {

    // The single instance of SocketManager (Singleton Pattern)
    private static SocketManager instance;
    
    // The listener that will receive updates (usually MainActivity)
    private SocketListener listener;
    
    // Background thread for network operations
    private Thread connectionThread;
    
    // The actual TCP socket used for communication
    private Socket socket;
    
    // Control flag to keep the connection running
    private boolean isRunning = false;
    
    // Handler used to post updates back to the Main (UI) Thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private BufferedReader scanner;

    /**
     * Private constructor to prevent multiple instances from being created.
     */
    private SocketManager() {}

    /**
     * Returns the single instance of SocketManager.
     * Uses the Singleton pattern to ensure only one connection exists across the entire app.
     * 
     * @return The singleton instance of SocketManager.
     */
    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * Interface for activities to listen for socket events.
     * Any class that wants updates (like MainActivity) must implement this.
     */
    public interface SocketListener {
        // Called when the connection is successfully established
        void onSocketConnected();
        
        // Called when the connection is closed
        void onSocketDisconnected();
        
        // Called when a new piece of data is received from the server
        void onMessageReceived(float message);
        
        // Called when an error occurs during connection or data reading
        void onError(Exception e);
    }

    /**
     * Sets the listener for socket events.
     * 
     * @param listener The listener implementation to receive updates.
     */
    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    /**
     * Starts a background thread to connect to the server and listen for data.
     * 
     * @param host The IP address or hostname of the server.
     * @param port The port number to connect to.
     */
    public void startConnection(String host, int port) {
        // Don't start a new connection if one is already running
        if (isRunning) return;
        isRunning = true;

        // All network operations MUST happen on a background thread in Android
        connectionThread = new Thread(() -> {
            try {
                // 1. Resolve hostname and create a socket connection to the server
                InetAddress inet = InetAddress.getByName(host);
                socket = new Socket(inet, port);
                
                // 2. Notify the UI that we are connected
                notifyConnected();

                // 3. Get the input stream to read data from the server
                scanner = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 4. Continuously read data while the connection is active
                String line;
                while (isRunning && (line = scanner.readLine()) != null) {
                    try {
                        float message = Float.parseFloat(line.trim());
                        if (listener != null) {
                            // Send the individual reading to the UI thread
                            notifyMessageReceived(message);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid data
                    }
                }
            } catch (IOException e) {
                // If an error occurs while running, notify the listener
                if (isRunning) {
                    notifyError(e);
                }
            } finally {
                // Ensure the connection is cleaned up when the thread exits
                stopConnection();
            }
        });
        
        // Start the background thread
        connectionThread.start();
    }

    /**
     * Stops the connection, closes the socket, and shuts down the background thread.
     */
    public void stopConnection() {
        isRunning = false;
        try {
            // Close the socket to stop blocking I/O operations
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Interrupt the thread if it's currently sleeping or blocking
        if (connectionThread != null) {
            connectionThread.interrupt();
            connectionThread = null;
        }
        
        // Notify the UI that we are disconnected
        notifyDisconnected();
    }

    // ============================================================
    // NOTIFICATION HELPERS
    // All of these use mainHandler.post() to ensure the callback
    // runs on the UI Thread, which is required for UI updates.
    // ============================================================

    /**
     * Posts a connection notification to the UI thread.
     */
    private void notifyConnected() {
        mainHandler.post(() -> {
            if (listener != null) listener.onSocketConnected();
        });
    }

    /**
     * Posts a disconnection notification to the UI thread.
     */
    private void notifyDisconnected() {
        mainHandler.post(() -> {
            if (listener != null) listener.onSocketDisconnected();
        });
    }

    /**
     * Posts a received message notification to the UI thread.
     */
    private void notifyMessageReceived(float message) {
        mainHandler.post(() -> {
            if (listener != null) listener.onMessageReceived(message);
        });
    }

    /**
     * Posts an error notification to the UI thread.
     */
    private void notifyError(Exception e) {
        mainHandler.post(() -> {
            if (listener != null) listener.onError(e);
        });
    }
}
