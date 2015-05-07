package greatlifedevelopers.studentrental.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.DetalleHabitacionActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListHabitacionesFragment extends ListFragment {

    ProgressDialog progressDialog;
    JSONParser jsonParser = new JSONParser();
    JSONArray habitacion = null;
    ArrayList<HashMap<String, String>> habitacionList;


    private static String URL_LIST_HABITACIONES = Constants.URL_DOMINIO + "list_habitaciones.php";

    //TAGS

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_NODO = "alojamiento";
    private static final String TAG_ID_HABITACION = "id_habitacion";
    private static final String TAG_PRECIO = "precio";
    private static final String TAG_DESCRIPCION = "descripcion";


    String idAlojamiento;

    public ListHabitacionesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        AlojamientoActivity activity = (AlojamientoActivity) getActivity();
        idAlojamiento = activity.getMyData();


        View view = inflater.inflate(R.layout.fragment_list_habitaciones, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        habitacionList = new ArrayList<HashMap<String, String>>();
        ListView listView = getListView();
        new getAllHabitaciones().execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idHabitacion = ((TextView) getActivity().findViewById(R.id.idAlojamientoDetalle)).getText().toString();

                Intent dHabitacion = new Intent(getActivity(), DetalleHabitacionActivity.class);
                dHabitacion.putExtra("idHabitacion", idHabitacion);
                startActivity(dHabitacion);

            }
        });

    }

    class getAllHabitaciones extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ListHabitacionesFragment.this.getActivity());
            progressDialog.setMessage("Cargando... Por favor espere.");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));

            JSONObject jsonObject = jsonParser.makeHttpRequest(URL_LIST_HABITACIONES, "GET", params);

            try{

                int success = jsonObject.getInt(TAG_SUCCESS);

                if(success==1){
                    habitacion = jsonObject.getJSONArray(TAG_NODO);

                    for(int i = 0; i<habitacion.length(); i++){
                        JSONObject a = habitacion.getJSONObject(i);

                        String idHabitacion = a.getString(TAG_ID_HABITACION);
                        String precioHabitacion = a.getString(TAG_PRECIO);
                        String descripcionHabitacion = a.getString(TAG_DESCRIPCION);
                        String precio = "$ " + precioHabitacion;


                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(TAG_ID_HABITACION, idHabitacion);
                        map.put(TAG_PRECIO, precio);
                        map.put(TAG_DESCRIPCION, descripcionHabitacion);

                        habitacionList.add(map);

                    }
                } else {

                    //COMENTAR
                    progressDialog.dismiss();


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Lo sentimos..");
                            alert.setMessage("El Alojamiento no cuenta con habitaciones disponibles o aún no han sido ingresadas");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alert.create().show();
                        }
                    });
                }

            } catch (JSONException e){
                e.printStackTrace();

                //COMENTAR
                progressDialog.dismiss();
                e.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("¡Error!");
                        alert.setMessage("Revisa la conexión de red o vuelve a intentarlo más tarde.");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert.create().show();

                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            ListHabitacionesFragment.this.getActivity(), habitacionList,
                            R.layout.lista_items_big, new String[]{TAG_PRECIO, TAG_DESCRIPCION,
                            TAG_ID_HABITACION},
                            new int[]{R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle});
                    setListAdapter(adapter);
                }
            });
        }
    }
}
