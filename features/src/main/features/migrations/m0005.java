package features.migrations;

import static joist.domain.migrations.MigrationKeywords.createTable;
import static joist.domain.migrations.MigrationKeywords.foreignKey;
import static joist.domain.migrations.MigrationKeywords.integer;
import static joist.domain.migrations.MigrationKeywords.primaryKey;
import static joist.domain.migrations.MigrationKeywords.varchar;
import joist.domain.migrations.AbstractMigration;

public class m0005 extends AbstractMigration {

    public m0005() {
        super("Parent with multiple children.");
    }

    public void apply() {
        createTable("parent_b_parent", primaryKey("id"), varchar("name"), integer("version"));
        createTable("parent_b_child_foo", primaryKey("id"), foreignKey("parent_b_parent"), varchar("name"), integer("version"));
        createTable("parent_b_child_bar", primaryKey("id"), foreignKey("parent_b_parent"), varchar("name"), integer("version"));
    }

}