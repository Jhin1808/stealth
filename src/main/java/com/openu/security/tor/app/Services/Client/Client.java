package com.openu.security.tor.app.Services.Client;

import com.openu.security.tor.app.Logger.LogLevel;
import com.openu.security.tor.app.Logger.Logger;
import com.openu.security.tor.app.PublicEncryption.ChainedEncryption;
import com.openu.security.tor.app.PublicEncryption.ChainedResponse;
import com.openu.security.tor.app.PublicEncryption.KeyHelper;
import com.openu.security.tor.app.PublicEncryption.KeyPairs;
import com.openu.security.tor.app.Services.Config;
import com.openu.security.tor.app.Services.Service;
import com.openu.security.tor.app.Sockets.ClientSocket;
import com.openu.security.tor.app.Sockets.Database;
import com.openu.security.tor.app.Sockets.ServerDetails;

import java.util.Scanner;

public class Client implements Service {

    private int chainLength;
    private ClientSocket clientSocket;
    private Scanner scanner;
    private KeyPairs keyPairs;

    public Client(int chainLength) throws Exception {
        this.chainLength = chainLength;
        this.keyPairs = new KeyPairs();
        this.keyPairs.generateKeyPair();
        this.clientSocket = new ClientSocket(Config.TRUSTED_SERVER_HOST, Config.TRUSTED_SERVER_PORT, this.keyPairs,
                KeyHelper.base64ToPublicKey(Config.TRUSTED_SERVER_PUBLIC_KEY));
        this.scanner = new Scanner(System.in);

        Logger.info(
            "<Client> Connected to Trusted Server (" + Config.TRUSTED_SERVER_HOST + ":" + Config.TRUSTED_SERVER_PORT + ")"
        );
    }

    public void execute() throws Exception {
        String input;

        while (true) {
            System.out.print("URL for HTTP Get request (CTRL+C to stop): ");
            input = scanner.nextLine();

            // @TODO: validate URL?

            // Send get Relays
            this.clientSocket.getRelays(chainLength, keyPairs.getPublicKey());

            ChainedResponse chain = ChainedEncryption.chain(chainLength, "HTTP_GET_REQUEST " + input + " "
                    + KeyHelper.publicKeyToBase64(keyPairs.getPublicKey()));

            ServerDetails relayDetails = chain.getFirstRelay();
            Logger.info("<Get Request> Sending to initial relay " + relayDetails.getHost() + ":" + relayDetails.getPort());
            ClientSocket relay = new ClientSocket(relayDetails.getHost(), relayDetails.getPort(), this.keyPairs, relayDetails.getPublicKey());

            String response = relay.send(chain.getEncryptedPacket(), true, true);

            System.out.println("Response: " + response);
        }
    }
}