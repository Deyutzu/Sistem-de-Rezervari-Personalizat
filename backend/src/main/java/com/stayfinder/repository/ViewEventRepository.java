package com.stayfinder.repository;

import com.stayfinder.model.ViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewEventRepository extends JpaRepository<ViewEvent, Long> {

    List<ViewEvent> findByUserId(Long userId);
}
