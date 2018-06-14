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

public final class Util {
    private static Reflections reflections = new Reflections("de.uni_passau.fim.sommercamp.sc2.bots");
    private static Set<Class<? extends BaseBot>> bots = reflections.getSubTypesOf(BaseBot.class);

    private Util() { }

    static BaseBot getBot(S2Client client) {
        return getMultipleBots(1, Collections.singletonList(client)).get(0);
    }

    static List<BaseBot> getMultipleBots(int count, List<S2Client> clients) {
        if (count > clients.size()) {
            throw new IllegalArgumentException("Need separate client for every bot.");
        }

        Iterator<S2Client> clientIterator = clients.iterator();
        return bots.stream().filter(c -> !Modifier.isAbstract(c.getModifiers())).limit(count)
                .map(c -> instantiate(c, clientIterator.next())).collect(Collectors.toList());
    }

    static BaseBot getBotByName(String name, S2Client client) {
        return bots.stream().filter(c -> c.getCanonicalName().equals(name)).findFirst().map(c -> instantiate(c, client)).get();
    }

    private static BaseBot instantiate(Class<? extends BaseBot> c, S2Client client) {
        try {
            return c.getConstructor(S2Client.class).newInstance(client);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("The found bot cannot be instantiated with a constructor with only the argument of the game client");
        }
    }
}
