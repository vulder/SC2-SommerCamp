package de.uni_passau.fim.sommercamp.sc2;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Main entry point for stand-alone jar usage.
 */
public class Main {

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

    @Option(name = "-fps",
            metaVar = "FPS",
            usage = "Tries to enforce the given frame-rate (= simulation steps per second) " +
                    "by delaying bots that are too fast.")
    private int framerate = 20;

    enum AiMode {
        OFF, DEFENSIVE, OFFENSIVE
    }

    @Option(name = "-ai",
            metaVar = "AI_MODE",
            usage = "Sets the ai to {OFF, DEFENSIVE, OFFENSIVE} if available.")
    private AiMode ai = AiMode.OFF;

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

        BaseBot.FRAME_RATE = framerate;

        switch (ai) {
            case OFF:
                break; // nothing to do
            case DEFENSIVE:
            case OFFENSIVE:
                if (ClassLoader.getSystemResource(ai.name().toLowerCase() + File.separator + mapName) != null) {
                        mapName = ai.name().toLowerCase() + File.separator + mapName;
                }
                break;
        }

        if (multiPlayer) {
            MultiPlayerMain.run(mapName, Arrays.asList(botA, botB));
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
