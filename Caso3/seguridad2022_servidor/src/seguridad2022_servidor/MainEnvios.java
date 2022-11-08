package seguridad2022_servidor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;



public class MainEnvios {

    public static ArrayList personas;
    

    public static void main (String args[])
    {
        Scanner scan = new Scanner(System.in);
        

        System.out.println("Número de clientes a probar (utilice de 8 a 64 clientes): ");
        int numClientes = scan.nextInt();

        CyclicBarrier cb = new CyclicBarrier(numClientes,new Runnable(){
            @Override
            public void run(){
                System.out.println("Todos los clientes terminaron su ejecuccion correctamente");
            }
        });
        for (int i =0; numClientes>i; i++)
        {
            
            Persona cliente = new Persona(i,cb);
            cliente.run();
        }
    }


}

    
