package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for reflection api access to find and instantiate bots.
 */
public final class ReflectionUtil {

    static String MAP_EXTENSION = ".SC2Map";

    private static Reflections reflections = new Reflections("de.uni_passau.fim.sommercamp.sc2.bots");
    private static Set<Class<? extends BaseBot>> bots = reflections.getSubTypesOf(BaseBot.class);

    /**
     * Utility constructor.
     */
    private ReflectionUtil() { }

    /**
     *
     * @return
     */
    static List<String> getMaps() {
        List<String> filenames = new ArrayList<>();

        URI uri = null;
        try {
            uri = ControlGUI.class.getResource("/maps").toURI();

            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = null;
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/maps");
            } else {
                myPath = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(myPath, 1);
            for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                File file = it.next().toFile();
                if (file.isFile()) {
                    filenames.add(file.getName().replace(".SC2Map", ""));
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return filenames;
    }

    /**
     *
     * @param name
     * @return
     */
    static LocalMap getMapFromName(String name) {
        try {
            return LocalMap.of(IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("maps/" + (name.endsWith(MAP_EXTENSION) ? name : name + MAP_EXTENSION))));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a list of all available bots.
     *
     * @return a alphabetically sorted list of all bots in the class path
     */
    static List<String> getBotList() {
        return bots.stream().map(Class::getSimpleName).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    /**
     * Gets a list of all available bots.
     *
     * @return a alphabetically sorted list of all bots in the class path
     */
    static List<String> getBotList(File path) {
        return bots.stream().map(Class::getSimpleName).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }


    /**
     * Creates the first bot it can find for the given client.
     *
     * @param client the client for access to the game
     * @return the new bot
     */
    static BaseBot getBot(S2Client client) {
        return getMultipleBots(Collections.singletonList(client)).get(0);
    }

    /**
     * Creates a list of bots, one for each given client.
     *
     * @param clients the list of clients to use for game access
     * @return the list of bots
     */
    static List<BaseBot> getMultipleBots(List<S2Client> clients) {
        Iterator<S2Client> clientIterator = clients.iterator();
        return bots.stream().filter(c -> !Modifier.isAbstract(c.getModifiers())).limit(clients.size())
                .map(c -> instantiate(c, clientIterator.next())).collect(Collectors.toList());
    }

    /**
     * Creates a list of bots, one for each given class name and client.
     *
     * @param classNames the list of names of the Java classes of the bots
     * @param clients    the list of clients to use for game access
     * @return the list of bots
     */
    static List<BaseBot> getMultipleBots(List<String> classNames, List<S2Client> clients) {
        if (classNames.size() != clients.size()) {
            throw new IllegalArgumentException("Need separate client for every bot.");
        }

        Iterator<S2Client> clientIterator = clients.iterator();
        return classNames.stream().map(name -> getBotByName(name, clientIterator.next())).collect(Collectors.toList());
    }

    /**
     * Creates a bot for the given class name and client.
     *
     * @param name   the name of the Java class
     * @param client the client for access to the game
     * @return the bot
     */
    static BaseBot getBotByName(String name, S2Client client) {
        return bots.stream().filter(c -> c.getSimpleName().equals(name)).findFirst().map(c -> instantiate(c, client)).get();
    }

    /**
     * Instantiates a bot for the given class
     *
     * @param clazz  the Java class
     * @param client the client for access to the game
     * @return the newly created bot
     */
    private static BaseBot instantiate(Class<? extends BaseBot> clazz, S2Client client) {
        try {
            return clazz.getConstructor(S2Client.class).newInstance(client);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("The found bot cannot be instantiated with a constructor with only the argument of the game client", e);
        }
    }
}
