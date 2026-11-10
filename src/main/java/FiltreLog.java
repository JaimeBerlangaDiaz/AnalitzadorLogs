import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Classe encarregada de filtrar un flux de text línia per línia,
 * comptant les ocurrències d'una paraula clau específica passada per arguments.
 * Si rep un text buit, emet un error per la sortida estàndard d'errors i finalitza amb codi 1.
 */
public class FiltreLog {

    private static final String PARAULA_DEFECTE = "ERROR";
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;

    public static void main(String[] args) {
        String paraulaClau = obtenirParaulaClau(args);
        
        int comptadorOccurencies = 0;
        boolean textBuit = true;

        // Lectura eficient amb try-with-resources per garantir el tancament del buffer
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(System.in))) {
            String linia;
            while ((linia = lector.readLine()) != null) {
                textBuit = false; // Hem rebut almenys una línia de dades
                if (linia.contains(paraulaClau)) {
                    comptadorOccurencies++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error crític de lectura en el flux d'entrada: " + e.getMessage());
            System.exit(EXIT_ERROR);
        }

        // Validació del Nivell 2: Comprovació de text buit o nul
        if (textBuit) {
            System.err.println("Error: Text buit");
            System.exit(EXIT_ERROR);
        }

        // Retorn del resultat numèric final per la sortida estàndard
        System.out.println(comptadorOccurencies);
        System.out.flush();
        System.exit(EXIT_SUCCESS);
    }

    /**
     * Determina la paraula clau a cercar a partir dels arguments rebuts.
     */
    private static String obtenirParaulaClau(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            return args[0];
        }
        return PARAULA_DEFECTE;
    }
}