package greatlifedevelopers.studentrental.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.DialogPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.ListBusquedaActivity;
import greatlifedevelopers.studentrental.activitys.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class BusquedaAlojamientoFragment extends android.support.v4.app.Fragment {

    public String comuna = "";
    public String tipoAlojamiento = "";
    public int precio1, precio2;



    public BusquedaAlojamientoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_busqueda_alojamiento, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        Spinner comunaSpinner = (Spinner) getView().findViewById(R.id.comuna_spinner);
        Spinner tipoAlojamientoSpinner = (Spinner) getView().findViewById(R.id.tipo_alojamiento_spinner);
        final EditText editPrecio1 = (EditText) getActivity().findViewById(R.id.edit_precio_1);
        final EditText editPrecio2 = (EditText) getActivity().findViewById(R.id.edit_precio_2);


        Button busquedaAlojamiento = (Button) getView().findViewById(R.id.busqueda_alojamiento);

        busquedaAlojamiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                precio1 = Integer.parseInt(editPrecio1.getText().toString());
                precio2 = Integer.parseInt(editPrecio2.getText().toString());

                Intent intent = new Intent(getActivity(), ListBusquedaActivity.class);
                intent.putExtra("comuna", comuna);
                intent.putExtra("tipoAlojamiento", tipoAlojamiento);
                intent.putExtra("precio1", precio1);
                intent.putExtra("precio2", precio2);

                getActivity().startActivity(intent);

                /*try{

                    precio1 = Integer.parseInt(editPrecio1.getText().toString());
                    precio2 = Integer.parseInt(editPrecio2.getText().toString());

                    Intent intent = new Intent(getActivity(), ListBusquedaActivity.class);
                    intent.putExtra("comuna", comuna);
                    intent.putExtra("tipoAlojamiento", tipoAlojamiento);
                    intent.putExtra("precio1", precio1);
                    intent.putExtra("precio2", precio2);

                    getActivity().startActivity(intent);

                } catch (NumberFormatException e){

                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("¡Error!");
                            alert.setMessage("Por favor, rellena todos los campos");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alert.create().show();



                } */



            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.comuna_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        comunaSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> tipoAlojamientoAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.tipo_alojamiento_spinnner, android.R.layout.simple_spinner_item);
        tipoAlojamientoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoAlojamientoSpinner.setAdapter(tipoAlojamientoAdapter);


        comunaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                comuna = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tipoAlojamientoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoAlojamiento = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

}
