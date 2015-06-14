package greatlifedevelopers.studentrental.data;

import android.database.DataSetObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.R;

/*
 * Created by kenny on 5/23/15.
 */


/** A class to parse json data */
public class AlojamientoJSONParser {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_COMUNA = "id_comuna";
    private static final String TAG_FECHA = "fecha_ingreso";
    private static final String TAG_IMAGE = "";
    private static final String TAG_LATITUD = "latitud";
    private static final String TAG_LONGITUD = "longitud";



    // Receives a JSONObject and returns a list
    public List<HashMap<String,Object>> parse(JSONObject jObject){

        JSONArray jAlojamientos = null;
        try {
            // Retrieves all the elements in the 'alojamientos' array
            jAlojamientos = jObject.getJSONArray(TAG_ALOJAMIENTO);
        } catch (JSONException e) {
            e.printStackTrace();


        }

        // Invoking getAlojamientos with the array of json object
        // where each json object represent a alojamiento
        return getAlojamientos(jAlojamientos);
    }

    private List<HashMap<String, Object>> getAlojamientos(JSONArray jAlojamientos){
        int alojamientoCount = jAlojamientos.length();
        List<HashMap<String, Object>> alojamientoList = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> alojamiento = null;

        // Taking each alojamiento, parses and adds to list object
        for(int i=0; i<alojamientoCount;i++){
            try {
                // Call getCountry with country JSON object to parse the alojamiento
                alojamiento = getAlojamientos((JSONObject) jAlojamientos.get(i));
                alojamientoList.add(alojamiento);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return alojamientoList;
    }

    // Parsing the Country JSON object
    private HashMap<String, Object> getAlojamientos(JSONObject jCountry){

        HashMap<String, Object> country = new HashMap<String, Object>();

        String flag="";
        String idAlojamiento = "", nombre = "", comuna = "", fechaIngreso = "", latitud = "", longitud = "";

        try {

            idAlojamiento = jCountry.getString(TAG_ID);
            nombre = jCountry.getString(TAG_NOMBRE);
            comuna = jCountry.getString(TAG_COMUNA);
            fechaIngreso = jCountry.getString(TAG_FECHA);
            latitud = jCountry.getString(TAG_LATITUD);
            longitud = jCountry.getString(TAG_LONGITUD);
            String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamientos_1.jpg";
            flag = urlImg;

/*
            String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamiento.jpg";
            countryName = jCountry.getString("countryname");
            flag = jCountry.getString("flag");
            /*flag = urlImg;
            language = jCountry.getString("language");
            capital = jCountry.getString("capital");
            currencyCode = jCountry.getJSONObject("currency").getString("code");
            currencyName = jCountry.getJSONObject("currency").getString("currencyname");

            String details =        "Language : " + language + "\n" +
                    "Capital : " + capital + "\n" +
                    "Currency : " + currencyName + "(" + currencyCode + ")"; */

            country.put("nombre", nombre);
            country.put("flag", R.drawable.blank);
            country.put("flag_path", flag);
            country.put("id_alojamiento", idAlojamiento);
            country.put("fecha_ingreso", fechaIngreso);
            country.put("latitud", latitud);
            country.put("longitud", longitud);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return country;
    }
}

