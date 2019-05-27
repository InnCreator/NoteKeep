package com.example.notekeep;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.example.notekeep.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeep.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeep.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeep.ORIGINAL_NOTE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    public NoteInfo mNote;
    public boolean mIsNewNote;
    public Spinner mSpinnerCourses;
    public EditText mTextNoteTitle;
    public EditText mTextNoteText;
    public int mNotePosition;
    public boolean mIsCancelling;
    public String mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = findViewById(R.id.spinner_Courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        if(savedInstanceState == null) {
            saveOriginalNoteValues();
        }else{
            restoreOriginalValues(savedInstanceState);
        }

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }
    }

    private void restoreOriginalValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;

        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);

        textNoteTitle.setText(mNote.getTitle());
        textNoteTitle.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        } else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }
            else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteTitle.getText().toString());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }
            return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {

        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = " Checkout my course notes \"" + course.getTitle() + "\"\n"
                + mTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}
