package claudiosoft.moreitalians;

import static java.lang.System.exit;

/**
 * Sample job for SalefGeneratingJar project
 *
 * @author Claudio
 */
public final class GenerateItalians {

    final int RET_CODE_OK = 0;
    final int RET_CODE_ERR = 1;

    public static void main(String[] args) {
        parseArguments(args);

        GenerateItalians generator = new GenerateItalians();
    }

    public GenerateItalians() {
        try {
            loadResources();
            String name = getNewName();
            display(name);
            updateDatabase(name);
            exit(RET_CODE_OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            exit(RET_CODE_ERR);
        }
    }

    public static void parseArguments(String[] args) {

    }

    public void loadResources() {

    }

    public String getNewName() {
        String nameSurname = "";
        return nameSurname;
    }

    public void display(String newName) {

    }

    public void updateDatabase(String newName) {

    }
}
