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


                //NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    public void add(File file) {
        View view = getLayoutInflater().inflate(R.layout.folder, null);
        TextView text = view.findViewById(R.id.name);
        text.setText(file.getAbsolutePath());
        ImageView image = view.findViewById(R.id.logo);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(view);
            }
        });

        layout.addView(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}