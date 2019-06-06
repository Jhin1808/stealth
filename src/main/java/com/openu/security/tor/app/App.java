package com.openu.security.tor.app;

import com.openu.security.tor.app.Services.Client.Client;
import com.openu.security.tor.app.Services.Relay.Relay;
import com.openu.security.tor.app.Services.TrustedServer.TrustedServer;
import picocli.CommandLine;
import picocli.CommandLine.*;
import com.openu.security.tor.app.Services.*;

enum ServiceNames { Relay, TrustedServer, Client }

@Command(name = "proxy", mixinStandardHelpOptions = true, version = "Encrypted proxy 1.0")
public class App implements Runnable {
    @Option(
        names = { "-v", "--verbose" },
        description = "Verbose mode. Helpful for troubleshooting. "
    )
    private boolean verbose = false;

    @Parameters(arity = "1", paramLabel = "SERVICE", description = "Service types: ${COMPLETION-CANDIDATES}.")
    private ServiceNames serviceName = null;

    @Option(
            names = { "-p", "--parallel" },
            description = "Amount of instances to run in parallel."
    )
    private int instanceAmount = 1;

    public void run() {
        if (verbose) {
            System.out.println("Seleted: " + serviceName);
        }


        Service service = null;

        switch (serviceName) {
            case Relay: service = new Relay(); break;
            case Client: service = new Client(); break;
            case TrustedServer: service = new TrustedServer(); break;

            default:
                System.out.println("ERROR: No such service!");
                return;
        }

        service.execute(instanceAmount, verbose);
    }

    public static void main(String[] args) {
        CommandLine.run(new App(), args);
    }
}