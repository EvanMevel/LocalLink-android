package fr.emevel.locallink.locallink_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fr.emevel.locallink.locallink_android.databinding.FragmentSecondBinding;
import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.server.LocalLinkClient;

public class SecondFragment extends Fragment {

    public static SecondFragment instance;

    private FragmentSecondBinding binding;

    private LinearLayout layout;

    private List<ClientElement> clients = new ArrayList<>();

    private Timer timer;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        instance = this;

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        layout = binding.container2;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ClientElement client : clients) {
                            client.update();
                        }
                    }
                });
            }
        };

        timer = new Timer();

        timer.scheduleAtFixedRate(task, 0, 100);

        for (LinkSocket client : MainActivity.server.getNetworkServer().getClients()) {
            add((LocalLinkClient) client);
        }

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    public void add(LocalLinkClient client) {
        View view = getLayoutInflater().inflate(R.layout.client, null);

        Button b = view.findViewById(R.id.client_folders);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectClientFolderFragment.client = client;
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_SelectSyncFolder);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.addView(view);
            }
        });


        clients.add(new ClientElement(client, view));
    }

    public void remove(LocalLinkClient client) {
        ClientElement element = clients.stream().filter(c -> c.client == client).findFirst().orElse(null);

        if (element != null) {
            clients.remove(element);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout.removeView(element.view);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        timer.cancel();
    }

}