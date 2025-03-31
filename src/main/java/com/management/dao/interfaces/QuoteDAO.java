package com.management.dao.interfaces;

import com.management.model.Quote;

import java.util.List;
import java.util.Optional;

public interface QuoteDAO {
    // Create
    int save(Quote quote);

    // Read
    Optional<Quote> findById(int quoteId);
    List<Quote> findAll();
    List<Quote> findByJobId(int jobId);
    List<Quote> findByStatus(String status);

    // Update
    boolean update(Quote quote);
    boolean updateStatus(int quoteId, String status);

    // Delete
    boolean delete(int quoteId);

    // Utility
    int count();
}