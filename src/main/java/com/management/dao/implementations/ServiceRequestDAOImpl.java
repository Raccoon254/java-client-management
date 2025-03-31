package com.management.dao.implementations;

import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.DatabaseService;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceRequestDAOImpl implements ServiceRequestDAO {
    private final DatabaseService databaseService;

    public ServiceRequestDAOImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int save(ServiceRequest serviceRequest) {
        String sql = "INSERT INTO service_requests (description, service_cost, customer_id, service_date, " +
                "ref_no, start_time, end_time, building_name, service_address, service_city, service_state, " +
                "service_zip, poc_name, poc_phone, service_participant_name, service_notes, added_cost, " +
                "status, postref_number, parking_fees, start_time_ics, end_time_ics, technician_status, " +
                "technician_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, serviceRequest.getDescription());
            pstmt.setDouble(2, serviceRequest.getServiceCost());
            pstmt.setInt(3, serviceRequest.getCustomerId());
            pstmt.setString(4, serviceRequest.getServiceDate().toString());
            pstmt.setString(5, serviceRequest.getRefNo());

            if (serviceRequest.getStartTime() != null) {
                pstmt.setString(6, serviceRequest.getStartTime().toString());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (serviceRequest.getEndTime() != null) {
                pstmt.setString(7, serviceRequest.getEndTime().toString());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
            }

            pstmt.setString(8, serviceRequest.getBuildingName());
            pstmt.setString(9, serviceRequest.getServiceAddress());
            pstmt.setString(10, serviceRequest.getServiceCity());
            pstmt.setString(11, serviceRequest.getServiceState());
            pstmt.setString(12, serviceRequest.getServiceZip());
            pstmt.setString(13, serviceRequest.getPocName());
            pstmt.setString(14, serviceRequest.getPocPhone());
            pstmt.setString(15, serviceRequest.getServiceParticipantName());
            pstmt.setString(16, serviceRequest.getServiceNotes());
            pstmt.setDouble(17, serviceRequest.getAddedCost());
            pstmt.setString(18, serviceRequest.getStatus());
            pstmt.setString(19, serviceRequest.getPostrefNumber());
            pstmt.setDouble(20, serviceRequest.getParkingFees());
            pstmt.setString(21, serviceRequest.getStartTimeIcs());
            pstmt.setString(22, serviceRequest.getEndTimeIcs());
            pstmt.setString(23, serviceRequest.getTechnicianStatus());
            pstmt.setString(24, serviceRequest.getTechnicianNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating service request failed, no rows affected.");
            }

            // Get the generated ID using SQLite's last_insert_rowid() function
            int jobId;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    jobId = rs.getInt(1);
                } else {
                    throw new SQLException("Creating service request failed, no ID obtained.");
                }
            }

            // Assign technicians if available
            if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
                for (Technician technician : serviceRequest.getTechnicians()) {
                    assignTechnician(jobId, technician.getTechnicianId());
                }
            }

            return jobId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean assignTechnician(int jobId, int technicianId) {
        String sql = "INSERT INTO service_technicians (job_id, technician_id) VALUES (?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);
            pstmt.setInt(2, technicianId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Check if this is a unique constraint violation (error code 19 in SQLite is SQLITE_CONSTRAINT)
            if (e.getMessage().contains("UNIQUE constraint failed") || e.getErrorCode() == 19) {
                return true; // Assignment already exists
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<ServiceRequest> findById(int jobId) {
        String sql = "SELECT * FROM service_requests WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                    // Load technicians for this service request
                    List<Technician> technicians = findTechniciansForJob(jobId);
                    serviceRequest.setTechnicians(technicians);

                    return Optional.of(serviceRequest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<ServiceRequest> findAll() {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests ORDER BY service_date DESC";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                // Load technicians for this service request
                List<Technician> technicians = findTechniciansForJob(serviceRequest.getJobId());
                serviceRequest.setTechnicians(technicians);

                serviceRequests.add(serviceRequest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceRequests;
    }

    @Override
    public List<ServiceRequest> findByCustomerId(int customerId) {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests WHERE customer_id = ? ORDER BY service_date DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                    // Load technicians for this service request
                    List<Technician> technicians = findTechniciansForJob(serviceRequest.getJobId());
                    serviceRequest.setTechnicians(technicians);

                    serviceRequests.add(serviceRequest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceRequests;
    }

    @Override
    public List<ServiceRequest> findByTechnicianId(int technicianId) {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String sql = "SELECT sr.* FROM service_requests sr " +
                "JOIN service_technicians st ON sr.job_id = st.job_id " +
                "WHERE st.technician_id = ? " +
                "ORDER BY sr.service_date DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, technicianId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                    // Load technicians for this service request
                    List<Technician> technicians = findTechniciansForJob(serviceRequest.getJobId());
                    serviceRequest.setTechnicians(technicians);

                    serviceRequests.add(serviceRequest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceRequests;
    }

    @Override
    public List<ServiceRequest> findByStatus(String status) {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests WHERE status = ? ORDER BY service_date DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                    // Load technicians for this service request
                    List<Technician> technicians = findTechniciansForJob(serviceRequest.getJobId());
                    serviceRequest.setTechnicians(technicians);

                    serviceRequests.add(serviceRequest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceRequests;
    }

    @Override
    public List<ServiceRequest> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests WHERE service_date BETWEEN ? AND ? ORDER BY service_date";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest serviceRequest = mapResultSetToServiceRequest(rs);

                    // Load technicians for this service request
                    List<Technician> technicians = findTechniciansForJob(serviceRequest.getJobId());
                    serviceRequest.setTechnicians(technicians);

                    serviceRequests.add(serviceRequest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serviceRequests;
    }

    @Override
    public List<Technician> findTechniciansForJob(int jobId) {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT t.* FROM technicians t " +
                "JOIN service_technicians st ON t.technician_id = st.technician_id " +
                "WHERE st.job_id = ? " +
                "ORDER BY t.last_name, t.first_name";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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

                    technicians.add(technician);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return technicians;
    }

    @Override
    public boolean update(ServiceRequest serviceRequest) {
        String sql = "UPDATE service_requests SET description = ?, service_cost = ?, customer_id = ?, " +
                "service_date = ?, ref_no = ?, start_time = ?, end_time = ?, building_name = ?, " +
                "service_address = ?, service_city = ?, service_state = ?, service_zip = ?, " +
                "poc_name = ?, poc_phone = ?, service_participant_name = ?, service_notes = ?, " +
                "added_cost = ?, status = ?, postref_number = ?, parking_fees = ?, " +
                "start_time_ics = ?, end_time_ics = ?, technician_status = ?, technician_notes = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, serviceRequest.getDescription());
            pstmt.setDouble(2, serviceRequest.getServiceCost());
            pstmt.setInt(3, serviceRequest.getCustomerId());
            pstmt.setString(4, serviceRequest.getServiceDate().toString());
            pstmt.setString(5, serviceRequest.getRefNo());

            if (serviceRequest.getStartTime() != null) {
                pstmt.setString(6, serviceRequest.getStartTime().toString());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (serviceRequest.getEndTime() != null) {
                pstmt.setString(7, serviceRequest.getEndTime().toString());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
            }

            pstmt.setString(8, serviceRequest.getBuildingName());
            pstmt.setString(9, serviceRequest.getServiceAddress());
            pstmt.setString(10, serviceRequest.getServiceCity());
            pstmt.setString(11, serviceRequest.getServiceState());
            pstmt.setString(12, serviceRequest.getServiceZip());
            pstmt.setString(13, serviceRequest.getPocName());
            pstmt.setString(14, serviceRequest.getPocPhone());
            pstmt.setString(15, serviceRequest.getServiceParticipantName());
            pstmt.setString(16, serviceRequest.getServiceNotes());
            pstmt.setDouble(17, serviceRequest.getAddedCost());
            pstmt.setString(18, serviceRequest.getStatus());
            pstmt.setString(19, serviceRequest.getPostrefNumber());
            pstmt.setDouble(20, serviceRequest.getParkingFees());
            pstmt.setString(21, serviceRequest.getStartTimeIcs());
            pstmt.setString(22, serviceRequest.getEndTimeIcs());
            pstmt.setString(23, serviceRequest.getTechnicianStatus());
            pstmt.setString(24, serviceRequest.getTechnicianNotes());
            pstmt.setInt(25, serviceRequest.getJobId());

            int affectedRows = pstmt.executeUpdate();

            // If technicians are provided, update the assignments
            if (serviceRequest.getTechnicians() != null) {
                // Remove all current technician assignments
                String deleteSql = "DELETE FROM service_technicians WHERE job_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, serviceRequest.getJobId());
                    deleteStmt.executeUpdate();
                }

                // Add new technician assignments
                for (Technician technician : serviceRequest.getTechnicians()) {
                    assignTechnician(serviceRequest.getJobId(), technician.getTechnicianId());
                }
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatus(int jobId, String status) {
        String sql = "UPDATE service_requests SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, jobId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int jobId) {
        String sql = "DELETE FROM service_requests WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection()) {
            // First delete related records in service_technicians
            String deleteRelatedSql = "DELETE FROM service_technicians WHERE job_id = ?";
            try (PreparedStatement deleteRelatedStmt = conn.prepareStatement(deleteRelatedSql)) {
                deleteRelatedStmt.setInt(1, jobId);
                deleteRelatedStmt.executeUpdate();
            }

            // Then delete the service request
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, jobId);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeTechnician(int jobId, int technicianId) {
        String sql = "DELETE FROM service_technicians WHERE job_id = ? AND technician_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);
            pstmt.setInt(2, technicianId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM service_requests";

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
    public double calculateTotalCost(int jobId) {
        String sql = "SELECT service_cost + added_cost + parking_fees AS total_cost FROM service_requests WHERE job_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_cost");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    @Override
    public List<Technician> getTechniciansForServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        return findTechniciansForJob(jobId);
    }

    private ServiceRequest mapResultSetToServiceRequest(ResultSet rs) throws SQLException {
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setJobId(rs.getInt("job_id"));
        serviceRequest.setDescription(rs.getString("description"));
        serviceRequest.setServiceCost(rs.getDouble("service_cost"));
        serviceRequest.setCustomerId(rs.getInt("customer_id"));

        String serviceDateStr = rs.getString("service_date");
        if (serviceDateStr != null && !serviceDateStr.isEmpty()) {
            serviceRequest.setServiceDate(LocalDate.parse(serviceDateStr));
        }

        serviceRequest.setRefNo(rs.getString("ref_no"));

        String startTimeStr = rs.getString("start_time");
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            serviceRequest.setStartTime(LocalTime.parse(startTimeStr));
        }

        String endTimeStr = rs.getString("end_time");
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            serviceRequest.setEndTime(LocalTime.parse(endTimeStr));
        }

        serviceRequest.setBuildingName(rs.getString("building_name"));
        serviceRequest.setServiceAddress(rs.getString("service_address"));
        serviceRequest.setServiceCity(rs.getString("service_city"));
        serviceRequest.setServiceState(rs.getString("service_state"));
        serviceRequest.setServiceZip(rs.getString("service_zip"));
        serviceRequest.setPocName(rs.getString("poc_name"));
        serviceRequest.setPocPhone(rs.getString("poc_phone"));
        serviceRequest.setServiceParticipantName(rs.getString("service_participant_name"));
        serviceRequest.setServiceNotes(rs.getString("service_notes"));
        serviceRequest.setAddedCost(rs.getDouble("added_cost"));
        serviceRequest.setStatus(rs.getString("status"));
        serviceRequest.setPostrefNumber(rs.getString("postref_number"));
        serviceRequest.setParkingFees(rs.getDouble("parking_fees"));
        serviceRequest.setStartTimeIcs(rs.getString("start_time_ics"));
        serviceRequest.setEndTimeIcs(rs.getString("end_time_ics"));
        serviceRequest.setTechnicianStatus(rs.getString("technician_status"));
        serviceRequest.setTechnicianNotes(rs.getString("technician_notes"));

        // Convert timestamps to LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            serviceRequest.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            serviceRequest.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return serviceRequest;
    }
}