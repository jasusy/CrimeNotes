package com.example.steve.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Steve on 10/7/2015.
 */
public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "extra_crime_id";
    private static final String DIALOG_DATE ="dialog_date";
    private static final String DIALOG_LARGE_PHOTO = "dialog_large_photo";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
//    private static final String TAG = "CrimeFGLifeCycle";

    private Crime mCrime;
    private EditText titleEditText;
    private Button dateButton;
    private Button reportButton;
    private Button suspectButton;
    private Button callSuspectButton;
    private CheckBox solvedCheckBox;
    private String phoneNum;
    private ImageView crimeImageView;
    private ImageButton cameraImageButton;
    private File photoFile;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }


    public static CrimeFragment newIntent(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
        photoFile = CrimeLab.getInstance(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);
        titleEditText = (EditText) view.findViewById(R.id.crime_title);
        titleEditText.setText(mCrime.getTitle()); //set title of crime at beginning
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dateButton = (Button) view.findViewById(R.id.details_date_button);
        updateButtonDate();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        solvedCheckBox = (CheckBox) view.findViewById(R.id.isSolved_checkbox);
        solvedCheckBox.setChecked(mCrime.isSolved()); //update checkbox at beginning like button.
        solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        reportButton = (Button) view.findViewById(R.id.crime_report_button);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent it = new Intent(Intent.ACTION_SEND);
//                it.setType("text/plain");
//                it.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
//                it = Intent.createChooser(it, getString(R.string.send_report));
//                startActivity(it);
                ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder
                        .from(getActivity());
                builder.setSubject(getString(R.string.crime_report_subject));
                builder.setText(getCrimeReport());
                builder.setType("text/plain");
                builder.startChooser();
            }
        });

        final Intent pickContactIt = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        suspectButton = (Button) view.findViewById(R.id.crime_suspect_button);
        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContactIt, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null){
            suspectButton.setText(mCrime.getSuspect());
        }

        //in case on contacts app
        PackageManager pm = getActivity().getPackageManager();
        if (pm.resolveActivity(pickContactIt, PackageManager.MATCH_DEFAULT_ONLY) == null){
            suspectButton.setEnabled(false);
        }

        callSuspectButton = (Button) view.findViewById(R.id.call_suspect_button);
        if(mCrime.getIdSuspect() != null && queryNumber(mCrime.getIdSuspect())) {
            callSuspectButton.setVisibility(View.VISIBLE);
        }
        callSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Intent.ACTION_DIAL);
                it.setData(Uri.parse("tel:" + phoneNum));
                if(it.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(it);
                }
            }
        });

        cameraImageButton = (ImageButton) view.findViewById(R.id.crime_camera_image_button);
        final Intent takePhotoIt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePicture = (photoFile != null) && (takePhotoIt.resolveActivity(pm) != null);
        cameraImageButton.setEnabled(canTakePicture);
        if (canTakePicture){
            Uri uri = Uri.fromFile(photoFile);
            takePhotoIt.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(takePhotoIt, REQUEST_PHOTO);
            }
        });

        crimeImageView = (ImageView) view.findViewById(R.id.crime_photo_image_view);
        crimeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLargePhotoDialog();
            }
        });
        updateImageView();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) return;

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateButtonDate();
        } else if (requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID,
            };

            Cursor cursor = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            assert cursor != null;

            try{
                if(cursor.getCount() == 0) return;
                cursor.moveToFirst();
                String suspect = cursor.getString(0); //0 is index for DISPLAY_NAME
                mCrime.setSuspect(suspect);
                updateCrime();
                suspectButton.setText(suspect);
                String id = cursor.getString(1); //1 is index for _ID
                mCrime.setSuspectId(id);

                if(queryNumber(id)){
                    callSuspectButton.setVisibility(View.VISIBLE);
                }

            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_PHOTO){
            updateCrime();
            updateImageView();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //crime info. changed in CrimeFragment is saved to db when it ended.
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /*---------------------Following are private method----------------------*/
    private void updateCrime(){
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateButtonDate() {
        //format date by SimpleDateFormat class
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mma EEE, MMM d, yyyy", Locale.US);
        dateButton.setText(sdf.format(mCrime.getDate())); //text changed to capital in button
//        dateButton.setText(mCrime.getDate().toString());
    }


    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString =  getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        //format by DateFormat class
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);

        return report;
    }

    //query contact phone number by given contact id, save number to phoneNum
    //return boolean for sucess or failure
    private boolean queryNumber(String contactId){
        Cursor c2 = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID  + " = ?",
                new String[]{contactId},
                null
        );
        if(c2 == null) return false;

        try{
            c2.moveToFirst();
            phoneNum = c2.getString(c2.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
        } catch (Exception e){  return false;}
        finally {c2.close();}

        return true;
    }

    //update photo to photo thumbnail Image View
    private void updateImageView(){
        if (photoFile == null || !photoFile.exists()){
            crimeImageView.setImageDrawable(null);
            crimeImageView.setClickable(false);
        } else {
            crimeImageView.setClickable(true);
            Bitmap bitmap = PictureUtils.getScaleBitmap(photoFile.getPath(), 100, 100);
            crimeImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * used to show dialog for large photo of thumbnail
     * called when thumbnail clicked, in listener
     */
    private void showLargePhotoDialog(){

        //create dialog and inflate it
        final Dialog largePhotoDialog = new Dialog(getActivity(),android.R.style.Theme_Black_NoTitleBar);
//                largePhotoDialog.setCancelable(false);
        largePhotoDialog.setContentView(R.layout.dialog_large_photo);

        //set image view
        ImageView largePhoto = (ImageView)largePhotoDialog.findViewById(R.id.large_photo_image_view);
        Bitmap bitmap = PictureUtils.getScaleBitmap(photoFile.getPath(), getActivity());
        largePhoto.setImageBitmap(bitmap);

        //close button listener
        Button btnClose = (Button)largePhotoDialog.findViewById(R.id.btn_Dialog_Close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                largePhotoDialog.dismiss();
            }
        });

        //show dialog
        largePhotoDialog.show();
    }

}
