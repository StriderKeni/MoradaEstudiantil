package greatlifedevelopers.studentrental.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.DetalleAlojamiento;
import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.LoginActivity;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.activitys.SignUpRegister;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFavoritosFragment extends ListFragment {

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> favoritosList;
    JSONArray alojamientoFavorito = null;
    String idUsuario;


    private static String url_favoritos = Constants.URL_DOMINIO + "favoritos.php";
    private static String URL_USUARIO_DATA = Constants.URL_DOMINIO + "usuario_data";
    private static String TAG_LIST_USUARIO = "list_array_usuario";
    private static String TAG_ID_USUARIO = "id";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_FECHA_INGRESO = "fecha_ingreso";


    public ListFavoritosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_favoritos, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        idUsuario = activity.getUsuario();


        ListView listView = getListView();

        favoritosList = new ArrayList<HashMap<String, String>>();
        new LoadAllFavorites().execute();

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText().toString();
                Intent i = new Intent(getActivity(), AlojamientoActivity.class);

                i.putExtra("tagid", idAlojamiento);
                i.putExtra("id_usuario", idUsuario);
                getActivity().startActivityForResult(i, 100);

            }
        });

    }


    // Respuesta desde VerAlojamientos activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // si resultado es 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            getActivity().startActivity(intent);
        }

    }


    private StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    class LoadAllFavorites extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ListFavoritosFragment.this.getActivity());
            pDialog.setMessage("Cargando Favoritos... Por favor espere.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_usuario", idUsuario));

            JSONObject json = jsonParser.makeHttpRequest(url_favoritos, "GET", params);

            try{
                int success = json.getInt(TAG_SUCCESS);

                if(success == 1){
                    alojamientoFavorito = json.getJSONArray(TAG_ALOJAMIENTO);

                    for (int i=0; i<alojamientoFavorito.length(); i++){
                        JSONObject c = alojamientoFavorito.getJSONObject(i);
                        String idAlojamiento = c.getString(TAG_ID);
                        String nombre = c.getString(TAG_NOMBRE);
                        String fechaIngreso = c.getString(TAG_FECHA_INGRESO);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_ID, idAlojamiento);
                        map.put(TAG_NOMBRE, nombre);
                        map.put(TAG_FECHA_INGRESO, fechaIngreso);

                        favoritosList.add(map);
                    }

                } else {

                    //COMENTAR
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Lo sentimos");
                            alert.setMessage("Aún no cuentas con Alojamientos en Favoritos" +
                                    "\nTe invitamos a seguir revisando cada una de las publicaciones.");
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

                //COMENTAR
                pDialog.dismiss();
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
            } catch (Exception e){
                e.printStackTrace();

                pDialog.dismiss();
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
            pDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            ListFavoritosFragment.this.getActivity(), favoritosList, R.layout.lista_items, new String[]{TAG_NOMBRE, TAG_FECHA_INGRESO, TAG_ID},
                            new int[]{R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle});

                    setListAdapter(adapter);
                }
            });
        }
    }



}
