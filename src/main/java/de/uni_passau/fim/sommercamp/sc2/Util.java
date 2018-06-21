package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for reflection api access to find and instantiate bots.
 */
public final class Util {
    private static Reflections reflections = new Reflections("de.uni_passau.fim.sommercamp.sc2.bots");
    private static Set<Class<? extends BaseBot>> bots = reflections.getSubTypesOf(BaseBot.class);

    /**
     * Utility constructor.
     */
    private Util() { }

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
            throw new IllegalArgumentException("The found bot cannot be instantiated with a constructor with only the argument of the game client");
        }
    }
}
