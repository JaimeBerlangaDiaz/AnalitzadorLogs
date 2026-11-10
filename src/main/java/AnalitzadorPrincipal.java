import java.io.*;

/**
 * Classe orquestradora (pare) que gestiona un sistema multiprocés en paral·lel
 * mitjançant ProcessBuilder, aplicant canonades (IPC) i evitant bloquejos de buffer (Deadlocks).
 */
public class AnalitzadorPrincipal {

    private static final String CLASSE_FILL = "FiltreLog";
    private static final String FITXER_ERRORS = "errors_filtre.log";
    
    private static final String TEXT_PROVA = 
        """
        2026-11-09 INFO Inici del sistema
        2026-11-09 ERROR Fallada de connexi\u00f3 a BD
        2026-11-09 WARNING Mem\u00f2ria RAM al 85%
        2026-11-09 ERROR Fallada de disc dur
        2026-11-09 WARNING \u00das elevat de CPU
        2026-11-09 ERROR Excepci\u00f3 no controlada
        2026-11-09 WARNING Espai de disc baix
        """;

    public static void main(String[] args) {
        String classpath = obtenirClasspathCompilat();
        File fitxerErrors = new File(FITXER_ERRORS);

        try {
            // 1. Inicialització dels processos fills en paral·lel (Fill A: ERROR, Fill B: WARNING)
            Process procesErrors = crearIIniciarProcess(classpath, "ERROR", fitxerErrors);
            Process procesWarnings = crearIIniciarProcess(classpath, "WARNING", fitxerErrors);

            // 2. Transmissió concurrent de dades evitant Buffer Deadlocks mitjançant escriptura i flush
            enviarDadesAProces(procesErrors, TEXT_PROVA);
            enviarDadesAProces(procesWarnings, TEXT_PROVA);

            // 3. Lectura de les respostes dels fills des del seu InputStream
            String resultatErrors = llegirSortidaProces(procesErrors);
            String resultatWarnings = llegirSortidaProces(procesWarnings);

            // 4. Espera de finalització i obtenció dels codis de sortida
            int exitCodeError = procesErrors.waitFor();
            int exitCodeWarning = procesWarnings.waitFor();
            int exitCodeFinal = (exitCodeError == 0 && exitCodeWarning == 0) ? 0 : 1;

            // 5. Impressió del format estricte requerit per l'avaluació automàtica
            imprimirResultatFinal(resultatErrors, resultatWarnings, exitCodeFinal);

        } catch (Exception e) {
            System.err.println("S'ha produït un error durant l'orquestració dels processos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura i inicia un procés fill independent amb ProcessBuilder.
     */
    private static Process crearIIniciarProcess(String classpath, String paraulaClau, File fitxerErrors) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", classpath, CLASSE_FILL, paraulaClau);
        pb.redirectError(fitxerErrors); // Redirecció de stderr a fitxer físic (Nivell 2)
        return pb.start();
    }

    /**
     * Envia el text de prova al flux d'entrada del procés fill i aplica flush i tancament (EOF).
     */
    private static void enviarDadesAProces(Process proces, String text) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proces.getOutputStream()))) {
            writer.write(text);
            writer.flush(); // Assegura l'enviaments immediat de les dades i evita bloquejos
        } // El tancament automàtic envia el senyal EOF al fill
    }

    /**
     * Llegeix la única línia de resposta retornada per la sortida estàndard del fill.
     */
    private static String llegirSortidaProces(Process proces) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proces.getInputStream()))) {
            String linia = reader.readLine();
            return (linia != null) ? linia.trim() : "0";
        }
    }

    /**
     * Obté la ruta absoluta de target/classes per garantir la portabilitat de la execució.
     */
    private static String obtenirClasspathCompilat() {
        File targetClasses = new File("target" + File.separator + "classes");
        return targetClasses.exists() ? targetClasses.getAbsolutePath() : System.getProperty("java.class.path");
    }

    /**
     * Imprimeix el resultat final seguint estrictament el format de sortida de l'enunciat.
     */
    private static void imprimirResultatFinal(String errors, String warnings, int exitCode) {
        System.out.println("RESULTAT: ERRORS=" + errors + " | WARNINGS=" + warnings + " | EXIT_CODE=" + exitCode);
    }
}