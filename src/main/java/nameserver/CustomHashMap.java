package nameserver;

import java.io.*;
import java.util.concurrent.ConcurrentSkipListMap;

// concurrentskiplistmap slaat nood voor locker over

public class CustomHashMap extends ConcurrentSkipListMap<Integer, String> {

    public void exportMap() throws IOException {
        FileOutputStream fo = new FileOutputStream("./src/main/java/map.txt");
        ObjectOutputStream out = new ObjectOutputStream(fo);
        out.writeObject(this);
        out.close();
        fo.close();
        System.out.println("Current Database save to ./src/main/map.txt");
    }

    public void importMap() throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream("./src/main/java/map.txt");
        ObjectInputStream in = new ObjectInputStream(fi);
        CustomHashMap c = (CustomHashMap) in.readObject();
        in.close();
        fi.close();

        for(ConcurrentSkipListMap.Entry<Integer,String> m :c.entrySet()){
            this.put(m.getKey(), m.getValue());
        }
        System.out.println("Database imported from ./src/main/map.txt");
    }

}
