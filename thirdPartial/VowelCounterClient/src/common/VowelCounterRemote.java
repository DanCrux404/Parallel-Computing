package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import model.VowelResult;

// RMI Remote Interface — shared between server and client
// This is the CONTRACT — what the server can ask clients to do
public interface VowelCounterRemote extends Remote {
    // Server calls this ONCE per client — sends ALL files at once
    // Minimal communication — one call, all files, all results back
    // Client handles its own threads internally — server doesn't care how
    //
    // filesContent → raw bytes of each file (server reads and sends)
    // fileNames    → just for logging/display purposes
    // returns      → one VowelResult per file

    VowelResult[] countFiles(byte[][] filesContent, String[] fileNames,
             int threadsPerNode)
            throws RemoteException;

    // Server calls this to check if client is alive
    // Useful before sending files — avoid sending to dead clients
    boolean ping() throws RemoteException;
}
