package model.others;

import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;
import model.entities.Specialization;

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
}
