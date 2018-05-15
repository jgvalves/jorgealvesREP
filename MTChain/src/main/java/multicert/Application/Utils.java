package multicert.Application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public final class Utils {

    private Utils(){}

    public static UUID generateID(Hashtable<String, List<UUID>> IDHashtable){
        UUID uniqueID = UUID.randomUUID();

        try {
            Collection<List<UUID>> idLists = IDHashtable.values();

            for (List<UUID> idList : idLists) {
                if (idList.contains(uniqueID)) {
                    uniqueID = generateID(IDHashtable);
                }
            }

        }
        catch(Exception e){}
        return uniqueID;
    }

    public static void serialize(Object object, String name) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get("src/main/resources/JSON/" + name + ".jso")))) {
            oos.writeObject(object);
        }
    }

    /**
     * Deserialize AppUser object from file
     *
     * @param name The name of the user. Used to build file name ${name}.jso
     * @return
     * @throws Exception
     */
    static Object tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get("src/main/resources/JSON/" + name + ".jso"))) {
            return deserialize(name);
        }
        throw new IOException();
    }

    static Object deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get("src/main/resources/JSON/" + name + ".jso")))) {
            return decoder.readObject();
        }
    }
}
