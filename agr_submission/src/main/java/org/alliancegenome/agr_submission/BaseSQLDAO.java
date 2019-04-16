package org.alliancegenome.agr_submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Singleton
public class BaseSQLDAO<E extends BaseEntity> extends BaseDAO<E> {

    @PersistenceContext(name="primary")
    protected EntityManager entityManager;

    protected BaseSQLDAO(Class<E> myClass) {
        super(myClass);
    }
    
    public E persist(E entity) {
        log.info("SqlDAO: persist: " + entityManager);
        entityManager.persist(entity);
        return entity;
    }

    public E find(Long id) {
        log.info("SqlDAO: find: " + id + " " + entityManager);
        E entity = entityManager.find(myClass, id);
        log.debug("Entity Found: " + entity);
        return entity;
    }

    public List<E> findAll() {
        //log.info("SqlDAO: findAll: " + entityManager);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(myClass);
        Root<E> rootEntry = cq.from(myClass);
        CriteriaQuery<E> all = cq.select(rootEntry);
        TypedQuery<E> allQuery = entityManager.createQuery(all);
        return allQuery.getResultList();
    }

    public E merge(E entity) {
        log.info("SqlDAO: merge: " + entityManager);
        entityManager.merge(entity);
        return entity;
    }
    
    public E remove(Long id) {
        log.info("SqliteDAO: remove: " + id + " " + entityManager);
        E entity = find(id);
        entityManager.remove(entity);
        return entity;
    }
    
    public E findByField(String field, String value) {
        log.info("SqlDAO: findByField: " + field + " " + value + " " + entityManager);
        HashMap<String, Object> params = new HashMap<>();
        params.put(field, value);
        List<E> list = search(params);
        if(list.size() > 0) {
            //sv = schemaDAO.getSchemaVersion(schema_id);
            return list.get(0);
        } else {
            return null;
        }
    }
    
    public List<E> search(Map<String, Object> params) {
        return search(params, null);
    }

    public List<E> search(Map<String, Object> params, String orderByField) {
        log.info("Search By Params: " + params + " Order by: " + orderByField);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = builder.createQuery(myClass);
        Root<E> root = query.from(myClass);
        //System.out.println("Root: " + root);
        List<Predicate> restrictions = new ArrayList<Predicate>();
        //System.out.println(params);
        for(String key: params.keySet()) {
            Path<Object> column = null;
            //System.out.println("Key: " + key);
            if(key.contains(".")) {
                String[] objects = key.split("\\.");
                for(String s: objects) {
                    //System.out.println("Looking up: " + s);
                    if(column != null) {
                        column = column.get(s);
                    } else {
                        column = root.get(s);
                    }
                    //System.out.println(column.getAlias());
                }
            } else {
                column = root.get(key);
            }

            //System.out.println(column.getAlias());

            Object value = params.get(key);
            if (value instanceof Integer) {
                Integer desiredValue = (Integer) value;
                restrictions.add(builder.equal(column, desiredValue));
            } else {
                String desiredValue = (String) value;
                restrictions.add(builder.equal(column, desiredValue));
            }
        }
        if(orderByField != null) {
            query.orderBy(builder.asc(root.get(orderByField)));
        }

        query.where(builder.and(restrictions.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

}
