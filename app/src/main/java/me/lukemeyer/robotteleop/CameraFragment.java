package me.lukemeyer.robotteleop;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {
    private ImageView imageView = null;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        imageView = view.findViewById(R.id.imageView);

        return view;
    }

    public void displayImage(Bitmap bmp) {
        if(imageView == null) {
            return;
        }
        imageView.setImageBitmap(bmp);
    }
}
