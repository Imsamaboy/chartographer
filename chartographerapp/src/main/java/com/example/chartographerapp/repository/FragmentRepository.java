package com.example.chartographerapp.repository;

import com.example.chartographerapp.entity.Charta;
import com.example.chartographerapp.entity.Fragment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FragmentRepository extends CrudRepository<Fragment, Integer> {
    List<Fragment> findFragmentByCharta(Charta charta);

    @Query("select max(id) from Fragment")
    Optional<Integer> findCurrentFragmentId();
}
