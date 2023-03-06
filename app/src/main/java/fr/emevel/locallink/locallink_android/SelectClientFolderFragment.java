package fr.emevel.locallink.locallink_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import fr.emevel.locallink.locallink_android.databinding.SelectClientFolderBinding;
import fr.emevel.locallink.network.packets.PacketFolderList;
import fr.emevel.locallink.server.LocalLinkClient;
import fr.emevel.locallink.server.sync.SyncFolder;

public class SelectClientFolderFragment extends Fragment {

    private SelectClientFolderBinding binding;

    private LinearLayout layout;

    public static LocalLinkClient client;

    public static SyncFolder syncFolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SelectClientFolderBinding.inflate(inflater, container, false);

        layout = binding.container;

        return binding.getRoot();
    }

    public void addFolder(String parent, PacketFolderList.Folder clientFolder) {
        View folder = getLayoutInflater().inflate(R.layout.folder_in_selector, null);

        TextView te = folder.findViewById(R.id.name);

        te.setText(parent + clientFolder.getName());
        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    client.createLink(syncFolder, parent + clientFolder.getName());
                }).start();
                Toast.makeText(getContext(), "Added link!", Toast.LENGTH_SHORT).show();

                NavHostFragment.findNavController(SelectClientFolderFragment.this)
                        .navigate(R.id.action_SelectClientFolder_to_Second);
            }
        });
        layout.addView(folder);

        for (PacketFolderList.Folder child : clientFolder.getChilds()) {
            addFolder(parent + clientFolder.getName() + "/", child);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (PacketFolderList.Folder folder : client.getClientFolders()) {
            addFolder("", folder);
        }
    }
}
