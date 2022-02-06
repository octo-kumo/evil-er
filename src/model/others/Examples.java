package model.others;

import com.google.gson.stream.JsonReader;
import main.rs.Converter;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.rs.Table;
import model.serializers.Serializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class Examples {
    public static void populate(ArrayList<Entity> entities) {
        Entity a, b, c, d;
        entities.add(a = new Entity("Entity A"));
        a.set(500, 500);
        a.addAttribute((Attribute) new Attribute().setName("haha").set(0, 100));
        a.addAttribute((Attribute) new Attribute().setName("nice").setWeak(true).set(200, 0));

        entities.add(b = new Entity("Entity B"));
        b.setWeak(true);
        b.set(300, 500);
        entities.add(c = new Entity("Entity C"));
        c.set(400, 600);
        c.addAttribute((Attribute) new Attribute().setName("haha").set(0, 100));
        c.addAttribute((Attribute) new Attribute().setName("nice").setWeak(true).set(200, 0));
        entities.add(d = new Entity("Entity D"));
        d.set(600, 600);
        d.addAttribute((Attribute) new Attribute().setName("haha").set(0, 100));
        d.addAttribute((Attribute) new Attribute().setName("nice").setWeak(true).set(200, 0));
        Relationship<Entity> r;
        entities.add(r = new Relationship<>("R"));
        r.addNode(a, new Relationship.RelationshipSpec("1", false));
        r.addNode(b, new Relationship.RelationshipSpec("N", true));
        r.set(400, 300);
        Specialization s;
        entities.add(s = new Specialization(a));
        s.addNode(c, new Relationship.RelationshipSpec(false));
        s.addNode(d, new Relationship.RelationshipSpec(true));
        s.set(500, 300);
    }

    public static void populateTables(ArrayList<Table> tables) {
        try {
            JsonReader reader = new JsonReader(new FileReader("C:\\Users\\zy\\OneDrive\\Documents\\diagram.dig"));
            ArrayList<Entity> deserialized = Serializer.deserialize(reader);
            Converter.convert(deserialized, tables);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
