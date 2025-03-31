package com.management.dao.implementations;

import com.management.dao.interfaces.QuoteDAO;
import com.management.model.Quote;
import com.management.service.DatabaseService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuoteDAOImpl implements QuoteDAO {
    private final DatabaseService databaseService;

    public QuoteDAOImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int save(Quote quote) {
        String sql = "INSERT INTO quotes (job_id, start_date, end_date, amount, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quote.getJobId());

            if (quote.getStartDate() != null) {
                pstmt.setString(2, quote.getStartDate().toString());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            if (quote.getEndDate() != null) {
                pstmt.setString(3, quote.getEndDate().toString());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setDouble(4, quote.getAmount());
            pstmt.setString(5, quote.getStatus());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating quote failed, no rows affected.");
            }

            // Get the generated ID using SQLite's last_insert_rowid() function
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating quote failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Optional<Quote> findById(int quoteId) {
        String sql = "SELECT * FROM quotes WHERE quote_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quoteId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToQuote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Quote> findAll() {
        List<Quote> quotes = new ArrayList<>();
        String sql = "SELECT * FROM quotes ORDER BY created_at DESC";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                quotes.add(mapResultSetToQuote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quotes;
    }

    @Override
    public List<Quote> findByJobId(int jobId) {
        List<Quote> quotes = new ArrayList<>();
        String sql = "SELECT * FROM quotes WHERE job_id = ? ORDER BY created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    quotes.add(mapResultSetToQuote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quotes;
    }

    @Override
    public List<Quote> findByStatus(String status) {
        List<Quote> quotes = new ArrayList<>();
        String sql = "SELECT * FROM quotes WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    quotes.add(mapResultSetToQuote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quotes;
    }

    @Override
    public boolean update(Quote quote) {
        String sql = "UPDATE quotes SET job_id = ?, start_date = ?, end_date = ?, amount = ?, " +
                "status = ?, updated_at = CURRENT_TIMESTAMP WHERE quote_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quote.getJobId());

            if (quote.getStartDate() != null) {
                pstmt.setString(2, quote.getStartDate().toString());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            if (quote.getEndDate() != null) {
                pstmt.setString(3, quote.getEndDate().toString());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setDouble(4, quote.getAmount());
            pstmt.setString(5, quote.getStatus());
            pstmt.setInt(6, quote.getQuoteId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatus(int quoteId, String status) {
        String sql = "UPDATE quotes SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE quote_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, quoteId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int quoteId) {
        String sql = "DELETE FROM quotes WHERE quote_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quoteId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM quotes";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Quote mapResultSetToQuote(ResultSet rs) throws SQLException {
        Quote quote = new Quote();

        quote.setQuoteId(rs.getInt("quote_id"));
        quote.setJobId(rs.getInt("job_id"));

        String startDateStr = rs.getString("start_date");
        if (startDateStr != null && !startDateStr.isEmpty()) {
            quote.setStartDate(LocalDate.parse(startDateStr));
        }

        String endDateStr = rs.getString("end_date");
        if (endDateStr != null && !endDateStr.isEmpty()) {
            quote.setEndDate(LocalDate.parse(endDateStr));
        }

        quote.setAmount(rs.getDouble("amount"));
        quote.setStatus(rs.getString("status"));

        // Convert timestamps to LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            quote.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            quote.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return quote;
    }
}