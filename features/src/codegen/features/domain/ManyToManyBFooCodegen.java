package features.domain;

import features.domain.queries.ManyToManyBFooQueries;
import java.util.ArrayList;
import java.util.List;
import joist.domain.AbstractChanged;
import joist.domain.AbstractDomainObject;
import joist.domain.Changed;
import joist.domain.Shim;
import joist.domain.orm.ForeignKeyListHolder;
import joist.domain.uow.UoW;
import joist.domain.util.ListProxy;
import joist.domain.validation.rules.MaxLength;
import joist.domain.validation.rules.NotEmpty;
import joist.domain.validation.rules.NotNull;
import joist.util.Copy;
import joist.util.ListDiff;

@SuppressWarnings("all")
abstract class ManyToManyBFooCodegen extends AbstractDomainObject {

  public static final ManyToManyBFooQueries queries;
  private Long id = null;
  private String name = null;
  private Long version = null;
  private final ForeignKeyListHolder<ManyToManyBFoo, ManyToManyBFooToBar> ownerManyToManyBFooToBars = new ForeignKeyListHolder<ManyToManyBFoo, ManyToManyBFooToBar>((ManyToManyBFoo) this, Aliases.manyToManyBFooToBar(), Aliases.manyToManyBFooToBar().ownerManyToManyBFoo, new OwnerManyToManyBFooToBarsListDelegate());
  protected Changed changed;

  static {
    Aliases.manyToManyBFoo();
    queries = new ManyToManyBFooQueries();
  }

  protected ManyToManyBFooCodegen() {
    this.addExtraRules();
  }

  private void addExtraRules() {
    this.addRule(new NotNull<ManyToManyBFoo>(Shims.name));
    this.addRule(new MaxLength<ManyToManyBFoo>(Shims.name, 100));
    this.addRule(new NotEmpty<ManyToManyBFoo>(Shims.name));
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.getChanged().record("id", this.id, id);
    this.id = id;
    if (UoW.isOpen()) {
      UoW.getIdentityMap().store(this);
    }
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.getChanged().record("name", this.name, name);
    this.name = name;
  }

  protected void defaultName(String name) {
    this.name = name;
  }

  public Long getVersion() {
    return this.version;
  }

  private List<ManyToManyBFooToBar> getOwnerManyToManyBFooToBars() {
    return this.ownerManyToManyBFooToBars.get();
  }

  private void setOwnerManyToManyBFooToBars(List<ManyToManyBFooToBar> ownerManyToManyBFooToBars) {
    ListDiff<ManyToManyBFooToBar> diff = ListDiff.of(this.getOwnerManyToManyBFooToBars(), ownerManyToManyBFooToBars);
    for (ManyToManyBFooToBar o : diff.removed) {
      this.removeOwnerManyToManyBFooToBar(o);
    }
    for (ManyToManyBFooToBar o : diff.added) {
      this.addOwnerManyToManyBFooToBar(o);
    }
  }

  private void addOwnerManyToManyBFooToBar(ManyToManyBFooToBar o) {
    if (o.getOwnerManyToManyBFoo() == this) {
      return;
    }
    o.setOwnerManyToManyBFooWithoutPercolation((ManyToManyBFoo) this);
    this.addOwnerManyToManyBFooToBarWithoutPercolation(o);
  }

  private void removeOwnerManyToManyBFooToBar(ManyToManyBFooToBar o) {
    if (o.getOwnerManyToManyBFoo() != this) {
      return;
    }
    o.setOwnerManyToManyBFooWithoutPercolation(null);
    this.removeOwnerManyToManyBFooToBarWithoutPercolation(o);
    if (UoW.isOpen()) {
      ManyToManyBFooToBar.queries.delete(o);
    }
  }

  protected void addOwnerManyToManyBFooToBarWithoutPercolation(ManyToManyBFooToBar o) {
    this.getChanged().record("ownerManyToManyBFooToBars");
    this.ownerManyToManyBFooToBars.add(o);
  }

  protected void removeOwnerManyToManyBFooToBarWithoutPercolation(ManyToManyBFooToBar o) {
    this.getChanged().record("ownerManyToManyBFooToBars");
    this.ownerManyToManyBFooToBars.remove(o);
  }

  public List<ManyToManyBBar> getOwneds() {
    List<ManyToManyBBar> l = new ArrayList<ManyToManyBBar>();
    for (ManyToManyBFooToBar o : this.getOwnerManyToManyBFooToBars()) {
      l.add(o.getOwned());
    }
    return l;
  }

  public void setOwneds(List<ManyToManyBBar> owneds) {
    ListDiff<ManyToManyBBar> diff = ListDiff.of(this.getOwneds(), owneds);
    for (ManyToManyBBar o : diff.removed) {
      this.removeOwned(o);
    }
    for (ManyToManyBBar o : diff.added) {
      this.addOwned(o);
    }
  }

  public void addOwned(ManyToManyBBar o) {
    ManyToManyBFooToBar a = new ManyToManyBFooToBar();
    a.setOwnerManyToManyBFoo((ManyToManyBFoo) this);
    a.setOwned(o);
  }

  public void removeOwned(ManyToManyBBar o) {
    for (ManyToManyBFooToBar a : Copy.list(this.getOwnerManyToManyBFooToBars())) {
      if (a.getOwned().equals(o)) {
        a.setOwned(null);
        a.setOwnerManyToManyBFoo(null);
        if (UoW.isOpen()) {
          UoW.delete(a);
        }
      }
    }
  }

  public ManyToManyBFooChanged getChanged() {
    if (this.changed == null) {
      this.changed = new ManyToManyBFooChanged((ManyToManyBFoo) this);
    }
    return (ManyToManyBFooChanged) this.changed;
  }

  @Override
  public void clearAssociations() {
    super.clearAssociations();
    for (ManyToManyBFooToBar o : Copy.list(this.getOwnerManyToManyBFooToBars())) {
      removeOwnerManyToManyBFooToBar(o);
    }
  }

  static class Shims {
    protected static final Shim<ManyToManyBFoo, Long> id = new Shim<ManyToManyBFoo, Long>() {
      public void set(ManyToManyBFoo instance, Long id) {
        ((ManyToManyBFooCodegen) instance).id = id;
      }
      public Long get(ManyToManyBFoo instance) {
        return ((ManyToManyBFooCodegen) instance).id;
      }
      public String getName() {
        return "id";
      }
    };
    protected static final Shim<ManyToManyBFoo, String> name = new Shim<ManyToManyBFoo, String>() {
      public void set(ManyToManyBFoo instance, String name) {
        ((ManyToManyBFooCodegen) instance).name = name;
      }
      public String get(ManyToManyBFoo instance) {
        return ((ManyToManyBFooCodegen) instance).name;
      }
      public String getName() {
        return "name";
      }
    };
    protected static final Shim<ManyToManyBFoo, Long> version = new Shim<ManyToManyBFoo, Long>() {
      public void set(ManyToManyBFoo instance, Long version) {
        ((ManyToManyBFooCodegen) instance).version = version;
      }
      public Long get(ManyToManyBFoo instance) {
        return ((ManyToManyBFooCodegen) instance).version;
      }
      public String getName() {
        return "version";
      }
    };
  }

  private class OwnerManyToManyBFooToBarsListDelegate implements ListProxy.Delegate<ManyToManyBFooToBar> {
    public void doAdd(ManyToManyBFooToBar e) {
      addOwnerManyToManyBFooToBar(e);
    }
    public void doRemove(ManyToManyBFooToBar e) {
      removeOwnerManyToManyBFooToBar(e);
    }
  }

  public static class ManyToManyBFooChanged extends AbstractChanged {
    public ManyToManyBFooChanged(ManyToManyBFoo instance) {
      super(instance);
    }
    public boolean hasId() {
      return this.contains("id");
    }
    public Long getOriginalId() {
      return (Long) this.getOriginal("id");
    }
    public boolean hasName() {
      return this.contains("name");
    }
    public String getOriginalName() {
      return (java.lang.String) this.getOriginal("name");
    }
    public boolean hasVersion() {
      return this.contains("version");
    }
    public Long getOriginalVersion() {
      return (Long) this.getOriginal("version");
    }
    public boolean hasOwnerManyToManyBFooToBars() {
      return this.contains("ownerManyToManyBFooToBars");
    }
  }

}
