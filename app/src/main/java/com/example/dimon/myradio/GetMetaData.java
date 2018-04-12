package com.example.dimon.myradio;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Dimon on 18.03.2018.
 */

    class GetMetaData extends AsyncTask<Void, Void, ArrayList<String>> {

     private  ArrayAdapter<String> ad;

    GetMetaData(ArrayAdapter<String> adapter){
        this.ad = adapter;
    }

    

    @Override
    public ArrayList doInBackground(Void... voids) {

        Elements content;
        ArrayList<String> titleList = new ArrayList<>();
        final Document[] doc = new Document[1];


        try {
            doc[0] = Jsoup.connect("https://www.radioroks.ua/player/hardnheavy/").userAgent("Chrome").get();
            content = doc[0].select(".song");
            titleList.clear();
            for (Element contents : content) {
                titleList.add(contents.text());
            }
//                Log.e("result", titleList.get(0));

            return titleList;
        } catch (Exception e) {
            Log.e("parser", e.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(ArrayList<String> result){
        super.onPostExecute(result);
        if(ad == null && result == null) return;

        if (ad != null) {
            if(ad.getCount() == 0){
                ad.addAll(result);
            }else{
                if (!Objects.equals ( ad.getItem ( ad.getCount () - 1 ), result.get ( 0 ) )){
                    String st = ad.getItem(ad.getCount()-1);
                    ad.clear();
                    ad.add(st);
                    ad.add(result.get(0));
                }
            }
        }
        if (ad != null) {
            ad.notifyDataSetChanged();
        }
    }
}
