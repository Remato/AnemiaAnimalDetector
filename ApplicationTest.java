package pausingtoplay.anemia_test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;


public class ApplicationTest extends Activity implements View.OnTouchListener, CvCameraViewListener2 {

    private static final String  TAG              = "OCVSample::Activity";
    private Mat                  mRgba;
    private Scalar               BLUE            = new Scalar(0, 0, 255, 255);
    private Scalar               RED             = new Scalar(255, 0, 0, 255);
    private Scalar               GREEN           = new Scalar(0, 255, 0, 255);
    private Scalar               YELLOW          = new Scalar(255, 255, 0, 255);
    private Scalar               BLACK           = new Scalar(0, 0, 0, 0);

    public  double Pupila_Indice;
    public  double Pupila_R = 0;
    public  double Pupila_G = 0;
    public  double Pupila_B = 0;
    public  double Membrana_Indice = 0;
    public  double Membrana_R = 0;
    public  double Membrana_G = 0;
    public  double Membrana_B = 0;
    public  double Resultado = 0;
    public  String Anemia = "---";

    public  int    X;   //area vermelha
    public  int    Y;   //area vermelha
    public  int    X_2; //area preta
    public  int    Y_2; //area preta

    public  boolean Mutex_black  = false;
    public  boolean Mutex_red    = false;
    public  boolean Mutex_select = false;

    ///controle de seleção


    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ApplicationTest.this);
                } break;

                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        X   = width/2;
        Y   = height/2 + 50;
        X_2 = width/2;
        Y_2 = height/2;
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {

        int cols = mRgba.cols();
        ///960 colunas
        int rows = mRgba.rows();
        ///720 linhas

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        if ((X < 0) || (Y < 0) || (X > cols) || (Y > rows)) return false;

        if(Mutex_black) {
            X_2 = (int) event.getX() - xOffset;
            Y_2 = (int) event.getY() - yOffset;
            Mutex_select = true;
        }

        if(Mutex_red){
            X = (int) event.getX() - xOffset;
            Y = (int) event.getY() - yOffset;
            Mutex_select = true;
        }

        Mat SquarePupila    = mRgba.submat(Y_2 - 5, Y_2 + 5, X_2 - 5, X_2 + 5);
        Mat SquareMembrana  = mRgba.submat(Y - 5, Y + 5, X - 5, X + 5);

        if( ((int)event.getX() - xOffset) >= (X_2 - 20) && ((int)event.getX() - xOffset) <= (X_2 + 20) && ((int)event.getY() - yOffset) >= (Y_2 - 20) && ((int)event.getY() - yOffset) <= (Y_2 + 20) && !Mutex_select)
            Mutex_black = true;
        else if( ((int)event.getX() - xOffset) >= (X - 20) && ((int)event.getX() - xOffset) <= (X + 20) && ((int)event.getY() - yOffset) >= (Y - 20) && ((int)event.getY() - yOffset) <= (Y + 20) && !Mutex_select)
            Mutex_red   = true;

        if(Mutex_black && Mutex_select){
            Mutex_select = false;
            Mutex_black  = false;
        }

        else if(Mutex_red && Mutex_select){
            Mutex_red    = false;
            Mutex_select = false;
        }

        if( ((int)event.getX() - xOffset) >= (cols - 200) && ((int)event.getX() - xOffset) <= (cols - 10) && ((int)event.getY() - yOffset) >= (rows - 70) && ((int)event.getY() - yOffset) <= (rows - 10)) {    ///garantindo que acabou de mecher na tela

            Pupila_R = 0;
            Pupila_G = 0;
            Pupila_B = 0;

            Membrana_R = 0;
            Membrana_G = 0;
            Membrana_B = 0;
            Resultado = 0;
            Pupila_Indice = 0;
            Membrana_Indice = 0;


            double[] pixel;// percorredor

            Size sizePupila = SquarePupila.size();
            Size sizeMembrana = SquareMembrana.size();
            ///perguntar sobre analise do quarto canal pixel[3] luminosidade


            for (int i = 0; i < sizePupila.height; i++) {    //mat(y,x)
                for (int j = 0; j < sizePupila.width; j++) {

                    pixel = SquarePupila.get(i, j);
                    Pupila_R = Pupila_R + pixel[0];
                    Pupila_G = Pupila_G + pixel[1];
                    Pupila_B = Pupila_B + pixel[2];
                }
            }

            for (int i = 0; i < sizeMembrana.height; i++) {    //mat(y,x)
                for (int j = 0; j < sizeMembrana.width; j++) {

                    pixel = SquareMembrana.get(i, j);
                    Membrana_R = Membrana_R + pixel[0];
                    Membrana_G = Membrana_G + pixel[1];
                    Membrana_B = Membrana_B + pixel[2];
                }
            }
            ///Retirando media
            Membrana_R = Membrana_R / 100;
            Membrana_G = Membrana_G / 100;
            Membrana_B = Membrana_B / 100;

            Pupila_R = Pupila_R / 100;
            Pupila_G = Pupila_G / 100;
            Pupila_B = Pupila_B / 100;

            Pupila_Indice = Pupila_G / (Pupila_R + Pupila_G + Pupila_B);
            Membrana_Indice = Membrana_G / (Membrana_R + Membrana_G + Membrana_B);


            /// verificar função "modulo"
            Resultado = Pupila_Indice - Membrana_Indice;
            Resultado = Math.abs(Resultado);
            Resultado = Resultado / Pupila_Indice;


            //O Indice sempre está dando 0

            if (Resultado >= 0.5)
                Anemia = "Tem Anemia";
            else if (Resultado > 0.4 && Resultado < 0.5)
                Anemia = "90% de chance de ter Anemia";
            else if (Resultado > 0.3 && Resultado < 0.4)
                Anemia = "70% de chance de ter Anemia";
            else
                Anemia = "Nao tem Anemia";

        }

        return false; // don't need subsequent touch events
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();


        //regiões criticas
        //  cols/2 - 5, pixel inicial do quadrado
        //  cols/2 + 5, pixel final do quadrado

        int cols = mRgba.cols();
        int rows = mRgba.rows();
        String R1 = "R_Pupila:"+Pupila_R;
        String G1 = "G_Pupila:"+Pupila_G;
        String B1 = "B_Pupila:"+Pupila_B;

        String R2 = "R_Membrana:"+Membrana_R;
        String G2 = "G_Membrana:"+Membrana_G;
        String B2 = "B_Membrana:"+Membrana_B;

        String I_Pul  = "I_Pupila:"+ Pupila_Indice;
        String I_Mem  = "I_Membrana:"+ Membrana_Indice;
        String Result = "Resultado:"+ Resultado;
        String Process= "PROCESSA";

        Imgproc.putText(mRgba, R1, new Point(0,21), 2, 1, RED);
        Imgproc.putText(mRgba, G1, new Point(0,46), 2, 1, GREEN);
        Imgproc.putText(mRgba, B1, new Point(0,71), 2, 1, BLUE);

        Imgproc.putText(mRgba, I_Pul, new Point(0,96), 2, 1, YELLOW);

        Imgproc.putText(mRgba, R2, new Point(0,121), 2, 1, RED);
        Imgproc.putText(mRgba, G2, new Point(0,146), 2, 1, GREEN);
        Imgproc.putText(mRgba, B2, new Point(0,171), 2, 1, BLUE);

        Imgproc.putText(mRgba, I_Mem, new Point(0,196), 2, 1, YELLOW);

        Imgproc.putText(mRgba, Result, new Point(0, 221), 2, 1, YELLOW);
        Imgproc.putText(mRgba, Anemia, new Point(0, 246), 2, 1, YELLOW);

        Imgproc.rectangle(mRgba, new Point(X_2 - 6, Y_2 - 6), new Point(X_2 + 6, Y_2 + 6), BLACK, 1);
        ///fixo pupila
        Imgproc.rectangle(mRgba, new Point(X - 6, Y - 6), new Point(X + 6, Y + 6), RED, 1);
        ///altera esclera

        Imgproc.rectangle(mRgba, new Point(cols - 200, rows - 70), new Point(cols - 10, rows - 10), YELLOW, 3);
        Imgproc.putText(mRgba, Process, new Point(cols - 188, rows - 27), 2, 1, YELLOW);

        ///usa 1 scalar efeito para fazer mudar de cor e dar impressão de ter sido pressionado.
        return mRgba;
    }


}
