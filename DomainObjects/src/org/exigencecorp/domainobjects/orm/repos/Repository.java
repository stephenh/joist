package org.exigencecorp.domainobjects.orm.repos;

import java.util.List;

import org.exigencecorp.domainobjects.DomainObject;
import org.exigencecorp.domainobjects.queries.Insert;
import org.exigencecorp.domainobjects.queries.Select;
import org.exigencecorp.domainobjects.queries.Update;

public interface Repository {

    void open();

    void commit();

    void rollback();

    void close();

    <T extends DomainObject> void assignId(T instance);

    <T extends DomainObject> void store(T instance);

    <T extends DomainObject> void insert(Insert<T> insert);

    <T extends DomainObject> void update(Update<T> update);

    <T extends DomainObject> List<T> select(Select<T> select, Class<T> instanceType);

}
