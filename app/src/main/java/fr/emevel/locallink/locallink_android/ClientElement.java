package fr.emevel.locallink.locallink_android;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import fr.emevel.locallink.server.LocalLinkClient;
import fr.emevel.locallink.server.sync.FileSender;

public class ClientElement {

    View view;
    LocalLinkClient client;
    TextView name;
    List<Pair<TextView, ProgressBar>> progressbars = new ArrayList<>(5);

    public ClientElement(LocalLinkClient client, View view) {
        this.client = client;
        this.view = view;
        this.name = view.findViewById(R.id.name);

        TextView fname = view.findViewById(R.id.textView1);
        ProgressBar bar = view.findViewById(R.id.progressBar1);
        progressbars.add(Pair.of(fname, bar));

        fname = view.findViewById(R.id.textView2);
        bar = view.findViewById(R.id.progressBar2);
        progressbars.add(Pair.of(fname, bar));

        fname = view.findViewById(R.id.textView3);
        bar = view.findViewById(R.id.progressBar3);
        progressbars.add(Pair.of(fname, bar));

        fname = view.findViewById(R.id.textView4);
        bar = view.findViewById(R.id.progressBar4);
        progressbars.add(Pair.of(fname, bar));

        fname = view.findViewById(R.id.textView5);
        bar = view.findViewById(R.id.progressBar5);
        progressbars.add(Pair.of(fname, bar));
    }

    public void update() {
        if (client.getName() != null) {
            name.setText(client.getName() + " (" + client.getPrintableAddress() + ")");
        }
        List<FileSender> fileSenders = client.getFileSenderExecutor().getCurrentlySending();

        for (int i = 0; i < 5; i++) {
            if (i < fileSenders.size()) {
                FileSender fileSender = fileSenders.get(i);
                progressbars.get(i).getLeft().setText(fileSender.getFile().getName());
                progressbars.get(i).getLeft().setVisibility(View.VISIBLE);
                int progress = (int) Math.round((fileSender.getCurrent() / (double) fileSender.getLength()) * 100.0);
                progressbars.get(i).getRight().setProgress(progress);
                progressbars.get(i).getRight().setVisibility(View.VISIBLE);
            } else {
                progressbars.get(i).getLeft().setVisibility(View.INVISIBLE);
                progressbars.get(i).getRight().setVisibility(View.INVISIBLE);
            }
        }
    }

}
