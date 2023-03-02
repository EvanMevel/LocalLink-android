package fr.emevel.locallink.locallink_android;

import java.io.IOException;
import java.net.Socket;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.server.LocalLinkClient;
import fr.emevel.locallink.server.LocalLinkServer;
import fr.emevel.locallink.server.LocalLinkServerData;

public class AndroidLocalLinkServer extends LocalLinkServer {

    public AndroidLocalLinkServer(LocalLinkServerData data, Runnable dataSaver) throws IOException {
        super(data, dataSaver);
    }

    @Override
    protected void clientDisconnected(LinkSocket client) {
        super.clientDisconnected(client);

        if (SecondFragment.instance != null) {
            SecondFragment.instance.remove((LocalLinkClient) client);
        }

    }

    @Override
    protected LinkSocket createClient(Socket sock) throws IOException {
        LocalLinkClient client = (LocalLinkClient) super.createClient(sock);

        if (SecondFragment.instance != null) {
            SecondFragment.instance.add(client);
        }
        return client;
    }
}
