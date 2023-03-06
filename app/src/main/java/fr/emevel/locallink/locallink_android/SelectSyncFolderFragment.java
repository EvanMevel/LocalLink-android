package fr.emevel.locallink.locallink_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import fr.emevel.locallink.locallink_android.databinding.SelectSyncFolderBinding;
import fr.emevel.locallink.server.sync.SyncFolder;

public class SelectSyncFolderFragment extends Fragment {

    private SelectSyncFolderBinding binding;

    private LinearLayout layout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SelectSyncFolderBinding.inflate(inflater, container, false);

        layout = binding.container;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (SyncFolder syncFolder : MainActivity.serverData.getFolders().getFolders()) {
            View folder = getLayoutInflater().inflate(R.layout.folder_in_selector, null);

            TextView te = folder.findViewById(R.id.name);

            te.setText(syncFolder.getName());
            folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SelectClientFolderFragment.syncFolder = syncFolder;

                    NavHostFragment.findNavController(SelectSyncFolderFragment.this)
                            .navigate(R.id.action_SelectSyncFolder_to_SelectClientFolder);
                    System.out.println("Clicked: " + te.getText());
                }
            });
            layout.addView(folder);
        }

    }
}
