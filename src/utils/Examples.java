package utils;

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
        entities.add(a = new Entity("Student"));
        a.set(500, 200);
        a.addAttribute((Attribute) new Attribute().setKey(true).setName("id").set(0, -100));
        a.addAttribute((Attribute) new Attribute().setName("majors").setWeak(true).set(200, 0));

        entities.add(b = new Entity("Relative"));
        b.setWeak(true);
        b.set(300, 200);
        b.addAttribute((Attribute) new Attribute().setKey(true).setName("name").set(0, -100));
        b.addAttribute((Attribute) new Attribute().setName("relationship").set(-160, 0));

        entities.add(c = new Entity("Alumni"));
        c.set(400, 300);
        c.addAttribute((Attribute) new Attribute().setName("year").set(0, 130));
        c.addAttribute((Attribute) new Attribute().setName("graduated").set(-50, 100));
        Attribute grades = new Attribute();
        grades.addAttribute((Attribute) new Attribute().setName("subject").set(0, 80));
        grades.addAttribute((Attribute) new Attribute().setName("cap").set(80, 60));
        grades.setName("grades").setWeak(true).set(50, 100);
        c.addAttribute(grades);

        entities.add(d = new Entity("Dropout"));
        d.set(600, 300);
        d.addAttribute((Attribute) new Attribute().setName("year").set(-50, 100));
        d.addAttribute((Attribute) new Attribute().setName("reasons").setWeak(true).set(50, 100));
        Relationship<Entity> r;
        entities.add(r = new Relationship<>("related to"));
        r.setWeak(true);
        r.addNode(a, new Relationship.RelationshipSpec("1", false));
        r.addNode(b, new Relationship.RelationshipSpec("N", true));
        r.set(400, 100);
        Specialization s;
        entities.add(s = new Specialization(a));
        s.addNode(c, new Relationship.RelationshipSpec());
        s.addNode(d, new Relationship.RelationshipSpec());
    }
}
