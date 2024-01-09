
package p1remot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author orozcos
 */
public class CEnvia {
    
    public static int[] tipoFile;
    public static String[] nomFile;
    public static int pto = 8000;
    public static String dir = "127.0.0.1";
    private static String pathServer ="";
    private static String pathActual ="";
    public static String separ = System.getProperty("file.separator");
    public static String pathDescarga="";
    
    
    public static String iniciarRuta(){
        File f = new File("");
        String ruta = f.getAbsolutePath();
        String carpeta="archivosDescarga";
        String ruta_archivos = ruta+separ+carpeta+separ;
        System.out.println("ruta:"+ruta_archivos);
        return ruta_archivos;
    }
    
    public static void main(String[] args){
        try{
            Socket cl = new Socket(dir,pto);
            System.out.println("servidor activo para peticiones...");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            /*System.out.println("Directorio Remoto actual:");
                    direcRemot();*/
            DataInputStream dis = new DataInputStream(cl.getInputStream());
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
            dos.writeInt(-1);
            dos.flush();
            System.out.println(">"+dis.readUTF());
            pathServer= dis.readUTF();
            pathActual= pathServer;
            System.out.println("    rutaServer: "+pathActual);
            dos.close();
            dis.close();
            cl.close();
            pathDescarga=iniciarRuta();
            direcRemot();
            
            for(;;){
                //revDirectRem(cl);
                System.out.println("\n\nSeleccione opción\n99 - salir\n0 - Revisar directorio\n1 - subir archivo\n2 - descargar archivo\n3 - navegar directorio\n5 - Eliminar directorios\n8 - Regresar al path origen");
                int comando = Integer.parseInt(br.readLine());
                /*DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                dos.writeInt(comando);
                dos.flush();  */   
                
                if(comando == 0){ // directorios
                    System.out.println("\nDirectorio Remoto:");
                    direcRemot();
                }else if(comando == 1){
                    enviaArch();
                    //main(args);
                }else if(comando ==2){
                    direcADescargar1(br);
                
                }else if(comando ==3){
                    navDirec(br);
                }else if(comando ==5){
                    System.out.println("eliminar directorios");
                    direcAEliminar(br);
                }
                else if(comando == 8){
                    System.out.println(" cambio de ruta: "+pathActual);
                    pathActual= pathServer;
                    System.out.print(" -> actual: "+pathActual+"\n");
                    direcRemot();
                }else if(comando == 99){
                    cl.close();
                    System.exit(0);
                }else
                    System.out.println("Comando invalido");
                
                //dos.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("error de comunicación");
        }//catch
    }//main
    
    
    public static void direcADescargar1(BufferedReader br) throws IOException{
        System.out.println("");
        for (int i = 0; i < tipoFile.length; i++) {
            System.out.print(i);
            System.out.print( ( tipoFile[i]==1?" D ":" F " ) );
            String aux = nomFile[i].replaceFirst(pathServer, separ);
            System.out.print(aux+"\n");
        }
        
        for(;;){
            int dirInd = Integer.parseInt(br.readLine());
            if (dirInd<tipoFile.length && 0<=dirInd ) {
                //if (tipoFile[dirInd]==2) {
                    
                
                int tipo = tipoFile[dirInd];
                String direc = nomFile[dirInd];
                System.out.println("se descargará "+nomFile[dirInd].replaceFirst(pathServer, separ));
                System.out.println("en "+pathDescarga+nomFile[dirInd].replaceFirst(pathServer, ""));
                
                //Path relativo=Paths.get(pathDescarga+nomFile[dirInd].replaceFirst(pathServer, ""));
                File fref=new File(pathDescarga+nomFile[dirInd].replaceFirst(pathServer, ""));
                String aux = fref.getParent();
                fref=new File(fref.getParent());
                if (!fref.exists()) 
                    fref.mkdirs();
                
                prepDescarga1(tipo,direc,aux);
                
                break;
                /*}else 
                System.out.println("Ingrese un índice de archivo válido");*/
            } else 
                System.out.println("Ingrese un índice de archivo válido");
            
        }
    }
    public static void prepDescarga1(int tipo, String direc, String local) throws IOException{
        Socket cl = new Socket(dir, pto);
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
        DataInputStream dis = new DataInputStream(cl.getInputStream()); 
        
        if (tipo == 2)
            dos.writeInt(2);
        else 
            dos.writeInt(3);
        dos.flush();
        
        System.out.println(direc);
        System.out.println(tipo);
        System.out.println(local);
        dos.writeUTF(direc);
        dos.writeInt(tipo);
        dos.flush();
        
        String nombreExt = dis.readUTF();
        long tam = dis.readLong();
        
        
        System.out.println("Comienza descarga del archivo "+nombreExt+" de "+tam+" bytes en la ruta "+local+"\n");
        DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(local+separ+nombreExt));
        
        long recibidos=0;
        int l=0, porcentaje=0;
        while(recibidos<tam){
            byte[] b = new byte[1500];
            l = dis.read(b);
            System.out.println("leidos: "+l);
            dos2.write(b,0,l);
            dos2.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/tam);
            System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
        }//while
        System.out.println("Archivo "+nombreExt+" de tamaño "+tam+"  recibido..");
        
        System.out.println("Archivo "+nombreExt+" de tamaño "+tam+"  recibido..");
        dos2.close();
        dis.close();
        cl.close();
    }
    
    
    public static void direcAEliminar(BufferedReader br) throws IOException{
        System.out.println("");
        for (int i = 0; i < tipoFile.length; i++) {
            System.out.print(i);
            System.out.print( ( tipoFile[i]==1?" D ":" F " ) );
            String aux = nomFile[i].replaceFirst(pathServer, separ);
            System.out.print(aux+"\n");
        }
        
        for(;;){
            int dirInd = Integer.parseInt(br.readLine());
            if (dirInd<tipoFile.length && 0<=dirInd) {
                int tipo = tipoFile[dirInd];
                String direc = nomFile[dirInd];
                System.out.println("se eliminará "+nomFile[dirInd].replaceFirst(pathServer, separ));
                
                enviDirecBorrar(tipo,direc);
                
                break;
            } else 
                System.out.println("Ingrese un índice de directorio válido");
            
        }
    }
    
    public static void enviDirecBorrar(int tipo, String direc) throws IOException{
        Socket cl = new Socket(dir, pto);
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); 
        DataInputStream dis = new DataInputStream(cl.getInputStream()); 
        
        dos.writeInt(5);
        dos.flush();
        
        dos.writeUTF(direc);
        dos.writeInt(tipo);
        dos.flush();
        
        System.out.println(dis.readUTF());
        
        dos.close();
        dis.close();
        cl.close();
        
    }
    
    public static boolean enviaArch(File f, String pathDesti) throws IOException{
        Socket cl = new Socket(dir, pto);
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
        String nombre = f.getName();
        String path = f.getAbsolutePath();
        
        dos.writeInt(1);

        long tam = f.length();
            System.out.println("Preparandose pare enviar archivo "+path+" de "+tam+" bytes\n\n");
            DataInputStream dis = new DataInputStream(new FileInputStream(path));
            dos.writeUTF(nombre);
            dos.flush();
            dos.writeUTF(pathDesti);
            dos.flush();
            dos.writeLong(tam);
            dos.flush();
            long enviados = 0;
            int l=0,porcentaje=0;
            while(enviados<tam){
                byte[] b = new byte[1500];
                l=dis.read(b);
                System.out.println("enviados: "+l);
                dos.write(b,0,l);// dos.write(b);
                dos.flush();
                enviados = enviados + l;
                porcentaje = (int)((enviados*100)/tam);
                System.out.print("\rEnviado el "+porcentaje+" % del archivo");
            }//while
            System.out.println("\nArchivo enviado..\n ");
            dis.close();
            dos.close();
            cl.close();
        return true;
    }
    public static boolean enviaArch() throws IOException{
        Socket cl = new Socket(dir, pto);
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
            
        System.out.println("Selecciona archivo, lanzando FileChooser..");
        JFileChooser jf = new JFileChooser();
        jf.setMultiSelectionEnabled(true);
        jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int r;
                r=jf.showOpenDialog(null);
        if(r==JFileChooser.APPROVE_OPTION){
            File f = jf.getSelectedFile();
            File ff= new File("");
             String z = ff.getAbsolutePath()+"/";
             System.out.println("Ruta:"+z);
            
            String nombre = f.getName();
            String path = f.getAbsolutePath();
            
            if (f.isDirectory()) {
                
                System.out.println("es directorio : "+nombre+" Ruta: "+path);
                
                dos.writeInt(7);
                dos.flush();
                
                dos.writeUTF(pathActual+nombre);
                 
                dos.close();
                cl.close();
                File[] subfiles = f.listFiles();
                for(File sfile : subfiles){
                    String subpath = pathActual+nombre+separ;
                    enviaArch(sfile,subpath);
                }
            }else if(f.isFile()){
                dos.writeInt(1);
                long tam = f.length();
                System.out.println("Preparandose pare enviar archivo "+path+" de "+tam+" bytes\n\n");
                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeUTF(pathActual);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                long enviados = 0;
                int l=0,porcentaje=0;
                while(enviados<tam){
                    byte[] b = new byte[1500];
                    l=dis.read(b);
                    System.out.println("enviados: "+l);
                    dos.write(b,0,l);// dos.write(b);
                    dos.flush();
                    enviados = enviados + l;
                    porcentaje = (int)((enviados*100)/tam);
                    System.out.print("\rEnviado el "+porcentaje+" % del archivo");
                }//while
                System.out.println("\nArchivo enviado..\n ");
                dis.close();
                dos.close();
                cl.close();
                
            }
            }//if
        jf.removeAll();
            return true;
    }//envia arch
    
    
    public static void direcRemot() throws IOException{
        Socket cl = new Socket(dir,pto);
        DataInputStream dis = new DataInputStream(cl.getInputStream());
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
        dos.writeInt(0);
        dos.writeUTF(pathActual);
        dos.flush();
        
        int nDirec = dis.readInt();
        tipoFile = new int[nDirec];
        nomFile = new String[nDirec];
        System.out.println("");
        for (int i = 0; i < nDirec; i++) {
            nomFile[i] = dis.readUTF();
            tipoFile[i] = dis.readInt();
            System.out.print(i+((tipoFile[i]==1)?" d: ":" f: "));
            String aux = nomFile[i].replaceFirst(pathServer, separ);
            System.out.print(aux+"\n");
        }        
        dos.close();
        dis.close();
        
        cl.close();
    }
    public static void navDirec(BufferedReader br) throws IOException {
        for (int i = 0; i < tipoFile.length; i++) {
            if (tipoFile[i]==1){
                String aux = nomFile[i].replaceFirst(pathServer, separ);
                System.out.println(i+"  "+aux);
                /*File replic1=new File(pathDescarga+nomFile[i].replaceFirst(pathServer, ""));
                if(!replic1.exists())
                    replic1.mkdirs();*/
        }}
            System.out.println("seleccione el directorio");
        for(;;){
            int dirInd = Integer.parseInt(br.readLine());
            if (dirInd<tipoFile.length) {
                if (dirInd < 0){
                    pathActual=pathServer;
                    break;
                }
                if (tipoFile[dirInd]==1) {
                    pathActual=nomFile[dirInd]+separ;
                    break;
                }else{
                    System.out.println(nomFile[dirInd]+" no es un directorio");
                }
            }else{
                System.out.println("Ingrese un índice válido");
            }
        }
        System.out.println(pathActual);
        direcRemot();
    }
}
