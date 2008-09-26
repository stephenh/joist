package org.exigencecorp.domainobjects.queries;

import java.util.ArrayList;
import java.util.List;

import org.exigencecorp.domainobjects.DomainObject;
import org.exigencecorp.domainobjects.queries.columns.AliasColumn;
import org.exigencecorp.domainobjects.uow.UoW;
import org.exigencecorp.util.Copy;
import org.exigencecorp.util.Join;

public class Select<T extends DomainObject> {

    public static <T extends DomainObject> Select<T> from(Alias<T> alias) {
        return new Select<T>(alias);
    }

    private final Alias<T> from;
    private final List<JoinClause> joins = new ArrayList<JoinClause>();
    private final List<SelectItem> selectItems = new ArrayList<SelectItem>();
    private Where where = null;
    private Order[] orderBy = null;

    private Select(Alias<T> alias) {
        this.from = alias;

        for (AliasColumn<T, ?, ?> c : alias.getColumns()) {
            this.selectItems.add(new SelectItem(c));
        }

        int i = 0;
        List<String> subClassCases = new ArrayList<String>();
        for (Alias<?> sub : alias.getSubClassAliases()) {
            this.join(new JoinClause("LEFT OUTER JOIN", sub, sub.getSubClassIdColumn(), alias.getIdColumn()));
            for (AliasColumn<?, ?, ?> c : sub.getColumns()) {
                this.selectItems.add(new SelectItem(c));
            }
            subClassCases.add("WHEN " + sub.getSubClassIdColumn().getQualifiedName() + " IS NOT NULL THEN " + (i++));
        }
        if (i > 0) {
            this.selectItems.add(new SelectItem("CASE " + Join.space(subClassCases) + " ELSE -1 END AS _clazz"));
        }

        if (alias.getBaseClassAlias() != null) {
            Alias<?> base = alias.getBaseClassAlias();
            this.join(new JoinClause("INNER JOIN", base, base.getIdColumn(), alias.getSubClassIdColumn()));
            for (AliasColumn<?, ?, ?> c : base.getColumns()) {
                this.selectItems.add(new SelectItem(c));
            }
        }
    }

    public void join(JoinClause join) {
        this.joins.add(join);
    }

    public void select(SelectItem... selectItems) {
        this.selectItems.clear();
        this.selectItems.addAll(Copy.list(selectItems));
    }

    public void where(Where where) {
        this.where = where;
    }

    public void orderBy(Order... columns) {
        this.orderBy = columns;
    }

    public List<T> list() {
        return UoW.getCurrent().getRepository().select(this, this.from.getDomainClass());
    }

    public T unique() {
        List<T> results = this.list();
        if (results.size() == 0) {
            throw new RuntimeException("Not found");
        } else if (results.size() > 1) {
            throw new RuntimeException("Too many");
        }
        return results.get(0);
    }

    public Alias<T> getFrom() {
        return this.from;
    }

    public List<JoinClause> getJoins() {
        return this.joins;
    }

    public List<SelectItem> getSelectItems() {
        return this.selectItems;
    }

    public Where getWhere() {
        return this.where;
    }

    public Order[] getOrderBy() {
        return this.orderBy;
    }

}
