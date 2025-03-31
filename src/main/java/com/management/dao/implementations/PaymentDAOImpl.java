package com.management.dao.implementations;

import com.management.dao.interfaces.PaymentDAO;
import com.management.model.Payment;
import com.management.service.DatabaseService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAOImpl implements PaymentDAO {
    private final DatabaseService databaseService;

    public PaymentDAOImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int save(Payment payment) {
        String sql = "INSERT INTO payments (job_id, amount, status, payment_date, payment_method, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getJobId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setString(3, payment.getStatus());

            if (payment.getPaymentDate() != null) {
                pstmt.setString(4, payment.getPaymentDate().toString());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.setString(5, payment.getPaymentMethod());
            pstmt.setString(6, payment.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }

            // Get the generated ID using SQLite's last_insert_rowid() function
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Optional<Payment> findById(int paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paymentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC, created_at DESC";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public List<Payment> findByJobId(int jobId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE job_id = ? ORDER BY payment_date DESC, created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public List<Payment> findByStatus(String status) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE status = ? ORDER BY payment_date DESC, created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE payment_date BETWEEN ? AND ? " +
                "ORDER BY payment_date DESC, created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public boolean update(Payment payment) {
        String sql = "UPDATE payments SET job_id = ?, amount = ?, status = ?, payment_date = ?, " +
                "payment_method = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getJobId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setString(3, payment.getStatus());

            if (payment.getPaymentDate() != null) {
                pstmt.setString(4, payment.getPaymentDate().toString());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.setString(5, payment.getPaymentMethod());
            pstmt.setString(6, payment.getNotes());
            pstmt.setInt(7, payment.getPaymentId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, paymentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paymentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM payments";

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

    @Override
    public double getTotalPaymentsForJob(int jobId) {
        String sql = "SELECT SUM(amount) AS total_amount FROM payments WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();

        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setJobId(rs.getInt("job_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setStatus(rs.getString("status"));

        String paymentDateStr = rs.getString("payment_date");
        if (paymentDateStr != null && !paymentDateStr.isEmpty()) {
            payment.setPaymentDate(LocalDate.parse(paymentDateStr));
        }

        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setNotes(rs.getString("notes"));

        // Convert timestamps to LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            payment.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            payment.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return payment;
    }
}