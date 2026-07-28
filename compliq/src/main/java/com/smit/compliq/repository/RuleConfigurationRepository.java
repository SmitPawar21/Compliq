package com.smit.compliq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.Organization;
import com.smit.compliq.entity.RuleConfiguration;

@Repository
public interface RuleConfigurationRepository extends JpaRepository<RuleConfiguration, Long> {
    List<RuleConfiguration> findByOrganization(Organization organization);
}
