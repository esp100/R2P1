/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p1remot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.zip.*;

/**
 *
 * @author orozcos
 * 
 *  "\\" -> "/"
 */
public class SRecibe {
    
    public static String separ = System.getProperty("file.separator");
    public static int pto = 8000;
    private static String rutaServer = "";
    private static String rutaActual = "";
    private static String rutaZipT ="";
    private static File[] lista;
    
    
    
    public static void main(String[] args){
      try{
          ServerSocket s = new ServerSocket(pto);
          s.setReuseAddress(true);
          System.out.println("Servidor iniciado esperando por archivos..");
                    
          String ruta_archivos = iniciarRuta();
          File f2 = new File(ruta_archivos);
          f2.mkdirs();
          f2.setWritable(true);
          
          System.out.println(mDirectorios(f2, "",2,0));
          
          rutaServer = iniciarRuta();
          
          //loop de espera
          for(;;){
              Socket cl = s.accept();
              //System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
              DataOutputStream dos= new DataOutputStream(cl.getOutputStream());
              DataInputStream dis = new DataInputStream(cl.getInputStream());
              
              int comando = dis.readInt();
              
              //envArDirec( cl,  f2);
                //0 --  enviar lista  directorios
                if(comando == 0){
                    rutaActual=dis.readUTF();
                    directorioArch(cl,  dis,  rutaActual);
                    rutaActual="";
                    System.out.println(mDirectorios(f2, "",0,0));
                }//1 -- Envia al server
                else if(comando == 1){
                    cargarArch(dis);
                    System.out.println(mDirectorios(f2, "",0,0));
                }//2 -- Server envia
                else if(comando==2)
                    enviarArch(dis,dos);
                //3 - zip enviardir
                else if(comando==3){
                    enviarZip(dis,dos);
                }                    
                //5 -- Eliminar Archivo
                else if(comando==5)
                    eliminar(dis,dos);
                else if(comando == 7){
                    String pathDiectorio=dis.readUTF();
                    File director1=new File(pathDiectorio);
                    if(!director1.exists()){
                        director1.mkdirs();
                    }
                }
                else if (comando==-1) {
                    System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                    dos.writeUTF("Bienvenido");
                    dos.writeUTF(rutaServer);
                    dos.close();
                }
                else
                    System.out.println("comando invalido");
                
                dis.close();
              
              cl.close();
              
            System.out.println(mDirectorios(f2, "",0,0));
          }//for
          
      }catch(Exception e){
          e.printStackTrace();
          //main(args);
      }  
    }//main
    
    
    public static void enviarZip(DataInputStream dis, DataOutputStream dos) throws IOException{
        String recurso = dis.readUTF();
        int tipo = dis.readInt();
        
        File f = new File(recurso);
        
        String rZip = rutaZipT+f.getName()+".zip";
        System.out.println(rZip);
        File archivoZip = new File(rZip);
        
        
        FileOutputStream fos = new FileOutputStream(archivoZip);
        ZipOutputStream zos = new ZipOutputStream(fos);
            comprimirDirectorio(f,f.getName(),zos);
        zos.close();
        fos.close();
        
        System.out.println(rZip);
        System.out.println(archivoZip.getName());
        dos.writeUTF(archivoZip.getName());
        dos.flush();
        long tam=archivoZip.length();
        dos.writeLong(tam);
        dos.flush();
        
        long enviados = 0;
        int l=0,porcentaje=0;
        DataInputStream dis2 = new DataInputStream(new FileInputStream(archivoZip.getAbsolutePath()));
        while(enviados<tam){
            byte[] b = new byte[1500];
            l=dis2.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);// dos.write(b);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/tam);
            System.out.print("\rEnviado el "+porcentaje+" % del archivo");
        }//while
        archivoZip.delete();
        System.out.println("\nArchivo enviado..\n ");
        dis2.close();
        dos.close();
        
    }
    
    public static void comprimirDirectorio(File directorio, String nombre, ZipOutputStream zos) throws IOException{
        
        File[] archivos = directorio.listFiles();

        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    comprimirDirectorio(archivo, nombre + "/" + archivo.getName(), zos);
                } else {
                    FileInputStream fis = new FileInputStream(archivo);
                    ZipEntry zipEntry = new ZipEntry(nombre + "/" + archivo.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    fis.close();
                    zos.closeEntry();
                }
            }
        }
    }
    
    public static void eliminar(DataInputStream dis, DataOutputStream dos) throws IOException{
        String directorio = dis.readUTF();
        int tipo = dis.readInt();
        File f = new File(directorio);
        
        if (tipo==1) {
            deleteFolder(f);
            dos.writeUTF("Directorio eliminado con exito");            
        }else if(tipo==2){
            f.delete();
            dos.writeUTF("Archivo eliminado con exito");
        }
        dos.close();
    }
    public static void deleteFolder(File fdir){
        File[] f = fdir.listFiles();
        if(f!=null){
            for(File ff: f){
                if (ff.isDirectory()) {
                    deleteFolder(ff);
                }else
                    ff.delete();
            }
        }
        fdir.delete();
    }
    
    public static void enviarArch(DataInputStream dis, DataOutputStream dos) throws IOException{
        String recurso = dis.readUTF();
        int tipo = dis.readInt();
        
        File f = new File(recurso);
        System.out.println(recurso);
        System.out.println(f.getName());
        dos.writeUTF(f.getName());
        dos.flush();
        long tam=f.length();
        dos.writeLong(tam);
        dos.flush();
        
        long enviados = 0;
        int l=0,porcentaje=0;
        DataInputStream dis2 = new DataInputStream(new FileInputStream(f.getAbsolutePath()));
        while(enviados<tam){
            byte[] b = new byte[1500];
            l=dis2.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);// dos.write(b);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/tam);
            System.out.print("\rEnviado el "+porcentaje+" % del archivo");
        }//while
        System.out.println("\nArchivo enviado..\n ");
        dis2.close();
        dos.close();
        
    }
    
    
    public static void cargarArch(DataInputStream dis2) throws IOException{
        String nombreExt = dis2.readUTF();
        String ruta = dis2.readUTF();
        long tam = dis2.readLong();
        
        System.out.println("Comienza descarga del archivo "+nombreExt+" de "+tam+" bytes en la ruta"+nombreExt+"\n\n");
        DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(ruta+nombreExt));

        long recibidos=0;
        int l=0, porcentaje=0;
        while(recibidos<tam){
            byte[] b = new byte[1500];
            l = dis2.read(b);
            System.out.println("leidos: "+l);
            dos2.write(b,0,l);
            dos2.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/tam);
            System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
        }//while
        System.out.println("Archivo "+nombreExt+" de tamaño "+tam+"  recibido..");
        dos2.close();
        dis2.close();
    }//cargar archivos al sevidor
    
    public static String mDirectorios(File direc, String ident, int limit, int profun){
        try { 
            String arbolDirec ="";      
            File[] files = direc.listFiles();
            for(File file:files){
                //file.getCanonicalPath(
                if(file.isDirectory()){
                    arbolDirec = (limit<=profun)? arbolDirec+"\n"+ident+"d: "+file.getName() : arbolDirec+"\n"+ident+"d: "+file.getName()+mDirectorios(file,ident+"   ",limit, profun+1);
                }else
                    arbolDirec =  arbolDirec +"\n"+ident+"f: "+file.getName();                
            }
            return arbolDirec;
        }catch (Exception ex) {
            Logger.getLogger(SRecibe.class.getName()).log(Level.SEVERE, null, ex);
            return "error al mostrar directorios";
        }
    }
    
    public static void envArDirec(DataOutputStream dos2, File fd) throws IOException{
            //DataOutputStream dos2 = new DataOutputStream(cl.getOutputStream());
            dos2.writeUTF(mDirectorios(fd, "",2,0));
            dos2.flush();
            //dos2.close();
    }
    
    
    public static void directorioArch(Socket cl, DataInputStream dis, String path) throws IOException {
        File archivosRuta = new File(path);
        if (!archivosRuta.exists()) {
            archivosRuta.mkdir();
        }
        
        lista = archivosRuta.listFiles();
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
        dos.writeInt(lista.length);
        dos.flush();

        String direc = "";
        int tipo = 0;

        for (int i = 0; i<lista.length;i++) {
            File f = lista[i];
            if (f.isDirectory()) {
                tipo = 1;
                direc = path+ f.getName();
            }//if directorio
            else {
                tipo = 2;
                direc = path + f.getName();
            }//else
            dos.writeUTF(direc);
            dos.flush();
            dos.writeInt(tipo);
            dos.flush();
            tipo = 0;
        }//for
        dos.close();
        System.out.println("directorio enviado al cliente");
    }//directorioArch
    
    public static String iniciarRuta(){
        File f = new File("");
        String ruta = f.getAbsolutePath();
        String carpeta="archivos";
        String ruta_archivos = ruta+separ+carpeta+separ;
        System.out.println("ruta:"+ruta_archivos);

        File f2 = new File("");
        ruta = f2.getAbsolutePath();
        carpeta ="tmpZip";
        rutaZipT= ruta+separ+carpeta+separ;;
        //System.out.println(rutaZipT);
        if (!f2.exists()) {
            f2.mkdirs();
        }
        
        return ruta_archivos;
    }
    
}
