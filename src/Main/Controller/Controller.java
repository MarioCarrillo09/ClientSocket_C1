package Main.Controller;

import Main.Model.Ahorcado;
import Main.Model.ThreadClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ObservableFaceArray;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class Controller implements  Observer {
    private Socket socket;
    private DataOutputStream bufferDeSalida = null;
    private String[] diccionario = {"ABEJA","OSO", "BANANO","PLATANO", "PERRO",  "LOBO","MOSCA","PINGUINO", "ELEFANTE",
            "GATO", "SAPO", "JIRAFA", "COCODRILO","TORTUGA","CABALLO", "LEON", "TIGRE"};
    char[] palabra_secreta;
    private Ahorcado ahorcado;
    @FXML
    private Label labelPalabraOculta;

    @FXML
    private Button btnSalir;

    @FXML
    private Button btnConectar;

    @FXML
    private TextField ipServer;

    @FXML
    private TextField portServer;

    @FXML
    private TextArea log;

    @FXML
    private TextField txtEnviar;

    @FXML
    private Button btnEnviar;

    @FXML
    private Circle circle;
    @FXML
    private Label numIntentos;
    @FXML
    private Label labelWin;

    @FXML
    void btnConectarOnMouseClicked(MouseEvent event) {
        try {
            socket = new Socket(ipServer.getText(), Integer.valueOf(portServer.getText()));
            log.setText( "Creado");
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();

            ThreadClient cliente = new ThreadClient(socket);
            cliente.addObserver(this);
            new Thread(cliente).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnSalirOnMouseClicked(MouseEvent event) {
        Platform.exit();
        System.exit(1);

    }

    @FXML
    void btnCerrarOnMouseClicked(MouseEvent event) {
        try {
            socket.close();
            System.out.println("Cerrando...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnEnviarOnMouseClicked(MouseEvent event) {
        /*try {
            bufferDeSalida.writeUTF(txtEnviar.getText());
            log.appendText(txtEnviar.getText()+ "\n");
            bufferDeSalida.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if(ahorcado.isPlayAgain() && !ahorcado.isWordFound()){
            ahorcado.verificarPalabra(txtEnviar.getText().charAt(0));
            numIntentos.setText(String.valueOf(ahorcado.getNumStrike()));
            labelPalabraOculta.setText(String.valueOf(ahorcado.imprimirProgreso()));
        }else{
            verificarGano();
        }

    }

    @Override
    public void update(Observable o, Object arg) {

        if(o instanceof ThreadClient){
            //comprueba si lo que mando es una lista del diccionario
            for(int i=0;i<diccionario.length;i++){
                if(arg.equals(diccionario[i])){
                    Platform.runLater(() -> play((String) arg));
                }
            }


        }
    }
    void play(String palabraOculta){
        ahorcado = new Ahorcado(palabraOculta);
        ahorcado.generarPalabra();
        labelPalabraOculta.setText(String.valueOf(ahorcado.imprimirProgreso()));

    }
    void verificarGano(){
        if(ahorcado.isWordFound()== true){
            labelWin.setText("FELIICIDADES, GANASTE ERES TODO UN CRACK");
            try {
                bufferDeSalida.writeUTF("El Cliente ha acertado la palabra");
                bufferDeSalida.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            labelWin.setText("MAS SUERTE A LA PROXIMA :((");
            try {
                bufferDeSalida.writeUTF("El Cliente ha perdido");
                bufferDeSalida.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
