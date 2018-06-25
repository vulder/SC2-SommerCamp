package de.uni_passau.fim.sommercamp.sc2;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

/**
 * Main entry point for stand-alone jar usage.
 */
public class Main {
    private static String MAP_EXTENSION = ".SC2Map";

    @Option(name = "-map",
            metaVar = "MAP",
            usage = "The name of the map file (with or without extension).")
    private String mapName = "Marines_2v2_d";

    @Option(name = "-bot1",
            metaVar = "BOT",
            required = true,
            usage = "The class name of the first or only bot.")
    private String botA = "ExampleBot";

    @Option(name = "-bot2",
            metaVar = "BOT",
            depends = {"-multiPlayer"},
            usage = "The class name of the second in 'Bot vs. Bot' mode.")
    private String botB = "ExampleBot";

    @Option(name = "-multiPlayer",
            depends = {"-bot2"},
            usage = "This activates the 'Bot vs. Bot' mode, you need to specify both bots.")
    private boolean multiPlayer = false;

    private void doMain(final String[] arguments) throws IOException {

        final CmdLineParser parser = new CmdLineParser(this);

        if (arguments.length < 1) {
            parser.printUsage(System.out);
            System.exit(-1);
        }

        try {
            parser.parseArgument(arguments);
        } catch (CmdLineException clEx) {
            System.out.println("ERROR: Unable to parse command-line options: " + clEx);
        }

        if (!mapName.endsWith(MAP_EXTENSION)) {
            mapName += MAP_EXTENSION;
        }

        if (multiPlayer) {
            MultiPlayerMain.run(mapName, botA, botB);
        } else {
            SinglePlayerMain.run(mapName, botA);
        }
    }

    public static void main(String[] args) {
        final Main instance = new Main();

        try {
            instance.doMain(args);
        } catch (Exception e) {
            System.out.println("ERROR: Exception encountered: " + e);
        }
    }
}
