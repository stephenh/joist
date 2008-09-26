package org.exigencecorp.domainobjects.codegen.passes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.exigencecorp.domainobjects.codegen.Codegen;
import org.exigencecorp.domainobjects.codegen.dtos.Entity;
import org.exigencecorp.domainobjects.codegen.dtos.EnumEntity;
import org.exigencecorp.jdbc.Jdbc;
import org.exigencecorp.jdbc.RowMapper;

public class FindCodeValuesPass implements Pass {

    public void pass(Codegen codegen) {
        for (Entity entity : codegen.getEntities().values()) {
            if (entity.isEnum()) {
                final EnumEntity e = (EnumEntity) entity;
                Jdbc.query(codegen.getConfig().getDataSource(), "select id, code, name from " + e.getTableName() + " order by id", new RowMapper() {
                    public void mapRow(ResultSet rs) throws SQLException {
                        e.addCode(rs.getString("id"), rs.getString("code"), rs.getString("name"));
                    }
                });
            }
        }
    }

}
