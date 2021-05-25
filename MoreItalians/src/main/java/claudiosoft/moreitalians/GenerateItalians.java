package claudiosoft.moreitalians;

import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import javax.swing.JOptionPane;

/**
 * Sample job for SalefGeneratingJar project
 *
 * @author Claudio
 */
public final class GenerateItalians {

    final int RET_CODE_OK = 0;
    final int RET_CODE_ERR = 1;
    private final Class MAIN_CLASS = GenerateItalians.class;
    private final String DB_NAME = "anagraphic.txt";

    private List<String> nameDB;
    private List<String> surnameDB;
    private String workspaceFolder = "";

    // parameters
    private static boolean displayName;
    private static int nItaliansToGenerate;

    public static void main(String[] args) {
        displayName = true;
        nItaliansToGenerate = 1;
//        System.setProperty("nItaliansToGenerate", "5"); // debug
        parseParameters(args);

        GenerateItalians generator = new GenerateItalians();
    }

    public GenerateItalians() {
        this.nameDB = new LinkedList<>();
        this.surnameDB = new LinkedList<>();
        try {
            this.workspaceFolder = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath() + File.separator + ".." + File.separator + "workspace";
            new File(workspaceFolder).mkdirs();

            loadResources();
            for (int i = 0; i < nItaliansToGenerate; i++) {
                String name = getNewName();
                if (displayName) {
                    display(name);
                }
                updateDatabase(name);
            }
            exit(RET_CODE_OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            exit(RET_CODE_ERR);
        }
    }

    public static void parseParameters(String[] args) {
        for (int iAr = 0; iAr < args.length; iAr++) {
            if (args[iAr] == null || args[iAr].isEmpty() || !args[iAr].startsWith("-")) {
                continue;
            }
            String arg = args[iAr].substring(1);
            if (arg.equalsIgnoreCase("noDisplay")) {
                displayName = false;
            } else {
                System.out.println("unsupported argument: " + arg);
            }
        }
        String nItalians = System.getProperty("nItaliansToGenerate", "");
        if (!nItalians.isEmpty()) {
            try {
                nItaliansToGenerate = Integer.parseInt(nItalians);
            } catch (NumberFormatException ex) {
                System.out.println("unsupported property: " + nItalians);
            }
        }
    }

    public void loadResources() throws IOException {
        File nameFile = Utils.getFileFromRes("nomi_italiani.txt");
        nameDB = Files.readAllLines(nameFile.toPath());

        File surnameFile = Utils.getFileFromRes("cognomi_italiani.txt");
        surnameDB = Files.readAllLines(surnameFile.toPath());
    }

    public String getNewName() {
        String nameSurname = "";
        Random generator = new Random();
        int rndName = 1 + generator.nextInt(nameDB.size());
        int rndSurname = 1 + generator.nextInt(surnameDB.size());
        nameSurname = String.format("%s %s", nameDB.get(rndName), surnameDB.get(rndSurname));
        nameSurname = nameSurname.substring(0, 1).toUpperCase() + nameSurname.substring(1); // capitalize the name
        return nameSurname;
    }

    public void display(String newName) {
        System.out.println("Ciao " + newName);

        JOptionPane.showMessageDialog(null, newName, "Ciao", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateDatabase(String newName) throws IOException {
        File dbFile = new File(workspaceFolder + File.separator + DB_NAME);
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Files.write(dbFile.toPath(), String.format("%s - %s\n", newName, sdf.format(new Date())).getBytes(), StandardOpenOption.APPEND);
    }
}
