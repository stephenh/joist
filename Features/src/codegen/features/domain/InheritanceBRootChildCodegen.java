package features.domain;

import features.domain.mappers.InheritanceBRootChildAlias;
import org.exigencecorp.domainobjects.AbstractDomainObject;
import org.exigencecorp.domainobjects.Id;
import org.exigencecorp.domainobjects.Shim;
import org.exigencecorp.domainobjects.orm.AliasRegistry;
import org.exigencecorp.domainobjects.orm.ForeignKeyHolder;

public abstract class InheritanceBRootChildCodegen extends AbstractDomainObject {

    static {
        AliasRegistry.register(InheritanceBRootChild.class, new InheritanceBRootChildAlias("a"));
    }

    private Id<InheritanceBRootChild> id = null;
    private String name = null;
    private Integer version = null;
    private ForeignKeyHolder<InheritanceBRoot> inheritanceBRoot = new ForeignKeyHolder<InheritanceBRoot>(InheritanceBRoot.class);

    public Id<InheritanceBRootChild> getId() {
        return this.id;
    }

    public void setId(Id<InheritanceBRootChild> id) {
        this.recordIfChanged("id", this.id, id);
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.recordIfChanged("name", this.name, name);
        this.name = name;
    }

    public Integer getVersion() {
        return this.version;
    }

    public InheritanceBRoot getInheritanceBRoot() {
        return this.inheritanceBRoot.get();
    }

    public void setInheritanceBRoot(InheritanceBRoot inheritanceBRoot) {
        if (this.inheritanceBRoot.get() != null) {
           this.inheritanceBRoot.get().removeInheritanceBRootChildWithoutPercolation((InheritanceBRootChild) this);
        }
        this.setInheritanceBRootWithoutPercolation(inheritanceBRoot);
        if (this.inheritanceBRoot.get() != null) {
           this.inheritanceBRoot.get().addInheritanceBRootChildWithoutPercolation((InheritanceBRootChild) this);
        }
    }

    public void setInheritanceBRootWithoutPercolation(InheritanceBRoot inheritanceBRoot) {
        this.recordIfChanged("inheritanceBRoot", this.inheritanceBRoot, inheritanceBRoot);
        this.inheritanceBRoot.set(inheritanceBRoot);
    }

    public static class Shims {
        public static final Shim<InheritanceBRootChild, Id<InheritanceBRootChild>> id = new Shim<InheritanceBRootChild, Id<InheritanceBRootChild>>() {
            public void set(InheritanceBRootChild instance, Id<InheritanceBRootChild> id) {
                ((InheritanceBRootChildCodegen) instance).id = id;
            }
            public Id<InheritanceBRootChild> get(InheritanceBRootChild instance) {
                return ((InheritanceBRootChildCodegen) instance).id;
            }
        };
        public static final Shim<InheritanceBRootChild, String> name = new Shim<InheritanceBRootChild, String>() {
            public void set(InheritanceBRootChild instance, String name) {
                ((InheritanceBRootChildCodegen) instance).name = name;
            }
            public String get(InheritanceBRootChild instance) {
                return ((InheritanceBRootChildCodegen) instance).name;
            }
        };
        public static final Shim<InheritanceBRootChild, Integer> version = new Shim<InheritanceBRootChild, Integer>() {
            public void set(InheritanceBRootChild instance, Integer version) {
                ((InheritanceBRootChildCodegen) instance).version = version;
            }
            public Integer get(InheritanceBRootChild instance) {
                return ((InheritanceBRootChildCodegen) instance).version;
            }
        };
        public static final Shim<InheritanceBRootChild, Integer> inheritanceBRootId = new Shim<InheritanceBRootChild, Integer>() {
            public void set(InheritanceBRootChild instance, Integer inheritanceBRootId) {
                ((InheritanceBRootChildCodegen) instance).inheritanceBRoot.setId(inheritanceBRootId);
            }
            public Integer get(InheritanceBRootChild instance) {
                return ((InheritanceBRootChildCodegen) instance).inheritanceBRoot.getId();
            }
        };
    }

}