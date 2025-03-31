package com.management.dao.implementations;

import com.management.dao.interfaces.TechnicianDAO;
import com.management.model.Technician;
import com.management.service.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TechnicianDAOImpl implements TechnicianDAO {
    private final DatabaseService databaseService;

    public TechnicianDAOImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int save(Technician technician) {
        String sql = "INSERT INTO technicians (first_name, last_name, credentials, credential_level, " +
                "email, zip_code, coverage_area, pay_type, account_info, address, city, state, zip, " +
                "legal_name, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, technician.getFirstName());
            pstmt.setString(2, technician.getLastName());
            pstmt.setString(3, technician.getCredentials());
            pstmt.setString(4, technician.getCredentialLevel());
            pstmt.setString(5, technician.getEmail());
            pstmt.setString(6, technician.getZipCode());
            pstmt.setString(7, technician.getCoverageArea());
            pstmt.setString(8, technician.getPayType());
            pstmt.setString(9, technician.getAccountInfo());
            pstmt.setString(10, technician.getAddress());
            pstmt.setString(11, technician.getCity());
            pstmt.setString(12, technician.getState());
            pstmt.setString(13, technician.getZip());
            pstmt.setString(14, technician.getLegalName());
            pstmt.setString(15, technician.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating technician failed, no rows affected.");
            }

            // Get the generated ID using SQLite's last_insert_rowid() function
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating technician failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Optional<Technician> findById(int technicianId) {
        String sql = "SELECT * FROM technicians WHERE technician_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, technicianId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTechnician(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Technician> findAll() {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT * FROM technicians ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                technicians.add(mapResultSetToTechnician(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return technicians;
    }

    @Override
    public List<Technician> findByName(String searchTerm) {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT * FROM technicians WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    technicians.add(mapResultSetToTechnician(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return technicians;
    }

    @Override
    public Optional<Technician> findByEmail(String email) {
        String sql = "SELECT * FROM technicians WHERE email = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTechnician(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Technician> findByCredentialLevel(String credentialLevel) {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT * FROM technicians WHERE credential_level = ? ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, credentialLevel);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    technicians.add(mapResultSetToTechnician(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return technicians;
    }

    @Override
    public List<Technician> findByCoverageArea(String coverageArea) {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT * FROM technicians WHERE coverage_area LIKE ? OR zip_code = ? ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + coverageArea + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, coverageArea); // Exact match for zip code

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    technicians.add(mapResultSetToTechnician(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return technicians;
    }

    @Override
    public boolean update(Technician technician) {
        String sql = "UPDATE technicians SET first_name = ?, last_name = ?, credentials = ?, " +
                "credential_level = ?, email = ?, zip_code = ?, coverage_area = ?, pay_type = ?, " +
                "account_info = ?, address = ?, city = ?, state = ?, zip = ?, legal_name = ?, " +
                "notes = ?, updated_at = CURRENT_TIMESTAMP WHERE technician_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, technician.getFirstName());
            pstmt.setString(2, technician.getLastName());
            pstmt.setString(3, technician.getCredentials());
            pstmt.setString(4, technician.getCredentialLevel());
            pstmt.setString(5, technician.getEmail());
            pstmt.setString(6, technician.getZipCode());
            pstmt.setString(7, technician.getCoverageArea());
            pstmt.setString(8, technician.getPayType());
            pstmt.setString(9, technician.getAccountInfo());
            pstmt.setString(10, technician.getAddress());
            pstmt.setString(11, technician.getCity());
            pstmt.setString(12, technician.getState());
            pstmt.setString(13, technician.getZip());
            pstmt.setString(14, technician.getLegalName());
            pstmt.setString(15, technician.getNotes());
            pstmt.setInt(16, technician.getTechnicianId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int technicianId) {
        String sql = "DELETE FROM technicians WHERE technician_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, technicianId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM technicians";

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

    private Technician mapResultSetToTechnician(ResultSet rs) throws SQLException {
        Technician technician = new Technician();

        technician.setTechnicianId(rs.getInt("technician_id"));
        technician.setFirstName(rs.getString("first_name"));
        technician.setLastName(rs.getString("last_name"));
        technician.setCredentials(rs.getString("credentials"));
        technician.setCredentialLevel(rs.getString("credential_level"));
        technician.setEmail(rs.getString("email"));
        technician.setZipCode(rs.getString("zip_code"));
        technician.setCoverageArea(rs.getString("coverage_area"));
        technician.setPayType(rs.getString("pay_type"));
        technician.setAccountInfo(rs.getString("account_info"));
        technician.setAddress(rs.getString("address"));
        technician.setCity(rs.getString("city"));
        technician.setState(rs.getString("state"));
        technician.setZip(rs.getString("zip"));
        technician.setLegalName(rs.getString("legal_name"));
        technician.setNotes(rs.getString("notes"));

        // Convert timestamps to LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            technician.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            technician.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return technician;
    }
}