package com.example.chartographerapp.repository;

import com.example.chartographerapp.entity.Charta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartoRepository extends CrudRepository<Charta, Integer> {
    List<Charta> findChartaById(Integer id);

    Optional<Charta> getChartaById(Integer id);

    @Query("select max(id) from Charta")
    Optional<Integer> findCurrentChartaId();
}
