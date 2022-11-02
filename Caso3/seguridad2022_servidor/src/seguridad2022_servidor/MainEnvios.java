package seguridad2022_servidor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class MainEnvios {

    public static ArrayList personas;
    

    public static void main (String args[])
    {
        Scanner scan = new Scanner(System.in);

        System.out.println("Número de clientes a probar (utilice de 8 a 64 clientes): ");
        int numClientes = scan.nextInt();

        for (int i =0; numClientes>i; i++)
        {
            int id = i;
            personas.add(new Persona(i));
        }
    }

    public void inicializarClientes()
    {

    }
}
