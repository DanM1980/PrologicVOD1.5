package com.prologdigital.prologvod;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DanM on 17/05/2016.
 */
public class DataHandler {
    public String jsonCoursesStr = new String();
    public ArrayList<HashMap<String, String>> coursesList = new ArrayList<>();

    public String jsonStr = new String();
    public  HashMap<String, String> courseInfo = new HashMap<>();
    public ArrayList<String> pdfUrls = new ArrayList<>();
    public ArrayList<HashMap<String, String>> lessonsList = new ArrayList<>();

    public static final String TAG_COURSES_ID = "ID";
    public static final String TAG_COURSES_NAME = "Name";
    public static final String TAG_COURSES_MAKAT = "Makat";
    public static final String TAG_COURSES_ICON = "Icon";

    public static final String TAG_COURSE_ICON = "ProductImageDataURL";
    public static final String TAG_COURSE_MAKAT = "Makat";
    public static final String TAG_COURSE_NAME = "ProductName";
    public static final String TAG_COURSE_DESCRIPTION = "ProductDescription";
    public static final String TAG_COURSE_CURRENCY = "CurrencySign";
    public static final String TAG_COURSE_PRICE = "Price";
    public static final String TAG_COURSE_PURCHASE_TEXT = "txtPurchasedText";

    public static final String TAG_ITEM = "DigitalProductItemList";
    public static final String TAG_ITEM_ID = "ItemID";
    public static final String TAG_NAME = "Name";
    public static final String TAG_CHAPTER_NAME = "ChapterName";
    public static final String TAG_DESCRIPTION = "Description";
    public static final String TAG_URL_DB = "Url_DB";
    public static final String TAG_URL_CURRENT = "Url_Current";
    public static final String TAG_VIMEO_PREVIEW_IMAGE = "VimeoPreviewImage";
    public static final String TAG_FULL_FILENAME = "FullFileName";
    public static final String TAG_FILE_TYPE = "FileType";
    public static final String TAG_IS_FREE = "IsFree";
    public static final String TAG_RATING_ID = "RaitingID";
    public static final String TAG_SPOKEN_LANGUAGE_ID = "SpokenLanguageID";
    public static final String TAG_WATCHED = "Watched";
    public static final String TAG_PRODUCT = "Product";
    public static final String TAG_PDF_URL = "Pdf_url";

    public boolean parseJSON(){
        try {
            lessonsList = new ArrayList<>();
            pdfUrls = new ArrayList<>();
            ArrayList<HashMap<String, String>> LessonsListMP4 = new ArrayList<>();
            ArrayList<HashMap<String, String>> LessonsListThumb = new ArrayList<>();
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray lessons = jsonObj.getJSONArray(TAG_ITEM);
            for (int i = 0; i < lessons.length(); i++) {
                HashMap<String, String> lesson = new HashMap<>();
                JSONObject c = lessons.getJSONObject(i);
                if (c.getString(TAG_FILE_TYPE).compareTo("mp4")==0){
                    lesson.put(TAG_NAME, c.getString(TAG_NAME));
                    lesson.put(TAG_CHAPTER_NAME, c.getString(TAG_CHAPTER_NAME));
                    lesson.put(TAG_DESCRIPTION, c.getString(TAG_DESCRIPTION));
                    lesson.put(TAG_IS_FREE, c.getString(TAG_IS_FREE));
                    lesson.put(TAG_WATCHED, c.getString(TAG_WATCHED));
                    lesson.put(TAG_URL_CURRENT, c.getString(TAG_URL_CURRENT));
                    LessonsListMP4.add(lesson);
                } else if (c.getString(TAG_FILE_TYPE).compareTo("vimeo")==0){
                    lesson.put(TAG_VIMEO_PREVIEW_IMAGE, c.getString(TAG_VIMEO_PREVIEW_IMAGE));
                    lesson.put(TAG_URL_CURRENT, c.getString(TAG_URL_CURRENT));
                    LessonsListThumb.add(lesson);
                } else if (c.getString(TAG_FILE_TYPE).compareTo("pdf")==0){
                    pdfUrls.add(c.getString(TAG_URL_CURRENT));
                }
            }
            for(int i=0; i<LessonsListMP4.size(); i++){
                HashMap<String, String> lesson = new HashMap<>();
                lesson.put(TAG_NAME, LessonsListMP4.get(i).get(TAG_NAME));
                lesson.put(TAG_CHAPTER_NAME, LessonsListMP4.get(i).get(TAG_CHAPTER_NAME));
                lesson.put(TAG_DESCRIPTION, LessonsListMP4.get(i).get(TAG_DESCRIPTION));
                lesson.put(TAG_URL_CURRENT, LessonsListThumb.get(i).get(TAG_URL_CURRENT));
                lesson.put(TAG_IS_FREE, LessonsListMP4.get(i).get(TAG_IS_FREE));
                lesson.put(TAG_WATCHED, LessonsListMP4.get(i).get(TAG_WATCHED));
                lesson.put(TAG_VIMEO_PREVIEW_IMAGE, LessonsListThumb.get(i).get(TAG_VIMEO_PREVIEW_IMAGE));
                lessonsList.add(lesson);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void parseCourse(){
        try {
            JSONObject c = new JSONObject(jsonStr.substring(0));
            courseInfo = new HashMap<>();
            courseInfo.put(TAG_COURSE_ICON, c.getString(TAG_COURSE_ICON));
            courseInfo.put(TAG_COURSE_MAKAT, c.getString(TAG_COURSE_MAKAT));
            courseInfo.put(TAG_COURSE_NAME, c.getString(TAG_COURSE_NAME));
            courseInfo.put(TAG_COURSE_DESCRIPTION, c.getString(TAG_COURSE_DESCRIPTION));
            courseInfo.put(TAG_COURSE_CURRENCY, c.getString(TAG_COURSE_CURRENCY));
            courseInfo.put(TAG_COURSE_PRICE, c.getString(TAG_COURSE_PRICE));
            courseInfo.put(TAG_COURSE_PURCHASE_TEXT, c.getString(TAG_COURSE_PURCHASE_TEXT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void parseCourses(int[] coursesIds){
        try {
            coursesList = new ArrayList<>();
            for(int i=0; i<coursesIds.length; i++){
                JSONObject jsonObj = new JSONObject(jsonCoursesStr);
                if (jsonObj.has(""+coursesIds[i])) {
                    JSONArray course = jsonObj.getJSONArray("" + coursesIds[i]);
                    JSONObject c = course.getJSONObject(0);

                    HashMap<String, String> courseHash = new HashMap<>();
                    courseHash.put(TAG_COURSES_ID, ""+coursesIds[i]);
                    courseHash.put(TAG_COURSES_NAME, c.getString(TAG_COURSES_NAME));
                    courseHash.put(TAG_COURSES_MAKAT, c.getString(TAG_COURSES_MAKAT));
                    courseHash.put(TAG_COURSES_ICON, c.getString(TAG_COURSES_ICON));

                    coursesList.add(courseHash);
                } else Log.d("Course ID NOT FOUND", ""+coursesIds[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
