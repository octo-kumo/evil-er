package model.serializers;

import model.er.Attribute;
import model.er.Entity;
import model.rs.Column;
import model.rs.Table;

import java.util.ArrayList;
import java.util.UUID;

import static main.renderer.DiagramGraphics.flatten;

public class VersionUpgrade {
    public static void upgradeTables(ArrayList<Table> tables) {
        tables.forEach(t -> {
            t.attributeMap.forEach((a, b) -> {
                if (b.getType() == null) b.setType(Column.DataType.VARCHAR);
                if (b.getParam() == null) b.setParam("8");
            });
        });
    }

    public static void upgradeEntities(ArrayList<Entity> entities) {
        flatten(entities).forEach(e -> {
            if (e.id == null) e.id = UUID.randomUUID().toString();
            if (e instanceof Attribute) {
                Attribute a = (Attribute) e;
                if (a.getDataType() == null) a.setDataType(Column.DataType.VARCHAR);
                if (a.getDataParam() == null) a.setDataParam("");
            }
        });
    }
}
