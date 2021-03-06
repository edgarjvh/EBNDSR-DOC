package clases;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.villasoftgps.ebndsrdoc.Frm_Principal;
import com.villasoftgps.ebndsrdoc.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import vistas.lvMensajesItems;
import static com.google.android.gms.gcm.GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE;

public class GCMIntentService extends IntentService
{
    private static final String TAG = "EJVH";
    private SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_CONVERSATIONS = "conversations";
    private static final String PROPERTY_CURRENT_ID_REP = "currentIdRepresentante";
    private static final String PROPERTY_CURRENT_TAB = "currentTab";
    private static final String PROPERTY_IS_FOREGROUND = "isForeground";
    private Docente docente;
    private ArrayList<lvMensajesItems> conversaciones;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        docente = new Docente();
        conversaciones = new ArrayList<>();
        gson = new Gson();
    }

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //se instancia la herramienta de google cloud messaging
        GoogleCloudMessaging GCM = GoogleCloudMessaging.getInstance(this);

        // se declara el tipo de mensaje entrante y se obtienen los extras en caso de existir
        String messageType = GCM.getMessageType(intent);
        Bundle extras = intent.getExtras();

        // se verifica si hay extras en el mensaje entrante
        if (!extras.isEmpty())
        {
            //noinspection deprecation
            if (MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                try {
                    JSONObject jsonObj = new JSONObject(extras.getString("mensaje"));
                    String result = jsonObj.get("Result").toString();

                    if (sPrefs == null){
                        sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    }

                    Log.d("EJVH result doc",result);

                    switch (result){
                        case "INCOMING":
                            JSONObject array = new JSONObject(jsonObj.get("Mensaje").toString());

                            Log.d("EJVH result doc via",Integer.toString(array.getInt("Via")));
                            Log.d("EJVH result doc Msj",jsonObj.get("Mensaje").toString());

                            if (array.getInt("Via") == 1){ // se valida que sea un docente quien este enviando el msj
                                // se convierte la fecha quitando caracteres no numericos

                                String value = "";
                                if (array.getString("fechaHora").matches("^/Date\\(\\d+\\)/$")) {
                                    value = array.getString("fechaHora").replaceAll("^/Date\\((\\d+)\\)/$", "$1");
                                }

                                lvMensajesItems mensaje = new lvMensajesItems(
                                        0,
                                        array.getInt("IdMensaje"),
                                        array.getInt("Via"),
                                        array.getInt("IdDocente"),
                                        array.getInt("IdRepresentante"),
                                        array.getInt("Estado"),
                                        Long.parseLong(value),
                                        array.getString("Texto")
                                );

                                int idRepresentante = array.getInt("IdRepresentante");

                                if (sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                    conversaciones.add(mensaje);
                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();
                                }else{
                                    Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                    conversaciones = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);
                                    conversaciones.add(mensaje);
                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();
                                }

                                Frm_Principal.actualizarConversaciones();

                                if (!sPrefs.getString(PROPERTY_USER,"").equals("")){
                                    docente = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Docente.class);

                                    if (docente.getId() == array.getInt("IdDocente")){

                                        confirmarRecepcion(array.getInt("IdMensaje"),1);
                                        mostrarNotification(array.getString("Texto"),Long.parseLong(value),array.getString("NombreRepresentante"), idRepresentante);
                                    }
                                }
                            }

                            break;

                        case "UPDATE":
                            Log.d("EJVH update doc via",jsonObj.get("Via").toString());

                            if (jsonObj.get("Via").toString().equals("0")){ // si el representante esta confirmando....
                                if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                    Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                    conversaciones = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                                    for (int i = 0; i < conversaciones.size();i++){
                                        if (conversaciones.get(i).getIdMensaje() == Integer.parseInt(jsonObj.get("IdMensaje").toString()) &&
                                                conversaciones.get(i).getIdDocente() == Integer.parseInt(jsonObj.get("IdDocente").toString()) &&
                                                conversaciones.get(i).getIdRepresentante() == Integer.parseInt(jsonObj.get("IdRepresentante").toString())){

                                            conversaciones.get(i).setStatus(Integer.parseInt(jsonObj.get("Estado").toString()));
                                            break;
                                        }
                                    }

                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();

                                    Frm_Principal.actualizarConversaciones();
                                }
                            }

                            break;
                        case "UNREGISTERED":
                            try{
                                gson = new Gson();
                                docente = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Docente.class);

                                if (docente.getId() == Integer.parseInt(jsonObj.get("IdDocente").toString())){
                                    Frm_Principal.cerrarSesion();
                                }
                            }catch(Exception ex){
                                Log.d(TAG + " CATCH", ex.getMessage());
                            }

                            break;

                        case "REPREGISTERED":
                            Log.d("EJVH REPREGISTERED", "idRepresentante: " + jsonObj.get("IdRepresentante").toString());
                            Frm_Principal.actualizarRegistroRepresentante(Integer.parseInt(jsonObj.get("IdRepresentante").toString()), 1);
                            break;

                        case "REPUNREGISTERED":
                            Log.d("EJVH REPUNREGISTERED", "idRepresentante: " + jsonObj.get("IdRepresentante").toString());
                            Frm_Principal.actualizarRegistroRepresentante(Integer.parseInt(jsonObj.get("IdRepresentante").toString()), 0);
                            break;
                        default:
                            // ERROR
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void confirmarRecepcion (int idMensaje, int estado){
        ArrayList<Object>  parametros = new ArrayList<>(3);
        parametros.add(0, "idMensaje*" + idMensaje);
        parametros.add(1, "estado*" + estado);
        parametros.add(2, "confirmarMensaje");

        Respuesta ws = new Respuesta();
        Object response = ws.getData(parametros);

        try
        {
            JSONObject jsonObj = new JSONObject(response.toString());
            String result = jsonObj.get("Result").toString();

            switch (result) {
                case "CONFIRMADO":
                    Log.d("EJVH CONFIRMACION", response.toString());
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e) {
            Log.d("EJVH CATCH", e.getMessage());
        }

    }

    private void mostrarNotification(String mensaje, long fecha, String nombreRepresentante, int idRepresentante)
    {
        if(sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        Intent targetIntent = new Intent(getApplicationContext(),Frm_Principal.class);
        targetIntent.putExtra("incoming",idRepresentante);
        PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, targetIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        long vibrate[] = {0,100,100};

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.teacher)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentTitle(nombreRepresentante)
                .setContentText(mensaje)
                .setVibrate(vibrate)
                .setWhen(fecha)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.docente_large));

        boolean isForeground = sPrefs.getBoolean(PROPERTY_IS_FOREGROUND,false);
        int currentTab = sPrefs.getInt(PROPERTY_CURRENT_TAB,0);
        int curIdRepresentante = sPrefs.getInt(PROPERTY_CURRENT_ID_REP,0);

        if (isForeground){
            if (currentTab == 0){
                mNotificationManager.notify(0,builder.build());
            }else{
                if (curIdRepresentante != idRepresentante){
                    mNotificationManager.notify(0,builder.build());
                }
            }
        }else{
            mNotificationManager.notify(0,builder.build());
        }
    }
}