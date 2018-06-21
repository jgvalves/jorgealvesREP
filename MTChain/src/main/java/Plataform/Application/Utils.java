package Plataform.Application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

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
                Paths.get("./src/", (name + ".jso"))))) {
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
    public static Object tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get("./src/", (name + ".jso")))) {
            return deserialize(name);
        }
        throw new IOException();
    }

    private static Object deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get("./src/", (name + ".jso"))))) {
            return decoder.readObject();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static void writeFile(String str){
        try {
            List<String> lines = Arrays.asList(str);
            Path file = Paths.get("/home/jorge/Documents/MTChain/log.txt");
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
