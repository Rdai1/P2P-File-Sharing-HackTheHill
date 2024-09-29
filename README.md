# Peer-to-Peer (P2P) File Sharing System - README

## Overview
This P2P file-sharing system is built using the TCP/IP networking stack. It facilitates file sharing between multiple nodes in a network. Each node is capable of both uploading and downloading chunks of files. The system ensures that files are split into 512-byte chunks and shared among at least 4 nodes, maintaining file integrity and correctness during the transfer process.

## Challenge Structure
The system was developed to meet the following challenge objectives:

- **File Chunking**: Files are divided into 512-byte chunks.
- **Peer-to-Peer Network**: The system involves a minimum of 4 nodes capable of uploading and downloading file chunks.
- **File Integrity**: The system verifies the integrity of the transferred files using SHA-256 hashing.
- **TCP/IP Communication**: All file transfers and communication between nodes are managed using TCP/IP.

## System Components
The system consists of several components to handle file chunking, peer discovery, file transfer, and integrity checks.

### 1. FileChunk Class
The `FileChunk` class handles individual file chunks. Each chunk has a chunk number and data (in bytes).

### 2. FileHandling Class
The `FileHandling` class provides functionality to chunk a file into 512-byte pieces and reassemble it.

- `chunkFile(String filePath)`: Splits a file into 512-byte chunks.
- `assembleFile(List<byte[]> chunks, String outputFilePath)`: Reassembles the chunks into a complete file.

### 3. HashUtil Class
The `HashUtil` class generates SHA-256 hash values to ensure file integrity.

- `generateFileHash(File file)`: Generates the hash of a given file.

### 4. Info Class
The `Info` class holds information about peers, including their ID, IP address, and port.

### 5. Node Class
The `Node` class represents each peer in the P2P network. It handles:

- Peer discovery and connection.
- Sending and receiving files.
- Maintaining a list of connected peers.
- File transfer and integrity checks.

## How to Run

### Step 1: Compile the Code
Use the following command to compile the system:
```bash
javac Node.java
```

Step 2: Start Peers
Create multiple peers (at least 4) using the following command on different terminals, where <ID> is any unique identifier for each peer:
```bash
java Node <ID>
```

For example, you can create peers with the IDs "Peer1", "Peer2", "Peer3", and "Peer4":
```bash
java Node Peer1
java Node Peer2
java Node Peer3
java Node Peer4
```

### Step 3: Interact with Peers
After starting a peer, you will be prompted with the following options:

- **List all peers**: View the list of peers currently connected to the node.
- **Send a file**: Transfer a file to another peer. You will need to provide the target peer’s ID and the file path.
- **Exit**: Shut down the peer.

### Example Workflow
1. Start four peers (Peer1, Peer2, Peer3, Peer4).
2. On Peer1, list all connected peers.
3. On Peer1, send a file to Peer2 by specifying the file path.
4. Peer2 will receive the file, verify its integrity using the hash, and save it.

### File Integrity Verification
The system ensures file integrity by comparing the SHA-256 hash of the original file with the hash of the reassembled file on the receiving peer. If the hashes match, the transfer is verified; otherwise, the system flags potential corruption.
