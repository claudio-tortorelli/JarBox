package test;

import claudiosoft.selfjar.SelfJar;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlackBoxTests extends BaseJUnitTest {

    @BeforeClass
    public static void setUpClass() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Setup Error");
        }
    }

    @Test
    @Ignore
    public void t01CommandLine() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[10];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]addenv=nItaliansToGenerate=3";
        args[3] = "[sj]addpar=-noDisplay";
        SelfJar.main(args);
    }

    @Test
    @Ignore
    public void t01PrintInfo() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[2];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        SelfJar.main(args);
    }

    @Test
    @Ignore
    public void t02AddEnv() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]addenv=nItaliansToGenerate=3";
        SelfJar.main(args);
    }

    @Test
    @Ignore
    public void t03DelEnv() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]delenv=nItaliansToGenerate";
        SelfJar.main(args);
    }

    @Test
    @Ignore
    public void t04AddPar() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]addpar=-noDisplay";
        SelfJar.main(args);
    }

    @Test
    @Ignore
    public void t05DelPar() throws URISyntaxException, IOException, InterruptedException {
        String[] args = new String[3];
        args[0] = "[sj]info=true";
        args[1] = "[sj]loglevel=debug";
        args[2] = "[sj]delpar=-noDisplay";
        SelfJar.main(args);
    }

}
