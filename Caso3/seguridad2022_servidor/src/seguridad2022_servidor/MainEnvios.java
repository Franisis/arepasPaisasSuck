package seguridad2022_servidor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;



public class MainEnvios {


    public static void main (String args[])
    {
        Scanner scan = new Scanner(System.in);
        

        System.out.println("Número de clientes a probar (utilice de 8 a 64 clientes): ");
        int numClientes = scan.nextInt();

        //CyclicBarrier cb = new CyclicBarrier(numClientes,new Runnable(){
        
        for (int i =0; numClientes>i; i++)
        {
            
            Persona cliente = new Persona(i);
            cliente.run();
        }

        System.out.println("Todos los clientes terminaron su ejecuccion correctamente");
        System.out.println("Tiempo gastado en cifrar las consultas: " +Persona.getTcifrarConsulta() + " milisegundos");
        System.out.println("Tiempo gastado en generar codigos hmac: "+Persona.getTgenerarAuth() + " milisegundos");
        System.out.println("Tiempo gastado en verificar firmas: "+Persona.getTverificarFirma() + " milisegundos");
        System.out.println("Tiempo gastado en calcular G a la y: "+Persona.getCalG() + " milisegundos");
        
    }


}

    
