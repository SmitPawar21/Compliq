package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	java.util.Optional<Organization> findByName(String name);
}
