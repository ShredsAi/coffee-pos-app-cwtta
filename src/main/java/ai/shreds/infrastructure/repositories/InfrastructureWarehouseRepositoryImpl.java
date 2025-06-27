package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.ports.DomainOutputPortWarehouseRepository;
import ai.shreds.infrastructure.repositories.entities.InfrastructureWarehouseJpaEntity;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the warehouse repository port.
 * Bridges the domain layer with the persistence layer (Spring Data JPA).
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureWarehouseRepositoryImpl implements DomainOutputPortWarehouseRepository {

    private final InfrastructureWarehouseSpringDataRepository springDataRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainWarehouseEntity save(DomainWarehouseEntity warehouse) {
        try {
            log.debug("Saving warehouse with ID: {}", warehouse.getId());
            InfrastructureWarehouseJpaEntity jpaEntity = entityMapper.toJpaWarehouse(warehouse);
            InfrastructureWarehouseJpaEntity saved = springDataRepository.save(jpaEntity);
            DomainWarehouseEntity result = entityMapper.toDomainWarehouse(saved);
            log.debug("Successfully saved warehouse with ID: {}", result.getId());
            return result;
        } catch (DataAccessException ex) {
            log.error("Failed to save warehouse with ID: {}", warehouse.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to save warehouse", 
                "DomainWarehouseEntity", 
                "save", 
                ex
            );
        }
    }

    @Override
    public DomainWarehouseEntity findById(UUID id) {
        try {
            log.debug("Finding warehouse by ID: {}", id);
            return springDataRepository.findById(id)
                    .map(entityMapper::toDomainWarehouse)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouse by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouse by ID", 
                "DomainWarehouseEntity", 
                "findById", 
                ex
            );
        }
    }

    @Override
    public boolean existsByCode(String code) {
        try {
            log.debug("Checking if warehouse exists by code: {}", code);
            return springDataRepository.existsByCode(code);
        } catch (DataAccessException ex) {
            log.error("Failed to check warehouse existence by code: {}", code, ex);
            throw new InfrastructurePersistenceException(
                "Failed to check warehouse existence by code", 
                "DomainWarehouseEntity", 
                "existsByCode", 
                ex
            );
        }
    }

    @Override
    public DomainWarehouseEntity findByCode(String code) {
        try {
            log.debug("Finding warehouse by code: {}", code);
            return springDataRepository.findByCode(code)
                    .map(entityMapper::toDomainWarehouse)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouse by code: {}", code, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouse by code", 
                "DomainWarehouseEntity", 
                "findByCode", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findAllActive() {
        try {
            log.debug("Finding all active warehouses");
            return springDataRepository.findByIsActiveTrue()
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find all active warehouses", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find all active warehouses", 
                "DomainWarehouseEntity", 
                "findAllActive", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findAll() {
        try {
            log.debug("Finding all warehouses");
            return springDataRepository.findAll()
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find all warehouses", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find all warehouses", 
                "DomainWarehouseEntity", 
                "findAll", 
                ex
            );
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        try {
            log.debug("Deleting warehouse by ID: {}", id);
            if (springDataRepository.existsById(id)) {
                springDataRepository.deleteById(id);
                log.debug("Successfully deleted warehouse with ID: {}", id);
                return true;
            }
            log.debug("Warehouse with ID {} not found for deletion", id);
            return false;
        } catch (DataAccessException ex) {
            log.error("Failed to delete warehouse by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to delete warehouse by ID", 
                "DomainWarehouseEntity", 
                "deleteById", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findByStatus(boolean isActive) {
        try {
            log.debug("Finding warehouses by status: {}", isActive);
            return springDataRepository.findByIsActive(isActive)
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouses by status: {}", isActive, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouses by status", 
                "DomainWarehouseEntity", 
                "findByStatus", 
                ex
            );
        }
    }

    @Override
    public long count() {
        try {
            log.debug("Counting total warehouses");
            return springDataRepository.count();
        } catch (DataAccessException ex) {
            log.error("Failed to count warehouses", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count warehouses", 
                "DomainWarehouseEntity", 
                "count", 
                ex
            );
        }
    }

    @Override
    public long countByStatus(boolean isActive) {
        try {
            log.debug("Counting warehouses by status: {}", isActive);
            return springDataRepository.countByIsActive(isActive);
        } catch (DataAccessException ex) {
            log.error("Failed to count warehouses by status: {}", isActive, ex);
            throw new InfrastructurePersistenceException(
                "Failed to count warehouses by status", 
                "DomainWarehouseEntity", 
                "countByStatus", 
                ex
            );
        }
    }

    @Override
    public boolean existsById(UUID id) {
        try {
            log.debug("Checking if warehouse exists by ID: {}", id);
            return springDataRepository.existsById(id);
        } catch (DataAccessException ex) {
            log.error("Failed to check warehouse existence by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to check warehouse existence by ID", 
                "DomainWarehouseEntity", 
                "existsById", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findByNameContainingIgnoreCase(String namePart) {
        try {
            log.debug("Finding warehouses by name containing: {}", namePart);
            return springDataRepository.findByNameContainingIgnoreCase(namePart)
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouses by name containing: {}", namePart, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouses by name", 
                "DomainWarehouseEntity", 
                "findByNameContainingIgnoreCase", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findByCountry(String country) {
        try {
            log.debug("Finding warehouses by country: {}", country);
            return springDataRepository.findByCountry(country)
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouses by country: {}", country, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouses by country", 
                "DomainWarehouseEntity", 
                "findByCountry", 
                ex
            );
        }
    }

    @Override
    public List<DomainWarehouseEntity> findByCity(String city) {
        try {
            log.debug("Finding warehouses by city: {}", city);
            return springDataRepository.findByCity(city)
                    .stream()
                    .map(entityMapper::toDomainWarehouse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find warehouses by city: {}", city, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find warehouses by city", 
                "DomainWarehouseEntity", 
                "findByCity", 
                ex
            );
        }
    }
}
