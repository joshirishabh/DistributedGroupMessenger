package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final String PROVIDER_URI = "edu.buffalo.cse.cse486586.groupmessenger2.provider";
    HashMap<String,String> map1 = new HashMap<String, String>();
    HashMap<String,String> map2 = new HashMap<String, String>();
    HashMap<String,String> map3 = new HashMap<String, String>();
    HashMap<String,String> map4 = new HashMap<String, String>();
    HashMap<String,String> map5 = new HashMap<String, String>();
    public Integer count =0;
    public Integer localCount =0;
    public String myPort_main =null;
    public Integer proposed_count =0;
    public Integer alive_avd0 =0;
    public Integer alive_avd1 =0;
    public Integer alive_avd2 =0;
    public Integer alive_avd3 =0;
    public Integer alive_avd4 =0;
    public Lock lock = new ReentrantLock();
    public Lock lock1 = new ReentrantLock();
    public Lock lockQueue = new ReentrantLock();

    PriorityQueue<Message> queue = new PriorityQueue<Message>(1000,new MessageComparator());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myPort_main = myPort;
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            Log.v("error", "testing "+e.getMessage());
            return;
        }
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        final EditText editText = (EditText) findViewById(R.id.editText1);
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.v("test", " it came here");
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                try {
                    new ClientTask1().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,myPort);
//                    Log.d("debug","test log");
                }
                catch (Exception ex){
                    Log.v("error","I am error"+ex.getMessage());
                }
            }
        });
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String,Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            while(true) {
                ServerSocket serverSocket = sockets[0];
                Socket socket = null;
                Integer portClient = -1;
                try {
                    socket = serverSocket.accept();
                    DataInputStream messageReceived = new DataInputStream(socket.getInputStream());
                    String message = messageReceived.readUTF();
                    Log.v("info of server",message+"zzzzz");
                    String []messageArray = message.split(Pattern.quote("|"));
                    portClient = Integer.parseInt(messageArray[2]);
                    Message pMessage = new Message();
                    if(messageArray[3].equals("proposal")){
                        String val1 = null;
                        if(myPort_main.equals(REMOTE_PORT0)){
                            val1 ="0";
                        }
                        else if(myPort_main.equals(REMOTE_PORT1)){
                            val1 ="1";
                        }
                        else if(myPort_main.equals(REMOTE_PORT2)){
                            val1 ="2";
                        }
                        else if(myPort_main.equals(REMOTE_PORT3)){
                            val1 ="3";
                        }
                        else if(myPort_main.equals(REMOTE_PORT4)){
                            val1 ="4";
                        }
                        Integer e = getCount();
                        lock.unlock();
                        pMessage.self_proposed_number = e;
                        pMessage.process_id = Integer.parseInt(val1);
                        pMessage.my_id = Integer.parseInt(messageArray[2]);
                        pMessage.status = "proposal";
                        pMessage.value = messageArray[0];
                        lockQueue.lock();
                        queueAdd(pMessage);
                        lockQueue.unlock();
                        String msgToSend = messageArray[0]+"|"+e.toString()+"|"+val1+"|"+"proposal";
                        DataOutputStream message0 = new DataOutputStream(socket.getOutputStream());
                        message0.writeUTF(msgToSend);
                        incerementCount();
                        String k = messageReceived.readUTF();
                        String []messageArraydecided = k.split(Pattern.quote("|"));
                        Log.v("message","message info of decision is "+k);
                        if(Integer.parseInt(messageArray[1])>getCount()) {
                            lock.unlock();
                            setCount(Integer.parseInt(messageArray[1]));
                            incerementCount();
                        }
                        Iterator it = queue.iterator();
//                        Log.v("queue", " queue yahan tak chala 1");
                        try {
                            /*while (it.hasNext()) {
                                Message m = (Message) it.next();
//                                Log.v("queue", " queue yahan tak chala 2");
//                                Log.v("Message", " message is "+m.value);
                                //&& m.status.equals("proposal")
                                if (m.self_proposed_number == Integer.parseInt(messageArraydecided[4])&& m.status.equals("proposal")) {
                                    Log.v("queue", " queue yahan tak chala 3");
                                    queue.remove(m);
                                    //Message msg = new Message();
                                    m.self_proposed_number = Integer.parseInt(messageArraydecided[1]);
                                    m.process_id = Integer.parseInt(messageArraydecided[2]);
                                    m.status = "decided";
//                                    msg.value = messageArray[0];
                                    queue.add(m);
                                    Iterator i = queue.iterator();
//                                    Log.v("queue", " queue yahan tak chala 4");
                                    break;
                                }
                            }*/
                            queueDelete(pMessage);
                            pMessage.self_proposed_number = Integer.parseInt(messageArraydecided[1]);
                            pMessage.process_id = Integer.parseInt(messageArraydecided[2]);
                            pMessage.status = "decided";
                            lockQueue.lock();
                            queueAdd(pMessage);
                            lockQueue.unlock();
                        }
                        catch (Exception ex) {
                            Log.v("queue Exception", "testing "+ex.getMessage());
                        }
                        Iterator i = queue.iterator();
                        while (i.hasNext()){
                            Message w = (Message) i.next();
                            Log.v("queue message"," messages are "+w.value+" number is "+w.self_proposed_number+"."+w.process_id);
                        }
                        try {
                            startDeletion();
                        } catch (Exception ex){
                            Log.v("queue", " queue reason "+ex);
                        }
                    }
                   /* else if(messageArray[3].equals("decided")) {

                        Log.v("message", " it will get saved "+messageArray[0]);
                        setCount(Integer.parseInt(messageArray[1]));
                        incerementCount();
                        Iterator it = queue.iterator();
//                        Log.v("queue", " queue yahan tak chala 1");
                        try {
                            while (it.hasNext()) {
                                Message m = (Message) it.next();
//                                Log.v("queue", " queue yahan tak chala 2");
//                                Log.v("Message", " message is "+m.value);
                                //&& m.status.equals("proposal")
                                if (m.self_proposed_number == Integer.parseInt(messageArray[4])&& m.status.equals("proposal")) {
                                    Log.v("queue", " queue yahan tak chala 3");
                                    queue.remove(m);
                                    //Message msg = new Message();
                                    m.self_proposed_number = Integer.parseInt(messageArray[1]);
                                    m.process_id = Integer.parseInt(messageArray[2]);
                                    m.status = "decided";
//                                    msg.value = messageArray[0];
                                    queue.add(m);
                                    Iterator i = queue.iterator();
//                                    Log.v("queue", " queue yahan tak chala 4");
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.v("queue Exception", ex.getMessage());
                        }
                        Iterator i = queue.iterator();
                        while (i.hasNext()){
                            Message w = (Message) i.next();
                            Log.v("queue message"," messages are "+w.value+" number is "+w.self_proposed_number+"."+w.process_id);
                        }
                        try {
                            while (!queue.isEmpty() ) {
                                Message t =  (Message) queue.peek();
                                Log.v(" queue ", " t is "+t.value+" proposed number is "+t.self_proposed_number+"."+t.process_id+" status is "+t.status);
                                if(t.status.equals("decided")) {
                                    Log.v("queue", "value are " +t.value);
                                    publishProgress(t.value);
                                    queue.remove(t);
                                }
                                else{
                                    break;
                                }
                            }
                        } catch (Exception ex){
                            Log.v("queue", " queue reason "+ex);
                        }


                    }*/

                } catch (SocketTimeoutException sx){
//                    socket.getPort()
                    displayQueue();
                    removeMessage(portClient);
                }
                catch (IOException e) {
//                    e.printStackTrace();
                    Log.e("Major issue"," Everything stopped reason is "+e);
                    try{
                        Integer a = socket.getPort();
                        Log.v(" socket ka javab"," socket nikla "+portClient.toString());
                    }
                    catch (Exception ex){
                        Log.e(" port nai"," phata aur fayda nai hua");
                    }
                    displayQueue();
                    removeMessage(portClient);
//                    return null;
                }
                catch (Exception ex){
                    Log.e("phata"," yahan abhi check kia"+ex.getMessage());
                    displayQueue();
                    removeMessage(portClient);
                }
            }
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
//            String [] messages = strReceived.split("\\|");
//            String key = messages[1]+"."+messages[2];
//            if(Integer.parseInt(messages[1])>=getCount()){
//                setCount(Integer.parseInt(messages[1])+1);
//            }
            ContentValues keyValueToInsert = new ContentValues();
            String key = getLocalCount().toString();
            lock1.unlock();
            incrementLocalCount();
            keyValueToInsert.put("key",key);
            keyValueToInsert.put("value", strReceived );
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(PROVIDER_URI);
            uriBuilder.scheme("content");
            Uri uri = uriBuilder.build();
            Uri newUri = getContentResolver().insert(uri, keyValueToInsert);
            return ;
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

//            String filename = "SimpleMessengerOutput";
//            String string = strReceived + "\n";
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e(TAG, "File write failed");
//            }
//
//            return;
        }
    }

    private class ClientTask1 extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
//            try {
                Log.d("info"," Inside client");
                Socket socket0 = null;
                Socket socket1 = null;
                Socket socket2 = null;
                Socket socket3 = null;
                Socket socket4 = null;


                /// My code


//                socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                        Integer.parseInt(REMOTE_PORT1));
//                socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                        Integer.parseInt(REMOTE_PORT2));
//                socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                        Integer.parseInt(REMOTE_PORT3));
//                socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                        Integer.parseInt(REMOTE_PORT4));
                int a = 0;
                String msgToSend = msgs[0];
                msgToSend = msgToSend+"|";
                Integer c = -1;
                String p_id = null;
                Message message = new Message();
                message.value = msgs[0];
                msgToSend = msgToSend + c.toString()+"|";
                if(msgs[1].equals(REMOTE_PORT0)){
                    msgToSend = msgToSend+"0";
                    p_id ="0";
                }
                else if(msgs[1].equals(REMOTE_PORT1)){
                    msgToSend = msgToSend+"1";
                    p_id ="1";
                }
                else if(msgs[1].equals(REMOTE_PORT2)){
                    msgToSend = msgToSend+"2";
                    p_id ="2";
                }
                else if(msgs[1].equals(REMOTE_PORT3)){
                    msgToSend = msgToSend+"3";
                    p_id ="3";
                }
                else if(msgs[1].equals(REMOTE_PORT4)){
                    msgToSend = msgToSend+"4";
                    p_id ="4";
                }

                a = c;
                msgToSend = msgToSend +"|"+"proposal";
//                incerementCount();
//                message.self_proposed_number = a;
//                message.proposed_number_0 = a;
//                message.process_id = Integer.parseInt(p_id);
//                message.status = "proposal";
//                queue.add(message);
            Log.v("info"," message to send"+ msgToSend);
                try {

                    socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT0));
                    socket0.setSoTimeout(5000);


//                        if(!msgs[1].equals(REMOTE_PORT0)) {

                            DataOutputStream message0 = new DataOutputStream(socket0.getOutputStream());
                            message0.writeUTF(msgToSend);
//                    message0.flush();
                              DataInputStream messageReceived = new DataInputStream(socket0.getInputStream());

//                    Log.v(" my info",messageReceived.readUTF()+"heyyy");
//                      Log.v(" my info2",messageReceived.readUTF());
                           String m = messageReceived.readUTF();
//                            Log.v(" my info", m + "heyyy");
                            String[] messageArray = m.split(Pattern.quote("|"));
                            message.proposed_number_0 = Integer.parseInt(messageArray[1]);
                           if (messageArray[3].equals("proposal")) {
                                if (c < Integer.parseInt(messageArray[1])) {
                                        c = Integer.parseInt(messageArray[1]);
                                        p_id = messageArray[2];
                                }
                                else if(c == Integer.parseInt(messageArray[1])){
                                     if(Integer.parseInt(messageArray[2])>Integer.parseInt(p_id)){
                                         p_id=messageArray[2];
                                    }
                                }
                            }
//                       }
//                    socket0.close();

                } catch (SocketTimeoutException s) {
                    alive_avd0 =1;
                    removeMessage(0);

                    Log.e("phata", "ClientTask UnknownHostException in port 0 removing message of 0");
                } catch (UnknownHostException e) {
                    alive_avd0 =1;
                    removeMessage(0);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 0");
                } catch (IOException e) {
                    alive_avd0 =1;
                    removeMessage(0);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 0");
                }

//            Log.v(" info"," idhar bhi i am succesfull");
                try {
//                    Log.v(" info", " i am succesfull");

                    socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT1));
                    socket1.setSoTimeout(5000);
//                    if(!msgs[1].equals(REMOTE_PORT1)) {

                        DataOutputStream message1 = new DataOutputStream(socket1.getOutputStream());
                        message1.writeUTF(msgToSend);
//                    message0.flush();
                        message1.flush();
                        DataInputStream messageReceived1 = new DataInputStream(socket1.getInputStream());
                        String[] messageArray1 = messageReceived1.readUTF().split(Pattern.quote("|"));
//                        messageReceived1.close();
                        message.proposed_number_1 = Integer.parseInt(messageArray1[1]);
                        if (messageArray1[3].equals("proposal")) {
                            if (c < Integer.parseInt(messageArray1[1])) {
                                c = Integer.parseInt(messageArray1[1]);
                                p_id = messageArray1[2];
                            }
                        }
                        else if(c == Integer.parseInt(messageArray1[1])){
                            if(Integer.parseInt(messageArray1[2])>Integer.parseInt(p_id)){
                                p_id=messageArray1[2];
                            }
                        }
//                    }
//                socket1.close();

                }catch (SocketTimeoutException sx){
                    alive_avd1 = 1;
                    removeMessage(1);
                }
                catch (SocketException s) {
                    Log.e("phata", "ClientTask UnknownHostException in port 1 removing message of 1");
                } catch (UnknownHostException e) {
                    alive_avd1 = 1;
                    removeMessage(1);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 1");
                } catch (IOException e) {
                    alive_avd1 = 1;
                    removeMessage(1);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 1");
                }
            Log.v(" info"," port 2 ke upar");

                try {
                    socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT2));
                    socket2.setSoTimeout(5000);

//                    if(!msgs[1].equals(REMOTE_PORT2)) {

                        DataOutputStream message2 = new DataOutputStream(socket2.getOutputStream());
                        message2.writeUTF(msgToSend);
//                    message0.flush();
                        message2.flush();
                        DataInputStream messageReceived2 = new DataInputStream(socket2.getInputStream());
                        String[] messageArray2 = messageReceived2.readUTF().split(Pattern.quote("|"));
                        message.proposed_number_2 = Integer.parseInt(messageArray2[1]);
                        if (messageArray2[3].equals("proposal")) {
                            if (c < Integer.parseInt(messageArray2[1])) {
                                c = Integer.parseInt(messageArray2[1]);
                                p_id = messageArray2[2];
                            }
                        }
                        else if(c == Integer.parseInt(messageArray2[1])){
                            if(Integer.parseInt(messageArray2[2])>Integer.parseInt(p_id)){
                                p_id=messageArray2[2];
                            }
                        }
//                    }
//                socket2.close();
                } catch (SocketTimeoutException sx){
                    alive_avd2 =1;
                    removeMessage(2);
                }catch (SocketException s) {
                    Log.e("phata", "ClientTask UnknownHostException in port 2");
                } catch (UnknownHostException e) {
                    alive_avd2 =1;
                    removeMessage(2);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 2");
                } catch (IOException e) {
                    alive_avd2 =1;
                    removeMessage(2);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 2");
                    Log.v("phata2", "io ki ai");
                }
            Log.v(" info"," port 3 ke upar");
                try {
                    socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT3));
                    socket3.setSoTimeout(5000);

//                     if(!msgs[1].equals(REMOTE_PORT3)) {

                         DataOutputStream message3 = new DataOutputStream(socket3.getOutputStream());
                         message3.writeUTF(msgToSend);
//                    message0.flush();
                         message3.flush();
                         DataInputStream messageReceived3 = new DataInputStream(socket3.getInputStream());
                         String[] messageArray3 = messageReceived3.readUTF().split(Pattern.quote("|"));
                         message.proposed_number_3 = Integer.parseInt(messageArray3[1]);
                         if (messageArray3[3].equals("proposal")) {
                             if (c < Integer.parseInt(messageArray3[1])) {
                                 c = Integer.parseInt(messageArray3[1]);
                                 p_id = messageArray3[2];
                             }
                             else if(c == Integer.parseInt(messageArray3[1])){
                                 if(Integer.parseInt(messageArray3[2])>Integer.parseInt(p_id)){
                                     p_id=messageArray3[2];
                                 }
                             }
                         }
//                     }
//                socket3.close();

                }catch (SocketTimeoutException sx){
                    alive_avd3 =1;
                    removeMessage(3);
                } catch (SocketException s) {
                    alive_avd3 =1;
                    removeMessage(3);
                    Log.e(TAG, "ClientTask UnknownHostException in port 3");
                } catch (UnknownHostException e) {
                    alive_avd3 =1;
                    removeMessage(3);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 3");
                } catch (IOException e) {

                    alive_avd3 =1;
                    removeMessage(3);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 3");
                    Log.v(" phata 3", "io phata");
                }
            Log.v(" info"," port 4 ke upar");
                try {
                    Log.v("info"," idhar bhi sab changa");

                    Log.v("info"," idhar bhi sab changa si");
                    socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT4));
                    socket4.setSoTimeout(5000);
//                    if (!msgs[1].equals(REMOTE_PORT4)) {

//                        Log.v("info"," idhar bhi sab changa si 1");
                        DataOutputStream message4 = new DataOutputStream(socket4.getOutputStream());
//                        Log.v("info"," idhar bhi sab changa si 2");
                        message4.writeUTF(msgToSend);
//                        Log.v("info"," idhar bhi sab changa si 3");
//                    message0.flush();
                        message4.flush();
                        DataInputStream messageReceived4 = new DataInputStream(socket4.getInputStream());
                        String[] messageArray4 = messageReceived4.readUTF().split(Pattern.quote("|"));
                        message.proposed_number_4 = Integer.parseInt(messageArray4[1]);
//                        Log.v("info"," idhar bhi sab changa si 4");
                        if (messageArray4[3].equals("proposal")) {
                            if (c < Integer.parseInt(messageArray4[1])) {
                                c = Integer.parseInt(messageArray4[1]);
                                p_id = messageArray4[2];
                            }
                            else if(c == Integer.parseInt(messageArray4[1])){
                                if(Integer.parseInt(messageArray4[2])>Integer.parseInt(p_id)){
                                    p_id=messageArray4[2];
                                }
                            }
                        }
//                    }
//                    socket4.close();

                       }
                catch (SocketTimeoutException sx){
                    alive_avd4 = 1;
                    removeMessage(4);
                    Log.e("issue"," I hope the queue deletion works for 4");
                }
                catch (SocketException s) {
                    alive_avd4 = 1;
                    removeMessage(4);
                    Log.e(TAG, "ClientTask UnknownHostException in port 4 removing messages of 4");
                } catch (UnknownHostException e) {
                    alive_avd4 = 1;
                    alive_avd4 = 1;
                    removeMessage(4);
//                    e.printStackTrace();
                    Log.e("Phata", "ClientTask UnknownHostException in port 4");
                } catch (IOException e) {
                    alive_avd4 = 1;
                    removeMessage(4);
//                    e.printStackTrace();
                    Log.e("issue"," I hope the queue deletion works for 4");
                    Log.v("phata 4", " io phata");
                } catch (Exception ex){
                    Log.v("error","checking bada wala error"+ex.getMessage());
                }
//            Log.v(" info1"," yahan tak chala");
            try {
                String r = msgs[0] + "|" + c.toString() + "|" + p_id + "|" + "decided";
                String r0 = r ;//+ "|" ;//+ message.proposed_number_0.toString();
                String r1 = r ;//+ "|" ;//+ message.proposed_number_1.toString();
                String r2 = r ;//+ "|" ;//+ message.proposed_number_2.toString();
                String r3 = r ;//+ "|" ;//+ message.proposed_number_3.toString();
                String r4 = r ;//+ "|" ;//+ message.proposed_number_4.toString();
//                Log.v(" info1"," yeah bhi chala");
               /* socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORT0));
                socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORT1));
                socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORT2));
                socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORT3));
                socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORT4));*/
                Log.v("meesage", " message to save" + msgs[0]);
                try {
                   try {
                       DataOutputStream message0 = new DataOutputStream(socket0.getOutputStream());

                       message0.writeUTF(r0);
                   }
                   catch (IOException ex){
                       removeMessage(0);
                       Log.e("issue"," I hope the queue deletion works for 4");
                   }
                   catch (Exception ex){
                       removeMessage(0);
                       Log.e("issue"," I hope the queue deletion works for 4");
                       Log.v("major"," what happened"+ex);
                   }
//                Log.v(" info1"," yeah bhi chala age");
                    try {
                        DataOutputStream message2 = new DataOutputStream(socket2.getOutputStream());
                        message2.writeUTF(r1);
                    }
                    catch (IOException ex){
                        Log.e("issue"," I hope the queue deletion works for 0");
                        removeMessage(2);
                    }
                    catch (Exception ex){
                        Log.e("issue"," I hope the queue deletion works for 0");
                        removeMessage(2);
                    }
//                Log.v(" info1"," yeah bhi chala age 2");
                    try {
                        DataOutputStream message1 = new DataOutputStream(socket1.getOutputStream());
                        message1.writeUTF(r2);
                    }
                    catch (IOException ex){
                        Log.e("issue"," I hope the queue deletion works for 1");
                        removeMessage(1);
                    }
                    catch (Exception ex){
                        Log.e("issue"," I hope the queue deletion works for 1");
                        removeMessage(1);
                    }
                    try {
                        DataOutputStream message3 = new DataOutputStream(socket3.getOutputStream());
                        message3.writeUTF(r3);
                    }catch (IOException ex){
                        Log.e("issue"," I hope the queue deletion works for 3");
                        removeMessage(3);
                    }
                    catch (Exception ex){
                        Log.e("issue"," I hope the queue deletion works for 3");
                        removeMessage(3);
                    }
                    try {
                        DataOutputStream message4 = new DataOutputStream(socket4.getOutputStream());
                        message4.writeUTF(r4);
                    }catch (IOException ex){
                        Log.e("issue"," I hope the queue deletion works for 4");
                        removeMessage(4);
                    }
                    catch (Exception ex){
                        Log.e("issue"," I hope the queue deletion works for 4");
                        removeMessage(4);
                    }


//                Log.v(" info1"," yeah bhi chala age last");
                    socket0.close();
                    socket1.close();
                    socket2.close();
                    socket3.close();
                    socket4.close();
                } catch (Exception ex) {
                    Log.v("error", "pata tha yahin phatega");
                }


            }//catch (SocketException s){
               // s.printStackTrace();
               // Log.e(" error",s.getMessage());
            //}
           // catch (IOException e) {
             //   e.printStackTrace();
            //    Log.e("error","yahan phata");
           // }
            catch (Exception ex){
                Log.v("chal","please don't fight "+ex);
            }
//            DataOutputStream message1 = new DataOutputStream(socket1.getOutputStream());
//                message1.writeUTF(msgToSend);
//                message1.flush();
//                DataOutputStream message2 = new DataOutputStream(socket2.getOutputStream());
//                message2.writeUTF(msgToSend);
//                message2.flush();
//                DataOutputStream message3 = new DataOutputStream(socket3.getOutputStream());
//                message3.writeUTF(msgToSend);
//                message3.flush();
//                DataOutputStream message4 = new DataOutputStream(socket4.getOutputStream());
//                message4.writeUTF(msgToSend);
//                message4.flush();

                /*
                 * TODO: Fill in your client code that sends out a message.
//                 */
//                socket0.close();
//                socket1.close();
//                socket2.close();
//                socket3.close();
//                socket4.close();
//            } catch (UnknownHostException e) {
//                Log.e(TAG, "ClientTask UnknownHostException");
//            } catch (IOException e) {
//                Log.e(TAG, "ClientTask socket IOException");
//            }
            return null;
        }
    }
    public synchronized Integer getCount(){
        lock.lock();
        return proposed_count;

    }

    public synchronized void incerementCount(){
        lock.lock();
        proposed_count++;
        lock.unlock();
    }
    public synchronized void setCount(Integer count){
        lock.lock();
        proposed_count = count;
        lock.unlock();
    }
    public synchronized void removeMessage(Integer number){
        Iterator it = queue.iterator();
        while (it.hasNext()) {
            Message m = (Message) it.next();
//                                Log.v("queue", " queue yahan tak chala 2");
//                                Log.v("Message", " message is "+m.value);
            //&& m.status.equals("proposal")
            if (m.my_id == number&& m.status.equals("proposal")) {
                Log.v("queue", " queue yahan tak chala 3");
                queueDelete(m);
                //Message msg = new Message();
            }
            displayQueue();
            startDeletion();
        }

    }
    public synchronized void displayQueue(){
        Iterator i = queue.iterator();
        while (i.hasNext()){
            Message w = (Message) i.next();
            Log.v("queue message"," messages are "+w.value+" number is "+w.self_proposed_number+"."+w.process_id+" status is "+w.status+ " originated from "+w.my_id);
        }
    }
    public synchronized void startDeletion(){
        while (!queue.isEmpty() ) {
            Message t =  (Message) queue.peek();
            Log.v(" queue ", " t is "+t.value+" proposed number is "+t.self_proposed_number+"."+t.process_id+" status is "+t.status+ " originated from "+ t.my_id+ " it is in start deletion");
            if(t.status.equals("decided")) {
                Log.v("queue", "value are " +t.value);
                insertValues(t.value);
                queueDelete(t);
            }
            else{
                break;
            }
        }
    }
    public synchronized void insertValues(String value){
        Log.v(" test"," insert value checking");
        ContentValues keyValueToInsert = new ContentValues();
        String key = getLocalCount().toString();
        lock1.unlock();
        incrementLocalCount();
        keyValueToInsert.put("key",key);
        keyValueToInsert.put("value",  value);
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(PROVIDER_URI);
        uriBuilder.scheme("content");
        Uri uri = uriBuilder.build();
        Uri newUri = getContentResolver().insert(uri, keyValueToInsert);
        return ;
    }
    public synchronized void queueAdd(Message m){
        queue.add(m);
    }
    public synchronized void queueDelete(Message m){
        queue.remove(m);
    }
    public synchronized void  incrementLocalCount(){
        lock1.lock();
        localCount++;
        lock1.unlock();
    }
    public synchronized Integer getLocalCount(){
        lock1.lock();
        return localCount;
    }
    public class Message{
        Integer proposed_number_0;
        Integer proposed_number_1;
        Integer proposed_number_2;
        Integer proposed_number_3;
        Integer proposed_number_4;
        Integer self_proposed_number;
        Integer decided_number;
        Integer process_id;
        Integer my_id;
        String status;
        String value;
        public Message(Integer proposed_number_0, Integer proposed_number_1, Integer proposed_number_2,
                       Integer proposed_number_3, Integer proposed_number_4,
                       Integer process_id, String status){
            this.proposed_number_0 = proposed_number_0;
            this.process_id = process_id;
            this.status = status;
        }
        public Message (){

        }
    }
    public class MessageComparator implements Comparator<Message>{
        public int compare(Message a, Message b){
            if(a.self_proposed_number>b.self_proposed_number){
                return 1;
            }
            else if(a.self_proposed_number==b.self_proposed_number){
                if (a.process_id>b.process_id){
                    return 1;
                }
                else{
                    return 0;
                }
            }
            else {
                return -1;
            }
        }
    }

}
