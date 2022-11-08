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

        CyclicBarrier cb = new CyclicBarrier(numClientes,new Runnable(){
            @Override
            public void run(){
                System.out.println("Todos los clientes terminaron su ejecuccion correctamente");
                System.out.println("Tiempo gastado en cifrar las consultas: " +Persona.getTcifrarConsulta()/1000 + " segundos");
                System.out.println("Tiempo gastado en generar codigos hmac: "+Persona.getTgenerarAuth()/1000 + " segundos");
                System.out.println("Tiempo gastado en verificar firmas: "+Persona.getTverificarFirma()/1000 + " segundos");
                System.out.println("Tiempo gastado en calcular G a la y: "+Persona.getCalG()/1000 + " segundos");
            }
        });
        for (int i =0; numClientes>i; i++)
        {
            
            Persona cliente = new Persona(i,cb);
            cliente.run();
        }
    }


}

    
