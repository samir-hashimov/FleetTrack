package com.fleettrack.specification;

import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.util.VehicleStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> hasStatus(VehicleStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Vehicle> hasMinYear(Integer minYear) {
        return (root, query, cb) -> minYear == null ? null : cb.greaterThanOrEqualTo(root.get("year"), minYear);
    }

    public static Specification<Vehicle> hasMaxYear(Integer maxYear) {
        return (root, query, cb) -> maxYear == null ? null : cb.lessThanOrEqualTo(root.get("year"), maxYear);
    }

    public static Specification<Vehicle> hasAssignedDriver(Long driverId) {
        return (root, query, cb) -> {
            if (driverId == null) {
                return null;
            }
            Join<Object, Object> driverJoin = root.join("driver", JoinType.LEFT);
            return cb.equal(driverJoin.get("id"), driverId);
        };
    }

    public static Specification<Vehicle> hasAssignedDriver(Boolean assigned) {
        return (root, query, cb) -> {
            if (assigned == null) {
                return null;
            }
            if (assigned) {
                return cb.isNotNull(root.get("driver"));
            }
            return cb.isNull(root.get("driver"));
        };
    }

    public static Specification<Vehicle> fetchDriver() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("driver", JoinType.LEFT);
                query.distinct(true);
            }
            return null;
        };
    }

    public static Specification<Vehicle> licensePlateContains(String licensePlate) {
        return (root, query, cb) -> {
            if (licensePlate == null || licensePlate.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("licensePlate")), "%" + licensePlate.toLowerCase() + "%");
        };
    }
}
