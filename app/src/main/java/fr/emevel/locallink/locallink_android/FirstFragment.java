package fr.emevel.locallink.locallink_android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;

import fr.emevel.locallink.locallink_android.databinding.FragmentFirstBinding;
import fr.emevel.locallink.server.sync.LocalSyncFolder;
import fr.emevel.locallink.server.sync.SyncFolder;

public class FirstFragment extends Fragment {

    public static FirstFragment instance;

    private FragmentFirstBinding binding;

    private final ActivityResultLauncher<Uri> dirRequest = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri == null) {
                    return;
                } else {
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(
                            uri,
                            DocumentsContract.getTreeDocumentId(uri)
                    );

                    String path = PathUtils.getPath(getActivity(), docUri);

                    System.out.println(path);

                    File f = new File(path);

                    instance.add(f);
                }
            });

    private LinearLayout layout;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        instance = this;

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        layout = binding.container;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dirRequest.launch(
                        Uri.parse(Environment.getExternalStorageDirectory().toURI().toString())
                );
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    public void loadAll() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (SyncFolder folder : MainActivity.serverData.getFolders().getFolders()) {
                    if (folder instanceof LocalSyncFolder) {
                        add((LocalSyncFolder) folder);
                    }
                }
            }
        });
    }

    public void add(LocalSyncFolder folder) {
        View view = getLayoutInflater().inflate(R.layout.folder, null);
        TextView text = view.findViewById(R.id.name);
        text.setText(folder.getFolder().getAbsolutePath());
        ImageView image = view.findViewById(R.id.logo);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.serverData.getFolders().removeFolder(folder);
                MainActivity.serverSaveData.run();
                layout.removeView(view);
            }
        });

        layout.addView(view);
    }

    public void add(File file) {
        LocalSyncFolder folder = new LocalSyncFolder(file);
        add(folder);
        MainActivity.serverData.getFolders().addFolder(folder);

        MainActivity.serverSaveData.run();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}