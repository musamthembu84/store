package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Matches customer ids where at least one whole word in their name contains the given (already LIKE-escaped)
     * substring. The {@code c.name ILIKE} clause is logically implied by the word-level check below it, but is kept so
     * the planner can use the trigram index on {@code customer.name} to cheaply discard non-matching rows before
     * evaluating the per-word {@code unnest} check only on the remaining candidates. Returns only ids (not fetch-joined
     * Customer entities) so the caller can hydrate just the matching rows in a second query - see
     * {@link #findByIdInWithOrders(List)}.
     */
    @Query(
            value =
                    """
            SELECT c.id
            FROM customer c
            WHERE c.name ILIKE CONCAT('%', :escapedQuery, '%') ESCAPE '\\'
              AND EXISTS (
                  SELECT 1
                  FROM unnest(string_to_array(c.name, ' ')) AS word
                  WHERE word ILIKE CONCAT('%', :escapedQuery, '%') ESCAPE '\\'
              )
            """,
            nativeQuery = true)
    List<Long> findIdsByNameWordContaining(@Param("escapedQuery") String escapedQuery);

    /**
     * Fetch-joins {@code orders} so the collection is populated in this single query instead of triggering a lazy load
     * (batched or not) per customer once the mapper touches it.
     */
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.orders")
    List<Customer> findAllWithOrders();

    /** Same fetch-join as {@link #findAllWithOrders()}, scoped to a specific set of ids. */
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id IN :ids")
    List<Customer> findByIdInWithOrders(@Param("ids") List<Long> ids);
}
