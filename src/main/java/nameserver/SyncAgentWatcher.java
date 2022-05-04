package nameserver;

import agents.SyncAgent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class SyncAgentWatcher implements Runnable {
    NameServer nameServer;
    int agentIdentifier;
    LocalDateTime lastUpdate;

    public SyncAgentWatcher(NameServer nameServer) {
        this.nameServer = nameServer;
        this.agentIdentifier = new Random().nextInt();
        this.lastUpdate = LocalDateTime.now();
    }

    public int getAgentIdentifier() {
        return agentIdentifier;
    }

    public boolean update(int id) {
        if (id != agentIdentifier) {
            lastUpdate = LocalDateTime.now();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        (new Thread(new SyncAgent(this.nameServer, 0, this.agentIdentifier))).start();

        while(true) {
            if (lastUpdate.until(LocalDateTime.now(), ChronoUnit.SECONDS) > 30) {
                this.agentIdentifier = new Random().nextInt();
                (new Thread(new SyncAgent(this.nameServer, 0, this.agentIdentifier))).start();
            }

            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
