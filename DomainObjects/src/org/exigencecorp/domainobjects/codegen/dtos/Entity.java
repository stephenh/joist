package org.exigencecorp.domainobjects.codegen.dtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.exigencecorp.domainobjects.codegen.Codegen;
import org.exigencecorp.domainobjects.codegen.CodegenConfig;
import org.exigencecorp.util.Inflector;

public class Entity {

    protected CodegenConfig config;
    private String tableName;
    private List<PrimitiveProperty> primitiveProperties = new ArrayList<PrimitiveProperty>();
    private List<ManyToOneProperty> manyToOneProperties = new ArrayList<ManyToOneProperty>();
    private List<OneToManyProperty> oneToManyProperties = new ArrayList<OneToManyProperty>();
    private List<ManyToManyProperty> manyToManyProperties = new ArrayList<ManyToManyProperty>();
    private Set<String> imports = new TreeSet<String>();
    private Entity baseEntity;
    private List<Entity> subEntities = new ArrayList<Entity>();

    public Entity(Codegen codegen, String tableName) {
        this.config = codegen.getConfig();
        this.tableName = tableName;
    }

    public String getParentClassName() {
        if (this.isSubclass()) {
            return this.baseEntity.getClassName();
        } else {
            return this.config.getDomainObjectBaseClass();
        }
    }

    public boolean hasSubclasses() {
        return this.subEntities.size() > 0;
    }

    public CodegenConfig getConfig() {
        return this.config;
    }

    public boolean isAbstract() {
        return this.hasSubclasses() && !this.config.isNotAbstractEvenThoughSubclassed(this.tableName);
    }

    public boolean isConcrete() {
        return !this.isAbstract();
    }

    public boolean isSubclass() {
        return this.getBaseEntity() != null;
    }

    public boolean isEnum() {
        return false;
    }

    public String getClassName() {
        return Inflector.camelize(this.getTableName());
    }

    public String getMapperClassName() {
        return this.getClassName() + "Mapper";
    }

    public String getMapperCodegenClassName() {
        return this.getClassName() + "MapperCodegen";
    }

    public String getCodegenClassName() {
        return this.getClassName() + "Codegen";
    }

    public String getFullCodegenClassName() {
        return this.config.getDomainObjectPackage() + "." + this.getCodegenClassName();
    }

    public String getFullAliasClassName() {
        return this.config.getMapperPackage() + "." + this.getClassName() + "Alias";
    }

    public String getFullClassName() {
        return this.config.getDomainObjectPackage() + "." + this.getClassName();
    }

    public String getFullMapperClassName() {
        return this.config.getMapperPackage() + "." + this.getMapperClassName();
    }

    public String getIdSequence() {
        return this.tableName + "_id_seq";
    }

    public String getSortBy() {
        return this.config.getOrder(this.tableName);
    }

    public List<ManyToOneProperty> getManyToOneProperties() {
        return this.manyToOneProperties;
    }

    public List<OneToManyProperty> getOneToManyProperties() {
        return this.oneToManyProperties;
    }

    public List<OneToManyProperty> getGeneratedIncomingForeignKeyProperties() {
        List<OneToManyProperty> properties = new ArrayList<OneToManyProperty>();
        for (OneToManyProperty fk : this.getOneToManyProperties()) {
            if (!fk.isNotGenerated()) {
                properties.add(fk);
            }
        }
        return properties;
    }

    public List<ManyToManyProperty> getManyToManyProperties() {
        return this.manyToManyProperties;
    }

    public List<PrimitiveProperty> getPrimitiveProperties() {
        return this.primitiveProperties;
    }

    public List<Property> getAllGeneratedProperties() {
        List<Property> all = new ArrayList<Property>();
        all.addAll(this.getPrimitiveProperties());
        all.addAll(this.getManyToOneProperties());
        all.addAll(this.getOneToManyProperties());
        all.addAll(this.getManyToManyProperties());
        for (Iterator<Property> i = all.iterator(); i.hasNext();) {
            if (i.next().isNotGenerated()) {
                i.remove();
            }
        }
        return all;
    }

    public List<Property> getIncomingProperties() {
        List<Property> incoming = new ArrayList<Property>();
        incoming.addAll(this.getOneToManyProperties());
        incoming.addAll(this.getManyToManyProperties());
        return incoming;
    }

    public String getTableName() {
        return this.tableName;
    }

    public Set<String> getImports() {
        return this.imports;
    }

    public Entity getBaseEntity() {
        return this.baseEntity;
    }

    public void setBaseEntity(Entity baseObject) {
        this.baseEntity = baseObject;
    }

    public void addSubEntity(Entity subEntity) {
        this.subEntities.add(subEntity);
    }

    public void removeIdColumn() {
        for (Iterator<PrimitiveProperty> i = this.getPrimitiveProperties().iterator(); i.hasNext();) {
            if (i.next().getColumnName().equals("id")) {
                i.remove();
            }
        }
    }

    public List<Entity> getSubEntities() {
        return this.subEntities;
    }

    public String toString() {
        return this.getClassName();
    }

}
