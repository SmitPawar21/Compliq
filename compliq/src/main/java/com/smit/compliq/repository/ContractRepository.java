package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Contract;

public interface ContractRepository extends JpaRepository<Contract, String> {

}
